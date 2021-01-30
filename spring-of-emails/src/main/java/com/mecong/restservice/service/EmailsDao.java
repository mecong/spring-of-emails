package com.mecong.restservice.service;

import com.mecong.restservice.model.EmailData;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.support.JdbcDaoSupport;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Repository
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class EmailsDao extends JdbcDaoSupport {
    EmailDataRowMapper emailDataRowMapper = new EmailDataRowMapper();

    @Autowired
    public EmailsDao(DataSource dataSource) {
        setDataSource(dataSource);
    }

    public List<EmailData> getEmailsCounts() {
        return getJdbcTemplate().query("select * from emails", emailDataRowMapper);
    }

    public List<EmailData> getEmailCounts(String email) {
        return getJdbcTemplate().query("select * from emails where email=?", emailDataRowMapper, email);
    }

    public void persistEmails(Map<String, String> emails, String currentBatch) {
        List<Map.Entry<String, String>> listDataToInsert = new ArrayList<>(emails.entrySet());

        BatchPreparedStatementSetter batchInsert = new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int index) throws SQLException {
                String email = listDataToInsert.get(index).getKey();
                String count = listDataToInsert.get(index).getValue();

                ps.setInt(1, Integer.parseInt(currentBatch));
                ps.setString(2, email);
                ps.setInt(3, Integer.parseInt(count));
                log.debug("persist {}:{}->{}", currentBatch, email, count);
            }

            @Override
            public int getBatchSize() {
                return emails.size();
            }
        };

        getJdbcTemplate().batchUpdate("insert into emails(batch, email, count) values(?, ?, ?) ", batchInsert);
    }

    private static class EmailDataRowMapper implements RowMapper<EmailData> {
        @Override
        public EmailData mapRow(ResultSet resultSet, int rowNum) throws SQLException {
            return EmailData.builder()
                    .batch(resultSet.getInt(1))
                    .email(resultSet.getString(2))
                    .count(resultSet.getInt(3))
                    .build();
        }
    }
}

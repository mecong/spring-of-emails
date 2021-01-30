package com.mecong.restservice.service;

import com.mecong.restservice.model.EmailData;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@AllArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class EmailsService {
    EmailsDao emailsDao;

    public Map<String, Map<Integer, Integer>> getEmailsCounts() {
        return getBatchesPerEmail(emailsDao.getEmailsCounts());
    }

    public Map<String, Map<Integer, Integer>> getEmailCounts(String email) {
        return getBatchesPerEmail(emailsDao.getEmailCounts(email));
    }

    private Map<String, Map<Integer, Integer>> getBatchesPerEmail(List<EmailData> emailsData) {
        Map<String, Map<Integer, Integer>> accum = new HashMap<>();
        emailsData.forEach(emailData -> {
            Map<Integer, Integer> batchCountMap = accum.computeIfAbsent(emailData.getEmail(), k -> new HashMap<>());
            batchCountMap.put(emailData.getBatch(), emailData.getCount());
        });
        return accum;
    }
}

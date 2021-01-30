package com.mecong.restservice.service;

import com.mecong.restservice.configuration.AppProperties;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.data.redis.core.*;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class EmailProcessorService implements InitializingBean {
    public static final String SPE_CURRENT_BATCH = "spe:current-batch";
    public static final String SPE_LOCK = "spe:current-batch-lock";
    public static final String SPE_CURRENT_EMAILS = "spe:current-emails";
    public static final String SPE_CURRENT_EMAILS_PERSISTING = "spe:current-emails:persisting";

    final EmailValidator emailValidator;
    final StringRedisTemplate redisTemplate;
    final EmailsDao emailsDao;
    final AppProperties appProperties;

    HashOperations<String, String, String> hashOps;
    ValueOperations<String, String> valueOps;

    public boolean processEmail(String email) {
        if (emailValidator.emailValid(email)) {

            if (batchTimeExpired() && tryToSetLock()) {
                startNewBatch();
            }

            hashOps.increment(SPE_CURRENT_EMAILS, email, 1);

            return true;
        } else {
            return false;
        }
    }

    private Boolean tryToSetLock() {
        return valueOps.setIfAbsent(SPE_LOCK, appProperties.getConsumerName(), appProperties.getBatchTimeoutMinutes(), TimeUnit.MINUTES);
    }

    private void startNewBatch() {

        String currentBatch = valueOps.get(SPE_CURRENT_BATCH);

        List<Object> newBatchTransaction = redisTemplate.execute(new SessionCallback<>() {
            @Override
            public <K, V> List<Object> execute(RedisOperations<K, V> operations) {
                redisTemplate.watch(SPE_CURRENT_BATCH);
                operations.multi();
                if (atLeastOneBatchStarted(currentBatch)) {
                    redisTemplate.rename(SPE_CURRENT_EMAILS, SPE_CURRENT_EMAILS_PERSISTING);
                }
                valueOps.increment(SPE_CURRENT_BATCH);
                return operations.exec();
            }
        });

        analiseTransaction(currentBatch, newBatchTransaction);
    }

    private void analiseTransaction(String currentBatch, List<Object> newBatchTransaction) {
        if (transactionIsSuccessful(newBatchTransaction) && atLeastOneBatchStarted(currentBatch)) {
            emailsDao.persistEmails(hashOps.entries(SPE_CURRENT_EMAILS_PERSISTING), currentBatch);
            redisTemplate.delete(SPE_CURRENT_EMAILS_PERSISTING);
        }
    }

    private boolean batchTimeExpired() {
        return valueOps.get(SPE_LOCK) == null;
    }

    private boolean atLeastOneBatchStarted(String currentBatch) {
        return currentBatch != null;
    }

    private boolean transactionIsSuccessful(List<Object> newBatchTransaction) {
        return newBatchTransaction != null && !newBatchTransaction.isEmpty();
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        hashOps = redisTemplate.opsForHash();
        valueOps = redisTemplate.opsForValue();
    }
}

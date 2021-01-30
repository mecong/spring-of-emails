package com.mecong.restservice.service;

import com.mecong.restservice.configuration.AppProperties;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.stream.StreamRecords;
import org.springframework.data.redis.connection.stream.StringRecord;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

import static com.mecong.restservice.service.UrlsStreamConsumer.URL_KEY;

@Service
@Slf4j
@AllArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class UrlProcessorService {
    UrlValidator urlValidator;
    AppProperties appProperties;
    StringRedisTemplate redisTemplate;

    public void processUrl(String url) {
        if (urlValidator.validateUrl(url)) {
            Map<String, String> fields = new HashMap<>();
            fields.put(URL_KEY, url);
            StringRecord record = StreamRecords.string(fields).withStreamKey(appProperties.getUrlsStreamName());
            redisTemplate.opsForStream().add(record);
            log.info("New URL published: {}", url);
        }
    }
}

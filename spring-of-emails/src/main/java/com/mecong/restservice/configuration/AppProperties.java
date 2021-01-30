package com.mecong.restservice.configuration;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import java.util.UUID;

@Setter
@Getter
@Configuration
@ConfigurationProperties(prefix = "app")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AppProperties {
    @Min(1)
    @Max(10)
    int urlFetchRetryCount;

    @Min(1)
    int batchTimeoutMinutes;

    String consumerGroupName = "application";

    String consumerName = UUID.randomUUID().toString();

    String urlsStreamName = "spe:urls-stream";

    long streamPollTimeout = 1000;
}

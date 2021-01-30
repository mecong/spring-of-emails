package com.mecong.restservice.service;

import com.mecong.restservice.configuration.AppProperties;
import io.lettuce.core.api.async.RedisAsyncCommands;
import io.lettuce.core.codec.StringCodec;
import io.lettuce.core.output.StatusOutput;
import io.lettuce.core.protocol.CommandArgs;
import io.lettuce.core.protocol.CommandKeyword;
import io.lettuce.core.protocol.CommandType;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.data.redis.connection.stream.Consumer;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.ReadOffset;
import org.springframework.data.redis.connection.stream.StreamOffset;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.stream.StreamListener;
import org.springframework.data.redis.stream.StreamMessageListenerContainer;
import org.springframework.data.redis.stream.Subscription;
import org.springframework.remoting.RemoteAccessException;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Component;

import javax.xml.stream.XMLStreamException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UrlsStreamConsumer implements StreamListener<String, MapRecord<String, String, String>>, InitializingBean, DisposableBean {

    public static final String URL_KEY = "ulr-key";

    final AppProperties config;
    final StringRedisTemplate redisTemplate;
    final AppProperties appProperties;
    final XMLProcessor xmlProcessor;

    StreamMessageListenerContainer<String, MapRecord<String, String, String>> listenerContainer;
    Subscription subscription;


    @Override
    public void onMessage(MapRecord<String, String, String> message) {
        //extract the number from the message
        try {
            String urlStringToProcess = message.getValue().get(URL_KEY);
            log.info("Received url to process: {}", urlStringToProcess);
            processUrl(urlStringToProcess);
            redisTemplate.opsForStream().acknowledge(config.getConsumerGroupName(), message);
            log.info("Message has been processed");
        } catch (Exception ex) {
            //log the exception and increment the number of errors count
            log.error("Failed to process the message: {} ", message.getValue().get(URL_KEY), ex);
        }
    }

    private void processUrl(String urlString) {
        try {
            URL url = new URL(urlString);

            RetryTemplate template = RetryTemplate.builder()
                    .maxAttempts(appProperties.getUrlFetchRetryCount())
                    .fixedBackoff(1000)
                    .retryOn(RemoteAccessException.class)
                    .build();

            template.execute(ctx -> {
                try {
                    InputStream inputStream = url.openStream();
                    xmlProcessor.processXMLInput(inputStream);
                } catch (IOException | XMLStreamException e) {
                    log.error(e.getMessage(), e);
                    throw new RemoteAccessException(e.getMessage());
                }
                return null;
            });
        } catch (MalformedURLException e) {
            log.error(e.getMessage(), e);
        }
    }

    @Override
    public void destroy() throws Exception {
        if (subscription != null) {
            subscription.cancel();
        }

        if (listenerContainer != null) {
            listenerContainer.stop();
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        //name for this consumer which will be registered with consumer group
        String consumerName = config.getConsumerName();
        String consumerGroupName = config.getConsumerGroupName();
        String streamName = config.getUrlsStreamName();

        try {
            //create consumer group for the stream
            // if stream does not exist it will create stream first then create consumer group
            if (!redisTemplate.hasKey(streamName)) {
                log.info("{} does not exist. Creating stream along with the consumer group", streamName);
                RedisAsyncCommands<String, String> commands =
                        (RedisAsyncCommands) redisTemplate.getConnectionFactory().getConnection().getNativeConnection();
                CommandArgs<String, String> args = new CommandArgs<>(StringCodec.UTF8)
                        .add(CommandKeyword.CREATE)
                        .add(streamName)
                        .add(consumerGroupName)
                        .add("0")
                        .add("MKSTREAM");
                commands.dispatch(CommandType.XGROUP, new StatusOutput<>(StringCodec.UTF8), args);
            } else {
                //creating consumer group
                redisTemplate.opsForStream().createGroup(streamName, ReadOffset.from("0"), consumerGroupName);
            }
        } catch (Exception ex) {
            log.info("Consumer group already present: {}", consumerGroupName);
        }


        this.listenerContainer = StreamMessageListenerContainer.create(redisTemplate.getConnectionFactory(),
                StreamMessageListenerContainer
                        .StreamMessageListenerContainerOptions.builder()
                        .pollTimeout(Duration.ofMillis(config.getStreamPollTimeout()))
                        .build());

        this.subscription = listenerContainer.receive(
                Consumer.from(consumerGroupName, consumerName),
                StreamOffset.create(streamName, ReadOffset.lastConsumed()),
                this);

        subscription.await(Duration.ofSeconds(2));
        listenerContainer.start();
    }
}
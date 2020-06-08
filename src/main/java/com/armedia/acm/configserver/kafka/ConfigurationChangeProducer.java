package com.armedia.acm.configserver.kafka;

import com.armedia.acm.configserver.config.KafkaTopicsProperties;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;

@Service
public class ConfigurationChangeProducer
{
    private final Logger logger = LogManager.getLogger(ConfigurationChangeProducer.class);

    private final KafkaTopicsProperties kafkaTopicsProperties;
    private final KafkaTemplate<String, String> configurationChangeKafkaTemplate;

    public ConfigurationChangeProducer(KafkaTopicsProperties kafkaTopicsProperties,
            KafkaTemplate<String, String> configurationChangeKafkaTemplate)
    {
        this.kafkaTopicsProperties = kafkaTopicsProperties;
        this.configurationChangeKafkaTemplate = configurationChangeKafkaTemplate;
    }

    public void sendConfigurationChangedMessage()
    {
        ListenableFuture<SendResult<String, String>> future = configurationChangeKafkaTemplate.send(kafkaTopicsProperties.getConfigurationChangedTopic(), "Configuration changed");
        future.addCallback(callback);
    }

    public void sendLabelsChangedMessage()
    {
        ListenableFuture<SendResult<String, String>> future = configurationChangeKafkaTemplate.send(kafkaTopicsProperties.getLabelsChangedTopic(), "Labels changed");
        future.addCallback(callback);
    }

    public void sendLdapChangedMessage()
    {
        ListenableFuture<SendResult<String, String>> future = configurationChangeKafkaTemplate.send(kafkaTopicsProperties.getLdapChangedTopic(), "Ldap changed");
        future.addCallback(callback);
    }

    private ListenableFutureCallback<SendResult<String, String>> callback = new ListenableFutureCallback<SendResult<String, String>>()
    {
        @Override
        public void onSuccess(SendResult<String, String> result)
        {
            logger.debug("Sent message=[{}] with offset=[{}]", result.toString(), result.getRecordMetadata().offset());
        }

        @Override
        public void onFailure(Throwable ex)
        {
            logger.error("Unable to send message due to [{}]", ex.getMessage());
        }
    };
}

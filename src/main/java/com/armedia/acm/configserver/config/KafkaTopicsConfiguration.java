package com.armedia.acm.configserver.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.config.TopicConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicsConfiguration
{
    private final KafkaTopicsProperties kafkaTopicsProperties;

    public KafkaTopicsConfiguration(KafkaTopicsProperties kafkaTopicsProperties)
    {
        this.kafkaTopicsProperties = kafkaTopicsProperties;
    }

    @Bean
    public NewTopic configurationChangedTopic()
    {
        return TopicBuilder.name(kafkaTopicsProperties.getConfigurationChangedTopic())
                .partitions(kafkaTopicsProperties.getConfigurationChangedTopicPartitions())
                .replicas(kafkaTopicsProperties.getConfigurationChangedTopicReplicas())
                .config(TopicConfig.RETENTION_MS_CONFIG, kafkaTopicsProperties.getRetentionMs())
                .build();
    }

    @Bean
    public NewTopic labelsChangedTopic()
    {
        return TopicBuilder.name(kafkaTopicsProperties.getLabelsChangedTopic())
                .partitions(kafkaTopicsProperties.getLabelsChangedTopicPartitions())
                .replicas(kafkaTopicsProperties.getLabelsChangedTopicReplicas())
                .config(TopicConfig.RETENTION_MS_CONFIG, kafkaTopicsProperties.getRetentionMs())
                .build();
    }

    @Bean
    public NewTopic ldapChangedTopic()
    {
        return TopicBuilder.name(kafkaTopicsProperties.getLdapChangedTopic())
                .partitions(kafkaTopicsProperties.getLdapChangedTopicPartitions())
                .replicas(kafkaTopicsProperties.getLdapChangedTopicReplicas())
                .config(TopicConfig.RETENTION_MS_CONFIG, kafkaTopicsProperties.getRetentionMs())
                .build();
    }

    @Bean
    public NewTopic configurationFileCreatedTopic()
    {
        return TopicBuilder.name(kafkaTopicsProperties.getConfigurationFileCreatedTopic())
                .partitions(kafkaTopicsProperties.getConfigurationFileCreatedTopicPartitions())
                .replicas(kafkaTopicsProperties.getConfigurationFileCreatedTopicReplicas())
                .config(TopicConfig.RETENTION_MS_CONFIG, kafkaTopicsProperties.getRetentionMs())
                .build();
    }
}

package com.armedia.acm.configserver.config;

/*-
 * #%L
 * acm-config-server
 * %%
 * Copyright (C) 2019 - 2020 ArkCase LLC
 * %%
 * This file is part of the ArkCase software. 
 * 
 * If the software was purchased under a paid ArkCase license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
 * 
 * ArkCase is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *  
 * ArkCase is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with ArkCase. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

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
    public NewTopic lookupsChangedTopic()
    {
        return TopicBuilder.name(kafkaTopicsProperties.getLookupsChangedTopic())
                .partitions(kafkaTopicsProperties.getLookupsChangedTopicPartitions())
                .replicas(kafkaTopicsProperties.getLookupsChangedTopicReplicas())
                .config(TopicConfig.RETENTION_MS_CONFIG, kafkaTopicsProperties.getRetentionMs())
                .build();
    }

    @Bean
    public NewTopic rulesChangedTopic()
    {
        return TopicBuilder.name(kafkaTopicsProperties.getRulesChangedTopic())
                .partitions(kafkaTopicsProperties.getRulesChangedTopicPartitions())
                .replicas(kafkaTopicsProperties.getRulesChangedTopicReplicas())
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

    @Bean
    public NewTopic avroSchemaFileConfigurationTopic()
    {
        return TopicBuilder.name(kafkaTopicsProperties.getAvroSchemaFileTopic())
                .partitions(kafkaTopicsProperties.getAvroSchemaFileTopicPartitions())
                .replicas(kafkaTopicsProperties.getAvroSchemaFileTopicReplicas())
                .config(TopicConfig.RETENTION_MS_CONFIG, kafkaTopicsProperties.getRetentionMs())
                .build();
    }

    @Bean
    public NewTopic formSchemaFileConfigurationTopic()
    {
        return TopicBuilder.name(kafkaTopicsProperties.getFormSchemaFileTopic())
                .partitions(kafkaTopicsProperties.getFormSchemaFileTopicPartitions())
                .replicas(kafkaTopicsProperties.getFormSchemaFileTopicReplicas())
                .config(TopicConfig.RETENTION_MS_CONFIG, kafkaTopicsProperties.getRetentionMs())
                .build();
    }
}

package com.armedia.acm.configserver.kafka;

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

import com.armedia.acm.configserver.config.KafkaTopicsProperties;

import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
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

    public ConfigurationChangeProducer(KafkaTopicsProperties kafkaTopicsProperties)
    {
        this.kafkaTopicsProperties = kafkaTopicsProperties;

        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaTopicsProperties.getBootstrapAddress());
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);

        configProps.put("security.protocol", kafkaTopicsProperties.getSecurityProtocol());
        
        this.configurationChangeKafkaTemplate = new KafkaTemplate<>(new DefaultKafkaProducerFactory<>(configProps));
    }

    public void sendConfigurationChangedMessage()
    {
        ListenableFuture<SendResult<String, String>> future = configurationChangeKafkaTemplate
                .send(kafkaTopicsProperties.getConfigurationChangedTopic(), "Configuration changed");
        future.addCallback(callback);
    }

    public void sendLabelsChangedMessage()
    {
        ListenableFuture<SendResult<String, String>> future = configurationChangeKafkaTemplate
                .send(kafkaTopicsProperties.getLabelsChangedTopic(), "Labels changed");
        future.addCallback(callback);
    }

    public void sendLdapChangedMessage()
    {
        ListenableFuture<SendResult<String, String>> future = configurationChangeKafkaTemplate
                .send(kafkaTopicsProperties.getLdapChangedTopic(), "Ldap changed");
        future.addCallback(callback);
    }

    public void sendRulesChangedMessage(String filePath)
    {
        ListenableFuture<SendResult<String, String>> future = configurationChangeKafkaTemplate
                .send(kafkaTopicsProperties.getRulesChangedTopic(), filePath);
        future.addCallback(callback);
    }

    public void sendLookupsChangedMessage()
    {
        ListenableFuture<SendResult<String, String>> future = configurationChangeKafkaTemplate
                .send(kafkaTopicsProperties.getLookupsChangedTopic(), "Lookups changed");
        future.addCallback(callback);
    }

    public void sendFormsChangedMessage(String fileName)
    {
        ListenableFuture<SendResult<String, String>> future = configurationChangeKafkaTemplate
                .send(kafkaTopicsProperties.getFormsChangedTopic(), fileName);
        future.addCallback(callback);
    }

    public void sendMenuChangedMessage(String fileName)
    {
        ListenableFuture<SendResult<String, String>> future = configurationChangeKafkaTemplate
                .send(kafkaTopicsProperties.getMenuChangedTopic(), fileName);
        future.addCallback(callback);
    }

    public void sendQueryChangedMessage(String fileName)
    {
        ListenableFuture<SendResult<String, String>> future = configurationChangeKafkaTemplate
                .send(kafkaTopicsProperties.getQueryChangedTopic(), fileName);
        future.addCallback(callback);
    }

    public void sendPermissionChangeMessage(String fileName)
    {
        ListenableFuture<SendResult<String, String>> future = configurationChangeKafkaTemplate
                .send(kafkaTopicsProperties.getPermissionsChangedTopic(), fileName);
        future.addCallback(callback);
    }

    public void sendConfigurationFileCreatedMessage(String message)
    {
        ListenableFuture<SendResult<String, String>> future = configurationChangeKafkaTemplate
                .send(kafkaTopicsProperties.getConfigurationFileCreatedTopic(), message);
        future.addCallback(callback);
    }

    public void sendAvroSchemasFileMessage(String message, String messageKey)
    {
        ListenableFuture<SendResult<String, String>> future = configurationChangeKafkaTemplate
                .send(kafkaTopicsProperties.getAvroSchemaFileTopic(), messageKey, message);
        future.addCallback(callback);
    }

    public void sendFormSchemasFileMessage(String message, String messageKey)
    {
        ListenableFuture<SendResult<String, String>> future = configurationChangeKafkaTemplate
                .send(kafkaTopicsProperties.getFormSchemaFileTopic(), messageKey, message);
        future.addCallback(callback);
    }

    public void sendMenuSchemasFileMessage(String message, String messageKey)
    {
        ListenableFuture<SendResult<String, String>> future = configurationChangeKafkaTemplate
                .send(kafkaTopicsProperties.getMenuSchemaFileTopic(), messageKey, message);
        future.addCallback(callback);
    }

    public void sendQuerySchemasFileMessage(String message, String messageKey)
    {
        ListenableFuture<SendResult<String, String>> future = configurationChangeKafkaTemplate
                .send(kafkaTopicsProperties.getQuerySchemaFileTopic(),messageKey, message);
        future.addCallback(callback);
    }

    public void sendProcessesFileMessage(String message, String messageKey)
    {
        ListenableFuture<SendResult<String, String>> future = configurationChangeKafkaTemplate
                .send(kafkaTopicsProperties.getProcessFileTopic(), messageKey, message);
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

package com.armedia.acm.configserver.jms;

import org.apache.activemq.ActiveMQSslConnectionFactory;
import org.apache.activemq.command.ActiveMQTopic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.core.JmsTemplate;

@Configuration
public class ActiveMqFactory
{
    private ActiveMqConfiguration configuration;

    private static final Logger logger = LoggerFactory.getLogger(ActiveMqFactory.class);

    public ActiveMqFactory(ActiveMqConfiguration configuration)
    {
        this.configuration = configuration;
    }

    @Bean
    public ActiveMQSslConnectionFactory activeMQSslConnectionFactory()
    {
        ActiveMQSslConnectionFactory factory = new ActiveMQSslConnectionFactory();
        factory.setBrokerURL(configuration.getBrokerUrl());
        try
        {
            factory.setTrustStore(configuration.getTruststore());
            factory.setTrustStorePassword(configuration.getTruststorePassword());
            factory.setKeyStore(configuration.getKeystore());
            factory.setKeyStorePassword(configuration.getKeystorePassword());
        }
        catch (Exception e)
        {
            logger.warn("Can't load truststore or keystore. {}", e.getMessage());
        }
        return factory;
    }

    @Bean
    public JmsTemplate acmJmsTemplate()
    {
        JmsTemplate jmsTemplate = new JmsTemplate();
        jmsTemplate.setConnectionFactory(activeMQSslConnectionFactory());
        jmsTemplate.setDefaultDestination(new ActiveMQTopic(configuration.getDefaultDestination()));
        return jmsTemplate;
    }
}

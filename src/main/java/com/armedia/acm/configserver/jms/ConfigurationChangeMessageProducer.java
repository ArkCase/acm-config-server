package com.armedia.acm.configserver.jms;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

import javax.jms.Session;

@Component
public class ConfigurationChangeMessageProducer
{
    private JmsTemplate jmsTemplate;

    private static final Logger logger = LoggerFactory.getLogger(ConfigurationChangeMessageProducer.class);

    public ConfigurationChangeMessageProducer(JmsTemplate jmsTemplate)
    {
        this.jmsTemplate = jmsTemplate;
    }

    public void sendMessage()
    {
        logger.info("Sending configuration change topic message...");
        jmsTemplate.send("configuration.changed", Session::createMessage);
    }
}

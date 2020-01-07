package com.armedia.acm.configserver.jms

import org.springframework.jms.core.JmsTemplate
import spock.lang.Specification

class ConfigurationChangeMessageProducerTest extends Specification {

    JmsTemplate jmsTemplate = Mock()

    def "should execute only single send message when 2 calls are made in window of 3 seconds"() {
        given:
        ConfigurationChangeMessageProducer messageProducer = new ConfigurationChangeMessageProducer(3, jmsTemplate)
        when:
        messageProducer.sendMessage()
        messageProducer.sendMessage()
        Thread.sleep(3000)
        then:
        1 * jmsTemplate.send(_, _)
    }

    def "should execute only two send message when 2 calls are made in window of more than 3 seconds"() {
        given:
        ConfigurationChangeMessageProducer messageProducer = new ConfigurationChangeMessageProducer(3, jmsTemplate)
        when:
        messageProducer.sendMessage()
        Thread.sleep(3500)
        messageProducer.sendMessage()
        Thread.sleep(3000)
        then:
        2 * jmsTemplate.send(_, _)
    }

}

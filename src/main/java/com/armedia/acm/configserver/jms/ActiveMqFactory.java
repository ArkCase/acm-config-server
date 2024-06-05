/*-
 * #%L
 * acm-config-server
 * %%
 * Copyright (C) 2019 ArkCase LLC
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
package com.armedia.acm.configserver.jms;

import java.time.Duration;

/*-
 * #%L
 * acm-config-server
 * %%
 * Copyright (C) 2019 ArkCase LLC
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
    private final ActiveMqConfiguration configuration;

    private static final Logger logger = LoggerFactory.getLogger(ActiveMqFactory.class);

    public ActiveMqFactory(ActiveMqConfiguration configuration)
    {
        this.configuration = configuration;
    }

    @Bean
    public ActiveMQSslConnectionFactory activeMQSslConnectionFactory()
    {
        ActiveMQSslConnectionFactory factory = new ActiveMQSslConnectionFactory();
        ActiveMqFactory.logger.info("Configuring ActiveMQ with configuration [{}]", this.configuration);
        factory.setBrokerURL(this.configuration.getBrokerUrl());
        final int timeout = (int) Duration.ofSeconds(this.configuration.getTimeout()).toMillis();
        try
        {
            factory.setTrustStore(this.configuration.getTruststore());
            factory.setTrustStorePassword(this.configuration.getTruststorePassword());
            factory.setKeyStore(this.configuration.getKeystore());
            factory.setKeyStorePassword(this.configuration.getKeystorePassword());
            factory.setSendTimeout(timeout);
            factory.setConnectResponseTimeout(timeout);
            factory.setUserName(this.configuration.getUser());
            factory.setPassword(this.configuration.getPassword());
        }
        catch (Exception e)
        {
            ActiveMqFactory.logger.warn("Can't load truststore or keystore. {}", e.getMessage());
        }
        return factory;
    }

    @Bean
    public JmsTemplate acmJmsTemplate()
    {
        JmsTemplate jmsTemplate = new JmsTemplate();
        jmsTemplate.setConnectionFactory(activeMQSslConnectionFactory());
        jmsTemplate.setDefaultDestination(new ActiveMQTopic(this.configuration.getDefaultDestination()));
        return jmsTemplate;
    }
}
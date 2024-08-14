package com.armedia.acm.configserver.service;

/*-
 * #%L
 * acm-config-server
 * %%
 * Copyright (C) 2019 - 2024 ArkCase LLC
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

import org.apache.activemq.command.ActiveMQTopic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;

import javax.jms.DeliveryMode;

@Service
public class NotificationService
{
    public static final String VIRTUAL_TOPIC_CONFIG_FILE_UPDATED = "VirtualTopic.ConfigFileUpdated";
    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);

    private final JmsTemplate acmJmsTemplate;

    public NotificationService(JmsTemplate acmJmsTemplate)
    {
        this.acmJmsTemplate = acmJmsTemplate;
    }

    public void sendNotification(String message, String destination)
    {
        ActiveMQTopic topic = new ActiveMQTopic(destination);

        this.acmJmsTemplate.setDeliveryMode(DeliveryMode.PERSISTENT);
        this.acmJmsTemplate.send(topic, inJmsSession -> inJmsSession.createTextMessage(message));

        NotificationService.logger.debug("File with name {} is updated and success message is sent for updating", message);

    }
}

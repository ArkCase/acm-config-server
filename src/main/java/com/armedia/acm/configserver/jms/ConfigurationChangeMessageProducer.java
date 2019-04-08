package com.armedia.acm.configserver.jms;

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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

import javax.jms.Session;
import java.time.LocalDateTime;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Component
public class ConfigurationChangeMessageProducer
{
    private JmsTemplate acmJmsTemplate;

    private final int delayInSeconds;

    private LocalDateTime lastSendTime;

    private final ScheduledExecutorService scheduledExecutorService;

    private static final Logger logger = LoggerFactory.getLogger(ConfigurationChangeMessageProducer.class);

    public ConfigurationChangeMessageProducer(@Value("${jms.message.buffer.window}") int delayInSeconds, JmsTemplate acmJmsTemplate)
    {
        this.acmJmsTemplate = acmJmsTemplate;
        this.delayInSeconds = delayInSeconds;
        scheduledExecutorService = Executors.newScheduledThreadPool(1);
        lastSendTime = LocalDateTime.MIN;
    }

    public void sendMessage()
    {
        LocalDateTime now = LocalDateTime.now();
        logger.debug("Last configuration change topic message send in [{}]", lastSendTime);
        if (now.isAfter(lastSendTime.plusSeconds(delayInSeconds)))
        {
            lastSendTime = now;
            logger.debug("Schedule configuration changed message in [{}] seconds", delayInSeconds);
            scheduledExecutorService.schedule(() -> {
                        logger.info("Sending configuration change topic message...");
                        acmJmsTemplate.send(Session::createMessage);
                    },
                    delayInSeconds, TimeUnit.SECONDS);
        }
    }
}

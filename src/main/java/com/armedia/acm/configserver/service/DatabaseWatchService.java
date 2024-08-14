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

import com.armedia.acm.configserver.jms.ConfigurationChangeMessageProducer;
import com.armedia.acm.configserver.model.DatabaseChangeEvent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

@Service
@Profile("run-with-db")
public class DatabaseWatchService
{
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final Map<String, Consumer<DatabaseChangeEvent>> handlers;
    private final Consumer<DatabaseChangeEvent> defaultHandler;

    public DatabaseWatchService(
            @Value("${acm.activemq.labels-destination}") final String labelsDestination,
            @Value("${acm.activemq.ldap-destination}") final String ldapDestination,
            @Value("${acm.activemq.lookups-destination}") final String lookupsDestination,
            @Value("${acm.activemq.rules-destination}") final String rulesDestination,
            @Value("${acm.activemq.default-destination}") final String configurationChangedDestination,
            @Value("${acm.activemq.permissions-destination}") final String permissionsChangedDestination,
            final ConfigurationChangeMessageProducer configurationChangeMessageProducer)
    {

        this.logger.debug("Initializing DatabaseWatchService");

        // Define the handlers similar to FileWatchService
        this.handlers = Collections.unmodifiableMap(new HashMap<>()
        {
            private static final long serialVersionUID = 1L;

            {
                put("ldap", (event) -> configurationChangeMessageProducer
                        .sendMessage(ldapDestination));

                put("labels", (event) -> configurationChangeMessageProducer
                        .sendMessage(labelsDestination));

                put("lookups", (event) -> configurationChangeMessageProducer
                        .sendMessage(lookupsDestination));
            }
        });

        // Default handler
        this.defaultHandler = (event) -> configurationChangeMessageProducer
                .sendMessage(configurationChangedDestination);
    }

    @EventListener
    @TransactionalEventListener
    public void handleDatabaseChangeEvent(DatabaseChangeEvent event)
    {
        this.logger.info("Database change detected: {}", event.getDetails());
        this.handlers.getOrDefault(event.getChangeType(), this.defaultHandler).accept(event);
    }
}

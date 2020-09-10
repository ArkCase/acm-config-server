package com.armedia.acm.configserver.service;

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

import com.armedia.acm.configserver.config.KafkaTopicsProperties;
import com.armedia.acm.configserver.kafka.ConfigurationChangeProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.bus.endpoint.RefreshBusEndpoint;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.time.LocalDateTime;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
public class FileWatchService
{
    private final String propertiesFolderPath;

    private final RefreshBusEndpoint refreshBusEndpoint;

    private final ConfigurationChangeProducer configurationChangeProducer;

    private final KafkaTopicsProperties kafkaTopicsProperties;

    private final ScheduledExecutorService executorService;

    private LocalDateTime lastSendTime;

    private static final Logger logger = LoggerFactory.getLogger(FileWatchService.class);

    public FileWatchService(@Value("${properties.folder.path}") String propertiesFolderPath,
            RefreshBusEndpoint refreshBusEndpoint,
            ConfigurationChangeProducer configurationChangeProducer,
            KafkaTopicsProperties kafkaTopicsProperties, ScheduledExecutorService executorService)
    {
        this.propertiesFolderPath = propertiesFolderPath;
        this.refreshBusEndpoint = refreshBusEndpoint;
        this.configurationChangeProducer = configurationChangeProducer;
        this.kafkaTopicsProperties = kafkaTopicsProperties;
        this.executorService = executorService;

        lastSendTime = LocalDateTime.MIN;

        logger.debug("Initializing FileWatchService");
    }

    @Async
    public void monitor()
    {
        try
        {
            WatchService watchService = FileSystems.getDefault().newWatchService();
            Path path = Paths.get(propertiesFolderPath);
            path.register(watchService, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_MODIFY, StandardWatchEventKinds.ENTRY_DELETE);

            Path labelsPath = Paths.get(propertiesFolderPath + "/labels");
            labelsPath.register(watchService, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_MODIFY, StandardWatchEventKinds.ENTRY_DELETE);

            Path ldapPath = Paths.get(propertiesFolderPath + "/ldap");
            ldapPath.register(watchService, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_MODIFY, StandardWatchEventKinds.ENTRY_DELETE);

            Path lookupsPath = Paths.get(propertiesFolderPath + "/lookups");
            lookupsPath.register(watchService, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_MODIFY, StandardWatchEventKinds.ENTRY_DELETE);

            Path rulesPath = Paths.get(propertiesFolderPath + "/rules");
            rulesPath.register(watchService, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_MODIFY, StandardWatchEventKinds.ENTRY_DELETE);

            WatchKey key;
            while (true)
            {
                try
                {
                    logger.debug("Waiting for file change on path [{}]", path.toString());
                    if ((key = watchService.take()) != null)
                    {
                        logger.debug("Watch key event present...");
                        for (WatchEvent<?> event : key.pollEvents())
                        {
                            Path filePath = (Path) event.context();
                            File modifiedFile = filePath.toFile();
                            logger.info("Configuration file [{}] in folder [{}] has been updated!", modifiedFile, propertiesFolderPath);
                            String parentDirectory = key.watchable().toString();
                            refreshConfiguration(parentDirectory);
                        }
                        key.reset();
                        logger.debug("Reset watch key...");
                    }
                }
                catch (Exception e)
                {
                    logger.error("Monitoring folder [{}] failed. {}", propertiesFolderPath, e.getMessage());
                    logger.trace("Cause: ", e);
                }
            }
        }
        catch (IOException e)
        {
            logger.error("Error trying to find folder path [{}]. {}", propertiesFolderPath, e.getMessage());
        }
    }

    private void refreshConfiguration(String parentDirectory) {
        LocalDateTime now = LocalDateTime.now();
        logger.debug("Last configuration change topic message send in [{}]", lastSendTime);
        if (now.isAfter(lastSendTime.plusSeconds(kafkaTopicsProperties.getMessageBufferWindow())))
        {
            lastSendTime = now;
            logger.debug("Schedule configuration changed message in [{}] seconds", lastSendTime.plusSeconds(kafkaTopicsProperties.getMessageBufferWindow()));
            executorService.schedule(() -> {
                // Send message to ArkCase to update its configuration
                if(parentDirectory.contains("ldap"))
                {
                    configurationChangeProducer.sendLdapChangedMessage();
                }
                else if (parentDirectory.contains("labels"))
                {
                    configurationChangeProducer.sendLabelsChangedMessage();
                }
                else if (parentDirectory.contains("rules"))
                {
                    configurationChangeProducer.sendRulesChangedMessage();
                }
                else if (parentDirectory.contains("lookups"))
                {
                    configurationChangeProducer.sendLookupsChangedMessage();
                }
                else
                {
                    configurationChangeProducer.sendConfigurationChangedMessage();
                }

                // Send message to all subscribed nodes (mServices) in spring cloud bus to update their configuration
                refreshBusEndpoint.busRefresh();
            }, kafkaTopicsProperties.getMessageBufferWindow(), TimeUnit.SECONDS);
        }
    }
}

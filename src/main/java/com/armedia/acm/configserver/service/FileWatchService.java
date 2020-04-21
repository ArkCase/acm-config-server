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

import com.armedia.acm.configserver.jms.ConfigurationChangeMessageProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
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

@Service
public class FileWatchService
{
    private final String propertiesFolderPath;

    private final String labelsDestination;

    private final String ldapDestination;

    private final String configurationChangedDestination;

    private final ConfigurationChangeMessageProducer configurationChangeMessageProducer;

    private static final Logger logger = LoggerFactory.getLogger(FileWatchService.class);

    public FileWatchService(@Value("${properties.folder.path}") String propertiesFolderPath,
                            @Value("${acm.activemq.labels-destination}") String labelsDestination,
                            @Value("${acm.activemq.ldap-destination}") String ldapDestination,
                            @Value("${acm.activemq.default-destination}") String configurationChangedDestination,
                            ConfigurationChangeMessageProducer configurationChangeMessageProducer)
    {
        this.propertiesFolderPath = propertiesFolderPath;
        this.labelsDestination = labelsDestination;
        this.ldapDestination = ldapDestination;
        this.configurationChangedDestination = configurationChangedDestination;
        this.configurationChangeMessageProducer = configurationChangeMessageProducer;
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

                            if(parentDirectory.contains("ldap"))
                            {
                                configurationChangeMessageProducer.sendMessage(ldapDestination);
                            }
                            else if (parentDirectory.contains("labels"))
                            {
                                configurationChangeMessageProducer.sendMessage(labelsDestination);
                            }
                            else
                            {
                                configurationChangeMessageProducer.sendMessage(configurationChangedDestination);
                            }
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
}

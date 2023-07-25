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

import com.armedia.acm.configserver.jms.ActiveMQDestinationConfiguration;
import com.armedia.acm.configserver.jms.ActiveMqConfiguration;
import com.armedia.acm.configserver.jms.ConfigurationChangeMessageProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
public class FileWatchService
{
    private static final Logger logger = LoggerFactory.getLogger(FileWatchService.class);
    private final String propertiesFolderPath;
    @Autowired
    private final ActiveMQDestinationConfiguration activeMQDestinationConfiguration;

    private final ActiveMqConfiguration activeMqConfiguration;
    private final ScheduledExecutorService executorService;

    private final int messageBufferWindow;

    private final ConfigurationChangeMessageProducer configurationChangeMessageProducer;
    private Map<String, LocalDateTime> filesLastSendTime = new HashMap<>();

    public FileWatchService(@Value("${properties.folder.path}") String propertiesFolderPath,
                            @Value("${acm.activemq.messageBufferWindow}") int messageBufferWindow,
                            ActiveMQDestinationConfiguration activeMQDestinationConfiguration, ActiveMqConfiguration activeMqConfiguration,
                            ScheduledExecutorService executorService,
                            ConfigurationChangeMessageProducer configurationChangeMessageProducer)
    {
        this.propertiesFolderPath = propertiesFolderPath;
        this.activeMQDestinationConfiguration = activeMQDestinationConfiguration;
        this.messageBufferWindow = messageBufferWindow;
        this.activeMqConfiguration = activeMqConfiguration;
        this.executorService = executorService;
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
            registerConfigDirIncludingTheSubFolders(path, watchService);
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
                            String parentDirectory = key.watchable().toString();
                            File modifiedFile = new File(parentDirectory + File.separator + filePath.getFileName());
                            logger.info("Configuration file [{}] in folder [{}] has been updated!", modifiedFile, propertiesFolderPath);

                            if (!isTemporaryFile(modifiedFile))
                            {
                                refreshConfiguration(parentDirectory, modifiedFile.getAbsolutePath());
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

    private boolean isTemporaryFile(File modifiedFile) throws IOException
    {
        return modifiedFile.isHidden();
    }

    private void refreshConfiguration(String parentDirectory, String filePath)
    {
        LocalDateTime now = LocalDateTime.now();

        LocalDateTime lastSendTime = filesLastSendTime.get(filePath);
        lastSendTime = lastSendTime != null ? lastSendTime : LocalDateTime.MIN;

        logger.debug("Last configuration change topic message send in [{}]", lastSendTime);

        if (now.isAfter(lastSendTime.plusSeconds(messageBufferWindow)))
        {
            lastSendTime = now;
            filesLastSendTime.put(filePath, lastSendTime);
            logger.debug("Schedule configuration changed message in [{}] seconds",
                    lastSendTime.plusSeconds(messageBufferWindow));
            executorService.schedule(() -> {
                // Send message to ArkCase to update its configuration
                if (parentDirectory.contains("ldap"))
                {
                    configurationChangeMessageProducer.sendMessage(activeMQDestinationConfiguration.getLdapDestination());
                }
                else if (parentDirectory.contains("labels"))
                {
                    configurationChangeMessageProducer.sendMessage(activeMQDestinationConfiguration.getLabelsDestination());
                }
                else if (parentDirectory.contains("lookups"))
                {
                    configurationChangeMessageProducer.sendMessage(activeMQDestinationConfiguration.getLookupsDestination());
                }
                else if (parentDirectory.contains("rules"))
                {
                    configurationChangeMessageProducer.sendTextMessage(activeMQDestinationConfiguration.getRulesDestination(),
                            filePath.toString());
                }
                else if (parentDirectory.contains("permissions"))
                {
                    configurationChangeMessageProducer.sendTextMessage(activeMQDestinationConfiguration.getPermissionsDestination(),
                            filePath.toString());
                }
                else
                {
                    configurationChangeMessageProducer.sendMessage(activeMqConfiguration.getDefaultDestination());
                }

            }, messageBufferWindow, TimeUnit.SECONDS);
        }
    }


    private void registerConfigDirIncludingTheSubFolders(final Path root, WatchService watchService) throws IOException
    {
        Files.walkFileTree(root, new SimpleFileVisitor<Path>()
        {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException
            {
                dir.register(watchService, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_MODIFY,
                        StandardWatchEventKinds.ENTRY_DELETE);
                return FileVisitResult.CONTINUE;
            }
        });
    }
}

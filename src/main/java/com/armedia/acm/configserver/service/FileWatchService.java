package com.armedia.acm.configserver.service;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

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

@Service
public class FileWatchService
{
    private final String propertiesFolderPath;

    private final Map<String, Consumer<Path>> handlers;

    private final Consumer<Path> defaultHandler;

    private static final Logger logger = LoggerFactory.getLogger(FileWatchService.class);

    public FileWatchService(@Value("${properties.folder.path}") final String propertiesFolderPath,
            @Value("${acm.activemq.labels-destination}") final String labelsDestination,
            @Value("${acm.activemq.ldap-destination}") final String ldapDestination,
            @Value("${acm.activemq.lookups-destination}") final String lookupsDestination,
            @Value("${acm.activemq.rules-destination}") final String rulesDestination,
            @Value("${acm.activemq.default-destination}") final String configurationChangedDestination,
            @Value("${acm.activemq.permissions-destination}") final String permissionsChangedDestination,
            final ConfigurationChangeMessageProducer configurationChangeMessageProducer)
    {
        this.propertiesFolderPath = propertiesFolderPath;
        FileWatchService.logger.debug("Initializing FileWatchService");

        // Define the handlers we want ...
        this.handlers = Collections.unmodifiableMap(new HashMap<String, Consumer<Path>>()
        {
            private static final long serialVersionUID = 1L;

            {
                put("ldap", (filePath) -> configurationChangeMessageProducer
                        .sendMessage(ldapDestination));

                put("labels", (filePath) -> configurationChangeMessageProducer
                        .sendMessage(labelsDestination));

                put("rules",
                        (filePath) -> configurationChangeMessageProducer.sendTextMessage(
                                rulesDestination,
                                filePath.toString()));

                put("lookups", (filePath) -> configurationChangeMessageProducer
                        .sendMessage(lookupsDestination));

                put("permissions",
                        (filePath) -> configurationChangeMessageProducer.sendTextMessage(
                                permissionsChangedDestination,
                                filePath.toString()));
            }
        });

        // If none of the above match, we need a default handler...
        this.defaultHandler = (filePath) -> configurationChangeMessageProducer
                .sendMessage(configurationChangedDestination);
    }

    @Async
    public void monitor()
    {
        // Use try-with-resources with the watch service...
        try (WatchService watchService = FileSystems.getDefault().newWatchService())
        {
            Path path = Paths.get(this.propertiesFolderPath);
            path.register(watchService, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_MODIFY,
                    StandardWatchEventKinds.ENTRY_DELETE);

            // Register the handlers' watchers
            for (String k : this.handlers.keySet())
            {
                Path watchedPath = Paths.get(this.propertiesFolderPath, k);
                FileWatchService.logger.info("Registering the watcher for [{}]", watchedPath);
                watchedPath.register(watchService, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_MODIFY,
                        StandardWatchEventKinds.ENTRY_DELETE);
            }

            while (true)
            {
                try
                {
                    FileWatchService.logger.debug("Waiting for file change on path [{}]", path.toString());
                    WatchKey key = watchService.take();
                    if (key == null)
                    {
                        // Nothing to process, wait some more...
                        continue;
                    }

                    FileWatchService.logger.debug("Watch key event present...");
                    for (WatchEvent<?> event : key.pollEvents())
                    {
                        Path filePath = (Path) event.context();
                        String watchedPath = key.watchable().toString();
                        FileWatchService.logger.info("Configuration file [{}] in folder [{}] has been updated!", filePath,
                                watchedPath);
                        String parentDirectoryName = Paths.get(watchedPath).getFileName().toString();

                        FileWatchService.logger.debug("Launching the handler for [{}]", parentDirectoryName);
                        this.handlers.getOrDefault(parentDirectoryName, this.defaultHandler).accept(filePath);
                    }
                    key.reset();
                    FileWatchService.logger.debug("Reset watch key...");
                }
                catch (Exception e)
                {
                    FileWatchService.logger.error("Monitoring folder [{}] failed. {}", this.propertiesFolderPath, e.getMessage());
                    FileWatchService.logger.trace("Cause: ", e);
                }
            }
        }
        catch (IOException e)
        {
            FileWatchService.logger.error("Error trying to find folder path [{}]. {}", this.propertiesFolderPath, e.getMessage());
        }
    }
}

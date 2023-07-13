package com.armedia.acm.configserver.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
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

    private final String labelsDestination;

    private final String ldapDestination;

    private final String lookupsDestination;

    private final String rulesDestination;

    private final String configurationChangedDestination;

    private final ConfigurationChangeMessageProducer configurationChangeMessageProducer;

    private static final Logger logger = LoggerFactory.getLogger(FileWatchService.class);

    public FileWatchService(@Value("${properties.folder.path}") String propertiesFolderPath,
            @Value("${acm.activemq.labels-destination}") String labelsDestination,
            @Value("${acm.activemq.ldap-destination}") String ldapDestination,
            @Value("${acm.activemq.lookups-destination}") String lookupsDestination,
            @Value("${acm.activemq.rules-destination}") String rulesDestination,
            @Value("${acm.activemq.default-destination}") String configurationChangedDestination,
            ConfigurationChangeMessageProducer configurationChangeMessageProducer)
    {
        this.propertiesFolderPath = propertiesFolderPath;
        this.labelsDestination = labelsDestination;
        this.ldapDestination = ldapDestination;
        this.lookupsDestination = lookupsDestination;
        this.rulesDestination = rulesDestination;
        this.configurationChangedDestination = configurationChangedDestination;
        this.configurationChangeMessageProducer = configurationChangeMessageProducer;
        FileWatchService.logger.debug("Initializing FileWatchService");
    }

    private Object watch(final Path path, final Consumer<String> sender) throws Exception
    {
        try
        {
            final WatchService watchService = FileSystems.getDefault().newWatchService();
            path.register(watchService, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_MODIFY,
                    StandardWatchEventKinds.ENTRY_DELETE);
            while (true)
            {
                try
                {
                    FileWatchService.logger.debug("Waiting for file change on path [{}]", path.toString());
                    WatchKey key = watchService.take();

                    // Shouldn't happen, but be consistent with the old code
                    if (key == null)
                    {
                        continue;
                    }

                    FileWatchService.logger.debug("Watch key event present...");
                    for (WatchEvent<?> event : key.pollEvents())
                    {
                        Path filePath = (Path) event.context();
                        File modifiedFile = filePath.toFile();
                        FileWatchService.logger.info("Configuration file [{}] in folder [{}] has been updated!", modifiedFile,
                                this.propertiesFolderPath);
                        sender.accept(filePath.toString());
                    }
                    key.reset();
                    FileWatchService.logger.debug("Reset watch key...");
                }
                catch (InterruptedException e)
                {
                    // We've been asked to stop, so we should be good citizens
                    FileWatchService.logger.warn("Watcher for path [{}] has been interrupted", path);
                    return null;
                }
                catch (Exception e)
                {
                    FileWatchService.logger.error("Monitoring folder [{}] failed. {}", path, e.getMessage());
                    FileWatchService.logger.trace("Cause: ", e);
                }
            }
        }
        catch (final IOException e)
        {
            FileWatchService.logger.error("Error trying to set up the watch for path [{}]. {}", path, e.getMessage());
            FileWatchService.logger.trace("Cause: ", e);
            // Rethrow, for cleanliness
            throw e;
        }
    }

    @Async
    public void monitor()
    {
        List<Callable<?>> workers = new LinkedList<>();
        List<Future<?>> futures = new LinkedList<>();
        Path root = Paths.get(this.propertiesFolderPath);

        // We add the workers to a list first so we don't have to manually tell
        // the thread pool how big we want it to be. Thus, we can use the list's size
        // to determine that
        workers.add(() -> watch(root,
                (s) -> this.configurationChangeMessageProducer.sendMessage(this.configurationChangedDestination)));

        workers.add(() -> watch(root.resolve("labels"),
                (s) -> this.configurationChangeMessageProducer.sendMessage(this.labelsDestination)));

        workers.add(() -> watch(root.resolve("ldap"),
                (s) -> this.configurationChangeMessageProducer.sendMessage(this.ldapDestination)));

        workers.add(() -> watch(root.resolve("rules"),
                (s) -> this.configurationChangeMessageProducer.sendTextMessage(this.rulesDestination, s)));

        workers.add(() -> watch(root.resolve("lookups"),
                (s) -> this.configurationChangeMessageProducer.sendMessage(this.lookupsDestination)));

        // Just for S&G's ;)
        if (workers.isEmpty())
        {
            return;
        }

        final ExecutorService threads = Executors.newFixedThreadPool(workers.size());
        try
        {
            for (Callable<?> c : workers)
            {
                futures.add(threads.submit(c));
            }
            // No more workers allowed here
            threads.shutdown();

            for (Future<?> f : futures)
            {
                try
                {
                    f.get();
                }
                catch (InterruptedException | ExecutionException e)
                {
                    // We don't really care ... ;)
                }
            }
        }
        finally
        {
            threads.shutdownNow();
        }

    }
}

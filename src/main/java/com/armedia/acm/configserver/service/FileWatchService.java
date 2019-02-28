package com.armedia.acm.configserver.service;

import com.armedia.acm.configserver.jms.ConfigurationChangeMessageProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Service
public class FileWatchService
{
    private String propertiesFolderPath;

    private ConfigurationChangeMessageProducer configurationChangeMessageProducer;

    private static Logger logger = LoggerFactory.getLogger(FileWatchService.class);

    public FileWatchService(@Value("${properties.folder.path}") String propertiesFolderPath,
                            ConfigurationChangeMessageProducer configurationChangeMessageProducer)
    {
        this.propertiesFolderPath = propertiesFolderPath;
        this.configurationChangeMessageProducer = configurationChangeMessageProducer;
    }

    @Async
    public void monitor()
    {
        try
        {
            WatchService watchService = FileSystems.getDefault().newWatchService();
            Path path = Paths.get(propertiesFolderPath);
            path.register(watchService, StandardWatchEventKinds.ENTRY_MODIFY);

            Set<String> configurationFiles = Files.walk(path)
                    .map(Path::toFile)
                    .filter(File::isFile)
                    .map(File::getName)
                    .collect(Collectors.toSet());

            Predicate<String> isNotTemporaryFile = configurationFiles::contains;

            WatchKey key;
            while (true)
            {
                try
                {
                    if ((key = watchService.take()) != null)
                    {
                        for (WatchEvent<?> event : key.pollEvents())
                        {
                            Path filePath = (Path) event.context();
                            File modifiedFile = filePath.toFile();
                            if (isNotTemporaryFile.test(modifiedFile.getName()))
                            {
                                logger.info("Configuration file in folder [{}] has been updated!", propertiesFolderPath);
                                configurationChangeMessageProducer.sendMessage();
                            }

                        }
                        key.reset();
                    }
                }
                catch (Exception e)
                {
                    logger.error("Monitoring folder [{}] failed. {}", propertiesFolderPath, e.getMessage());
                }
            }
        }
        catch (IOException e)
        {
            logger.error("Error trying to find folder path [{}]. {}", propertiesFolderPath, e.getMessage());
        }
    }
}

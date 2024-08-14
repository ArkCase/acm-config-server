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

import static com.armedia.acm.configserver.service.ConfigurationService._RUNTIME;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

@Service
@Profile("!run-with-db")
@ConditionalOnMissingBean(MinioResourceStorageService.class)
public class FileSystemResourceStorageService implements ResourceStorageService
{

    private final String configServerRepo;
    private final NotificationService notificationService;
    private static final Logger logger = LoggerFactory.getLogger(FileSystemResourceStorageService.class);

    public FileSystemResourceStorageService(@Value("${properties.folder.path}") String configRepo, NotificationService notificationService)
    {
        this.configServerRepo = configRepo;
        this.notificationService = notificationService;
    }

    @Override
    public void moveFileToConfiguration(MultipartFile file, String fileName, boolean isBrandingFile) throws IOException
    {
        try (InputStream inputStream = file.getInputStream())
        {

            String originalFileName = getOriginalFileNameFromFilePath(fileName);

            String profileBasedFile = setProfileBasedResource(fileName);

            File destinationFile = new File(this.configServerRepo, profileBasedFile);

            FileUtils.copyInputStreamToFile(inputStream, destinationFile);

            FileSystemResourceStorageService.logger.info("File with name {} created on the config server", fileName);

            if (isBrandingFile)
            {
                this.notificationService.sendNotification(originalFileName,
                        NotificationService.VIRTUAL_TOPIC_CONFIG_FILE_UPDATED);
            }
        }
        catch (IOException e)
        {
            FileSystemResourceStorageService.logger.error("File can't be updated");
            throw e;
        }
    }

    private String getOriginalFileNameFromFilePath(String filePath)
    {
        String[] splitedFilePath = filePath.split("/");
        String originalFileName = splitedFilePath[splitedFilePath.length - 1];

        FileSystemResourceStorageService.logger.debug("Original file name from path {} is {}", filePath, originalFileName);

        return originalFileName;
    }

    private String setProfileBasedResource(String fileName)
    {
        return new StringBuilder(fileName).insert(fileName.indexOf("."), _RUNTIME).toString();
    }
}

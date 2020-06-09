package com.armedia.acm.configserver.service;

/*-
 * #%L
 * acm-config-server
 * %%
 * Copyright (C) 2019 - 2020 ArkCase LLC
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

import com.armedia.acm.configserver.kafka.ConfigurationChangeProducer;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.InputStream;

@Service
@Qualifier(value = "fileConfigurationService")
public class FileConfigurationService {

    private final String configServerRepo;

    private final ConfigurationChangeProducer configurationChangeProducer;

    private static final Logger logger = LoggerFactory.getLogger(FileConfigurationService.class);


    public FileConfigurationService(@Value("${properties.folder.path}") String configRepo,
            ConfigurationChangeProducer configurationChangeProducer)
    {
        this.configServerRepo = configRepo;
        this.configurationChangeProducer = configurationChangeProducer;
    }

    public void moveFileToConfiguration(MultipartFile file, String fileName) throws Exception {
        try (InputStream logoStream = file.getInputStream())
        {

            String originalFileName = getOriginalFileNameFromFilePath(fileName);

            String profileBasedfile = setProfileBasedResource(fileName);

            File logoFile = new File(configServerRepo + "/" + profileBasedfile);

            FileUtils.copyInputStreamToFile(logoStream, logoFile);

            logger.info("File is with name {} created on the config server", fileName);

            sendNotification(originalFileName);

        }
        catch (Exception e)
        {
            throw new Exception("Can't update logo file");
        }
    }

    private String getOriginalFileNameFromFilePath(String filePath)
    {
        String[] splitedFilePath = filePath.split("/");
        String originalFileName = splitedFilePath[splitedFilePath.length - 1];

        logger.debug("Original file name from path {} is {}", filePath, originalFileName);

        return originalFileName;
    }

    private String setProfileBasedResource(String fileName)
    {
        return new StringBuilder(fileName).insert(fileName.indexOf("."), "-" + "runtime").toString();
    }

    public void sendNotification(String message)
    {
        configurationChangeProducer.configurationFileCreatedMessage(message);

        logger.debug("File with name {} is updated and success message is sent for updating", message);

    }

}

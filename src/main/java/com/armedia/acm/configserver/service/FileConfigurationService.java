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

import com.armedia.acm.configserver.exception.ConfigurationException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.activemq.command.ActiveMQTopic;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.*;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.jms.DeliveryMode;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.nio.file.NoSuchFileException;

@Service
@Qualifier(value = "fileConfigurationService")
public class FileConfigurationService {

    private final String configServerRepo;
    private static final Logger logger = LoggerFactory.getLogger(FileConfigurationService.class);

    private final JmsTemplate jmsTemplate;

    public static final String VIRTUAL_TOPIC_CONFIG_FILE_UPDATED = "VirtualTopic.ConfigFileUpdated";

    private ObjectMapper objectMapper = new ObjectMapper();


    public FileConfigurationService(@Value("${properties.folder.path}") String configRepo,
            JmsTemplate jmsTemplate)
    {
        this.configServerRepo = configRepo;
        this.jmsTemplate = jmsTemplate;
    }

    public void moveFileToConfiguration(MultipartFile file, String fileName) throws Exception {
        try (InputStream logoStream = file.getInputStream())
        {

            String originalFileName = getOriginalFileNameFromFilePath(fileName);

            String profileBasedFile = setProfileBasedResource(fileName);

            File logoFile = new File(configServerRepo + "/" + profileBasedFile);

            FileUtils.copyInputStreamToFile(logoStream, logoFile);

            logger.info("File is with name {} created on the config server", fileName);

            sendNotification(originalFileName, VIRTUAL_TOPIC_CONFIG_FILE_UPDATED);

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

    public void sendNotification(String message, String destination)
    {
        ActiveMQTopic topic = new ActiveMQTopic(destination);

        jmsTemplate.setDeliveryMode(DeliveryMode.PERSISTENT);
        jmsTemplate.send(topic, inJmsSession -> inJmsSession.createTextMessage(message));

        logger.debug("File with name {} is updated and success message is sent for updating", message);

    }

    public ResponseEntity getResourceDetails(String path) throws FileNotFoundException
    {
        File resourcePath = new File(configServerRepo.concat(path));
        if(resourcePath.exists())
        {
            if (resourcePath.isDirectory())
            {
                return retrieveResourceDetails(resourcePath);
            }
            else
            {
                return downloadFileResource(path);
            }
        }
        else
        {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Resource with path " + path + " not found on config server.");
        }
    }

    public void removeFileFromConfiguration(String filePath) throws NoSuchFileException, ConfigurationException
    {
        File fileToBeRemoved = new File(configServerRepo.concat(filePath));

        if(!fileToBeRemoved.exists())
        {
            logger.error("File with path " + filePath + " doesn't exists.");
            throw new NoSuchFileException("File with path " + filePath + " doesn't exist.");
        }

        logger.info("Deleting file with path " + filePath);
        if (!fileToBeRemoved.delete())
        {
            throw new ConfigurationException("File with path " +filePath + " could not be deleted");
        }

    }

    private ResponseEntity retrieveResourceDetails(File resource)
    {
        File[] listOfFiles = resource.listFiles();
        ArrayNode result = objectMapper.createArrayNode();
        for (File file : listOfFiles)
        {
            ObjectNode fileNode = objectMapper.createObjectNode();
            fileNode.put("fileName", file.getName());
            fileNode.put("isFile", file.isFile());
            result.add(fileNode);
        }
        return ResponseEntity.status(HttpStatus.OK).body(result);
    }

    private ResponseEntity downloadFileResource(String resourcePath) throws FileNotFoundException
    {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);

        try
        {
            InputStreamResource resource = new InputStreamResource(new FileInputStream(new File(resourcePath)));
            return ResponseEntity.ok()
                    .headers(headers)
                    .contentLength(resourcePath.length())
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(resource);
        }
        catch (FileNotFoundException e)
        {
            logger.error("File with path " + resourcePath + " not found on config server.");
            throw new FileNotFoundException("File with path " + resourcePath + " not found on config server.");
        }
    }


}

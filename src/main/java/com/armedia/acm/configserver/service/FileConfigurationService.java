package com.armedia.acm.configserver.service;

import java.io.File;
import java.io.InputStream;

import javax.jms.DeliveryMode;

import org.apache.activemq.command.ActiveMQTopic;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@Qualifier(value = "fileConfigurationService")
public class FileConfigurationService
{

    private final String configServerRepo;

    private final JmsTemplate acmJmsTemplate;

    public static final String VIRTUAL_TOPIC_CONFIG_FILE_UPDATED = "VirtualTopic.ConfigFileUpdated";

    private static final Logger logger = LoggerFactory.getLogger(FileConfigurationService.class);

    public FileConfigurationService(@Value("${properties.folder.path}") String configRepo, JmsTemplate acmJmsTemplate)
    {
        this.configServerRepo = configRepo;
        this.acmJmsTemplate = acmJmsTemplate;
    }

    public void moveFileToConfiguration(MultipartFile file, String fileName) throws Exception
    {
        try (InputStream logoStream = file.getInputStream())
        {

            String originalFileName = getOriginalFileNameFromFilePath(fileName);

            String profileBasedFile = setProfileBasedResource(fileName);

            File logoFile = new File(this.configServerRepo + "/" + profileBasedFile);

            FileUtils.copyInputStreamToFile(logoStream, logoFile);

            FileConfigurationService.logger.info("File is with name {} created on the config server", fileName);

            sendNotification(originalFileName,
                    FileConfigurationService.VIRTUAL_TOPIC_CONFIG_FILE_UPDATED);

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

        FileConfigurationService.logger.debug("Original file name from path {} is {}", filePath, originalFileName);

        return originalFileName;
    }

    private String setProfileBasedResource(String fileName)
    {
        return new StringBuilder(fileName).insert(fileName.indexOf("."), "-" + "runtime").toString();
    }

    public void sendNotification(String message, String destination)
    {
        ActiveMQTopic topic = new ActiveMQTopic(destination);

        this.acmJmsTemplate.setDeliveryMode(DeliveryMode.PERSISTENT);
        this.acmJmsTemplate.send(topic, inJmsSession -> inJmsSession.createTextMessage(message));

        FileConfigurationService.logger.debug("File with name {} is updated and success message is sent for updating", message);

    }

}

package com.armedia.acm.configserver.service;

import org.apache.activemq.command.ActiveMQTopic;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.jms.DeliveryMode;
import javax.jms.TextMessage;
import java.io.File;
import java.io.InputStream;

@Service
@Qualifier(value = "fileConfigurationService")
public class FileConfigurationService {

    private final String configServerRepo;

    private final JmsTemplate acmJmsTemplate;

    public static final String VIRTUAL_TOPIC_CONFIG_FILE_UPDATED = "VirtualTopic.ConfigFileUpdated";


    public FileConfigurationService(@Value("${properties.folder.path}") String configRepo, JmsTemplate acmJmsTemplate)
    {
        this.configServerRepo = configRepo;
        this.acmJmsTemplate = acmJmsTemplate;
    }

    public void moveFileToConfiguration(MultipartFile file, String fileName) throws Exception {
        try (InputStream logoStream = file.getInputStream())
        {

            String originalFileName = getOriginalFileNameFromFilePath(fileName);

            String profileBasedfile = setProfileBasedResource(fileName);

            File logoFile = new File(configServerRepo + "/" + profileBasedfile);
            FileUtils.copyInputStreamToFile(logoStream, logoFile);

            sendNotification(originalFileName,
                    VIRTUAL_TOPIC_CONFIG_FILE_UPDATED);

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

        return originalFileName;
    }

    private String setProfileBasedResource(String fileName)
    {
        return new StringBuilder(fileName).insert(fileName.indexOf("."), "-" + "runtime").toString();
    }

    public void sendNotification(String message, String destination)
    {
        ActiveMQTopic topic = new ActiveMQTopic(destination);

        acmJmsTemplate.setDeliveryMode(DeliveryMode.PERSISTENT);
        acmJmsTemplate.send(topic, inJmsSession -> {
            TextMessage theTextMessage = inJmsSession.createTextMessage(message);
            return theTextMessage;
        });
    }

}

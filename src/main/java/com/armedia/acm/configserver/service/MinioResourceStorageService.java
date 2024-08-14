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

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;

@Service
@Profile("run-with-db")
public class MinioResourceStorageService implements ResourceStorageService
{
    private static final Logger logger = LoggerFactory.getLogger(MinioResourceStorageService.class);

    private final NotificationService notificationService;
    private final AmazonS3 amazonS3;
    private final String bucketName;

    public MinioResourceStorageService(NotificationService notificationService, AmazonS3 amazonS3)
    {
        this.notificationService = notificationService;
        this.amazonS3 = amazonS3;
        this.bucketName = "arkcase";
    }

    @Override
    public void moveFileToConfiguration(MultipartFile file, String fileName, boolean isBrandingFile) throws IOException
    {
        try {
            // String key = isBrandingFile ? "branding/" + fileName : "assets/" + fileName;

            // Get the input stream of the file
            InputStream inputStream = file.getInputStream();

            // Set metadata (if needed)
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(file.getSize());
            metadata.setContentType(file.getContentType());

            // Upload the file to S3 (or MinIO in this case)
            amazonS3.putObject(new PutObjectRequest(bucketName, fileName, inputStream, metadata));
            if(Boolean.TRUE.equals(isBrandingFile))
            {
                logger.info("Successfully uploaded file: {} to Minio", fileName);
                notificationService.sendNotification(fileName, NotificationService.VIRTUAL_TOPIC_CONFIG_FILE_UPDATED);
            }
        } catch (Exception e) {
            logger.error("Unable to save file {}, reason {}", fileName, e.getMessage(), e);
            throw e;
        }
    }
}

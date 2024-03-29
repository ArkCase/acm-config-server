package com.armedia.acm.configserver.api;

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
import com.armedia.acm.configserver.service.FileConfigurationService;
import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.HandlerMapping;

import javax.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;

import java.io.FileNotFoundException;
import java.nio.file.NoSuchFileException;


@RestController
@RequestMapping({"/file", "/files"})
public class FileConfigurationApiController {

    @Autowired
    FileConfigurationService fileConfigurationService;

    private static final Logger logger = LoggerFactory.getLogger(FileConfigurationApiController.class);

    @PostMapping()
    public ResponseEntity moveFileToConfiguration(@RequestParam("file") MultipartFile file,
                                                  @RequestParam("fileName") String fileName) throws Exception {

        logger.info("branding file is received on config server! [{}]", fileName);

        fileConfigurationService.moveFileToConfiguration(file, fileName);

        logger.info("file is moved to config server!");

        return ResponseEntity.ok().build();
    }

    @GetMapping(value = "/**/*")
    public ResponseEntity getResourceDetails(HttpServletRequest request) throws FileNotFoundException
    {
        String fullPath = request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE).toString();
        String path = StringUtils.substringAfter(fullPath, fullPath.contains("files") ? "files" : "file");
        logger.info("resource details to be fetched from " + path);

        return fileConfigurationService.getResourceDetails(path);
    }

    @DeleteMapping(value = "/**/*")
    public void removeFileFromConfiguration(HttpServletRequest request) throws NoSuchFileException, ConfigurationException
    {
        String fullPath = request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE).toString();
        String filePath = StringUtils.substringAfter(fullPath, fullPath.contains("files") ? "files" : "file");

        logger.info("file with path " + filePath + " to be removed from config server");

        fileConfigurationService.removeFileFromConfiguration(filePath);
    }
}

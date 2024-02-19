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

import com.armedia.acm.configserver.service.FileConfigurationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/file")
public class FileConfigurationApiController {

    private static final Logger logger = LoggerFactory.getLogger(FileConfigurationApiController.class);
    private final FileConfigurationService fileConfigurationService;

    public FileConfigurationApiController(final FileConfigurationService fileConfigurationService) {
        this.fileConfigurationService = fileConfigurationService;
    }

    @PostMapping()
    public ResponseEntity<Void> moveFileToConfiguration(@RequestParam("file") MultipartFile file,
                                                  @RequestParam("fileName") String fileName) {

        logger.info("branding file is received on config server! [{}]", fileName);

        try {
            fileConfigurationService.moveFileToConfiguration(file, fileName);

            logger.info("file [{}] is moved to config server!", fileName);

            return ResponseEntity.ok().build();
        }
        catch (Exception e) {
            logger.debug("Moving {} to config server failed. {}", fileName, e.getMessage());
            logger.trace("Cause: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}

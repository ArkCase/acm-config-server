package com.armedia.acm.configserver.api;

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

import com.armedia.acm.configserver.service.FileSystemConfigurationService;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/config")
public class ModulesListAPIController
{
    private static final Logger logger = LoggerFactory.getLogger(ModulesListAPIController.class);

    private final FileSystemConfigurationService fileSystemConfigurationService;

    public ModulesListAPIController(FileSystemConfigurationService fileSystemConfigurationService)
    {
        this.fileSystemConfigurationService = fileSystemConfigurationService;
    }

    @GetMapping(value = "/modules", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<String>> getModules()
    {
        try {
            List<String> result = fileSystemConfigurationService.getModulesNames();
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            logger.debug("Failed to list modules. {}", e.getMessage());
            logger.trace("Cause: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}

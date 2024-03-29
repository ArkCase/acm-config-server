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

import com.armedia.acm.configserver.exception.ConfigurationException;
import com.armedia.acm.configserver.service.ConfigurationService;
import com.armedia.acm.configserver.service.FileConfigurationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.file.NoSuchFileException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/config")
public class ConfigurationAPIController
{
    private static final Logger logger = LoggerFactory.getLogger(ConfigurationAPIController.class);

    private final List<String> langs;

    private final ConfigurationService configServerService;
    private final FileConfigurationService fileConfigurationService;

    public ConfigurationAPIController(@Qualifier(value = "fileSystemConfigurationService") ConfigurationService configServerService, @Value("${arkcase.languages}") String arkcaseLanguages, FileConfigurationService fileConfigurationService)
    {
        this.configServerService = configServerService;
        this.langs = Arrays.asList(arkcaseLanguages.split(","));
        this.fileConfigurationService = fileConfigurationService;
    }

    @PostMapping("/{applicationName}")
    public ResponseEntity updateProperties(@PathVariable String applicationName, @RequestBody Map<String, Object> properties)
    {
        logger.info("Update properties {}", properties.keySet());
        try
        {
            if(langs.stream().anyMatch(applicationName::contains))
            {
                applicationName = "labels/" + applicationName;
            }
            else if(applicationName.equals("ldap")){
                applicationName = "ldap/" + applicationName;
            } else if (applicationName.equals("lookups")){
                applicationName = "lookups/" + applicationName;
            }
            configServerService.updateProperties(properties, applicationName);
            logger.debug("Properties successfully updated");
            return ResponseEntity.ok().build();
        }
        catch (ConfigurationException e)
        {
            logger.debug("Failed to update properties. {}", e.getMessage());
            logger.trace("Cause: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/remove/{applicationName}")
    public ResponseEntity removeProperties(@PathVariable String applicationName, @RequestBody List<String> properties)
    {
        logger.info("Remove properties {}", properties);
        try
        {
            configServerService.removeProperties(properties, applicationName);
            logger.debug("Properties successfully removed");
            return ResponseEntity.ok().build();
        }
        catch (ConfigurationException e)
        {
            logger.debug("Failed to remove properties. {}", e.getMessage());
            logger.trace("Cause: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }


    @DeleteMapping("/reset")
    public ResponseEntity resetPropertiesToDefault()
    {
        logger.info("Resetting all properties");
        try
        {
            configServerService.resetConfigurationBrandingFilesToDefault();
            configServerService.resetPropertiesToDefault();
            return ResponseEntity.ok().build();
        }
        catch (ConfigurationException e)
        {
            logger.debug("Failed to reset properties. {}", e.getMessage());
            logger.trace("Cause: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/reset/{applicationName}")
    public ResponseEntity resetFilePropertiesToDefault(@PathVariable String applicationName)
    {
        logger.info("Resetting properties for: {}", applicationName);
        if(langs.parallelStream().anyMatch(applicationName::contains))
        {
            applicationName = "labels/" + applicationName;
        }
        try
        {
            configServerService.resetFilePropertiesToDefault(applicationName);

            return ResponseEntity.ok().build();
        }
        catch (NoSuchFileException e)
        {
            return ResponseEntity.ok().build();
        }
        catch (ConfigurationException e)
        {
            logger.debug("Failed to reset properties. {}", e.getMessage());
            logger.trace("Cause: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}

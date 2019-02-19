package com.armedia.acm.configserver.api;

import com.armedia.acm.configserver.exception.ConfigurationException;
import com.armedia.acm.configserver.service.ConfigurationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/config")
public class ConfigurationAPIController
{
    private static final Logger logger = LoggerFactory.getLogger(ConfigurationAPIController.class);

    private ConfigurationService configServerService;

    public ConfigurationAPIController(@Qualifier(value = "fileSystemConfigurationService") ConfigurationService configServerService)
    {
        this.configServerService = configServerService;
    }

    @PostMapping
    public ResponseEntity updateProperties(@RequestBody Map<String, Object> properties)
    {
        logger.info("Update properties {}", properties.keySet());
        try
        {
            configServerService.updateProperties(properties);
            return ResponseEntity.ok().build();
        }
        catch (ConfigurationException e)
        {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}

package com.armedia.acm.configserver.service;

import com.armedia.acm.configserver.exception.ConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Service;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
@Qualifier(value = "fileSystemConfigurationService")
public class FileSystemConfigurationService implements ConfigurationService
{
    private static final Logger logger = LoggerFactory.getLogger(FileSystemConfigurationService.class);

    private final String propertiesPath;

    public FileSystemConfigurationService(@Value("${propertiesPath}") String propertiesPath)
    {
        this.propertiesPath = propertiesPath;
    }

    @Override
    public synchronized void updateProperties(Map<String, Object> properties) throws ConfigurationException
    {
        FileSystemResource yamlResource = new FileSystemResource(propertiesPath);
        DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);

        Yaml yaml = new Yaml(options);
        try
        {
            Map<String, Object> configMap = yaml.load(yamlResource.getInputStream());
            if (configMap == null)
            {
                configMap = new LinkedHashMap<>();
            }
            configMap.putAll(properties);
            yaml.dump(configMap, new FileWriter(yamlResource.getFile()));
        }
        catch (IOException e)
        {
            logger.warn("Failed to read configuration from path [{}]", propertiesPath);
            throw new ConfigurationException("Failed to read configuration.", e);
        }
    }
}

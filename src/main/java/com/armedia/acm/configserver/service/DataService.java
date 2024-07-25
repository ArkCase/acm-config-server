package com.armedia.acm.configserver.service;

import static com.armedia.acm.configserver.service.ConfigUtils.extractFromFileName;
import static com.armedia.acm.configserver.service.ConfigUtils.findProfile;

import com.armedia.acm.configserver.model.ApplicationProperty;
import com.armedia.acm.configserver.model.ArkcaseConfig;
import com.armedia.acm.configserver.repository.ApplicationPropertyRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class DataService
{
    protected final Logger log = LoggerFactory.getLogger(this.getClass());
    protected final ApplicationPropertyRepository applicationPropertyRepository;
    protected final ArkcaseConfig arkcaseConfig;
    protected final ObjectMapper mapper;

    protected DataService(ApplicationPropertyRepository applicationPropertyRepository, ArkcaseConfig arkcaseConfig)
    {
        this.applicationPropertyRepository = applicationPropertyRepository;
        this.arkcaseConfig = arkcaseConfig;
        this.mapper = new ObjectMapper(new YAMLFactory());
    }

    protected void readResource(Resource resource)
    {
        log.debug("Import runtime properties from file: {}", resource.getFilename());
        try (var reader = new BufferedReader(
                new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8)))
        {
            readFromYaml(resource.getFilename(), reader);
        }
        catch (IOException e)
        {
            log.debug("Unable to read resource: {}, reason:{}", resource.getFilename(), e.getMessage(), e);
        }
    }

    protected void readFromYaml(String fileName, BufferedReader reader) throws IOException
    {
        if (reader.ready())
        {
            try
            {
                var yamlProperties = mapper.readValue(reader, Map.class);
                if (yamlProperties != null && !yamlProperties.isEmpty())
                {
                    var applicationProperties = yamlToApplicationProperties(fileName, yamlProperties);
                    this.applicationPropertyRepository.saveAll(applicationProperties);
                }
            }
            catch (Exception e)
            {
                log.debug("Unable to read YAML file: {}, reason:{}", fileName, e.getMessage(), e);
            }
        }
        else
        {
            log.warn("YAML file is empty: {}", fileName);
        }
    }

    protected List<ApplicationProperty> yamlToApplicationProperties(String fileName, Map<String, Object> yamlProperties)
    {
        var applicationProperties = new ArrayList<ApplicationProperty>();

        if(fileName == null || fileName.isBlank())
            return List.of();

        var label = extractFromFileName(fileName, this.arkcaseConfig.getLanguages());

        var matchedProfile = findProfile(fileName, arkcaseConfig.getProfiles());
        var profile = matchedProfile.orElse("default");

        var applicationName = matchedProfile.map(s -> fileName.substring(0, fileName.indexOf(s) - 1))
                .orElseGet(() -> fileName.substring(0, fileName.lastIndexOf('.')));

        var flattenedProperties = new HashMap<String, String>();
        flattenProperties("", yamlProperties, flattenedProperties);

        for (var entry : flattenedProperties.entrySet())
        {
            var applicationProperty = new ApplicationProperty(applicationName, profile, label, entry.getKey(), entry.getValue());
            applicationProperties.add(applicationProperty);
        }

        return applicationProperties;
    }

    protected void flattenProperties(String prefix, Map<String, Object> source, Map<String, String> target)
    {
        try
        {
            source.forEach((key, value) -> {
                var newKey = prefix.isEmpty() ? key : prefix + "." + key;
                if (value instanceof Map)
                {
                    flattenProperties(newKey, (Map<String, Object>) value, target);
                }
                else
                {
                    target.put(newKey, String.valueOf(value));
                }
            });
        }
        catch (Exception e)
        {
            log.error("Unable to flatten prefix:{}, source:{}, reason: {}", prefix, source, e.getMessage(), e);
        }
    }
}

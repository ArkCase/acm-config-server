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

import static com.armedia.acm.configserver.service.ConfigUtils.extractFromFileName;
import static com.armedia.acm.configserver.service.ConfigUtils.findProfile;

import com.armedia.acm.configserver.model.ApplicationProperty;
import com.armedia.acm.configserver.model.ArkcaseConfig;
import com.armedia.acm.configserver.repository.ApplicationPropertyRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Profile("run-with-db")
public class DataImporterService
{

    private static final Logger log = LoggerFactory.getLogger(DataImporterService.class);
    private final ApplicationPropertyRepository applicationPropertyRepository;
    private final ArkcaseConfig arkcaseConfig;

    public DataImporterService(ApplicationPropertyRepository applicationPropertyRepository, ArkcaseConfig arkcaseConfig)
    {
        this.applicationPropertyRepository = applicationPropertyRepository;
        this.arkcaseConfig = arkcaseConfig;
    }

    public void importData()
    {
        if (Boolean.TRUE.equals(this.applicationPropertyRepository.existsAnyRecord()))
            return;

        var mapper = new ObjectMapper(new YAMLFactory());
        var resolver = new PathMatchingResourcePatternResolver();

        for (var folder : this.arkcaseConfig.getFolders())
        {
            try
            {
                var resources = resolver.getResources("file:" + folder + "/*.yaml");
                for (var resource : resources)
                {
                    log.debug("Import properties from file: {}", resource.getFilename());
                    try (var reader = new BufferedReader(
                            new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8)))
                    {
                        readFromYaml(resource, reader, mapper);
                    }
                    catch (IOException e)
                    {
                        log.debug("Unable to read resource: {}, reason:{}", resource.getFilename(), e.getMessage(), e);
                    }
                }
            }
            catch (IOException e)
            {
                log.error("Unable to resolve resources in folder: {}, reason: {}", folder, e.getMessage(), e);
            }
        }
    }

    private void readFromYaml(Resource resource, BufferedReader reader, ObjectMapper mapper) throws IOException
    {
        if (reader.ready())
        {
            try
            {
                var yamlProperties = mapper.readValue(reader, Map.class);
                if (yamlProperties != null && !yamlProperties.isEmpty())
                {
                    var applicationProperties = yamlToApplicationProperties(resource, yamlProperties);
                    this.applicationPropertyRepository.saveAll(applicationProperties);
                }
            }
            catch (Exception e)
            {
                log.debug("Unable to read YAML file: {}, reason:{}", resource.getFilename(), e.getMessage(), e);
            }
        }
        else
        {
            log.warn("YAML file is empty: {}", resource.getFilename());
        }
    }

    private List<ApplicationProperty> yamlToApplicationProperties(Resource resource, Map<String, Object> yamlProperties)
    {
        var applicationProperties = new ArrayList<ApplicationProperty>();
        var fileName = resource.getFilename();

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

    private void flattenProperties(String prefix, Map<String, Object> source, Map<String, String> target)
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

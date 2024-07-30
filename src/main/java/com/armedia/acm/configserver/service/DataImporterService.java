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

import static com.armedia.acm.configserver.service.ConfigUtils.extractLabelFromFileName;
import static com.armedia.acm.configserver.service.ConfigurationService.RUNTIME;

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
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Profile("run-with-db")
public class DataImporterService
{
    private static final Logger logger = LoggerFactory.getLogger(DataImporterService.class);
    protected final ApplicationPropertyRepository applicationPropertyRepository;
    protected final ArkcaseConfig arkcaseConfig;
    protected final ObjectMapper mapper;

    public DataImporterService(ApplicationPropertyRepository applicationPropertyRepository, ArkcaseConfig arkcaseConfig)
    {
        this.applicationPropertyRepository = applicationPropertyRepository;
        this.arkcaseConfig = arkcaseConfig;
        this.mapper = new ObjectMapper(new YAMLFactory());
    }

    public void importRuntimeFiles()
    {
        var resolver = new PathMatchingResourcePatternResolver();

        for (var folder : this.arkcaseConfig.getFolders())
        {
            try
            {
                var resources = resolver.getResources("file:" + folder + "/*-runtime.yaml");
                for (var resource : resources)
                {
                    readResource(resource);

                    deleteResource(resource);
                }
            }
            catch (IOException e)
            {
                DataImporterService.logger.error("Unable to resolve resources in folder: {}, reason: {}", folder, e.getMessage(), e);
            }
        }
    }

    private void readResource(Resource resource)
    {
        DataImporterService.logger.debug("Import runtime properties from file: {}", resource.getFilename());
        try (var reader = new BufferedReader(
                new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8)))
        {
            readFromYaml(resource.getFilename(), reader);
        }
        catch (IOException e)
        {
            DataImporterService.logger.debug("Unable to read resource: {}, reason:{}", resource.getFilename(), e.getMessage(), e);
        }
    }

    private void readFromYaml(String fileName, BufferedReader reader) throws IOException
    {
        if (reader.ready())
        {
            try
            {
                var yamlProperties = mapper.readValue(reader, Map.class);
                if (yamlProperties != null && !yamlProperties.isEmpty())
                {
                    var applicationProperties = yamlToApplicationProperties(fileName, yamlProperties);
                    saveRuntimeProperties(applicationProperties);
                }
            }
            catch (Exception e)
            {
                DataImporterService.logger.debug("Unable to read YAML file: {}, reason:{}", fileName, e.getMessage(), e);
            }
        }
        else
        {
            DataImporterService.logger.warn("YAML file is empty: {}", fileName);
        }
    }

    private List<ApplicationProperty> yamlToApplicationProperties(String fileName, Map<String, Object> yamlProperties)
    {
        var applicationProperties = new ArrayList<ApplicationProperty>();

        if (fileName == null || fileName.isBlank())
            return List.of();

        var label = extractLabelFromFileName(fileName, this.arkcaseConfig.getLanguages());

        var profile = RUNTIME;

        var applicationName = fileName.substring(0, fileName.indexOf(profile) - 1);

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
            DataImporterService.logger.error("Unable to flatten prefix:{}, source:{}, reason: {}", prefix, source, e.getMessage(), e);
        }
    }

    private void saveRuntimeProperties(List<ApplicationProperty> applicationProperties)
    {
        applicationProperties.forEach(ap -> {
            var existingAp = this.applicationPropertyRepository.findByApplicationAndProfileAndLabelAndKey(
                    ap.getApplication(),
                    ap.getProfile(), ap.getLabel(), ap.getKey());
            if (existingAp.isEmpty())
            {
                this.applicationPropertyRepository.save(ap);
            }
            else
            {
                existingAp.get().setValue(ap.getValue());
                this.applicationPropertyRepository.save(existingAp.get());
            }
        });
    }

    private void deleteResource(Resource resource)
    {
        try
        {
            var path = resource.getFile().toPath();
            Files.delete(path);
        }
        catch (IOException e)
        {
            DataImporterService.logger.warn("Failed to delete file: {}, reason: {}", resource.getFilename(), e.getMessage());
        }
    }
}

package com.armedia.acm.configserver.service;

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Service;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Qualifier(value = "fileSystemConfigurationService")
public class FileSystemConfigurationService implements ConfigurationService
{
    private static final Logger logger = LoggerFactory.getLogger(FileSystemConfigurationService.class);

    private final String serverRepoPath;

    public FileSystemConfigurationService(@Value("${properties.folder.path}") String serverRepoPath)
    {
        this.serverRepoPath = serverRepoPath;
    }

    @Override
    public synchronized void updateProperties(Map<String, Object> properties, String applicationName, String runtimeProfile)
            throws ConfigurationException
    {
        String configurationFile = String.format("%s-%s.yaml", applicationName, runtimeProfile);

        Set<String> configurationFiles;
        try
        {
            configurationFiles = Files.walk(Paths.get(serverRepoPath))
                    .map(Path::toFile)
                    .filter(File::isFile)
                    .map(File::getName)
                    .collect(Collectors.toSet());
        }
        catch (IOException e)
        {
            throw new ConfigurationException("File " + configurationFile + " might not exists");
        }

        if (!configurationFiles.contains(configurationFile))
        {
            throw new ConfigurationException("File " + configurationFile + " does not exist");
        }

        String pathToConfigurationFile = String.format("%s/%s", serverRepoPath, configurationFile);
        FileSystemResource yamlResource = new FileSystemResource(pathToConfigurationFile);
        DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        options.setDefaultScalarStyle(DumperOptions.ScalarStyle.DOUBLE_QUOTED);

        Yaml yaml = new Yaml(options);
        try (InputStreamReader configStreamReader = new InputStreamReader(yamlResource.getInputStream(), StandardCharsets.UTF_8))
        {
            Map<String, Object> configMap = yaml.load(configStreamReader);
            if (configMap == null)
            {
                configMap = new LinkedHashMap<>();
            }
            configMap.putAll(properties);
            yaml.dump(configMap, new FileWriter(yamlResource.getFile()));

            for (Map.Entry<String, Object> entry : properties.entrySet())
            {
                configMap.put(entry.getKey(), entry.getValue());
            }

            try (FileWriter fw = new FileWriter(yamlResource.getFile()))
            {
                yaml.dump(configMap, fw);
            }
        }
        catch (IOException e)
        {
            logger.warn("Failed to read configuration from path [{}]", configurationFile);
            throw new ConfigurationException("Failed to read configuration.", e);
        }
    }
}

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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@Qualifier(value = "fileSystemConfigurationService")
public class FileSystemConfigurationService implements ConfigurationService
{
    private static final Logger logger = LoggerFactory.getLogger(FileSystemConfigurationService.class);

    private final String propertiesPath;

    private final String propertiesFolderPath;

    private static final String RUNTIME = "-runtime";

    public FileSystemConfigurationService(@Value("${properties.path}") String propertiesPath, @Value("${properties.folder.path}") String propertiesFolderPath)
    {
        this.propertiesPath = propertiesPath;
        this.propertiesFolderPath = propertiesFolderPath;
    }

    @Override
    public synchronized void updateProperties(Map<String, Object> properties) throws ConfigurationException
    {
        FileSystemResource yamlResource = new FileSystemResource(propertiesPath);
        if(!yamlResource.getFile().exists())
        {
            File file = new File(yamlResource.getPath());
            try
            {
                file.createNewFile();
            }
            catch (IOException e)
            {
                logger.warn("Failed to create file to path [{}]", yamlResource.getPath());
                throw new ConfigurationException(e);
            }
        }
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
            logger.warn("Failed to read configuration from path [{}]", propertiesPath);
            throw new ConfigurationException("Failed to read configuration.", e);
        }
    }


    @Override
    public void resetPropertiesToDefault() throws ConfigurationException
    {
        List<File> fileList = listAllRuntimeFilesInFolderAndSubfolders(propertiesFolderPath);
            for(File file : fileList)
            {
                if(file.getName().contains(FileSystemConfigurationService.RUNTIME))
                {
                    logger.info("Deleting file [{}]", file.getName());
                    file.delete();
                }
            }
    }

    private List<File> listAllRuntimeFilesInFolderAndSubfolders(String directoryName) {
        File directory = new File(directoryName);

        List<File> resultList = new ArrayList<>();

        File[] fList = directory.listFiles();
        for (File file : fList) {
            if (file.isFile() && file.getName().contains(FileSystemConfigurationService.RUNTIME)) {
                resultList.add(file);
            } else if (file.isDirectory()) {
                resultList.addAll(listAllRuntimeFilesInFolderAndSubfolders(file.getAbsolutePath()));
            }
        }
        return resultList;
    }
}

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
import com.armedia.acm.configserver.model.ArkcaseConfig;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Service;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.NoSuchFileException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@Profile("!run-with-db")
@ConditionalOnMissingBean(DatabaseConfigurationService.class)
public class FileSystemConfigurationService implements ConfigurationService
{
    private static final Logger logger = LoggerFactory.getLogger(FileSystemConfigurationService.class);

    private final String labelsFolderPath;
    private final ArkcaseConfig arkcaseConfig;
    private final NotificationService notificationService;

    public FileSystemConfigurationService(ArkcaseConfig arkcaseConfig, NotificationService notificationService)
    {
        this.arkcaseConfig = arkcaseConfig;
        this.labelsFolderPath = this.arkcaseConfig.getPropertiesFolderPath() + "/labels";
        this.notificationService = notificationService;
    }

    @Override
    public synchronized void updateProperties(Map<String, Object> properties, String applicationName) throws ConfigurationException
    {
        if (this.arkcaseConfig.getLanguages().stream().anyMatch(applicationName::contains))
        {
            applicationName = "labels/" + applicationName;
        }
        else if (applicationName.equals("ldap"))
        {
            applicationName = "ldap/" + applicationName;
        }
        else if (applicationName.equals("lookups"))
        {
            applicationName = "lookups/" + applicationName;
        }

        String configurationFilePath = getRuntimeConfigurationFilePath(applicationName);

        FileSystemResource yamlResource = loadYamlSystemResource(configurationFilePath);

        DumperOptions options = buildDumperOptions();

        Yaml yaml = new Yaml(options);
        try (InputStreamReader configStreamReader = new InputStreamReader(yamlResource.getInputStream(), StandardCharsets.UTF_8))
        {
            Map<String, Object> configMap = yaml.load(configStreamReader);
            if (configMap == null)
            {
                configMap = new LinkedHashMap<>();
            }

            configMap.putAll(properties);

            try (FileWriter fw = new FileWriter(yamlResource.getFile()))
            {
                yaml.dump(configMap, fw);
            }
        }
        catch (IOException e)
        {
            FileSystemConfigurationService.logger.warn("Failed to read configuration from path [{}]", configurationFilePath);
            throw new ConfigurationException("Failed to read configuration.", e);
        }
    }

    @Override
    public synchronized void removeProperties(List<String> properties, String applicationName) throws ConfigurationException
    {
        String configurationFilePath = getRuntimeConfigurationFilePath(applicationName);

        FileSystemResource yamlResource = loadYamlSystemResource(configurationFilePath);

        DumperOptions options = buildDumperOptions();

        Yaml yaml = new Yaml(options);

        try (InputStreamReader configStreamReader = new InputStreamReader(yamlResource.getInputStream(), StandardCharsets.UTF_8))
        {
            Map<String, Object> configMap = yaml.load(configStreamReader);
            if (configMap == null)
            {
                configMap = new LinkedHashMap<>();
            }

            properties.forEach(configMap::remove);

            try (FileWriter fw = new FileWriter(yamlResource.getFile()))
            {
                yaml.dump(configMap, fw);
            }
        }
        catch (IOException e)
        {
            FileSystemConfigurationService.logger.warn("Failed to read configuration from path [{}]", configurationFilePath);
            throw new ConfigurationException("Failed to read configuration.", e);
        }
    }

    /**
     * Reset properties for file with name 'applicationName'
     *
     * @param applicationName
     *            - ex. 'cases-en', without the file extension (.yaml)
     *            - If cases-en was updated, cases-en-runtime is created including just the updated properties
     *            - This method will remove just the updated (cases-en-runtime.yaml)
     * @throws ConfigurationException
     */
    @Override
    public void resetFilePropertiesToDefault(String applicationName) throws ConfigurationException, NoSuchFileException
    {
        if (this.arkcaseConfig.getLanguages().parallelStream().anyMatch(applicationName::contains))
        {
            applicationName = "labels/" + applicationName;
        }

        if (!applicationName.contains(ConfigurationService._RUNTIME))
        {
            applicationName = String.format("%s/%s%s.yaml", this.arkcaseConfig.getPropertiesFolderPath(), applicationName,
                    ConfigurationService._RUNTIME);
        }
        else
        {
            applicationName = String.format("%s/%s.yaml", this.arkcaseConfig.getPropertiesFolderPath(), applicationName);
        }

        String[] fileNameHelper = applicationName.split("/");
        String fileName = fileNameHelper[fileNameHelper.length - 1];

        FileSystemConfigurationService.logger.info("Deleting file [{}]", fileName);

        File fileToBeDeleted = new File(applicationName);
        if (!fileToBeDeleted.exists())
        {
            FileSystemConfigurationService.logger.warn("File [{}] does not exists, nothing to delete.", fileName);
            throw new NoSuchFileException(String.format("File %s does not exists, nothing to delete.", fileName));
        }
        else if (!fileToBeDeleted.delete())
        {
            throw new ConfigurationException(String.format("File %s could not be deleted", fileName));
        }
    }

    @Override
    public void resetConfigurationBrandingFilesToDefault() throws ConfigurationException
    {
        List<File> fileList = listAllRuntimeFilesInFolderAndSubfolders(this.arkcaseConfig.getBrandingFilesFolder());

        for (File file : fileList)
        {
            if (file.getName().contains(ConfigurationService._RUNTIME))
            {
                if (file.delete())
                {
                    FileSystemConfigurationService.logger.info("Reset file [{}] to default version.", file.getName());
                    String originalFileName = file.getName().replace(ConfigurationService._RUNTIME, "");
                    this.notificationService.sendNotification(originalFileName,
                            NotificationService.VIRTUAL_TOPIC_CONFIG_FILE_UPDATED);
                }
                else
                {
                    throw new ConfigurationException(String.format("File %s could not be fetched", file.getName()));
                }
            }
        }
    }

    @Override
    public void resetPropertiesToDefault() throws ConfigurationException
    {
        List<File> fileList = listAllRuntimeFilesInFolderAndSubfolders(this.arkcaseConfig.getPropertiesFolderPath());
        for (File file : fileList)
        {
            if (file.getName().contains(ConfigurationService._RUNTIME))
            {
                FileSystemConfigurationService.logger.info("Deleting file [{}]", file.getName());
                if (!file.delete())
                {
                    throw new ConfigurationException(String.format("File %s could not be deleted", file.getName()));
                }
            }
        }
    }

    @Override
    public List<String> getModulesNames()
    {
        File modulesDir = new File(this.labelsFolderPath);

        File[] files = modulesDir.listFiles(file -> file.isFile() && !file.getName().toLowerCase().contains(_RUNTIME));

        List<String> modules = new ArrayList<>();

        for (File labelResource : files)
        {
            for (String lang : this.arkcaseConfig.getLanguages())
            {
                String fileName = labelResource.getName();
                if (fileName.contains(lang))
                {
                    int sepPos = fileName.indexOf(lang);
                    String moduleName = fileName.substring(0, sepPos);
                    if (modules.stream().noneMatch(module -> module.equals(moduleName)))
                    {
                        modules.add(moduleName);
                    }
                }
            }
        }

        FileSystemConfigurationService.logger.info("Returns modules names. [{}]", modules.toArray());
        return modules;
    }

    private List<File> listAllRuntimeFilesInFolderAndSubfolders(String directoryName)
    {
        File directory = new File(directoryName);

        List<File> resultList = new ArrayList<>();

        File[] fList = directory.listFiles();
        for (File file : fList)
        {
            if (file.isFile() && file.getName().contains(ConfigurationService._RUNTIME))
            {
                resultList.add(file);
            }
            else if (file.isDirectory())
            {
                resultList.addAll(listAllRuntimeFilesInFolderAndSubfolders(file.getAbsolutePath()));
            }
        }
        return resultList;
    }

    private FileSystemResource loadYamlSystemResource(String configurationFilePath) throws ConfigurationException
    {
        FileSystemResource yamlResource = new FileSystemResource(configurationFilePath);
        if (!yamlResource.getFile().exists())
        {
            File file = new File(yamlResource.getPath());
            try
            {
                file.createNewFile();
            }
            catch (IOException e)
            {
                FileSystemConfigurationService.logger.warn("Failed to create file to path [{}]", yamlResource.getPath());
                throw new ConfigurationException(e);
            }
        }
        return yamlResource;
    }

    private String getRuntimeConfigurationFilePath(String applicationName)
    {
        return String.format("%s/%s%s.yaml", this.arkcaseConfig.getPropertiesFolderPath(), applicationName,
                ConfigurationService._RUNTIME);
    }

    private DumperOptions buildDumperOptions()
    {
        DumperOptions options = new DumperOptions();
        options.setSplitLines(false);
        options.setMaxSimpleKeyLength(1024);
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        options.setDefaultScalarStyle(DumperOptions.ScalarStyle.DOUBLE_QUOTED);
        return options;
    }
}

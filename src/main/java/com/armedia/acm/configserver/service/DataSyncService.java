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

import com.armedia.acm.configserver.model.ApplicationProperty;
import com.armedia.acm.configserver.model.ArkcaseConfig;
import com.armedia.acm.configserver.repository.ApplicationPropertyRepository;

import org.springframework.context.annotation.Profile;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@Service
@Profile("run-with-db")
public class DataSyncService extends DataService
{
    protected DataSyncService(ApplicationPropertyRepository applicationPropertyRepository, ArkcaseConfig arkcaseConfig)
    {
        super(applicationPropertyRepository, arkcaseConfig);
    }

    public void syncRuntimeFiles()
    {
        var resolver = new PathMatchingResourcePatternResolver();

        for (var folder : arkcaseConfig.getFolders())
        {
            try
            {
                var resources = resolver.getResources("file:" + folder + "/*-runtime.yaml");
                for (var resource : resources)
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
            }
            catch (IOException e)
            {
                log.error("Unable to resolve resources in folder: {}, reason: {}", folder, e.getMessage(), e);
            }
        }
    }

    @Override
    public void readFromYaml(String fileName, BufferedReader reader) throws IOException
    {
        if (reader.ready())
        {
            try
            {
                var yamlProperties = super.mapper.readValue(reader, Map.class);
                if (yamlProperties != null && !yamlProperties.isEmpty())
                {
                    List<ApplicationProperty> applicationProperties = yamlToApplicationProperties(fileName, yamlProperties);

                    applicationProperties.forEach(ap -> {
                        var existingAp = super.applicationPropertyRepository.findByApplicationAndProfileAndLabelAndKey(ap.getApplication(),
                                ap.getProfile(), ap.getLabel(), ap.getKey());
                        if (existingAp.isEmpty())
                        {
                            super.applicationPropertyRepository.save(ap);
                        }
                        else
                        {
                            existingAp.get().setValue(ap.getValue());
                            super.applicationPropertyRepository.save(existingAp.get());
                        }
                    });
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
}

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

import com.armedia.acm.configserver.exception.ConfigurationException;
import com.armedia.acm.configserver.model.ApplicationProperty;
import com.armedia.acm.configserver.model.ArkcaseConfig;
import com.armedia.acm.configserver.model.DatabaseChangeEvent;
import com.armedia.acm.configserver.repository.ApplicationPropertyRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@Profile("run-with-db")
public class DatabaseConfigurationService implements ConfigurationService
{

    private static final Logger logger = LoggerFactory.getLogger(DatabaseConfigurationService.class);

    private final ApplicationPropertyRepository applicationPropertyRepository;
    private final ArkcaseConfig arkcaseConfig;
    private final ApplicationEventPublisher eventPublisher;

    public DatabaseConfigurationService(ApplicationPropertyRepository applicationPropertyRepository, ArkcaseConfig arkcaseConfig,
            ApplicationEventPublisher eventPublisher)
    {
        this.applicationPropertyRepository = applicationPropertyRepository;
        this.arkcaseConfig = arkcaseConfig;
        this.eventPublisher = eventPublisher;
    }

    /**
     *
     * @param properties
     *            - properties for update
     * @param applicationName
     *            - name of the file whose properties will be updated
     *            format: case-en, case-en-foia, arkcase, arkcase-foia
     * @throws ConfigurationException
     */
    @Override
    @Transactional
    public synchronized void updateProperties(Map<String, Object> properties, String applicationName) throws ConfigurationException
    {
        try
        {
            var appNameWithoutProfile = !applicationName.contains(RUNTIME) ? applicationName
                    : applicationName.substring(0, applicationName.indexOf(RUNTIME));
            var label = extractLabelFromFileName(appNameWithoutProfile, arkcaseConfig.getLanguages());

            for (var entry : properties.entrySet())
            {
                var config = this.applicationPropertyRepository.findByApplicationAndProfileAndKey(appNameWithoutProfile, RUNTIME,
                        entry.getKey());
                if (config == null)
                {
                    config = new ApplicationProperty();
                    config.setApplication(appNameWithoutProfile);
                    config.setProfile(RUNTIME);
                    config.setLabel(extractLabelFromFileName(appNameWithoutProfile, this.arkcaseConfig.getLanguages()));
                    config.setKey(entry.getKey());
                    config.setValue(entry.getValue().toString());
                }
                else
                {
                    config.setValue(entry.getValue().toString());
                }
                this.applicationPropertyRepository.save(config);
            }

            eventPublisher.publishEvent(new DatabaseChangeEvent(this, label,
                    String.format("Updated properties: %s for application: %s", properties, appNameWithoutProfile)));
        }
        catch (Exception e)
        {
            logger.warn("Failed to update properties for application [{}]", applicationName);
            throw new ConfigurationException("Failed to update properties.", e);
        }
    }

    @Override
    @Transactional
    public synchronized void removeProperties(List<String> properties, String applicationName) throws ConfigurationException
    {
        var appNameWithoutProfile = !applicationName.contains(RUNTIME) ? applicationName
                : applicationName.substring(0, applicationName.indexOf(RUNTIME));
        var label = extractLabelFromFileName(appNameWithoutProfile, arkcaseConfig.getLanguages());

        try
        {
            this.applicationPropertyRepository.deleteAllByApplicationAndProfileAndKeyIn(appNameWithoutProfile, RUNTIME, properties);

            eventPublisher.publishEvent(new DatabaseChangeEvent(this, label,
                    String.format("Deleted properties: %s from application: %s", properties, appNameWithoutProfile)));
        }
        catch (Exception e)
        {
            logger.warn("Failed to remove properties for application [{}]", applicationName);
            throw new ConfigurationException("Failed to remove properties.", e);
        }
    }

    @Override
    @Transactional
    public void resetFilePropertiesToDefault(String applicationName) throws ConfigurationException
    {
        var appNameWithoutProfile = !applicationName.contains(RUNTIME) ? applicationName
                : applicationName.substring(0, applicationName.indexOf(RUNTIME));
        var label = extractLabelFromFileName(appNameWithoutProfile, arkcaseConfig.getLanguages());

        try
        {
            this.applicationPropertyRepository.deleteAllByApplicationAndProfile(appNameWithoutProfile, RUNTIME);
            eventPublisher.publishEvent(new DatabaseChangeEvent(this, label,
                    String.format("Reset properties for application: %s", appNameWithoutProfile)));
        }
        catch (Exception e)
        {
            logger.warn("Failed to reset properties for application [{}]", applicationName);
            throw new ConfigurationException("Failed to reset properties.", e);
        }
    }

    @Override
    public void resetConfigurationBrandingFilesToDefault()
    {
        // not implemented
    }

    @Override
    @Transactional
    public void resetPropertiesToDefault() throws ConfigurationException
    {
        try
        {
            this.applicationPropertyRepository.findAllUniqueApplicationsWithRuntimeProfile()
                    .forEach(application -> {
                        this.applicationPropertyRepository.deleteAllByApplicationAndProfile(application, RUNTIME);
                        var label = extractLabelFromFileName(application, arkcaseConfig.getLanguages());
                        eventPublisher.publishEvent(new DatabaseChangeEvent(this, label,
                                String.format("Reset properties for application: %s", application)));
                    });
        }
        catch (Exception e)
        {
            logger.warn("Failed to reset all properties to default");
            throw new ConfigurationException("Failed to reset all properties to default.", e);
        }
    }

    @Override
    public List<String> getModulesNames()
    {
        return this.applicationPropertyRepository.findAllUniqueLabels();
    }
}

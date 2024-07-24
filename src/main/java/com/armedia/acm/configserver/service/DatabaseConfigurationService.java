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

import static com.armedia.acm.configserver.service.ConfigUtils.findProfile;

import com.armedia.acm.configserver.exception.ConfigurationException;
import com.armedia.acm.configserver.model.AppConfig;
import com.armedia.acm.configserver.model.ArkcaseConfig;
import com.armedia.acm.configserver.repository.AppConfigRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@Profile("run-with-db")
public class DatabaseConfigurationService implements ConfigurationService
{

    private static final Logger logger = LoggerFactory.getLogger(DatabaseConfigurationService.class);

    private final AppConfigRepository appConfigRepository;
    private final ArkcaseConfig arkcaseConfig;

    public DatabaseConfigurationService(AppConfigRepository appConfigRepository, ArkcaseConfig arkcaseConfig)
    {
        this.appConfigRepository = appConfigRepository;
        this.arkcaseConfig = arkcaseConfig;
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
    public synchronized void updateProperties(Map<String, Object> properties, String applicationName) throws ConfigurationException
    {
        try
        {
            for (var entry : properties.entrySet())
            {
                var matchedProfile = findProfile(applicationName, this.arkcaseConfig.getProfiles());

                var profile = matchedProfile.orElse("default");

                var appNameWithoutProfile = matchedProfile.map(s -> applicationName.substring(0, applicationName.indexOf(s) - 1))
                        .orElse(applicationName);

                var config = this.appConfigRepository.findByApplicationAndProfileAndKey(appNameWithoutProfile, profile, entry.getKey());
                if (config == null)
                {
                    // label is missing
                    config = new AppConfig();
                    config.setApplication(appNameWithoutProfile);
                    config.setKey(entry.getKey());
                    config.setValue(entry.getValue().toString());
                    config.setProfile(profile);
                }
                else
                {
                    config.setValue(entry.getValue().toString());
                }
                this.appConfigRepository.save(config);
            }
        }
        catch (Exception e)
        {
            logger.warn("Failed to update properties for application [{}]", applicationName);
            throw new ConfigurationException("Failed to update properties.", e);
        }
    }

    @Override
    public synchronized void removeProperties(List<String> properties, String applicationName) throws ConfigurationException
    {
        var matchedProfile = findProfile(applicationName, this.arkcaseConfig.getProfiles());

        var appNameWithoutProfile = matchedProfile.map(s -> applicationName.substring(0, applicationName.indexOf(s) - 1))
                .orElse(applicationName);

        try
        {
            this.appConfigRepository.deleteByApplicationAndProfileAndKeyIn(appNameWithoutProfile, "runtime", properties);
        }
        catch (Exception e)
        {
            logger.warn("Failed to remove properties for application [{}]", applicationName);
            throw new ConfigurationException("Failed to remove properties.", e);
        }
    }

    @Override
    public void resetFilePropertiesToDefault(String applicationName) throws ConfigurationException
    {
        var matchedProfile = findProfile(applicationName, this.arkcaseConfig.getProfiles());

        var appNameWithoutProfile = matchedProfile.map(s -> applicationName.substring(0, applicationName.indexOf(s) - 1))
                .orElse(applicationName);

        try
        {
            this.appConfigRepository.deleteByApplicationAndProfile(appNameWithoutProfile, "runtime");
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
    public void resetPropertiesToDefault() throws ConfigurationException
    {
        try
        {
            this.appConfigRepository.deleteAllByProfile("runtime");
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
        return this.appConfigRepository.findAllUniqueLabels();
    }
}

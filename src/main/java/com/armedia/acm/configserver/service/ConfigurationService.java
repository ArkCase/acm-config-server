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

import java.io.IOException;
import java.nio.file.NoSuchFileException;
import java.util.List;
import java.util.Map;

public interface ConfigurationService
{
    /**
     * Update properties in yaml file with name - applicationName,
     * updated properties will be written in the runtime file.
     *
     * @param properties - properties for update
     * @param applicationName - name of the file whose properties will be updated
     * @throws ConfigurationException
     */
    void updateProperties(Map<String, Object> properties, String applicationName) throws ConfigurationException;

    void removeProperties(List<String> properties, String applicationName) throws ConfigurationException;

    void resetPropertiesToDefault() throws ConfigurationException;

    void resetFilePropertiesToDefault(String applicationName) throws NoSuchFileException, ConfigurationException;

    void resetConfigurationBrandingFilesToDefault() throws ConfigurationException;
}

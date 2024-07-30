package com.armedia.acm.configserver.repository;

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

import static com.armedia.acm.configserver.service.ConfigurationService.RUNTIME;

import org.springframework.cloud.config.environment.Environment;
import org.springframework.cloud.config.environment.PropertySource;
import org.springframework.cloud.config.server.environment.EnvironmentRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@Profile("run-with-db")
public class CustomEnvironmentRepository implements EnvironmentRepository {

    private final ApplicationPropertyRepository applicationPropertyRepository;

    public CustomEnvironmentRepository(ApplicationPropertyRepository applicationPropertyRepository)
    {
        this.applicationPropertyRepository = applicationPropertyRepository;
    }

    @Override
    public Environment findOne(String application, String profile, String label) {
        var profiles = List.of(RUNTIME);

        var properties = applicationPropertyRepository.findByApplicationAndProfileIn(application, profiles);

        var environment = new Environment(application, profiles.toArray(new String[0]), null, null, null);

        var propertySourceMap = new HashMap<String, Map<String, String>>();
        for (var property : properties)
        {
            var sourceName = String.format("%s-%s.yaml", application, property.getProfile());
            propertySourceMap.computeIfAbsent(sourceName, k -> new HashMap<>()).put(property.getKey(), property.getValue());
        }

        for (var entry : propertySourceMap.entrySet())
        {
            environment.add(new PropertySource(entry.getKey(), entry.getValue()));
        }

        return environment;
    }
}


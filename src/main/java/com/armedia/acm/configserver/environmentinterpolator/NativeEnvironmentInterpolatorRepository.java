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
package com.armedia.acm.configserver.environmentinterpolator;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.config.environment.Environment;
import org.springframework.cloud.config.environment.PropertySource;
import org.springframework.cloud.config.server.environment.EnvironmentRepository;
import org.springframework.cloud.config.server.environment.NativeEnvironmentProperties;
import org.springframework.cloud.config.server.environment.NativeEnvironmentRepository;
import org.springframework.cloud.config.server.environment.SearchPathLocator;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;

class NativeEnvironmentInterpolatorRepository implements EnvironmentRepository, SearchPathLocator, Ordered
{
    private final Logger log = LoggerFactory.getLogger(getClass());

    private final NativeEnvironmentInterpolator cloudMapper;

    private final NativeEnvironmentRepository delegate;

    NativeEnvironmentInterpolatorRepository(NativeEnvironmentInterpolator cloudMapper, ConfigurableEnvironment environment, NativeEnvironmentProperties properties)
    {
        this.cloudMapper = cloudMapper;
        this.delegate = new NativeEnvironmentRepository(environment, properties)
        {
            @Override
            public Environment findOne(String config, String profile, String label)
            {
                Environment env = super.findOne(config, profile, label);
                NativeEnvironmentInterpolatorRepository.this.log.info(
                        "Applying cloud mappings for the environment {} ({}), version {}, with profiles {}", env.getName(),
                        env.getLabel(), env.getVersion(), env.getProfiles());

                // We call the superclass method first, so we can
                // make sure we don't get hijacked...
                Environment result = new Environment(env.getName(), env.getProfiles(), env.getLabel(), env.getVersion(), env.getState());
                for (PropertySource source : env.getPropertySources())
                {
                    NativeEnvironmentInterpolatorRepository.this.log.debug("Mapping for PropertySource [{}]", source.getName());

                    Map<Object, Object> map = new LinkedHashMap<>(
                            source.getSource());
                    for (Map.Entry<Object, Object> entry : new LinkedHashSet<>(map.entrySet()))
                    {
                        Object key = entry.getKey();
                        String name = key.toString();
                        String value = entry.getValue().toString();

                        NativeEnvironmentInterpolatorRepository.this.log.trace("Mapping [{}]=[{}]", name, value);
                        String newValue = NativeEnvironmentInterpolatorRepository.this.cloudMapper.map(name, value);

                        // If we're supposed to remove it, then we do so
                        if (newValue == null)
                        {
                            map.remove(key);
                            continue;
                        }

                        // It's ok to do a != comparison, since the exact
                        // same reference will be returned if there is no
                        // mapping to be performed, and thus no need to
                        // operate on the map
                        if (newValue != value)
                        {
                            map.put(key, newValue);
                        }
                    }
                    result.add(new PropertySource(source.getName(), map));
                }
                return result;
            }
        };
        this.delegate.setDefaultLabel("native-cloud-environment");
    }

    @Override
    public int getOrder()
    {
        return this.delegate.getOrder();
    }

    @Override
    public Locations getLocations(String application, String profile, String label)
    {
        return this.delegate.getLocations(application, profile, label);
    }

    @Override
    public Environment findOne(String application, String profile, String label)
    {
        return this.delegate.findOne(application, profile, label);
    }
}
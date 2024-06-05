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
package com.armedia.acm.configserver.environment;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.config.environment.Environment;
import org.springframework.cloud.config.environment.PropertySource;
import org.springframework.cloud.config.server.environment.NativeEnvironmentProperties;
import org.springframework.cloud.config.server.environment.NativeEnvironmentRepository;
import org.springframework.core.env.ConfigurableEnvironment;

import com.armedia.acm.configserver.environment.mapper.CloudMapper;

public class NativeCloudEnvironmentRepository extends NativeEnvironmentRepository
{
    private static final BiFunction<String, String, String> NO_MAP = (k, v) -> v;

    @Autowired(required = false)
    private Optional<CloudMapper> cloudMapper;

    private final BiFunction<String, String, String> mapper;

    public NativeCloudEnvironmentRepository(ConfigurableEnvironment environment, NativeEnvironmentProperties properties)
    {
        super(environment, properties);
        this.mapper = this.cloudMapper.isPresent() ? this.cloudMapper.get()::map
                : NativeCloudEnvironmentRepository.NO_MAP;
    }

    @Override
    protected Environment clean(Environment environment)
    {
        // We call the superclass method first, so we can
        // make sure we don't get hijacked...
        Environment result = super.clean(environment);
        for (PropertySource source : environment.getPropertySources())
        {
            Map<Object, Object> map = new LinkedHashMap<>(
                    source.getSource());
            for (Map.Entry<Object, Object> entry : new LinkedHashSet<>(map.entrySet()))
            {
                Object key = entry.getKey();
                String name = key.toString();
                String value = entry.getValue().toString();

                String newValue = this.mapper.apply(name, value);

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
}
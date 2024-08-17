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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.config.server.config.ConfigServerProperties;
import org.springframework.cloud.config.server.environment.NativeEnvironmentProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.ConfigurableEnvironment;

@Configuration
@Profile("native-interpolator")
public class NativeEnvironmentInterpolatorRepositoryConfiguration
{
    private final Logger log = LoggerFactory.getLogger(getClass());

    @Autowired
    private NativeEnvironmentInterpolator cloudMapper;

    @Bean
    @Lazy
    public NativeEnvironmentInterpolatorRepository nativeCloudEnvironmentRepository(NativeEnvironmentInterpolatorRepositoryFactory factory,
            NativeEnvironmentProperties environmentProperties)
    {
        this.log.info("Creating a new NativeEnvironmentInterpolatorRepository");
        return factory.build(environmentProperties);
    }

    @Bean
    @Lazy
    public NativeEnvironmentInterpolatorRepositoryFactory nativeCloudEnvironmentRepositoryFactory(
            ConfigurableEnvironment environment, ConfigServerProperties properties)
    {
        return new NativeEnvironmentInterpolatorRepositoryFactory(this.cloudMapper, environment, properties);
    }

}
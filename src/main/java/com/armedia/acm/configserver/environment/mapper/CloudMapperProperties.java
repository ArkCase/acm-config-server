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
package com.armedia.acm.configserver.environment.mapper;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Profile("native-cloud")
@Configuration
@ConfigurationProperties("cloud-mapper")
public class CloudMapperProperties
{
    private static final int DEFAULT_THREADS = Runtime.getRuntime().availableProcessors() * 2;
    private static final int MAX_THREADS = Runtime.getRuntime().availableProcessors() * 8;

    private String namespace = null;
    private Boolean enabled = false;
    private Boolean failIfMissing = false;
    private Boolean disableInterpolator = false;
    private Integer threads = CloudMapperProperties.DEFAULT_THREADS;

    public String getNamespace()
    {
        return this.namespace;
    }

    public void setNamespace(String namespace)
    {
        this.namespace = namespace;
    }

    public Boolean isEnabled()
    {
        return this.enabled;
    }

    public void setEnabled(Boolean enabled)
    {
        this.enabled = enabled;
    }

    public Boolean isFailIfMissing()
    {
        return Boolean.TRUE.equals(this.failIfMissing);
    }

    public void setFailIfMissing(Boolean FailIfMissing)
    {
        this.failIfMissing = FailIfMissing;
    }

    public Boolean isDisableInterpolator()
    {
        return this.disableInterpolator;
    }

    public void setDisableInterpolator(Boolean disableInterpolator)
    {
        this.disableInterpolator = disableInterpolator;
    }

    public Integer getThreads()
    {
        return this.threads;
    }

    public void setThreads(int threads)
    {
        if (threads <= 0)
        {
            threads = CloudMapperProperties.DEFAULT_THREADS;
        }
        this.threads = Math.min(threads, CloudMapperProperties.MAX_THREADS);
    }
}
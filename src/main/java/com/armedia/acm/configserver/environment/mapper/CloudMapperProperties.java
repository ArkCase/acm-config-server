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

import java.util.Objects;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import com.nimbusds.oauth2.sdk.util.StringUtils;

@Configuration
@ConfigurationProperties("cloud-mapper")
public class CloudMapperProperties
{
    private static final Boolean DEFAULT_ENABLED = Boolean.TRUE;
    private static final Boolean DEFAULT_FAIL_IF_MISSING = Boolean.FALSE;
    private static final Boolean DEFAULT_MISSING_AS_EMPTY = Boolean.TRUE;
    private static final Boolean DEFAULT_DISABLE_INTERPOLATOR = Boolean.FALSE;
    private static final int DEFAULT_THREADS = Runtime.getRuntime().availableProcessors() * 2;
    private static final int MAX_THREADS = Runtime.getRuntime().availableProcessors() * 8;

    private String namespace = null;
    private Boolean enabled = null;
    private Boolean failIfMissing = null;
    private Boolean missingAsEmpty = null;
    private Boolean disableInterpolator = null;
    private Integer threads = null;

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
        return Objects.requireNonNullElse(this.enabled, CloudMapperProperties.DEFAULT_ENABLED && StringUtils.isNotBlank(this.namespace));
    }

    public void setEnabled(Boolean enabled)
    {
        this.enabled = enabled;
    }

    public Boolean isFailIfMissing()
    {
        return Objects.requireNonNullElse(this.failIfMissing, CloudMapperProperties.DEFAULT_FAIL_IF_MISSING);
    }

    public void setFailIfMissing(Boolean failIfMissing)
    {
        this.failIfMissing = failIfMissing;
    }

    public Boolean isMissingAsEmpty()
    {
        return Objects.requireNonNullElse(this.missingAsEmpty, CloudMapperProperties.DEFAULT_MISSING_AS_EMPTY);
    }

    public void setMissingAsEmpty(Boolean missingAsEmpty)
    {
        this.missingAsEmpty = missingAsEmpty;
    }

    public Boolean isDisableInterpolator()
    {
        return Objects.requireNonNullElse(this.disableInterpolator, CloudMapperProperties.DEFAULT_DISABLE_INTERPOLATOR);
    }

    public void setDisableInterpolator(Boolean disableInterpolator)
    {
        this.disableInterpolator = disableInterpolator;
    }

    public Integer getThreads()
    {
        return Objects.requireNonNullElse(this.threads, CloudMapperProperties.DEFAULT_THREADS);
    }

    public void setThreads(Integer threads)
    {
        if (threads == null)
        {
            this.threads = null;
        }
        else
        {
            this.threads = (threads <= 0)
                    ? CloudMapperProperties.DEFAULT_THREADS
                    : Math.min(threads, CloudMapperProperties.MAX_THREADS);
        }
    }
}
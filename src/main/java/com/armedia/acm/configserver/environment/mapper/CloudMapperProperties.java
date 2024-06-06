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

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Lazy
@Component
public class CloudMapperProperties
{
    protected static final Boolean DEFAULT_ENABLED = Boolean.TRUE;
    protected static final Boolean DEFAULT_DISABLE_INTERPOLATOR = Boolean.FALSE;
    protected static final Boolean DEFAULT_MISSING_AS_EMPTY = Boolean.FALSE;

    public static CloudMapperProperties DEFAULT = new CloudMapperProperties(null, CloudMapperProperties.DEFAULT_ENABLED,
            CloudMapperProperties.DEFAULT_DISABLE_INTERPOLATOR, CloudMapperProperties.DEFAULT_MISSING_AS_EMPTY);

    public final String namespace;
    public final boolean enabled;
    public final boolean disableInterpolator;
    public final boolean missingAsEmpty;

    public CloudMapperProperties(
            @Value("${cloud-mapper.namespace:#{null}}") String namespace,
            @Value("${cloud-mapper.enabled:#{null}}") Boolean enabled,
            @Value("${cloud-mapper.disableInterpolator:#{null}}") Boolean disableInterpolator,
            @Value("${cloud-mapper.missingAsEmpty:#{null}}") Boolean missingAsEmpty)
    {
        this.namespace = namespace;
        this.enabled = StringUtils.isNotBlank(this.namespace) && Objects.requireNonNullElse(enabled, CloudMapperProperties.DEFAULT_ENABLED);
        this.disableInterpolator = Objects.requireNonNullElse(disableInterpolator, CloudMapperProperties.DEFAULT_DISABLE_INTERPOLATOR);
        this.missingAsEmpty = Objects.requireNonNullElse(missingAsEmpty, CloudMapperProperties.DEFAULT_MISSING_AS_EMPTY);
    }
}
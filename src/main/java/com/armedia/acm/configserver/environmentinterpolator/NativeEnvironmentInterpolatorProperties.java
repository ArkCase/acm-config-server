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

import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Lazy
@Component
public class NativeEnvironmentInterpolatorProperties
{
    protected static final Boolean DEFAULT_ENABLED = Boolean.TRUE;
    protected static final Boolean DEFAULT_ENABLE_INTERPOLATOR = Boolean.TRUE;
    protected static final Boolean DEFAULT_ENABLE_INTERPOLATOR_EXTRAS = Boolean.FALSE;
    protected static final Boolean DEFAULT_MISSING_AS_EMPTY = Boolean.FALSE;

    public static NativeEnvironmentInterpolatorProperties DEFAULT = new NativeEnvironmentInterpolatorProperties(
            NativeEnvironmentInterpolatorProperties.DEFAULT_ENABLED,
            NativeEnvironmentInterpolatorProperties.DEFAULT_ENABLE_INTERPOLATOR,
            NativeEnvironmentInterpolatorProperties.DEFAULT_ENABLE_INTERPOLATOR_EXTRAS,
            NativeEnvironmentInterpolatorProperties.DEFAULT_MISSING_AS_EMPTY);

    public final boolean enabled;
    public final boolean interpolator;
    public final boolean interpolatorExtras;
    public final boolean missingAsEmpty;

    public NativeEnvironmentInterpolatorProperties(
            @Value("${cloud-mapper.enabled:#{null}}") Boolean enabled,
            @Value("${cloud-mapper.interpolator:#{null}}") Boolean interpolator,
            @Value("${cloud-mapper.interpolatorExtras:#{null}}") Boolean interpolatorExtras,
            @Value("${cloud-mapper.missingAsEmpty:#{null}}") Boolean missingAsEmpty)
    {
        // This can serve as the master flag to disable the whole thing in one go, if needed
        this.enabled = ObjectUtils.defaultIfNull(enabled, NativeEnvironmentInterpolatorProperties.DEFAULT_ENABLED);
        this.interpolator = this.enabled
                && ObjectUtils.defaultIfNull(interpolator, NativeEnvironmentInterpolatorProperties.DEFAULT_ENABLE_INTERPOLATOR);
        this.interpolatorExtras = this.enabled
                && ObjectUtils.defaultIfNull(interpolatorExtras, NativeEnvironmentInterpolatorProperties.DEFAULT_ENABLE_INTERPOLATOR_EXTRAS);
        this.missingAsEmpty = this.enabled
                && ObjectUtils.defaultIfNull(missingAsEmpty, NativeEnvironmentInterpolatorProperties.DEFAULT_MISSING_AS_EMPTY);
    }
}
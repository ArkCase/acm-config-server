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

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import io.kubernetes.client.util.Config;

@Lazy
@Component
public class CloudMapperProperties
{
    private static final Logger LOG = LoggerFactory.getLogger(CloudMapperProperties.class);

    private static final Path SERVICEACCOUNT_NAMESPACE_PATH = Paths.get(Config.SERVICEACCOUNT_NAMESPACE_PATH);

    protected static final Boolean DEFAULT_ENABLE_INTERPOLATOR = Boolean.TRUE;
    protected static final Boolean DEFAULT_ENABLE_CLOUD = Boolean.FALSE;
    protected static final Boolean DEFAULT_MISSING_AS_EMPTY = Boolean.FALSE;
    protected static final String DEFAULT_NAMESPACE;
    static
    {
        // If the namespace is not given, grab the running pods' namespace
        String namespace = null;
        try (Reader r = Files.newBufferedReader(CloudMapperProperties.SERVICEACCOUNT_NAMESPACE_PATH, StandardCharsets.UTF_8))
        {
            namespace = IOUtils.toString(r);
            CloudMapperProperties.LOG.debug("Read the default namespace [{}] from [{}]", namespace,
                    CloudMapperProperties.SERVICEACCOUNT_NAMESPACE_PATH);
        }
        catch (IOException e)
        {
            CloudMapperProperties.LOG.warn("Failed to read the pod's namespace from the file at [{}], it must be configured explicitly",
                    CloudMapperProperties.SERVICEACCOUNT_NAMESPACE_PATH, e.getMessage());
        }

        if (StringUtils.isNotBlank(namespace))
        {
            CloudMapperProperties.LOG.debug("The default namespace for the CloudMapper will be {}", namespace);
        }

        DEFAULT_NAMESPACE = namespace;
    }

    public static CloudMapperProperties DEFAULT = new CloudMapperProperties( //
            CloudMapperProperties.DEFAULT_ENABLE_INTERPOLATOR, //
            CloudMapperProperties.DEFAULT_ENABLE_CLOUD, //
            CloudMapperProperties.DEFAULT_MISSING_AS_EMPTY, //
            CloudMapperProperties.DEFAULT_NAMESPACE //
    );

    public final boolean enableInterpolator;
    public final boolean enableCloud;
    public final boolean missingAsEmpty;
    public final String namespace;

    public CloudMapperProperties(
            @Value("${cloud-mapper.enableInterpolator:#{null}}") Boolean enableInterpolator,
            @Value("${cloud-mapper.enabledCloud:#{null}}") Boolean enableCloud,
            @Value("${cloud-mapper.missingAsEmpty:#{null}}") Boolean missingAsEmpty,
            @Value("${cloud-mapper.namespace:#{null}}") String namespace)
    {
        this.namespace = Objects.toString(namespace, CloudMapperProperties.DEFAULT_NAMESPACE);
        this.enableInterpolator = Objects.requireNonNullElse(enableInterpolator, CloudMapperProperties.DEFAULT_ENABLE_INTERPOLATOR);
        this.enableCloud = StringUtils.isNotBlank(this.namespace)
                && Objects.requireNonNullElse(enableCloud, CloudMapperProperties.DEFAULT_ENABLE_CLOUD);
        this.missingAsEmpty = Objects.requireNonNullElse(missingAsEmpty, CloudMapperProperties.DEFAULT_MISSING_AS_EMPTY);
    }
}
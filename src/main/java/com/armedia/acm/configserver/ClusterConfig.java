/*-
 * #%L
 * acm-config-server
 * %%
 * Copyright (C) 2019 - 2023 ArkCase LLC
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
package com.armedia.acm.configserver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.armedia.acm.curator.Session;
import com.armedia.acm.curator.tools.Tools;

public class ClusterConfig
{
    private static final Logger LOG = LoggerFactory.getLogger(ClusterConfig.class);
    private static final Integer INT_ZERO = Integer.valueOf(0);

    public static Session.Builder newSessionBuilder()
    {
        Integer sessionTimeout = Tools.coalesce(ClusterConfig.getInteger("session.timeout"), ClusterConfig.INT_ZERO);
        Integer connectionTimeout = Tools.coalesce(ClusterConfig.getInteger("connection.timeout"), ClusterConfig.INT_ZERO);
        Integer retryCount = Tools.coalesce(ClusterConfig.getInteger("retry.count"), ClusterConfig.INT_ZERO);
        Integer retryDelay = Tools.coalesce(ClusterConfig.getInteger("retry.delay"), ClusterConfig.INT_ZERO);
        return new Session.Builder() //
                .sessionTimeout(sessionTimeout) //
                .connectionTimeout(connectionTimeout) //
                .retryCount(retryCount) //
                .retryDelay(retryDelay) //
                .connect(ClusterConfig.get("connect")) //
        ;
    }

    private static String sanitize(String value)
    {
        return (value != null ? value.trim() : value);
    }

    public static Integer getInteger(String name)
    {
        String v = ClusterConfig.get(name);
        if (v == null)
        {
            return null;
        }
        try
        {
            return Integer.valueOf(v);
        }
        catch (NumberFormatException e)
        {
            ClusterConfig.LOG.error("Invalid int syntax for value '{}': [{}]", name, v);
            return null;
        }
    }

    public static String get(String name)
    {
        String prop = null;
        String v = null;

        // Step one: is it set via a system property?
        prop = String.format("arkcase.cluster.%s", name);
        ClusterConfig.LOG.debug("Seeking system property [{}]", prop);
        v = System.getProperty(prop);
        if (v != null)
        {
            ClusterConfig.LOG.debug("Value found: [{}]", v);
            return ClusterConfig.sanitize(v);
        }

        // Step two: is it set via environment variable?
        prop = String.format("ARKCASE_CLUSTER_%s", name.replace(".", "_").toUpperCase());
        ClusterConfig.LOG.debug("Seeking environment property [{}]", prop);
        v = System.getenv(prop);
        if (v != null)
        {
            ClusterConfig.LOG.debug("Value found: [{}]", v);
            return ClusterConfig.sanitize(v);
        }

        // No matches? Return null...
        ClusterConfig.LOG.debug("No values found for property '{}'", name);
        return null;
    }
}
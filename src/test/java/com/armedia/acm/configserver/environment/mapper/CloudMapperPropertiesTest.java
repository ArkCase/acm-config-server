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

import java.util.UUID;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class CloudMapperPropertiesTest
{
    @Test
    public void testCloudMapperProperties()
    {
        CloudMapperProperties props = null;
        String namespace = null;
        Boolean enabled = null;
        Boolean enableCloud = null;
        Boolean enableInterpolator = null;
        Boolean missingAsEmpty = null;

        props = CloudMapperProperties.DEFAULT;
        Assertions.assertEquals(CloudMapperProperties.DEFAULT_NAMESPACE, props.namespace);
        Assertions.assertFalse(props.cloud);
        Assertions.assertEquals(CloudMapperProperties.DEFAULT_ENABLE_INTERPOLATOR, props.interpolator);
        Assertions.assertEquals(CloudMapperProperties.DEFAULT_MISSING_AS_EMPTY, props.missingAsEmpty);

        namespace = null;
        enableCloud = null;
        enableInterpolator = null;
        missingAsEmpty = null;
        props = new CloudMapperProperties(enabled, enableInterpolator, enableCloud, missingAsEmpty, namespace);
        Assertions.assertEquals(CloudMapperProperties.DEFAULT_NAMESPACE, props.namespace);
        Assertions.assertFalse(props.cloud);
        Assertions.assertEquals(CloudMapperProperties.DEFAULT_ENABLE_INTERPOLATOR, props.interpolator);
        Assertions.assertEquals(CloudMapperProperties.DEFAULT_MISSING_AS_EMPTY, props.missingAsEmpty);

        namespace = UUID.randomUUID().toString();
        enableCloud = null;
        enableInterpolator = null;
        missingAsEmpty = null;
        props = new CloudMapperProperties(enabled, enableInterpolator, enableCloud, missingAsEmpty, namespace);
        Assertions.assertEquals(namespace, props.namespace);
        Assertions.assertEquals(CloudMapperProperties.DEFAULT_ENABLE_CLOUD, props.cloud);
        Assertions.assertEquals(CloudMapperProperties.DEFAULT_ENABLE_INTERPOLATOR, props.interpolator);
        Assertions.assertEquals(CloudMapperProperties.DEFAULT_MISSING_AS_EMPTY, props.missingAsEmpty);

        namespace = UUID.randomUUID().toString();
        enableCloud = true;
        enableInterpolator = null;
        missingAsEmpty = null;
        props = new CloudMapperProperties(enabled, enableInterpolator, enableCloud, missingAsEmpty, namespace);
        Assertions.assertEquals(namespace, props.namespace);
        Assertions.assertTrue(props.cloud);
        Assertions.assertEquals(CloudMapperProperties.DEFAULT_ENABLE_INTERPOLATOR, props.interpolator);
        Assertions.assertEquals(CloudMapperProperties.DEFAULT_MISSING_AS_EMPTY, props.missingAsEmpty);

        namespace = UUID.randomUUID().toString();
        enableCloud = false;
        enableInterpolator = null;
        missingAsEmpty = null;
        props = new CloudMapperProperties(enabled, enableInterpolator, enableCloud, missingAsEmpty, namespace);
        Assertions.assertEquals(namespace, props.namespace);
        Assertions.assertFalse(props.cloud);
        Assertions.assertEquals(CloudMapperProperties.DEFAULT_ENABLE_INTERPOLATOR, props.interpolator);
        Assertions.assertEquals(CloudMapperProperties.DEFAULT_MISSING_AS_EMPTY, props.missingAsEmpty);

        namespace = UUID.randomUUID().toString();
        enableCloud = null;
        enableInterpolator = null;
        missingAsEmpty = null;
        for (boolean di : new boolean[] { Boolean.TRUE, Boolean.FALSE })
        {
            props = new CloudMapperProperties(enabled, di, enableCloud, missingAsEmpty, namespace);
            Assertions.assertEquals(namespace, props.namespace);
            Assertions.assertEquals(CloudMapperProperties.DEFAULT_ENABLE_CLOUD, props.cloud);
            Assertions.assertEquals(di, props.interpolator);
            Assertions.assertEquals(CloudMapperProperties.DEFAULT_MISSING_AS_EMPTY, props.missingAsEmpty);
        }

        namespace = UUID.randomUUID().toString();
        enableCloud = null;
        enableInterpolator = null;
        missingAsEmpty = null;
        for (boolean me : new boolean[] { Boolean.TRUE, Boolean.FALSE })
        {
            props = new CloudMapperProperties(enabled, enableInterpolator, enableCloud, me, namespace);
            Assertions.assertEquals(namespace, props.namespace);
            Assertions.assertEquals(CloudMapperProperties.DEFAULT_ENABLE_CLOUD, props.cloud);
            Assertions.assertEquals(CloudMapperProperties.DEFAULT_ENABLE_INTERPOLATOR, props.interpolator);
            Assertions.assertEquals(me, props.missingAsEmpty);
        }
    }
}

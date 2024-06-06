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
        Boolean disableInterpolator = null;
        Boolean missingAsEmpty = null;

        props = CloudMapperProperties.DEFAULT;
        Assertions.assertNull(props.namespace);
        Assertions.assertFalse(props.enabled);
        Assertions.assertEquals(CloudMapperProperties.DEFAULT_DISABLE_INTERPOLATOR, props.disableInterpolator);
        Assertions.assertEquals(CloudMapperProperties.DEFAULT_MISSING_AS_EMPTY, props.missingAsEmpty);

        namespace = null;
        enabled = null;
        disableInterpolator = null;
        missingAsEmpty = null;
        props = new CloudMapperProperties(namespace, enabled, disableInterpolator, missingAsEmpty);
        Assertions.assertNull(props.namespace);
        Assertions.assertFalse(props.enabled);
        Assertions.assertEquals(CloudMapperProperties.DEFAULT_DISABLE_INTERPOLATOR, props.disableInterpolator);
        Assertions.assertEquals(CloudMapperProperties.DEFAULT_MISSING_AS_EMPTY, props.missingAsEmpty);

        namespace = UUID.randomUUID().toString();
        enabled = null;
        disableInterpolator = null;
        missingAsEmpty = null;
        props = new CloudMapperProperties(namespace, enabled, disableInterpolator, missingAsEmpty);
        Assertions.assertEquals(namespace, props.namespace);
        Assertions.assertEquals(CloudMapperProperties.DEFAULT_ENABLED, props.enabled);
        Assertions.assertEquals(CloudMapperProperties.DEFAULT_DISABLE_INTERPOLATOR, props.disableInterpolator);
        Assertions.assertEquals(CloudMapperProperties.DEFAULT_MISSING_AS_EMPTY, props.missingAsEmpty);

        namespace = UUID.randomUUID().toString();
        enabled = true;
        disableInterpolator = null;
        missingAsEmpty = null;
        props = new CloudMapperProperties(namespace, enabled, disableInterpolator, missingAsEmpty);
        Assertions.assertEquals(namespace, props.namespace);
        Assertions.assertTrue(props.enabled);
        Assertions.assertEquals(CloudMapperProperties.DEFAULT_DISABLE_INTERPOLATOR, props.disableInterpolator);
        Assertions.assertEquals(CloudMapperProperties.DEFAULT_MISSING_AS_EMPTY, props.missingAsEmpty);

        namespace = UUID.randomUUID().toString();
        enabled = false;
        disableInterpolator = null;
        missingAsEmpty = null;
        props = new CloudMapperProperties(namespace, enabled, disableInterpolator, missingAsEmpty);
        Assertions.assertEquals(namespace, props.namespace);
        Assertions.assertFalse(props.enabled);
        Assertions.assertEquals(CloudMapperProperties.DEFAULT_DISABLE_INTERPOLATOR, props.disableInterpolator);
        Assertions.assertEquals(CloudMapperProperties.DEFAULT_MISSING_AS_EMPTY, props.missingAsEmpty);

        namespace = UUID.randomUUID().toString();
        enabled = null;
        disableInterpolator = null;
        missingAsEmpty = null;
        for (boolean di : new boolean[] { Boolean.TRUE, Boolean.FALSE })
        {
            props = new CloudMapperProperties(namespace, enabled, di, missingAsEmpty);
            Assertions.assertEquals(namespace, props.namespace);
            Assertions.assertEquals(CloudMapperProperties.DEFAULT_ENABLED, props.enabled);
            Assertions.assertEquals(di, props.disableInterpolator);
            Assertions.assertEquals(CloudMapperProperties.DEFAULT_MISSING_AS_EMPTY, props.missingAsEmpty);
        }

        namespace = UUID.randomUUID().toString();
        enabled = null;
        disableInterpolator = null;
        missingAsEmpty = null;
        for (boolean me : new boolean[] { Boolean.TRUE, Boolean.FALSE })
        {
            props = new CloudMapperProperties(namespace, enabled, disableInterpolator, me);
            Assertions.assertEquals(namespace, props.namespace);
            Assertions.assertEquals(CloudMapperProperties.DEFAULT_ENABLED, props.enabled);
            Assertions.assertEquals(CloudMapperProperties.DEFAULT_DISABLE_INTERPOLATOR, props.disableInterpolator);
            Assertions.assertEquals(me, props.missingAsEmpty);
        }
    }
}

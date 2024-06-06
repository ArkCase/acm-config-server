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
        Integer threads = null;

        props = new CloudMapperProperties();
        Assertions.assertNull(props.namespace);
        Assertions.assertFalse(props.enabled);
        Assertions.assertEquals(CloudMapperProperties.DEFAULT_DISABLE_INTERPOLATOR, props.disableInterpolator);
        Assertions.assertEquals(CloudMapperProperties.DEFAULT_MISSING_AS_EMPTY, props.missingAsEmpty);
        Assertions.assertEquals(CloudMapperProperties.DEFAULT_THREADS, props.threads);

        namespace = null;
        enabled = null;
        disableInterpolator = null;
        missingAsEmpty = null;
        threads = null;
        props = new CloudMapperProperties(namespace, enabled, disableInterpolator, missingAsEmpty, threads);
        Assertions.assertNull(props.namespace);
        Assertions.assertFalse(props.enabled);
        Assertions.assertEquals(CloudMapperProperties.DEFAULT_DISABLE_INTERPOLATOR, props.disableInterpolator);
        Assertions.assertEquals(CloudMapperProperties.DEFAULT_MISSING_AS_EMPTY, props.missingAsEmpty);
        Assertions.assertEquals(CloudMapperProperties.DEFAULT_THREADS, props.threads);

        namespace = UUID.randomUUID().toString();
        enabled = null;
        disableInterpolator = null;
        missingAsEmpty = null;
        threads = null;
        props = new CloudMapperProperties(namespace, enabled, disableInterpolator, missingAsEmpty, threads);
        Assertions.assertEquals(namespace, props.namespace);
        Assertions.assertEquals(CloudMapperProperties.DEFAULT_ENABLED, props.enabled);
        Assertions.assertEquals(CloudMapperProperties.DEFAULT_DISABLE_INTERPOLATOR, props.disableInterpolator);
        Assertions.assertEquals(CloudMapperProperties.DEFAULT_MISSING_AS_EMPTY, props.missingAsEmpty);
        Assertions.assertEquals(CloudMapperProperties.DEFAULT_THREADS, props.threads);

        namespace = UUID.randomUUID().toString();
        enabled = true;
        disableInterpolator = null;
        missingAsEmpty = null;
        threads = null;
        props = new CloudMapperProperties(namespace, enabled, disableInterpolator, missingAsEmpty, threads);
        Assertions.assertEquals(namespace, props.namespace);
        Assertions.assertTrue(props.enabled);
        Assertions.assertEquals(CloudMapperProperties.DEFAULT_DISABLE_INTERPOLATOR, props.disableInterpolator);
        Assertions.assertEquals(CloudMapperProperties.DEFAULT_MISSING_AS_EMPTY, props.missingAsEmpty);
        Assertions.assertEquals(CloudMapperProperties.DEFAULT_THREADS, props.threads);

        namespace = UUID.randomUUID().toString();
        enabled = false;
        disableInterpolator = null;
        missingAsEmpty = null;
        threads = null;
        props = new CloudMapperProperties(namespace, enabled, disableInterpolator, missingAsEmpty, threads);
        Assertions.assertEquals(namespace, props.namespace);
        Assertions.assertFalse(props.enabled);
        Assertions.assertEquals(CloudMapperProperties.DEFAULT_DISABLE_INTERPOLATOR, props.disableInterpolator);
        Assertions.assertEquals(CloudMapperProperties.DEFAULT_MISSING_AS_EMPTY, props.missingAsEmpty);
        Assertions.assertEquals(CloudMapperProperties.DEFAULT_THREADS, props.threads);

        namespace = UUID.randomUUID().toString();
        enabled = null;
        disableInterpolator = null;
        missingAsEmpty = null;
        threads = null;
        for (boolean di : new boolean[] { Boolean.TRUE, Boolean.FALSE })
        {
            props = new CloudMapperProperties(namespace, enabled, di, missingAsEmpty, threads);
            Assertions.assertEquals(namespace, props.namespace);
            Assertions.assertEquals(CloudMapperProperties.DEFAULT_ENABLED, props.enabled);
            Assertions.assertEquals(di, props.disableInterpolator);
            Assertions.assertEquals(CloudMapperProperties.DEFAULT_MISSING_AS_EMPTY, props.missingAsEmpty);
            Assertions.assertEquals(CloudMapperProperties.DEFAULT_THREADS, props.threads);
        }

        namespace = UUID.randomUUID().toString();
        enabled = null;
        disableInterpolator = null;
        missingAsEmpty = null;
        threads = null;
        for (boolean me : new boolean[] { Boolean.TRUE, Boolean.FALSE })
        {
            props = new CloudMapperProperties(namespace, enabled, disableInterpolator, me, threads);
            Assertions.assertEquals(namespace, props.namespace);
            Assertions.assertEquals(CloudMapperProperties.DEFAULT_ENABLED, props.enabled);
            Assertions.assertEquals(CloudMapperProperties.DEFAULT_DISABLE_INTERPOLATOR, props.disableInterpolator);
            Assertions.assertEquals(me, props.missingAsEmpty);
            Assertions.assertEquals(CloudMapperProperties.DEFAULT_THREADS, props.threads);
        }

        namespace = UUID.randomUUID().toString();
        enabled = null;
        disableInterpolator = null;
        missingAsEmpty = null;
        threads = null;
        for (int t : new int[] { Integer.MIN_VALUE, -1, 0, CloudMapperProperties.DEFAULT_THREADS, CloudMapperProperties.MAX_THREADS,
                Integer.MAX_VALUE })
        {
            props = new CloudMapperProperties(namespace, enabled, disableInterpolator, missingAsEmpty, t);
            Assertions.assertEquals(namespace, props.namespace);
            Assertions.assertEquals(CloudMapperProperties.DEFAULT_ENABLED, props.enabled);
            Assertions.assertEquals(CloudMapperProperties.DEFAULT_DISABLE_INTERPOLATOR, props.disableInterpolator);
            Assertions.assertEquals(CloudMapperProperties.DEFAULT_MISSING_AS_EMPTY, props.missingAsEmpty);

            if (t <= 0)
            {
                Assertions.assertEquals(CloudMapperProperties.DEFAULT_THREADS, props.threads);
            }
            else if (t >= CloudMapperProperties.MAX_THREADS)
            {
                Assertions.assertEquals(CloudMapperProperties.MAX_THREADS, props.threads);
            }
            else
            {
                Assertions.assertEquals(t, props.threads);
            }
        }

        for (int t = 1; t < CloudMapperProperties.MAX_THREADS; t++)
        {
            props = new CloudMapperProperties(namespace, enabled, disableInterpolator, missingAsEmpty, t);
            Assertions.assertEquals(namespace, props.namespace);
            Assertions.assertEquals(CloudMapperProperties.DEFAULT_ENABLED, props.enabled);
            Assertions.assertEquals(CloudMapperProperties.DEFAULT_DISABLE_INTERPOLATOR, props.disableInterpolator);
            Assertions.assertEquals(CloudMapperProperties.DEFAULT_MISSING_AS_EMPTY, props.missingAsEmpty);
            Assertions.assertEquals(t, props.threads);
        }
    }
}
package com.armedia.acm.configserver.environment;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;

import org.springframework.cloud.config.environment.Environment;
import org.springframework.cloud.config.environment.PropertySource;
import org.springframework.cloud.config.server.environment.NativeEnvironmentRepository;
import org.springframework.core.env.ConfigurableEnvironment;

public class NativeCloudEnvironmentRepository extends NativeEnvironmentRepository
{
    public NativeCloudEnvironmentRepository(ConfigurableEnvironment environment, NativeCloudEnvironmentProperties properties)
    {
        super(environment, properties);
    }

    /**
     * <p>
     * Map the given value into its new, final value. If the returned value is {@code null}, it
     * means that the value should be removed. Otherwise, the new value should be used instead. If no mapping has
     * occurred, the exact same old value reference will be returned.
     * </p>
     *
     * @param key
     * @param value
     * @return the mapped value, or the original value
     */
    protected String mapValue(String key, String value)
    {
        // Does this match our syntax?
        return value;
    }

    @Override
    protected Environment clean(Environment environment)
    {
        // We call the superclass method first, so we can
        // make sure we don't get hijacked...
        Environment result = super.clean(environment);
        for (PropertySource source : environment.getPropertySources())
        {
            Map<Object, Object> map = new LinkedHashMap<>(
                    source.getSource());
            for (Map.Entry<Object, Object> entry : new LinkedHashSet<>(map.entrySet()))
            {
                Object key = entry.getKey();
                String name = key.toString();
                String value = entry.getValue().toString();

                String newValue = mapValue(name, value);

                // If we're supposed to remove it, then we do so
                if (newValue == null)
                {
                    map.remove(key);
                    continue;
                }

                // It's ok to do a != comparison, since the exact
                // same reference will be returned if there is no
                // mapping to be performed, and thus no need to
                // operate on the map
                if (newValue != value)
                {
                    map.put(key, newValue);
                }
            }
            result.add(new PropertySource(source.getName(), map));
        }
        return result;
    }
}
package com.armedia.acm.configserver.environment;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.config.environment.Environment;
import org.springframework.cloud.config.environment.PropertySource;
import org.springframework.cloud.config.server.environment.NativeEnvironmentRepository;
import org.springframework.core.env.ConfigurableEnvironment;

import com.armedia.acm.configserver.environment.mapper.CloudMapper;

public class NativeCloudEnvironmentRepository extends NativeEnvironmentRepository
{
    private static final BiFunction<String, String, String> NO_MAP = (k, v) -> v;

    @Autowired(required = false)
    private Optional<CloudMapper> mapper;

    public NativeCloudEnvironmentRepository(ConfigurableEnvironment environment, NativeCloudEnvironmentProperties properties)
    {
        super(environment, properties);
    }

    @Override
    protected Environment clean(Environment environment)
    {
        // We call the superclass method first, so we can
        // make sure we don't get hijacked...
        Environment result = super.clean(environment);
        BiFunction<String, String, String> mapper = this.mapper.isPresent() ? this.mapper.get()::map
                : NativeCloudEnvironmentRepository.NO_MAP;
        for (PropertySource source : environment.getPropertySources())
        {
            Map<Object, Object> map = new LinkedHashMap<>(
                    source.getSource());
            for (Map.Entry<Object, Object> entry : new LinkedHashSet<>(map.entrySet()))
            {
                Object key = entry.getKey();
                String name = key.toString();
                String value = entry.getValue().toString();

                String newValue = mapper.apply(name, value);

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
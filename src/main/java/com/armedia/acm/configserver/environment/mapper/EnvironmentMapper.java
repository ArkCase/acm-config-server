package com.armedia.acm.configserver.environment.mapper;

import org.springframework.stereotype.Component;

@Component
public class EnvironmentMapper
{
    private final EnvironmentMapperProperties properties;

    // This is the mapping syntax ... we support sys + env
    // b/c we also want to support doing replacement within
    // long streams.
    //
    // @{env:envVar}
    // @{sys:sysProp}
    // @{map:mapName:key}
    // @{sec:secName:key}
    // @{file:/absolute/path/of/file/to/read}

    public EnvironmentMapper(EnvironmentMapperProperties properties)
    {
        this.properties = properties;
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
    public String map(String key, String value)
    {
        // TODO: Do your mappings here
        return value;
    }
}
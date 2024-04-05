package com.armedia.acm.configserver.environment;

import org.springframework.cloud.config.server.environment.NativeEnvironmentProperties;

public class NativeCloudEnvironmentProperties extends NativeEnvironmentProperties
{
    private Boolean enableMappings = false;
    private Boolean file = true;
    private Boolean sec = true;
    private Boolean map = true;

    public Boolean getEnableMappings()
    {
        return this.enableMappings;
    }

    public void setEnableMappings(Boolean enableMappings)
    {
        this.enableMappings = enableMappings;
    }

    public Boolean getFile()
    {
        return this.file;
    }

    public void setFile(Boolean file)
    {
        this.file = file;
    }

    public Boolean getSec()
    {
        return this.sec;
    }

    public void setSec(Boolean sec)
    {
        this.sec = sec;
    }

    public Boolean getMap()
    {
        return this.map;
    }

    public void setMap(Boolean map)
    {
        this.map = map;
    }
}
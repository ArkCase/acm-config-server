package com.armedia.acm.configserver.environment.mapper;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Profile("native-cloud")
@Configuration
@ConfigurationProperties("value-mapper")
public class EnvironmentMapperProperties
{
    private Boolean enable = false;
    private Boolean file = true;
    private Boolean sec = true;
    private Boolean map = true;

    public Boolean getenable()
    {
        return this.enable;
    }

    public void setenable(Boolean enable)
    {
        this.enable = enable;
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
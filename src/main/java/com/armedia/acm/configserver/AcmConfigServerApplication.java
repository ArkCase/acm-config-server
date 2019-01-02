package com.armedia.acm.configserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.config.server.EnableConfigServer;

@EnableConfigServer
@SpringBootApplication
@ConfigurationProperties
public class AcmConfigServerApplication
{

    public static void main(String[] args)
    {
        SpringApplication.run(AcmConfigServerApplication.class, args);
    }
}

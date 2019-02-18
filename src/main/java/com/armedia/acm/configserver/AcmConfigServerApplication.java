package com.armedia.acm.configserver;

import com.armedia.acm.configserver.service.FileWatchService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.config.server.EnableConfigServer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableConfigServer
@SpringBootApplication
@ConfigurationProperties
@EnableJms
@EnableAsync
public class AcmConfigServerApplication
{

    public static void main(String[] args)
    {
        ConfigurableApplicationContext context = SpringApplication.run(AcmConfigServerApplication.class, args);

        FileWatchService fileWatchService = context.getBean(FileWatchService.class);
        fileWatchService.monitor();
    }
}

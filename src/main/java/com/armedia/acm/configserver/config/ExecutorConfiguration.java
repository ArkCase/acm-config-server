package com.armedia.acm.configserver.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

@Configuration
public class ExecutorConfiguration
{
    @Bean
    public ScheduledExecutorService singleThreadExecutor() {
        return Executors.newScheduledThreadPool(1);
    }
}

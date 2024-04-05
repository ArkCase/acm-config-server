package com.armedia.acm.configserver.environment;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("native-cloud")
public class NativeCloudRepositoryConfiguration
{
    @Bean
    public NativeCloudEnvironmentRepository nativeCloudEnvironmentRepository(NativeCloudEnvironmentRepositoryFactory factory,
            NativeCloudEnvironmentProperties environmentProperties)
    {
        return factory.build(environmentProperties);
    }
}
package com.armedia.acm.configserver.environment;

import org.springframework.cloud.config.server.config.ConfigServerProperties;
import org.springframework.cloud.config.server.environment.EnvironmentRepositoryFactory;
import org.springframework.core.env.ConfigurableEnvironment;

public class NativeCloudEnvironmentRepositoryFactory
        implements EnvironmentRepositoryFactory<NativeCloudEnvironmentRepository, NativeCloudEnvironmentProperties>
{
    private ConfigurableEnvironment environment;
    private ConfigServerProperties properties;

    public NativeCloudEnvironmentRepositoryFactory(ConfigurableEnvironment environment, ConfigServerProperties properties)
    {
        this.environment = environment;
        this.properties = properties;
    }

    @Override
    public NativeCloudEnvironmentRepository build(NativeCloudEnvironmentProperties environmentProperties)
    {
        NativeCloudEnvironmentRepository repository = new NativeCloudEnvironmentRepository(this.environment, environmentProperties);
        repository.setDefaultLabel(this.properties.getDefaultLabel());
        return repository;
    }
}
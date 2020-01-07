package com.armedia.acm.configserver.config;

import com.armedia.acm.configserver.service.FileWatchService;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class AppInitializer implements ApplicationRunner
{
    private final FileWatchService fileWatchService;

    public AppInitializer(FileWatchService fileWatchService)
    {
        this.fileWatchService = fileWatchService;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception
    {
        fileWatchService.monitor();
    }
}

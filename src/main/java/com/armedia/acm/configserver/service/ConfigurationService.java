package com.armedia.acm.configserver.service;

import com.armedia.acm.configserver.exception.ConfigurationException;

import java.util.Map;

public interface ConfigurationService
{
    void updateProperties(Map<String, Object> properties) throws ConfigurationException;
}

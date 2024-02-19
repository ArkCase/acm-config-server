package com.armedia.acm.configserver.api;

/*-
 * #%L
 * acm-config-server
 * %%
 * Copyright (C) 2019 ArkCase LLC
 * %%
 * This file is part of the ArkCase software.
 *
 * If the software was purchased under a paid ArkCase license, the terms of
 * the paid license agreement will prevail.  Otherwise, the software is
 * provided under the following open source license terms:
 *
 * ArkCase is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * ArkCase is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with ArkCase. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.armedia.acm.configserver.exception.ConfigurationException;
import com.armedia.acm.configserver.service.FileSystemConfigurationService;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.file.NoSuchFileException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.*;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.util.UriComponentsBuilder;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class ConfigurationAPIControllerTest
{
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    FileSystemConfigurationService configurationService;

    @Value("${arkcase.languages}")
    String languages;

    private ObjectMapper objectMapper;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);

        objectMapper = new ObjectMapper();
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    }

    @Test
    public void updatePropertiesShouldReturnStatusOk() throws Exception {
        String applicationName = "ldap";
        Map<String, Object> properties = new HashMap<>();
        properties.put("key", "value");

        String content = objectMapper.writeValueAsString(properties);

        doNothing().when(configurationService).updateProperties(properties, applicationName);

        mockMvc.perform(post(ConfigurationAPIController.Path.RESOURCE + "/" + applicationName)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(content))
                .andExpect(status().isOk());

        // Verify that the updateProperties method was called
        verify(configurationService, times(1)).updateProperties(anyMap(), anyString());
    }

    @Test
    public void updatePropertiesReturnInternalServerError() throws Exception {
        String applicationName = "ldap";
        Map<String, Object> properties = new HashMap<>();
        properties.put("key", "value");

        // Mock the behavior of updateProperties to throw ConfigurationException
        doThrow(ConfigurationException.class).when(configurationService).updateProperties(anyMap(), anyString());

        String content = objectMapper.writeValueAsString(properties);

        mockMvc.perform(post(ConfigurationAPIController.Path.RESOURCE + "/" + applicationName)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(content))
                .andExpect(status().is5xxServerError());

        // Verify that the updateProperties method was called
        verify(configurationService, times(1)).updateProperties(anyMap(), anyString());
    }

    @Test
    public void resetConfigShouldReturnStatusOK() throws Exception {
        doNothing().when(configurationService).resetConfigurationBrandingFilesToDefault();
        doNothing().when(configurationService).resetPropertiesToDefault();

        mockMvc.perform(delete(ConfigurationAPIController.Path.RESOURCE + ConfigurationAPIController.Path.RESET_ALL))
                .andExpect(status().isOk());

        // Assert response status code
        verify(configurationService, times(1)).resetConfigurationBrandingFilesToDefault();
        verify(configurationService, times(1)).resetPropertiesToDefault();
    }

    @Test
    public void resetConfigShouldReturnInternalServerError() throws Exception {
        doThrow(ConfigurationException.class).when(configurationService).resetConfigurationBrandingFilesToDefault();

        mockMvc.perform(delete(ConfigurationAPIController.Path.RESOURCE + ConfigurationAPIController.Path.RESET_ALL))
                .andExpect(status().is5xxServerError());

        verify(configurationService, times(1)).resetConfigurationBrandingFilesToDefault();
        verify(configurationService, times(0)).resetPropertiesToDefault();
    }

    @Test
    public void removePropertiesShouldReturnStatusOk() throws Exception {
        doNothing().when(configurationService).removeProperties(anyList(), anyString());

        String uri = UriComponentsBuilder.fromPath(ConfigurationAPIController.Path.RESOURCE)
                .path(ConfigurationAPIController.Path.REMOVE)
                .buildAndExpand("ldap")
                .toUriString();

        String content = objectMapper.writeValueAsString(Collections.singletonList("property1"));

        mockMvc.perform(post(uri)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(content))
                .andExpect(status().isOk());

        verify(configurationService, times(1)).removeProperties(anyList(), anyString());
    }

    @Test
    public void removePropertiesShouldReturnInternalServerError() throws Exception {
        doThrow(ConfigurationException.class).when(configurationService).removeProperties(anyList(), anyString());

        String uri = UriComponentsBuilder.fromPath(ConfigurationAPIController.Path.RESOURCE)
                .path(ConfigurationAPIController.Path.REMOVE)
                .buildAndExpand("ldap")
                .toUriString();

        String content = objectMapper.writeValueAsString(Collections.singletonList("property1"));

        mockMvc.perform(post(uri)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(content))
                .andExpect(status().is5xxServerError());

        verify(configurationService, times(1)).removeProperties(anyList(), anyString());
    }

    @Test
    public void resetPropertiesByApplicationNameShouldReturnStatusOk() throws Exception {
        doNothing().when(configurationService).resetFilePropertiesToDefault(anyString());

        String uri = UriComponentsBuilder.fromPath(ConfigurationAPIController.Path.RESOURCE)
                .path(ConfigurationAPIController.Path.RESET)
                .buildAndExpand("ldap")
                .toUriString();

        mockMvc.perform(delete(uri))
                .andExpect(status().isOk());

        verify(configurationService, times(1)).resetFilePropertiesToDefault("ldap");
    }

    @Test
    public void resetLabelsByApplicationNameShouldReturnStatusOk() throws Exception {
        doNothing().when(configurationService).resetFilePropertiesToDefault(anyString());

        String uri = UriComponentsBuilder.fromPath(ConfigurationAPIController.Path.RESOURCE)
                .path(ConfigurationAPIController.Path.RESET)
                .buildAndExpand("admin-en")
                .toUriString();

        mockMvc.perform(delete(uri))
                .andExpect(status().isOk());

        verify(configurationService, times(1)).resetFilePropertiesToDefault("labels/admin-en");
    }

    @Test
    public void resetPropertiesByApplicationNameShouldReturnInternalServerError() throws Exception {
        doThrow(ConfigurationException.class).when(configurationService).resetFilePropertiesToDefault(anyString());

        String uri = UriComponentsBuilder.fromPath(ConfigurationAPIController.Path.RESOURCE)
                .path(ConfigurationAPIController.Path.RESET)
                .buildAndExpand("admin-en")
                .toUriString();

        mockMvc.perform(delete(uri))
                .andExpect(status().isInternalServerError());

        verify(configurationService, times(1)).resetFilePropertiesToDefault("labels/admin-en");
    }

    @Test
    public void resetPropertiesByApplicationNameShouldReturnStatusOkIfNotFound() throws Exception {
        doThrow(NoSuchFileException.class).when(configurationService).resetFilePropertiesToDefault(anyString());

        String uri = UriComponentsBuilder.fromPath(ConfigurationAPIController.Path.RESOURCE)
                .path(ConfigurationAPIController.Path.RESET)
                .buildAndExpand("admin-en")
                .toUriString();

        mockMvc.perform(delete(uri))
                .andExpect(status().isOk());

        verify(configurationService, times(1)).resetFilePropertiesToDefault("labels/admin-en");
    }
}

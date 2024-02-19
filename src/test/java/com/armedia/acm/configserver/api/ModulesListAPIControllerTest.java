package com.armedia.acm.configserver.api;

/*-
 * #%L
 * acm-config-server
 * %%
 * Copyright (C) 2019 - 2024 ArkCase LLC
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

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.armedia.acm.configserver.service.FileSystemConfigurationService;
import java.util.Arrays;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class ModulesListAPIControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    FileSystemConfigurationService fileSystemConfigurationService;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void getModulesListSuccess() throws Exception
    {
        when(fileSystemConfigurationService.getModulesNames()).thenReturn(Arrays.asList("labels", "ldap"));

        mockMvc.perform(get("/config/modules"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(content().string(containsString("labels")))
                .andExpect(content().string(containsString("ldap")));

        verify(fileSystemConfigurationService, times(1)).getModulesNames();
    }

    @Test
    public void getModulesListFailed() throws Exception
    {
        doThrow(NullPointerException.class).when(fileSystemConfigurationService).getModulesNames();

        mockMvc.perform(get("/config/modules"))
                .andExpect(status().is5xxServerError());

        verify(fileSystemConfigurationService, times(1)).getModulesNames();
    }
}

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

import static org.mockito.Mockito.*;
import static org.mockito.Mockito.times;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.armedia.acm.configserver.exception.ConfigurationException;
import com.armedia.acm.configserver.service.FileConfigurationService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.multipart.MultipartFile;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class FileConfigurationApiControllerTest {
    @Mock
    private MockMvc mockMvc;

    @Mock
    FileConfigurationService fileConfigurationService;

    @Before
    public void setup() {
        FileConfigurationApiController controller = new FileConfigurationApiController(fileConfigurationService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    public void moveFileToConfigurationSuccess() throws Exception
    {
        doNothing().when(fileConfigurationService).moveFileToConfiguration(any(MultipartFile.class), anyString());

        MockMultipartFile file = new MockMultipartFile("file", "test.txt", MediaType.TEXT_PLAIN_VALUE, "Test file content".getBytes());

        mockMvc.perform(
                        multipart("/file")
                                .file(file)
                                .param("fileName", "test.txt")
                                .contentType(MediaType.MULTIPART_FORM_DATA)
                )
                .andExpect(status().isOk());

        verify(fileConfigurationService, times(1)).moveFileToConfiguration(any(MultipartFile.class), anyString());
    }

    @Test
    public void moveFileToConfigurationFailed() throws Exception
    {
        doThrow(ConfigurationException.class).when(fileConfigurationService).moveFileToConfiguration(any(MultipartFile.class), anyString());

        MockMultipartFile file = new MockMultipartFile("file", "test.txt", MediaType.TEXT_PLAIN_VALUE, "Test file content".getBytes());

        mockMvc.perform(
                        multipart("/file")
                                .file(file)
                                .param("fileName", "test.txt")
                                .contentType(MediaType.MULTIPART_FORM_DATA)
                )
                .andExpect(status().isInternalServerError());

        verify(fileConfigurationService, times(1)).moveFileToConfiguration(any(MultipartFile.class), anyString());    }
}

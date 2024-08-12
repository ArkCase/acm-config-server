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
package com.armedia.acm.configserver.environment.mapper;

import java.io.IOException;
import java.net.ServerSocket;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;

import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.Configuration;
import io.kubernetes.client.openapi.JSON;
import io.kubernetes.client.openapi.models.V1ConfigMap;
import io.kubernetes.client.openapi.models.V1ConfigMapList;
import io.kubernetes.client.openapi.models.V1ConfigMapListBuilder;
import io.kubernetes.client.openapi.models.V1ObjectMeta;
import io.kubernetes.client.openapi.models.V1Secret;
import io.kubernetes.client.openapi.models.V1SecretList;
import io.kubernetes.client.openapi.models.V1SecretListBuilder;

public class CloudMapperTest
{
    private static final int PORT;
    static
    {
        try (ServerSocket serverSocket = new ServerSocket(0))
        {
            PORT = serverSocket.getLocalPort();
        }
        catch (IOException e)
        {
            throw new RuntimeException("Failed to find an available port to listen on", e);
        }
    }

    @RegisterExtension
    private static final WireMockExtension SERVER = WireMockExtension.newInstance()
            .options(WireMockConfiguration.options().port(CloudMapperTest.PORT))
            .build();

    private static final ApiClient CLIENT = new ApiClient();

    private static final String NAMESPACE = UUID.randomUUID().toString();

    private static final Map<String, V1Secret> SECRETS = new TreeMap<>();
    private static V1SecretList SECRET_LIST = null;

    private static final Map<String, V1ConfigMap> CONFIG_MAPS = new TreeMap<>();
    private static V1ConfigMapList CONFIG_MAP_LIST = null;

    @BeforeAll
    public static void beforeAll()
    {
        CloudMapperTest.CLIENT.setBasePath("http://localhost:" + CloudMapperTest.SERVER.getPort());
        Configuration.setDefaultApiClient(CloudMapperTest.CLIENT);

        V1SecretListBuilder secretBuilder = new V1SecretListBuilder();
        for (int s = 1; s <= 10; s++)
        {
            String secretName = String.format("secret-%02d", s);

            V1Secret secret = new V1Secret();
            secret.metadata(new V1ObjectMeta().namespace(CloudMapperTest.NAMESPACE).name(secretName).resourceVersion("1"));

            Map<String, byte[]> data = new TreeMap<>();
            for (int v = 1; v <= 5; v++)
            {
                data.put(String.format("%s.value-%02d", secretName, v),
                        String.format("%s.result-%02d-%s", secretName, v, UUID.randomUUID()).getBytes(StandardCharsets.UTF_8));
            }
            secret.data(data);

            secretBuilder.addToItems(secret);
            CloudMapperTest.SECRETS.put(secretName, secret);
        }
        CloudMapperTest.SECRET_LIST = secretBuilder.build();

        V1ConfigMapListBuilder configMapBuilder = new V1ConfigMapListBuilder();
        for (int c = 1; c <= 10; c++)
        {
            String configMapName = String.format("config-map-%02d", c);

            V1ConfigMap configMap = new V1ConfigMap();
            configMap.metadata(new V1ObjectMeta().namespace(CloudMapperTest.NAMESPACE).name(configMapName).resourceVersion("1"));

            Map<String, String> data = new TreeMap<>();
            for (int v = 1; v <= 5; v++)
            {
                data.put(String.format("%s.value-%02d", configMapName, v),
                        String.format("%s.result-%02d-%s", configMapName, v, UUID.randomUUID().toString()));
            }
            configMap.data(data);

            Map<String, byte[]> bin = new TreeMap<>();
            for (int v = 1; v <= 5; v++)
            {
                bin.put(String.format("%s.bin-value-%02d", configMapName, v),
                        String.format("%s.bin-result-%02d-%s", configMapName, v, UUID.randomUUID()).getBytes(StandardCharsets.UTF_8));
            }
            configMap.binaryData(bin);

            configMapBuilder.addToItems(configMap);
            CloudMapperTest.CONFIG_MAPS.put(configMapName, configMap);
        }
        CloudMapperTest.CONFIG_MAP_LIST = configMapBuilder.build();
    }

    @AfterAll
    public static void afterAll()
    {
        CloudMapperTest.SERVER.shutdownServer();
    }

    @BeforeEach
    public void beforeEach()
    {
        CloudMapperTest.SERVER.stubFor(
                WireMock.get(WireMock.urlEqualTo("/api/v1/namespaces/" + CloudMapperTest.NAMESPACE + "/secrets"))
                        .willReturn(
                                WireMock
                                        .aResponse()
                                        .withHeader("Content-Type", "application/json")
                                        .withBody(JSON.serialize(CloudMapperTest.SECRET_LIST)) //
                        ) //
        );
        CloudMapperTest.SERVER.stubFor(
                WireMock.get(WireMock.urlEqualTo("/api/v1/namespaces/" + CloudMapperTest.NAMESPACE + "/configmaps"))
                        .willReturn(
                                WireMock
                                        .aResponse()
                                        .withHeader("Content-Type", "application/json")
                                        .withBody(JSON.serialize(CloudMapperTest.CONFIG_MAP_LIST)) //
                        ) //
        );

    }

    @Test
    public void testBasicExpansion() throws Exception
    {
        CloudMapperProperties properties = new CloudMapperProperties(null, null, true, null, CloudMapperTest.NAMESPACE);
        CloudMapper cloudMapper = new CloudMapper(properties, CloudMapperTest.CLIENT);
        cloudMapper.postConstruct();

        // Now we test the expansion
        for (final V1Secret secret : CloudMapperTest.SECRETS.values())
        {
            final String secretName = secret.getMetadata().getName();
            final Map<String, byte[]> data = secret.getData();
            for (final String key : data.keySet())
            {
                final String expected = new String(data.get(key), StandardCharsets.UTF_8);
                final String var = String.format("@{secret:%s:%s}", secretName, key);
                final String actual = cloudMapper.map(String.format("%s-test-%s", secretName, key), var);
                Assertions.assertEquals(expected, actual);

                final String nonExistentSecret = String.format("non-existent-secret-%s", UUID.randomUUID());
                final String var2 = String.format("@{secret:%s:%s}", nonExistentSecret, key);
                Assertions.assertEquals(var2, cloudMapper.map(String.format("%s-test-nonExistent-%s", secretName, key), var2),
                        nonExistentSecret);
            }

            {
                final String nonExistentKey = String.format("%s-non-existent-test-%s", secretName, UUID.randomUUID());
                final String var = String.format("@{secret:%s:%s}", secretName, nonExistentKey);
                Assertions.assertEquals(var, cloudMapper.map(String.format("%s-test-nonExistent", secretName), var),
                        nonExistentKey);
            }
        }
        for (final V1ConfigMap configMap : CloudMapperTest.CONFIG_MAPS.values())
        {
            final String configMapName = configMap.getMetadata().getName();
            final Map<String, String> data = configMap.getData();
            for (final String key : data.keySet())
            {
                final String expected = data.get(key);
                final String var = String.format("@{config:%s:%s}", configMapName, key);
                final String actual = cloudMapper.map(String.format("%s-test-%s", configMapName, key), var);
                Assertions.assertEquals(expected, actual);

                final String nonExistentConfigMap = String.format("non-existent-config-%s", UUID.randomUUID());
                final String var2 = String.format("@{config:%s:%s}", nonExistentConfigMap, key);
                Assertions.assertEquals(var2,
                        cloudMapper.map(String.format("%s-test-nonExistent-%s", configMapName, key), var2),
                        nonExistentConfigMap);
            }

            {
                final String nonExistentKey = String.format("%s-non-existent-test-%s", configMapName, UUID.randomUUID());
                final String var = String.format("@{config:%s:%s}", configMapName, nonExistentKey);
                Assertions.assertEquals(var, cloudMapper.map(String.format("%s-test-nonExistent", configMapName), var),
                        nonExistentKey);
            }
        }
    }

    @Test
    public void testMissingAsEmpty() throws Exception
    {
        CloudMapperProperties properties = new CloudMapperProperties(null, null, true, true, CloudMapperTest.NAMESPACE);
        CloudMapper cloudMapper = new CloudMapper(properties, CloudMapperTest.CLIENT);
        cloudMapper.postConstruct();

        // Now we test the expansion
        for (final V1Secret secret : CloudMapperTest.SECRETS.values())
        {
            final String secretName = secret.getMetadata().getName();
            final Map<String, byte[]> data = secret.getData();
            for (final String key : data.keySet())
            {
                final String expected = new String(data.get(key), StandardCharsets.UTF_8);
                final String var = String.format("@{secret:%s:%s}", secretName, key);
                final String actual = cloudMapper.map(String.format("%s-test-%s", secretName, key), var);
                Assertions.assertEquals(expected, actual);

                final String nonExistentSecret = String.format("non-existent-secret-%s", UUID.randomUUID());
                final String var2 = String.format("@{secret:%s:%s}", nonExistentSecret, key);
                Assertions.assertEquals(StringUtils.EMPTY, cloudMapper.map(String.format("%s-test-nonExistent-%s", secretName, key), var2),
                        nonExistentSecret);
            }

            {
                final String nonExistentKey = String.format("%s-non-existent-test-%s", secretName, UUID.randomUUID());
                final String var = String.format("@{secret:%s:%s}", secretName, nonExistentKey);
                Assertions.assertEquals(StringUtils.EMPTY, cloudMapper.map(String.format("%s-test-nonExistent", secretName), var),
                        nonExistentKey);
            }
        }
        for (final V1ConfigMap configMap : CloudMapperTest.CONFIG_MAPS.values())
        {
            final String configMapName = configMap.getMetadata().getName();
            final Map<String, String> data = configMap.getData();
            for (final String key : data.keySet())
            {
                final String expected = data.get(key);
                final String var = String.format("@{config:%s:%s}", configMapName, key);
                final String actual = cloudMapper.map(String.format("%s-test-%s", configMapName, key), var);
                Assertions.assertEquals(expected, actual);

                final String nonExistentConfigMap = String.format("non-existent-config-%s", UUID.randomUUID());
                final String var2 = String.format("@{config:%s:%s}", nonExistentConfigMap, key);
                Assertions.assertEquals(StringUtils.EMPTY,
                        cloudMapper.map(String.format("%s-test-nonExistent-%s", configMapName, key), var2),
                        nonExistentConfigMap);
            }

            {
                final String nonExistentKey = String.format("%s-non-existent-test-%s", configMapName, UUID.randomUUID());
                final String var = String.format("@{config:%s:%s}", configMapName, nonExistentKey);
                Assertions.assertEquals(StringUtils.EMPTY, cloudMapper.map(String.format("%s-test-nonExistent", configMapName), var),
                        nonExistentKey);
            }
        }
    }
}

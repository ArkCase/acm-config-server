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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;

import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.Configuration;
import io.kubernetes.client.openapi.JSON;
import io.kubernetes.client.openapi.models.V1ConfigMap;
import io.kubernetes.client.openapi.models.V1ConfigMapListBuilder;
import io.kubernetes.client.openapi.models.V1ObjectMeta;
import io.kubernetes.client.openapi.models.V1Secret;
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

    @AfterAll
    public static void afterAll()
    {
        if (CloudMapperTest.SERVER != null)
        {
            CloudMapperTest.SERVER.shutdownServer();
        }
    }

    @Test
    public void testCloudMapper() throws Exception
    {
        ApiClient client = new ApiClient();
        client.setBasePath("http://localhost:" + CloudMapperTest.SERVER.getPort());
        Configuration.setDefaultApiClient(client);

        String namespace = UUID.randomUUID().toString();

        V1SecretListBuilder secretBuilder = new V1SecretListBuilder();
        Map<String, V1Secret> secrets = new TreeMap<>();
        for (int s = 1; s <= 10; s++)
        {
            String secretName = String.format("secret-%02d", s);

            V1Secret secret = new V1Secret();
            secret.metadata(new V1ObjectMeta().namespace(namespace).name(secretName).resourceVersion("1"));

            Map<String, byte[]> data = new TreeMap<>();
            for (int v = 1; v <= 5; v++)
            {
                data.put(String.format("%s.value-%02d", secretName, v),
                        String.format("%s.result-%02d-%s", secretName, v, UUID.randomUUID()).getBytes(StandardCharsets.UTF_8));
            }
            secret.data(data);

            secretBuilder.addToItems(secret);
            secrets.put(secretName, secret);
        }
        CloudMapperTest.SERVER.stubFor(
                WireMock.get(WireMock.urlEqualTo("/api/v1/namespaces/" + namespace + "/secrets"))
                        .willReturn(
                                WireMock
                                        .aResponse()
                                        .withHeader("Content-Type", "application/json")
                                        .withBody(JSON.serialize(secretBuilder.build())) //
                        ) //
        );

        V1ConfigMapListBuilder configMapBuilder = new V1ConfigMapListBuilder();
        Map<String, V1ConfigMap> configMaps = new TreeMap<>();
        for (int c = 1; c <= 10; c++)
        {
            String configMapName = String.format("config-map-%02d", c);

            V1ConfigMap configMap = new V1ConfigMap();
            configMap.metadata(new V1ObjectMeta().namespace(namespace).name(configMapName).resourceVersion("1"));

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
            configMaps.put(configMapName, configMap);
        }

        CloudMapperTest.SERVER.stubFor(
                WireMock.get(WireMock.urlEqualTo("/api/v1/namespaces/" + namespace + "/configmaps"))
                        .willReturn(
                                WireMock
                                        .aResponse()
                                        .withHeader("Content-Type", "application/json")
                                        .withBody(JSON.serialize(configMapBuilder.build())) //
                        ) //
        );

        CloudMapperProperties properties = new CloudMapperProperties();
        properties.setNamespace(namespace);
        CloudMapper cloudMapper = new CloudMapper(client, properties);
        cloudMapper.postConstruct();

        // Now we test the expansion
        for (final V1Secret secret : secrets.values())
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
        for (final V1ConfigMap configMap : configMaps.values())
        {
            final String configMapName = configMap.getMetadata().getName();
            final Map<String, String> data = configMap.getData();
            for (final String key : data.keySet())
            {
                final String expected = data.get(key);
                final String var = String.format("@{config:%s:%s}", configMapName, key);
                final String actual = cloudMapper.map(String.format("%s-test-%s", configMapName, key), var);
                Assertions.assertEquals(expected, actual);

                final String nonExistentSecret = String.format("non-existent-config-%s", UUID.randomUUID());
                final String var2 = String.format("@{config:%s:%s}", nonExistentSecret, key);
                Assertions.assertEquals(StringUtils.EMPTY,
                        cloudMapper.map(String.format("%s-test-nonExistent-%s", configMapName, key), var2),
                        nonExistentSecret);
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
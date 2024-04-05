package com.armedia.acm.configserver.environment.mapper;

import java.security.MessageDigest;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.Configuration;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.apis.CoreV1Api.APIreadNamespacedConfigMapRequest;
import io.kubernetes.client.openapi.apis.CoreV1Api.APIreadNamespacedSecretRequest;
import io.kubernetes.client.openapi.models.V1ConfigMap;
import io.kubernetes.client.openapi.models.V1Secret;
import io.kubernetes.client.util.ClientBuilder;

@Component
public class EnvironmentMapper
{
    protected static final String CUSTOM_ENV_PREFIX = "OPENSHIFT_KUBE_PING_";

    protected static String getEnv(String... keys)
    {
        String val = null;
        for (String key : keys)
        {
            val = System.getenv(key);
            if (val != null)
            {
                return val;
            }
        }
        return null;
    }

    private final Logger log = LoggerFactory.getLogger(getClass());

    private final EnvironmentMapperProperties properties;

    protected String url;
    protected int connectionTimeout;
    protected int readTimeout;

    protected Instant startTime;
    protected MessageDigest md5;

    protected Map<String, String> headers = new HashMap<>();

    protected String localIp;
    protected int port;

    protected long expirationTime = 5000;

    // This is the mapping syntax ... we support sys + env
    // b/c we also want to support doing replacement within
    // long streams.
    //
    // @{env:envVar}
    // @{sys:sysProp}
    // @{map:[mapNS/]mapName:key}
    // @{sec:[secNS/]secName:key}
    // @{file:/absolute/path/of/file/to/read}

    public EnvironmentMapper(EnvironmentMapperProperties properties)
    {
        this.properties = properties;

        ApiClient client = ClientBuilder.cluster().build();

        Configuration.setDefaultApiClient(client);

        CoreV1Api api = new CoreV1Api();

        APIreadNamespacedSecretRequest secretReq = api.readNamespacedSecret(this.localIp, EnvironmentMapper.CUSTOM_ENV_PREFIX);
        V1Secret secret = secretReq.execute();
        APIreadNamespacedConfigMapRequest configMapReq = api.readNamespacedConfigMap(this.localIp, EnvironmentMapper.CUSTOM_ENV_PREFIX);
        V1ConfigMap configMap = configMapReq.execute();
    }

    protected String getNamespace()
    {
        String namespace = EnvironmentMapper.getEnv(EnvironmentMapper.CUSTOM_ENV_PREFIX + "NAMESPACE", "KUBERNETES_NAMESPACE");
        return (((namespace != null) && !namespace.isEmpty())
                ? namespace
                : null);
    }

    /**
     * <p>
     * Map the given value into its new, final value. If the returned value is {@code null}, it
     * means that the value should be removed. Otherwise, the new value should be used instead. If no mapping has
     * occurred, the exact same old value reference will be returned.
     * </p>
     *
     * @param key
     * @param value
     * @return the mapped value, or the original value
     */
    public String map(String key, String value)
    {

        // TODO: Do your mappings here
        // TODO: Copy K8s reading from here
        // https://github.com/apache/tomcat/blob/main/java/org/apache/catalina/tribes/membership/cloud/KubernetesMembershipProvider.java
        return value;
    }
}
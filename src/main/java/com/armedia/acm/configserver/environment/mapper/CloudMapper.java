package com.armedia.acm.configserver.environment.mapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executors;
import java.util.function.BiFunction;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringSubstitutor;
import org.apache.commons.text.lookup.StringLookup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.kubernetes.client.common.KubernetesObject;
import io.kubernetes.client.informer.ResourceEventHandler;
import io.kubernetes.client.informer.SharedIndexInformer;
import io.kubernetes.client.informer.SharedInformerFactory;
import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.models.V1ConfigMap;
import io.kubernetes.client.openapi.models.V1ConfigMapList;
import io.kubernetes.client.openapi.models.V1ObjectMeta;
import io.kubernetes.client.openapi.models.V1Secret;
import io.kubernetes.client.openapi.models.V1SecretList;
import io.kubernetes.client.util.Config;

@Component
public class CloudMapper
{
    private final Logger log = LoggerFactory.getLogger(getClass());

    private static final Pattern PARSER = Pattern.compile("^([^:]+):([^:]+):(.+)$");

    private abstract class ResourceWrapper<ApiType extends KubernetesObject> implements ResourceEventHandler<ApiType>
    {
        private final String type;
        private final ConcurrentMap<String, ApiType> cache = new ConcurrentHashMap<>();

        private ResourceWrapper(String type)
        {
            this.type = type;
            CloudMapper.this.masterCache.put(type, this);
        }

        protected void debugContents(ApiType obj)
        {
            // Do nothing by default...
        }

        @Override
        public final void onUpdate(ApiType oldObj, ApiType newObj)
        {
            if (newObj == null)
            {
                // This is a deletion
                V1ObjectMeta meta = oldObj.getMetadata();
                if (CloudMapper.this.log.isDebugEnabled())
                {
                    CloudMapper.this.log.debug("Deleting {} {}/{} v{}", this.type, meta.getNamespace(), meta.getName(),
                            meta.getResourceVersion());
                }
                this.cache.remove(meta.getName());
            }
            else
            {
                // This is an addition or an update ... just replace the stored object
                V1ObjectMeta meta = newObj.getMetadata();
                if (CloudMapper.this.log.isDebugEnabled())
                {
                    if (oldObj != null)
                    {
                        CloudMapper.this.log.debug("Updating {} {}/{} v{} (from v{})", this.type,
                                meta.getNamespace(), meta.getName(),
                                meta.getResourceVersion(), oldObj.getMetadata().getResourceVersion());
                    }
                    else
                    {
                        CloudMapper.this.log.debug("Adding {} {}/{} v{}", this.type,
                                meta.getNamespace(), meta.getName(),
                                meta.getResourceVersion());
                    }
                }
                this.cache.put(meta.getName(), newObj);
                if (CloudMapper.this.log.isDebugEnabled())
                {
                    debugContents(newObj);
                }
            }
        }

        @Override
        public final void onAdd(ApiType obj)
        {
            onUpdate(null, obj);
        }

        @Override
        public final void onDelete(ApiType obj, boolean deletedFinalStateUnknown)
        {
            onUpdate(obj, null);
        }

        public String getValue(String name, String key)
        {
            if (!this.cache.containsKey(name))
            {
                return null;
            }

            return extractValue(this.cache.get(name), key);
        }

        protected abstract String extractValue(ApiType obj, String key);
    }

    @SuppressWarnings("serial")
    private static final class ValueMissing extends RuntimeException
    {
        ValueMissing(String valueSpec)
        {
            super(valueSpec);
        }
    }

    private static final StringLookup ERROR_LOOKUP = (v) -> {
        throw new RuntimeException(String.format("Invalid cloud value spec [%s]", v));
    };

    private final ApiClient client = Config.defaultClient();
    private final CoreV1Api api = new CoreV1Api(this.client);
    private final SharedInformerFactory informerFactory;

    private final ConcurrentMap<String, ResourceWrapper<? extends KubernetesObject>> masterCache = new ConcurrentHashMap<>();

    private final BiFunction<String, String, String> mapper;

    private final ResourceWrapper<V1ConfigMap> configMapHandler = new ResourceWrapper<>("config")
    {
        @Override
        protected String extractValue(V1ConfigMap obj, String key)
        {
            Map<String, String> data = obj.getData();
            if (data.containsKey(key))
            {
                return data.get(key);
            }

            // Uhm ... not textual data ...
            Map<String, byte[]> bin = obj.getBinaryData();
            if (bin.containsKey(key))
            {
                return Base64.getEncoder().encodeToString(bin.get(key));
            }

            // Nothing?
            return null;
        }
    };

    private final ResourceWrapper<V1Secret> secretHandler = new ResourceWrapper<>("secret")
    {

        @Override
        protected String extractValue(V1Secret obj, String key)
        {
            // Uhm ... not textual data ...
            Map<String, byte[]> bin = obj.getData();
            if (bin.containsKey(key))
            {
                // Assume strings are UTF-8 ... is this correct?
                return new String(bin.get(key), StandardCharsets.UTF_8);
            }

            // Nothing?
            return null;
        }

    };

    public CloudMapper(@Autowired CloudMapperProperties properties) throws IOException, ApiException
    {
        if (!properties.isEnabled())
        {
            this.log.debug("The CloudMapper is disabled");
            this.mapper = (k, v) -> v;
            this.informerFactory = null;
            return;
        }

        this.log.debug("The CloudMapper is enabled");

        final StringSubstitutor substitutor;
        if (properties.isDisableInterpolator())
        {
            this.log.debug("CloudMapper's Interpolator is disabled, using a strict lookup");
            substitutor = new StringSubstitutor(CloudMapper.ERROR_LOOKUP);
        }
        else
        {
            this.log.debug("CloudMapper's Interpolator is ensabled");
            substitutor = StringSubstitutor.createInterpolator();
        }

        // Set our custom delimiters to avoid conflicting with Spring's OOTB stuff
        substitutor //
                .setVariablePrefix("@{") //
                .setVariableSuffix("}") //
        ;

        // This will either be the interpolator's delegate, or our error delegate
        // which will explode if a value we're meant to resolve isn't meant for us
        final StringLookup lookupDelegate = substitutor.getStringLookup();
        substitutor.setVariableResolver((key) -> {
            // Check to see if this is one of our values.
            this.log.trace("Resolving the key [{}]", key);
            Matcher m = CloudMapper.PARSER.matcher(key);

            // Not one of ours? Let it fly!
            if (!m.matches())
            {
                this.log.trace("The key [{}] doesn't match our expected format /{}/, will delegate the lookup", key,
                        CloudMapper.PARSER.pattern());
                return lookupDelegate.lookup(key);
            }

            // Not one of ours? Let it fly!
            final String resourceType = m.group(1);
            final String resourceName = m.group(2);
            final String resourceKey = m.group(3);
            this.log.trace("Looking up the value for [{}:{}:{}]", resourceType, resourceName, resourceKey);

            final ResourceWrapper<?> resource = this.masterCache.get(resourceType);
            if (resource == null)
            {
                this.log.trace("The key type [{}] is not one of ours ({}), will delegate the lookup", resourceType,
                        this.masterCache.keySet());
                return lookupDelegate.lookup(key);
            }

            // One of ours? Process it...
            final String result = resource.getValue(resourceName, resourceKey);
            this.log.trace("Value resolved for [{}:{}:{}] = [{}] (null == {})", resourceType, resourceName, resourceKey, result,
                    Objects.isNull(result));

            // If we're not accepting missing values, explode if the value is missing
            if ((result == null) && properties.isFailIfMissing())
            {
                throw new ValueMissing(key);
            }

            // If we didn't explode b/c we didn't get a value, then
            // we simply return the empty string if there's no value
            return StringUtils.defaultString(result);
        });

        this.mapper = (key, value) -> {
            try
            {
                return substitutor.replace(value);
            }
            catch (ValueMissing v)
            {
                throw new RuntimeException(String.format("Failed to resolve the cloud variable spec [%s] (key = [%s], value = [%s]",
                        v.getMessage(), key, value));
            }
        };

        // Now, initialize the cloud access stuff... including the caching.
        this.informerFactory = new SharedInformerFactory(this.client, Executors.newFixedThreadPool(properties.getThreads()));

        final String namespace = null;

        SharedIndexInformer<V1Secret> secretInformer = this.informerFactory.sharedIndexInformerFor(
                (params) -> this.api.listNamespacedSecret(namespace)
                        .sendInitialEvents(true)
                        .resourceVersion(params.resourceVersion)
                        .watch(params.watch)
                        .timeoutSeconds(params.timeoutSeconds)
                        .buildCall(null),
                V1Secret.class, V1SecretList.class);
        secretInformer.addEventHandler(this.secretHandler);

        SharedIndexInformer<V1ConfigMap> configMapInformer = this.informerFactory.sharedIndexInformerFor(
                (params) -> this.api.listNamespacedConfigMap(namespace)
                        .sendInitialEvents(true)
                        .resourceVersion(params.resourceVersion)
                        .watch(params.watch)
                        .timeoutSeconds(params.timeoutSeconds)
                        .buildCall(null),
                V1ConfigMap.class, V1ConfigMapList.class);
        configMapInformer.addEventHandler(this.configMapHandler);

        this.informerFactory.startAllRegisteredInformers();
    }

    /**
     * <p>
     * Map the given value into its new, final value. If the returned value is {@code null}, it
     * means that the value should be removed. Otherwise, the new value should be used instead. If no mapping has
     * occurred, the exact same old value reference will be returned.
     * </p>
     *
     * //
     * // The key is optional because we want to allow the key given
     * //
     * // @<config:name[:key]>
     * // @<secret:name[:key]>
     *
     * @param key
     * @param value
     * @return the mapped value, or the original value
     */
    public String map(final String key, final String value)
    {
        this.log.trace("Mapping the value [{}] -> [{}]", key, value);
        return this.mapper.apply(key, value);
    }
}
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
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executors;
import java.util.function.UnaryOperator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringSubstitutor;
import org.apache.commons.text.lookup.StringLookup;
import org.apache.commons.text.lookup.StringLookupFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import io.kubernetes.client.common.KubernetesObject;
import io.kubernetes.client.informer.ResourceEventHandler;
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
import io.kubernetes.client.util.Yaml;

@Lazy
@Component
public class CloudMapper
{
    private final Logger log = LoggerFactory.getLogger(getClass());

    private static final Pattern PARSER = Pattern.compile("^([^:]+):([^:]+):(.+)$");

    private static ApiClient buildDefaultClient()
    {
        try
        {
            return Config.defaultClient();
        }
        catch (IOException e)
        {
            throw new RuntimeException("Failed to construct the default Kubernetes client", e);
        }
    }

    private abstract class ResourceWrapper<ApiType extends KubernetesObject>
            implements ResourceEventHandler<ApiType>
    {
        protected final String type;
        private final ConcurrentMap<String, ApiType> cache = new ConcurrentHashMap<>();

        private ResourceWrapper(String type)
        {
            this.type = type;
            CloudMapper.this.masterCache.put(type, this);
        }

        protected abstract Iterable<ApiType> loadAll() throws ApiException;

        public final void initialize() throws ApiException
        {
            this.cache.clear();
            loadAll().forEach(this::onAdd);
        }

        public final void clear()
        {
            this.cache.clear();
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

        @Override
        public final void onUpdate(ApiType oldObj, ApiType newObj)
        {
            if (newObj == null)
            {
                // This is a deletion
                V1ObjectMeta meta = oldObj.getMetadata();
                CloudMapper.this.log.info("Deleting {} {}/{} v{}", this.type, meta.getNamespace(), meta.getName(),
                        meta.getResourceVersion());
                this.cache.remove(meta.getName());
                return;
            }

            // This is an addition or an update ... just replace the stored object
            V1ObjectMeta meta = newObj.getMetadata();
            if (oldObj == null)
            {
                CloudMapper.this.log.info("Adding {} {}/{} v{}", this.type,
                        meta.getNamespace(), meta.getName(),
                        meta.getResourceVersion());
            }
            else
            {
                CloudMapper.this.log.info("Updating {} {}/{} v{} (from v{})", this.type,
                        meta.getNamespace(), meta.getName(),
                        meta.getResourceVersion(), oldObj.getMetadata().getResourceVersion());
            }

            this.cache.put(meta.getName(), newObj);
            if (CloudMapper.this.log.isTraceEnabled())
            {
                CloudMapper.this.log.trace("New object:\n{}", Yaml.dump(newObj));
            }
        }

        public String getValue(String name, String key)
        {
            if (!this.cache.containsKey(name))
            {
                return null;
            }

            return getValue(this.cache.get(name), key);
        }

        protected abstract String getValue(ApiType obj, String key);
    }

    private static final StringLookup NULL_LOOKUP = (v) -> null;
    private static final UnaryOperator<String> NULL_RESOLVER = (v) -> null;

    private static final StringLookup LOOKUP_SYS = StringLookupFactory.INSTANCE.systemPropertyStringLookup();
    private static final StringLookup LOOKUP_ENV = StringLookupFactory.INSTANCE.environmentVariableStringLookup();
    private static final StringLookup LOOKUP_ALL = StringLookupFactory.INSTANCE.interpolatorStringLookup();

    private static final String lookup(String key)
    {
        String r = null;

        // System property?
        r = CloudMapper.LOOKUP_SYS.lookup(key);
        if (r != null)
        {
            return r;
        }

        // Environment variable?
        r = CloudMapper.LOOKUP_ENV.lookup(key);
        if (r != null)
        {
            return r;
        }

        // No hits
        return null;
    }

    private static final String lookupExtas(String key)
    {
        String r = null;

        // Try the simple lookup first
        r = CloudMapper.lookup(key);
        if (r != null)
        {
            return r;
        }

        // Use the extra lookups
        r = CloudMapper.LOOKUP_ALL.lookup(key);
        if (r != null)
        {
            return r;
        }

        // No hits
        return null;
    }

    // TODO: Should we do this differently? i.e. allow client configurability?
    private final ApiClient client;
    private final CoreV1Api api;
    private final String namespace;

    private final CloudMapperProperties properties;

    private SharedInformerFactory informerFactory = null;

    private final ConcurrentMap<String, ResourceWrapper<? extends KubernetesObject>> masterCache = new ConcurrentHashMap<>();

    private final UnaryOperator<String> resolver;

    private final ResourceWrapper<V1ConfigMap> configMapHandler = new ResourceWrapper<>("config")
    {
        @Override
        protected String getValue(V1ConfigMap obj, String key)
        {
            Map<String, String> data = obj.getData();
            if (data.containsKey(key))
            {
                String value = data.get(key);
                if (CloudMapper.this.log.isTraceEnabled())
                {
                    CloudMapper.this.log.trace("Resolved configMap {} textual value {} as [{}]", obj.getMetadata().getName(), key, value);
                }
                return value;
            }

            // Uhm ... not textual data ... return a B64 representation
            Map<String, byte[]> bin = obj.getBinaryData();
            if (bin.containsKey(key))
            {
                String value = Base64.getEncoder().encodeToString(bin.get(key));
                if (CloudMapper.this.log.isTraceEnabled())
                {
                    CloudMapper.this.log.trace("Resolved configMap {} binary value {} as [{}]", obj.getMetadata().getName(), key, value);
                }
                return value;
            }

            // Nothing?
            if (CloudMapper.this.log.isTraceEnabled())
            {
                CloudMapper.this.log.trace("No configMap value {} found on {}", key, obj.getMetadata().getName());
            }
            return null;
        }

        @Override
        protected Iterable<V1ConfigMap> loadAll() throws ApiException
        {
            return CloudMapper.this.api.listNamespacedConfigMap(CloudMapper.this.namespace).execute().getItems();
        }
    };

    private final ResourceWrapper<V1Secret> secretHandler = new ResourceWrapper<>("secret")
    {

        @Override
        protected String getValue(V1Secret obj, String key)
        {
            // Secrets are always stored as binary streams
            Map<String, byte[]> data = obj.getData();
            if (data.containsKey(key))
            {
                // The binary value was created by encoding the source string using UTF-8,
                // so we use the same encoding when converting the bytes back to a string
                String value = new String(data.get(key), StandardCharsets.UTF_8);
                if (CloudMapper.this.log.isTraceEnabled())
                {
                    CloudMapper.this.log.trace("Resolved secret {} value {} as [{}]", obj.getMetadata().getName(), key, value);
                }
                return value;
            }

            if (CloudMapper.this.log.isTraceEnabled())
            {
                CloudMapper.this.log.trace("No secret value {} found on {}", key, obj.getMetadata().getName());
            }
            return null;
        }

        @Override
        protected Iterable<V1Secret> loadAll() throws ApiException
        {
            return CloudMapper.this.api.listNamespacedSecret(CloudMapper.this.namespace).execute().getItems();
        }

    };

    @Autowired
    public CloudMapper(CloudMapperProperties properties) throws IOException, ApiException
    {
        this(properties, null);
    }

    protected CloudMapper(CloudMapperProperties properties, ApiClient client) throws IOException, ApiException
    {
        this.log.debug("Creating the CloudMapper");
        if (this.log.isTraceEnabled())
        {
            this.log.trace("CloudMapperProperties:\n{}", Yaml.dump(properties));
        }

        this.client = Objects.requireNonNullElseGet(client, CloudMapper::buildDefaultClient);

        // Apparently, the informers need this
        this.client.setReadTimeout(0);

        this.api = new CoreV1Api(this.client);
        this.properties = Objects.requireNonNullElse(properties, CloudMapperProperties.DEFAULT);

        final StringSubstitutor substitutor;

        if (this.properties.interpolator)
        {
            // Our interpolator will first try system properties, then try environment variables...
            // then try the default interpolator, and finally give up ...
            this.log.info("CloudMapper's interpolator is enabled (extras = {})", this.properties.interpolatorExtras);
            StringLookup lookup = (this.properties.interpolatorExtras ? CloudMapper::lookup : CloudMapper::lookupExtas);
            substitutor = new StringSubstitutor(lookup);
            this.resolver = substitutor::replace;
        }
        else
        {
            this.log.info("CloudMapper's interpolator is disabled, using a strict lookup");
            substitutor = new StringSubstitutor(CloudMapper.NULL_LOOKUP);
            this.resolver = CloudMapper.NULL_RESOLVER;
        }

        // If the cloud lookup is not enabled, this is as far as we go
        if (!this.properties.cloud)
        {
            this.log.info("The CloudMapper's cloud support is disabled");
            this.informerFactory = null;
            this.namespace = null;
            return;
        }

        // Cloud lookup is enabled. As a result, we add all the stuff
        // needed to support it.
        this.namespace = this.properties.namespace;
        this.log.info("The CloudMapper's cloud support is enabled (namespace = {})", this.namespace);

        final String missing = (properties.missingAsEmpty ? StringUtils.EMPTY : null);
        this.log.info("CloudMapper missing-as-empty: {}", properties.missingAsEmpty);

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

            final ResourceWrapper<?> resourceWrapper = this.masterCache.get(resourceType);
            if (resourceWrapper == null)
            {
                this.log.trace("The key type [{}] is not one of ours ({}), will delegate the lookup", resourceType,
                        this.masterCache.keySet());
                return lookupDelegate.lookup(key);
            }

            // One of ours? Process it...
            final String result = resourceWrapper.getValue(resourceName, resourceKey);
            this.log.trace("Value resolved for [{}:{}:{}] = [{}] (null == {})", resourceType, resourceName, resourceKey, result,
                    Objects.isNull(result));
            return (result != null ? result : missing);
        });
    }

    @PostConstruct
    protected void postConstruct() throws ApiException
    {
        if (!this.properties.cloud)
        {
            return;
        }

        for (ResourceWrapper<?> w : this.masterCache.values())
        {
            this.log.info("Initializing resources of type [{}]", w.type);
            w.initialize();
        }

        // Now, initialize the cloud access stuff... including the caching.
        this.log.info("Registering the informers...");
        this.informerFactory = new SharedInformerFactory(this.client, Executors.newFixedThreadPool(this.masterCache.size()));
        this.informerFactory.sharedIndexInformerFor(
                (params) -> this.api.listNamespacedSecret(this.namespace)
                        .resourceVersion(params.resourceVersion)
                        .watch(params.watch)
                        .timeoutSeconds(params.timeoutSeconds)
                        .buildCall(null),
                V1Secret.class, V1SecretList.class)
                .addEventHandler(this.secretHandler);

        this.informerFactory.sharedIndexInformerFor(
                (params) -> this.api.listNamespacedConfigMap(this.namespace)
                        .resourceVersion(params.resourceVersion)
                        .watch(params.watch)
                        .timeoutSeconds(params.timeoutSeconds)
                        .buildCall(null),
                V1ConfigMap.class, V1ConfigMapList.class)
                .addEventHandler(this.configMapHandler);

        this.log.info("Starting all {} registered informers", this.masterCache.size());
        this.informerFactory.startAllRegisteredInformers();
    }

    @PreDestroy
    protected void preDestroy()
    {
        if (!this.properties.cloud)
        {
            return;
        }

        try
        {
            this.log.info("Stopping all {} registered informers", this.masterCache.size());
            this.informerFactory.stopAllRegisteredInformers(true);
        }
        finally
        {
            this.masterCache.values().forEach(ResourceWrapper::clear);
            this.informerFactory = null;
        }
    }

    public String map(final String key, final String value)
    {
        this.log.trace("Mapping the value [{}] -> [{}]", key, value);
        return resolve(value);
    }

    public String resolve(final String value)
    {
        this.log.trace("Resolving the value [{}]", value);
        return this.resolver.apply(value);
    }
}
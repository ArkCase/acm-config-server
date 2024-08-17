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
package com.armedia.acm.configserver.environmentinterpolator;

import java.io.IOException;
import java.util.function.UnaryOperator;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringSubstitutor;
import org.apache.commons.text.lookup.StringLookup;
import org.apache.commons.text.lookup.StringLookupFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.yaml.snakeyaml.Yaml;

@Lazy
@Component
public class NativeEnvironmentInterpolator
{
    private final Logger log = LoggerFactory.getLogger(getClass());

    private static final StringLookup NULL_LOOKUP = (v) -> null;
    private static final UnaryOperator<String> NULL_RESOLVER = (v) -> null;

    private static enum Folding
    {
        UPPER(StringUtils::upperCase),
        LOWER(StringUtils::lowerCase),
        NONE();

        public UnaryOperator<String> fold;

        private Folding()
        {
            this(null);
        }

        private Folding(UnaryOperator<String> fold)
        {
            this.fold = ObjectUtils.defaultIfNull(fold, UnaryOperator.identity());
        }
    }

    private static class LookupInfo
    {
        private final String key;
        private final Folding folding;

        private LookupInfo(String raw)
        {
            String key = raw;
            Folding folding = Folding.NONE;
            if (raw.endsWith("^^"))
            {
                folding = Folding.UPPER;
                key = StringUtils.removeEnd(raw, "^^");
            }
            else if (raw.endsWith(",,"))
            {
                folding = Folding.LOWER;
                key = StringUtils.removeEnd(raw, ",,");
            }
            this.folding = folding;
            this.key = key;
        }
    }

    private static final StringLookup LOOKUP_SYS = StringLookupFactory.INSTANCE.systemPropertyStringLookup();
    private static final StringLookup LOOKUP_ENV = StringLookupFactory.INSTANCE.environmentVariableStringLookup();
    private static final StringLookup LOOKUP_ALL = StringLookupFactory.INSTANCE.interpolatorStringLookup();

    private static final String rawLookup(String key)
    {
        String r = null;

        // System property?
        r = NativeEnvironmentInterpolator.LOOKUP_SYS.lookup(key);
        if (r != null)
        {
            return r;
        }

        // Environment variable?
        r = NativeEnvironmentInterpolator.LOOKUP_ENV.lookup(key);
        if (r != null)
        {
            return r;
        }

        // No hits
        return null;
    }

    private static final String lookup(String key)
    {
        LookupInfo lookup = new LookupInfo(key);
        String result = NativeEnvironmentInterpolator.rawLookup(lookup.key);
        return lookup.folding.fold.apply(result);
    }

    private static final String rawLookupExtras(String key)
    {
        String r = null;

        // Try the simple lookup first
        r = NativeEnvironmentInterpolator.rawLookup(key);
        if (r != null)
        {
            return r;
        }

        // Use the extra lookups
        r = NativeEnvironmentInterpolator.LOOKUP_ALL.lookup(key);
        if (r != null)
        {
            return r;
        }

        // No hits
        return null;
    }

    private static final String lookupExtras(String key)
    {
        LookupInfo lookup = new LookupInfo(key);
        String result = NativeEnvironmentInterpolator.rawLookupExtras(lookup.key);
        return lookup.folding.fold.apply(result);
    }

    private final NativeEnvironmentInterpolatorProperties properties;

    private final UnaryOperator<String> resolver;

    @Autowired
    public NativeEnvironmentInterpolator(NativeEnvironmentInterpolatorProperties properties) throws IOException
    {
        this.log.debug("Creating the NativeEnvironmentInterpolator");
        if (this.log.isTraceEnabled())
        {
            this.log.trace("NativeEnvironmentInterpolatorProperties:\n{}", new Yaml().dump(properties));
        }

        this.properties = ObjectUtils.defaultIfNull(properties, NativeEnvironmentInterpolatorProperties.DEFAULT);

        final StringSubstitutor substitutor;

        if (this.properties.interpolator)
        {
            // Our interpolator will first try system properties, then try environment variables...
            // then try the default interpolator, and finally give up ...
            this.log.info("NativeEnvironmentInterpolator's interpolator is enabled (extras = {})", this.properties.interpolatorExtras);
            StringLookup lookup = (this.properties.interpolatorExtras ? NativeEnvironmentInterpolator::lookup : NativeEnvironmentInterpolator::lookupExtras);
            substitutor = new StringSubstitutor(lookup);
            this.resolver = substitutor::replace;
        }
        else
        {
            this.log.info("NativeEnvironmentInterpolator's interpolator is disabled, using a strict lookup");
            substitutor = new StringSubstitutor(NativeEnvironmentInterpolator.NULL_LOOKUP);
            this.resolver = NativeEnvironmentInterpolator.NULL_RESOLVER;
        }

        // Re-add this ... apparently the simplified syntax causes conflicts with
        // the OOTB Spring stuff...
        substitutor
                .setVariablePrefix("@{")
                .setVariableSuffix("}");
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
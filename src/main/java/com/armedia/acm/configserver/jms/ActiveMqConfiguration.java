package com.armedia.acm.configserver.jms;

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

import com.google.common.base.MoreObjects;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "acm.activemq")
public class ActiveMqConfiguration
{
    private String brokerUrl;

    private String user;

    private String password;

    private String keystore;

    private String keystorePassword;

    private String truststore;

    private String truststorePassword;

    private String defaultDestination;

    private int timeout;

    public String getBrokerUrl()
    {
        return brokerUrl;
    }

    public void setBrokerUrl(String brokerUrl)
    {
        this.brokerUrl = brokerUrl;
    }

    public String getUser()
    {
        return user;
    }

    public void setUser(String user)
    {
        this.user = user;
    }

    public String getPassword()
    {
        return password;
    }

    public void setPassword(String password)
    {
        this.password = password;
    }

    public String getKeystore()
    {
        return keystore;
    }

    public void setKeystore(String keystore)
    {
        this.keystore = keystore;
    }

    public String getKeystorePassword()
    {
        return keystorePassword;
    }

    public void setKeystorePassword(String keystorePassword)
    {
        this.keystorePassword = keystorePassword;
    }

    public String getTruststore()
    {
        return truststore;
    }

    public void setTruststore(String truststore)
    {
        this.truststore = truststore;
    }

    public String getTruststorePassword()
    {
        return truststorePassword;
    }

    public void setTruststorePassword(String truststorePassword)
    {
        this.truststorePassword = truststorePassword;
    }

    public String getDefaultDestination()
    {
        return defaultDestination;
    }

    public void setDefaultDestination(String defaultDestination)
    {
        this.defaultDestination = defaultDestination;
    }

    public int getTimeout()
    {
        return timeout;
    }

    public void setTimeout(int timeout)
    {
        this.timeout = timeout;
    }

    @Override
    public String toString()
    {
        return MoreObjects.toStringHelper(this)
                .add("brokerUrl", brokerUrl)
                .add("user", user)
                .add("defaultDestination", defaultDestination)
                .add("timeout", timeout)
                .toString();
    }
}

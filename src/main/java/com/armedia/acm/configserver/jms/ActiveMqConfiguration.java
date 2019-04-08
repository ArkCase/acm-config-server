package com.armedia.acm.configserver.jms;

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
}

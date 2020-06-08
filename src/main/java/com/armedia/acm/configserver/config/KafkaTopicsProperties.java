package com.armedia.acm.configserver.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "arkcase.kafka")
public class KafkaTopicsProperties
{
    private String configurationChangedTopic;
    private int configurationChangedTopicReplicas;
    private int configurationChangedTopicPartitions;
    private String labelsChangedTopic;
    private int labelsChangedTopicReplicas;
    private int labelsChangedTopicPartitions;
    private String ldapChangedTopic;
    private int ldapChangedTopicReplicas;
    private int ldapChangedTopicPartitions;
    private String retentionMs;

    public String getConfigurationChangedTopic()
    {
        return configurationChangedTopic;
    }

    public void setConfigurationChangedTopic(String configurationChangedTopic)
    {
        this.configurationChangedTopic = configurationChangedTopic;
    }

    public int getConfigurationChangedTopicReplicas()
    {
        return configurationChangedTopicReplicas;
    }

    public void setConfigurationChangedTopicReplicas(int configurationChangedTopicReplicas)
    {
        this.configurationChangedTopicReplicas = configurationChangedTopicReplicas;
    }

    public int getConfigurationChangedTopicPartitions()
    {
        return configurationChangedTopicPartitions;
    }

    public void setConfigurationChangedTopicPartitions(int configurationChangedTopicPartitions)
    {
        this.configurationChangedTopicPartitions = configurationChangedTopicPartitions;
    }

    public String getLabelsChangedTopic()
    {
        return labelsChangedTopic;
    }

    public void setLabelsChangedTopic(String labelsChangedTopic)
    {
        this.labelsChangedTopic = labelsChangedTopic;
    }

    public int getLabelsChangedTopicReplicas()
    {
        return labelsChangedTopicReplicas;
    }

    public void setLabelsChangedTopicReplicas(int labelsChangedTopicReplicas)
    {
        this.labelsChangedTopicReplicas = labelsChangedTopicReplicas;
    }

    public int getLabelsChangedTopicPartitions()
    {
        return labelsChangedTopicPartitions;
    }

    public void setLabelsChangedTopicPartitions(int labelsChangedTopicPartitions)
    {
        this.labelsChangedTopicPartitions = labelsChangedTopicPartitions;
    }

    public String getLdapChangedTopic()
    {
        return ldapChangedTopic;
    }

    public void setLdapChangedTopic(String ldapChangedTopic)
    {
        this.ldapChangedTopic = ldapChangedTopic;
    }

    public int getLdapChangedTopicReplicas()
    {
        return ldapChangedTopicReplicas;
    }

    public void setLdapChangedTopicReplicas(int ldapChangedTopicReplicas)
    {
        this.ldapChangedTopicReplicas = ldapChangedTopicReplicas;
    }

    public int getLdapChangedTopicPartitions()
    {
        return ldapChangedTopicPartitions;
    }

    public void setLdapChangedTopicPartitions(int ldapChangedTopicPartitions)
    {
        this.ldapChangedTopicPartitions = ldapChangedTopicPartitions;
    }

    public String getRetentionMs()
    {
        return retentionMs;
    }

    public void setRetentionMs(String retentionMs)
    {
        this.retentionMs = retentionMs;
    }
}

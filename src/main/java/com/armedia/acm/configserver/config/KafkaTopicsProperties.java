package com.armedia.acm.configserver.config;

/*-
 * #%L
 * acm-config-server
 * %%
 * Copyright (C) 2019 - 2020 ArkCase LLC
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
    private String configurationFileCreatedTopic;
    private int configurationFileCreatedTopicReplicas;
    private int configurationFileCreatedTopicPartitions;
    private String retentionMs;
    private int messageBufferWindow;

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

    public String getConfigurationFileCreatedTopic()
    {
        return configurationFileCreatedTopic;
    }

    public void setConfigurationFileCreatedTopic(String configurationFileCreatedTopic)
    {
        this.configurationFileCreatedTopic = configurationFileCreatedTopic;
    }

    public int getConfigurationFileCreatedTopicReplicas()
    {
        return configurationFileCreatedTopicReplicas;
    }

    public void setConfigurationFileCreatedTopicReplicas(int configurationFileCreatedTopicReplicas)
    {
        this.configurationFileCreatedTopicReplicas = configurationFileCreatedTopicReplicas;
    }

    public int getConfigurationFileCreatedTopicPartitions()
    {
        return configurationFileCreatedTopicPartitions;
    }

    public void setConfigurationFileCreatedTopicPartitions(int configurationFileCreatedTopicPartitions)
    {
        this.configurationFileCreatedTopicPartitions = configurationFileCreatedTopicPartitions;
    }

    public String getRetentionMs()
    {
        return retentionMs;
    }

    public void setRetentionMs(String retentionMs)
    {
        this.retentionMs = retentionMs;
    }

    public int getMessageBufferWindow()
    {
        return messageBufferWindow;
    }

    public void setMessageBufferWindow(int messageBufferWindow)
    {
        this.messageBufferWindow = messageBufferWindow;
    }
}

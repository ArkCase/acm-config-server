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
    private String lookupsChangedTopic;
    private int lookupsChangedTopicReplicas;
    private int lookupsChangedTopicPartitions;
    private String rulesChangedTopic;
    private int rulesChangedTopicReplicas;
    private int rulesChangedTopicPartitions;
    private String formsChangedTopic;
    private int formsChangedTopicReplicas;
    private int formsChangedTopicPartitions;
    private String menuChangedTopic;
    private int menuChangedTopicReplicas;
    private int menuChangedTopicPartitions;
    private String configurationFileCreatedTopic;
    private int configurationFileCreatedTopicReplicas;
    private int configurationFileCreatedTopicPartitions;
    private String retentionMs;
    private int messageBufferWindow;
    private String formSchemaFileTopic;
    private int formSchemaFileTopicReplicas;
    private int formSchemaFileTopicPartitions;
    private String menuSchemaFileTopic;
    private int menuSchemaFileTopicReplicas;
    private int menuSchemaFileTopicPartitions;
    private String avroSchemaFileTopic;
    private int avroSchemaFileTopicReplicas;
    private int avroSchemaFileTopicPartitions;
    private String processFileTopic;
    private int processFileTopicReplicas;
    private int processFileTopicPartitions;

    public int getFormsChangedTopicPartitions() { return formsChangedTopicPartitions; }

    public void setFormsChangedTopicPartitions(int formsChangedTopicPartitions)
    {
        this.formsChangedTopicPartitions = formsChangedTopicPartitions;
    }

    public int getFormsChangedTopicReplicas() { return formsChangedTopicReplicas; }

    public void setFormsChangedTopicReplicas(int formsChangedTopicReplicas)
    {
        this.formsChangedTopicReplicas = formsChangedTopicReplicas;
    }

    public String getFormsChangedTopic() { return formsChangedTopic; }

    public void setFormsChangedTopic(String formsChangedTopic)
    {
        this.formsChangedTopic = formsChangedTopic;
    }

    public String getMenuChangedTopic()
    {
        return menuChangedTopic;
    }

    public void setMenuChangedTopic(String menuChangedTopic)
    {
        this.menuChangedTopic = menuChangedTopic;
    }

    public int getMenuChangedTopicReplicas()
    {
        return menuChangedTopicReplicas;
    }

    public void setMenuChangedTopicReplicas(int menuChangedTopicReplicas)
    {
        this.menuChangedTopicReplicas = menuChangedTopicReplicas;
    }

    public int getMenuChangedTopicPartitions()
    {
        return menuChangedTopicPartitions;
    }

    public void setMenuChangedTopicPartitions(int menuChangedTopicPartitions)
    {
        this.menuChangedTopicPartitions = menuChangedTopicPartitions;
    }

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

    public String getLookupsChangedTopic()
    {
        return lookupsChangedTopic;
    }

    public void setLookupsChangedTopic(String lookupsChangedTopic)
    {
        this.lookupsChangedTopic = lookupsChangedTopic;
    }

    public int getLookupsChangedTopicReplicas()
    {
        return lookupsChangedTopicReplicas;
    }

    public void setLookupsChangedTopicReplicas(int lookupsChangedTopicReplicas)
    {
        this.lookupsChangedTopicReplicas = lookupsChangedTopicReplicas;
    }

    public int getLookupsChangedTopicPartitions()
    {
        return lookupsChangedTopicPartitions;
    }

    public void setLookupsChangedTopicPartitions(int lookupsChangedTopicPartitions)
    {
        this.lookupsChangedTopicPartitions = lookupsChangedTopicPartitions;
    }

    public String getRulesChangedTopic()
    {
        return rulesChangedTopic;
    }

    public void setRulesChangedTopic(String rulesChangedTopic)
    {
        this.rulesChangedTopic = rulesChangedTopic;
    }

    public int getRulesChangedTopicReplicas()
    {
        return rulesChangedTopicReplicas;
    }

    public void setRulesChangedTopicReplicas(int rulesChangedTopicReplicas)
    {
        this.rulesChangedTopicReplicas = rulesChangedTopicReplicas;
    }

    public int getRulesChangedTopicPartitions()
    {
        return rulesChangedTopicPartitions;
    }

    public void setRulesChangedTopicPartitions(int rulesChangedTopicPartitions)
    {
        this.rulesChangedTopicPartitions = rulesChangedTopicPartitions;
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

    public String getFormSchemaFileTopic()
    {
        return formSchemaFileTopic;
    }

    public void setFormSchemaFileTopic(String formSchemaFileTopic)
    {
        this.formSchemaFileTopic = formSchemaFileTopic;
    }

    public int getFormSchemaFileTopicReplicas()
    {
        return formSchemaFileTopicReplicas;
    }

    public void setFormSchemaFileTopicReplicas(int formSchemaFileTopicReplicas)
    {
        this.formSchemaFileTopicReplicas = formSchemaFileTopicReplicas;
    }

    public int getFormSchemaFileTopicPartitions()
    {
        return formSchemaFileTopicPartitions;
    }

    public void setFormSchemaFileTopicPartitions(int formSchemaFileTopicPartitions)
    {
        this.formSchemaFileTopicPartitions = formSchemaFileTopicPartitions;
    }

    public String getMenuSchemaFileTopic()
    {
        return menuSchemaFileTopic;
    }

    public void setMenuSchemaFileTopic(String menuSchemaFileTopic)
    {
        this.menuSchemaFileTopic = menuSchemaFileTopic;
    }

    public int getMenuSchemaFileTopicReplicas()
    {
        return menuSchemaFileTopicReplicas;
    }

    public void setMenuSchemaFileTopicReplicas(int menuSchemaFileTopicReplicas)
    {
        this.menuSchemaFileTopicReplicas = menuSchemaFileTopicReplicas;
    }

    public int getMenuSchemaFileTopicPartitions()
    {
        return menuSchemaFileTopicPartitions;
    }

    public void setMenuSchemaFileTopicPartitions(int menuSchemaFileTopicPartitions)
    {
        this.menuSchemaFileTopicPartitions = menuSchemaFileTopicPartitions;
    }

    public String getAvroSchemaFileTopic()
    {
        return avroSchemaFileTopic;
    }

    public void setAvroSchemaFileTopic(String avroSchemaFileTopic)
    {
        this.avroSchemaFileTopic = avroSchemaFileTopic;
    }

    public int getAvroSchemaFileTopicReplicas()
    {
        return avroSchemaFileTopicReplicas;
    }

    public void setAvroSchemaFileTopicReplicas(int avroSchemaFileTopicReplicas)
    {
        this.avroSchemaFileTopicReplicas = avroSchemaFileTopicReplicas;
    }

    public int getAvroSchemaFileTopicPartitions()
    {
        return avroSchemaFileTopicPartitions;
    }

    public void setAvroSchemaFileTopicPartitions(int avroSchemaFileTopicPartitions)
    {
        this.avroSchemaFileTopicPartitions = avroSchemaFileTopicPartitions;
    }

    public String getProcessFileTopic()
    {
        return processFileTopic;
    }

    public void setProcessFileTopic(String processFileTopic)
    {
        this.processFileTopic = processFileTopic;
    }

    public int getProcessFileTopicReplicas()
    {
        return processFileTopicReplicas;
    }

    public void setProcessFileTopicReplicas(int processFileTopicReplicas)
    {
        this.processFileTopicReplicas = processFileTopicReplicas;
    }

    public int getProcessFileTopicPartitions()
    {
        return processFileTopicPartitions;
    }

    public void setProcessFileTopicPartitions(int processFileTopicPartitions)
    {
        this.processFileTopicPartitions = processFileTopicPartitions;
    }
}

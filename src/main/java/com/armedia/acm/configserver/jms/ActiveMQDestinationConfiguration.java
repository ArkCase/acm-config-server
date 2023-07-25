package com.armedia.acm.configserver.jms;

/*-
 * #%L
 * acm-config-server
 * %%
 * Copyright (C) 2019 - 2023 ArkCase LLC
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
@ConfigurationProperties(prefix = "acm.activemq.destinations")
public class ActiveMQDestinationConfiguration
{
    private String labelsDestination;

    private String ldapDestination;

    private String lookupsDestination;

    private String rulesDestination;
    private String permissionsDestination;


    public String getLabelsDestination()
    {
        return labelsDestination;
    }

    public void setLabelsDestination(String labelsDestination)
    {
        this.labelsDestination = labelsDestination;
    }

    public String getLdapDestination()
    {
        return ldapDestination;
    }

    public void setLdapDestination(String ldapDestination)
    {
        this.ldapDestination = ldapDestination;
    }

    public String getLookupsDestination()
    {
        return lookupsDestination;
    }

    public void setLookupsDestination(String lookupsDestination)
    {
        this.lookupsDestination = lookupsDestination;
    }

    public String getRulesDestination()
    {
        return rulesDestination;
    }

    public void setRulesDestination(String rulesDestination)
    {
        this.rulesDestination = rulesDestination;
    }

    public String getPermissionsDestination()
    {
        return permissionsDestination;
    }

    public void setPermissionsDestination(String permissionsDestination)
    {
        this.permissionsDestination = permissionsDestination;
    }
}

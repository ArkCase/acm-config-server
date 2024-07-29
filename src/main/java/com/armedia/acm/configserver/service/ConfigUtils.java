package com.armedia.acm.configserver.service;

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

import java.util.List;
import java.util.Optional;

import lombok.experimental.UtilityClass;

@UtilityClass
public class ConfigUtils
{

    public static String extractFromFileName(String fileName, List<String> languages)
    {
        if(fileName.contains("lookups"))
            return "lookups";

        if(fileName.contains("ldap"))
            return "ldap";

        /* TODO: Not sure what do we sent on permissions handler
        if(fileName.contains("permission"))
            return "permission";
        */

        /* TODO: Not sure what to do with rules - xls file - probably they will go to file storage with branding files and others
        if(fileName.contains("drools"))
            return "drools";
        */

        if (languages.stream().anyMatch(fileName::contains))
            return "labels";

        return "default";
    }

    public static Optional<String> findProfile(String applicationName, List<String> profiles)
    {
        return profiles.stream()
                .filter(applicationName::contains)
                .findFirst();
    }
}

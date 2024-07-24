package com.armedia.acm.configserver.service;

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

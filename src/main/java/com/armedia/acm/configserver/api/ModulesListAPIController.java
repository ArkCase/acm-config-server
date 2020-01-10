package com.armedia.acm.configserver.api;

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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/config")
public class ModulesListAPIController
{
    private static final Logger logger = LoggerFactory.getLogger(ModulesListAPIController.class);

    private final List<String> langs;

    private final String labelsFolderPath;

    public ModulesListAPIController(@Value("${properties.folder.path}") String propertiesFolderPath,
            @Value("${arkcase.languages}") String arkcaseLanguages)
    {
        this.labelsFolderPath = propertiesFolderPath + "/labels";
        this.langs = Arrays.asList(arkcaseLanguages.split(","));
    }

    @GetMapping(value = "/modules", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<String> getModules()
    {
        return getModulesNames();
    }

    /**
     * Return list of modules configuration
     *
     * @return
     */
    public List<String> getModulesNames()
    {
        File modulesDir = new File(labelsFolderPath);

        File[] files = modulesDir.listFiles(file -> {
            if (file.isFile() && !file.getName().toLowerCase().contains("-runtime"))
            {
                return true;
            }
            return false;
        });

        List<String> modules = new ArrayList<>();

        for (File labelResource : files)
        {
            for (String lang : langs)
            {
                String fileName = labelResource.getName();
                if (fileName.contains(lang))
                {
                    int sepPos = fileName.indexOf(lang);
                    String moduleName = fileName.substring(0, sepPos);
                    if (!modules.stream().anyMatch(module -> module.equals(moduleName)))
                    {
                        modules.add(moduleName);
                    }
                }
            }
        }

        logger.info("Returns modules names. [{}]", modules.toArray());
        return modules;
    }
}

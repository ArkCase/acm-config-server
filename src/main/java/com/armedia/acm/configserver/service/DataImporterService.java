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

import com.armedia.acm.configserver.model.ArkcaseConfig;
import com.armedia.acm.configserver.repository.ApplicationPropertyRepository;

import org.springframework.context.annotation.Profile;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@Profile("run-with-db")
public class DataImporterService extends DataService
{

    protected DataImporterService(ApplicationPropertyRepository applicationPropertyRepository, ArkcaseConfig arkcaseConfig)
    {
        super(applicationPropertyRepository, arkcaseConfig);
    }

    public void importData()
    {
        if (Boolean.TRUE.equals(this.applicationPropertyRepository.existsAnyRecord()))
            return;

        var resolver = new PathMatchingResourcePatternResolver();

        for (var folder : this.arkcaseConfig.getFolders())
        {
            try
            {
                var resources = resolver.getResources("file:" + folder + "/*.yaml");
                for (var resource : resources)
                {
                    super.readResource(resource);
                }
            }
            catch (IOException e)
            {
                log.error("Unable to resolve resources in folder: {}, reason: {}", folder, e.getMessage(), e);
            }
        }
    }
}

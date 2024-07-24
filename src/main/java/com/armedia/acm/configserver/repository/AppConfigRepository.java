package com.armedia.acm.configserver.repository;

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

import com.armedia.acm.configserver.model.AppConfig;

import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@Profile("run-with-db")
public interface AppConfigRepository extends JpaRepository<AppConfig, Long> {
    AppConfig findByApplicationAndProfileAndKey(String application, String profile, String key);

    void deleteAllByProfile(String profile);
    void deleteByApplicationAndProfileAndKeyIn(String application, String profile, List<String> keys);
    void deleteByApplicationAndProfile(String appNameWithoutProfile, String runtime);

    @Query("SELECT DISTINCT a.label FROM AppConfig a")
    List<String> findAllUniqueLabels();

    @Query("SELECT CASE WHEN COUNT(c) > 0 THEN TRUE ELSE FALSE END FROM AppConfig c")
    Boolean existsAnyRecord();

}

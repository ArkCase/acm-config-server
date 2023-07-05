package com.armedia.acm.configserver;

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
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.config.server.EnableConfigServer;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.scheduling.annotation.EnableAsync;

import com.armedia.acm.curator.Session;
import com.armedia.acm.curator.recipe.Leader;

@EnableConfigServer
@SpringBootApplication
@EnableJms
@EnableAsync
public class AcmConfigServerApplication
{

    private static final Logger LOG = LoggerFactory.getLogger(AcmConfigServerApplication.class);

    private static void run(String... args)
    {
        // Launch the spring boot application
        AcmConfigServerApplication.LOG.info("Launching the main workload");
        SpringApplication.run(AcmConfigServerApplication.class, args);

        // In this mode of operation we have to wait until we're interrupted b/c
        // the Spring Boot app runs in the background
        final Object waiter = new Object();
        synchronized (waiter)
        {
            while (true)
            {
                try
                {
                    // This is a simple means of waiting forever without consuming resources
                    AcmConfigServerApplication.LOG.info("Waiting until interrupted");
                    waiter.wait();
                }
                catch (InterruptedException e)
                {
                    // If we're interrupted, we simply return
                    return;
                }
            }
        }
    }

    public static void main(String... args) throws Exception
    {
        try (Session session = ClusterConfig.newSessionBuilder().build())
        {
            if (session.isEnabled())
            {
                // This is the new, "clusterable" code path
                AcmConfigServerApplication.LOG.info("Running in clustered mode");
                // TODO: Should we instead make this listen on a different port
                // to signal readiness? I.e. port 6666? It would just
                // listen on that address and instantly close the connections,
                // thus only being useful for TCP connectivity checks
                try (AutoCloseable leadership = new Leader(session, "cloudconfig").awaitLeadership())
                {
                    AcmConfigServerApplication.run(args);
                }
                catch (Exception e)
                {
                    AcmConfigServerApplication.LOG.error("Exception caught from the curator wrapper", e);
                }
            }
            else
            {
                // This is the original code path, plus more logging :D
                AcmConfigServerApplication.LOG.info("Running in standalone mode");
                AcmConfigServerApplication.run(args);
            }
        }
    }
}
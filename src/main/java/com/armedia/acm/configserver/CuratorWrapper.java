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

package com.armedia.acm.configserver;

import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.leader.LeaderSelector;
import org.apache.curator.framework.recipes.leader.LeaderSelectorListener;
import org.apache.curator.framework.recipes.leader.LeaderSelectorListenerAdapter;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.retry.RetryForever;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class CuratorWrapper
{
    private static final int DEFAULT_RETRY_COUNT = 3;

    private static final int MIN_RETRY_DELAY = 100;
    private static final int DEFAULT_RETRY_DELAY = 1000;

    private final Logger log = LoggerFactory.getLogger(getClass());

    private void waitUntilInterrupted()
    {
        final Object waiter = new Object();
        synchronized (waiter)
        {
            while (true)
            {
                try
                {
                    // This is a simple means of waiting forever without consuming resources
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

    private String sanitize(String value)
    {
        return (value != null ? value.trim() : value);
    }

    private String getClusterValue(String name)
    {
        String prop = null;
        String v = null;

        // Step one: is it set via a system property?
        prop = String.format("arkcase.cluster.%s", name);
        this.log.debug("Seeking system property [{}]", prop);
        v = System.getProperty(prop);
        if (v != null)
        {
            this.log.debug("Value found: [{}]", v);
            return sanitize(v);
        }

        // Step two: is it set via environment variable?
        prop = String.format("ARKCASE_CLUSTER_%s", name.toUpperCase());
        this.log.debug("Seeking environment property [{}]", prop);
        v = System.getenv(prop);
        if (v != null)
        {
            this.log.debug("Value found: [{}]", v);
            return sanitize(v);
        }

        // No matches? Return null...
        this.log.debug("No values found for property '{}'", name);
        return null;
    }

    private final String zk;

    CuratorWrapper()
    {
        this.zk = getClusterValue("zookeeper");
    }

    boolean isEnabled()
    {
        return (this.zk != null);
    }

    void run(final Consumer<String[]> run, String... args) throws Exception
    {
        if (!isEnabled())
        {
            throw new IllegalStateException("Clustering is not enabled");
        }

        this.log.info("ZooKeeper connection string: [{}]", this.zk);
        String uuidStr = getClusterValue("clusterId");
        if (uuidStr == null)
        {
            throw new RuntimeException("ERROR: The Zookeeper UUID was not provided, cannot continue in clustered mode");
        }

        UUID uuid = null;
        try
        {
            uuid = UUID.fromString(uuidStr);
        }
        catch (IllegalArgumentException e)
        {
            throw new RuntimeException(
                    String.format("The UUID [%s] is not a valid UUID valie, cannot continue%n", uuidStr), e);
        }

        this.log.info("Cluster mode enabled with UUID = [{}]", uuid);

        final String path = String.format("/arkcase/cloudconfig/%s/node-", uuid);
        this.log.debug("ZooKeeper node path: [{}]", path);

        int retryCount = CuratorWrapper.DEFAULT_RETRY_COUNT;
        String retryCountStr = getClusterValue("retries");
        if (retryCountStr != null)
        {
            try
            {
                retryCount = Integer.valueOf(retryCountStr);
            }
            catch (NumberFormatException e)
            {
                retryCount = CuratorWrapper.DEFAULT_RETRY_COUNT;
                this.log.warn("Invalid retry count value [{}] - will default to {}", retryCountStr, retryCount);
            }
        }

        int retryDelay = CuratorWrapper.DEFAULT_RETRY_DELAY;
        String retryDelayStr = getClusterValue("retryDelay");
        if (retryDelayStr != null)
        {
            try
            {
                // Ensure it's never below the minimum...
                retryDelay = Math.max(CuratorWrapper.MIN_RETRY_DELAY, Integer.valueOf(retryDelayStr));
            }
            catch (NumberFormatException e)
            {
                retryDelay = CuratorWrapper.DEFAULT_RETRY_DELAY;
                this.log.warn("Invalid retry delay value [{}] - will default to {}", retryDelayStr, retryDelay);
            }
        }

        final RetryPolicy retryPolicy = (retryCount <= 0 ? new RetryForever(retryDelay)
                : new ExponentialBackoffRetry(retryDelay, retryCount));

        this.log.debug("Clustering retry policy is {}, with a delay of {}", retryPolicy.getClass().getSimpleName(),
                retryDelay);
        if (retryCount > 0)
        {
            this.log.debug("Clustering retry count is {}", retryCount);
        }

        final AtomicReference<Exception> thrown = new AtomicReference<>();
        this.log.trace("Initializing the Curator client");
        final CuratorFramework client = CuratorFrameworkFactory.newClient(this.zk, (int) Duration.ofSeconds(1).toMillis(),
                (int) Duration.ofSeconds(15).toMillis(), retryPolicy);
        final Thread shutdownHook = new Thread(() -> {
            this.log.warn("Emergency closing of the curator client");
            client.close();
        }, "Curator-Shutdown");
        Runtime.getRuntime().addShutdownHook(shutdownHook);
        try
        {
            this.log.info("Starting the Curator client");
            client.start();

            // This will help us make the main thread sit idly until the background thread is
            // done with its leadership selection work ...
            final CyclicBarrier barrier = new CyclicBarrier(2);

            final LeaderSelectorListener listener = new LeaderSelectorListenerAdapter()
            {
                @Override
                public void takeLeadership(CuratorFramework client) throws Exception
                {
                    try
                    {
                        CuratorWrapper.this.log.info("Leadership role acquired, starting the listener");
                        run.accept(args);
                        CuratorWrapper.this.log.info("Main loop launched");
                        waitUntilInterrupted();
                    }
                    catch (final Exception e)
                    {
                        thrown.set(e);
                    }
                    finally
                    {
                        CuratorWrapper.this.log.trace("Awaiting on the barrier");
                        try
                        {
                            barrier.await();
                        }
                        finally
                        {
                            CuratorWrapper.this.log.trace("The barrier wait is finished");
                        }
                    }
                }
            };

            this.log.trace("Creating the leadership selector");
            try (LeaderSelector selector = new LeaderSelector(client, path, listener))
            {

                this.log.trace("Enabling auto-requeue");
                selector.autoRequeue();

                this.log.trace("Starting the leadership selector");
                selector.start();

                // Now that we're listening and waiting to become leaders, we simply wait
                // until the main code block exits...
                this.log.trace("Awaiting on the barrier");
                try
                {
                    barrier.await();
                }
                finally
                {
                    this.log.trace("The barrier wait is finished");
                }
            }
        }
        finally
        {
            this.log.trace("Performing the final cleanup");
            client.close();
            Runtime.getRuntime().removeShutdownHook(shutdownHook);

            Exception e = thrown.get();
            if (e != null)
            {
                throw new Exception("Exception caught from the main execution loop", e);
            }
        }
    }
}
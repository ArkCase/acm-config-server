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
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.imps.CuratorFrameworkState;
import org.apache.curator.framework.recipes.leader.LeaderSelector;
import org.apache.curator.framework.recipes.leader.LeaderSelectorListener;
import org.apache.curator.framework.recipes.leader.LeaderSelectorListenerAdapter;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.retry.RetryForever;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class CuratorWrapper implements AutoCloseable
{
    private static final int DEFAULT_RETRY_COUNT = 3;
    private static final int MIN_RETRY_DELAY = 100;
    private static final int DEFAULT_RETRY_DELAY = 1000;

    private static final String BASE_PATH = "/arkcase/cloudconfig";
    private static final String BASE_LEADER_PATH = String.format("%s/leader", CuratorWrapper.BASE_PATH);
    private static final String BASE_MUTEX_PATH = String.format("%s/mutex", CuratorWrapper.BASE_PATH);

    private final Logger log = LoggerFactory.getLogger(getClass());

    private final String zk;
    private final RetryPolicy retryPolicy;
    private final CuratorFramework client;
    private final Thread cleanup;
    private final AtomicInteger selectorCounter = new AtomicInteger(0);
    private final Map<Integer, LeaderSelector> selectors = Collections.synchronizedMap(new TreeMap<>());

    CuratorWrapper() throws InterruptedException
    {
        this.zk = ClusterConfig.get("zookeeper");
        this.log.info("ZooKeeper connection string: [{}]", this.zk);

        if (this.zk == null)
        {
            this.retryPolicy = null;
            this.client = null;
            this.cleanup = null;
            return;
        }

        int retryCount = CuratorWrapper.DEFAULT_RETRY_COUNT;
        String retryCountStr = ClusterConfig.get("retry.count");
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
        String retryDelayStr = ClusterConfig.get("retry.delay");
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
        this.retryPolicy = (retryCount <= 0 //
                ? new RetryForever(retryDelay) //
                : new ExponentialBackoffRetry(retryDelay, retryCount) //
        );
        this.log.debug("Clustering retry policy is {}, with a delay of {}", this.retryPolicy.getClass().getSimpleName(),
                retryDelay);
        if (retryCount > 0)
        {
            this.log.debug("Clustering retry count is {}", retryCount);
        }

        this.log.trace("Initializing the Curator client");
        this.client = CuratorFrameworkFactory.newClient(this.zk, (int) Duration.ofSeconds(1).toMillis(),
                (int) Duration.ofSeconds(15).toMillis(), this.retryPolicy);
        this.log.info("Starting the Curator client");
        this.client.start();
        this.client.blockUntilConnected();
        this.cleanup = new Thread(this::cleanup, "CuratorWrapper-Cleanup");
        this.cleanup.setDaemon(false);
        Runtime.getRuntime().addShutdownHook(this.cleanup);
    }

    private void cleanup()
    {
        for (Integer i : this.selectors.keySet())
        {
            LeaderSelector l = this.selectors.get(i);
            this.log.warn("Emergency cleanup: closing out leadership selector # {}", i);
            try
            {
                l.close();
            }
            catch (Exception e)
            {
                this.log.warn("Exception caught during cleanup of leadership selector # {}", i, e);
            }
        }

        if (this.client.getState() != CuratorFrameworkState.STOPPED)
        {
            try
            {
                this.client.close();
            }
            catch (Exception e)
            {
                this.log.warn("Exception caught while closing the main client", e);
            }
        }
    }

    private boolean isEmpty(String str)
    {
        return ((str == null) || (str.length() == 0));
    }

    public boolean isEnabled()
    {
        return (this.client != null);
    }

    public AutoCloseable acquireMutex() throws Exception
    {
        return acquireMutex(null, null);
    }

    public AutoCloseable acquireMutex(Duration maxWait) throws Exception
    {
        return acquireMutex(null, maxWait);
    }

    public AutoCloseable acquireMutex(String mutexName) throws Exception
    {
        return acquireMutex(mutexName, null);
    }

    public AutoCloseable acquireMutex(String mutexName, Duration maxWait) throws Exception
    {
        if (!isEnabled())
        {
            throw new IllegalStateException("Clustering is not enabled");
        }
        final String mutexPath = (mutexName != null ? String.format("%s/%s", CuratorWrapper.BASE_MUTEX_PATH, mutexName)
                : CuratorWrapper.BASE_MUTEX_PATH);
        final InterProcessMutex lock = new InterProcessMutex(this.client, mutexPath);
        if ((maxWait != null) && !maxWait.isNegative() && !maxWait.isZero())
        {
            if (!lock.acquire(maxWait.toMillis(), TimeUnit.MILLISECONDS))
            {
                throw new IllegalStateException(String.format("Timed out acquiring the lock [%s] (timeout = %s)", mutexName, maxWait));
            }
        }
        else
        {
            lock.acquire();
        }

        this.log.trace("Acquired the lock [{}]", mutexName);
        return () -> {
            this.log.trace("Releasing the lock [{}]", mutexName);
            lock.release();
        };
    }

    public AutoCloseable acquireLeadership() throws InterruptedException
    {
        return acquireLeadership(null);
    }

    public AutoCloseable acquireLeadership(String leadershipName) throws InterruptedException
    {
        if (!isEnabled())
        {
            throw new IllegalStateException("Clustering is not enabled");
        }

        final String path = (isEmpty(leadershipName) //
                ? CuratorWrapper.BASE_LEADER_PATH //
                : String.format("%s/%s", CuratorWrapper.BASE_LEADER_PATH, leadershipName) //
        );
        this.log.debug("Leadership node path: [{}]", path);

        final CountDownLatch awaitLeadership = new CountDownLatch(1);
        final CountDownLatch awaitCompletion = new CountDownLatch(1);
        final int selectorKey = this.selectorCounter.getAndIncrement();
        final LeaderSelectorListener listener = new LeaderSelectorListenerAdapter()
        {
            @Override
            public void takeLeadership(CuratorFramework client)
            {
                CuratorWrapper.this.log.info("Assuming Leadership (# {})", selectorKey);
                awaitLeadership.countDown();
                try
                {
                    CuratorWrapper.this.log.info("Signalled the start of the execution, awaiting completion (# {})", selectorKey);
                    awaitCompletion.await();
                    CuratorWrapper.this.log.info("Execution completed; the latch returned normally (# {})", selectorKey);
                }
                catch (InterruptedException e)
                {
                    Thread.interrupted();
                    CuratorWrapper.this.log.error("Interrupted waiting for execution to complete (# {})", selectorKey);
                }
                finally
                {
                    CuratorWrapper.this.log.trace("Relinquishing leadership (# {})", selectorKey);
                }
            }
        };

        this.log.trace("Creating a new leadership selector");
        final LeaderSelector selector = new LeaderSelector(this.client, path, listener);
        this.log.trace("Starting the leadership selector (# {})", selectorKey);
        selector.start();
        this.selectors.put(selectorKey, selector);

        // We will block in this await() invocation until leadership is acquired.
        this.log.trace("Waiting for leadership to be attained (# {})", selectorKey);
        try
        {
            awaitLeadership.await();
        }
        catch (final InterruptedException e)
        {
            this.log.warn("Leadership wait interrupted (# {})!", selectorKey, e);
            Thread.interrupted();
            throw e;
        }
        finally
        {
            this.log.trace("The leadership wait is finished (# {})", selectorKey);
        }

        return () -> {
            this.log.info("Processing completed, relinquishing leadership (selector # {})", selectorKey);
            awaitCompletion.countDown();
            try
            {
                selector.close();
            }
            catch (Exception e)
            {
                this.log.warn("Exception caught closing down leadership selector # {}", selectorKey, e);
            }
            finally
            {
                this.selectors.remove(selectorKey);
            }
        };
    }

    @Override
    public void close()
    {
        try
        {
            cleanup();
        }
        finally
        {
            Runtime.getRuntime().removeShutdownHook(this.cleanup);
        }
    }
}
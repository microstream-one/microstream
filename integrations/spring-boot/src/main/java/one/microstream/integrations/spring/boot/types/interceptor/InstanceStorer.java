package one.microstream.integrations.spring.boot.types.interceptor;

/*-
 * #%L
 * microstream-integrations-spring-boot
 * %%
 * Copyright (C) 2019 - 2022 MicroStream Software
 * %%
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the Eclipse
 * Public License, v. 2.0 are satisfied: GNU General Public License, version 2
 * with the GNU Classpath Exception which is
 * available at https://www.gnu.org/software/classpath/license.html.
 *
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 * #L%
 */


import one.microstream.reference.Lazy;
import one.microstream.storage.types.StorageManager;
import org.aspectj.lang.reflect.Advice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Bean that is responsible for storing the instances through the Storage Manager. It stores it directly
 * through {@code storeChanged()} method or asynchronously through {@code queueForProcessing}. The asynchronous
 * way uses a thread and BlockingQueue.
 */
@Component
@ConditionalOnClass(Advice.class)
public class InstanceStorer
{

    private static final Logger LOGGER = LoggerFactory.getLogger(InstanceStorer.class);

    private final StorageManager manager;

    private final BlockingQueue<InstanceData> pendingChanges = new ArrayBlockingQueue<>(10000);

    private boolean signalledToStop;

    private final CountDownLatch stopPumpSynchronizer = new CountDownLatch(1);

    public InstanceStorer(final StorageManager manager)
    {
        this.manager = manager;
    }

    @PostConstruct
    public void init()
    {
        this.signalledToStop = false;
        this.initializePump();  // Start thread
    }

    @PreDestroy  // FIXME This doesn't seems to be working! It is never called By Spring
    public void stopPump()
    {
        this.signalledToStop = true;  // Flags the thread to stop its while loop
        boolean pumpStopped = false;
        try
        {
            // Wait max 1 seconds for the Thread to stop.
            pumpStopped = this.stopPumpSynchronizer.await(1, TimeUnit.SECONDS);
        } catch (InterruptedException e)
        {
            // Re-interrupt the current thread to have proper cleanup.
            Thread.currentThread()
                    .interrupt();
        }
        if (!pumpStopped)
        {
            LOGGER.warn("The Thread responsible for storing the objects marked as dirty did not stop properly in time");
        }
    }

    void initializePump()
    {
        final Thread pumpThread = new Thread(() ->
             // @formatter:off
             {
                 while (!signalledToStop)
                 {
                     InstanceData data = pendingChanges.peek();
                     // any data waiting?
                     if (data != null)
                     {
                         try
                         {
                             // We peeked, now remove it now from the queue.
                             // This does not block since we have data waiting.
                             data = pendingChanges.take();
                         } catch (InterruptedException e)
                         {
                             // Re-interrupt the current thread to have proper cleanup.
                             Thread.currentThread()
                                     .interrupt();
                         }

                         try
                         {
                             // Perform the store.
                             this.storeChanged(data.getDirtyInstance(), data.isClearLazy());
                         } catch (Throwable t)
                         {
                             LOGGER.warn("Problem during Asynchronous storage : " + t.getMessage(), t);
                         }
                     }
                     else
                     {

                         // let us wait a bit before peeking again so that we can check the state
                         try
                         {
                             // Busy waiting loop but can't change .peek() to .take() as
                             // that would leave us no way of stopping the thread.
                             Thread.sleep(500L);
                         } catch (InterruptedException e)
                         {
                             // Re-interrupt the current thread to have proper cleanup.
                             Thread.currentThread()
                                     .interrupt();
                         }
                     }
                 }
                 // Signal the CDI bean PreDestroy method that thread is stopped.
                 this.stopPumpSynchronizer.countDown();
             });
            // @formatter:on
        pumpThread.setName("InstanceStorer pending changes pump");
        pumpThread.setDaemon(true);
        pumpThread.start();
    }

    public void storeChanged(final Object dirtyInstance, final boolean clearLazy)
    {
        if (dirtyInstance instanceof Lazy)
        {
            // When a Lazy is marked, the developer probably wants to store the referenced instance in the Lazy.
            Object instance = ((Lazy<?>) dirtyInstance).peek();
            if (instance != null)
            {
                this.manager.store(instance);
            }
        }

        this.manager.store(dirtyInstance);
        if (clearLazy && dirtyInstance instanceof Lazy)
        {
            ((Lazy<?>) dirtyInstance).clear();
        }
    }

    public void queueForProcessing(final InstanceData instanceData)
    {
        this.pendingChanges.add(instanceData);
    }
}

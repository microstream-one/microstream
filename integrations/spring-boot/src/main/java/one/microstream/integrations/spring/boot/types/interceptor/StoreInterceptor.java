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

import one.microstream.integrations.spring.boot.types.Store;
import one.microstream.integrations.spring.boot.types.dirty.DirtyInstanceCollector;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.Advice;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Configuration;

import java.lang.reflect.Method;

@Aspect
@Configuration
@ConditionalOnClass(Advice.class)
public class StoreInterceptor
{
    private static final Logger LOGGER = LoggerFactory.getLogger(StoreInterceptor.class);

    @Autowired
    private DirtyInstanceCollector collector;

    @Autowired
    private InstanceStorer instanceStorer;

    @Around("@annotation(one.microstream.integrations.spring.boot.types.Store)")
    public Object saveItems(final ProceedingJoinPoint pjp) throws Throwable
    {
        Object result;
        try
        {
            result = pjp.proceed();

            final MethodSignature signature = (MethodSignature) pjp.getSignature();
            final Method method = signature.getMethod();

            // Only on method, Around Advice not picked on all methods when putting on class.
            final Store store = method.getAnnotation(Store.class);
            this.processDirtyInstances(store);
        } finally
        {
            // Clean out the list of dirty instances and make sure ThreadLocal is cleaned up.
            this.collector.processedInstances();

        }
        return result;
    }

    private void processDirtyInstances(final Store store)
    {
        for (Object dirtyInstance : this.collector.getDirtyInstances())
        {
            LOGGER.debug("Storing object type {}", dirtyInstance.getClass()
                    .getName());
            if (store.asynchronous())
            {
                this.instanceStorer.queueForProcessing(new InstanceData(dirtyInstance, store.clearLazy()));
            }
            else
            {
                this.instanceStorer.storeChanged(dirtyInstance, store.clearLazy());
            }
        }
    }
}

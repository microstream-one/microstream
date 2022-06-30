
package one.microstream.integrations.cdi.types.extension;

/*-
 * #%L
 * microstream-integrations-cdi
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

import static java.util.Optional.ofNullable;

import javax.annotation.Priority;
import javax.inject.Inject;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;

import one.microstream.integrations.cdi.types.Store;
import one.microstream.integrations.cdi.types.dirty.DirtyInstanceCollector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * CDI Interceptor to store the instances that are marked as dirty.
 */
@Store
@Interceptor
@Priority(Interceptor.Priority.APPLICATION)
public class StoreInterceptor
{
	private static final Logger LOGGER = LoggerFactory.getLogger(StoreInterceptor.class);

	@Inject
	private DirtyInstanceCollector collector;

	@Inject
	private InstanceStorer instanceStorer;
	
	@AroundInvoke
	public Object store(final InvocationContext context) throws Exception
	{
		
		final Store store = ofNullable(context.getMethod().getAnnotation(Store.class)).orElse(
			context.getMethod().getDeclaringClass().getAnnotation(Store.class));
		LOGGER.debug(
			"Using Store operation in the "
				+ context.getMethod()
				+ " using the store methodology: "
				+ (store.asynchronous() ? "Asynchronous" : "Synchronous")
		);
		
		final Object result = context.proceed();
		this.processDirtyInstances(store);
		
		return result;
	}

	private void processDirtyInstances(final Store store)
	{
		for (Object dirtyInstance : this.collector.getDirtyInstances()) {
			LOGGER.debug("Storing object type {}",dirtyInstance.getClass().getName());
			if (store.asynchronous()) {
				this.instanceStorer.queueForProcessing(new InstanceData(dirtyInstance, store.clearLazy()));
			} else {
				this.instanceStorer.storeChanged(dirtyInstance, store.clearLazy());
			}
		}

		// Clean out the list of dirty instances.
		this.collector.processedInstances();

	}
}

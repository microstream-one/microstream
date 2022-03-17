
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

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Priority;
import javax.inject.Inject;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;

import one.microstream.concurrency.XThreads;
import one.microstream.integrations.cdi.types.Store;
import one.microstream.storage.types.StorageManager;


@Store
@Interceptor
@Priority(Interceptor.Priority.APPLICATION)
class StoreInterceptor
{
	private static final Logger LOGGER = Logger.getLogger(StoreInterceptor.class.getName());
	
	@Inject
	private StorageManager      manager;
	
	@Inject
	private StorageExtension    extension;
	
	@AroundInvoke
	public Object store(final InvocationContext context) throws Exception
	{
		
		final Store store = ofNullable(context.getMethod().getAnnotation(Store.class)).orElse(
			context.getMethod().getDeclaringClass().getAnnotation(Store.class));
		LOGGER.log(
			Level.FINEST,
			"Using Store operation in the "
				+ context.getMethod()
				+ " using the store type: "
				+ store.value()
		);
		
		final Object result = context.proceed();
		XThreads.executeSynchronized(() ->
		{
			final StoreStrategy strategy = StoreStrategy.of(store);
			strategy.store(store, this.manager, this.extension);
		});
		
		return result;
	}
}

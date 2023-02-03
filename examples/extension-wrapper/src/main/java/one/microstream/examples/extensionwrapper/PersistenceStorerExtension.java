
package one.microstream.examples.extensionwrapper;

/*-
 * #%L
 * microstream-examples-extension-wrapper
 * %%
 * Copyright (C) 2019 - 2023 MicroStream Software
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

import one.microstream.persistence.binary.types.Binary;
import one.microstream.persistence.types.PersistenceObjectManager;
import one.microstream.persistence.types.PersistenceStorer;
import one.microstream.persistence.types.PersistenceTarget;
import one.microstream.persistence.types.PersistenceTypeHandlerManager;
import one.microstream.reference.ObjectSwizzling;
import one.microstream.util.BufferSizeProviderIncremental;


/**
 * Extension for {@link PersistenceStorer} which adds logic to store operations
 *
 */
public class PersistenceStorerExtension extends PersistenceStorerWrapper
{
	public PersistenceStorerExtension(final PersistenceStorer delegate)
	{
		super(delegate);
	}
	
	private void beforeStoreObject(final Object instance)
	{
		System.out.println("Storing " + instance.getClass().getName() + "@" + System.identityHashCode(instance));
	}
	
	@Override
	public long store(final Object instance)
	{
		this.beforeStoreObject(instance);
		
		return super.store(instance);
	}
	
	@Override
	public void storeAll(final Iterable<?> instances)
	{
		instances.forEach(this::beforeStoreObject);
		
		super.storeAll(instances);
	}
	
	@Override
	public long[] storeAll(final Object... instances)
	{
		for(final Object instance : instances)
		{
			this.beforeStoreObject(instance);
		}
		
		return super.storeAll(instances);
	}
	
	
	
	
	public static class Creator implements PersistenceStorer.Creator<Binary>
	{
		private final PersistenceStorer.Creator<Binary> delegate;

		public Creator(PersistenceStorer.Creator<Binary> delegate)
		{
			super();
			this.delegate = delegate;
		}

		@Override
		public PersistenceStorer createLazyStorer(
			final PersistenceTypeHandlerManager<Binary> typeManager       ,
			final PersistenceObjectManager<Binary>      objectManager     ,
			final ObjectSwizzling                       objectRetriever   ,
			final PersistenceTarget<Binary>             target            ,
			final BufferSizeProviderIncremental         bufferSizeProvider
		)
		{
			return new PersistenceStorerExtension(
				this.delegate.createLazyStorer(typeManager, objectManager, objectRetriever, target, bufferSizeProvider)
			);
		}

		@Override
		public PersistenceStorer createEagerStorer(
			final PersistenceTypeHandlerManager<Binary> typeManager       ,
			final PersistenceObjectManager<Binary>      objectManager     ,
			final ObjectSwizzling                       objectRetriever   ,
			final PersistenceTarget<Binary>             target            ,
			final BufferSizeProviderIncremental         bufferSizeProvider
		)
		{
			return new PersistenceStorerExtension(
				this.delegate.createEagerStorer(typeManager, objectManager, objectRetriever, target, bufferSizeProvider)
			);
		}
		
		
	}
	
}

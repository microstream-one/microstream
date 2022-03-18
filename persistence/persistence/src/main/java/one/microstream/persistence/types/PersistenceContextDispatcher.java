package one.microstream.persistence.types;

/*-
 * #%L
 * microstream-persistence
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

public interface PersistenceContextDispatcher<D>
{
	// loading //
	
	public default PersistenceTypeHandlerLookup<D> dispatchTypeHandlerLookup(
		final PersistenceTypeHandlerLookup<D> typeHandlerLookup
	)
	{
		return typeHandlerLookup;
	}
	
	public default PersistenceObjectRegistry dispatchObjectRegistry(
		final PersistenceObjectRegistry objectRegistry
	)
	{
		return objectRegistry;
	}
	
	// storing //
	
	public default PersistenceTypeHandlerManager<D> dispatchTypeHandlerManager(
		final PersistenceTypeHandlerManager<D> typeHandlerManager
	)
	{
		return typeHandlerManager;
	}
	
	public default PersistenceObjectManager<D> dispatchObjectManager(
		final PersistenceObjectManager<D> objectManager
	)
	{
		return objectManager;
	}
	
	
	
	public static <D> PersistenceContextDispatcher.PassThrough<D> PassThrough()
	{
		return new PersistenceContextDispatcher.PassThrough<>();
	}
	
	public static <D> PersistenceContextDispatcher.LocalObjectRegistration<D> LocalObjectRegistration()
	{
		return new PersistenceContextDispatcher.LocalObjectRegistration<>();
	}
	
	public final class PassThrough<D> implements PersistenceContextDispatcher<D>
	{
		PassThrough()
		{
			super();
		}
		
		// once again missing interface stateless instantiation.
	}
	
	public final class LocalObjectRegistration<D> implements PersistenceContextDispatcher<D>
	{
		LocalObjectRegistration()
		{
			super();
		}
		
		@Override
		public final PersistenceObjectRegistry dispatchObjectRegistry(
			final PersistenceObjectRegistry objectRegistry
		)
		{
			return objectRegistry.Clone();
		}
		
		@Override
		public final PersistenceObjectManager<D> dispatchObjectManager(
			final PersistenceObjectManager<D> objectManager
		)
		{
			return objectManager.Clone();
		}
		
	}
	
}

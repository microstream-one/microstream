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

import one.microstream.exceptions.InstantiationRuntimeException;
import one.microstream.memory.XMemory;


public interface PersistenceInstantiator<D>
{
	public <T> T instantiate(Class<T> type, D data) throws InstantiationRuntimeException;
		


	public static <T> T instantiateBlank(final Class<T> type)
	{
		return XMemory.instantiateBlank(type);
	}
	
	
	
	public static <D> PersistenceInstantiator<D> New()
	{
		return new PersistenceInstantiator.Default<>();
	}
	
	public final class Default<D> implements PersistenceInstantiator<D>, PersistenceTypeInstantiatorProvider<D>
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		Default()
		{
			super();
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		public <T> T instantiate(final Class<T> type, final D data)
			throws InstantiationRuntimeException
		{
			return PersistenceInstantiator.instantiateBlank(type);
		}
		
		@Override
		public <T> PersistenceTypeInstantiator<D, T> provideTypeInstantiator(final Class<T> type)
		{
			return PersistenceTypeInstantiator.New(type, this);
		}
		
	}
	
}

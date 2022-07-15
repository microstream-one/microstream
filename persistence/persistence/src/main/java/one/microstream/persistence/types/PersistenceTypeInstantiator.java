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

import static one.microstream.X.notNull;

@FunctionalInterface
public interface PersistenceTypeInstantiator<D, T>
{
	public T instantiate(D data);
	
	
	
	public static <T, D> PersistenceTypeInstantiator<D, T> New(final Class<T> type)
	{
		return New(type, PersistenceInstantiator.New());
	}
	
	public static <T, D> PersistenceTypeInstantiator<D, T> New(
		final Class<T>                   type                 ,
		final PersistenceInstantiator<D> universalInstantiator
	)
	{
		return new PersistenceTypeInstantiator.Default<>(
			notNull(type),
			notNull(universalInstantiator)
		);
	}
	
	public final class Default<D, T> implements PersistenceTypeInstantiator<D, T>
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private final Class<T>                   type                 ;
		private final PersistenceInstantiator<D> universalInstantiator;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		Default(
			final Class<T>                   type                 ,
			final PersistenceInstantiator<D> universalInstantiator
		)
		{
			super();
			this.type                  = type                 ;
			this.universalInstantiator = universalInstantiator;
		}


		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		public T instantiate(final D data)
		{
			return this.universalInstantiator.instantiate(this.type, data);
		}
		
	}
	
}

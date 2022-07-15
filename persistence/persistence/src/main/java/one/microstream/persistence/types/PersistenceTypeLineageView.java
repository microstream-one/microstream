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

import one.microstream.collections.EqConstHashTable;
import one.microstream.collections.types.XGettingTable;


public interface PersistenceTypeLineageView extends PersistenceTypeLineage
{
	@Override
	public default PersistenceTypeLineageView view()
	{
		return this;
	}
	
	@Override
	public default boolean registerTypeDefinition(final PersistenceTypeDefinition typeDefinition)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public default boolean setRuntimeTypeDefinition(final PersistenceTypeDefinition runtimeDefinition)
	{
		throw new UnsupportedOperationException();
	}
	
	
	public static PersistenceTypeLineageView New(final PersistenceTypeLineage typeLineage)
	{
		synchronized(typeLineage)
		{
			return new PersistenceTypeLineageView.Default(
				typeLineage.typeName()                     ,
				typeLineage.type()                         ,
				EqConstHashTable.New(typeLineage.entries()),
				typeLineage.runtimeDefinition()
			);
		}
	}
		
	public final class Default implements PersistenceTypeLineageView
	{
		////////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		final String                                            runtimeTypeName  ;
		final Class<?>                                          runtimeType      ;
		final EqConstHashTable<Long, PersistenceTypeDefinition> entries          ;
		final PersistenceTypeDefinition                         runtimeDefinition;



		////////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		Default(
			final String                                            runtimeTypeName  ,
			final Class<?>                                          runtimeType      ,
			final EqConstHashTable<Long, PersistenceTypeDefinition> entries          ,
			final PersistenceTypeDefinition                         runtimeDefinition
		)
		{
			super();
			this.runtimeTypeName   = runtimeTypeName  ;
			this.runtimeType       = runtimeType      ;
			this.entries           = entries          ;
			this.runtimeDefinition = runtimeDefinition;
		}



		////////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		public final String typeName()
		{
			return this.runtimeTypeName;
		}

		@Override
		public final XGettingTable<Long, PersistenceTypeDefinition> entries()
		{
			return this.entries;
		}

		@Override
		public final Class<?> type()
		{
			return this.runtimeType;
		}

		@Override
		public final PersistenceTypeDefinition runtimeDefinition()
		{
			return this.runtimeDefinition;
		}
		
		@Override
		public final PersistenceTypeDefinition latest()
		{
			return this.entries.values().peek();
		}
				
	}

}

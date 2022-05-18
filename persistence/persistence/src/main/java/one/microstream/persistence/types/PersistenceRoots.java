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

import static one.microstream.X.mayNull;
import static one.microstream.X.notNull;

import java.util.function.BiConsumer;

import one.microstream.collections.EqConstHashTable;
import one.microstream.collections.EqHashTable;
import one.microstream.collections.types.XGettingTable;
import one.microstream.util.cql.CQL;


public interface PersistenceRoots extends PersistenceRootsView
{
	@Override
	public PersistenceRootReference rootReference();
	
	public XGettingTable<String, Object> entries();
	
	public boolean hasChanged();
	
	public void reinitializeEntries(XGettingTable<String, Object> newEntries);

	public void updateEntries(XGettingTable<String, Object> newEntries);
	
	@Override
	public default <C extends BiConsumer<String, Object>> C iterateEntries(final C iterator)
	{
		this.entries().iterate(e ->
			iterator.accept(e.key(), e.value())
		);
		
		return iterator;
	}

	
	
	public static PersistenceRoots New(final PersistenceRootResolver rootResolver)
	{
		return PersistenceRoots.Default.New(rootResolver, null);
	}

	public final class Default implements PersistenceRoots
	{
		///////////////////////////////////////////////////////////////////////////
		// static methods //
		///////////////////
		
		public static PersistenceRoots.Default New(
			final PersistenceRootResolver   rootResolver ,
			final EqHashTable<String, Long> rootIdMapping
		)
		{
			return new PersistenceRoots.Default(
				notNull(rootResolver) ,
				mayNull(rootIdMapping),
				null                  ,
				false
			);
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		/*
		 * The transient keyword actually doesn't matter at all since a custom TypeHandler is used.
		 * Its only purpose here is to indicate that the fields are not directly persisted.
		 */

		private final transient PersistenceRootResolver          rootResolver   ;
		private       transient EqHashTable<String, Long>        rootIdMapping  ;
		private       transient EqConstHashTable<String, Object> resolvedEntries;
		private       transient boolean                          hasChanged     ;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		Default(
			final PersistenceRootResolver          rootResolver   ,
			final EqHashTable<String, Long>        rootIdMapping  ,
			final EqConstHashTable<String, Object> resolvedEntries,
			final boolean                          hasChanged
		)
		{
			super();
			this.rootResolver    = rootResolver   ;
			this.rootIdMapping   = rootIdMapping  ;
			this.resolvedEntries = resolvedEntries;
			this.hasChanged      = hasChanged     ;
		}

		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		@Override
		public final synchronized boolean hasChanged()
		{
			return this.hasChanged;
		}
		
		@Override
		public final synchronized PersistenceRootReference rootReference()
		{
			return this.rootResolver.root();
		}

		@Override
		public final synchronized XGettingTable<String, Object> entries()
		{
			if(this.resolvedEntries == null)
			{
				final XGettingTable<String, Object> effectiveRoots = this.rootResolver.resolveDefinedRootInstances();
				this.resolvedEntries = EqConstHashTable.New(effectiveRoots);
				this.hasChanged = false;
			}
			
			/*
			 * Internal collection is Intentionally publicly available
			 * as this implementation is actually just a typed wrapper for it.
			 * The instance is imutable, so there can be no harm done
			 */
			return this.resolvedEntries;
		}
		

		@Override
		public final synchronized void reinitializeEntries(final XGettingTable<String, Object> newEntries)
		{
			// having to replace/update the entries is a change as well.
			this.resolvedEntries = EqConstHashTable.New(newEntries);
		}
		
		/**
		 * Used for example during roots synchronization when initializing an embedded storage instance.
		 * 
		 * @param newEntries the actual entries to be set.
		 */
		@Override
		public final synchronized void updateEntries(final XGettingTable<String, Object> newEntries)
		{
			this.reinitializeEntries(newEntries);
			this.hasChanged = true;
		}

		public final synchronized void loadingUpdateEntries(final XGettingTable<String, Object> resolvedRoots)
		{
			final XGettingTable<String, Object> effectiveRoots = CQL
				.from(resolvedRoots)
				.select(kv -> kv.value() != null)
				.executeInto(EqHashTable.New())
			;
			
			// if at least one null entry was removed, the roots at runtime changed compared to the persistant state
			this.resolvedEntries = EqConstHashTable.New(effectiveRoots);
			this.hasChanged      = effectiveRoots.size() != resolvedRoots.size();
		}
		
		///////////////////////////////////////////////////////////////////////////
		// Hooks for TypeHandler //
		////////////
		
		public final PersistenceRootResolver $rootResolver()
		{
			return this.rootResolver;
		}
		
		public final EqHashTable<String, Long> $rootIdMapping()
		{
			return this.rootIdMapping;
		}
		
		public final void $discardRootIdMapping()
		{
			this.rootIdMapping = null;
		}

	}

}

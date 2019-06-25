package one.microstream.persistence.types;

import static one.microstream.X.notNull;

import one.microstream.collections.EqConstHashTable;
import one.microstream.collections.EqHashTable;
import one.microstream.collections.types.XGettingTable;
import one.microstream.reference.Reference;
import one.microstream.util.cql.CQL;


public interface PersistenceRoots
{
	public String defaultRootIdentifier();
	
	public Reference<Object> defaultRoot();
	                                       
	public String customRootIdentifier();
	
	public Object customRoot();
	
	public XGettingTable<String, Object> entries();
	
	public boolean hasChanged();
	
	public void replaceEntries(XGettingTable<String, Object> newEntries);
	

	
	public static PersistenceRoots New(final PersistenceRootResolver rootResolver)
	{
		return PersistenceRoots.Default.New(rootResolver);
	}

	public final class Default implements PersistenceRoots
	{
		///////////////////////////////////////////////////////////////////////////
		// static methods //
		///////////////////
		
		public static PersistenceRoots.Default New(final PersistenceRootResolver rootResolver)
		{
			// theoretically, it is correct to have neither explicit root but only implicit ones via constants.
			return new PersistenceRoots.Default(
				notNull(rootResolver)               ,
				rootResolver.defaultRootIdentifier(),
				rootResolver.defaultRoot()          ,
				rootResolver.customRootIdentifier() ,
				null                                ,
				false
			);
		}
		
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		/*
		 * The transient actually doesn't matter at all since a custom TypeHandler is used.
		 * Its only pupose here is to indicate that the fields are not directly persisted.
		 */

		final transient PersistenceRootResolver          rootResolver         ;
		final transient String                           defaultRootIdentifier;
		final transient Reference<Object>                defaultRoot          ;
		final transient String                           customRootIdentifier ;
		      transient EqConstHashTable<String, Object> resolvedEntries      ;
		      transient boolean                          hasChanged           ;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		Default(
			final PersistenceRootResolver          rootResolver         ,
			final String                           defaultRootIdentifier,
			final Reference<Object>                defaultRoot          ,
			final String                           customRootIdentifier ,
			final EqConstHashTable<String, Object> resolvedEntries      ,
			final boolean                          hasChanged
		)
		{
			super();
			this.rootResolver          = rootResolver         ;
			this.defaultRootIdentifier = defaultRootIdentifier;
			this.defaultRoot           = defaultRoot          ;
			this.customRootIdentifier    = customRootIdentifier   ;
			this.resolvedEntries       = resolvedEntries      ;
			this.hasChanged            = hasChanged           ;
		}

		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		public final String defaultRootIdentifier()
		{
			return this.defaultRootIdentifier;
		}

		@Override
		public final Reference<Object> defaultRoot()
		{
			return this.defaultRoot;
		}

		@Override
		public final String customRootIdentifier()
		{
			return this.customRootIdentifier;
		}

		@Override
		public final Object customRoot()
		{
			return this.entries().get(this.customRootIdentifier);
		}
		
		@Override
		public final synchronized boolean hasChanged()
		{
			return this.hasChanged;
		}

		@Override
		public final synchronized XGettingTable<String, Object> entries()
		{
			if(this.resolvedEntries == null)
			{
				this.resolveRoots();
			}
			
			/*
			 * Internal collection is Intentionally publicly available
			 * as this implementation is actually just a typed wrapper for it.
			 * The instance is imutable, so there can be no harm done
			 */
			return this.resolvedEntries;
		}
		
		private void resolveRoots()
		{
			final XGettingTable<String, Object> effectiveRoots = this.rootResolver.resolveRootInstances();
			
			this.resolvedEntries = EqConstHashTable.New(effectiveRoots);
			this.hasChanged = false;
		}
		
		
		/**
		 * Used for example by a type handler to set the actual state of an uninitialized instance.
		 * 
		 * @param identifiers the root identifiers to be resolved.
		 * 
		 * @return an array containing the actual root instances in the order of the passed entries,
		 *         including {@literal nulls}.
		 */
		public final synchronized void updateEntries(final XGettingTable<String, Object> resolvedRoots)
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
		
		/**
		 * Used for example during roots synchronization when initializing an embedded storage instance.
		 * 
		 * @param newEntries the actual entries to be set.
		 */
		@Override
		public final synchronized void replaceEntries(final XGettingTable<String, Object> newEntries)
		{
			// having to replace/update the entries is a change as well.
			this.resolvedEntries = EqConstHashTable.New(newEntries);
			this.hasChanged      = true;
		}

	}

}

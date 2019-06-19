package one.microstream.persistence.types;

import static one.microstream.X.coalesce;
import static one.microstream.X.notNull;

import one.microstream.X;
import one.microstream.collections.EqConstHashTable;
import one.microstream.collections.EqHashTable;
import one.microstream.collections.types.XGettingTable;
import one.microstream.reference.Reference;
import one.microstream.typing.KeyValue;


public interface PersistenceRoots
{
	public String defaultRootIdentifier();
	
	public Reference<Object> defaultRoot();
	                                       
	public String customRootIdentifier();
	
	public Object customRoot();
	
	public XGettingTable<String, Object> entries();
	
	public boolean hasChanged();
	
	public void replaceEntries(XGettingTable<String, Object> newEntries);
	

	
	public static PersistenceRoots New(
		final String                                      defaultRootIdentifier,
		final Reference<Object>                           defaultRoot          ,
		final String                                      customRootIdentifier ,
		final XGettingTable<String, PersistenceRootEntry> resolvableEntries
	)
	{
		// (19.06.2019 TM)TODO: proper exception
		notNull(coalesce(defaultRootIdentifier, customRootIdentifier));
		
		return new PersistenceRoots.Default(
			defaultRootIdentifier             ,
			defaultRoot                       ,
			customRootIdentifier              ,
			EqHashTable.New(resolvableEntries),
			null,
			false
		);
	}

	public final class Default implements PersistenceRoots
	{
		///////////////////////////////////////////////////////////////////////////
		// static methods //
		///////////////////
		
		public static PersistenceRoots.Default createUninitialized()
		{
			return new PersistenceRoots.Default(
				null,
				null,
				null,
				X.emptyTable(),
				null,
				false
			);
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		private final String            defaultRootIdentifier;
		private final Reference<Object> defaultRoot          ;
		private final String            customRootIdentifier ;
		

		private XGettingTable<String, PersistenceRootEntry> resolvableEntries;
		private EqConstHashTable<String, Object>            resolvedEntries  ;
		
		transient boolean hasChanged;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		Default(
			final String                                      defaultRootIdentifier,
			final Reference<Object>                           defaultRoot          ,
			final String                                      customRootIdentifier ,
			final XGettingTable<String, PersistenceRootEntry> resolvableEntries    ,
			final EqConstHashTable<String, Object>            resolvedEntries      ,
			final boolean                                     hasChanged
		)
		{
			super();
			this.defaultRootIdentifier = defaultRootIdentifier;
			this.defaultRoot           = defaultRoot          ;
			this.customRootIdentifier  = customRootIdentifier ;
			this.resolvableEntries     = resolvableEntries    ;
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
		
		private synchronized Object[] resolveRoots()
		{
			// internal state helpers
			final XGettingTable<String, PersistenceRootEntry> resolvableEntries = this.resolvableEntries;
			final EqHashTable<String, Object> resolvedEntries = EqHashTable.New();
			boolean hasChanged = false;
			
			// return value helper instance, including nulls to keep indexes consistent with objectIds.
			final Object[] instances = new Object[resolvableEntries.intSize()];
			
			int i = 0;
			for(final KeyValue<String, PersistenceRootEntry> resolvableEntry : resolvableEntries)
			{
				final String               identifier = resolvableEntry.key();
				final PersistenceRootEntry rootEntry  = resolvableEntry.value();
				final Object               instance   = rootEntry.instance(); // supplier may only be called once!
				
				// explicitly removed entry. Also represents a change. Array order must remain consistent to objectIds!
				if((instances[i++] = instance) == null)
				{
					hasChanged = true;
					continue;
				}

				// normal case: unremoved instances must be registered.
				resolvedEntries.put(identifier, instance);
				
				// if at least one entry has changed, the whole roots instance has changed.
				if(!hasChanged && !identifier.equals(rootEntry.identifier()))
				{
					hasChanged = true;
				}
			}
			
			// any change regarding mapping or removing root entries that requires an updating store later on.
			this.hasChanged = hasChanged;
			this.resolvedEntries = resolvedEntries.immure();
			this.resolvableEntries = null;
			
			return instances;
		}
		
		/**
		 * Used for example by a type handler to set the actual state of an uninitialized instance.
		 * 
		 * @param resolvableEntries the resolvable entries to be set.
		 * 
		 * @return an array containing the actual root instances in the order of the passed entries,
		 *         including {@literal nulls}.
		 */
		public final synchronized Object[] setResolvableRoots(
			final XGettingTable<String, PersistenceRootEntry> resolvableEntries
		)
		{
			this.resolvableEntries = resolvableEntries;
			this.resolvedEntries   = null             ;
			this.hasChanged        = false            ;
			
			return this.resolveRoots();
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
			this.resolvableEntries = null;
			this.resolvedEntries   = EqConstHashTable.New(newEntries);
			this.hasChanged        = true;
		}

	}

}

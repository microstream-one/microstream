package net.jadoth.persistence.types;

import net.jadoth.collections.EqHashTable;
import net.jadoth.collections.types.XGettingTable;
import net.jadoth.typing.KeyValue;


public interface PersistenceRoots
{
	public XGettingTable<String, Object> entries();
	
	public boolean hasChanged();
	
	public void updateEntries(final XGettingTable<String, Object> newEntries);



		
	public static PersistenceRoots New(final XGettingTable<String, Object> roots)
	{
		return PersistenceRoots.Implementation.New(roots);
	}

	public final class Implementation implements PersistenceRoots
	{
		///////////////////////////////////////////////////////////////////////////
		// static methods //
		///////////////////
		
		public static PersistenceRoots.Implementation createUninitialized()
		{
			return new PersistenceRoots.Implementation(
				EqHashTable.New()
			);
		}
		
		public static PersistenceRoots.Implementation New(final XGettingTable<String, Object> roots)
		{
			return new PersistenceRoots.Implementation(
				EqHashTable.New(roots)
			);
		}
		
		
		///////////////////////////////////////////////////////////////////////////
		// instance fields  //
		/////////////////////

		private final EqHashTable<String, Object> roots;
		
		transient boolean hasChanged;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		Implementation(final EqHashTable<String, Object> roots)
		{
			super();
			this.roots      = roots;
			this.hasChanged = false;
		}

		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		public final XGettingTable<String, Object> entries()
		{
			/* intentionally make internal collection publicly available
			 * as this implementation is actually just a typed wrapper for it.
			 */
			return this.roots;
		}
		
		public final synchronized Object[] setResolvedRoots(
			final XGettingTable<String, PersistenceRootEntry> resolvedRoots
		)
		{
			// inluding nulls to keep indexes consistent with objectIds.
			final Object[] instances = new Object[resolvedRoots.intSize()];
			
			boolean hasChanged = false;
			
			int i = 0;
			for(final KeyValue<String, PersistenceRootEntry> resolvedRoot : resolvedRoots)
			{
				final String               identifier = resolvedRoot.key();
				final PersistenceRootEntry rootEntry  = resolvedRoot.value();
				final Object               instance   = rootEntry.instance(); // call supplier logic only once.
				
				// explicitely removed entry special case. Also represents a change. Index must be kept consistent!
				if((instances[i++] = instance) == null)
				{
					hasChanged = true;
					continue;
				}

				// normal case: unremoved instances must be registered.
				this.roots.put(identifier, instance);
				
				// if at least one entry has changed, the whole roots instance has changed.
				if(!hasChanged && !identifier.equals(rootEntry.identifier()))
				{
					hasChanged = true;
				}
			}
			
			// any change regarding mapping or removing root entries that requires an updating store later on.
			this.hasChanged = hasChanged;
			
			return instances;
		}
		
		@Override
		public final synchronized boolean hasChanged()
		{
			return this.hasChanged;
		}
		
		@Override
		public final synchronized void updateEntries(final XGettingTable<String, Object> newEntries)
		{
			this.roots.clear();
			this.roots.addAll(newEntries);
			
			// having to replace/update the entries is a change as well.
			this.hasChanged = true;
		}

	}

}

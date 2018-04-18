package net.jadoth.persistence.types;

import net.jadoth.collections.EqHashTable;
import net.jadoth.collections.types.XGettingTable;
import net.jadoth.util.KeyValue;


public interface PersistenceRoots
{
	public XGettingTable<String, Object> entries();
	
	public boolean hasChanged();
	
	public boolean registerRootInstance(String identifier, Object instance);
	
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
		
		@Override
		public final boolean hasChanged()
		{
			return this.hasChanged;
		}
		
		public final Object[] setResolvedRoots(final XGettingTable<String, PersistenceRootEntry> resolvedRoots)
		{
			final Object[] instances = new Object[resolvedRoots.intSize()];
			boolean hasChanged = false;
			
			int i = 0;
			for(final KeyValue<String, PersistenceRootEntry> resolvedRoot : resolvedRoots)
			{
				final String               identifier = resolvedRoot.key();
				final PersistenceRootEntry rootEntry  = resolvedRoot.value();
				instances[i++] = rootEntry.instance(); // inluding nulls to keep indexes consistent with objectIds.
				
				// if at least one entry has changed, the whole roots instance has changed.
				if(!hasChanged && !identifier.equals(rootEntry.identifier()))
				{
					hasChanged = true;
				}
			}
			
			this.hasChanged = hasChanged;
			
			return instances;
		}
		

		@Override
		public final boolean registerRootInstance(final String identifier, final Object instance)
		{
			return this.roots.put(identifier, instance);
		}
		
		@Override
		public final void updateEntries(final XGettingTable<String, Object> newEntries)
		{
			this.roots.clear();
			this.roots.addAll(newEntries);
			this.hasChanged = true;
		}

	}

}

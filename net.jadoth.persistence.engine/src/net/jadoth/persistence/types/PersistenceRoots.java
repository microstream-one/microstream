package net.jadoth.persistence.types;

import net.jadoth.collections.EqHashTable;
import net.jadoth.collections.types.XGettingTable;


public interface PersistenceRoots
{
	public XGettingTable<String, Object> entries();
	
	public boolean hasChanged();
	
	public boolean registerRootInstance(String identifier, Object instance);
	
	public void updateEntries(final XGettingTable<String, Object> newEntries);



	public final class Implementation implements PersistenceRoots
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields  //
		/////////////////////

		private final EqHashTable<String, Object> roots = EqHashTable.New();
		
		transient PersistenceRootResolver.Result[] rootsResolvingResults;
		transient boolean                          hasChanged           ;


		///////////////////////////////////////////////////////////////////////////
		// override methods //
		/////////////////////

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
		
		public final Object[] setResolvedRoots(final PersistenceRootResolver.Result[] rootsResolvingResults)
		{
			final Object[] instances = new Object[rootsResolvingResults.length];
			boolean hasChanged = false;
			
			int i = 0;
			for(final PersistenceRootResolver.Result result : rootsResolvingResults)
			{
				if(result.resolvedIdentifier() != null)
				{
					this.roots.add(
						result.resolvedIdentifier(),
						instances[i++] = result.resolvedRootInstance()
					);
				}
				
				// if at least one entry has changed, the whole roots instance has changed.
				if(result.hasChanged())
				{
					hasChanged = true;
				}
			}
			
			this.rootsResolvingResults = rootsResolvingResults;
			this.hasChanged            = hasChanged           ;
			
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
			
			this.hasChanged            = true;
			this.rootsResolvingResults = null;
		}

	}

}

package net.jadoth.persistence.types;

import net.jadoth.collections.EqHashTable;
import net.jadoth.collections.types.XTable;


public interface PersistenceRoots
{
	public XTable<String, Object> entries();



	public final class Implementation implements PersistenceRoots
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields  //
		/////////////////////

		private final EqHashTable<String, Object> roots = EqHashTable.New();
		transient PersistenceRootResolver.Result[] rootsResolvingResults;



		///////////////////////////////////////////////////////////////////////////
		// override methods //
		/////////////////////

		@Override
		public final XTable<String, Object> entries()
		{
			/* intentionally make internal collection publicly available
			 * as this implementation is actually just a typed wrapper for it.
			 */
			return this.roots;
		}
		
		public final Object[] setResolvedRoots(
			final PersistenceRootResolver.Result[] rootsResolvingResults
		)
		{
			final Object[] instances = new Object[rootsResolvingResults.length];
			
			for(int i = 0; i < rootsResolvingResults.length; i++)
			{
				this.roots.add(
					rootsResolvingResults[i].resolvedIdentifier(),
					instances[i] = rootsResolvingResults[i].resolvedRootInstance()
				);
			}
			
			this.rootsResolvingResults = rootsResolvingResults;
			
			return instances;
		}

	}

}

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

		private final EqHashTable<String, Object> constants = EqHashTable.New();



		///////////////////////////////////////////////////////////////////////////
		// override methods //
		/////////////////////

		@Override
		public final XTable<String, Object> entries()
		{
			/* intentionally make internal collection publicly available
			 * as this implementation is actually just a typed wrapper for it.
			 */
			return this.constants;
		}

	}

}

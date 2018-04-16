package net.jadoth.persistence.types;

import net.jadoth.collections.EqConstHashTable;
import net.jadoth.collections.X;
import net.jadoth.collections.types.XGettingTable;

/**
 * A mapping that projects outdated identifiers (usually className#fieldName, but in case of root instances
 * also potentially arbitrary strings) to current identifiers.
 * 
 * @author TM
 *
 */
public interface PersistenceRefactoringMapping
{
	public XGettingTable<String, String> entries();
	
	
	
	public static PersistenceRefactoringMapping New(final XGettingTable<String, String> entries)
	{
		return new Implementation(
			EqConstHashTable.New(entries)
		);
	}
	
	public static PersistenceRefactoringMapping New()
	{
		return new Implementation(
			X.emptyTable()
		);
	}
	
	public final class Implementation implements PersistenceRefactoringMapping
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private final XGettingTable<String, String> entries;

		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		Implementation(final XGettingTable<String, String> entries)
		{
			super();
			this.entries = entries;
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		@Override
		public final XGettingTable<String, String> entries()
		{
			return this.entries;
		}
		
	}
	
}

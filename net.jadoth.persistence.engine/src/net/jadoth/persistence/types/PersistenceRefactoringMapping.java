package net.jadoth.persistence.types;

import net.jadoth.X;
import net.jadoth.collections.EqConstHashTable;
import net.jadoth.collections.types.XGettingTable;
import net.jadoth.collections.types.XImmutableTable;
import net.jadoth.typing.KeyValue;

/**
 * A mapping that projects outdated identifiers (usually className#fieldName, but in case of root instances
 * also potentially arbitrary strings) to current identifiers.
 * 
 * @author TM
 *
 */
public interface PersistenceRefactoringMapping
{
	public KeyValue<String, String> lookup(String key);
	

	
	public static PersistenceRefactoringMapping New()
	{
		return new Implementation(
			X.emptyTable()
		);
	}
		
	public static PersistenceRefactoringMapping New(final XGettingTable<String, String> entries)
	{
		return new Implementation(
			EqConstHashTable.New(entries)
		);
	}
	
	public final class Implementation implements PersistenceRefactoringMapping
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private final XImmutableTable<String, String> entries;

		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		Implementation(final XImmutableTable<String, String> entries)
		{
			super();
			this.entries = entries;
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		@Override
		public final KeyValue<String, String> lookup(final String key)
		{
			return this.entries.lookup(key);
		}
		
	}
	
}

package net.jadoth.persistence.types;

import net.jadoth.X;
import net.jadoth.collections.EqConstHashEnum;
import net.jadoth.collections.EqConstHashTable;
import net.jadoth.collections.types.XGettingEnum;
import net.jadoth.collections.types.XGettingTable;
import net.jadoth.collections.types.XImmutableEnum;
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
	
	public boolean isNewElement(String targetKey);
	

	
	public static PersistenceRefactoringMapping New()
	{
		return new Implementation(
			X.emptyTable(),
			X.empty()
		);
	}
		
	public static PersistenceRefactoringMapping New(
		final XGettingTable<String, String> entries    ,
		final XGettingEnum<String>          newElements
	)
	{
		return new Implementation(
			EqConstHashTable.New(entries),
			EqConstHashEnum.New(newElements)
		);
	}
	
	public final class Implementation implements PersistenceRefactoringMapping
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private final XImmutableTable<String, String> entries    ;
		private final XImmutableEnum<String>          newElements;

		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		Implementation(
			final XImmutableTable<String, String> entries    ,
			final XImmutableEnum<String>          newElements
		)
		{
			super();
			this.entries     = entries    ;
			this.newElements = newElements;
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		@Override
		public final KeyValue<String, String> lookup(final String key)
		{
			return this.entries.lookup(key);
		}
		
		@Override
		public final boolean isNewElement(final String targetKey)
		{
			return this.newElements.contains(targetKey);
		}
		
	}
	
}

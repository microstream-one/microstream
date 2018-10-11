package net.jadoth.persistence.types;

import net.jadoth.X;
import net.jadoth.collections.EqHashEnum;
import net.jadoth.collections.EqHashTable;
import net.jadoth.collections.types.XGettingEnum;
import net.jadoth.collections.types.XGettingSequence;
import net.jadoth.collections.types.XGettingTable;
import net.jadoth.typing.KeyValue;

public interface PersistenceRefactoringMappingProvider
{
	public PersistenceRefactoringMapping provideRefactoringMapping();
	
	public static PersistenceRefactoringMappingProvider NewEmpty()
	{
		return new PersistenceRefactoringMappingProvider.Implementation(
			X.emptyTable(),
			X.empty()
		);
	}
	
	public static PersistenceRefactoringMappingProvider New(
		final XGettingSequence<KeyValue<String, String>> entries
	)
	{
		final EqHashTable<String, String> table       = EqHashTable.New();
		final EqHashEnum<String>          newElements = EqHashEnum .New();
		
		for(final KeyValue<String, String> entry : entries)
		{
			if(entry.key() == null)
			{
				newElements.add(entry.value());
			}
			else
			{
				table.add(entry);
			}
		}
		
		return new PersistenceRefactoringMappingProvider.Implementation(table, newElements);
	}
	
	public final class Implementation implements PersistenceRefactoringMappingProvider
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private final XGettingTable<String, String> entries    ;
		private final XGettingEnum<String>          newElements;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		Implementation(
			final XGettingTable<String, String> entries    ,
			final XGettingEnum<String>          newElements
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
		public PersistenceRefactoringMapping provideRefactoringMapping()
		{
			// nifty: immure at creation time, not before.
			return new PersistenceRefactoringMapping.Implementation(
				this.entries.immure()    ,
				this.newElements.immure()
			);
		}
		
	}
	
}

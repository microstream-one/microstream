package net.jadoth.persistence.types;

import net.jadoth.collections.types.XGettingTable;

public interface PersistenceRefactoringMappingProvider
{
	public PersistenceRefactoringMapping provideRefactoringMapping();
	
	public static PersistenceRefactoringMappingProvider New(final XGettingTable<String, String> entries)
	{
		return new PersistenceRefactoringMappingProvider.Implementation(
			entries
		);
	}
	
	public final class Implementation implements PersistenceRefactoringMappingProvider
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
		public PersistenceRefactoringMapping provideRefactoringMapping()
		{
			// nifty: immure at creation time, not before.
			return new PersistenceRefactoringMapping.Implementation(
				this.entries.immure()
			);
		}
		
	}
	
}

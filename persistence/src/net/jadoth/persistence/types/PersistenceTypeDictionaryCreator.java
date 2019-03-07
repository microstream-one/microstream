package net.jadoth.persistence.types;

import static net.jadoth.X.notNull;

public interface PersistenceTypeDictionaryCreator
{
	public PersistenceTypeDictionary createTypeDictionary();
	
	
	
	public static PersistenceTypeDictionaryCreator.Implementation New(
		final PersistenceTypeLineageCreator typeLineageCreator
	)
	{
		return new PersistenceTypeDictionaryCreator.Implementation(
			notNull(typeLineageCreator)
		);
	}
	
	public final class Implementation implements PersistenceTypeDictionaryCreator
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private final PersistenceTypeLineageCreator typeLineageCreator;

		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		Implementation(final PersistenceTypeLineageCreator typeLineageCreator)
		{
			super();
			this.typeLineageCreator = typeLineageCreator;
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		public PersistenceTypeDictionary createTypeDictionary()
		{
			return PersistenceTypeDictionary.New(this.typeLineageCreator);
		}
		
	}
	
}

package net.jadoth.persistence.types;

import static net.jadoth.Jadoth.notNull;

public interface PersistenceTypeDictionaryProvider
{
	public PersistenceTypeDictionary provideTypeDictionary();
	
	
	
	public static PersistenceTypeDictionaryProvider.Implementation New(
		final PersistenceTypeLineageCreator typeLineageCreator
	)
	{
		return new PersistenceTypeDictionaryProvider.Implementation(
			notNull(typeLineageCreator)
		);
	}
	
	public final class Implementation implements PersistenceTypeDictionaryProvider
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
		public PersistenceTypeDictionary provideTypeDictionary()
		{
			return PersistenceTypeDictionary.New(this.typeLineageCreator);
		}
		
	}
	
}

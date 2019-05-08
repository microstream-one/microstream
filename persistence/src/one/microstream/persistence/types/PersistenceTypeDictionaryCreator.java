package one.microstream.persistence.types;

import static one.microstream.X.notNull;

public interface PersistenceTypeDictionaryCreator
{
	public PersistenceTypeDictionary createTypeDictionary();
	
	
	
	public static PersistenceTypeDictionaryCreator.Default New(
		final PersistenceTypeLineageCreator typeLineageCreator
	)
	{
		return new PersistenceTypeDictionaryCreator.Default(
			notNull(typeLineageCreator)
		);
	}
	
	public final class Default implements PersistenceTypeDictionaryCreator
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private final PersistenceTypeLineageCreator typeLineageCreator;

		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		Default(final PersistenceTypeLineageCreator typeLineageCreator)
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

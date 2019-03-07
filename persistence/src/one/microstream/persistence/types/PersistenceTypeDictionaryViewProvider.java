package one.microstream.persistence.types;

import static one.microstream.X.notNull;

@FunctionalInterface
public interface PersistenceTypeDictionaryViewProvider extends PersistenceTypeDictionaryProvider
{
	@Override
	public PersistenceTypeDictionaryView provideTypeDictionary();
	
	
	
	public static PersistenceTypeDictionaryViewProvider Wrapper(
		final PersistenceTypeDictionaryView typeDictionary
	)
	{
		return new PersistenceTypeDictionaryViewProvider.Wrapper(
			notNull(typeDictionary)
		);
	}
	
	public final class Wrapper implements PersistenceTypeDictionaryViewProvider
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		
		private final PersistenceTypeDictionaryView typeDictionary;

		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		Wrapper(final PersistenceTypeDictionaryView typeDictionary)
		{
			super();
			this.typeDictionary = typeDictionary;
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		@Override
		public PersistenceTypeDictionaryView provideTypeDictionary()
		{
			return this.typeDictionary;
		}
		
	}
	
}

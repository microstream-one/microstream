package one.microstream.persistence.types;

import static one.microstream.X.notNull;

import one.microstream.collections.types.XGettingSequence;
import one.microstream.persistence.exceptions.PersistenceExceptionParser;

public interface PersistenceTypeDictionaryCompiler
{
	public PersistenceTypeDictionary compileTypeDictionary(String input)
		throws PersistenceExceptionParser
	;
	
	
	
	public static PersistenceTypeDictionaryCompiler.Implementation New(
		final PersistenceTypeDictionaryParser  parser ,
		final PersistenceTypeDictionaryBuilder builder
	)
	{
		return new PersistenceTypeDictionaryCompiler.Implementation(
			notNull(parser) ,
			notNull(builder)
		);
	}
	
	public final class Implementation implements PersistenceTypeDictionaryCompiler
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields  //
		/////////////////////

		private final PersistenceTypeDictionaryParser  parser ;
		private final PersistenceTypeDictionaryBuilder builder;



		///////////////////////////////////////////////////////////////////////////
		// constructors     //
		/////////////////////

		Implementation(
			final PersistenceTypeDictionaryParser  parser ,
			final PersistenceTypeDictionaryBuilder builder
		)
		{
			super();
			this.parser  = parser ;
			this.builder = builder;
		}



		///////////////////////////////////////////////////////////////////////////
		// override methods //
		/////////////////////
		
		@Override
		public PersistenceTypeDictionary compileTypeDictionary(final String input) throws PersistenceExceptionParser
		{
			final XGettingSequence<? extends PersistenceTypeDictionaryEntry> entries =
				this.parser.parseTypeDictionaryEntries(input)
			;
			final PersistenceTypeDictionary typeDictionary =
				this.builder.buildTypeDictionary(entries)
			;
			
			return typeDictionary;
		}

	}
	
}

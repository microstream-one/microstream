package one.microstream.persistence.types;

import static one.microstream.X.notNull;

import one.microstream.chars.VarString;

public interface PersistenceTypeDictionaryExporter
{
	public void exportTypeDictionary(PersistenceTypeDictionary typeDictionary);

	
	public static PersistenceTypeDictionaryExporter New(
		final PersistenceTypeDictionaryStorer storer
	)
	{
		return New(
			PersistenceTypeDictionaryAssembler.New(),
			storer
		);
	}
	
	public static PersistenceTypeDictionaryExporter New(
		final PersistenceTypeDictionaryAssembler assembler,
		final PersistenceTypeDictionaryStorer    storer
	)
	{
		return new PersistenceTypeDictionaryExporter.Default(
			notNull(assembler),
			notNull(storer)
		);
	}


	public final class Default implements PersistenceTypeDictionaryExporter
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		private final PersistenceTypeDictionaryAssembler assembler;
		private final PersistenceTypeDictionaryStorer    storer   ;



		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		Default(
			final PersistenceTypeDictionaryAssembler assembler,
			final PersistenceTypeDictionaryStorer    storer
		)
		{
			super();
			this.assembler = assembler;
			this.storer    = storer   ;
		}



		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		public final void exportTypeDictionary(final PersistenceTypeDictionary typeDictionary)
		{
			final String typeDictionaryString = this.assembler.assemble(
				VarString.New(),
				typeDictionary
			).toString();
			
			this.storer.storeTypeDictionary(typeDictionaryString);
		}

	}

}

package one.microstream.persistence.types;

import static one.microstream.X.notNull;
import static one.microstream.chars.VarString.New;

public interface PersistenceTypeDictionaryExporter
{
	public void exportTypeDictionary(PersistenceTypeDictionary typeDictionary);



	public final class Implementation implements PersistenceTypeDictionaryExporter
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields  //
		/////////////////////

		private final PersistenceTypeDictionaryAssembler assembler;
		private final PersistenceTypeDictionaryStorer    storer   ;



		///////////////////////////////////////////////////////////////////////////
		// constructors     //
		/////////////////////

		public Implementation(
			final PersistenceTypeDictionaryAssembler assembler,
			final PersistenceTypeDictionaryStorer    storer
		)
		{
			super();
			this.assembler = notNull(assembler);
			this.storer = notNull(storer);
		}



		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		public final void exportTypeDictionary(final PersistenceTypeDictionary typeDictionary)
		{
			final String typeDictionaryString = this.assembler.assemble(
				New(),
				typeDictionary
			).toString();
			
			this.storer.storeTypeDictionary(typeDictionaryString);
		}

	}

}

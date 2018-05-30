package net.jadoth.persistence.types;

import static net.jadoth.X.notNull;
import static net.jadoth.chars.VarString.New;

import net.jadoth.meta.XDebug;

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
			// (30.05.2018 TM)FIXME: /!\ DEBUG
			XDebug.debugln("Exporting type dictionary with entry count " + typeDictionary.allTypeDefinitions().size());
			
			final String typeDictionaryString = this.assembler.appendTypeDictionary(
				New(),
				typeDictionary
			).toString();
			this.storer.storeTypeDictionary(typeDictionaryString);
		}

	}

}

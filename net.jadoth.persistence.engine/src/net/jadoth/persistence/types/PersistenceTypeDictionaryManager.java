package net.jadoth.persistence.types;

import static net.jadoth.Jadoth.notNull;

import net.jadoth.collections.types.XGettingCollection;

public interface PersistenceTypeDictionaryManager
{
	public PersistenceTypeDictionaryManager addTypeDefinition(PersistenceTypeDefinition<?> typeDefinition);

	public PersistenceTypeDictionaryManager validateTypeDefinitions(PersistenceTypeDictionary typeDictionary);

	public PersistenceTypeDictionaryManager validateTypeDefinitions(XGettingCollection<PersistenceTypeDefinition<?>> typeDefinitions);

	public PersistenceTypeDictionaryManager addTypeDefinitions(PersistenceTypeDictionary typeDictionary);

	public PersistenceTypeDictionaryManager addTypeDefinitions(XGettingCollection<PersistenceTypeDefinition<?>> typeDefinitions);
	
	public PersistenceTypeDictionaryManager exportTypeDictionary();
	
	public PersistenceTypeDictionary typeDictionary();


	
	public static PersistenceTypeDictionaryManager.Implementation New(
		final PersistenceTypeDictionary         cachedTypeDictionary  ,
		final PersistenceTypeDictionaryExporter typeDictionaryExporter
	)
	{
		return new PersistenceTypeDictionaryManager.Implementation(
			notNull(cachedTypeDictionary)  ,
			notNull(typeDictionaryExporter)
		);
	}

	public final class Implementation implements PersistenceTypeDictionaryManager
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields  //
		/////////////////////

		private final     PersistenceTypeDictionary         typeDictionary        ;
		private final     PersistenceTypeDictionaryExporter typeDictionaryExporter;
		private transient boolean                           changed               ;



		///////////////////////////////////////////////////////////////////////////
		// constructors     //
		/////////////////////

		Implementation(
			final PersistenceTypeDictionary         cachedTypeDictionary  ,
			final PersistenceTypeDictionaryExporter typeDictionaryExporter
		)
		{
			super();
			this.typeDictionary         = notNull(cachedTypeDictionary  );
			this.typeDictionaryExporter = notNull(typeDictionaryExporter);
		}



		///////////////////////////////////////////////////////////////////////////
		// declared methods //
		/////////////////////

		private boolean hasChanged()
		{
			return this.changed;
		}

		private void markChanged()
		{
			this.changed = true;
		}

		private void resetChangeMark()
		{
			this.changed = false;
		}

		@Override
		public final PersistenceTypeDictionary typeDictionary()
		{
			return this.typeDictionary;
		}

		final void internalValidateTypeDefinition(final PersistenceTypeDefinition<?> td)
		{
			final PersistenceTypeDictionary    dictionary    = this.typeDictionary();
			final PersistenceTypeDefinition<?> currentByTid  = dictionary.lookupTypeById  (td.typeId()  );
			final PersistenceTypeDefinition<?> currentByName = dictionary.lookupTypeByName(td.typeName());

			if(currentByTid != currentByName)
			{
				throw new RuntimeException("Invalid type description: " + td); // (05.04.2013 TM)EXCP: proper exception
			}
			
			// (31.07.2014 TM)NOTE: existing descriptions may not be altered, consistency must be preserved
			// (31.07.2014 TM)TODO: maybe modularize logic to make existing type descriptions alterable
			if(currentByTid != null && !PersistenceTypeDescriptionMember.equalMembers(currentByTid.members(), td.members()))
			{
				// (31.07.2014 TM)EXCP: proper exception
				throw new RuntimeException("Type Description mismatch: " + td);
			}
		}

		public final PersistenceTypeDictionaryManager.Implementation updateExport()
		{
			if(this.hasChanged())
			{
				this.exportTypeDictionary();
				this.resetChangeMark();
			}
			return this;
		}



		///////////////////////////////////////////////////////////////////////////
		// override methods //
		/////////////////////

		@Override
		public final PersistenceTypeDictionaryManager.Implementation addTypeDefinition(
			final PersistenceTypeDefinition<?> typeDefinition
		)
		{
			this.internalValidateTypeDefinition(typeDefinition);
			if(this.typeDictionary().registerType(typeDefinition))
			{
				this.markChanged();
			}
			
			// (11.10.2017 TM)FIXME: /!\ unnecessary TypeDictionary writes
			this.updateExport();
			return this;
		}

		@Override
		public final PersistenceTypeDictionaryManager validateTypeDefinitions(
			final PersistenceTypeDictionary typeDictionary
		)
		{
			if(this.typeDictionary() != typeDictionary)
			{
				this.validateTypeDefinitions(typeDictionary.allTypes().values());
			}
			return this;
		}

		@Override
		public final PersistenceTypeDictionaryManager.Implementation validateTypeDefinitions(
			final XGettingCollection<PersistenceTypeDefinition<?>> typeDefinitions
		)
		{
			typeDefinitions.iterate(this::internalValidateTypeDefinition);
			return this;
		}

		@Override
		public final PersistenceTypeDictionaryManager addTypeDefinitions(
			final PersistenceTypeDictionary typeDictionary
		)
		{
			if(this.typeDictionary() != typeDictionary)
			{
				this.addTypeDefinitions(typeDictionary.allTypes().values());
			}
			return this;
		}

		@Override
		public final PersistenceTypeDictionaryManager.Implementation addTypeDefinitions(
			final XGettingCollection<PersistenceTypeDefinition<?>> typeDefinitions
		)
		{
			typeDefinitions.iterate(this::internalValidateTypeDefinition);
			if(this.typeDictionary().registerTypes(typeDefinitions))
			{
				this.markChanged();
			}
			this.updateExport();
			return this;
		}

		@Override
		public final PersistenceTypeDictionaryManager.Implementation exportTypeDictionary()
		{
			this.typeDictionaryExporter.exportTypeDictionary(this.typeDictionary());
			return this;
		}

	}

}

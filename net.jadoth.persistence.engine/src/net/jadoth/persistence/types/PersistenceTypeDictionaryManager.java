package net.jadoth.persistence.types;

import static net.jadoth.X.notNull;

import net.jadoth.collections.types.XGettingCollection;

public interface PersistenceTypeDictionaryManager extends PersistenceTypeDictionaryProvider
{
	public PersistenceTypeDictionaryManager addTypeDescription(PersistenceTypeDescription<?> typeDescription);

	public PersistenceTypeDictionaryManager validateTypeDescriptions(PersistenceTypeDictionary typeDictionary);

	public PersistenceTypeDictionaryManager validateTypeDescriptions(XGettingCollection<PersistenceTypeDescription<?>> typeDescriptions);

	public PersistenceTypeDictionaryManager addTypeDescriptions(PersistenceTypeDictionary typeDictionary);

	public PersistenceTypeDictionaryManager addTypeDescriptions(XGettingCollection<PersistenceTypeDescription<?>> typeDescriptions);

	public PersistenceTypeDictionaryManager exportTypeDictionary();



	public final class Implementation implements PersistenceTypeDictionaryManager
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields  //
		/////////////////////

		private final PersistenceTypeDictionaryProvider typeDictionaryProvider;
		private final PersistenceTypeDictionaryExporter typeDictionaryExporter;

		private transient PersistenceTypeDictionary cachedTypeDictionary;

		private transient boolean changed;



		///////////////////////////////////////////////////////////////////////////
		// constructors     //
		/////////////////////

		public Implementation(
			final PersistenceTypeDictionaryProvider typeDictionaryProvider,
			final PersistenceTypeDictionaryExporter typeDictionaryExporter
		)
		{
			super();
			this.typeDictionaryProvider = notNull(typeDictionaryProvider);
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

		private PersistenceTypeDictionary cachedTypeDictionary()
		{
			if(this.cachedTypeDictionary == null)
			{
				this.cachedTypeDictionary = this.typeDictionaryProvider.provideDictionary();
				this.markChanged();
			}
			return this.cachedTypeDictionary;
		}

		final void internalValidateTypeDescription(final PersistenceTypeDescription<?> td)
		{
			final PersistenceTypeDictionary     dictionary    = this.cachedTypeDictionary();
			final PersistenceTypeDescription<?> currentByTid  = dictionary.lookupTypeById  (td.typeId()  );
			final PersistenceTypeDescription<?> currentByName = dictionary.lookupTypeByName(td.typeName());

			if(currentByTid != currentByName)
			{
				throw new RuntimeException("Invalid type description: " + td); // (05.04.2013 TM)EXCP: proper exception
			}
			// (31.07.2014 TM)NOTE: existing descriptions may not be altered, consistency must be preserved
			// (31.07.2014 TM)TODO: maybe modularize logic to make existing type descriptions alterable
			if(currentByTid != null && !PersistenceTypeDescription.equalDescription(currentByTid, td))
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
		// methods //
		////////////

		@Override
		public final PersistenceTypeDictionary provideDictionary()
		{
			return this.cachedTypeDictionary();
		}

		@Override
		public final PersistenceTypeDictionaryManager.Implementation addTypeDescription(
			final PersistenceTypeDescription<?> typeDescription
		)
		{
			this.internalValidateTypeDescription(typeDescription);
			if(this.cachedTypeDictionary().registerType(typeDescription))
			{
				this.markChanged();
			}
			this.updateExport();
			return this;
		}

		@Override
		public final PersistenceTypeDictionaryManager validateTypeDescriptions(
			final PersistenceTypeDictionary typeDictionary
		)
		{
			if(this.cachedTypeDictionary() != typeDictionary)
			{
				this.validateTypeDescriptions(typeDictionary.types());
			}
			return this;
		}

		@Override
		public final PersistenceTypeDictionaryManager.Implementation validateTypeDescriptions(
			final XGettingCollection<PersistenceTypeDescription<?>> typeDescriptions
		)
		{
			typeDescriptions.iterate(this::internalValidateTypeDescription);
			return this;
		}

		@Override
		public final PersistenceTypeDictionaryManager addTypeDescriptions(
			final PersistenceTypeDictionary typeDictionary
		)
		{
			if(this.cachedTypeDictionary() != typeDictionary)
			{
				this.addTypeDescriptions(typeDictionary.types());
			}
			return this;
		}

		@Override
		public final PersistenceTypeDictionaryManager.Implementation addTypeDescriptions(
			final XGettingCollection<PersistenceTypeDescription<?>> typeDescriptions
		)
		{
			typeDescriptions.iterate(this::internalValidateTypeDescription);
			if(this.cachedTypeDictionary().registerTypes(typeDescriptions))
			{
				this.markChanged();
			}
			this.updateExport();
			return this;
		}

		@Override
		public final PersistenceTypeDictionaryManager.Implementation exportTypeDictionary()
		{
			this.typeDictionaryExporter.exportTypeDictionary(this.cachedTypeDictionary());
			return this;
		}

	}

}

package net.jadoth.persistence.types;

import static net.jadoth.X.notNull;

public interface PersistenceTypeDictionaryManager extends PersistenceTypeDictionaryProvider
{
	public PersistenceTypeDictionaryManager validateTypeDefinition(PersistenceTypeDefinition<?> typeDefinition);
	
	public PersistenceTypeDictionaryManager validateTypeDefinitions(
		Iterable<? extends PersistenceTypeDefinition<?>> typeDefinitions
	);

	public boolean registerTypeDefinition(PersistenceTypeDefinition<?> typeDefinition);

	public boolean registerTypeDefinitions(Iterable<? extends PersistenceTypeDefinition<?>> typeDefinitions);

	public boolean registerRuntimeTypeDefinition(PersistenceTypeDefinition<?> typeDefinition);

	public boolean registerRuntimeTypeDefinitions(Iterable<? extends PersistenceTypeDefinition<?>> typeDefinitions);
		
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
				this.cachedTypeDictionary = this.typeDictionaryProvider.provideTypeDictionary();
				this.markChanged();
			}
			return this.cachedTypeDictionary;
		}

		@Override
		public synchronized PersistenceTypeDictionaryManager validateTypeDefinition(
			final PersistenceTypeDefinition<?> typeDefinition
		)
		{
			final PersistenceTypeDictionary dictionary = this.cachedTypeDictionary();
			
			// Only the TypeId is the unique identifier. The type name only identifies the TypeLineage.
			final PersistenceTypeDefinition<?> registered = dictionary.lookupTypeById(typeDefinition.typeId());

			// Any type definition (e.g. a custom TypeHandler) must match the definition in the dictionary.
			if(registered != null && !PersistenceTypeDescription.equalDescription(registered, typeDefinition))
			{
				// (31.07.2014 TM)EXCP: proper exception
				throw new RuntimeException("Type Definition mismatch: " + typeDefinition);
			}
			
			return this;
		}

		public final PersistenceTypeDictionaryManager.Implementation synchUpdateExport()
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
		public final synchronized PersistenceTypeDictionary provideTypeDictionary()
		{
			return this.cachedTypeDictionary();
		}
		
		@Override
		public final synchronized boolean registerTypeDefinition(final PersistenceTypeDefinition<?> typeDefinition)
		{
			this.validateTypeDefinition(typeDefinition);
			final boolean hasChanged = this.cachedTypeDictionary().registerTypeDefinition(typeDefinition);
			if(hasChanged)
			{
				this.markChanged();
				this.synchUpdateExport();
			}
			
			return hasChanged;
		}

		@Override
		public synchronized boolean registerTypeDefinitions(
			final Iterable<? extends PersistenceTypeDefinition<?>> typeDefinitions
		)
		{
			this.validateTypeDefinitions(typeDefinitions);
			final boolean hasChanged = this.cachedTypeDictionary().registerTypeDefinitions(typeDefinitions);
			if(hasChanged)
			{
				this.markChanged();
				this.synchUpdateExport();
			}
			
			return hasChanged;
		}

		@Override
		public synchronized boolean registerRuntimeTypeDefinition(
			final PersistenceTypeDefinition<?> typeDefinition
		)
		{
			this.validateTypeDefinition(typeDefinition);
			final boolean hasChanged = this.cachedTypeDictionary().registerRuntimeTypeDefinition(typeDefinition);
			if(hasChanged)
			{
				this.markChanged();
				this.synchUpdateExport();
			}
			
			return hasChanged;
		}
		

		@Override
		public final synchronized boolean registerRuntimeTypeDefinitions(
			final Iterable<? extends PersistenceTypeDefinition<?>> typeDefinitions
		)
		{
			this.validateTypeDefinitions(typeDefinitions);
			final boolean hasChanged = this.cachedTypeDictionary().registerRuntimeTypeDefinitions(typeDefinitions);
			if(hasChanged)
			{
				this.markChanged();
				this.synchUpdateExport();
			}
			
			return hasChanged;
		}

		@Override
		public final synchronized PersistenceTypeDictionaryManager.Implementation validateTypeDefinitions(
			final Iterable<? extends PersistenceTypeDefinition<?>> typeDefinitions
		)
		{
			typeDefinitions.forEach(this::validateTypeDefinition);
			return this;
		}

		@Override
		public final synchronized PersistenceTypeDictionaryManager.Implementation exportTypeDictionary()
		{
			this.typeDictionaryExporter.exportTypeDictionary(this.cachedTypeDictionary());
			return this;
		}

	}

}

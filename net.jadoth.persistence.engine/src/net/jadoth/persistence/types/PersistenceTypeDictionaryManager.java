package net.jadoth.persistence.types;

import static net.jadoth.X.notNull;

import net.jadoth.X;

public interface PersistenceTypeDictionaryManager extends PersistenceTypeDictionaryProvider
{
	public PersistenceTypeDictionaryManager validateTypeDefinition(PersistenceTypeDefinition typeDefinition);
	
	public PersistenceTypeDictionaryManager validateTypeDefinitions(
		Iterable<? extends PersistenceTypeDefinition> typeDefinitions
	);

	public boolean registerTypeDefinition(PersistenceTypeDefinition typeDefinition);

	public boolean registerTypeDefinitions(Iterable<? extends PersistenceTypeDefinition> typeDefinitions);

	public boolean registerRuntimeTypeDefinition(PersistenceTypeDefinition typeDefinition);

	public boolean registerRuntimeTypeDefinitions(Iterable<? extends PersistenceTypeDefinition> typeDefinitions);
		
	public PersistenceTypeDictionaryManager exportTypeDictionary();
	
	
	public static void validateTypeDefinition(
		final PersistenceTypeDictionary dictionary    ,
		final PersistenceTypeDefinition typeDefinition
	)
	{
		PersistenceTypeDictionary.validateTypeId(typeDefinition);
		
		// Only the TypeId is the unique identifier. The type name only identifies the TypeLineage.
		final PersistenceTypeDefinition registered = dictionary.lookupTypeById(typeDefinition.typeId());

		// Any type definition (e.g. a custom TypeHandler) must match the definition in the dictionary.
		if(registered != null && !PersistenceTypeDescription.equalDescription(registered, typeDefinition))
		{
			// (31.07.2014 TM)EXCP: proper exception
			throw new RuntimeException("Type Definition mismatch: " + typeDefinition);
		}
	}


	
	
	public static PersistenceTypeDictionaryManager New(
		final PersistenceTypeDictionaryProvider typeDictionaryProvider,
		final PersistenceTypeDictionaryExporter typeDictionaryExporter
	)
	{
		return new PersistenceTypeDictionaryManager.Implementation(
			notNull(typeDictionaryProvider),
			notNull(typeDictionaryExporter)
		);
	}

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

		Implementation(
			final PersistenceTypeDictionaryProvider typeDictionaryProvider,
			final PersistenceTypeDictionaryExporter typeDictionaryExporter
		)
		{
			super();
			this.typeDictionaryProvider = typeDictionaryProvider;
			this.typeDictionaryExporter = typeDictionaryExporter;
		}

		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

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

		private PersistenceTypeDictionary ensureTypeDictionary()
		{
			if(this.cachedTypeDictionary == null)
			{
				synchronized(this)
				{
					// recheck after synch
					if(this.cachedTypeDictionary == null)
					{
						this.cachedTypeDictionary = this.typeDictionaryProvider.provideTypeDictionary();
						this.markChanged();
					}
				}
			}
			
			return this.cachedTypeDictionary;
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

		@Override
		public final synchronized PersistenceTypeDictionary provideTypeDictionary()
		{
			return this.ensureTypeDictionary();
		}
		
		@Override
		public final synchronized boolean registerTypeDefinition(final PersistenceTypeDefinition typeDefinition)
		{
			this.validateTypeDefinition(typeDefinition);
			final boolean hasChanged = this.ensureTypeDictionary().registerTypeDefinition(typeDefinition);
			if(hasChanged)
			{
				this.markChanged();
				this.synchUpdateExport();
			}
			
			return hasChanged;
		}

		@Override
		public synchronized boolean registerTypeDefinitions(
			final Iterable<? extends PersistenceTypeDefinition> typeDefinitions
		)
		{
			this.validateTypeDefinitions(typeDefinitions);
			final boolean hasChanged = this.ensureTypeDictionary().registerTypeDefinitions(typeDefinitions);
			if(hasChanged)
			{
				this.markChanged();
				this.synchUpdateExport();
			}
			
			return hasChanged;
		}

		@Override
		public synchronized boolean registerRuntimeTypeDefinition(
			final PersistenceTypeDefinition typeDefinition
		)
		{
			this.validateTypeDefinition(typeDefinition);
			final boolean hasChanged = this.ensureTypeDictionary().registerRuntimeTypeDefinition(typeDefinition);
			if(hasChanged)
			{
				this.markChanged();
				this.synchUpdateExport();
			}
			
			return hasChanged;
		}
		

		@Override
		public final synchronized boolean registerRuntimeTypeDefinitions(
			final Iterable<? extends PersistenceTypeDefinition> typeDefinitions
		)
		{
			this.validateTypeDefinitions(typeDefinitions);
			final boolean hasChanged = this.ensureTypeDictionary().registerRuntimeTypeDefinitions(typeDefinitions);
			if(hasChanged)
			{
				this.markChanged();
				this.synchUpdateExport();
			}
			
			return hasChanged;
		}

		@Override
		public synchronized PersistenceTypeDictionaryManager validateTypeDefinition(
			final PersistenceTypeDefinition typeDefinition
		)
		{
			PersistenceTypeDictionaryManager.validateTypeDefinition(
				this.ensureTypeDictionary(),
				typeDefinition
			);
			
			return this;
		}

		@Override
		public final synchronized PersistenceTypeDictionaryManager.Implementation validateTypeDefinitions(
			final Iterable<? extends PersistenceTypeDefinition> typeDefinitions
		)
		{
			final PersistenceTypeDictionary typeDictionary = this.ensureTypeDictionary();
			for(final PersistenceTypeDefinition td : typeDefinitions)
			{
				PersistenceTypeDictionaryManager.validateTypeDefinition(typeDictionary, td);
			}
			
			return this;
		}

		@Override
		public final synchronized PersistenceTypeDictionaryManager.Implementation exportTypeDictionary()
		{
			this.typeDictionaryExporter.exportTypeDictionary(this.ensureTypeDictionary());
			return this;
		}

	}
	
	

	public final class Transient implements PersistenceTypeDictionaryManager
	{
		
	}

	
	public static PersistenceTypeDictionaryManager Immutable(
		final PersistenceTypeDictionaryViewProvider typeDictionaryProvider
	)
	{
		return new PersistenceTypeDictionaryManager.Immutable(
			notNull(typeDictionaryProvider)
		);
	}
	
	public final class Immutable implements PersistenceTypeDictionaryManager
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private final PersistenceTypeDictionaryViewProvider typeDictionaryProvider;
		
		private transient PersistenceTypeDictionaryView cachedTypeDictionary;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		Immutable(final PersistenceTypeDictionaryViewProvider typeDictionaryProvider)
		{
			super();
			this.typeDictionaryProvider = typeDictionaryProvider;
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		private PersistenceTypeDictionaryView ensureTypeDictionary()
		{
			if(this.cachedTypeDictionary == null)
			{
				synchronized(this)
				{
					// recheck after synch
					if(this.cachedTypeDictionary == null)
					{
						this.cachedTypeDictionary = this.typeDictionaryProvider.provideTypeDictionary();
					}
				}
			}
			
			return this.cachedTypeDictionary;
		}

		@Override
		public final PersistenceTypeDictionaryView provideTypeDictionary()
		{
			return this.ensureTypeDictionary();
		}

		@Override
		public PersistenceTypeDictionaryManager validateTypeDefinition(
			final PersistenceTypeDefinition typeDefinition
		)
		{
			PersistenceTypeDictionaryManager.validateTypeDefinition(
				this.ensureTypeDictionary(),
				typeDefinition
			);
			
			return this;
		}

		@Override
		public final PersistenceTypeDictionaryManager validateTypeDefinitions(
			final Iterable<? extends PersistenceTypeDefinition> typeDefinitions
		)
		{
			final PersistenceTypeDictionary typeDictionary = this.ensureTypeDictionary();
			for(final PersistenceTypeDefinition td : typeDefinitions)
			{
				PersistenceTypeDictionaryManager.validateTypeDefinition(typeDictionary, td);
			}
			
			return this;
		}

		@Override
		public final boolean registerTypeDefinition(
			final PersistenceTypeDefinition typeDefinition
		)
		{
			return this.registerTypeDefinitions(X.Constant(typeDefinition));
		}

		@Override
		public final boolean registerTypeDefinitions(
			final Iterable<? extends PersistenceTypeDefinition> typeDefinitions
		)
		{
			final PersistenceTypeDictionaryView typeDictionary = this.ensureTypeDictionary();
			for(final PersistenceTypeDefinition td : typeDefinitions)
			{
				PersistenceTypeDictionary.validateTypeId(td);
				
				// Only the TypeId is the unique identifier. The type name only identifies the TypeLineage.
				final PersistenceTypeDefinition registered = typeDictionary.lookupTypeById(td.typeId());

				// Any type definition (e.g. a custom TypeHandler) must match the definition in the dictionary.
				if(registered == null || !PersistenceTypeDescription.equalDescription(registered, td))
				{
					// (31.07.2014 TM)EXCP: proper exception
					throw new UnsupportedOperationException("Read-only TypeDictionary cannot change.");
				}
			}
			
			// no change required (no exception)
			return false;
		}

		@Override
		public final boolean registerRuntimeTypeDefinition(
			final PersistenceTypeDefinition typeDefinition
		)
		{
			return this.registerRuntimeTypeDefinition(typeDefinition);
		}

		@Override
		public final boolean registerRuntimeTypeDefinitions(
			final Iterable<? extends PersistenceTypeDefinition> typeDefinitions
		)
		{
			return this.registerTypeDefinitions(typeDefinitions);
		}

		@Override
		public final PersistenceTypeDictionaryManager exportTypeDictionary()
		{
			throw new UnsupportedOperationException();
		}
		
	}
	
}

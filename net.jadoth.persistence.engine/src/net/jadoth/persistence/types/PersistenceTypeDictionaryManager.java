package net.jadoth.persistence.types;

import static net.jadoth.X.notNull;

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
			final PersistenceTypeDefinition typeDefinition
		)
		{
			PersistenceTypeDictionary.validateTypeId(typeDefinition);
			
			final PersistenceTypeDictionary dictionary = this.cachedTypeDictionary();
			
			// Only the TypeId is the unique identifier. The type name only identifies the TypeLineage.
			final PersistenceTypeDefinition registered = dictionary.lookupTypeById(typeDefinition.typeId());

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
		public final synchronized boolean registerTypeDefinition(final PersistenceTypeDefinition typeDefinition)
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
			final Iterable<? extends PersistenceTypeDefinition> typeDefinitions
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
			final PersistenceTypeDefinition typeDefinition
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
			final Iterable<? extends PersistenceTypeDefinition> typeDefinitions
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
			final Iterable<? extends PersistenceTypeDefinition> typeDefinitions
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

	
	public final class Immutable implements PersistenceTypeDictionaryManager
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private final PersistenceTypeDictionaryViewProvider typeDictionaryProvider;
		
		private transient PersistenceTypeDictionary cachedTypeDictionary;
		
		
		
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
		
		private PersistenceTypeDictionary ensureTypeDictionary()
		{
			if(this.cachedTypeDictionary == null)
			{
				
			}
		}
		
		private PersistenceTypeDictionary wrapTypeDictionary()



		@Override
		public PersistenceTypeDictionary provideTypeDictionary()
		{
			throw new net.jadoth.meta.NotImplementedYetError(); // FIXME PersistenceTypeDictionaryProvider#provideTypeDictionary()
		}



		@Override
		public PersistenceTypeDictionaryManager validateTypeDefinition(final PersistenceTypeDefinition typeDefinition)
		{
			throw new net.jadoth.meta.NotImplementedYetError(); // FIXME PersistenceTypeDictionaryManager#validateTypeDefinition()
		}



		@Override
		public PersistenceTypeDictionaryManager validateTypeDefinitions(final Iterable<? extends PersistenceTypeDefinition> typeDefinitions)
		{
			throw new net.jadoth.meta.NotImplementedYetError(); // FIXME PersistenceTypeDictionaryManager#validateTypeDefinitions()
		}



		@Override
		public boolean registerTypeDefinition(final PersistenceTypeDefinition typeDefinition)
		{
			throw new net.jadoth.meta.NotImplementedYetError(); // FIXME PersistenceTypeDictionaryManager#registerTypeDefinition()
		}



		@Override
		public boolean registerTypeDefinitions(final Iterable<? extends PersistenceTypeDefinition> typeDefinitions)
		{
			throw new net.jadoth.meta.NotImplementedYetError(); // FIXME PersistenceTypeDictionaryManager#registerTypeDefinitions()
		}



		@Override
		public boolean registerRuntimeTypeDefinition(final PersistenceTypeDefinition typeDefinition)
		{
			throw new net.jadoth.meta.NotImplementedYetError(); // FIXME PersistenceTypeDictionaryManager#registerRuntimeTypeDefinition()
		}



		@Override
		public boolean registerRuntimeTypeDefinitions(final Iterable<? extends PersistenceTypeDefinition> typeDefinitions)
		{
			throw new net.jadoth.meta.NotImplementedYetError(); // FIXME PersistenceTypeDictionaryManager#registerRuntimeTypeDefinitions()
		}



		@Override
		public PersistenceTypeDictionaryManager exportTypeDictionary()
		{
			throw new net.jadoth.meta.NotImplementedYetError(); // FIXME PersistenceTypeDictionaryManager#exportTypeDictionary()
		}
		
		
	}
	
}

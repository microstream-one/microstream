package one.microstream.persistence.types;

/*-
 * #%L
 * microstream-persistence
 * %%
 * Copyright (C) 2019 - 2022 MicroStream Software
 * %%
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 * 
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the Eclipse
 * Public License, v. 2.0 are satisfied: GNU General Public License, version 2
 * with the GNU Classpath Exception which is
 * available at https://www.gnu.org/software/classpath/license.html.
 * 
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 * #L%
 */

import static one.microstream.X.notNull;

import one.microstream.X;
import one.microstream.persistence.exceptions.PersistenceException;

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
			
	
	public static void validateTypeDefinition(
		final PersistenceTypeDictionary dictionary    ,
		final PersistenceTypeDefinition typeDefinition
	)
	{
		PersistenceTypeDictionary.validateTypeId(typeDefinition);
		
		// Only the TypeId is the unique identifier. The type name only identifies the TypeLineage.
		final PersistenceTypeDefinition registered = dictionary.lookupTypeById(typeDefinition.typeId());

		// Any type definition (e.g. a custom TypeHandler) must match the structural description in the dictionary.
		if(registered != null && !PersistenceTypeDescription.equalStructure(registered, typeDefinition))
		{
			throw new PersistenceException("Type Definition mismatch: " + typeDefinition);
		}
	}


	
	
	public static PersistenceTypeDictionaryManager Exporting(
		final PersistenceTypeDictionaryProvider typeDictionaryProvider,
		final PersistenceTypeDictionaryExporter typeDictionaryExporter
	)
	{
		return new PersistenceTypeDictionaryManager.Exporting(
			notNull(typeDictionaryProvider),
			notNull(typeDictionaryExporter)
		);
	}
	
	public abstract class Abstract<D extends PersistenceTypeDictionary>
	implements PersistenceTypeDictionaryManager
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private transient D cachedTypeDictionary;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		protected Abstract()
		{
			super();
		}

		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		protected final D ensureTypeDictionary()
		{
			if(this.cachedTypeDictionary == null)
			{
				synchronized(this)
				{
					// recheck after synch
					if(this.cachedTypeDictionary == null)
					{
						this.cachedTypeDictionary = this.internalProvideTypeDictionary();
					}
				}
			}
			
			return this.cachedTypeDictionary;
		}
		
		protected abstract D internalProvideTypeDictionary();
		

		@Override
		public synchronized PersistenceTypeDictionary provideTypeDictionary()
		{
			return this.ensureTypeDictionary();
		}
		
		@Override
		public synchronized boolean registerTypeDefinition(final PersistenceTypeDefinition typeDefinition)
		{
			this.validateTypeDefinition(typeDefinition);

			return this.ensureTypeDictionary().registerTypeDefinition(typeDefinition);
		}

		@Override
		public synchronized boolean registerTypeDefinitions(
			final Iterable<? extends PersistenceTypeDefinition> typeDefinitions
		)
		{
			this.validateTypeDefinitions(typeDefinitions);
			
			return this.ensureTypeDictionary().registerTypeDefinitions(typeDefinitions);
		}

		@Override
		public synchronized boolean registerRuntimeTypeDefinition(
			final PersistenceTypeDefinition typeDefinition
		)
		{
			this.validateTypeDefinition(typeDefinition);

			return this.ensureTypeDictionary().registerRuntimeTypeDefinition(typeDefinition);
		}
		

		@Override
		public synchronized boolean registerRuntimeTypeDefinitions(
			final Iterable<? extends PersistenceTypeDefinition> typeDefinitions
		)
		{
			this.validateTypeDefinitions(typeDefinitions);
			
			return this.ensureTypeDictionary().registerRuntimeTypeDefinitions(typeDefinitions);
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
		public synchronized PersistenceTypeDictionaryManager validateTypeDefinitions(
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
	}

	
	
	public final class Exporting extends PersistenceTypeDictionaryManager.Abstract<PersistenceTypeDictionary>
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		private final PersistenceTypeDictionaryProvider typeDictionaryProvider;
		private final PersistenceTypeDictionaryExporter typeDictionaryExporter;

		private transient boolean changed;



		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		Exporting(
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
		
		@Override
		protected final PersistenceTypeDictionary internalProvideTypeDictionary()
		{
			final PersistenceTypeDictionary typeDictionary = this.typeDictionaryProvider.provideTypeDictionary();
			this.markChanged();
			
			return typeDictionary;
		}

		public final PersistenceTypeDictionaryManager.Exporting synchUpdateExport()
		{
			if(this.hasChanged())
			{
				this.exportTypeDictionary();
				this.resetChangeMark();
			}
			
			return this;
		}

		public final synchronized PersistenceTypeDictionaryManager.Exporting exportTypeDictionary()
		{
			this.typeDictionaryExporter.exportTypeDictionary(this.ensureTypeDictionary());
			return this;
		}
		
		@Override
		public final synchronized boolean registerTypeDefinition(final PersistenceTypeDefinition typeDefinition)
		{
			final boolean hasChanged = super.registerTypeDefinition(typeDefinition);
			if(hasChanged)
			{
				this.markChanged();
				this.synchUpdateExport();
			}
			
			return hasChanged;
		}

		@Override
		public final synchronized boolean registerTypeDefinitions(
			final Iterable<? extends PersistenceTypeDefinition> typeDefinitions
		)
		{
			final boolean hasChanged = super.registerTypeDefinitions(typeDefinitions);
			if(hasChanged)
			{
				this.markChanged();
				this.synchUpdateExport();
			}
			
			return hasChanged;
		}

		@Override
		public final synchronized boolean registerRuntimeTypeDefinition(
			final PersistenceTypeDefinition typeDefinition
		)
		{
			final boolean hasChanged = super.registerRuntimeTypeDefinition(typeDefinition);
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
			final boolean hasChanged = super.registerRuntimeTypeDefinitions(typeDefinitions);
			if(hasChanged)
			{
				this.markChanged();
				this.synchUpdateExport();
			}
			
			return hasChanged;
		}

	}
	
	
	
	public static PersistenceTypeDictionaryManager Transient(
		final PersistenceTypeDictionaryCreator typeDictionaryCreator
	)
	{
		return new PersistenceTypeDictionaryManager.Transient(
			notNull(typeDictionaryCreator)
		);
	}

	public final class Transient extends PersistenceTypeDictionaryManager.Abstract<PersistenceTypeDictionary>
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private final PersistenceTypeDictionaryCreator typeDictionaryCreator;
				
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		Transient(final PersistenceTypeDictionaryCreator typeDictionaryCreator)
		{
			super();
			this.typeDictionaryCreator = typeDictionaryCreator;
		}

		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		protected PersistenceTypeDictionary internalProvideTypeDictionary()
		{
			return this.typeDictionaryCreator.createTypeDictionary();
		}
		
	}

	
	
	public static PersistenceTypeDictionaryManager Immutable(
		final PersistenceTypeDictionaryViewProvider typeDictionaryProvider
	)
	{
		return new PersistenceTypeDictionaryManager.Immutable(
			notNull(typeDictionaryProvider)
		);
	}
	
	public final class Immutable extends PersistenceTypeDictionaryManager.Abstract<PersistenceTypeDictionaryView>
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private final PersistenceTypeDictionaryViewProvider typeDictionaryProvider;
		
		
		
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
		
		@Override
		protected PersistenceTypeDictionaryView internalProvideTypeDictionary()
		{
			return this.typeDictionaryProvider.provideTypeDictionary();
		}

		@Override
		public final synchronized boolean registerTypeDefinition(
			final PersistenceTypeDefinition typeDefinition
		)
		{
			return this.registerTypeDefinitions(X.Constant(typeDefinition));
		}

		@Override
		public final synchronized boolean registerTypeDefinitions(
			final Iterable<? extends PersistenceTypeDefinition> typeDefinitions
		)
		{
			final PersistenceTypeDictionaryView typeDictionary = this.ensureTypeDictionary();
			for(final PersistenceTypeDefinition td : typeDefinitions)
			{
				PersistenceTypeDictionary.validateTypeId(td);
				
				// Only the TypeId is the unique identifier. The type name only identifies the TypeLineage.
				final PersistenceTypeDefinition registered = typeDictionary.lookupTypeById(td.typeId());

				// Any type definition (e.g. a custom TypeHandler) must match the (exact) description in the dictionary.
				if(registered == null || !PersistenceTypeDescription.equalDescription(registered, td))
				{
					throw new UnsupportedOperationException("Read-only TypeDictionary cannot change.");
				}
			}
			
			// no change required (no exception)
			return false;
		}

		@Override
		public final synchronized boolean registerRuntimeTypeDefinition(
			final PersistenceTypeDefinition typeDefinition
		)
		{
			return this.registerTypeDefinition(typeDefinition);
		}

		@Override
		public final synchronized boolean registerRuntimeTypeDefinitions(
			final Iterable<? extends PersistenceTypeDefinition> typeDefinitions
		)
		{
			return this.registerTypeDefinitions(typeDefinitions);
		}
		
	}
	
}

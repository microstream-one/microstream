package one.microstream.storage.types;

import static one.microstream.X.notNull;

import java.io.File;
import java.util.function.Predicate;

import one.microstream.collections.types.XGettingEnum;
import one.microstream.collections.types.XGettingTable;
import one.microstream.persistence.binary.types.Binary;
import one.microstream.persistence.types.Persistence;
import one.microstream.persistence.types.PersistenceManager;
import one.microstream.persistence.types.PersistenceRoots;
import one.microstream.persistence.types.Storer;
import one.microstream.persistence.types.Unpersistable;
import one.microstream.reference.Reference;
import one.microstream.storage.exceptions.StorageException;

public interface EmbeddedStorageManager extends StorageController, StorageConnection
{
	public StorageTypeDictionary typeDictionary();

	public StorageConnection createConnection();

	public StorageConfiguration configuration();

	public void initialize();

	@Override
	public EmbeddedStorageManager start();

	@Override
	public boolean shutdown();
	
	public Object root();
	
	public Object setRoot(Object newRoot);
	
	public Reference<Object> defaultRoot();
	
	public Object customRoot();
	
	public default long storeRoot()
	{
		// if a default root is present, there cannot be a custom root, so store the default root
		final Reference<Object> defaultRoot = this.defaultRoot();
		if(defaultRoot != null)
		{
			final Storer storer = this.createStorer();
			final long defaultRootObjectId = storer.store(defaultRoot);
			
			final Object root = defaultRoot.get();
			if(root != null)
			{
				storer.store(root);
			}
			
			storer.commit();
			
			return defaultRootObjectId;
		}

		// if a custom root is present, store that
		final Object customRoot = this.customRoot();
		if(customRoot != null)
		{
			return this.store(customRoot);
		}

		return Persistence.nullId();
	}
	
	public default long storeDefaultRoot()
	{
		final Reference<Object> root = this.defaultRoot();

		return root == null
			? Persistence.nullId()
			: this.store(root)
		;
	}

	
	
	public static EmbeddedStorageManager.Default New(
		final StorageConfiguration                   configuration       ,
		final EmbeddedStorageConnectionFoundation<?> connectionFoundation,
		final PersistenceRoots                       definedRoots
	)
	{
		return new EmbeddedStorageManager.Default(
			notNull(configuration)       ,
			notNull(connectionFoundation),
			notNull(definedRoots)
		);
	}


	public final class Default implements EmbeddedStorageManager, Unpersistable
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		private final StorageConfiguration                   configuration       ;
		private final StorageManager                         storageManager      ;
		private final EmbeddedStorageConnectionFoundation<?> connectionFoundation;
		private final PersistenceRoots                       definedRoots        ;
		
		private StorageConnection singletonConnection;



		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		Default(
			final StorageConfiguration                   configuration       ,
			final EmbeddedStorageConnectionFoundation<?> connectionFoundation,
			final PersistenceRoots                       definedRoots
		)
		{
			super();
			this.configuration        = configuration                           ;
			this.storageManager       = connectionFoundation.getStorageManager(); // to ensure consistency
			this.connectionFoundation = connectionFoundation                    ;
			this.definedRoots         = definedRoots                            ;
		}


		private synchronized StorageConnection singletonConnection()
		{
			if(this.singletonConnection == null)
			{
				this.singletonConnection = this.createConnection();
			}
			return this.singletonConnection;
		}



		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		public Object root()
		{
			final Object customRoot = this.customRoot();
			if(customRoot != null)
			{
				return customRoot;
			}
			
			final Reference<Object> defaultRoot = this.defaultRoot();
			if(defaultRoot != null)
			{
				return defaultRoot.get();
			}
			
			return null;
		}
		
		@Override
		public Object setRoot(final Object newRoot)
		{
			final Object customRoot = this.customRoot();
			if(customRoot != null)
			{
				if(customRoot == newRoot)
				{
					// no-op, graciously abort
					return customRoot;
				}
				
				// (25.06.2019 TM)EXCP: proper exception
				throw new StorageException("Cannot replace an explicitely defined root instance.");
			}
			
			final Reference<Object> defaultRoot = this.defaultRoot();
			if(defaultRoot != null)
			{
				defaultRoot.set(newRoot);
				return newRoot;
			}

			// (25.06.2019 TM)EXCP: proper exception
			throw new StorageException("No default root (reference holder) present to reference the passed root.");
		}
		
		@Override
		public Reference<Object> defaultRoot()
		{
			return this.definedRoots.defaultRoot();
		}
		
		@Override
		public Object customRoot()
		{
			return this.definedRoots.customRoot();
		}

		@Override
		public PersistenceManager<Binary> persistenceManager()
		{
			return this.singletonConnection().persistenceManager();
		}

		@Override
		public Storer createStorer()
		{
			return this.singletonConnection().createStorer();
		}

		@Override
		public final EmbeddedStorageManager.Default start()
		{
			this.storageManager.start();

			this.ensureRequiredTypeHandlers();
			this.initialize();
			
			return this;
		}

		private void synchronizeRoots(final PersistenceRoots loadedRoots)
		{
			final XGettingTable<String, Object> loadedEntries  = loadedRoots.entries();
			final XGettingTable<String, Object> definedEntries = this.definedRoots.entries();

			final boolean equalContent = loadedEntries.equalsContent(definedEntries, (e1, e2) ->
				// keys (identifier Strings) must be value-equal, root instance must be the same (identical)
				e1.key().equals(e2.key()) && e1.value() == e2.value()
			);
			
			// if the loaded roots does not match the defined roots, its entries must be updated to catch up.
			if(!equalContent)
			{
				loadedRoots.replaceEntries(definedEntries);
			}
			
			/*
			 * If the loaded roots had to change in any way to match the runtime state of the application,
			 * it means that it has to be stored to update the persistent state to the current (changed) one.
			 * The loaded roots instance is the one that has to be stored to maintain the associated ObjectId,
			 * hence the entry synchronization instead of just storing the defined roots instance right away.
			 * There are 3 possible cases for a change:
			 * 1.) An entry has been explicitly removed by a refactoring mapping.
			 * 2.) An entry has been mapped to a new identifier by a refactoring mapping.
			 * 3.) Loaded roots and defined roots do not match, so the loaded roots entries must be replaced/updated.
			 */
		}
		
		private void ensureRequiredTypeHandlers()
		{
			// make sure a functional type handler is present for every occuring type id or throw an exception.
			final StorageIdAnalysis  idAnalysis      = this.storageManager.initializationIdAnalysis();
			final XGettingEnum<Long> occuringTypeIds = idAnalysis.occuringTypeIds();
			this.connectionFoundation.getTypeHandlerManager().ensureTypeHandlersByTypeIds(occuringTypeIds);
		}
		
		private PersistenceRoots loadExistingRoots(final StorageConnection initConnection)
		{
			return initConnection.persistenceManager().createLoader().loadRoots();
		}
		
		private PersistenceRoots validateEmptyDatabaseAndReturnDefinedRoots(final StorageConnection initConnection)
		{
			// no loaded roots is only valid if there is no data yet at all (no database files / content)
			final StorageRawFileStatistics statistics = initConnection.createStorageStatistics();
			if(statistics.liveDataLength() != 0)
			{
				// (14.09.2015 TM)EXCP: proper exception
				throw new RuntimeException("No roots found for existing data.");
			}

			return this.definedRoots;
		}

		@Override
		public final void initialize()
		{
			final StorageConnection initConnection = this.createConnection();

			PersistenceRoots loadedRoots = this.loadExistingRoots(initConnection);
			if(loadedRoots == null)
			{
				loadedRoots = this.validateEmptyDatabaseAndReturnDefinedRoots(initConnection);
			}
			else
			{
				this.synchronizeRoots(loadedRoots);
			}
			
			// update enum constants root entries in any case (newly created or loaded and synched)
			this.validateAndUpdateEnumConstants(loadedRoots);
			
			if(loadedRoots.hasChanged())
			{
				// a not perfectly synchronous loaded roots instance needs to be stored after it has been synchronized
				initConnection.store(loadedRoots);
			}
		}
		
		private void validateAndUpdateEnumConstants(final PersistenceRoots roots)
		{
			/* (06.08.2019 TM)FIXME: priv#23: check typehandlermanager for pending root storing
			 * - scan type dictionary for all already existing / known enum types
			 * - validate and insert / update their enum constants entry
			 * - any change must always result in a completely new Object[] for the storer to store it
			 * - merge with synchronizeRoots() or modify it to ignore enums?
			 * ? what about legacy enum types? Actually, only a lineage's most current enums may be stored (exist!)
			 * ? what about the pending enums in the TypeHandlerManager from TypeHandler creation?
			 *   does that make the dictionary check unnecessary because there is a handler for every
			 *   dictionary entry, anyway?
			 * - is that a fluid transition to LTM-handling enum constants?
			 */
			throw new one.microstream.meta.NotImplementedYetError();
		}

		@Override
		public final boolean shutdown()
		{
			return this.storageManager.shutdown();
		}

		@Override
		public final boolean isAcceptingTasks()
		{
			return this.storageManager.isAcceptingTasks();
		}

		@Override
		public final boolean isRunning()
		{
			return this.storageManager.isRunning();
		}

		@Override
		public final boolean isStartingUp()
		{
			return this.storageManager.isStartingUp();
		}

		@Override
		public final boolean isShuttingDown()
		{
			return this.storageManager.isShuttingDown();
		}

		@Override
		public final void checkAcceptingTasks()
		{
			this.storageManager.checkAcceptingTasks();
		}

		@Override
		public final StorageConfiguration configuration()
		{
			return this.configuration;
		}

		@Override
		public final StorageTypeDictionary typeDictionary()
		{
			return this.storageManager.typeDictionary();
		}

		@Override
		public final StorageConnection createConnection()
		{
			return this.connectionFoundation.createStorageConnection();
		}
		
		@Override
		public final long initializationTime()
		{
			return this.storageManager.initializationTime();
		}
		
		@Override
		public final long operationModeTime()
		{
			return this.storageManager.operationModeTime();
		}

		@Override
		public final void issueFullGarbageCollection()
		{
			this.singletonConnection().issueFullGarbageCollection();
		}

		@Override
		public final boolean issueGarbageCollection(final long nanoTimeBudget)
		{
			return this.singletonConnection().issueGarbageCollection(nanoTimeBudget);
		}

		@Override
		public final void issueFullFileCheck()
		{
			this.singletonConnection().issueFullFileCheck();
		}

		@Override
		public final void issueFullFileCheck(final StorageDataFileDissolvingEvaluator fileDissolvingEvaluator)
		{
			this.singletonConnection().issueFullFileCheck(fileDissolvingEvaluator);
		}

		@Override
		public boolean issueFileCheck(final long nanoTimeBudgetBound)
		{
			return this.singletonConnection().issueFileCheck(nanoTimeBudgetBound);
		}

		@Override
		public final boolean issueFileCheck(
			final long nanoTimeBudgetBound,
			final StorageDataFileDissolvingEvaluator fileDissolvingEvaluator)
		{
			return this.singletonConnection().issueFileCheck(nanoTimeBudgetBound, fileDissolvingEvaluator);
		}

		@Override
		public final void issueFullCacheCheck()
		{
			this.singletonConnection().issueFullCacheCheck();
		}

		@Override
		public final void issueFullCacheCheck(final StorageEntityCacheEvaluator entityEvaluator)
		{
			this.singletonConnection().issueFullCacheCheck(entityEvaluator);
		}

		@Override
		public final boolean issueCacheCheck(final long nanoTimeBudgetBound)
		{
			return this.singletonConnection().issueCacheCheck(nanoTimeBudgetBound);
		}

		@Override
		public final boolean issueCacheCheck(
			final long                        nanoTimeBudgetBound,
			final StorageEntityCacheEvaluator entityEvaluator
		)
		{
			return this.singletonConnection().issueCacheCheck(nanoTimeBudgetBound, entityEvaluator);
		}

		@Override
		public final StorageRawFileStatistics createStorageStatistics()
		{
			return this.singletonConnection().createStorageStatistics();
		}

		@Override
		public final void exportChannels(
			final StorageIoHandler fileHandler             ,
			final boolean          performGarbageCollection
		)
		{
			this.singletonConnection().exportChannels(fileHandler, performGarbageCollection);
		}

		@Override
		public final StorageEntityTypeExportStatistics exportTypes(
			final StorageEntityTypeExportFileProvider         exportFileProvider,
			final Predicate<? super StorageEntityTypeHandler> isExportType
		)
		{
			return this.singletonConnection().exportTypes(exportFileProvider, isExportType);
		}

		@Override
		public final void importFiles(final XGettingEnum<File> importFiles)
		{
			this.singletonConnection().importFiles(importFiles);
		}

	}

}

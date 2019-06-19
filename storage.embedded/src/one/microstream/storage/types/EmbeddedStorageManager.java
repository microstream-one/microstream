package one.microstream.storage.types;

import static one.microstream.X.mayNull;
import static one.microstream.X.notNull;

import java.io.File;
import java.util.function.Predicate;

import one.microstream.collections.types.XGettingEnum;
import one.microstream.collections.types.XGettingTable;
import one.microstream.persistence.binary.types.Binary;
import one.microstream.persistence.types.PersistenceManager;
import one.microstream.persistence.types.PersistenceRoots;
import one.microstream.persistence.types.Storer;
import one.microstream.persistence.types.Unpersistable;
import one.microstream.reference.Reference;

public interface EmbeddedStorageManager extends StorageController, StorageConnection
{
	public StorageTypeDictionary typeDictionary();

	public StorageConnection createConnection();

	public StorageConfiguration configuration();

	public void initialize();

	@Override
	public default EmbeddedStorageManager start()
	{
		return this.start(null);
	}

	public EmbeddedStorageManager start(XGettingEnum<File> initialImportFiles);

	@Override
	public boolean shutdown();
	
	public Object root();
	
	public Reference<Object> defaultRoot();
	
	public default long storeRoot()
	{
		Object effectiveRoot = this.root();
		if(effectiveRoot == null)
		{
			effectiveRoot = this.defaultRoot();
		}
		
		return this.store(effectiveRoot);
		
//		final Reference<Object> root = this.root();
//
//		return root == null
//			? Persistence.nullId()
//			: this.store(root)
//		;
	}

	
	
	public static EmbeddedStorageManager.Default New(
		final StorageConfiguration                   configuration       ,
		final EmbeddedStorageConnectionFoundation<?> connectionFoundation,
		final PersistenceRoots                       definedRoots        ,
		final Reference<Object>                      explicitRoot
	)
	{
		return new EmbeddedStorageManager.Default(
			notNull(configuration)       ,
			notNull(connectionFoundation),
			notNull(definedRoots)        ,
			mayNull(explicitRoot)
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
		public Reference<Object> root()
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
		public final EmbeddedStorageManager.Default start(final XGettingEnum<File> initialImportFiles)
		{
			this.storageManager.start();

			// special initial import for refactoring purposes: after validation but before roots loading.
			if(initialImportFiles != null && !initialImportFiles.isEmpty())
			{
				this.initialImport(initialImportFiles);
			}

			this.ensureRequiredTypeHandlers();
			this.initialize();
			
			return this;
		}

		private void initialImport(final XGettingEnum<File> initialImportFiles)
		{
			this.connectionFoundation.createStorageConnection().importFiles(initialImportFiles);
		}

		private boolean synchronizeRoots(final PersistenceRoots loadedRoots)
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
			return !loadedRoots.hasChanged();
		}
		
		private void ensureRequiredTypeHandlers()
		{
			// make sure a functional type handler is present for every occuring type id or throw an exception.
			final StorageIdAnalysis  idAnalysis      = this.storageManager.initializationIdAnalysis();
			final XGettingEnum<Long> occuringTypeIds = idAnalysis.occuringTypeIds();
			this.connectionFoundation.getTypeHandlerManager().ensureTypeHandlersByTypeIds(occuringTypeIds);
		}

		@Override
		public final void initialize()
		{
			final StorageConnection initConnection = this.createConnection();

			/* (22.09.2014 TM)NOTE: Constants OID consistency conflict
			 * If more than one roots instance exists, all are loaded and all are built,
			 * even if afterwards only the first one is considered.
			 * Of course for multiple roots instances, different OIDs can point to the
			 * same (<- !) runtime constant instance, hence causing a consistency error on validation in the registry.
			 * Solution:
			 * Storage may only send at most one roots instance. Must be strictly ensured:
			 * - independently from roots GC
			 * - GC must collect all but one roots instance (already does but not testet yet)
			 */
			PersistenceRoots loadedRoots = initConnection.persistenceManager().createLoader().loadRoots();
			if(loadedRoots == null)
			{
				// no loaded roots is only valid if there is no data yet at all (no database files / content)
				final StorageRawFileStatistics statistics = initConnection.createStorageStatistics();
				if(statistics.liveDataLength() != 0)
				{
					// (14.09.2015 TM)EXCP: proper exception
					throw new RuntimeException("No roots found for existing data.");
				}

				loadedRoots = this.definedRoots;
			}
			else if(this.synchronizeRoots(loadedRoots))
			{
				// loaded roots are perfectly synchronous to defined roots, no store update required.
				return;
			}

			// a not perfectly synchronous loaded roots instance needs to be stored after it has been synchronized
			initConnection.store(loadedRoots);
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

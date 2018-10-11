package net.jadoth.storage.types;

import static net.jadoth.X.mayNull;
import static net.jadoth.X.notNull;

import java.io.File;
import java.util.function.Predicate;

import net.jadoth.collections.types.XGettingEnum;
import net.jadoth.collections.types.XGettingTable;
import net.jadoth.persistence.binary.types.Binary;
import net.jadoth.persistence.types.PersistenceManager;
import net.jadoth.persistence.types.PersistenceRootResolver;
import net.jadoth.persistence.types.PersistenceRoots;
import net.jadoth.persistence.types.Storer;
import net.jadoth.persistence.types.Unpersistable;
import net.jadoth.reference.Reference;
import net.jadoth.swizzling.types.Swizzle;

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

	public void truncateData();
	
	/**
	 * A reference to the application's explicit root. Potentially <code>null</code> if roots are resolved otherwise.
	 * E.g. via custom {@link PersistenceRootResolver}, static {@link EmbeddedStorage}{@link #root()} or constants.
	 * 
	 * @return the explicit root.
	 */
	public Reference<Object> root();
	
	public default long storeRoot()
	{
		final Reference<Object> root = this.root();
		
		return root == null
			? Swizzle.nullId()
			: this.store(this.root())
		;
	}

	
	
	public static EmbeddedStorageManager.Implementation New(
		final StorageConfiguration                   configuration    ,
		final EmbeddedStorageConnectionFoundation<?> connectionFactory,
		final PersistenceRoots                       definedRoots     ,
		final Reference<Object>                      explicitRoot
	)
	{
		return new EmbeddedStorageManager.Implementation(
			notNull(configuration)    ,
			notNull(connectionFactory),
			notNull(definedRoots)     ,
			mayNull(explicitRoot)
		);
	}


	public final class Implementation implements EmbeddedStorageManager, Unpersistable
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		private final StorageConfiguration                   configuration    ;
		private final StorageManager                         storageManager   ;
		private final EmbeddedStorageConnectionFoundation<?> connectionFactory;
		private final PersistenceRoots                       definedRoots     ;
		private final Reference<Object>                      explicitRoot     ;
		
		private StorageConnection singletonConnection;



		///////////////////////////////////////////////////////////////////////////
		// constructors     //
		/////////////////////

		Implementation(
			final StorageConfiguration                   configuration    ,
			final EmbeddedStorageConnectionFoundation<?> connectionFactory,
			final PersistenceRoots                       definedRoots     ,
			final Reference<Object>                      explicitRoot
		)
		{
			super();
			this.configuration     = configuration                        ;
			this.storageManager    = connectionFactory.getStorageManager(); // to ensure consistency
			this.connectionFactory = connectionFactory                    ;
			this.definedRoots      = definedRoots                         ;
			this.explicitRoot      = explicitRoot                         ;
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
			return this.explicitRoot;
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
		public final EmbeddedStorageManager.Implementation start(final XGettingEnum<File> initialImportFiles)
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
			this.connectionFactory.createStorageConnection().importFiles(initialImportFiles);
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
				loadedRoots.updateEntries(definedEntries);
			}
			
			/*
			 * If the loaded roots had to change in any way to match the runtime state of the application,
			 * it means that it has to be stored to update the persistent state to the current (changed) one.
			 * The loaded roots instance is the one that has to be stored to maintain the associated ObjectId,
			 * hence the entry synchronizsation instead of just storing the defined roots instance right away.
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
			this.connectionFactory.getTypeHandlerManager().ensureTypeHandlersByTypeIds(occuringTypeIds);
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
		public final void truncateData()
		{
			this.storageManager.truncateData();
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
		public final boolean isShutdown()
		{
			return this.storageManager.isShutdown();
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
			return this.connectionFactory.createStorageConnection();
		}

		@Override
		public void issueFullGarbageCollection()
		{
			this.singletonConnection().issueFullGarbageCollection();
		}

		@Override
		public boolean issueGarbageCollection(final long nanoTimeBudget)
		{
			return this.singletonConnection().issueGarbageCollection(nanoTimeBudget);
		}

		@Override
		public void issueFullFileCheck()
		{
			this.singletonConnection().issueFullFileCheck();
		}

		@Override
		public void issueFullFileCheck(final StorageDataFileDissolvingEvaluator fileDissolvingEvaluator)
		{
			this.singletonConnection().issueFullFileCheck(fileDissolvingEvaluator);
		}

		@Override
		public boolean issueFileCheck(final long nanoTimeBudgetBound)
		{
			return this.singletonConnection().issueFileCheck(nanoTimeBudgetBound);
		}

		@Override
		public boolean issueFileCheck(
			final long nanoTimeBudgetBound,
			final StorageDataFileDissolvingEvaluator fileDissolvingEvaluator)
		{
			return this.singletonConnection().issueFileCheck(nanoTimeBudgetBound, fileDissolvingEvaluator);
		}

		@Override
		public void issueFullCacheCheck()
		{
			this.singletonConnection().issueFullCacheCheck();
		}

		@Override
		public void issueFullCacheCheck(final StorageEntityCacheEvaluator entityEvaluator)
		{
			this.singletonConnection().issueFullCacheCheck(entityEvaluator);
		}

		@Override
		public boolean issueCacheCheck(final long nanoTimeBudgetBound)
		{
			return this.singletonConnection().issueCacheCheck(nanoTimeBudgetBound);
		}

		@Override
		public boolean issueCacheCheck(
			final long                        nanoTimeBudgetBound,
			final StorageEntityCacheEvaluator entityEvaluator
		)
		{
			return this.singletonConnection().issueCacheCheck(nanoTimeBudgetBound, entityEvaluator);
		}

		@Override
		public StorageRawFileStatistics createStorageStatistics()
		{
			return this.singletonConnection().createStorageStatistics();
		}

		@Override
		public void exportChannels(final StorageIoHandler fileHandler, final boolean performGarbageCollection)
		{
			this.singletonConnection().exportChannels(fileHandler, performGarbageCollection);
		}

		@Override
		public StorageEntityTypeExportStatistics exportTypes(
			final StorageEntityTypeExportFileProvider         exportFileProvider,
			final Predicate<? super StorageEntityTypeHandler> isExportType
		)
		{
			return this.singletonConnection().exportTypes(exportFileProvider, isExportType);
		}

		@Override
		public void importFiles(final XGettingEnum<File> importFiles)
		{
			this.singletonConnection().importFiles(importFiles);
		}


	}

}

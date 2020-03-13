package one.microstream.storage.types;

import static one.microstream.X.notNull;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.function.Predicate;

import one.microstream.collections.EqHashTable;
import one.microstream.collections.XSort;
import one.microstream.collections.types.XGettingEnum;
import one.microstream.collections.types.XGettingTable;
import one.microstream.persistence.binary.types.Binary;
import one.microstream.persistence.types.Persistence;
import one.microstream.persistence.types.PersistenceManager;
import one.microstream.persistence.types.PersistenceRootReference;
import one.microstream.persistence.types.PersistenceRoots;
import one.microstream.persistence.types.PersistenceRootsProvider;
import one.microstream.persistence.types.PersistenceRootsView;
import one.microstream.persistence.types.Storer;
import one.microstream.persistence.types.Unpersistable;
import one.microstream.reference.LazyReferenceManager;
import one.microstream.reference.Reference;
import one.microstream.reference.Swizzling;
import one.microstream.storage.exceptions.StorageException;
import one.microstream.typing.KeyValue;


/**
 * {@link StorageManager} sub type for usage as an embedded storage solution.<p>
 * "Embedded" is meant in the context that a database is managed in the same process that uses this database,
 * as opposed to the database being managed by a different process that the using process connects to via network
 * communication. That would be a "remote" or "standalone" storage process.
 * 
 * @author TM
 */
public interface EmbeddedStorageManager extends StorageManager
{
	@Override
	public EmbeddedStorageManager start();

	
	
	public static EmbeddedStorageManager.Default New(
		final Database                               database            ,
		final StorageConfiguration                   configuration       ,
		final EmbeddedStorageConnectionFoundation<?> connectionFoundation,
		final PersistenceRootsProvider<?>            rootsProvider
	)
	{
		return new EmbeddedStorageManager.Default(
			notNull(database)            ,
			notNull(configuration)       ,
			notNull(connectionFoundation),
			notNull(rootsProvider)
		);
	}


	public final class Default implements EmbeddedStorageManager, Unpersistable, LazyReferenceManager.Controller
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		private final Database                               database            ;
		private final StorageConfiguration                   configuration       ;
		private final StorageSystem                          storageSystem       ;
		private final EmbeddedStorageConnectionFoundation<?> connectionFoundation;
		private final PersistenceRootsProvider<?>            rootsProvider       ;
		
		private StorageConnection singletonConnection;



		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		Default(
			final Database                               database            ,
			final StorageConfiguration                   configuration       ,
			final EmbeddedStorageConnectionFoundation<?> connectionFoundation,
			final PersistenceRootsProvider<?>            rootsProvider
		)
		{
			super();
			this.database             = database                               ;
			this.configuration        = configuration                          ;
			this.storageSystem        = connectionFoundation.getStorageSystem(); // to ensure consistency
			this.connectionFoundation = connectionFoundation                   ;
			this.rootsProvider        = rootsProvider                          ;
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
		public final Database database()
		{
			return this.database;
		}

		@Override
		public final Object root()
		{
			return this.rootReference().get();
		}
		
		@Override
		public final Object setRoot(final Object newRoot)
		{
			this.rootReference().setRoot(newRoot);
			
			return newRoot;
		}
		
		final PersistenceRootReference rootReference()
		{
			return this.rootsProvider.provideRoots().rootReference();
		}
		
		@Override
		public long storeRoot()
		{
			final Storer storer = this.createStorer();
			
			final PersistenceRootReference rootReference = this.rootReference();
			storer.store(rootReference);
			
			final Object root = rootReference.get();
			final long rootObjectId = root != null
				? storer.store(root)
				: Swizzling.nullId()
			;
				
			storer.commit();
			
			return rootObjectId;
		}
		
		@Override
		public final PersistenceRootsView viewRoots()
		{
			return this.rootsProvider.provideRoots();
		}

		@Override
		public PersistenceManager<Binary> persistenceManager()
		{
			return this.singletonConnection().persistenceManager();
		}

		@Override
		public final Storer createStorer()
		{
			return this.singletonConnection().createStorer();
		}
		
		@Override
		public boolean mayRun()
		{
			return this.isRunning();
		}
				
		private void ensureActiveLazyReferenceManager(
			final LazyReferenceManager lazyReferenceManager         ,
			final boolean              lazyReferenceManagerIsRunning
		)
		{
			lazyReferenceManager.addController(this);
			if(lazyReferenceManagerIsRunning)
			{
				return;
			}
			
			lazyReferenceManager.start();
		}
		
		private void rollbackLazyReferenceManager(
			final LazyReferenceManager lazyReferenceManager,
			final boolean    lazyReferenceManagerWasRunning
		)
		{
			lazyReferenceManager.removeController(this);
			if(!lazyReferenceManagerWasRunning)
			{
				lazyReferenceManager.stop();
			}
		}

		@Override
		public final EmbeddedStorageManager.Default start()
		{
			final LazyReferenceManager lazyReferenceManager = LazyReferenceManager.get();
			final boolean lazyReferenceManagerIsRunning = lazyReferenceManager.isRunning();
			
			this.storageSystem.start();
			
			try
			{
				this.ensureRequiredTypeHandlers();
				this.initialize();
				
				// this depends on completed initialization
				this.ensureActiveLazyReferenceManager(lazyReferenceManager, lazyReferenceManagerIsRunning);
			}
			catch(final Throwable t)
			{
				try
				{
					this.rollbackLazyReferenceManager(lazyReferenceManager, lazyReferenceManagerIsRunning);
					
					if(this.storageSystem instanceof StorageKillable)
					{
						((StorageKillable)this.storageSystem).killStorage(t);
					}
					else
					{
						this.storageSystem.shutdown();
					}
				}
				catch(final Throwable t1)
				{
					t1.addSuppressed(t);
					throw t1;
				}
				throw t;
			}
			
			return this;
		}
		
		static final boolean isEqualRootEntry(final KeyValue<String, Object> e1, final KeyValue<String, Object> e2)
		{
			if(!e1.key().equals(e2.key()))
			{
				return false;
			}
			
			// Enum special case: enum holder arrays are not identical, only their content must be.
			if(Persistence.isEnumRootIdentifier(e1.key()))
			{
				return Arrays.equals((Object[])e1.value(), (Object[])e2.value());
			}
			
			// All non-Enum cases must have identical root instances.
			return e1.value() == e2.value();
		}
		
		private static EqHashTable<String, Object> normalize(final XGettingTable<String, Object> entries)
		{
			final EqHashTable<String, Object> preparedEntries = EqHashTable.New(entries);
			
			// order of enum entries can differ (sorted type dictionary vs. encountering order). Normalize here.
			preparedEntries.keys().sort(XSort::compare);
			
			return preparedEntries;
		}
		
				
		private void synchronizeRoots(final PersistenceRoots loadedRoots)
		{
			final EqHashTable<String, Object> loadedEntries  = normalize(loadedRoots.entries());
			final EqHashTable<String, Object> definedEntries = normalize(this.rootsProvider.provideRoots().entries());
			
			final boolean match = loadedEntries.equalsContent(definedEntries, Default::isEqualRootEntry);
			if(!match)
			{
				// change detected. Entries of loadedRoots must be updated/replaced
				loadedRoots.updateEntries(definedEntries);
			}
			
			/*
			 * If the loaded roots had to change in any way to match the runtime state of the application,
			 * it means that it has to be stored to update the persistent state to the current (changed) one.
			 * The loaded roots instance is the one that has to be stored to maintain the associated ObjectId,
			 * hence the entry synchronization instead of just storing the defined roots instance right away.
			 */
			
			// must update the roots provider with the loadedRoots instance for the same reason
			this.rootsProvider.updateRuntimeRoots(loadedRoots);
		}
		
		private void ensureRequiredTypeHandlers()
		{
			// make sure a functional type handler is present for every occuring type id or throw an exception.
			final StorageIdAnalysis  idAnalysis      = this.storageSystem.initializationIdAnalysis();
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
				throw new StorageException("No roots found for existing data.");
			}

			return this.rootsProvider.provideRoots();
		}

		private void initialize()
		{
			final StorageConnection initConnection = this.createConnection();

			PersistenceRoots loadedRoots = this.loadExistingRoots(initConnection);
			if(loadedRoots == null)
			{
				// gets stored below, no matter the changed state (which is initially false)
				loadedRoots = this.validateEmptyDatabaseAndReturnDefinedRoots(initConnection);
			}
			else
			{
				this.synchronizeRoots(loadedRoots);
				if(!loadedRoots.hasChanged())
				{
					//  abort before storing because there is no need to.
					return;
				}
			}
			
			// any other case than a perfectly synchronous loaded roots instance needs to store
			initConnection.store(loadedRoots);
		}
		
		@Override
		public final boolean shutdown()
		{
			return this.storageSystem.shutdown();
		}

		@Override
		public final boolean isAcceptingTasks()
		{
			return this.storageSystem.isAcceptingTasks();
		}

		@Override
		public final boolean isRunning()
		{
			return this.storageSystem.isRunning();
		}
		
		@Override
		public final boolean isActive()
		{
			return this.storageSystem.isActive();
		}

		@Override
		public final boolean isStartingUp()
		{
			return this.storageSystem.isStartingUp();
		}

		@Override
		public final boolean isShuttingDown()
		{
			return this.storageSystem.isShuttingDown();
		}

		@Override
		public final void checkAcceptingTasks()
		{
			this.storageSystem.checkAcceptingTasks();
		}

		@Override
		public final StorageConfiguration configuration()
		{
			return this.configuration;
		}

		@Override
		public final StorageTypeDictionary typeDictionary()
		{
			return this.storageSystem.typeDictionary();
		}

		@Override
		public final StorageConnection createConnection()
		{
			return this.connectionFoundation.createStorageConnection();
		}
		
		@Override
		public final long initializationTime()
		{
			return this.storageSystem.initializationTime();
		}
		
		@Override
		public final long operationModeTime()
		{
			return this.storageSystem.operationModeTime();
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
		public final boolean issueFileCheck(final long nanoTimeBudget)
		{
			return this.singletonConnection().issueFileCheck(nanoTimeBudget);
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
		public final boolean issueCacheCheck(final long nanoTimeBudget)
		{
			return this.singletonConnection().issueCacheCheck(nanoTimeBudget);
		}

		@Override
		public final boolean issueCacheCheck(
			final long                        nanoTimeBudget,
			final StorageEntityCacheEvaluator entityEvaluator
		)
		{
			return this.singletonConnection().issueCacheCheck(nanoTimeBudget, entityEvaluator);
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
		public final void importFiles(final XGettingEnum<Path> importFiles)
		{
			this.singletonConnection().importFiles(importFiles);
		}
		

		
		@Deprecated
		@Override
		public final Reference<Object> defaultRoot()
		{
			return this.rootReference();
		}

	}

}

package one.microstream.storage.embedded.types;

import static one.microstream.X.notNull;

import java.util.function.Predicate;

import one.microstream.afs.types.AFile;
import one.microstream.collections.types.XGettingEnum;
import one.microstream.persistence.binary.types.Binary;
import one.microstream.persistence.types.PersistenceManager;
import one.microstream.persistence.types.PersistenceRootsView;
import one.microstream.persistence.types.PersistenceTypeDictionaryExporter;
import one.microstream.persistence.types.Storer;
import one.microstream.reference.Reference;
import one.microstream.storage.exceptions.StorageException;
import one.microstream.storage.types.Database;
import one.microstream.storage.types.StorageConfiguration;
import one.microstream.storage.types.StorageConnection;
import one.microstream.storage.types.StorageEntityCacheEvaluator;
import one.microstream.storage.types.StorageEntityTypeExportFileProvider;
import one.microstream.storage.types.StorageEntityTypeExportStatistics;
import one.microstream.storage.types.StorageEntityTypeHandler;
import one.microstream.storage.types.StorageLiveFileProvider;
import one.microstream.storage.types.StorageLoggingWrapper;
import one.microstream.storage.types.StorageRawFileStatistics;
import one.microstream.storage.types.StorageTypeDictionary;

public interface EmbeddedStorageManagerLogging
	extends EmbeddedStorageManager, StorageLoggingWrapper<EmbeddedStorageManager>
{
	public static EmbeddedStorageManagerLogging New(
		final EmbeddedStorageManager wrapped
	)
	{
		return new Default(notNull(wrapped));
	}


	public static class Default
		extends StorageLoggingWrapper.Abstract<EmbeddedStorageManager>
		implements EmbeddedStorageManagerLogging
	{
		protected Default(
			final EmbeddedStorageManager wrapped
		)
		{
			super(wrapped);
		}

		@Override
		public EmbeddedStorageManager start()
		{
			this.logger().embeddedStorageManager_beforeStart(this);

			this.wrapped().start();

			this.logger().embeddedStorageManager_afterStart(this);

			return this;
		}

		@Override
		public boolean shutdown()
		{
			this.logger().embeddedStorageManager_beforeShutdown(this);

			final boolean success = this.wrapped().shutdown();

			this.logger().embeddedStorageManager_afterShutdown(this);

			return success;
		}

		@Override
		public boolean isActive()
		{
			return this.wrapped().isActive();
		}

		@Override
		public boolean isAcceptingTasks()
		{
			return this.wrapped().isAcceptingTasks();
		}

		@Override
		public boolean isRunning()
		{
			return this.wrapped().isRunning();
		}

		@Override
		public boolean isStartingUp()
		{
			return this.wrapped().isStartingUp();
		}

		@Override
		public void issueFullGarbageCollection()
		{
			this.wrapped().issueFullGarbageCollection();
		}

		@Override
		public void issueFullBackup(
			final StorageLiveFileProvider targetFileProvider,
			final PersistenceTypeDictionaryExporter typeDictionaryExporter)
		{
			this.wrapped().issueFullBackup(targetFileProvider, typeDictionaryExporter);
		}

		@Override
		public boolean isShuttingDown()
		{
			return this.wrapped().isShuttingDown();
		}

		@Override
		public boolean isShutdown()
		{
			return this.wrapped().isShutdown();
		}

		@Override
		public void checkAcceptingTasks()
		{
			this.wrapped().checkAcceptingTasks();
		}

		@Override
		public long initializationTime()
		{
			return this.wrapped().initializationTime();
		}

		@Override
		public long operationModeTime()
		{
			return this.wrapped().operationModeTime();
		}

		@Override
		public long initializationDuration()
		{
			return this.wrapped().initializationDuration();
		}

		@Override
		public void close() throws StorageException
		{
			this.wrapped().close();
		}

		@Override
		public StorageConfiguration configuration()
		{
			return this.wrapped().configuration();
		}

		@Override
		public StorageTypeDictionary typeDictionary()
		{
			return this.wrapped().typeDictionary();
		}

		@Override
		public StorageConnection createConnection()
		{
			return this.wrapped().createConnection();
		}

		@Override
		public boolean issueGarbageCollection(final long nanoTimeBudget)
		{
			return this.wrapped().issueGarbageCollection(nanoTimeBudget);
		}

		@Override
		public Object root()
		{
			return this.wrapped().root();
		}

		@Override
		public void issueFullFileCheck()
		{
			this.wrapped().issueFullFileCheck();
		}

		@Override
		public Object setRoot(final Object newRoot)
		{
			return this.wrapped().setRoot(newRoot);
		}

		@Override
		public long storeRoot()
		{
			return this.wrapped().storeRoot();
		}

		@Override
		public boolean issueFileCheck(final long nanoTimeBudget)
		{
			return this.wrapped().issueFileCheck(nanoTimeBudget);
		}

		@Override
		public PersistenceRootsView viewRoots()
		{
			return this.wrapped().viewRoots();
		}

		@Deprecated
		@Override
		public Reference<Object> defaultRoot()
		{
			return this.wrapped().defaultRoot();
		}

		@Override
		public void issueFullCacheCheck()
		{
			this.wrapped().issueFullCacheCheck();
		}

		@Deprecated
		@Override
		public Object customRoot()
		{
			return this.wrapped().customRoot();
		}

		@Deprecated
		@Override
		public long storeDefaultRoot()
		{
			return this.wrapped().storeDefaultRoot();
		}

		@Override
		public Database database()
		{
			return this.wrapped().database();
		}

		@Override
		public String databaseName()
		{
			return this.wrapped().databaseName();
		}

		@Override
		public void issueFullCacheCheck(final StorageEntityCacheEvaluator entityEvaluator)
		{
			this.wrapped().issueFullCacheCheck(entityEvaluator);
		}

		@Override
		public boolean issueCacheCheck(final long nanoTimeBudget)
		{
			return this.wrapped().issueCacheCheck(nanoTimeBudget);
		}

		@Override
		public boolean issueCacheCheck(final long nanoTimeBudget, final StorageEntityCacheEvaluator entityEvaluator)
		{
			return this.wrapped().issueCacheCheck(nanoTimeBudget, entityEvaluator);
		}

		@Override
		public StorageRawFileStatistics createStorageStatistics()
		{
			return this.wrapped().createStorageStatistics();
		}
		
		@Override
		public StorageEntityTypeExportStatistics exportTypes(
			final StorageEntityTypeExportFileProvider exportFileProvider,
			final Predicate<? super StorageEntityTypeHandler> isExportType)
		{
			return this.wrapped().exportTypes(exportFileProvider, isExportType);
		}

		@Override
		public StorageEntityTypeExportStatistics exportTypes(final StorageEntityTypeExportFileProvider exportFileProvider)
		{
			return this.wrapped().exportTypes(exportFileProvider);
		}
		
		@Override
		public PersistenceManager<Binary> persistenceManager()
		{
			return this.wrapped().persistenceManager();
		}

		@Override
		public long store(final Object instance)
		{
			return this.wrapped().store(instance);
		}

		@Override
		public long[] storeAll(final Object... instances)
		{
			return this.wrapped().storeAll(instances);
		}

		@Override
		public void storeAll(final Iterable<?> instances)
		{
			this.wrapped().storeAll(instances);
		}

		@Override
		public Storer createLazyStorer()
		{
			return this.wrapped().createLazyStorer();
		}

		@Override
		public Storer createStorer()
		{
			return this.wrapped().createStorer();
		}

		@Override
		public Storer createEagerStorer()
		{
			return this.wrapped().createEagerStorer();
		}

		@Override
		public Object getObject(final long objectId)
		{
			return this.wrapped().getObject(objectId);
		}

		@Override
		public void exportChannels(
			final StorageLiveFileProvider fileProvider,
			final boolean performGarbageCollection
		)
		{
			this.wrapped().exportChannels(fileProvider, performGarbageCollection);
		}

		@Override
		public void importFiles(
			final XGettingEnum<AFile> importFiles
		)
		{
			this.wrapped().importFiles(importFiles);
		}

	}

}

package one.microstream.storage.embedded.types;

import java.util.function.Consumer;
import java.util.function.Supplier;

import one.microstream.functional.InstanceDispatcherLogic;
import one.microstream.persistence.binary.types.Binary;
import one.microstream.persistence.types.PersistenceRefactoringMappingProvider;
import one.microstream.persistence.types.PersistenceRootResolverProvider;
import one.microstream.persistence.types.PersistenceTypeEvaluator;
import one.microstream.persistence.types.PersistenceTypeHandler;
import one.microstream.persistence.types.PersistenceTypeHandlerRegistration;
import one.microstream.storage.types.Databases;
import one.microstream.storage.types.StorageBackupThreadProvider;
import one.microstream.storage.types.StorageChannelThreadProvider;
import one.microstream.storage.types.StorageChannelsCreator;
import one.microstream.storage.types.StorageConfiguration;
import one.microstream.storage.types.StorageDataChunkValidator.Provider;
import one.microstream.storage.types.StorageDataChunkValidator.Provider2;
import one.microstream.storage.types.StorageEventLogger;
import one.microstream.storage.types.StorageExceptionHandler;
import one.microstream.storage.types.StorageGCZombieOidHandler;
import one.microstream.storage.types.StorageHousekeepingBroker;
import one.microstream.storage.types.StorageInitialDataFileNumberProvider;
import one.microstream.storage.types.StorageLockFileManagerThreadProvider;
import one.microstream.storage.types.StorageLockFileSetup;
import one.microstream.storage.types.StorageLoggingWrapper;
import one.microstream.storage.types.StorageObjectIdRangeEvaluator;
import one.microstream.storage.types.StorageRequestTaskCreator;
import one.microstream.storage.types.StorageRootTypeIdProvider;
import one.microstream.storage.types.StorageSystem;
import one.microstream.storage.types.StorageThreadNameProvider;
import one.microstream.storage.types.StorageThreadProvider;
import one.microstream.storage.types.StorageTimestampProvider;
import one.microstream.storage.types.StorageTypeDictionary;
import one.microstream.storage.types.StorageWriteController;
import one.microstream.util.InstanceDispatcher;
import one.microstream.util.ProcessIdentityProvider;

public interface EmbeddedStorageFoundationLogging<F extends EmbeddedStorageFoundation<?>>
	extends EmbeddedStorageFoundation<F>, StorageLoggingWrapper<EmbeddedStorageFoundation<F>>
{
	public static <F extends EmbeddedStorageFoundation<?>> EmbeddedStorageFoundationLogging<F> New(
		final EmbeddedStorageFoundation<F> wrapped
	)
	{
		final EmbeddedStorageLoggingWrapperCreator dispatcherLogic = EmbeddedStorageLoggingWrapperCreator.New(wrapped);
		wrapped.setInstanceDispatcherLogic(dispatcherLogic);
		wrapped.getConnectionFoundation().setInstanceDispatcher(dispatcherLogic);
		return new Default<>(wrapped);
	}


	public static class Default<F extends EmbeddedStorageFoundation<?>>
		extends StorageLoggingWrapper.Abstract<EmbeddedStorageFoundation<F>>
		implements EmbeddedStorageFoundationLogging<F>
	{
		protected Default(
			final EmbeddedStorageFoundation<F> wrapped
		)
		{
			super(wrapped);
		}

		@Override
		public InstanceDispatcher setInstanceDispatcherLogic(final InstanceDispatcherLogic logic)
		{
			return this.wrapped().setInstanceDispatcherLogic(
				logic instanceof EmbeddedStorageLoggingWrapperCreator
					? logic
					: EmbeddedStorageLoggingWrapperCreator.New(this, logic)
			);
		}

		@Override
		public InstanceDispatcherLogic getInstanceDispatcherLogic()
		{
			return this.wrapped().getInstanceDispatcherLogic();
		}

		@Override
		public EmbeddedStorageManager createEmbeddedStorageManager(final Object explicitRoot)
		{
			this.logger().embeddedStorageFoundation_beforeCreateEmbeddedStorageManager();
			this.logger().logConfiguration(this.wrapped().getConfiguration());
			
			final EmbeddedStorageManager embeddedStorageManager = EmbeddedStorageManagerLogging.New(
				this.wrapped().createEmbeddedStorageManager(explicitRoot)
			);
			
			this.logger().embeddedStorageFoundation_afterCreateEmbeddedStorageManager(embeddedStorageManager);
			
			return embeddedStorageManager;
		}
		
		@Override
		public EmbeddedStorageConnectionFoundation<?> getConnectionFoundation()
		{
			return this.wrapped().getConnectionFoundation();
		}

		@Override
		public StorageConfiguration getConfiguration()
		{
			return this.wrapped().getConfiguration();
		}

		@Override
		public Databases getDatabases()
		{
			return this.wrapped().getDatabases();
		}

		@Override
		public String getDataBaseName()
		{
			return this.wrapped().getDataBaseName();
		}

		@Override
		public StorageInitialDataFileNumberProvider getInitialDataFileNumberProvider()
		{
			return this.wrapped().getInitialDataFileNumberProvider();
		}

		@Override
		public PersistenceRootResolverProvider getRootResolverProvider()
		{
			return this.wrapped().getRootResolverProvider();
		}

		@Override
		public one.microstream.storage.types.StorageRequestAcceptor.Creator getRequestAcceptorCreator()
		{
			return this.wrapped().getRequestAcceptorCreator();
		}

		@Override
		public PersistenceTypeEvaluator getTypeEvaluatorPersistable()
		{
			return this.wrapped().getTypeEvaluatorPersistable();
		}

		@Override
		public F onConnectionFoundation(final Consumer<? super EmbeddedStorageConnectionFoundation<?>> logic)
		{
			return this.wrapped().onConnectionFoundation(logic);
		}

		@Override
		public F onThis(final Consumer<? super EmbeddedStorageFoundation<?>> logic)
		{
			return this.wrapped().onThis(logic);
		}

		@Override
		public one.microstream.storage.types.StorageTaskBroker.Creator getTaskBrokerCreator()
		{
			return this.wrapped().getTaskBrokerCreator();
		}

		@Override
		public Provider getDataChunkValidatorProvider()
		{
			return this.wrapped().getDataChunkValidatorProvider();
		}

		@Override
		public Provider2 getDataChunkValidatorProvider2()
		{
			return this.wrapped().getDataChunkValidatorProvider2();
		}

		@Override
		public StorageChannelsCreator getChannelCreator()
		{
			return this.wrapped().getChannelCreator();
		}

		@Override
		public StorageThreadNameProvider getThreadNameProvider()
		{
			return this.wrapped().getThreadNameProvider();
		}

		@Override
		public StorageChannelThreadProvider getChannelThreadProvider()
		{
			return this.wrapped().getChannelThreadProvider();
		}

		@Override
		public F setConnectionFoundation(final EmbeddedStorageConnectionFoundation<?> connectionFoundation)
		{
			return this.wrapped().setConnectionFoundation(connectionFoundation);
		}

		@Override
		public F setDatabases(final Databases databases)
		{
			return this.wrapped().setDatabases(databases);
		}

		@Override
		public StorageBackupThreadProvider getBackupThreadProvider()
		{
			return this.wrapped().getBackupThreadProvider();
		}

		@Override
		public F setDataBaseName(final String dataBaseName)
		{
			return this.wrapped().setDataBaseName(dataBaseName);
		}

		@Override
		public F setRoot(final Object root)
		{
			return this.wrapped().setRoot(root);
		}

		@Override
		public StorageLockFileManagerThreadProvider getLockFileManagerThreadProvider()
		{
			return this.wrapped().getLockFileManagerThreadProvider();
		}

		@Override
		public StorageThreadProvider getThreadProvider()
		{
			return this.wrapped().getThreadProvider();
		}

		@Override
		public F setRootSupplier(final Supplier<?> rootSupplier)
		{
			return this.wrapped().setRootSupplier(rootSupplier);
		}

		@Override
		public StorageRequestTaskCreator getRequestTaskCreator()
		{
			return this.wrapped().getRequestTaskCreator();
		}

		@Override
		public StorageTypeDictionary getTypeDictionary()
		{
			return this.wrapped().getTypeDictionary();
		}

		@Override
		public F setRootResolverProvider(final PersistenceRootResolverProvider rootResolverProvider)
		{
			return this.wrapped().setRootResolverProvider(rootResolverProvider);
		}

		@Override
		public StorageRootTypeIdProvider getRootTypeIdProvider()
		{
			return this.wrapped().getRootTypeIdProvider();
		}

		@Override
		public F setTypeEvaluatorPersistable(final PersistenceTypeEvaluator typeEvaluatorPersistable)
		{
			return this.wrapped().setTypeEvaluatorPersistable(typeEvaluatorPersistable);
		}

		@Override
		public F setRefactoringMappingProvider(final PersistenceRefactoringMappingProvider refactoringMappingProvider)
		{
			return this.wrapped().setRefactoringMappingProvider(refactoringMappingProvider);
		}

		@Override
		public StorageTimestampProvider getTimestampProvider()
		{
			return this.wrapped().getTimestampProvider();
		}

		@Override
		public F executeTypeHandlerRegistration(final PersistenceTypeHandlerRegistration<Binary> typeHandlerRegistration)
		{
			return this.wrapped().executeTypeHandlerRegistration(typeHandlerRegistration);
		}

		@Override
		public StorageObjectIdRangeEvaluator getObjectIdRangeEvaluator()
		{
			return this.wrapped().getObjectIdRangeEvaluator();
		}

		@Override
		public F registerTypeHandler(final PersistenceTypeHandler<Binary, ?> typeHandler)
		{
			return this.wrapped().registerTypeHandler(typeHandler);
		}

		@Override
		public F registerTypeHandlers(final Iterable<? extends PersistenceTypeHandler<Binary, ?>> typeHandlers)
		{
			return this.wrapped().registerTypeHandlers(typeHandlers);
		}

		@Override
		public StorageGCZombieOidHandler getGCZombieOidHandler()
		{
			return this.wrapped().getGCZombieOidHandler();
		}

		@Override
		public one.microstream.storage.types.StorageRootOidSelector.Provider getRootOidSelectorProvider()
		{
			return this.wrapped().getRootOidSelectorProvider();
		}

		@Override
		public one.microstream.storage.types.StorageObjectIdMarkQueue.Creator getOidMarkQueueCreator()
		{
			return this.wrapped().getOidMarkQueueCreator();
		}

		@Override
		public one.microstream.storage.types.StorageEntityMarkMonitor.Creator getEntityMarkMonitorCreator()
		{
			return this.wrapped().getEntityMarkMonitorCreator();
		}

		@Override
		public one.microstream.storage.types.StorageDataFileValidator.Creator getDataFileValidatorCreator()
		{
			return this.wrapped().getDataFileValidatorCreator();
		}

		@Override
		public one.microstream.persistence.binary.types.BinaryEntityRawDataIterator.Provider getEntityDataIteratorProvider()
		{
			return this.wrapped().getEntityDataIteratorProvider();
		}

		@Override
		public one.microstream.storage.types.StorageEntityDataValidator.Creator getEntityDataValidatorCreator()
		{
			return this.wrapped().getEntityDataValidatorCreator();
		}

		@Override
		public ProcessIdentityProvider getProcessIdentityProvider()
		{
			return this.wrapped().getProcessIdentityProvider();
		}

		@Override
		public StorageLockFileSetup getLockFileSetup()
		{
			return this.wrapped().getLockFileSetup();
		}

		@Override
		public one.microstream.storage.types.StorageLockFileSetup.Provider getLockFileSetupProvider()
		{
			return this.wrapped().getLockFileSetupProvider();
		}

		@Override
		public one.microstream.storage.types.StorageLockFileManager.Creator getLockFileManagerCreator()
		{
			return this.wrapped().getLockFileManagerCreator();
		}

		@Override
		public StorageExceptionHandler getExceptionHandler()
		{
			return this.wrapped().getExceptionHandler();
		}

		@Override
		public StorageEventLogger getEventLogger()
		{
			return this.wrapped().getEventLogger();
		}

		@Override
		public F setConfiguration(final StorageConfiguration configuration)
		{
			return this.wrapped().setConfiguration(configuration);
		}

		@Override
		public F setInitialDataFileNumberProvider(final StorageInitialDataFileNumberProvider initDataFileNumberProvider)
		{
			return this.wrapped().setInitialDataFileNumberProvider(initDataFileNumberProvider);
		}

		@Override
		public F setRequestAcceptorCreator(
			final one.microstream.storage.types.StorageRequestAcceptor.Creator requestAcceptorCreator)
		{
			return this.wrapped().setRequestAcceptorCreator(requestAcceptorCreator);
		}

		@Override
		public F setTaskBrokerCreator(final one.microstream.storage.types.StorageTaskBroker.Creator taskBrokerCreator)
		{
			return this.wrapped().setTaskBrokerCreator(taskBrokerCreator);
		}

		@Override
		public F setDataChunkValidatorProvider(final Provider dataChunkValidatorProvider)
		{
			return this.wrapped().setDataChunkValidatorProvider(dataChunkValidatorProvider);
		}

		@Override
		public F setDataChunkValidatorProvider2(final Provider2 dataChunkValidatorProvider2)
		{
			return this.wrapped().setDataChunkValidatorProvider2(dataChunkValidatorProvider2);
		}

		@Override
		public F setChannelCreator(final StorageChannelsCreator channelCreator)
		{
			return this.wrapped().setChannelCreator(channelCreator);
		}

		@Override
		public F setThreadNameProvider(final StorageThreadNameProvider threadNameProvider)
		{
			return this.wrapped().setThreadNameProvider(threadNameProvider);
		}

		@Override
		public F setChannelThreadProvider(final StorageChannelThreadProvider channelThreadProvider)
		{
			return this.wrapped().setChannelThreadProvider(channelThreadProvider);
		}

		@Override
		public F setBackupThreadProvider(final StorageBackupThreadProvider backupThreadProvider)
		{
			return this.wrapped().setBackupThreadProvider(backupThreadProvider);
		}

		@Override
		public F setLockFileManagerThreadProvider(final StorageLockFileManagerThreadProvider lockFileManagerThreadProvider)
		{
			return this.wrapped().setLockFileManagerThreadProvider(lockFileManagerThreadProvider);
		}

		@Override
		public F setThreadProvider(final StorageThreadProvider threadProvider)
		{
			return this.wrapped().setThreadProvider(threadProvider);
		}

		@Override
		public F setTaskCreator(final StorageRequestTaskCreator taskCreator)
		{
			return this.wrapped().setTaskCreator(taskCreator);
		}

		@Override
		public F setTypeDictionary(final StorageTypeDictionary typeDictionary)
		{
			return this.wrapped().setTypeDictionary(typeDictionary);
		}

		@Override
		public F setRootTypeIdProvider(final StorageRootTypeIdProvider rootTypeIdProvider)
		{
			return this.wrapped().setRootTypeIdProvider(rootTypeIdProvider);
		}

		@Override
		public F setTimestampProvider(final StorageTimestampProvider timestampProvider)
		{
			return this.wrapped().setTimestampProvider(timestampProvider);
		}

		@Override
		public F setObjectIdRangeEvaluator(final StorageObjectIdRangeEvaluator objectIdRangeEvaluator)
		{
			return this.wrapped().setObjectIdRangeEvaluator(objectIdRangeEvaluator);
		}

		@Override
		public F setWriterProvider(final one.microstream.storage.types.StorageFileWriter.Provider writerProvider)
		{
			return this.wrapped().setWriterProvider(writerProvider);
		}

		@Override
		public F setRootOidSelectorProvider(
			final one.microstream.storage.types.StorageRootOidSelector.Provider rootOidSelectorProvider)
		{
			return this.wrapped().setRootOidSelectorProvider(rootOidSelectorProvider);
		}

		@Override
		public F setOidMarkQueueCreator(
			final one.microstream.storage.types.StorageObjectIdMarkQueue.Creator oidMarkQueueCreator)
		{
			return this.wrapped().setOidMarkQueueCreator(oidMarkQueueCreator);
		}

		@Override
		public F setEntityMarkMonitorCreator(
			final one.microstream.storage.types.StorageEntityMarkMonitor.Creator entityMarkMonitorCreator)
		{
			return this.wrapped().setEntityMarkMonitorCreator(entityMarkMonitorCreator);
		}

		@Override
		public F setDataFileValidatorCreator(
			final one.microstream.storage.types.StorageDataFileValidator.Creator dataFileValidatorCreator)
		{
			return this.wrapped().setDataFileValidatorCreator(dataFileValidatorCreator);
		}

		@Override
		public F setEntityDataIteratorProvider(
			final one.microstream.persistence.binary.types.BinaryEntityRawDataIterator.Provider entityRawDataIteratorProvider)
		{
			return this.wrapped().setEntityDataIteratorProvider(entityRawDataIteratorProvider);
		}

		@Override
		public F setEntityDataValidatorCreator(
			final one.microstream.storage.types.StorageEntityDataValidator.Creator entityDataValidatorCreator)
		{
			return this.wrapped().setEntityDataValidatorCreator(entityDataValidatorCreator);
		}

		@Override
		public F setProcessIdentityProvider(final ProcessIdentityProvider processIdentityProvider)
		{
			return this.wrapped().setProcessIdentityProvider(processIdentityProvider);
		}

		@Override
		public F setLockFileSetup(final StorageLockFileSetup lockFileSetup)
		{
			return this.wrapped().setLockFileSetup(lockFileSetup);
		}

		@Override
		public F setLockFileSetupProvider(
			final one.microstream.storage.types.StorageLockFileSetup.Provider lockFileSetupProvider)
		{
			return this.wrapped().setLockFileSetupProvider(lockFileSetupProvider);
		}

		@Override
		public F setLockFileManagerCreator(
			final one.microstream.storage.types.StorageLockFileManager.Creator lockFileManagerCreator)
		{
			return this.wrapped().setLockFileManagerCreator(lockFileManagerCreator);
		}

		@Override
		public F setExceptionHandler(final StorageExceptionHandler exceptionHandler)
		{
			return this.wrapped().setExceptionHandler(exceptionHandler);
		}

		@Override
		public F setEventLogger(final StorageEventLogger eventLogger)
		{
			return this.wrapped().setEventLogger(eventLogger);
		}

		@Override
		public StorageSystem createStorageSystem()
		{
			this.logger().embeddedStorageFoundation_beforeCreateStorageSystem();
			
			final StorageSystem storageSystem = this.wrapped().createStorageSystem();
			
			this.logger().embeddedStorageFoundation_afterCreateStorageSystem(storageSystem);
			
			return storageSystem;
		}

		@Override
		public one.microstream.storage.types.StorageFileWriter.Provider getWriterProvider()
		{
			return this.wrapped().getWriterProvider();
		}

		@Override
		public F setGCZombieOidHandler(
			final StorageGCZombieOidHandler gCZombieOidHandler
		)
		{
			return this.wrapped().setGCZombieOidHandler(gCZombieOidHandler);
		}

		@Override
		public StorageWriteController writeController()
		{
			return this.wrapped().writeController();
		}

		@Override
		public StorageWriteController getWriteController()
		{
			return this.wrapped().getWriteController();
		}

		@Override
		public StorageHousekeepingBroker housekeepingBroker()
		{
			return this.wrapped().housekeepingBroker();
		}

		@Override
		public StorageHousekeepingBroker getHousekeepingBroker()
		{
			return this.wrapped().getHousekeepingBroker();
		}

		@Override
		public F setWriteController(final StorageWriteController writeController)
		{
			return this.wrapped().setWriteController(writeController);
		}

		@Override
		public F setHousekeepingBroker(final StorageHousekeepingBroker housekeepingBroker)
		{
			return this.wrapped().setHousekeepingBroker(housekeepingBroker);
		}

		@Override
		public one.microstream.storage.types.StorageOperationController.Creator getOperationControllerCreator()
		{
			return this.wrapped().getOperationControllerCreator();
		}

		@Override
		public F setOperationControllerCreator(
			final one.microstream.storage.types.StorageOperationController.Creator operationControllerCreator)
		{
			return this.wrapped().setOperationControllerCreator(operationControllerCreator);
		}
	}
}

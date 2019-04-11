package one.microstream.storage.types;

import java.nio.ByteOrder;

import one.microstream.exceptions.MissingFoundationPartException;
import one.microstream.persistence.binary.types.BinaryEntityRawDataIterator;
import one.microstream.persistence.types.Unpersistable;
import one.microstream.storage.types.StorageDataChunkValidator.Provider2;
import one.microstream.storage.types.StorageFileWriter.Provider;
import one.microstream.util.InstanceDispatcher;

public interface StorageFoundation<F extends StorageFoundation<?>>
{
	public StorageOperationController.Creator getOperationControllerCreator();
	
	public StorageInitialDataFileNumberProvider getInitialDataFileNumberProvider();

	public StorageRequestAcceptor.Creator getRequestAcceptorCreator();

	public StorageTaskBroker.Creator getTaskBrokerCreator();

	public StorageDataChunkValidator.Provider getDataChunkValidatorProvider();
	
	public StorageDataChunkValidator.Provider2 getDataChunkValidatorProvider2();

	public StorageChannelsCreator getChannelCreator();

	public StorageChannelThreadProvider getChannelThreadProvider();

	public StorageBackupThreadProvider getBackupThreadProvider();
	
	public StorageThreadProvider getThreadProvider();

	public StorageRequestTaskCreator getRequestTaskCreator();

	public StorageTypeDictionary getTypeDictionary();

	public StorageRootTypeIdProvider getRootTypeIdProvider();

	public StorageConfiguration getConfiguration();

	public StorageTimestampProvider getTimestampProvider();

	public StorageObjectIdRangeEvaluator getObjectIdRangeEvaluator();

	public StorageFileReader.Provider getReaderProvider();

	public StorageFileWriter.Provider getWriterProvider();

	public StorageGCZombieOidHandler getGCZombieOidHandler();

	public StorageRootOidSelector.Provider getRootOidSelectorProvider();

	public StorageOidMarkQueue.Creator getOidMarkQueueCreator();

	public StorageEntityMarkMonitor.Creator getEntityMarkMonitorCreator();

	public StorageDataFileValidator.Creator getDataFileValidatorCreator();

	public BinaryEntityRawDataIterator.Provider getEntityDataIteratorProvider();
	
	public StorageEntityDataValidator.Creator getEntityDataValidatorCreator();

	public StorageExceptionHandler getExceptionHandler();


	public F setOperationControllerCreator(StorageOperationController.Creator operationControllerProvider);

	public F setInitialDataFileNumberProvider(StorageInitialDataFileNumberProvider initDataFileNumberProvider);

	public F setRequestAcceptorCreator(StorageRequestAcceptor.Creator requestAcceptorCreator);

	public F setTaskBrokerCreator(StorageTaskBroker.Creator taskBrokerCreator);

	public F setDataChunkValidatorProvider(StorageDataChunkValidator.Provider dataChunkValidatorProvider);
	
	public F setDataChunkValidatorProvider2(StorageDataChunkValidator.Provider2 dataChunkValidatorProvider2);

	public F setChannelCreator(StorageChannelsCreator channelCreator);

	public F setChannelThreadProvider(StorageChannelThreadProvider channelThreadProvider);
	
	public F setBackupThreadProvider(StorageBackupThreadProvider backupThreadProvider);
	
	public F setThreadProvider(StorageThreadProvider threadProvider);

	public F setTaskCreator(StorageRequestTaskCreator taskCreator);

	public F setTypeDictionary(StorageTypeDictionary typeDictionary);

	public F setRootTypeIdProvider(StorageRootTypeIdProvider rootTypeIdProvider);

	public F setConfiguration(StorageConfiguration configuration);

	public F setTimestampProvider(StorageTimestampProvider timestampProvider);

	public F setObjectIdRangeEvaluator(StorageObjectIdRangeEvaluator objectIdRangeEvaluator);

	public F setReaderProvider(StorageFileReader.Provider readerProvider);

	public F setWriterProvider(StorageFileWriter.Provider writerProvider);

	public F setGCZombieOidHandler(StorageGCZombieOidHandler gCZombieOidHandler);

	public F setRootOidSelectorProvider(StorageRootOidSelector.Provider rootOidSelectorProvider);

	public F setExceptionHandler(StorageExceptionHandler exceptionHandler);

	public F setOidMarkQueueCreator(StorageOidMarkQueue.Creator oidMarkQueueCreator);

	public F setEntityMarkMonitorCreator(StorageEntityMarkMonitor.Creator entityMarkMonitorCreator);

	public F setDataFileValidatorCreator(StorageDataFileValidator.Creator dataFileValidatorCreator);

	public F setEntityDataIteratorProvider(BinaryEntityRawDataIterator.Provider entityDataIteratorProvider);
	
	public F setEntityDataValidatorCreator(StorageEntityDataValidator.Creator entityDataValidatorCreator);

	public StorageManager createStorageManager();



	public class Implementation<F extends StorageFoundation.Implementation<?>>
	extends InstanceDispatcher.Implementation
	implements StorageFoundation<F>, Unpersistable
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		private StorageConfiguration                  configuration                ;
		private StorageOperationController.Creator    operationControllerCreator   ;
		private StorageInitialDataFileNumberProvider  initialDataFileNumberProvider;
		private StorageRequestAcceptor.Creator        requestAcceptorCreator       ;
		private StorageTaskBroker.Creator             taskBrokerCreator            ;
		private StorageDataChunkValidator.Provider    dataChunkValidatorProvider   ;
		private StorageDataChunkValidator.Provider2   dataChunkValidatorProvider2  ;
		private StorageChannelsCreator                channelCreator               ;
		private StorageChannelThreadProvider          channelThreadProvider        ;
		private StorageBackupThreadProvider           backupThreadProvider         ;
		private StorageThreadProvider                 threadProvider               ;
		private StorageRequestTaskCreator             requestTaskCreator           ;
		private StorageTypeDictionary                 typeDictionary               ;
		private StorageRootTypeIdProvider             rootTypeIdProvider           ;
		private StorageTimestampProvider              timestampProvider            ;
		private StorageObjectIdRangeEvaluator         objectIdRangeEvaluator       ;
		private StorageFileReader.Provider            readerProvider               ;
		private StorageFileWriter.Provider            writerProvider               ;
		private StorageGCZombieOidHandler             gCZombieOidHandler           ;
		private StorageRootOidSelector.Provider       rootOidSelectorProvider      ;
		private StorageOidMarkQueue.Creator           oidMarkQueueCreator          ;
		private StorageEntityMarkMonitor.Creator      entityMarkMonitorCreator     ;
		private StorageDataFileValidator.Creator      dataFileValidatorCreator     ;
		private BinaryEntityRawDataIterator.Provider  entityDataIteratorProvider   ;
		private StorageEntityDataValidator.Creator    entityDataValidatorCreator   ;
		private StorageExceptionHandler               exceptionHandler             ;

		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		public Implementation()
		{
			super();
		}

		

		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		@SuppressWarnings("unchecked") // magic self-type.
		protected final F $()
		{
			return (F)this;
		}
		
		

		protected StorageGCZombieOidHandler ensureStorageGCZombieOidHandler()
		{
			return new StorageGCZombieOidHandler.Implementation();
		}

		protected StorageConfiguration ensureConfiguration()
		{
			return Storage.Configuration();
		}
		
		protected StorageOperationController.Creator ensureOperationControllerCreator()
		{
			return StorageOperationController.Provider();
		}
		
		

		protected StorageInitialDataFileNumberProvider ensureInitialDataFileNumberProvider()
		{
			return new StorageInitialDataFileNumberProvider.Implementation(1); // constant 1 by default
		}

		protected StorageDataFileEvaluator ensureStorageConfiguration()
		{
			return this.getConfiguration().fileEvaluator();
		}

		protected StorageRequestAcceptor.Creator ensureRequestAcceptorCreator()
		{
			return new StorageRequestAcceptor.Creator.Implementation();
		}

		protected StorageTaskBroker.Creator ensureTaskBrokerCreator()
		{
			return new StorageTaskBroker.Creator.Default();
		}

		protected StorageDataChunkValidator.Provider ensureDataChunkValidatorProvider()
		{
			return getDataChunkValidatorProvider2().provideDataChunkValidatorProvider(this);
		}

		protected StorageDataChunkValidator.Provider2 ensureDataChunkValidatorProvider2()
		{
			return new StorageDataChunkValidator.NoOp();
		}

		protected StorageChannelsCreator ensureChannelCreator()
		{
			return new StorageChannelsCreator.Implementation();
		}

		protected StorageChannelThreadProvider ensureChannelThreadProvider()
		{
			return new StorageChannelThreadProvider.Implementation();
		}
		
		protected StorageBackupThreadProvider ensureBackupThreadProvider()
		{
			return StorageBackupThreadProvider.New();
		}
		
		protected StorageThreadProvider ensureThreadProvider()
		{
			return StorageThreadProvider.New(
				this.getChannelThreadProvider(),
				this.getBackupThreadProvider()
			);
		}

		protected StorageRequestTaskCreator ensureRequestTaskCreator()
		{
			return new StorageRequestTaskCreator.Implementation(
				this.getTimestampProvider()
			);
		}

		protected StorageTypeDictionary ensureTypeDictionary()
		{
			return new StorageTypeDictionary.Implementation(this.isByteOrderMismatch());
		}

		protected StorageChannelCountProvider ensureChannelCountProvider(final int channelCount)
		{
			return new StorageChannelCountProvider.Implementation(channelCount);
		}

		protected StorageRootTypeIdProvider ensureRootTypeIdProvider()
		{
			throw new MissingFoundationPartException(StorageRootTypeIdProvider.class);
		}

		protected StorageTimestampProvider ensureTimestampProvider()
		{
			return new StorageTimestampProvider.Implementation();
		}

		protected StorageObjectIdRangeEvaluator ensureObjectIdRangeEvaluator()
		{
			return new StorageObjectIdRangeEvaluator.Implementation();
		}

		protected StorageFileReader.Provider ensureReaderProvider()
		{
			return new StorageFileReader.Provider.Implementation();
		}

		protected StorageFileWriter.Provider ensureWriterProvider()
		{
			return new StorageFileWriter.Provider.Implementation();
		}

		protected StorageRootOidSelector.Provider ensureRootOidSelectorProvider()
		{
			return new StorageRootOidSelector.Provider.Implementation();
		}

		protected StorageOidMarkQueue.Creator ensureOidMarkQueueCreator()
		{
			return new StorageOidMarkQueue.Creator.Implementation();
		}

		protected StorageEntityMarkMonitor.Creator ensureEntityMarkMonitorCreator()
		{
			return new StorageEntityMarkMonitor.Creator.Implementation();
		}

		protected StorageDataFileValidator.Creator ensureDataFileValidatorCreator()
		{
			return StorageDataFileValidator.Creator(
				this.getEntityDataIteratorProvider(),
				this.getEntityDataValidatorCreator()
			);
		}
		
		protected BinaryEntityRawDataIterator.Provider ensureEntityDataIteratorProvider()
		{
			return BinaryEntityRawDataIterator.Provider();
		}
		
		protected StorageEntityDataValidator.Creator ensureEntityDataValidatorCreator()
		{
			return StorageEntityDataValidator.Creator();
		}
		
		
		
		

		protected StorageExceptionHandler ensureExceptionHandler()
		{
			return new StorageExceptionHandler.Implementation();
		}
		
		protected ByteOrder ensureTargetByteOrder()
		{
			return ByteOrder.nativeOrder();
		}
		
		
		

		@Override
		public StorageOperationController.Creator getOperationControllerCreator()
		{
			if(this.operationControllerCreator == null)
			{
				this.operationControllerCreator = this.dispatch(this.ensureOperationControllerCreator());
			}
			return this.operationControllerCreator;
		}

		@Override
		public StorageInitialDataFileNumberProvider getInitialDataFileNumberProvider()
		{
			if(this.initialDataFileNumberProvider == null)
			{
				this.initialDataFileNumberProvider = this.dispatch(this.ensureInitialDataFileNumberProvider());
			}
			return this.initialDataFileNumberProvider;
		}

		@Override
		public StorageRequestAcceptor.Creator getRequestAcceptorCreator()
		{
			if(this.requestAcceptorCreator == null)
			{
				this.requestAcceptorCreator = this.dispatch(this.ensureRequestAcceptorCreator());
			}
			return this.requestAcceptorCreator;
		}

		@Override
		public StorageTaskBroker.Creator getTaskBrokerCreator()
		{
			if(this.taskBrokerCreator == null)
			{
				this.taskBrokerCreator = this.dispatch(this.ensureTaskBrokerCreator());
			}
			return this.taskBrokerCreator;
		}

		@Override
		public StorageDataChunkValidator.Provider getDataChunkValidatorProvider()
		{
			if(this.dataChunkValidatorProvider == null)
			{
				this.dataChunkValidatorProvider = this.dispatch(this.ensureDataChunkValidatorProvider());
			}
			return this.dataChunkValidatorProvider;
		}
		
		@Override
		public Provider2 getDataChunkValidatorProvider2()
		{
			if(this.dataChunkValidatorProvider2 == null)
			{
				this.dataChunkValidatorProvider2 = this.dispatch(this.ensureDataChunkValidatorProvider2());
			}
			return this.dataChunkValidatorProvider2;
		}

		@Override
		public StorageChannelsCreator getChannelCreator()
		{
			if(this.channelCreator == null)
			{
				this.channelCreator = this.dispatch(this.ensureChannelCreator());
			}
			return this.channelCreator;
		}

		@Override
		public StorageChannelThreadProvider getChannelThreadProvider()
		{
			if(this.channelThreadProvider == null)
			{
				this.channelThreadProvider = this.dispatch(this.ensureChannelThreadProvider());
			}
			return this.channelThreadProvider;
		}
		
		@Override
		public StorageBackupThreadProvider getBackupThreadProvider()
		{
			if(this.backupThreadProvider == null)
			{
				this.backupThreadProvider = this.dispatch(this.ensureBackupThreadProvider());
			}
			return this.backupThreadProvider;
		}
		
		@Override
		public StorageThreadProvider getThreadProvider()
		{
			if(this.threadProvider == null)
			{
				this.threadProvider = this.dispatch(this.ensureThreadProvider());
			}
			return this.threadProvider;
		}

		@Override
		public StorageRequestTaskCreator getRequestTaskCreator()
		{
			if(this.requestTaskCreator == null)
			{
				this.requestTaskCreator = this.dispatch(this.ensureRequestTaskCreator());
			}
			return this.requestTaskCreator;
		}

		@Override
		public StorageTypeDictionary getTypeDictionary()
		{
			if(this.typeDictionary == null)
			{
				this.typeDictionary = this.dispatch(this.ensureTypeDictionary());
			}
			return this.typeDictionary;
		}

		@Override
		public StorageRootTypeIdProvider getRootTypeIdProvider()
		{
			if(this.rootTypeIdProvider == null)
			{
				this.rootTypeIdProvider = this.dispatch(this.ensureRootTypeIdProvider());
			}
			return this.rootTypeIdProvider;
		}

		@Override
		public StorageConfiguration getConfiguration()
		{
			if(this.configuration == null)
			{
				this.configuration = this.dispatch(this.ensureConfiguration());
			}
			return this.configuration;
		}

		@Override
		public StorageTimestampProvider getTimestampProvider()
		{
			if(this.timestampProvider == null)
			{
				this.timestampProvider = this.dispatch(this.ensureTimestampProvider());
			}
			return this.timestampProvider;
		}

		@Override
		public StorageObjectIdRangeEvaluator getObjectIdRangeEvaluator()
		{
			if(this.objectIdRangeEvaluator == null)
			{
				this.objectIdRangeEvaluator = this.dispatch(this.ensureObjectIdRangeEvaluator());
			}
			return this.objectIdRangeEvaluator;
		}

		@Override
		public StorageFileWriter.Provider getWriterProvider()
		{
			if(this.writerProvider == null)
			{
				this.writerProvider = this.dispatch(this.ensureWriterProvider());
			}
			return this.writerProvider;
		}

		@Override
		public StorageFileReader.Provider getReaderProvider()
		{
			if(this.readerProvider == null)
			{
				this.readerProvider = this.dispatch(this.ensureReaderProvider());
			}
			return this.readerProvider;
		}

		@Override
		public StorageGCZombieOidHandler getGCZombieOidHandler()
		{
			if(this.gCZombieOidHandler == null)
			{
				this.gCZombieOidHandler = this.dispatch(this.ensureStorageGCZombieOidHandler());
			}
			return this.gCZombieOidHandler;
		}

		@Override
		public StorageRootOidSelector.Provider getRootOidSelectorProvider()
		{
			if(this.rootOidSelectorProvider == null)
			{
				this.rootOidSelectorProvider = this.dispatch(this.ensureRootOidSelectorProvider());
			}
			return this.rootOidSelectorProvider;
		}

		@Override
		public StorageOidMarkQueue.Creator getOidMarkQueueCreator()
		{
			if(this.oidMarkQueueCreator == null)
			{
				this.oidMarkQueueCreator = this.dispatch(this.ensureOidMarkQueueCreator());
			}
			return this.oidMarkQueueCreator;
		}

		@Override
		public StorageEntityMarkMonitor.Creator getEntityMarkMonitorCreator()
		{
			if(this.entityMarkMonitorCreator == null)
			{
				this.entityMarkMonitorCreator = this.dispatch(this.ensureEntityMarkMonitorCreator());
			}
			return this.entityMarkMonitorCreator;
		}
		
		@Override
		public StorageDataFileValidator.Creator getDataFileValidatorCreator()
		{
			if(this.dataFileValidatorCreator == null)
			{
				this.dataFileValidatorCreator = this.dispatch(this.ensureDataFileValidatorCreator());
			}
			return this.dataFileValidatorCreator;
		}
		
		@Override
		public BinaryEntityRawDataIterator.Provider getEntityDataIteratorProvider()
		{
			if(this.entityDataIteratorProvider == null)
			{
				this.entityDataIteratorProvider = this.dispatch(this.ensureEntityDataIteratorProvider());
			}
			return this.entityDataIteratorProvider;
		}
		
		@Override
		public StorageEntityDataValidator.Creator getEntityDataValidatorCreator()
		{
			if(this.entityDataValidatorCreator == null)
			{
				this.entityDataValidatorCreator = this.dispatch(this.ensureEntityDataValidatorCreator());
			}
			return this.entityDataValidatorCreator;
		}

		@Override
		public StorageExceptionHandler getExceptionHandler()
		{
			if(this.exceptionHandler == null)
			{
				this.exceptionHandler = this.dispatch(this.ensureExceptionHandler());
			}
			return this.exceptionHandler;
		}

		
		
		@Override
		public F setOperationControllerCreator(
			final StorageOperationController.Creator operationControllerCreator
		)
		{
			this.operationControllerCreator = operationControllerCreator;
			return this.$();
		}
		
		@Override
		public F setInitialDataFileNumberProvider(
			final StorageInitialDataFileNumberProvider initialDataFileNumberProvider
		)
		{
			this.initialDataFileNumberProvider = initialDataFileNumberProvider;
			return this.$();
		}

		@Override
		public F setRequestAcceptorCreator(
			final StorageRequestAcceptor.Creator requestAcceptorCreator
		)
		{
			this.requestAcceptorCreator = requestAcceptorCreator;
			return this.$();
		}

		@Override
		public F setTaskBrokerCreator(final StorageTaskBroker.Creator taskBrokerCreator)
		{
			this.taskBrokerCreator = taskBrokerCreator;
			return this.$();
		}

		@Override
		public F setDataChunkValidatorProvider(
			final StorageDataChunkValidator.Provider dataChunkValidatorProvider
		)
		{
			this.dataChunkValidatorProvider = dataChunkValidatorProvider;
			return this.$();
		}
		
		@Override
		public F setDataChunkValidatorProvider2(
			final StorageDataChunkValidator.Provider2 dataChunkValidatorProvider2
		)
		{
			this.dataChunkValidatorProvider2 = dataChunkValidatorProvider2;
			return this.$();
		}

		@Override
		public F setChannelCreator(final StorageChannelsCreator channelCreator)
		{
			this.channelCreator = channelCreator;
			return this.$();
		}

		@Override
		public F setChannelThreadProvider(final StorageChannelThreadProvider channelThreadProvider)
		{
			this.channelThreadProvider = channelThreadProvider;
			return this.$();
		}
		
		@Override
		public F setBackupThreadProvider(final StorageBackupThreadProvider backupThreadProvider)
		{
			this.backupThreadProvider = backupThreadProvider;
			return this.$();
		}
		
		@Override
		public F setThreadProvider(final StorageThreadProvider threadProvider)
		{
			this.threadProvider = threadProvider;
			return this.$();
		}

		@Override
		public F setTaskCreator(final StorageRequestTaskCreator taskCreator)
		{
			this.requestTaskCreator = taskCreator;
			return this.$();
		}

		@Override
		public F setTypeDictionary(final StorageTypeDictionary typeDictionary)
		{
			this.typeDictionary = typeDictionary;
			return this.$();
		}

		@Override
		public F setRootTypeIdProvider(final StorageRootTypeIdProvider rootTypeIdProvider)
		{
			this.rootTypeIdProvider = rootTypeIdProvider;
			return this.$();
		}

		@Override
		public F setConfiguration(final StorageConfiguration configuration)
		{
			this.configuration = configuration;
			return this.$();
		}

		@Override
		public F setTimestampProvider(
			final StorageTimestampProvider timestampProvider
		)
		{
			this.timestampProvider = timestampProvider;
			return this.$();
		}

		@Override
		public F setObjectIdRangeEvaluator(
			final StorageObjectIdRangeEvaluator objectIdRangeEvaluator
		)
		{
			this.objectIdRangeEvaluator = objectIdRangeEvaluator;
			return this.$();
		}

		@Override
		public F setReaderProvider(final StorageFileReader.Provider readerProvider)
		{
			this.readerProvider = readerProvider;
			return this.$();
		}

		@Override
		public F setWriterProvider(final Provider writerProvider)
		{
			this.writerProvider = writerProvider;
			return this.$();
		}

		@Override
		public F setGCZombieOidHandler(final StorageGCZombieOidHandler gCZombieOidHandler)
		{
			this.gCZombieOidHandler = gCZombieOidHandler;
			return this.$();
		}

		@Override
		public F setRootOidSelectorProvider(
			final StorageRootOidSelector.Provider rootOidSelectorProvider
		)
		{
			this.rootOidSelectorProvider = rootOidSelectorProvider;
			return this.$();
		}

		@Override
		public F setOidMarkQueueCreator(final StorageOidMarkQueue.Creator oidMarkQueueCreator)
		{
			this.oidMarkQueueCreator = oidMarkQueueCreator;
			return this.$();
		}

		@Override
		public F setEntityMarkMonitorCreator(final StorageEntityMarkMonitor.Creator entityMarkMonitorCreator)
		{
			this.entityMarkMonitorCreator = entityMarkMonitorCreator;
			return this.$();
		}
		
		@Override
		public F setDataFileValidatorCreator(final StorageDataFileValidator.Creator dataFileValidatorCreator)
		{
			this.dataFileValidatorCreator = dataFileValidatorCreator;
			return this.$();
		}
		
		@Override
		public F setEntityDataIteratorProvider(final BinaryEntityRawDataIterator.Provider entityDataIteratorProvider)
		{
			this.entityDataIteratorProvider = entityDataIteratorProvider;
			return this.$();
		}
		
		@Override
		public F setEntityDataValidatorCreator(final StorageEntityDataValidator.Creator entityDataValidatorCreator)
		{
			this.entityDataValidatorCreator = entityDataValidatorCreator;
			return this.$();
		}

		@Override
		public F setExceptionHandler(final StorageExceptionHandler exceptionHandler)
		{
			this.exceptionHandler = exceptionHandler;
			return this.$();
		}
		
		public final boolean isByteOrderMismatch()
		{
			/* (11.02.2019 TM)NOTE: On byte order switching:
			 * Theoreticaly, the storage engine (OGS) could also use the switchByteOrder mechanism implemented for
			 * communication (OGC). However, there are a lot stumbling blocks in the details that are currently not
			 * worth resolving for a feature that is most probably never required in the foreseeable future.
			 * See StorageEntityCache$Implementation#putEntity
			 */
			return false;
		}
		
		@Override
		public StorageManager createStorageManager()
		{
			/* (11.02.2019 TM)NOTE: On byte order switching:
			 * Theoreticaly, the storage engine (OGS) could use the switchByteOrder mechanism implemented for
			 * communiction (OGC). However, there are a lot stumbling blocks involved in the details that
			 * are currently not worth resolving for a feature that is most probably never required in the
			 * foreseeable future.
			 * See StorageEntityCache$Implementation#putEntity
			 */
						
			return new StorageManager.Implementation(
				this.getConfiguration()                ,
				this.getOperationControllerCreator()   ,
				this.getDataFileValidatorCreator()     ,
				this.getWriterProvider()               ,
				this.getReaderProvider()               ,
				this.getInitialDataFileNumberProvider(),
				this.getRequestAcceptorCreator()       ,
				this.getTaskBrokerCreator()            ,
				this.getDataChunkValidatorProvider()   ,
				this.getChannelCreator()               ,
				this.getThreadProvider()               ,
				this.getRequestTaskCreator()           ,
				this.getTypeDictionary()               ,
				this.getRootTypeIdProvider()           ,
				this.getTimestampProvider()            ,
				this.getObjectIdRangeEvaluator()       ,
				this.getGCZombieOidHandler()           ,
				this.getRootOidSelectorProvider()      ,
				this.getOidMarkQueueCreator()          ,
				this.getEntityMarkMonitorCreator()     ,
				this.isByteOrderMismatch()             ,
				this.getExceptionHandler()
			);
		}

	}

}

package net.jadoth.storage.types;

import net.jadoth.exceptions.MissingFoundationPartException;
import net.jadoth.persistence.types.Unpersistable;
import net.jadoth.storage.types.StorageFileWriter.Provider;
import net.jadoth.util.InstanceDispatcher;

public interface StorageFoundation<F extends StorageFoundation<?>>
{
	public StorageInitialDataFileNumberProvider getInitialDataFileNumberProvider();

	public StorageRequestAcceptor.Creator getRequestAcceptorCreator();

	public StorageTaskBroker.Creator getTaskBrokerCreator();

	public StorageValidatorDataChunk.Provider getDataChunkValidatorProvider();

	public StorageChannel.Creator getChannelCreator();

	public StorageThreadProvider getThreadProvider();

	public StorageRequestTaskCreator getRequestTaskCreator();

	public StorageTypeDictionary getTypeDictionary();

	public StorageRootTypeIdProvider getRootTypeIdProvider();

	public StorageConfiguration getConfiguration();

	public StorageTimestampProvider getTimestampProvider();

	public StorageObjectIdRangeEvaluator getObjectIdRangeEvaluator();

	public StorageFileReader.Provider getReaderProvider();

	public StorageFileWriter.Provider getWriterProvider();

	public StorageWriteListener.Provider getWriteListenerProvider();

	public StorageGCZombieOidHandler getGCZombieOidHandler();

	public StorageRootOidSelector.Provider getRootOidSelectorProvider();

	public StorageOidMarkQueue.Creator getOidMarkQueueCreator();

	public StorageEntityMarkMonitor.Creator getEntityMarkMonitorCreator();

	public StorageExceptionHandler getExceptionHandler();



	public F setInitialDataFileNumberProvider(StorageInitialDataFileNumberProvider initDataFileNumberProvider);

	public F setRequestAcceptorCreator(StorageRequestAcceptor.Creator requestAcceptorCreator);

	public F setTaskBrokerCreator(StorageTaskBroker.Creator taskBrokerCreator);

	public F setDataChunkValidatorProvider(StorageValidatorDataChunk.Provider chunkValidatorProvider);

	public F setChannelCreator(StorageChannel.Creator channelCreator);

	public F setThreadProvider(StorageThreadProvider threadProvider);

	public F setTaskCreator(StorageRequestTaskCreator taskCreator);

	public F setTypeDictionary(StorageTypeDictionary typeDictionary);

	public F setRootTypeIdProvider(StorageRootTypeIdProvider rootTypeIdProvider);

	public F setConfiguration(StorageConfiguration configuration);

	public F setTimestampProvider(StorageTimestampProvider timestampProvider);

	public F setObjectIdRangeEvaluator(StorageObjectIdRangeEvaluator objectIdRangeEvaluator);

	public F setReaderProvider(StorageFileReader.Provider readerProvider);

	public F setWriterProvider(StorageFileWriter.Provider writerProvider);

	public F setWriteListenerProvider(StorageWriteListener.Provider writeListenerProvider);

	public F setGCZombieOidHandler(StorageGCZombieOidHandler gCZombieOidHandler);

	public F setRootOidSelectorProvider(StorageRootOidSelector.Provider rootOidSelectorProvider);

	public F setExceptionHandler(StorageExceptionHandler exceptionHandler);

	public F setOidMarkQueueCreator(StorageOidMarkQueue.Creator oidMarkQueueCreator);

	public F setEntityMarkMonitorCreator(StorageEntityMarkMonitor.Creator entityMarkMonitorCreator);

	public default boolean isByteOrderMismatch()
	{
		// storage currently simply assumes always direct byte order.
		return false;
	}

	public StorageManager createStorageManager();



	public class Implementation<F extends StorageFoundation.Implementation<?>>
	extends InstanceDispatcher.Implementation
	implements StorageFoundation<F>, Unpersistable
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		private StorageConfiguration                  configuration                ;
		private StorageInitialDataFileNumberProvider  initialDataFileNumberProvider;
		private StorageRequestAcceptor.Creator        requestAcceptorCreator       ;
		private StorageTaskBroker.Creator             taskBrokerCreator            ;
		private StorageValidatorDataChunk.Provider    dataChunkValidatorProvider   ;
		private StorageChannel.Creator                channelCreator               ;
		private StorageThreadProvider                 threadProvider               ;
		private StorageRequestTaskCreator             requestTaskCreator           ;
		private StorageTypeDictionary                 typeDictionary               ;
		private StorageRootTypeIdProvider             rootTypeIdProvider           ;
		private StorageTimestampProvider              timestampProvider            ;
		private StorageObjectIdRangeEvaluator         objectIdRangeEvaluator       ;
		private StorageFileReader.Provider            readerProvider               ;
		private StorageFileWriter.Provider            writerProvider               ;
		private StorageWriteListener.Provider         writeListenerProvider        ;
		private StorageGCZombieOidHandler             gCZombieOidHandler           ;
		private StorageRootOidSelector.Provider       rootOidSelectorProvider      ;
		private StorageOidMarkQueue.Creator           oidMarkQueueCreator          ;
		private StorageEntityMarkMonitor.Creator      entityMarkMonitorCreator     ;
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
		
		// (14.12.2018 TM)XXX: rename all create~ to ensure~

		protected StorageGCZombieOidHandler createStorageGCZombieOidHandler()
		{
			return new StorageGCZombieOidHandler.Implementation();
		}

		protected StorageConfiguration createConfiguration()
		{
			return Storage.Configuration();
		}

		protected StorageInitialDataFileNumberProvider createInitialDataFileNumberProvider()
		{
			return new StorageInitialDataFileNumberProvider.Implementation(1); // constant 1 by default
		}

		protected StorageDataFileEvaluator createStorageConfiguration()
		{
			return this.getConfiguration().fileEvaluator();
		}

		protected StorageRequestAcceptor.Creator createRequestAcceptorCreator()
		{
			return new StorageRequestAcceptor.Creator.Implementation();
		}

		protected StorageTaskBroker.Creator createTaskBrokerCreator()
		{
			return new StorageTaskBroker.Creator.Implementation();
		}

		protected StorageValidatorDataChunk.Provider createDataChunkValidatorProvider()
		{
			return new StorageValidatorDataChunk.NoOp();
		}

		protected StorageChannel.Creator createChannelCreator()
		{
			return new StorageChannel.Creator.Implementation();
		}

		protected StorageThreadProvider createThreadProvider()
		{
			return new StorageThreadProvider.Implementation();
		}

		protected StorageRequestTaskCreator createRequestTaskCreator()
		{
			return new StorageRequestTaskCreator.Implementation(
				this.getTimestampProvider()
			);
		}

		protected StorageTypeDictionary createTypeDictionary()
		{
			return new StorageTypeDictionary.Implementation(this.isByteOrderMismatch());
		}

		protected StorageChannelCountProvider createChannelCountProvider(final int channelCount)
		{
			return new StorageChannelCountProvider.Implementation(channelCount);
		}

		protected StorageRootTypeIdProvider createRootTypeIdProvider()
		{
			throw new MissingFoundationPartException(StorageRootTypeIdProvider.class);
		}

		protected StorageTimestampProvider createTimestampProvider()
		{
			return new StorageTimestampProvider.Implementation();
		}

		protected StorageObjectIdRangeEvaluator createObjectIdRangeEvaluator()
		{
			return new StorageObjectIdRangeEvaluator.Implementation();
		}

		protected StorageFileReader.Provider createReaderProvider()
		{
			return new StorageFileReader.Provider.Implementation();
		}

		protected StorageFileWriter.Provider createWriterProvider()
		{
			return new StorageFileWriter.Provider.Implementation();
		}

		protected StorageWriteListener.Provider createWriteListenerProvider()
		{
			return new StorageWriteListener.Provider.Implementation();
		}

		protected StorageRootOidSelector.Provider createRootOidSelectorProvider()
		{
			return new StorageRootOidSelector.Provider.Implementation();
		}

		protected StorageOidMarkQueue.Creator createOidMarkQueueCreator()
		{
			return new StorageOidMarkQueue.Creator.Implementation();
		}

		protected StorageEntityMarkMonitor.Creator createEntityMarkMonitorCreator()
		{
			return new StorageEntityMarkMonitor.Creator.Implementation();
		}

		protected StorageExceptionHandler createExceptionHandler()
		{
			return new StorageExceptionHandler.Implementation();
		}
		
		

		@Override
		public StorageInitialDataFileNumberProvider getInitialDataFileNumberProvider()
		{
			if(this.initialDataFileNumberProvider == null)
			{
				this.initialDataFileNumberProvider = this.dispatch(this.createInitialDataFileNumberProvider());
			}
			return this.initialDataFileNumberProvider;
		}

		@Override
		public StorageRequestAcceptor.Creator getRequestAcceptorCreator()
		{
			if(this.requestAcceptorCreator == null)
			{
				this.requestAcceptorCreator = this.dispatch(this.createRequestAcceptorCreator());
			}
			return this.requestAcceptorCreator;
		}

		@Override
		public StorageTaskBroker.Creator getTaskBrokerCreator()
		{
			if(this.taskBrokerCreator == null)
			{
				this.taskBrokerCreator = this.dispatch(this.createTaskBrokerCreator());
			}
			return this.taskBrokerCreator;
		}

		@Override
		public StorageValidatorDataChunk.Provider getDataChunkValidatorProvider()
		{
			if(this.dataChunkValidatorProvider == null)
			{
				this.dataChunkValidatorProvider = this.dispatch(this.createDataChunkValidatorProvider());
			}
			return this.dataChunkValidatorProvider;
		}

		@Override
		public StorageChannel.Creator getChannelCreator()
		{
			if(this.channelCreator == null)
			{
				this.channelCreator = this.dispatch(this.createChannelCreator());
			}
			return this.channelCreator;
		}

		@Override
		public StorageThreadProvider getThreadProvider()
		{
			if(this.threadProvider == null)
			{
				this.threadProvider = this.dispatch(this.createThreadProvider());
			}
			return this.threadProvider;
		}

		@Override
		public StorageRequestTaskCreator getRequestTaskCreator()
		{
			if(this.requestTaskCreator == null)
			{
				this.requestTaskCreator = this.dispatch(this.createRequestTaskCreator());
			}
			return this.requestTaskCreator;
		}

		@Override
		public StorageTypeDictionary getTypeDictionary()
		{
			if(this.typeDictionary == null)
			{
				this.typeDictionary = this.dispatch(this.createTypeDictionary());
			}
			return this.typeDictionary;
		}

		@Override
		public StorageRootTypeIdProvider getRootTypeIdProvider()
		{
			if(this.rootTypeIdProvider == null)
			{
				this.rootTypeIdProvider = this.dispatch(this.createRootTypeIdProvider());
			}
			return this.rootTypeIdProvider;
		}

		@Override
		public StorageConfiguration getConfiguration()
		{
			if(this.configuration == null)
			{
				this.configuration = this.dispatch(this.createConfiguration());
			}
			return this.configuration;
		}

		@Override
		public StorageTimestampProvider getTimestampProvider()
		{
			if(this.timestampProvider == null)
			{
				this.timestampProvider = this.dispatch(this.createTimestampProvider());
			}
			return this.timestampProvider;
		}

		@Override
		public StorageObjectIdRangeEvaluator getObjectIdRangeEvaluator()
		{
			if(this.objectIdRangeEvaluator == null)
			{
				this.objectIdRangeEvaluator = this.dispatch(this.createObjectIdRangeEvaluator());
			}
			return this.objectIdRangeEvaluator;
		}

		@Override
		public StorageFileWriter.Provider getWriterProvider()
		{
			if(this.writerProvider == null)
			{
				this.writerProvider = this.dispatch(this.createWriterProvider());
			}
			return this.writerProvider;
		}

		@Override
		public StorageFileReader.Provider getReaderProvider()
		{
			if(this.readerProvider == null)
			{
				this.readerProvider = this.dispatch(this.createReaderProvider());
			}
			return this.readerProvider;
		}

		@Override
		public StorageWriteListener.Provider getWriteListenerProvider()
		{
			if(this.writeListenerProvider == null)
			{
				this.writeListenerProvider = this.dispatch(this.createWriteListenerProvider());
			}
			return this.writeListenerProvider;
		}

		@Override
		public StorageGCZombieOidHandler getGCZombieOidHandler()
		{
			if(this.gCZombieOidHandler == null)
			{
				this.gCZombieOidHandler = this.dispatch(this.createStorageGCZombieOidHandler());
			}
			return this.gCZombieOidHandler;
		}

		@Override
		public StorageRootOidSelector.Provider getRootOidSelectorProvider()
		{
			if(this.rootOidSelectorProvider == null)
			{
				this.rootOidSelectorProvider = this.dispatch(this.createRootOidSelectorProvider());
			}
			return this.rootOidSelectorProvider;
		}

		@Override
		public StorageOidMarkQueue.Creator getOidMarkQueueCreator()
		{
			if(this.oidMarkQueueCreator == null)
			{
				this.oidMarkQueueCreator = this.dispatch(this.createOidMarkQueueCreator());
			}
			return this.oidMarkQueueCreator;
		}

		@Override
		public StorageEntityMarkMonitor.Creator getEntityMarkMonitorCreator()
		{
			if(this.entityMarkMonitorCreator == null)
			{
				this.entityMarkMonitorCreator = this.dispatch(this.createEntityMarkMonitorCreator());
			}
			return this.entityMarkMonitorCreator;
		}

		@Override
		public StorageExceptionHandler getExceptionHandler()
		{
			if(this.exceptionHandler == null)
			{
				this.exceptionHandler = this.dispatch(this.createExceptionHandler());
			}
			return this.exceptionHandler;
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
			final StorageValidatorDataChunk.Provider dataChunkValidatorProvider
		)
		{
			this.dataChunkValidatorProvider = dataChunkValidatorProvider;
			return this.$();
		}

		@Override
		public F setChannelCreator(final StorageChannel.Creator channelCreator)
		{
			this.channelCreator = channelCreator;
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
		public F setWriteListenerProvider(
			final StorageWriteListener.Provider writeListenerProvider
		)
		{
			this.writeListenerProvider = writeListenerProvider;
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
		public F setExceptionHandler(final StorageExceptionHandler exceptionHandler)
		{
			this.exceptionHandler = exceptionHandler;
			return this.$();
		}

		@Override
		public StorageManager createStorageManager()
		{
			return new StorageManager.Implementation(
				this.getConfiguration()                ,
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
				this.getReaderProvider()               ,
				this.getWriterProvider()               ,
				this.getWriteListenerProvider()        ,
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

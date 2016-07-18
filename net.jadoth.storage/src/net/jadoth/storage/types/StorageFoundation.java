package net.jadoth.storage.types;

import net.jadoth.storage.types.StorageFileWriter.Provider;
import net.jadoth.util.AbstractInstanceDispatcher;
import net.jadoth.util.MissingAssemblyPartException;

public interface StorageFoundation
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

	public StorageValidRootIdCalculator.Provider getValidRootIdCalculatorProvider();

	public StorageGCZombieOidHandler getGCZombieOidHandler();

	public StorageRootOidSelector.Provider getRootOidSelectorProvider();

	public StorageOidMarkQueue.Creator getOidMarkQueueCreator();

	public StorageEntityMarkMonitor.Creator getEntityMarkMonitorCreator();

	public StorageExceptionHandler getExceptionHandler();



	public StorageFoundation setInitialDataFileNumberProvider(StorageInitialDataFileNumberProvider initDataFileNumberProvider);

	public StorageFoundation setRequestAcceptorCreator(StorageRequestAcceptor.Creator requestAcceptorCreator);

	public StorageFoundation setTaskBrokerCreator(StorageTaskBroker.Creator taskBrokerCreator);

	public StorageFoundation setDataChunkValidatorProvider(StorageValidatorDataChunk.Provider chunkValidatorProvider);

	public StorageFoundation setChannelCreator(StorageChannel.Creator channelCreator);

	public StorageFoundation setThreadProvider(StorageThreadProvider threadProvider);

	public StorageFoundation setTaskCreator(StorageRequestTaskCreator taskCreator);

	public StorageFoundation setTypeDictionary(StorageTypeDictionary typeDictionary);

	public StorageFoundation setRootTypeIdProvider(StorageRootTypeIdProvider rootTypeIdProvider);

	public StorageFoundation setConfiguration(StorageConfiguration configuration);

	public StorageFoundation setStorageTimestampProvider(StorageTimestampProvider storageTimestampProvider);

	public StorageFoundation setObjectIdRangeEvaluator(StorageObjectIdRangeEvaluator objectIdRangeEvaluator);

	public StorageFoundation setReaderProvider(StorageFileReader.Provider readerProvider);

	public StorageFoundation setWriterProvider(StorageFileWriter.Provider writerProvider);

	public StorageFoundation setWriteListenerProvider(StorageWriteListener.Provider writeListenerProvider);

	public StorageFoundation setValidRootIdCalculatorProvider(StorageValidRootIdCalculator.Provider validRootIdCalculatorProvider);

	public StorageFoundation setGCZombieOidHandler(StorageGCZombieOidHandler gCZombieOidHandler);

	public StorageFoundation setRootOidSelectorProvider(StorageRootOidSelector.Provider rootOidSelectorProvider);

	public StorageFoundation setExceptionHandler(StorageExceptionHandler exceptionHandler);

	public StorageFoundation setOidMarkQueueCreator(StorageOidMarkQueue.Creator oidMarkQueueCreator);

	public StorageFoundation setEntityMarkMonitorCreator(StorageEntityMarkMonitor.Creator entityMarkMonitorCreator);



	public StorageManager createStorageManager();



	public class Implementation extends AbstractInstanceDispatcher implements StorageFoundation
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
		private StorageValidRootIdCalculator.Provider validRootIdCalculatorProvider;
		private StorageGCZombieOidHandler             gCZombieOidHandler           ;
		private StorageRootOidSelector.Provider       rootOidSelectorProvider      ;
		private StorageOidMarkQueue.Creator           oidMarkQueueCreator          ;
		private StorageEntityMarkMonitor.Creator      entityMarkMonitorCreator     ;
		private StorageExceptionHandler               exceptionHandler             ;



		///////////////////////////////////////////////////////////////////////////
		// declared methods //
		/////////////////////

		protected final void internalSetInitialDataFileNumberProvider(
			final StorageInitialDataFileNumberProvider initialDataFileNumberProvider
		)
		{
			this.initialDataFileNumberProvider = initialDataFileNumberProvider;
		}

		protected final void internalSetRequestAcceptorCreator(
			final StorageRequestAcceptor.Creator requestAcceptorCreator
		)
		{
			this.requestAcceptorCreator = requestAcceptorCreator;
		}

		protected final void internalSetTaskBrokerCreator(final StorageTaskBroker.Creator taskBrokerCreator)
		{
			this.taskBrokerCreator = taskBrokerCreator;
		}

		protected final void internalSetDataChunkValidatorProvider(
			final StorageValidatorDataChunk.Provider dataChunkValidatorProvider
		)
		{
			this.dataChunkValidatorProvider = dataChunkValidatorProvider;
		}

		protected final void internalSetChannelCreator(final StorageChannel.Creator channelCreator)
		{
			this.channelCreator = channelCreator;
		}

		protected final void internalSetThreadProvider(final StorageThreadProvider threadProvider)
		{
			this.threadProvider = threadProvider;
		}

		protected final void internalSetTaskCreator(final StorageRequestTaskCreator taskCreator)
		{
			this.requestTaskCreator = taskCreator;
		}

		protected final void internalSetTypeDictionary(final StorageTypeDictionary typeDictionary)
		{
			this.typeDictionary = typeDictionary;
		}

		protected final void internalSetRootTypeIdProvider(final StorageRootTypeIdProvider rootTypeIdProvider)
		{
			this.rootTypeIdProvider = rootTypeIdProvider;
		}

		protected final void internalSetConfiguration(final StorageConfiguration configuration)
		{
			this.configuration = configuration;
		}

		protected final void internalSetTimestampProvider(final StorageTimestampProvider timestampProvider)
		{
			this.timestampProvider = timestampProvider;
		}

		protected final void internalSetObjectIdRangeEvaluator(
			final StorageObjectIdRangeEvaluator objectIdRangeEvaluator
		)
		{
			this.objectIdRangeEvaluator = objectIdRangeEvaluator;
		}

		protected final void internalSetReaderProvider(final StorageFileReader.Provider readerProvider)
		{
			this.readerProvider = readerProvider;
		}

		protected final void internalSetWriterProvider(final StorageFileWriter.Provider writerProvider)
		{
			this.writerProvider = writerProvider;
		}

		protected final void internalSetWriteListenerProvider(final StorageWriteListener.Provider writeListenerProvider)
		{
			this.writeListenerProvider = writeListenerProvider;
		}

		protected final void internalSetValidRootIdCalculatorProvider(
			final StorageValidRootIdCalculator.Provider validRootIdCalculatorProvider
		)
		{
			this.validRootIdCalculatorProvider = validRootIdCalculatorProvider;
		}

		protected final void internalSetGCZombieOidHandler(
			final StorageGCZombieOidHandler gCZombieOidHandler
		)
		{
			this.gCZombieOidHandler = gCZombieOidHandler;
		}

		protected final void internalSetRootOidSelectorProvider(
			final StorageRootOidSelector.Provider rootOidSelectorProvider
		)
		{
			this.rootOidSelectorProvider = rootOidSelectorProvider;
		}

		protected final void internalSetOidMarkQueueCreator(
			final StorageOidMarkQueue.Creator oidMarkQueueCreator
		)
		{
			this.oidMarkQueueCreator = oidMarkQueueCreator;
		}

		protected final void internalSetEntityMarkMonitorCreator(
			final StorageEntityMarkMonitor.Creator entityMarkMonitorCreator
		)
		{
			this.entityMarkMonitorCreator = entityMarkMonitorCreator;
		}


		protected final void internalSetExceptionHandler(
			final StorageExceptionHandler exceptionHandler
		)
		{
			this.exceptionHandler = exceptionHandler;
		}

		protected StorageValidRootIdCalculator.Provider createValidRootIdCalculatorProvider()
		{
			return new StorageValidRootIdCalculator.Provider.Implementation();
		}

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
			return new StorageTypeDictionary.Implementation();
		}

		protected StorageChannelCountProvider createChannelCountProvider(final int channelCount)
		{
			return new StorageChannelCountProvider.Implementation(channelCount);
		}

		protected StorageRootTypeIdProvider createRootTypeIdProvider()
		{
			throw new MissingAssemblyPartException(StorageRootTypeIdProvider.class);
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
		public StorageValidRootIdCalculator.Provider getValidRootIdCalculatorProvider()
		{
			if(this.validRootIdCalculatorProvider == null)
			{
				this.validRootIdCalculatorProvider = this.dispatch(this.createValidRootIdCalculatorProvider());
			}
			return this.validRootIdCalculatorProvider;
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
		public StorageFoundation setInitialDataFileNumberProvider(
			final StorageInitialDataFileNumberProvider initialDataFileNumberProvider
		)
		{
			this.internalSetInitialDataFileNumberProvider(initialDataFileNumberProvider);
			return this;
		}

		@Override
		public StorageFoundation.Implementation setRequestAcceptorCreator(
			final StorageRequestAcceptor.Creator requestAcceptorCreator
		)
		{
			this.internalSetRequestAcceptorCreator(requestAcceptorCreator);
			return this;
		}

		@Override
		public StorageFoundation.Implementation setTaskBrokerCreator(final StorageTaskBroker.Creator taskBrokerCreator)
		{
			this.internalSetTaskBrokerCreator(taskBrokerCreator);
			return this;
		}

		@Override
		public StorageFoundation.Implementation setDataChunkValidatorProvider(
			final StorageValidatorDataChunk.Provider dataChunkValidatorProvider
		)
		{
			this.internalSetDataChunkValidatorProvider(dataChunkValidatorProvider);
			return this;
		}

		@Override
		public StorageFoundation.Implementation setChannelCreator(final StorageChannel.Creator channelCreator)
		{
			this.internalSetChannelCreator(channelCreator);
			return this;
		}

		@Override
		public StorageFoundation setThreadProvider(final StorageThreadProvider threadProvider)
		{
			this.internalSetThreadProvider(threadProvider);
			return this;
		}

		@Override
		public StorageFoundation.Implementation setTaskCreator(final StorageRequestTaskCreator taskCreator)
		{
			this.internalSetTaskCreator(taskCreator);
			return this;
		}

		@Override
		public StorageFoundation.Implementation setTypeDictionary(final StorageTypeDictionary typeDictionary)
		{
			this.internalSetTypeDictionary(typeDictionary);
			return this;
		}

		@Override
		public StorageFoundation.Implementation setRootTypeIdProvider(final StorageRootTypeIdProvider rootTIDProvider)
		{
			this.internalSetRootTypeIdProvider(rootTIDProvider);
			return this;
		}

		@Override
		public StorageFoundation.Implementation setConfiguration(final StorageConfiguration configuration)
		{
			this.internalSetConfiguration(configuration);
			return this;
		}

		@Override
		public StorageFoundation.Implementation setStorageTimestampProvider(
			final StorageTimestampProvider storageTimestampProvider
		)
		{
			this.internalSetTimestampProvider(storageTimestampProvider);
			return this;
		}

		@Override
		public StorageFoundation.Implementation setObjectIdRangeEvaluator(
			final StorageObjectIdRangeEvaluator objectIdRangeEvaluator
		)
		{
			this.internalSetObjectIdRangeEvaluator(objectIdRangeEvaluator);
			return this;
		}

		@Override
		public StorageFoundation setReaderProvider(final StorageFileReader.Provider readerProvider)
		{
			this.internalSetReaderProvider(readerProvider);
			return this;
		}

		@Override
		public StorageFoundation.Implementation setWriterProvider(final Provider writerProvider)
		{
			this.internalSetWriterProvider(writerProvider);
			return this;
		}

		@Override
		public StorageFoundation.Implementation setWriteListenerProvider(
			final StorageWriteListener.Provider writeListenerProvider
		)
		{
			this.internalSetWriteListenerProvider(writeListenerProvider);
			return this;
		}

		@Override
		public StorageFoundation.Implementation setValidRootIdCalculatorProvider(
			final StorageValidRootIdCalculator.Provider validRootIdCalculatorProvider
		)
		{
			this.internalSetValidRootIdCalculatorProvider(validRootIdCalculatorProvider);
			return this;
		}

		@Override
		public StorageFoundation.Implementation setGCZombieOidHandler(final StorageGCZombieOidHandler gCZombieOidHandler)
		{
			this.internalSetGCZombieOidHandler(gCZombieOidHandler);
			return this;
		}

		@Override
		public StorageFoundation.Implementation setRootOidSelectorProvider(
			final StorageRootOidSelector.Provider rootOidSelectorProvider
		)
		{
			this.internalSetRootOidSelectorProvider(rootOidSelectorProvider);
			return this;
		}

		@Override
		public StorageFoundation.Implementation setOidMarkQueueCreator(final StorageOidMarkQueue.Creator oidMarkQueueCreator)
		{
			this.internalSetOidMarkQueueCreator(oidMarkQueueCreator);
			return this;
		}

		@Override
		public StorageFoundation.Implementation setEntityMarkMonitorCreator(final StorageEntityMarkMonitor.Creator entityMarkMonitorCreator)
		{
			this.internalSetEntityMarkMonitorCreator(entityMarkMonitorCreator);
			return this;
		}

		@Override
		public StorageFoundation setExceptionHandler(final StorageExceptionHandler exceptionHandler)
		{
			this.internalSetExceptionHandler(exceptionHandler);
			return this;
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
				this.getValidRootIdCalculatorProvider(),
				this.getGCZombieOidHandler()           ,
				this.getRootOidSelectorProvider()      ,
				this.getOidMarkQueueCreator()          ,
				this.getEntityMarkMonitorCreator()     ,
				this.getExceptionHandler()
			);
		}

	}

}

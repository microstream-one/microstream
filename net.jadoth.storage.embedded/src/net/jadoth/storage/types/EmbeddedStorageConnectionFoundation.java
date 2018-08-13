package net.jadoth.storage.types;

import net.jadoth.exceptions.MissingFoundationPartException;
import net.jadoth.functional.InstanceDispatcherLogic;
import net.jadoth.persistence.binary.types.Binary;
import net.jadoth.persistence.binary.types.BinaryLoader;
import net.jadoth.persistence.binary.types.BinaryPersistenceFoundation;
import net.jadoth.persistence.binary.types.BinaryStorer;
import net.jadoth.persistence.types.BufferSizeProviderIncremental;
import net.jadoth.persistence.types.Persistence;
import net.jadoth.persistence.types.PersistenceChannel;
import net.jadoth.persistence.types.PersistenceLoader;
import net.jadoth.persistence.types.PersistenceRegisterer;
import net.jadoth.persistence.types.PersistenceRootResolver;
import net.jadoth.persistence.types.PersistenceSource;
import net.jadoth.persistence.types.PersistenceStorer;
import net.jadoth.persistence.types.PersistenceTarget;
import net.jadoth.persistence.types.PersistenceTypeDictionaryAssembler;
import net.jadoth.persistence.types.PersistenceTypeDictionaryExporter;
import net.jadoth.persistence.types.PersistenceTypeDictionaryLoader;
import net.jadoth.persistence.types.PersistenceTypeDictionaryManager;
import net.jadoth.persistence.types.PersistenceTypeDictionaryParser;
import net.jadoth.persistence.types.PersistenceTypeDictionaryProvider;
import net.jadoth.persistence.types.PersistenceTypeDictionaryStorer;
import net.jadoth.persistence.types.PersistenceTypeEvaluator;
import net.jadoth.persistence.types.PersistenceTypeHandlerEnsurer;
import net.jadoth.persistence.types.PersistenceTypeHandlerManager;
import net.jadoth.persistence.types.PersistenceTypeHandlerProvider;
import net.jadoth.persistence.types.PersistenceTypeHandlerRegistry;
import net.jadoth.swizzling.types.SwizzleObjectIdProvider;
import net.jadoth.swizzling.types.SwizzleObjectManager;
import net.jadoth.swizzling.types.SwizzleRegistry;
import net.jadoth.swizzling.types.SwizzleTypeIdProvider;
import net.jadoth.swizzling.types.SwizzleTypeManager;

public interface EmbeddedStorageConnectionFoundation extends BinaryPersistenceFoundation
{
	///////////////////////////////////////////////////////////////////////////
	// getters         //
	/////////////////////

	public StorageManager getStorageManager();



	///////////////////////////////////////////////////////////////////////////
	// setters          //
	/////////////////////

	@Override
	public EmbeddedStorageConnectionFoundation setInstanceDispatcher(InstanceDispatcherLogic instanceDispatcher);

	@Override
	public EmbeddedStorageConnectionFoundation setObjectManager(SwizzleObjectManager objectManager);

	@Override
	public EmbeddedStorageConnectionFoundation setObjectIdProvider(SwizzleObjectIdProvider oidProvider);

	@Override
	public EmbeddedStorageConnectionFoundation setTypeIdProvider(SwizzleTypeIdProvider tidProvider);

	@Override
	public EmbeddedStorageConnectionFoundation setTypeManager(SwizzleTypeManager typeManager);

	@Override
	public EmbeddedStorageConnectionFoundation setSwizzleRegistry(SwizzleRegistry swizzleRegistry);

	@Override
	public EmbeddedStorageConnectionFoundation setRegistererCreator(PersistenceRegisterer.Creator registererCreator);

	@Override
	public EmbeddedStorageConnectionFoundation setTypeDictionaryManager(
		PersistenceTypeDictionaryManager typeDictionaryManager
	);

	@Override
	public EmbeddedStorageConnectionFoundation setTypeDictionaryProvider(
		PersistenceTypeDictionaryProvider typeDictionaryProvider
	);

	@Override
	public EmbeddedStorageConnectionFoundation setTypeDictionaryExporter(
		PersistenceTypeDictionaryExporter typeDictionaryExporter
	);

	@Override
	public EmbeddedStorageConnectionFoundation setTypeDictionaryParser(
		PersistenceTypeDictionaryParser typeDictionaryParser
	);

	@Override
	public EmbeddedStorageConnectionFoundation setTypeDictionaryLoader(
		PersistenceTypeDictionaryLoader typeDictionaryLoader
	);

	@Override
	public EmbeddedStorageConnectionFoundation setTypeDictionaryAssembler(
		PersistenceTypeDictionaryAssembler typeDictionaryAssembler
	);

	@Override
	public EmbeddedStorageConnectionFoundation setTypeDictionaryStorer(
		PersistenceTypeDictionaryStorer typeDictionaryStorer
	);

	@Override
	public EmbeddedStorageConnectionFoundation setTypeHandlerCreatorLookup(
		PersistenceTypeHandlerEnsurer<Binary> typeHandlerCreatorLookup
	);

	@Override
	public <H extends PersistenceTypeDictionaryLoader & PersistenceTypeDictionaryStorer>
	BinaryPersistenceFoundation setDictionaryStorage(H typeDictionaryStorageHandler);

	@Override
	public <P extends SwizzleTypeIdProvider & SwizzleObjectIdProvider>
	BinaryPersistenceFoundation setSwizzleIdProvider(P swizzleTypeIdProvider);

	@Override
	public BinaryPersistenceFoundation setPersistenceChannel(PersistenceChannel<Binary> persistenceChannel);

	public EmbeddedStorageConnectionFoundation setStorageManager(StorageManager storageManager);



	public StorageConnection createStorageConnection();



	public class Implementation
	extends BinaryPersistenceFoundation.Implementation
	implements EmbeddedStorageConnectionFoundation
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields  //
		/////////////////////

		private           StorageManager         storageManager           ;
		private transient StorageRequestAcceptor connectionRequestAcceptor;



		///////////////////////////////////////////////////////////////////////////
		// declared methods //
		/////////////////////

		protected final void internalSetStorageManager(final StorageManager storageManager)
		{
			this.storageManager = storageManager;
		}

		protected StorageManager createStorageManager()
		{
			throw new MissingFoundationPartException(StorageManager.class);
		}



		///////////////////////////////////////////////////////////////////////////
		// getters          //
		/////////////////////

		@Override
		public StorageManager getStorageManager()
		{
			if(this.storageManager == null)
			{
				this.storageManager = this.dispatch(this.createStorageManager());
			}
			return this.storageManager;
		}



		///////////////////////////////////////////////////////////////////////////
		// setters          //
		/////////////////////

		@Override
		public EmbeddedStorageConnectionFoundation.Implementation setTypeDictionaryAssembler(
			final PersistenceTypeDictionaryAssembler typeDictionaryAssembler
		)
		{
			super.setTypeDictionaryAssembler(typeDictionaryAssembler);
			return this;
		}

		@Override
		public EmbeddedStorageConnectionFoundation.Implementation setTypeDictionaryStorer(
			final PersistenceTypeDictionaryStorer typeDictionaryStorer
		)
		{
			super.setTypeDictionaryStorer(typeDictionaryStorer);
			return this;
		}

		@Override
		public EmbeddedStorageConnectionFoundation.Implementation setTypeDictionaryProvider(
			final PersistenceTypeDictionaryProvider typeDictionaryProvider
		)
		{
			super.setTypeDictionaryProvider(typeDictionaryProvider);
			return this;
		}

		@Override
		public EmbeddedStorageConnectionFoundation.Implementation setTypeDictionaryManager(
			final PersistenceTypeDictionaryManager typeDictionaryManager
		)
		{
			super.setTypeDictionaryManager(typeDictionaryManager);
			return this;
		}

		@Override
		public EmbeddedStorageConnectionFoundation.Implementation setInstanceDispatcher(
			final InstanceDispatcherLogic instanceDispatcher
		)
		{
			super.setInstanceDispatcher(instanceDispatcher);
			return this;
		}

		@Override
		public EmbeddedStorageConnectionFoundation.Implementation setObjectManager(
			final SwizzleObjectManager objectManager
		)
		{
			super.setObjectManager(objectManager);
			return this;
		}

		@Override
		public EmbeddedStorageConnectionFoundation.Implementation setSwizzleRegistry(
			final SwizzleRegistry swizzleRegistry
		)
		{
			super.setSwizzleRegistry(swizzleRegistry);
			return this;
		}

		@Override
		public EmbeddedStorageConnectionFoundation.Implementation setStorerCreator(
			final PersistenceStorer.Creator<Binary> storerCreator
		)
		{
			super.setStorerCreator(storerCreator);
			return this;
		}

		@Override
		public EmbeddedStorageConnectionFoundation.Implementation setTypeHandlerManager(
			final PersistenceTypeHandlerManager<Binary> typeHandlerManager
		)
		{
			super.setTypeHandlerManager(typeHandlerManager);
			return this;
		}

		@Override
		public EmbeddedStorageConnectionFoundation.Implementation setObjectIdProvider(
			final SwizzleObjectIdProvider oidProvider
		)
		{
			super.setObjectIdProvider(oidProvider);
			return this;
		}

		@Override
		public EmbeddedStorageConnectionFoundation.Implementation setTypeIdProvider(
			final SwizzleTypeIdProvider tidProvider
		)
		{
			super.setTypeIdProvider(tidProvider);
			return this;
		}

		@Override
		public EmbeddedStorageConnectionFoundation.Implementation setTypeManager(final SwizzleTypeManager typeManager)
		{
			super.setTypeManager(typeManager);
			return this;
		}

		@Override
		public EmbeddedStorageConnectionFoundation.Implementation setTypeHandlerCreatorLookup(
			final PersistenceTypeHandlerEnsurer<Binary> typeHandlerCreatorLookup
		)
		{
			super.setTypeHandlerCreatorLookup(typeHandlerCreatorLookup);
			return this;
		}

		@Override
		public EmbeddedStorageConnectionFoundation.Implementation setTypeHandlerRegistry(
			final PersistenceTypeHandlerRegistry<Binary> typeHandlerRegistry
		)
		{
			super.setTypeHandlerRegistry(typeHandlerRegistry);
			return this;
		}

		@Override
		public EmbeddedStorageConnectionFoundation.Implementation setTypeHandlerProvider(
			final PersistenceTypeHandlerProvider<Binary> typeHandlerProvider
		)
		{
			super.setTypeHandlerProvider(typeHandlerProvider);
			return this;
		}

		@Override
		public EmbeddedStorageConnectionFoundation.Implementation setRegistererCreator(
			final PersistenceRegisterer.Creator registererCreator
		)
		{
			super.setRegistererCreator(registererCreator);
			return this;
		}

		@Override
		public EmbeddedStorageConnectionFoundation.Implementation setBuilderCreator(
			final PersistenceLoader.Creator<Binary> builderCreator
		)
		{
			super.setBuilderCreator(builderCreator);
			return this;
		}

		@Override
		public EmbeddedStorageConnectionFoundation.Implementation setPersistenceTarget(
			final PersistenceTarget<Binary> target
		)
		{
			super.setPersistenceTarget(target);
			return this;
		}

		@Override
		public EmbeddedStorageConnectionFoundation.Implementation setPersistenceSource(
			final PersistenceSource<Binary> source
		)
		{
			super.setPersistenceSource(source);
			return this;
		}

		@Override
		public EmbeddedStorageConnectionFoundation.Implementation setTypeDictionaryExporter(
			final PersistenceTypeDictionaryExporter typeDictionaryExporter
		)
		{
			super.setTypeDictionaryExporter(typeDictionaryExporter);
			return this;
		}

		@Override
		public EmbeddedStorageConnectionFoundation.Implementation setTypeDictionaryParser(
			final PersistenceTypeDictionaryParser typeDictionaryParser
		)
		{
			super.setTypeDictionaryParser(typeDictionaryParser);
			return this;
		}

		@Override
		public EmbeddedStorageConnectionFoundation.Implementation setTypeDictionaryLoader(
			final PersistenceTypeDictionaryLoader typeDictionaryLoader
		)
		{
			super.setTypeDictionaryLoader(typeDictionaryLoader);
			return this;
		}

		@Override
		public <H extends PersistenceTypeDictionaryLoader & PersistenceTypeDictionaryStorer>
		EmbeddedStorageConnectionFoundation.Implementation setDictionaryStorage(final H typeDictionaryStorageHandler)
		{
			super.setTypeDictionaryLoader(typeDictionaryStorageHandler);
			super.setTypeDictionaryStorer(typeDictionaryStorageHandler);
			return this;
		}

		@Override
		public EmbeddedStorageConnectionFoundation.Implementation setPersistenceChannel(final PersistenceChannel<Binary> persistenceChannel)
		{
			super.setPersistenceSource(persistenceChannel);
			super.setPersistenceTarget(persistenceChannel);
			return this;
		}

		@Override
		public <P extends SwizzleTypeIdProvider & SwizzleObjectIdProvider>
		EmbeddedStorageConnectionFoundation.Implementation setSwizzleIdProvider(final P swizzleTypeIdProvider)
		{
			this.setTypeIdProvider    (swizzleTypeIdProvider);
			this.setObjectIdProvider  (swizzleTypeIdProvider);
			return this;
		}

		@Override
		public EmbeddedStorageConnectionFoundation.Implementation setStorageManager(
			final StorageManager storageManager
		)
		{
			this.storageManager = storageManager;
			return this;
		}

		@Override
		public EmbeddedStorageConnectionFoundation.Implementation setTypeEvaluatorPersistable(
			final PersistenceTypeEvaluator typeEvaluatorPersistable
		)
		{
			super.setTypeEvaluatorPersistable(typeEvaluatorPersistable);
			return this;
		}

		@Override
		public EmbeddedStorageConnectionFoundation.Implementation setBufferSizeProvider(
			final BufferSizeProviderIncremental bufferSizeProvider
		)
		{
			super.setBufferSizeProvider(bufferSizeProvider);
			return this;
		}

		@Override
		public EmbeddedStorageConnectionFoundation.Implementation setTypeEvaluatorTypeIdMappable(
			final PersistenceTypeEvaluator typeEvaluatorTypeIdMappable
		)
		{
			super.setTypeEvaluatorTypeIdMappable(typeEvaluatorTypeIdMappable);
			return this;
		}

		@Override
		protected BinaryLoader.Creator createBuilderCreator()
		{
			return new BinaryLoader.CreatorChannelHashing(
				this.storageManager.channelController().channelCountProvider()
			);
		}

		@Override
		protected BinaryStorer.Creator createStorerCreator()
		{
			return BinaryStorer.Creator(
				this.storageManager.channelCountProvider()
			);
		}

		@Override
		protected EmbeddedStorageBinarySource createPersistenceSource()
		{
			return new EmbeddedStorageBinarySource.Implementation(this.internalGetStorageRequestAcceptor());
		}

		@Override
		protected EmbeddedStorageBinaryTarget createPersistenceTarget()
		{
			return new EmbeddedStorageBinaryTarget.Implementation(this.internalGetStorageRequestAcceptor());
		}

		protected StorageRequestAcceptor internalGetStorageRequestAcceptor()
		{
			if(this.connectionRequestAcceptor == null)
			{
				this.connectionRequestAcceptor = this.storageManager.createRequestAcceptor();
			}
			return this.connectionRequestAcceptor;
		}
		
		@Override
		protected PersistenceRootResolver createRootResolver()
		{
			return Persistence.RootResolver(EmbeddedStorage::root);
		}

		@Override
		public synchronized StorageConnection createStorageConnection()
		{
			// reset for new connection, gets set via method called in super method
			this.connectionRequestAcceptor = null;

			/*
			 * even though super.create() always gets called prior to reading the connectionRequestAcceptor
			 * and in the process calling internalGetStorageRequestAcceptor() and createRequestAcceptor(),
			 * sometimes it happens that despite the internalGetStorageRequestAcceptor() and despite being
			 * singlethreaded and even synchronized (= no code rearrangement), the field reference
			 * is still null when read as the second constructor argument.
			 * It is not clear why this happens under those conditions.
			 * As a workaround, the initializing getter has to be called once beforehand.
			 */
			this.internalGetStorageRequestAcceptor();

			// wrap actual persistence manager in connection implementation (see comment inside)
			return new StorageConnection.Implementation(
				super.createPersistenceManager(),
				this.connectionRequestAcceptor
			);
		}

	}

}

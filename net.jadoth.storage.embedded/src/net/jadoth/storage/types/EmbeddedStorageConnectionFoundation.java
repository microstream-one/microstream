package net.jadoth.storage.types;

import net.jadoth.functional.Dispatcher;
import net.jadoth.persistence.binary.types.Binary;
import net.jadoth.persistence.binary.types.BinaryLoader;
import net.jadoth.persistence.binary.types.BinaryPersistenceFoundation;
import net.jadoth.persistence.binary.types.BinaryStorer;
import net.jadoth.persistence.types.BufferSizeProvider;
import net.jadoth.persistence.types.PersistenceLoader;
import net.jadoth.persistence.types.PersistenceRegisterer;
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
import net.jadoth.persistence.types.PersistenceTypeHandlerManager;
import net.jadoth.persistence.types.PersistenceTypeHandlerProvider;
import net.jadoth.persistence.types.PersistenceTypeHandlerRegistry;
import net.jadoth.persistence.types.PersistenceTypeSovereignty;
import net.jadoth.swizzling.types.SwizzleObjectIdProvider;
import net.jadoth.swizzling.types.SwizzleObjectManager;
import net.jadoth.swizzling.types.SwizzleRegistry;
import net.jadoth.swizzling.types.SwizzleTypeIdProvider;
import net.jadoth.swizzling.types.SwizzleTypeManager;
import net.jadoth.util.MissingAssemblyPartException;

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
	public EmbeddedStorageConnectionFoundation setInstanceDispatcher(Dispatcher instanceDispatcher);

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
	public EmbeddedStorageConnectionFoundation setTypeSovereignty(PersistenceTypeSovereignty typeSovereignty);

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
	public <H extends PersistenceTypeDictionaryLoader & PersistenceTypeDictionaryStorer>
	BinaryPersistenceFoundation setDictionaryStorage(H typeDictionaryStorageHandler);

	@Override
	public <P extends SwizzleTypeIdProvider & SwizzleObjectIdProvider>
	BinaryPersistenceFoundation setSwizzleIdProvider(P swizzleTypeIdProvider);

	@Override
	public <S extends PersistenceTarget<Binary> & PersistenceSource<Binary>>
	BinaryPersistenceFoundation setPersistenceStorage(S persistenceStorage);

	public EmbeddedStorageConnectionFoundation setStorageManager(StorageManager storageManager);



	public StorageConnection createStorageConnection();


	
	public static EmbeddedStorageConnectionFoundation.Implementation New()
	{
		return new EmbeddedStorageConnectionFoundation.Implementation();
	}

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

		protected Implementation()
		{
			super();
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		protected final void internalSetStorageManager(final StorageManager storageManager)
		{
			this.storageManager = storageManager;
		}

		protected StorageManager createStorageManager()
		{
			throw new MissingAssemblyPartException(StorageManager.class);
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
			this.internalSetTypeDictionaryAssembler(typeDictionaryAssembler);
			return this;
		}

		@Override
		public EmbeddedStorageConnectionFoundation.Implementation setTypeDictionaryStorer(
			final PersistenceTypeDictionaryStorer typeDictionaryStorer
		)
		{
			this.internalSetTypeDictionaryStorer(typeDictionaryStorer);
			return this;
		}

		@Override
		public EmbeddedStorageConnectionFoundation.Implementation setTypeDictionaryProvider(
			final PersistenceTypeDictionaryProvider typeDictionaryProvider
		)
		{
			this.internalSetTypeDictionaryProvider(typeDictionaryProvider);
			return this;
		}

		@Override
		public EmbeddedStorageConnectionFoundation.Implementation setTypeDictionaryManager(
			final PersistenceTypeDictionaryManager typeDictionaryManager
		)
		{
			this.internalSetTypeDictionaryManager(typeDictionaryManager);
			return this;
		}

		@Override
		public EmbeddedStorageConnectionFoundation.Implementation setTypeSovereignty(
			final PersistenceTypeSovereignty typeSovereignty
		)
		{
			this.internalSetTypeSovereignty(typeSovereignty);
			return this;
		}

		@Override
		public EmbeddedStorageConnectionFoundation.Implementation setInstanceDispatcher(
			final Dispatcher instanceDispatcher
		)
		{
			this.internalSetDispatcher(instanceDispatcher);
			return this;
		}

		@Override
		public EmbeddedStorageConnectionFoundation.Implementation setObjectManager(
			final SwizzleObjectManager objectManager
		)
		{
			this.internalSetObjectManager(objectManager);
			return this;
		}

		@Override
		public EmbeddedStorageConnectionFoundation.Implementation setSwizzleRegistry(
			final SwizzleRegistry swizzleRegistry
		)
		{
			this.internalSetSwizzleRegistry(swizzleRegistry);
			return this;
		}

		@Override
		public EmbeddedStorageConnectionFoundation.Implementation setStorerCreator(
			final PersistenceStorer.Creator<Binary> persisterCreator
		)
		{
			this.internalSetPersisterCreator(persisterCreator);
			return this;
		}

		@Override
		public EmbeddedStorageConnectionFoundation.Implementation setTypeHandlerManager(
			final PersistenceTypeHandlerManager<Binary> typeHandlerManager
		)
		{
			this.internalSetTypeHandlerManager(typeHandlerManager);
			return this;
		}

		@Override
		public EmbeddedStorageConnectionFoundation.Implementation setObjectIdProvider(
			final SwizzleObjectIdProvider oidProvider
		)
		{
			this.internalSetOidProvider(oidProvider);
			return this;
		}

		@Override
		public EmbeddedStorageConnectionFoundation.Implementation setTypeIdProvider(
			final SwizzleTypeIdProvider tidProvider
		)
		{
			this.internalSetTidProvider(tidProvider);
			return this;
		}

		@Override
		public EmbeddedStorageConnectionFoundation.Implementation setTypeManager(final SwizzleTypeManager typeManager)
		{
			this.internalSetTypeManager(typeManager);
			return this;
		}

		@Override
		public EmbeddedStorageConnectionFoundation.Implementation setTypeHandlerRegistry(
			final PersistenceTypeHandlerRegistry<Binary> typeHandlerRegistry
		)
		{
			this.internalSetTypeHandlerRegistry(typeHandlerRegistry);
			return this;
		}

		@Override
		public EmbeddedStorageConnectionFoundation.Implementation setTypeHandlerProvider(
			final PersistenceTypeHandlerProvider<Binary> typeHandlerProvider
		)
		{
			this.internalSetTypeHandlerProvider(typeHandlerProvider);
			return this;
		}

		@Override
		public EmbeddedStorageConnectionFoundation.Implementation setRegistererCreator(
			final PersistenceRegisterer.Creator registererCreator
		)
		{
			this.internalSetRegistererCreator(registererCreator);
			return this;
		}

		@Override
		public EmbeddedStorageConnectionFoundation.Implementation setBuilderCreator(
			final PersistenceLoader.Creator<Binary> builderCreator
		)
		{
			this.internalSetBuilderCreator(builderCreator);
			return this;
		}

		@Override
		public EmbeddedStorageConnectionFoundation.Implementation setPersistenceTarget(
			final PersistenceTarget<Binary> target
		)
		{
			this.internalSetTarget(target);
			return this;
		}

		@Override
		public EmbeddedStorageConnectionFoundation.Implementation setPersistenceSource(
			final PersistenceSource<Binary> source
		)
		{
			this.internalSetSource(source);
			return this;
		}

		@Override
		public EmbeddedStorageConnectionFoundation.Implementation setTypeDictionaryExporter(
			final PersistenceTypeDictionaryExporter typeDictionaryExporter
		)
		{
			this.internalSetTypeDictionaryExporter(typeDictionaryExporter);
			return this;
		}

		@Override
		public EmbeddedStorageConnectionFoundation.Implementation setTypeDictionaryParser(
			final PersistenceTypeDictionaryParser typeDictionaryParser
		)
		{
			this.internalSetTypeDictionaryParser(typeDictionaryParser);
			return this;
		}

		@Override
		public EmbeddedStorageConnectionFoundation.Implementation setTypeDictionaryLoader(
			final PersistenceTypeDictionaryLoader typeDictionaryLoader
		)
		{
			this.internalSetTypeDictionaryLoader(typeDictionaryLoader);
			return this;
		}

		@Override
		public <H extends PersistenceTypeDictionaryLoader & PersistenceTypeDictionaryStorer>
		EmbeddedStorageConnectionFoundation.Implementation setDictionaryStorage(final H typeDictionaryStorageHandler)
		{
			this.internalSetTypeDictionaryLoader(typeDictionaryStorageHandler);
			this.internalSetTypeDictionaryStorer(typeDictionaryStorageHandler);
			return this;
		}

		@Override
		public <S extends PersistenceTarget<Binary> & PersistenceSource<Binary>>
		EmbeddedStorageConnectionFoundation.Implementation setPersistenceStorage(final S persistenceStorage)
		{
			this.internalSetSource(persistenceStorage);
			this.internalSetTarget(persistenceStorage);
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
			this.internalSetStorageManager(storageManager);
			return this;
		}

		@Override
		public EmbeddedStorageConnectionFoundation.Implementation setTypeEvaluatorPersistable(
			final PersistenceTypeEvaluator typeEvaluatorPersistable
		)
		{
			this.internalSetTypeEvaluatorPersistable(typeEvaluatorPersistable);
			return this;
		}

		@Override
		public EmbeddedStorageConnectionFoundation.Implementation setBufferSizeProvider(
			final BufferSizeProvider bufferSizeProvider
		)
		{
			this.internalSetBufferSizeProvider(bufferSizeProvider);
			return this;
		}

		@Override
		public EmbeddedStorageConnectionFoundation.Implementation setTypeEvaluatorTypeIdMappable(
			final PersistenceTypeEvaluator typeEvaluatorTypeIdMappable
		)
		{
			this.internalSetTypeEvaluatorTypeIdMappable(typeEvaluatorTypeIdMappable);
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
			return new BinaryStorer.CreatorChannelHashingDeep(
				this.storageManager.channelController().channelCountProvider()
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
		public synchronized StorageConnection createStorageConnection()
		{
			// reset for new connection, gets set via method called in super method
			this.connectionRequestAcceptor = null;

			/* even though super.create() always gets called prior to reading the connectionRequestAcceptor
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

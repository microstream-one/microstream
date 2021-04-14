package one.microstream.storage.embedded.types;

import static one.microstream.X.notNull;

import java.util.function.Supplier;

import one.microstream.exceptions.MissingFoundationPartException;
import one.microstream.persistence.binary.types.BinaryLoader;
import one.microstream.persistence.binary.types.BinaryPersistenceFoundation;
import one.microstream.persistence.binary.types.BinaryStorer;
import one.microstream.storage.types.StorageConnection;
import one.microstream.storage.types.StorageRequestAcceptor;
import one.microstream.storage.types.StorageSystem;
import one.microstream.storage.types.StorageWriteController;

public interface EmbeddedStorageConnectionFoundation<F extends EmbeddedStorageConnectionFoundation<?>>
extends BinaryPersistenceFoundation<F>
{
	// intentionally no "get" prefix since this is a pure pseudo-property getter and not an action.
	public Supplier<? extends StorageSystem> storageSystemSupplier();
	
	public StorageSystem getStorageSystem();

	public StorageWriteController writeController();
	
	public StorageWriteController getWriteController();

	public F setStorageSystem(StorageSystem storageSystem);
	
	public F setStorageSystemSupplier(Supplier<? extends StorageSystem> storageSystemSupplier);
	
	public F setWriteController(StorageWriteController writeController);
	
	public StorageConnection createStorageConnection();


	
	public static EmbeddedStorageConnectionFoundation<?> New()
	{
		return new EmbeddedStorageConnectionFoundation.Default<>();
	}

	public class Default<F extends EmbeddedStorageConnectionFoundation.Default<?>>
	extends BinaryPersistenceFoundation.Default<F>
	implements EmbeddedStorageConnectionFoundation<F>
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		private StorageSystem                     storageSystem         ;
		private Supplier<? extends StorageSystem> storageSystemSupplier ;
		private StorageWriteController            writeController       ;
		private transient StorageRequestAcceptor  storageRequestAcceptor;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		protected Default()
		{
			super();
		}

		

		///////////////////////////////////////////////////////////////////////////
		// getters //
		////////////

		@Override
		public Supplier<? extends StorageSystem> storageSystemSupplier()
		{
			return this.storageSystemSupplier;
		}
		
		@Override
		public StorageWriteController writeController()
		{
			return this.writeController;
		}
		
		@Override
		public StorageWriteController getWriteController()
		{
			if(this.writeController == null)
			{
				this.writeController = this.dispatch(this.ensureWriteController());
			}
			return this.writeController;
		}
		
		@Override
		public StorageSystem getStorageSystem()
		{
			if(this.storageSystem == null)
			{
				this.storageSystem = this.dispatch(this.ensureStorageSystem());
			}
			return this.storageSystem;
		}



		///////////////////////////////////////////////////////////////////////////
		// setters //
		////////////

		@Override
		public F setStorageSystem(
			final StorageSystem storageSystem
		)
		{
			this.storageSystem = storageSystem;
			return this.$();
		}
		
		@Override
		public F setStorageSystemSupplier(final Supplier<? extends StorageSystem> storageSystemSupplier)
		{
			this.storageSystemSupplier = storageSystemSupplier;
			return this.$();
		}
		
		@Override
		public F setWriteController(final StorageWriteController writeController)
		{
			this.writeController = writeController;
			
			return this.$();
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		protected final void internalSetStorageSystem(final StorageSystem storageSystem)
		{
			this.storageSystem = storageSystem;
		}

		protected StorageSystem ensureStorageSystem()
		{
			if(this.storageSystemSupplier != null)
			{
				return notNull(this.storageSystemSupplier.get());
			}
			
			throw new MissingFoundationPartException(StorageSystem.class);
		}
		
		protected StorageWriteController ensureWriteController()
		{
			return StorageWriteController.Wrap(
				this.getStorageSystem().fileSystem()
			);
		}

		@Override
		protected BinaryLoader.Creator ensureBuilderCreator()
		{
			return new BinaryLoader.CreatorChannelHashing(
				this.getStorageSystem().operationController().channelCountProvider(),
				this.isByteOrderMismatch()
			);
		}

		@Override
		protected BinaryStorer.Creator ensureStorerCreator()
		{
			return BinaryStorer.Creator(
				this.getStorageSystem().channelCountProvider(),
				this.isByteOrderMismatch()
			);
		}

		@Override
		protected EmbeddedStorageBinarySource ensurePersistenceSource()
		{
			return new EmbeddedStorageBinarySource.Default(this.internalGetStorageRequestAcceptor());
		}

		@Override
		protected EmbeddedStorageBinaryTarget ensurePersistenceTarget()
		{
			return EmbeddedStorageBinaryTarget.New(
				this.internalGetStorageRequestAcceptor(),
				this.getWriteController()
			);
		}

		protected StorageRequestAcceptor internalGetStorageRequestAcceptor()
		{
			if(this.storageRequestAcceptor == null)
			{
				this.storageRequestAcceptor = this.storageSystem.createRequestAcceptor();
			}
			return this.storageRequestAcceptor;
		}

		@Override
		public synchronized StorageConnection createStorageConnection()
		{
			// reset for new connection, gets set via method called in super method
			this.storageRequestAcceptor = null;

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

			// persistence manager is "connected" to the storage's request acceptor (= the storage threads)
			return StorageConnection.New(
				super.createPersistenceManager(),
				this.storageRequestAcceptor
			);
		}

	}

}

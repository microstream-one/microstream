package one.microstream.storage.types;

import static one.microstream.X.notNull;

import java.util.function.Supplier;

import one.microstream.exceptions.MissingFoundationPartException;
import one.microstream.persistence.binary.types.BinaryLoader;
import one.microstream.persistence.binary.types.BinaryPersistenceFoundation;
import one.microstream.persistence.binary.types.BinaryStorer;

public interface EmbeddedStorageConnectionFoundation<F extends EmbeddedStorageConnectionFoundation<?>>
extends BinaryPersistenceFoundation<F>
{
	// intentionally no "get" prefix since this is a pure pseudo-property getter and not an action.
	public Supplier<? extends StorageSystem> storageManagerSupplier();
	
	public StorageSystem getStorageManager();

	public F setStorageManager(StorageSystem storageManager);
	
	public F setStorageManagerSupplier(Supplier<? extends StorageSystem> storageManagerSupplier);
	
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

		private StorageSystem                     storageManager           ;
		private Supplier<? extends StorageSystem> storageManagerSupplier   ;
		private transient StorageRequestAcceptor   connectionRequestAcceptor;
		
		
		
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
		public Supplier<? extends StorageSystem> storageManagerSupplier()
		{
			return this.storageManagerSupplier;
		}
		
		@Override
		public StorageSystem getStorageManager()
		{
			if(this.storageManager == null)
			{
				this.storageManager = this.dispatch(this.ensureStorageManager());
			}
			return this.storageManager;
		}



		///////////////////////////////////////////////////////////////////////////
		// setters //
		////////////

		@Override
		public F setStorageManager(
			final StorageSystem storageManager
		)
		{
			this.storageManager = storageManager;
			return this.$();
		}
		
		@Override
		public F setStorageManagerSupplier(final Supplier<? extends StorageSystem> storageManagerSupplier)
		{
			this.storageManagerSupplier = storageManagerSupplier;
			return this.$();
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		protected final void internalSetStorageManager(final StorageSystem storageManager)
		{
			this.storageManager = storageManager;
		}

		protected StorageSystem ensureStorageManager()
		{
			if(this.storageManagerSupplier != null)
			{
				return notNull(this.storageManagerSupplier.get());
			}
			
			throw new MissingFoundationPartException(StorageSystem.class);
		}

		@Override
		protected BinaryLoader.Creator ensureBuilderCreator()
		{
			return new BinaryLoader.CreatorChannelHashing(
				this.getStorageManager().operationController().channelCountProvider(),
				this.isByteOrderMismatch()
			);
		}

		@Override
		protected BinaryStorer.Creator ensureStorerCreator()
		{
			return BinaryStorer.Creator(
				this.getStorageManager().channelCountProvider(),
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
			return new EmbeddedStorageBinaryTarget.Default(this.internalGetStorageRequestAcceptor());
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
			return new StorageConnection.Default(
				super.createPersistenceManager(),
				this.connectionRequestAcceptor
			);
		}

	}

}

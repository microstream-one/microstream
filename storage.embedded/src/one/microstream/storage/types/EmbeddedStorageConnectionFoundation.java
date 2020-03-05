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
	public Supplier<? extends StorageSystem> storageSystemSupplier();
	
	public StorageSystem getStorageSystem();

	public F setStorageSystem(StorageSystem storageSystem);
	
	public F setStorageSystemSupplier(Supplier<? extends StorageSystem> storageSystemSupplier);
	
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

		private StorageSystem                     storageSystem            ;
		private Supplier<? extends StorageSystem> storageSystemSupplier    ;
		private transient StorageRequestAcceptor  connectionRequestAcceptor;
		
		
		
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
			return new EmbeddedStorageBinaryTarget.Default(this.internalGetStorageRequestAcceptor());
		}

		protected StorageRequestAcceptor internalGetStorageRequestAcceptor()
		{
			if(this.connectionRequestAcceptor == null)
			{
				this.connectionRequestAcceptor = this.storageSystem.createRequestAcceptor();
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

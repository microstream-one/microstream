package one.microstream.storage.types;

import static one.microstream.X.notNull;

import java.util.function.Supplier;

import one.microstream.X;
import one.microstream.exceptions.MissingFoundationPartException;
import one.microstream.persistence.binary.types.BinaryLoader;
import one.microstream.persistence.binary.types.BinaryPersistenceFoundation;
import one.microstream.persistence.binary.types.BinaryStorer;
import one.microstream.persistence.types.PersistenceRootResolver;

public interface EmbeddedStorageConnectionFoundation<F extends EmbeddedStorageConnectionFoundation<?>>
extends BinaryPersistenceFoundation<F>
{
	// intentionally no "get" prefix since this is a pure pseudo-property getter and not an action.
	public Supplier<? extends StorageManager> storageManagerSupplier();
	
	public StorageManager getStorageManager();

	public F setStorageManager(StorageManager storageManager);
	
	public F setStorageManagerSupplier(Supplier<? extends StorageManager> storageManagerSupplier);
	
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

		private StorageManager                     storageManager           ;
		private Supplier<? extends StorageManager> storageManagerSupplier   ;
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
		public Supplier<? extends StorageManager> storageManagerSupplier()
		{
			return this.storageManagerSupplier;
		}
		
		@Override
		public StorageManager getStorageManager()
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
			final StorageManager storageManager
		)
		{
			this.storageManager = storageManager;
			return this.$();
		}
		
		@Override
		public F setStorageManagerSupplier(final Supplier<? extends StorageManager> storageManagerSupplier)
		{
			this.storageManagerSupplier = storageManagerSupplier;
			return this.$();
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		protected final void internalSetStorageManager(final StorageManager storageManager)
		{
			this.storageManager = storageManager;
		}

		protected StorageManager ensureStorageManager()
		{
			if(this.storageManagerSupplier != null)
			{
				return notNull(this.storageManagerSupplier.get());
			}
			
			throw new MissingFoundationPartException(StorageManager.class);
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
		protected PersistenceRootResolver ensureRootResolver()
		{
			final PersistenceRootResolver.Builder builder = this.getRootResolverBuilder();
			if(!builder.hasRootRegistered())
			{
				builder.registerDefaultRoot(
					X.Reference(null)
				);
			}
			
			final PersistenceRootResolver resolver = builder.build();
			
			final PersistenceRootResolver refactoringWrapper = PersistenceRootResolver.Wrap(
				resolver,
				this.getTypeDescriptionResolverProvider()
			);
			
			return refactoringWrapper;
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

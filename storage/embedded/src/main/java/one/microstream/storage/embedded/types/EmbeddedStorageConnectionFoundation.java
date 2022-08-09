package one.microstream.storage.embedded.types;

/*-
 * #%L
 * microstream-storage-embedded
 * %%
 * Copyright (C) 2019 - 2022 MicroStream Software
 * %%
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 * 
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the Eclipse
 * Public License, v. 2.0 are satisfied: GNU General Public License, version 2
 * with the GNU Classpath Exception which is
 * available at https://www.gnu.org/software/classpath/license.html.
 * 
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 * #L%
 */

import static one.microstream.X.notNull;

import java.util.function.Supplier;

import one.microstream.exceptions.MissingFoundationPartException;
import one.microstream.persistence.binary.types.Binary;
import one.microstream.persistence.binary.types.BinaryLoader;
import one.microstream.persistence.binary.types.BinaryPersistenceFoundation;
import one.microstream.persistence.binary.types.BinaryStorer;
import one.microstream.persistence.types.PersistenceLiveStorerRegistry;
import one.microstream.persistence.types.PersistenceManager;
import one.microstream.persistence.types.PersistenceStorer;
import one.microstream.reference.Reference;
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
	
	public EmbeddedStorageObjectRegistryCallback getObjectRegistryCallback();

	public Reference<PersistenceLiveStorerRegistry> getLiveStorerRegistryReference();

	public PersistenceLiveStorerRegistry getLiveStorerRegistry();

	public F setStorageSystem(StorageSystem storageSystem);
	
	public F setStorageSystemSupplier(Supplier<? extends StorageSystem> storageSystemSupplier);
	
	public F setWriteController(StorageWriteController writeController);
	
	public F setObjectRegistryCallback(EmbeddedStorageObjectRegistryCallback objectRegistryCallback);

	public F setLiveStorerRegistryReference(Reference<PersistenceLiveStorerRegistry> storerRegistryReference);

	public F setLiveStorerRegistry(PersistenceLiveStorerRegistry liveLiveStorerRegistry);
	
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

		private StorageSystem                            storageSystem          ;
		private Supplier<? extends StorageSystem>        storageSystemSupplier  ;
		private StorageWriteController                   writeController        ;
		private transient StorageRequestAcceptor         storageRequestAcceptor ;
		private EmbeddedStorageObjectRegistryCallback    objectRegistryCallback ;
		private Reference<PersistenceLiveStorerRegistry> storerRegistryReference;
		private PersistenceLiveStorerRegistry            liveLiveStorerRegistry ;
		
		
		
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

		@Override
		public final EmbeddedStorageObjectRegistryCallback getObjectRegistryCallback()
		{
			if(this.objectRegistryCallback == null)
			{
				this.objectRegistryCallback = this.dispatch(this.ensureObjectRegistryCallback());
			}
			return this.objectRegistryCallback;
		}

		@Override
		public final Reference<PersistenceLiveStorerRegistry> getLiveStorerRegistryReference()
		{
			if(this.storerRegistryReference == null)
			{
				this.storerRegistryReference = this.dispatch(this.ensureLiveStorerRegistryReference());
			}
			return this.storerRegistryReference;
		}

		@Override
		public final PersistenceLiveStorerRegistry getLiveStorerRegistry()
		{
			if(this.liveLiveStorerRegistry == null)
			{
				this.liveLiveStorerRegistry = this.dispatch(this.ensureLiveStorerRegistry());
			}
			return this.liveLiveStorerRegistry;
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
		
		@Override
		public F setObjectRegistryCallback(final EmbeddedStorageObjectRegistryCallback objectRegistryCallback)
		{
			this.objectRegistryCallback = objectRegistryCallback;
			return this.$();
		}

		@Override
		public final F setLiveStorerRegistryReference(final Reference<PersistenceLiveStorerRegistry> storerRegistryReference)
		{
			this.storerRegistryReference = storerRegistryReference;
			return this.$();
		}

		@Override
		public final F setLiveStorerRegistry(final PersistenceLiveStorerRegistry liveLiveStorerRegistry)
		{
			this.liveLiveStorerRegistry = liveLiveStorerRegistry;
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
		
		protected EmbeddedStorageObjectRegistryCallback ensureObjectRegistryCallback()
		{
			// initially empty, gets initialized upon storage connection creation
			return EmbeddedStorageObjectRegistryCallback.New();
		}

		protected Reference<PersistenceLiveStorerRegistry> ensureLiveStorerRegistryReference()
		{
			throw new MissingFoundationPartException(Reference.class, "to " + PersistenceLiveStorerRegistry.class.getSimpleName());
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
		
		protected PersistenceLiveStorerRegistry ensureLiveStorerRegistry()
		{
			// embedded storage must create a functional storer registry for use with the storage layer (GC sweep).
			return PersistenceLiveStorerRegistry.New();
		}

		@Override
		public PersistenceManager<Binary> createPersistenceManager()
		{
			final PersistenceLiveStorerRegistry storerRegistry = this.getLiveStorerRegistry();
			final PersistenceStorer.CreationObserver observer = this.getStorerCreationObserver();
			if(observer == null)
			{
				// registry can simply be set as the (sole) observer
				this.setStorerCreationObserver(storerRegistry);
			}
			else
			{
				// conserve existing observer
				this.setStorerCreationObserver(
					PersistenceStorer.CreationObserver.Chain(observer, storerRegistry)
				);
			}
			this.getLiveStorerRegistryReference().set(storerRegistry);

			final PersistenceManager<Binary> pm = super.createPersistenceManager();

			// reference explicitely the PM's object registry, just to be safe
			this.getObjectRegistryCallback().initializeObjectRegistry(pm.objectRegistry());
			// note: using more than 1 connection might cause consistency problems for the Storage GC using the callback

			return pm;
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

			// using this. instead of super. is important here!
			final PersistenceManager<Binary> pm = this.createPersistenceManager();

			// persistence manager is "connected" to the storage's request acceptor (= the storage threads)
			return StorageConnection.New(pm, this.storageRequestAcceptor);
		}

	}

}

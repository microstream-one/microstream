package one.microstream.persistence.types;

/*-
 * #%L
 * microstream-persistence
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

import static one.microstream.X.mayNull;
import static one.microstream.X.notNull;

import java.nio.ByteOrder;
import java.util.function.Consumer;

import one.microstream.X;
import one.microstream.util.BufferSizeProviderIncremental;


public interface PersistenceManager<D>
extends
PersistenceObjectManager<D>,
PersistenceRetrieving,
Persister,
PersistenceSourceSupplier<D>,
ByteOrderTargeting<PersistenceManager<D>>
{
	@Override
	public PersistenceStorer createLazyStorer();
	
	@Override
	public PersistenceStorer createStorer();

	@Override
	public PersistenceStorer createEagerStorer();

	public PersistenceStorer createStorer(PersistenceStorer.Creator<D> storerCreator);
	
	// manager methods //
	
	public PersistenceLoader createLoader();
	
	public PersistenceRegisterer createRegisterer();

	public void updateMetadata(PersistenceTypeDictionary typeDictionary, long highestTypeId, long highestObjectId);

	public default void updateMetadata(final PersistenceTypeDictionary typeDictionary)
	{
		this.updateMetadata(typeDictionary, 0, 0);
	}
	
	public PersistenceObjectRegistry objectRegistry();
	
	public PersistenceTypeDictionary typeDictionary();

	@Override
	public long currentObjectId();

	@Override
	public PersistenceManager<D> updateCurrentObjectId(long currentObjectId);
	
	@Override
	public PersistenceSource<D> source();
	
	public PersistenceTarget<D> target();
	
	/**
	 * Closes all ties to outside resources, if applicable. Typ
	 */
	public void close();
	

	
	public static <D> PersistenceManager<D> New(
		final PersistenceObjectRegistry          objectRegistering ,
		final PersistenceObjectManager<D>        objectManager     ,
		final PersistenceTypeHandlerManager<D>   typeHandlerManager,
		final PersistenceContextDispatcher<D>    contextDispatcher ,
		final PersistenceStorer.Creator<D>       storerCreator     ,
		final PersistenceLoader.Creator<D>       loaderCreator     ,
		final PersistenceRegisterer.Creator      registererCreator ,
		final Persister                          persister         ,
		final PersistenceTarget<D>               target            ,
		final PersistenceSource<D>               source            ,
		final PersistenceStorer.CreationObserver storerObserver ,
		final BufferSizeProviderIncremental      bufferSizeProvider,
		final ByteOrder                          targetByteOrder
	)
	{
		return new PersistenceManager.Default<>(
			notNull(objectRegistering) ,
			notNull(objectManager)     ,
			notNull(typeHandlerManager),
			notNull(contextDispatcher) ,
			notNull(storerCreator)     ,
			notNull(loaderCreator)     ,
			notNull(registererCreator) ,
			mayNull(persister)         , // non-null reference ensured by getEffectivePersister
			notNull(target)            ,
			notNull(source)            ,
			notNull(storerObserver)    ,
			notNull(bufferSizeProvider),
			notNull(targetByteOrder)
		);
	}

	public final class Default<D> implements PersistenceManager<D>, Unpersistable
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		// instance registration components //
		private final PersistenceObjectRegistry     objectRegistry   ;
		private final PersistenceObjectManager<D>   objectManager    ;
		private final PersistenceRegisterer.Creator registererCreator;

		// instance handling components //
		private final PersistenceTypeHandlerManager<D>   typeHandlerManager;
		private final PersistenceContextDispatcher<D>    contextDispatcher ;
		private final PersistenceStorer.Creator<D>       storerCreator     ;
		private final PersistenceLoader.Creator<D>       loaderCreator     ;
		private final PersistenceStorer.CreationObserver storerObserver    ;
		private final BufferSizeProviderIncremental      bufferSizeProvider;
		
		// callback linking components //
		private final Persister persister;

		// source and target //
		private final PersistenceSource<D> source;
		private final PersistenceTarget<D> target;
		
		private final ByteOrder targetByteOrder;
		
		/*
		 * To avoid race conditions in the implicitely created storer instances,
		 * their usage is serialized via mutex locking. If the suppressed parallelism
		 * is needed, explicitely created storers can be used, but must then be
		 * concurrency-managed by the using logic.
		 * Iterating the object graph and committing (i.e. I/O-flushing the collected
		 * bytes) is handled by different locks since the iteration is the concurrent-
		 * critical part but committing takes the vast majority of time (costly I/O).
		 */
		private final Object storeMutex = new Object();



		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		Default(
			final PersistenceObjectRegistry          objectRegistering ,
			final PersistenceObjectManager<D>        objectManager     ,
			final PersistenceTypeHandlerManager<D>   typeHandlerManager,
			final PersistenceContextDispatcher<D>    contextDispatcher ,
			final PersistenceStorer.Creator<D>       storerCreator     ,
			final PersistenceLoader.Creator<D>       loaderCreator     ,
			final PersistenceRegisterer.Creator      registererCreator ,
			final Persister                          persister         ,
			final PersistenceTarget<D>               target            ,
			final PersistenceSource<D>               source            ,
			final PersistenceStorer.CreationObserver storerObserver    ,
			final BufferSizeProviderIncremental      bufferSizeProvider,
			final ByteOrder                          targetByteOrder
		)
		{
			super();
			this.objectRegistry     = objectRegistering ;
			this.objectManager      = objectManager     ;
			this.typeHandlerManager = typeHandlerManager;
			this.contextDispatcher  = contextDispatcher ;
			this.storerCreator      = storerCreator     ;
			this.loaderCreator      = loaderCreator     ;
			this.registererCreator  = registererCreator ;
			this.persister          = persister         ;
			this.target             = target            ;
			this.source             = source            ;
			this.storerObserver     = storerObserver    ;
			this.bufferSizeProvider = bufferSizeProvider;
			this.targetByteOrder    = targetByteOrder   ;
		}



		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		@Override
		public final ByteOrder getTargetByteOrder()
		{
			return this.targetByteOrder;
		}
						
		@Override
		public final PersistenceObjectRegistry objectRegistry()
		{
			return this.objectRegistry;
		}
		
		@Override
		public final PersistenceTypeDictionary typeDictionary()
		{
			return this.typeHandlerManager.typeDictionary();
		}

		@Override
		public final void consolidate()
		{
			this.objectRegistry.consolidate();
		}
		
		private final Persister getEffectivePersister()
		{
			return X.coalesce(this.persister, this);
		}
		
		private <S extends PersistenceStorer> S registerStorer(final S storer)
		{
			this.storerObserver.observeCreatedStorer(storer);
			return storer;
		}
				
		@Override
		public final PersistenceStorer createLazyStorer()
		{
			return this.registerStorer(this.storerCreator.createLazyStorer(
				this.contextDispatcher.dispatchTypeHandlerManager(this.typeHandlerManager),
				this.contextDispatcher.dispatchObjectManager(this.objectManager),
				this.getEffectivePersister(),
				this.target,
				this.bufferSizeProvider
			));
		}
		
		@Override
		public final PersistenceStorer createStorer()
		{
			return this.registerStorer(this.storerCreator.createStorer(
				this.contextDispatcher.dispatchTypeHandlerManager(this.typeHandlerManager),
				this.contextDispatcher.dispatchObjectManager(this.objectManager),
				this.getEffectivePersister(),
				this.target,
				this.bufferSizeProvider
			));
		}

		@Override
		public final PersistenceStorer createEagerStorer()
		{
			return this.registerStorer(this.storerCreator.createEagerStorer(
				this.contextDispatcher.dispatchTypeHandlerManager(this.typeHandlerManager),
				this.contextDispatcher.dispatchObjectManager(this.objectManager),
				this.getEffectivePersister(),
				this.target,
				this.bufferSizeProvider
			));
		}
		
		@Override
		public final PersistenceStorer createStorer(final PersistenceStorer.Creator<D> storerCreator)
		{
			return this.registerStorer(storerCreator.createStorer(
				this.contextDispatcher.dispatchTypeHandlerManager(this.typeHandlerManager),
				this.contextDispatcher.dispatchObjectManager(this.objectManager),
				this.getEffectivePersister(),
				this.target,
				this.bufferSizeProvider
			));
		}

		@Override
		public final PersistenceRegisterer createRegisterer()
		{
			// undispatched (for now)
			return this.registererCreator.createRegisterer(
				this.objectManager,
				this.typeHandlerManager
			);
		}

		@Override
		public final long store(final Object object)
		{
			final long objectId;
			final PersistenceStorer persister = this.createStorer();
			
			synchronized(this.storeMutex)
			{
				objectId = persister.store(object);
				persister.commit();
			}

			return objectId;
		}
		
		@Override
		public final long[] storeAll(final Object... instances)
		{
			final long[] objectIds;
			final PersistenceStorer persister = this.createStorer();
			
			synchronized(this.storeMutex)
			{
				objectIds = persister.storeAll(instances);
				persister.commit();
			}

			return objectIds;
		}
		
		@Override
		public void storeAll(final Iterable<?> instances)
		{
			final PersistenceStorer persister = this.createStorer();
			
			synchronized(this.storeMutex)
			{
				persister.storeAll(instances);
				persister.commit();
			}
		}
		
		@Override
		public final long ensureObjectId(final Object object)
		{
			this.typeHandlerManager.ensureTypeHandler(object.getClass());
			return this.objectManager.ensureObjectId(object);
		}
		
		@Override
		public final <T> long ensureObjectId(
			final T                               object           ,
			final PersistenceObjectIdRequestor<D> objectIdRequestor,
			final PersistenceTypeHandler<D, T>    optionalHandler
		)
		{
			this.typeHandlerManager.ensureTypeHandler(object.getClass());
			return this.objectManager.ensureObjectId(object, objectIdRequestor, optionalHandler);
		}
		
		@Override
		public final <T> long ensureObjectIdGuaranteedRegister(
			final T                               object           ,
			final PersistenceObjectIdRequestor<D> objectIdRequestor,
			final PersistenceTypeHandler<D, T>    optionalHandler
		)
		{
			this.typeHandlerManager.ensureTypeHandler(object.getClass());
			return this.objectManager.ensureObjectIdGuaranteedRegister(object, objectIdRequestor, optionalHandler);
		}

		@Override
		public long currentObjectId()
		{
			return this.objectManager.currentObjectId();
		}

		@Override
		public final long lookupObjectId(final Object object)
		{
			return this.objectRegistry.lookupObjectId(object);
		}

		@Override
		public final Object lookupObject(final long objectId)
		{
			return this.objectRegistry.lookupObject(objectId);
		}
		
		@Override
		public final boolean registerLocalRegistry(final PersistenceLocalObjectIdRegistry<D> localRegistry)
		{
			return this.objectManager.registerLocalRegistry(localRegistry);
		}
		
		@Override
		public final void mergeEntries(final PersistenceLocalObjectIdRegistry<D> localRegistry)
		{
			this.objectManager.mergeEntries(localRegistry);
		}

		@Override
		public final Object get()
		{
			return this.createLoader().get();
		}

		@Override
		public final <C extends Consumer<Object>> C collect(final C collector, final long... objectIds)
		{
			return this.createLoader().collect(collector, objectIds);
		}

		@Override
		public final Object getObject(final long objectId)
		{
			final Object cachedInstance;
			if((cachedInstance = this.objectManager.lookupObject(objectId)) != null)
			{
				return cachedInstance;
			}
			return this.createLoader().getObject(objectId);
		}

		@Override
		public final PersistenceLoader createLoader()
		{
			return this.loaderCreator.createLoader(
				this.contextDispatcher.dispatchTypeHandlerLookup(this.typeHandlerManager),
				this.contextDispatcher.dispatchObjectRegistry(this.objectRegistry),
				this.getEffectivePersister(),
				this
			);
		}

		@Override
		public final PersistenceSource<D> source()
		{
			return this.source;
		}
		
		@Override
		public final PersistenceTarget<D> target()
		{
			return this.target;
		}
		
		@Override
		public synchronized void close()
		{
			this.target.closeTarget();
			this.source.closeSource();
		}

		@Override
		public synchronized PersistenceManager.Default<D> updateCurrentObjectId(
			final long currentObjectId
		)
		{
			this.objectManager.updateCurrentObjectId(currentObjectId);
			return this;
		}

		@Override
		public synchronized void updateMetadata(
			final PersistenceTypeDictionary typeDictionary ,
			final long                      highestTypeId  ,
			final long                      highestObjectId
		)
		{
			this.typeHandlerManager.update(typeDictionary, highestTypeId);
			this.updateCurrentObjectId(highestObjectId);
		}

	}

}

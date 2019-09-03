package one.microstream.persistence.types;

import static one.microstream.X.notNull;

import java.util.function.Consumer;

import one.microstream.util.BufferSizeProviderIncremental;


public interface PersistenceManager<M>
extends PersistenceObjectManager, PersistenceRetrieving, PersistenceStoring, PersistenceSourceSupplier<M>
{
	// manager methods //
	
	public PersistenceRegisterer createRegisterer();

	public PersistenceLoader<M> createLoader();

	public PersistenceStorer<M> createLazyStorer();
	
	public PersistenceStorer<M> createStorer();

	public PersistenceStorer<M> createEagerStorer();

	public PersistenceStorer<M> createStorer(PersistenceStorer.Creator<M> storerCreator);

	public void updateMetadata(PersistenceTypeDictionary typeDictionary, long highestTypeId, long highestObjectId);

	public default void updateMetadata(final PersistenceTypeDictionary typeDictionary)
	{
		this.updateMetadata(typeDictionary, 0, 0);
	}

	@Override
	public default void storeSelfStoring(final SelfStoring storing)
	{
		storing.storeBy(this.createStorer()).commit();
	}
	
	public PersistenceObjectRegistry objectRegistry();
	
	public PersistenceTypeDictionary typeDictionary();

	@Override
	public long currentObjectId();

	@Override
	public PersistenceManager<M> updateCurrentObjectId(long currentObjectId);
	
	@Override
	public PersistenceSource<M> source();
	
	public PersistenceTarget<M> target();
	
	/**
	 * Closes all ties to outside resources, if applicable. Typ
	 */
	public void close();
	

	
	public static <M> PersistenceManager<M> New(
		final PersistenceObjectRegistry        objectRegistering ,
		final PersistenceObjectManager         objectManager     ,
		final PersistenceTypeHandlerManager<M> typeHandlerManager,
		final PersistenceContextDispatcher<M>  contextDispatcher ,
		final PersistenceStorer.Creator<M>     storerCreator     ,
		final PersistenceLoader.Creator<M>     loaderCreator     ,
		final PersistenceRegisterer.Creator    registererCreator ,
		final PersistenceTarget<M>             target            ,
		final PersistenceSource<M>             source            ,
		final BufferSizeProviderIncremental    bufferSizeProvider
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
			notNull(target)            ,
			notNull(source)            ,
			notNull(bufferSizeProvider)
		);
	}

	public final class Default<M> implements PersistenceManager<M>, Unpersistable
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		// instance registration components //
		private final PersistenceObjectRegistry     objectRegistry   ;
		private final PersistenceObjectManager      objectManager    ;
		private final PersistenceRegisterer.Creator registererCreator;

		// instance handling components //
		private final PersistenceTypeHandlerManager<M> typeHandlerManager;
		private final PersistenceContextDispatcher<M>  contextDispatcher ;
		private final PersistenceStorer.Creator<M>     storerCreator     ;
		private final PersistenceLoader.Creator<M>     loaderCreator     ;
		private final BufferSizeProviderIncremental    bufferSizeProvider;

		// source and target //
		private final PersistenceSource<M> source;
		private final PersistenceTarget<M> target;



		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		Default(
			final PersistenceObjectRegistry        objectRegistering ,
			final PersistenceObjectManager         objectManager     ,
			final PersistenceTypeHandlerManager<M> typeHandlerManager,
			final PersistenceContextDispatcher<M>  contextDispatcher ,
			final PersistenceStorer.Creator<M>     storerCreator     ,
			final PersistenceLoader.Creator<M>     loaderCreator     ,
			final PersistenceRegisterer.Creator    registererCreator ,
			final PersistenceTarget<M>             target            ,
			final PersistenceSource<M>             source            ,
			final BufferSizeProviderIncremental    bufferSizeProvider
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
			this.target             = target            ;
			this.source             = source            ;
			this.bufferSizeProvider = bufferSizeProvider;
		}



		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
						
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
				
		@Override
		public final PersistenceStorer<M> createLazyStorer()
		{
			return this.storerCreator.createLazyStorer(
				this.contextDispatcher.dispatchTypeHandlerManager(this.typeHandlerManager),
				this.contextDispatcher.dispatchObjectManager(this.objectManager),
				this,
				this.target,
				this.bufferSizeProvider
			);
		}
		
		@Override
		public final PersistenceStorer<M> createStorer()
		{
			return this.storerCreator.createStorer(
				this.contextDispatcher.dispatchTypeHandlerManager(this.typeHandlerManager),
				this.contextDispatcher.dispatchObjectManager(this.objectManager),
				this,
				this.target,
				this.bufferSizeProvider
			);
		}

		@Override
		public final PersistenceStorer<M> createEagerStorer()
		{
			return this.storerCreator.createEagerStorer(
				this.contextDispatcher.dispatchTypeHandlerManager(this.typeHandlerManager),
				this.contextDispatcher.dispatchObjectManager(this.objectManager),
				this,
				this.target,
				this.bufferSizeProvider
			);
		}
		
		@Override
		public final PersistenceStorer<M> createStorer(final PersistenceStorer.Creator<M> storerCreator)
		{
			return storerCreator.createStorer(
				this.contextDispatcher.dispatchTypeHandlerManager(this.typeHandlerManager),
				this.contextDispatcher.dispatchObjectManager(this.objectManager),
				this,
				this.target,
				this.bufferSizeProvider
			);
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
			final PersistenceStorer<M> persister;
			final long objectId = (persister = this.createStorer()).store(object);
			persister.commit();
			return objectId;
		}
		
		@Override
		public final long[] storeAll(final Object... instances)
		{
			final PersistenceStorer<M> persister;
			final long[] objectIds = (persister = this.createStorer()).storeAll(instances);
			persister.commit();
			return objectIds;
		}
		
		@Override
		public void storeAll(final Iterable<?> instances)
		{
			final PersistenceStorer<M> persister;
			(persister = this.createStorer()).storeAll(instances);
			persister.commit();
		}
		
		@Override
		public final long ensureObjectId(final Object object)
		{
			this.typeHandlerManager.ensureTypeHandler(object.getClass());
			return this.objectManager.ensureObjectId(object);
		}
		
		@Override
		public final long ensureObjectId(final Object object, final PersistenceAcceptor newObjectIdCallback)
		{
			this.typeHandlerManager.ensureTypeHandler(object.getClass());
			return this.objectManager.ensureObjectId(object, newObjectIdCallback);
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
		public final PersistenceLoader<M> createLoader()
		{
			return this.loaderCreator.createLoader(
				this.contextDispatcher.dispatchTypeHandlerLookup(this.typeHandlerManager),
				this.contextDispatcher.dispatchObjectRegistry(this.objectRegistry),
				this
			);
		}

		@Override
		public final PersistenceSource<M> source()
		{
			return this.source;
		}
		
		@Override
		public final PersistenceTarget<M> target()
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
		public synchronized PersistenceManager.Default<M> updateCurrentObjectId(
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

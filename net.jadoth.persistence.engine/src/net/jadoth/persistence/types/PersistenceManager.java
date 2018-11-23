package net.jadoth.persistence.types;

import static net.jadoth.X.notNull;

import java.util.function.Consumer;

import net.jadoth.util.BufferSizeProviderIncremental;


public interface PersistenceManager<M>
extends PersistenceObjectManager, PersistenceRetrieving, PersistenceStoring, PersistenceSwizzleSupplier<M>
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

	public default void store(final SelfStoring storing)
	{
		storing.storeBy(this.createStorer()).commit();
	}
	
	public PersistenceObjectRegistry swizzleRegistry();
	
	public PersistenceTypeDictionary typeDictionary();
	
	@Override
	public PersistenceSource<M> source();
	
	public PersistenceTarget<M> target();
	
	/**
	 * Closes all ties to outside resources, if applicable. Typ
	 */
	public void close();



	public final class Implementation<M> implements PersistenceManager<M>, Unpersistable
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields  //
		/////////////////////

		// swizzling components //
		private final PersistenceObjectRegistry                  objectRegistry    ;
		private final PersistenceObjectManager             objectManager     ;
		private final PersistenceRegisterer.Creator    registererCreator ;

		// instance handling components //
		private final PersistenceTypeHandlerManager<M> typeHandlerManager;
		private final PersistenceStorer.Creator<M>     storerCreator     ;
		private final PersistenceLoader.Creator<M>     loaderCreator     ;
		private final BufferSizeProviderIncremental    bufferSizeProvider;

		// source and target //
		private final PersistenceSource<M>             source            ;
		private final PersistenceTarget<M>             target            ;



		///////////////////////////////////////////////////////////////////////////
		// constructors     //
		/////////////////////

		public Implementation(
			final PersistenceObjectRegistry                  objectRegistering ,
			final PersistenceObjectManager             objectManager     ,
			final PersistenceTypeHandlerManager<M> typeHandlerManager,
			final PersistenceStorer.Creator<M>     storerCreatorDeep ,
			final PersistenceLoader.Creator<M>     loaderCreator     ,
			final PersistenceRegisterer.Creator    registererCreator ,
			final PersistenceTarget<M>             target            ,
			final PersistenceSource<M>             source            ,
			final BufferSizeProviderIncremental    bufferSizeProvider
		)
		{
			super();
			this.objectRegistry     = notNull(objectRegistering );
			this.objectManager      = notNull(objectManager     );
			this.typeHandlerManager = notNull(typeHandlerManager);
			this.storerCreator      = notNull(storerCreatorDeep );
			this.registererCreator  = notNull(registererCreator );
			this.loaderCreator      = notNull(loaderCreator     );
			this.target             = notNull(target            );
			this.source             = notNull(source            );
			this.bufferSizeProvider = notNull(bufferSizeProvider);
		}



		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		@Override
		public final PersistenceObjectRegistry swizzleRegistry()
		{
			return this.objectRegistry;
		}
		
		@Override
		public final PersistenceTypeDictionary typeDictionary()
		{
			return this.typeHandlerManager.typeDictionary();
		}

		@Override
		public final void cleanUp()
		{
			this.objectRegistry.cleanUp();
		}
				
		@Override
		public final PersistenceStorer<M> createLazyStorer()
		{
			return this.storerCreator.createLazyStorer(
				this.objectManager     ,
				this                   ,
				this.typeHandlerManager,
				this.target            ,
				this.bufferSizeProvider
			);
		}
		
		@Override
		public final PersistenceStorer<M> createStorer()
		{
			return this.storerCreator.createStorer(
				this.objectManager     ,
				this                   ,
				this.typeHandlerManager,
				this.target            ,
				this.bufferSizeProvider
			);
		}

		@Override
		public final PersistenceStorer<M> createEagerStorer()
		{
			return this.storerCreator.createEagerStorer(
				this.objectManager     ,
				this                   ,
				this.typeHandlerManager,
				this.target            ,
				this.bufferSizeProvider
			);
		}
		
		@Override
		public final PersistenceStorer<M> createStorer(final PersistenceStorer.Creator<M> storerCreator)
		{
			return storerCreator.createStorer(
				this.objectManager     ,
				this                   ,
				this.typeHandlerManager,
				this.target            ,
				this.bufferSizeProvider
			);
		}

		@Override
		public final PersistenceRegisterer createRegisterer()
		{
			return this.registererCreator.createRegisterer(this.objectManager, this.typeHandlerManager);
		}

		@Override
		public final long store(final Object object)
		{
			final PersistenceStorer<M> persister;
			final long oid = (persister = this.createStorer()).store(object);
			persister.commit();
			return oid;
		}
		
		@Override
		public final long[] storeAll(final Object... instances)
		{
			final PersistenceStorer<M> persister;
			final long[] oids = (persister = this.createStorer()).storeAll(instances);
			persister.commit();
			return oids;
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
		public final Object lookupObject(final long oid)
		{
			return this.objectRegistry.lookupObject(oid);
		}

		@Override
		public final Object get()
		{
			return this.createLoader().get();
		}

		@Override
		public final <C extends Consumer<Object>> C collect(final C collector, final long... oids)
		{
			return this.createLoader().collect(collector, oids);
		}

		@Override
		public final Object get(final long oid)
		{
			final Object cachedInstance;
			if((cachedInstance = this.objectManager.lookupObject(oid)) != null)
			{
				return cachedInstance;
			}
			return this.createLoader().get(oid);
		}

		@Override
		public final PersistenceLoader<M> createLoader()
		{
			return this.loaderCreator.createBuilder(
				this.typeHandlerManager.createDistrict(this.objectRegistry),
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
		public synchronized void updateCurrentObjectId(final long currentObjectId)
		{
			this.objectManager.updateCurrentObjectId(currentObjectId);
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

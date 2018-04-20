package net.jadoth.persistence.types;

import static net.jadoth.Jadoth.notNull;

import java.util.function.Consumer;

import net.jadoth.swizzling.types.SwizzleObjectManager;
import net.jadoth.swizzling.types.SwizzleRegistry;


public interface PersistenceManager<M>
extends SwizzleObjectManager, PersistenceRetrieving, PersistenceStoring, PersistenceSwizzleSupplier<M>
{
	// manager methods //

	public PersistenceLoader<M> createLoader();

	public PersistenceStorer<M> createStorer();

	public PersistenceStorer<M> createStorer(BufferSizeProvider bufferSizeProvider);

	public PersistenceRegisterer createRegisterer();

	public void updateMetadata(PersistenceTypeDictionary typeDictionary, long highestTypeId, long highestObjectId);

	public default void updateMetadata(final PersistenceTypeDictionary typeDictionary)
	{
		this.updateMetadata(typeDictionary, 0, 0);
	}

	public default void store(final SelfStoring storing)
	{
		storing.storeBy(this.createStorer()).commit();
	}



	public final class Implementation<M> implements PersistenceManager<M>, Unpersistable
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields  //
		/////////////////////

		// swizzling components //
		private final SwizzleRegistry                  objectRegistry    ;
		private final SwizzleObjectManager             objectManager     ;
		private final PersistenceRegisterer.Creator    registererCreator ;

		// instance handling components //
		private final PersistenceTypeHandlerManager<M> typeHandlerManager;
		private final PersistenceStorer.Creator<M>     storerCreator     ;
		private final PersistenceLoader.Creator<M>     builderCreator    ;
		private final BufferSizeProvider               bufferSizeProvider;

		// source and target //
		private final PersistenceSource<M>             source            ;
		private final PersistenceTarget<M>             target            ;



		///////////////////////////////////////////////////////////////////////////
		// constructors     //
		/////////////////////

		public Implementation(
			final SwizzleRegistry                  objectRegistering ,
			final SwizzleObjectManager             objectManager     ,
			final PersistenceTypeHandlerManager<M> typeHandlerManager,
			final PersistenceStorer.Creator<M>     storerCreatorDeep ,
			final PersistenceLoader.Creator<M>     builderCreator    ,
			final PersistenceRegisterer.Creator    registererCreator ,
			final PersistenceTarget<M>             target            ,
			final PersistenceSource<M>             source            ,
			final BufferSizeProvider               bufferSizeProvider
		)
		{
			super();
			this.objectRegistry     = notNull(objectRegistering );
			this.objectManager      = notNull(objectManager     );
			this.typeHandlerManager = notNull(typeHandlerManager);
			this.storerCreator      = notNull(storerCreatorDeep );
			this.registererCreator  = notNull(registererCreator );
			this.builderCreator     = notNull(builderCreator    );
			this.target             = notNull(target            );
			this.source             = notNull(source            );
			this.bufferSizeProvider = notNull(bufferSizeProvider);
		}



		///////////////////////////////////////////////////////////////////////////
		// override methods //
		/////////////////////

		@Override
		public final void cleanUp()
		{
			this.objectRegistry.cleanUp();
		}

		@Override
		public final PersistenceStorer<M> createStorer()
		{
			return this.storerCreator.createPersistenceStorer(
				this.objectManager     ,
				this                   ,
				this.typeHandlerManager,
				this.target            ,
				this.bufferSizeProvider
			);
		}

		@Override
		public final PersistenceStorer<M> createStorer(final BufferSizeProvider bufferSizeProvider)
		{
			return this.storerCreator.createPersistenceStorer(
				this.objectManager     ,
				this                   ,
				this.typeHandlerManager,
				this.target            ,
				bufferSizeProvider
			);
		}

		@Override
		public final PersistenceRegisterer createRegisterer()
		{
			return this.registererCreator.createRegisterer(this.objectManager, this.typeHandlerManager);
		}

		@Override
		public final long storeFull(final Object object)
		{
			final PersistenceStorer<M> persister;
			final long oid = (persister = this.createStorer()).storeFull(object);
			persister.commit();
			return oid;
		}

		@Override
		public final long storeRequired(final Object object)
		{
			final PersistenceStorer<M> persister;
			final long oid = (persister = this.createStorer()).storeRequired(object);
			persister.commit();
			return oid;
		}

		@Override
		public final long[] storeAllFull(final Object... instances)
		{
			final PersistenceStorer<M> persister;
			final long[] oids = (persister = this.createStorer()).storeAllFull(instances);
			persister.commit();
			return oids;
		}

		@Override
		public final long[] storeAllRequired(final Object... instances)
		{
			final PersistenceStorer<M> persister;
			final long[] oids = (persister = this.createStorer()).storeAllRequired(instances);
			persister.commit();
			return oids;
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
		public final Object initialGet()
		{
			return this.createLoader().initialGet();
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
			return this.builderCreator.createBuilder(
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
		public void updateCurrentObjectId(final long currentObjectId)
		{
			this.objectManager.updateCurrentObjectId(currentObjectId);
		}

		@Override
		public void updateMetadata(
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

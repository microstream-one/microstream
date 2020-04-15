package one.microstream.entity;

import static one.microstream.X.notNull;

import java.lang.reflect.Constructor;

import one.microstream.X;
import one.microstream.collections.BulkList;
import one.microstream.collections.types.XGettingList;
import one.microstream.exceptions.NoSuchMethodRuntimeException;
import one.microstream.functional.Instantiator;
import one.microstream.persistence.binary.internal.AbstractBinaryHandlerCustomCollection;
import one.microstream.persistence.binary.types.Binary;
import one.microstream.persistence.types.Persistence;
import one.microstream.persistence.types.PersistenceFunction;
import one.microstream.persistence.types.PersistenceLoadHandler;
import one.microstream.persistence.types.PersistenceReferenceLoader;
import one.microstream.persistence.types.PersistenceStoreHandler;

public class BinaryHandlerEntityLayerIdentity<T extends EntityLayerIdentity> 
	extends AbstractBinaryHandlerCustomCollection<T>
{
	public static <T extends EntityLayerIdentity> BinaryHandlerEntityLayerIdentity<T> New(
		final Class<T>                 type                    ,
		final EntityTypeHandlerManager entityTypeHandlerManager
	)
	{
		return new BinaryHandlerEntityLayerIdentity<>(
			type, 
			WrapDefaultConstructor(type),
			notNull(entityTypeHandlerManager)
		);
	}
		
	private static <T> Instantiator<T> WrapDefaultConstructor(final Class<T> type)
		throws NoSuchMethodRuntimeException
	{
		try
		{
			final Constructor<T> constructor = type.getDeclaredConstructor();
			constructor.setAccessible(true);
			return Instantiator.WrapDefaultConstructor(constructor);
		}
		catch(final NoSuchMethodException e)
		{
			throw new NoSuchMethodRuntimeException(e);
		}
		catch(final SecurityException e)
		{
			throw new RuntimeException(e);
		}
	}
	
	private static XGettingList<Entity> collectLayers(
		final EntityLayerIdentity identity
	)
	{
		final BulkList<Entity> layers = BulkList.New();
		Entity layer = identity;
		while((layer = inner(layer)) != null)
		{
			layers.add(layer);
		}
		return layers;
	}
	
	private static Entity inner(
		final Entity entity
	)
	{
		return entity instanceof EntityLayer
			? ((EntityLayer)entity).inner()
			: null;
	}
	
	static final long BINARY_OFFSET_LAYERS = 0;
	
	
	private final Instantiator<T>          instantiator            ;
	private final EntityTypeHandlerManager entityTypeHandlerManager;
		
	BinaryHandlerEntityLayerIdentity(
		final Class<T>                 type                    ,
		final Instantiator<T>          instantiator            ,
		final EntityTypeHandlerManager entityTypeHandlerManager
	)
	{
		super(
			type,
			CustomFields(
				Complex("layers",
					CustomField(Entity.class, "layer")
				)
			)
		);
		this.instantiator             = instantiator            ;
		this.entityTypeHandlerManager = entityTypeHandlerManager;
	}
	
	@Override
	public final void store(
		final Binary                          data    ,
		final T                               instance,
		final long                            objectId,
		final PersistenceStoreHandler<Binary> handler
	)
	{
		final Entity[]        layers    = collectLayers(instance).toArray(Entity.class);
		final EntityPersister persister = EntityPersister.New(
			this.entityTypeHandlerManager, 
			handler
		);
		data.storeReferences(
			this.typeId()       ,
			objectId            ,
			BINARY_OFFSET_LAYERS,
			persister           ,
			layers              ,
			0                   ,
			layers.length       
		);
	}
	
	@Override
	public final T create(
		final Binary                 data   , 
		final PersistenceLoadHandler handler
	)
	{
		return this.instantiator.instantiate();
	}

	@Override
	public final void updateState(
		final Binary                 data    , 
		final T                      identity, 
		final PersistenceLoadHandler handler
	)
	{
		final int count = X.checkArrayRange(
			data.getListElementCountReferences(BINARY_OFFSET_LAYERS)
		);
		
		final Entity[] entities = new Entity[count];
		data.validateArrayLength(entities, BINARY_OFFSET_LAYERS);
		data.collectElementsIntoArray(BINARY_OFFSET_LAYERS, handler, entities);
		
		// count is always > 0, because minimum layered entity setup is identity and data
		identity.setInner(entities[0]);
		for(int i = 0; i < count - 1; i++)
		{
			((EntityLayer)entities[i]).setInner(entities[i + 1]);
		}
	}
	
	@Override
	public void iterateInstanceReferences(T instance, PersistenceFunction iterator)
	{
		Persistence.iterateReferences(iterator, collectLayers(instance));
	}

	@Override
	public final void iterateLoadableReferences(
		final Binary                     data    , 
		final PersistenceReferenceLoader iterator
	)
	{
		data.iterateListElementReferences(BINARY_OFFSET_LAYERS, iterator);
	}
	
}

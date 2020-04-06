package one.microstream.entity;

import java.lang.reflect.Constructor;

import one.microstream.X;
import one.microstream.collections.BulkList;
import one.microstream.collections.types.XGettingList;
import one.microstream.exceptions.NoSuchMethodRuntimeException;
import one.microstream.functional.Instantiator;
import one.microstream.persistence.binary.internal.AbstractBinaryHandlerCustom;
import one.microstream.persistence.binary.types.Binary;
import one.microstream.persistence.types.Persistence;
import one.microstream.persistence.types.PersistenceFunction;
import one.microstream.persistence.types.PersistenceLoadHandler;
import one.microstream.persistence.types.PersistenceReferenceLoader;
import one.microstream.persistence.types.PersistenceStoreHandler;

public class BinaryHandlerEntityLayerIdentity<T extends EntityLayerIdentity> 
	extends AbstractBinaryHandlerCustom<T>
{
	public static <T extends EntityLayerIdentity> BinaryHandlerEntityLayerIdentity<T> New(
		final Class<T> type
	)
	{
		return New(type, WrapDefaultConstructor(type));
	}
	
	public static <T extends EntityLayerIdentity> BinaryHandlerEntityLayerIdentity<T> New(
		final Class<T>        type        ,
		final Instantiator<T> instantiator
	)
	{
		return new BinaryHandlerEntityLayerIdentity<>(type, instantiator);
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
	
	
	private final Instantiator<T> instantiator;
	
	
	BinaryHandlerEntityLayerIdentity(
		final Class<T>        type        ,
		final Instantiator<T> instantiator
	)
	{
		super(
			type,
			SimpleArrayFields()
		);
		this.instantiator = instantiator;
	}
	
	@Override
	public final void store(
		final Binary                  data    ,
		final T                       instance,
		final long                    objectId,
		final PersistenceStoreHandler handler
	)
	{
		final Object[] layers = collectLayers(instance).toArray();
				
		data.storeEntityHeader(
			Binary.calculateReferenceListTotalBinaryLength(layers.length),
			this.typeId(),
			objectId
		);

		data.storeListHeader(
			0L,
			Binary.referenceBinaryLength(layers.length),
			layers.length
		);
		
		long offset = Long.BYTES * 2; // list header

		for(int i = 0; i < layers.length; i++)
		{
			data.store_long(
				offset + Binary.referenceBinaryLength(i),
				handler.applyEager(layers[i])
			);
		}
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
			data.getListElementCountReferences(0)
		);
		if(count > 0)
		{
			final Entity[] entities = new Entity[count];
			data.collectElementsIntoArray(0, handler, entities);
			
			identity.setInner(entities[0]);
			for(int i = 0; i < count - 1; i++)
			{
				((EntityLayer)entities[i]).setInner(entities[i + 1]);
			}
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
		data.iterateListElementReferences(0, iterator);
	}	
	
	@Override
	public final boolean hasPersistedReferences()
	{
		return true;
	}
	
	@Override
	public boolean hasPersistedVariableLength()
	{
		return true;
	}
	
	@Override
	public boolean hasVaryingPersistedLengthInstances()
	{
		return true;
	}
	
}

package one.microstream.entity;

import java.lang.reflect.Constructor;

import one.microstream.X;
import one.microstream.collections.BulkList;
import one.microstream.collections.MiniMap;
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
import one.microstream.persistence.types.PersistenceTypeHandler;
import one.microstream.persistence.types.PersistenceTypeHandlerCreator;
import one.microstream.persistence.types.PersistenceTypeHandlerManager;
import one.microstream.reference.Referencing;

public class BinaryHandlerEntityLayerIdentity<T extends EntityLayerIdentity> 
	extends AbstractBinaryHandlerCustom<T>
{
	public static <T extends EntityLayerIdentity> BinaryHandlerEntityLayerIdentity<T> New(
		final Class<T>                                           type              ,
		final Referencing<PersistenceTypeHandlerManager<Binary>> typeHandlerManager,
		final PersistenceTypeHandlerCreator<Binary>              typeHandlerCreator
	)
	{
		return new BinaryHandlerEntityLayerIdentity<>(
			type, 
			WrapDefaultConstructor(type),
			typeHandlerManager,
			typeHandlerCreator);
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
	
	
	private final Instantiator<T>                                      instantiator      ;
	private final Referencing<PersistenceTypeHandlerManager<Binary>>   typeHandlerManager;
	private final PersistenceTypeHandlerCreator<Binary>                typeHandlerCreator;
	private       MiniMap<Class<?>, PersistenceTypeHandler<Binary, ?>> internalHandlers  ;
		
	BinaryHandlerEntityLayerIdentity(
		final Class<T>                                           type              ,
		final Instantiator<T>                                    instantiator      ,
		final Referencing<PersistenceTypeHandlerManager<Binary>> typeHandlerManager,
		final PersistenceTypeHandlerCreator<Binary>              typeHandlerCreator
	)
	{
		super(
			type,
			X.ConstList(
				Complex("layers",
					CustomField(Entity.class, "layer")
				)
			)
		);
		this.instantiator       = instantiator      ;
		this.typeHandlerManager = typeHandlerManager;
		this.typeHandlerCreator = typeHandlerCreator;
	}
	
	@SuppressWarnings("unchecked")
	private <E extends Entity> PersistenceTypeHandler<Binary, E> ensureInternalHandler(
		final E entity
	)
	{
		MiniMap<Class<?>, PersistenceTypeHandler<Binary, ?>> internalHandlers;
		if((internalHandlers = this.internalHandlers) == null)
		{
			internalHandlers = this.internalHandlers = new MiniMap<>();
		}		
		final Class<E>                    type   = (Class<E>)entity.getClass();
		PersistenceTypeHandler<Binary, E> handler;
		if((handler = (PersistenceTypeHandler<Binary, E>)internalHandlers.get(type)) == null)
		{
			handler = this.createInternalHandler(type);
			PersistenceTypeHandlerManager<Binary> typeHandlerManager = this.typeHandlerManager.get();
			typeHandlerManager.registerTypeHandler(
				handler.initialize(
					typeHandlerManager.ensureTypeId(type)
				)
			);
			internalHandlers.put(
				type, 
				handler
			);
		}
		return (PersistenceTypeHandler<Binary, E>)handler;		
	}
	
	@SuppressWarnings("unchecked")
	private <E extends Entity> PersistenceTypeHandler<Binary, E> createInternalHandler(
		final Class<E> type
	)
	{
		if(EntityLayerVersioning.class.isAssignableFrom(type))
		{
			return (PersistenceTypeHandler<Binary, E>)new BinaryHandlerEntityLayerVersioning();
		}
		
		return this.typeHandlerCreator.createTypeHandlerGeneric(type);
	}
	
	@Override
	public final void store(
		final Binary                          data    ,
		final T                               instance,
		final long                            objectId,
		final PersistenceStoreHandler<Binary> handler
	)
	{
		final Entity[] layers = collectLayers(instance).toArray(Entity.class);
				
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
				handler.applyEager(
					layers[i], 
					this.ensureInternalHandler(layers[i])
				)
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
		
		// count is always > 0, because minimum layered entity setup is identity and data
		
		final Entity[] entities = new Entity[count];
		data.collectElementsIntoArray(0, handler, entities);
			
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

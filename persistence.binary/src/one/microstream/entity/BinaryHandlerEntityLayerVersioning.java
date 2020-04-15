package one.microstream.entity;

import static one.microstream.X.notNull;

import java.lang.reflect.Field;

import one.microstream.X;
import one.microstream.collections.EqHashTable;
import one.microstream.collections.old.KeyValueFlatCollector;
import one.microstream.memory.XMemory;
import one.microstream.persistence.binary.internal.AbstractBinaryHandlerCustom;
import one.microstream.persistence.binary.types.Binary;
import one.microstream.persistence.binary.types.BinaryTypeHandler;
import one.microstream.persistence.types.Persistence;
import one.microstream.persistence.types.PersistenceFunction;
import one.microstream.persistence.types.PersistenceLoadHandler;
import one.microstream.persistence.types.PersistenceReferenceLoader;
import one.microstream.persistence.types.PersistenceStoreHandler;

public class BinaryHandlerEntityLayerVersioning
	extends AbstractBinaryHandlerCustom<EntityLayerVersioning<?>>
	implements BinaryHandlerEntityLoading<EntityLayerVersioning<?>>
{
	public static BinaryHandlerEntityLayerVersioning New(
		final EntityTypeHandlerManager entityTypeHandlerManager
	)
	{
		return new BinaryHandlerEntityLayerVersioning(
			notNull(entityTypeHandlerManager)
		);
	}
	
	
	static final long BINARY_OFFSET_CONTEXT  =                                                   0;
	static final long BINARY_OFFSET_VERSIONS = BINARY_OFFSET_CONTEXT + Binary.objectIdByteLength();
	
	static final Field
		FIELD_CONTEXT = getInstanceFieldOfType(EntityLayerVersioning.class, EntityVersionContext.class);
	
	@SuppressWarnings({"unchecked", "rawtypes"})
	private static Class<EntityLayerVersioning<?>> handledType()
	{
		return (Class)EntityLayerVersioning.class; // no idea how to get ".class" to work otherwise
	}
	
	
	final EntityTypeHandlerManager entityTypeHandlerManager;
	
	BinaryHandlerEntityLayerVersioning(
		final EntityTypeHandlerManager entityTypeHandlerManager
	)
	{
		super(
			handledType(),
			CustomFields(
				CustomField(EntityVersionContext.class, "context"),
				Complex("versions",
					CustomField(Object.class, "version"),
					CustomField(Object.class, "entity")
				)
			)
		);
		this.entityTypeHandlerManager = entityTypeHandlerManager;
	}
	
	@Override
	public final EntityLayerVersioning<?> create(
		final Binary                 data   ,
		final PersistenceLoadHandler handler
	)
	{
		return new EntityLayerVersioning<>();
	}

	@Override
	public final void updateState(
		final Binary                   data    ,
		final EntityLayerVersioning<?> instance,
		final PersistenceLoadHandler   handler
	)
	{
		XMemory.setObject(
			instance,
			XMemory.objectFieldOffset(FIELD_CONTEXT),
			handler.lookupObject(data.read_long(BINARY_OFFSET_CONTEXT))
		);
		
		final int elementCount = X.checkArrayRange(data.getListElementCountKeyValue(BINARY_OFFSET_VERSIONS));
		final KeyValueFlatCollector<Object, Object> collector = KeyValueFlatCollector.New(elementCount);
		data.collectKeyValueReferences(BINARY_OFFSET_VERSIONS, elementCount, handler, collector);
		data.registerHelper(instance, collector.yield());
	}
	
	@SuppressWarnings({"rawtypes", "unchecked"})
	@Override
	public void complete(
		final Binary                   data    ,
		final EntityLayerVersioning    instance,
		final PersistenceLoadHandler   handler
	)
	{
		final Object[] elements = (Object[])data.getHelper(instance);
		instance.versions = EqHashTable.New(instance.context.equalator());
		for(int i = 0; i < elements.length; )
		{
			instance.versions.put(elements[i++], elements[i++]);
		}
	}
	
	@Override
	public void iterateInstanceReferences(
		final EntityLayerVersioning<?> instance,
		final PersistenceFunction      iterator
	)
	{
		iterator.apply(instance.context);
		Persistence.iterateReferences(iterator, instance.versions);
	}

	@Override
	public final void iterateLoadableReferences(
		final Binary                     data    ,
		final PersistenceReferenceLoader iterator
	)
	{
		iterator.acceptObjectId(data.read_long(BINARY_OFFSET_CONTEXT));
		data.iterateKeyValueEntriesReferences(BINARY_OFFSET_VERSIONS, iterator);
	}
	
	@Override
	public final boolean hasPersistedReferences()
	{
		return true;
	}
	
	@Override
	public boolean hasVaryingPersistedLengthInstances()
	{
		return true;
	}
		
	@Override
	public void store(
		final Binary                          data    ,
		final EntityLayerVersioning<?>        instance,
		final long                            objectId,
		final PersistenceStoreHandler<Binary> handler
	)
	{
		BinaryHandlerEntityLoading.super.store(data, instance, objectId, handler);
	}
	
	@Override
	public BinaryTypeHandler<EntityLayerVersioning<?>> createStoringEntityHandler()
	{
		final Storing storing = new Storing(this.entityTypeHandlerManager);
		storing.initialize(this.typeId());
		return storing;
	}
	
	
	static class Storing extends BinaryHandlerEntityLayerVersioning
	{
		Storing(
			final EntityTypeHandlerManager entityTypeHandlerManager
		)
		{
			super(entityTypeHandlerManager);
		}
	
		@Override
		public void store(
			final Binary                          data    ,
			final EntityLayerVersioning<?>        instance,
			final long                            objectId,
			final PersistenceStoreHandler<Binary> handler
		)
		{
			data.storeKeyValuesAsEntries(
				this.typeId()           ,
				objectId                ,
				BINARY_OFFSET_VERSIONS  ,
				instance.versions       ,
				instance.versions.size(),
				EntityPersister.New(this.entityTypeHandlerManager, handler)
			);
			
			data.store_long(
				BINARY_OFFSET_CONTEXT,
				handler.applyEager(instance.context)
			);
		}
		
	}
	
}

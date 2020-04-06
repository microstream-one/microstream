package one.microstream.entity;

import java.lang.reflect.Field;

import one.microstream.collections.EqHashTable;
import one.microstream.memory.XMemory;
import one.microstream.persistence.binary.internal.AbstractBinaryHandlerCustom;
import one.microstream.persistence.binary.types.Binary;
import one.microstream.persistence.types.PersistenceFunction;
import one.microstream.persistence.types.PersistenceLoadHandler;
import one.microstream.persistence.types.PersistenceReferenceLoader;
import one.microstream.persistence.types.PersistenceStoreHandler;

public class BinaryHandlerEntityLayerVersioning 
	extends AbstractBinaryHandlerCustom<EntityLayerVersioning<?>>
{	
	static final long
		BINARY_OFFSET_CONTEXT  =                                                   0,
		BINARY_OFFSET_VERSIONS = BINARY_OFFSET_CONTEXT + Binary.objectIdByteLength();
	
	static final Field
		FIELD_CONTEXT  = getInstanceFieldOfType(EntityLayerVersioning.class, EntityVersionContext.class),
		FIELD_VERSIONS = getInstanceFieldOfType(EntityLayerVersioning.class, EqHashTable.class         );

	
	@SuppressWarnings({"unchecked", "rawtypes"})
	private static Class<EntityLayerVersioning<?>> handledType()
	{
		return (Class)EntityLayerVersioning.class; // no idea how to get ".class" to work otherwise
	}
	
	public static BinaryHandlerEntityLayerVersioning New()
	{
		return new BinaryHandlerEntityLayerVersioning();
	}
	
	
	BinaryHandlerEntityLayerVersioning()
	{
		super(
			handledType(),
			CustomFields(
				CustomField(EntityVersionContext.class, "entityVersionContext"),
				CustomField(EqHashTable.class         , "versions"            )
			)
		);
	}
	
	@Override
	public final void store(
		final Binary                   data    ,
		final EntityLayerVersioning<?> instance,
		final long                     objectId,
		final PersistenceStoreHandler  handler
	)
	{
		data.storeEntityHeader(
			Long.BYTES * 2, 
			this.typeId(), 
			objectId
		);
		
		data.store_long(
			BINARY_OFFSET_CONTEXT,
			handler.applyEager(instance.context)
		);
		data.store_long(
			BINARY_OFFSET_VERSIONS,
			handler.applyEager(instance.versions)
		);
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
		XMemory.setObject(
			instance,
			XMemory.objectFieldOffset(FIELD_VERSIONS),
			handler.lookupObject(data.read_long(BINARY_OFFSET_VERSIONS))
		);
	}
	
	@Override
	public void iterateInstanceReferences(
		final EntityLayerVersioning<?> instance, 
		final PersistenceFunction      iterator
	)
	{
		iterator.apply(instance.context);
		iterator.apply(instance.versions);		
	}

	@Override
	public final void iterateLoadableReferences(
		final Binary                     data    , 
		final PersistenceReferenceLoader iterator
	)
	{
		iterator.acceptObjectId(data.read_long(BINARY_OFFSET_CONTEXT));
		iterator.acceptObjectId(data.read_long(BINARY_OFFSET_VERSIONS));
	}
	
	@Override
	public final boolean hasPersistedReferences()
	{
		return true;
	}
	
	@Override
	public boolean hasPersistedVariableLength()
	{
		return false;
	}
	
	@Override
	public boolean hasVaryingPersistedLengthInstances()
	{
		return false;
	}
	
}

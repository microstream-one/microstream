package one.microstream.java.util;

import java.util.LinkedHashMap;

import one.microstream.X;
import one.microstream.chars.XChars;
import one.microstream.collections.old.JavaUtilMapEntrySetFlattener;
import one.microstream.memory.XMemory;
import one.microstream.persistence.binary.internal.AbstractBinaryHandlerCustomCollection;
import one.microstream.persistence.binary.types.Binary;
import one.microstream.persistence.binary.types.BinaryCollectionHandling;
import one.microstream.persistence.types.Persistence;
import one.microstream.persistence.types.PersistenceFunction;
import one.microstream.persistence.types.PersistenceLoadHandler;
import one.microstream.persistence.types.PersistenceObjectIdAcceptor;
import one.microstream.persistence.types.PersistenceStoreHandler;


public final class BinaryHandlerLinkedHashMap extends AbstractBinaryHandlerCustomCollection<LinkedHashMap<?, ?>>
{
	///////////////////////////////////////////////////////////////////////////
	// constants        //
	/////////////////////

	static final long BINARY_OFFSET_LOAD_FACTOR  =                                        0;
	static final long BINARY_OFFSET_ACCESS_ORDER = BINARY_OFFSET_LOAD_FACTOR  + Float.BYTES;
	static final long BINARY_OFFSET_ELEMENTS     = BINARY_OFFSET_ACCESS_ORDER + Byte.BYTES ; // actually Boolean.BYTES



	///////////////////////////////////////////////////////////////////////////
	// static methods    //
	/////////////////////

	@SuppressWarnings({"unchecked", "rawtypes"})
	private static Class<LinkedHashMap<?, ?>> typeWorkaround()
	{
		return (Class)LinkedHashMap.class; // no idea how to get ".class" to work otherwise
	}

	static final float getLoadFactor(final Binary bytes)
	{
		return bytes.get_float(BINARY_OFFSET_LOAD_FACTOR);
	}

	static final boolean getAccessOrder(final Binary bytes)
	{
		return bytes.get_boolean(BINARY_OFFSET_ACCESS_ORDER);
	}

	static final int getElementCount(final Binary bytes)
	{
		return X.checkArrayRange(bytes.getListElementCountReferences(BINARY_OFFSET_ELEMENTS));
	}



	///////////////////////////////////////////////////////////////////////////
	// constructors     //
	/////////////////////

	public BinaryHandlerLinkedHashMap()
	{
		super(
			typeWorkaround(),
			BinaryCollectionHandling.elementsPseudoFields(
				pseudoField(float.class,   "loadFactor"),
				pseudoField(boolean.class, "accessOrder")
			)
		);
	}



	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////

	@Override
	public final void store(
		final Binary                  bytes   ,
		final LinkedHashMap<?, ?>     instance,
		final long                    objectId,
		final PersistenceStoreHandler handler
	)
	{
		// store elements simply as array binary form
		final long contentAddress = bytes.storeSizedIterableAsList(
			this.typeId()         ,
			objectId              ,
			BINARY_OFFSET_ELEMENTS,
			() ->
				JavaUtilMapEntrySetFlattener.New(instance),
			instance.size() * 2   ,
			handler
		);

		bytes.store_float(
			contentAddress + BINARY_OFFSET_LOAD_FACTOR,
			XMemory.accessLoadFactor(instance)
		);
		bytes.store_boolean(
			contentAddress + BINARY_OFFSET_ACCESS_ORDER,
			XMemory.accessAccessOrder(instance)
		);
	}
	
	

	@Override
	public final LinkedHashMap<?, ?> create(final Binary bytes)
	{
		return new LinkedHashMap<>(
			getElementCount(bytes) / 2,
			getLoadFactor(bytes),
			getAccessOrder(bytes)
		);
	}

	@Override
	public final void update(final Binary bytes, final LinkedHashMap<?, ?> instance, final PersistenceLoadHandler builder)
	{
		final int      elementCount   = getElementCount(bytes);
		final Object[] elementsHelper = new Object[elementCount];
		
		bytes.collectElementsIntoArray(BINARY_OFFSET_ELEMENTS, builder, elementsHelper);
		bytes.registerHelper(instance, elementsHelper);
	}

	@Override
	public void complete(final Binary rawData, final LinkedHashMap<?, ?> instance, final PersistenceLoadHandler handler)
	{
		final Object helper = rawData.getHelper(instance);
		if(helper == null)
		{
			// (22.04.2016 TM)EXCP: proper exception
			throw new RuntimeException(
				"Missing element collection helper instance for " + XChars.systemString(instance)
			);
		}
		
		if(!(helper instanceof Object[]))
		{
			// (22.04.2016 TM)EXCP: proper exception
			throw new RuntimeException(
				"Inconsistent element collection helper instance for " + XChars.systemString(instance)
			);
		}
		
		final Object[] elementsHelper = (Object[])helper;
		@SuppressWarnings("unchecked")
		final LinkedHashMap<Object, Object> castedInstance = (LinkedHashMap<Object, Object>)instance;
		
		for(int i = 0; i < elementsHelper.length; i += 2)
		{
			if(castedInstance.putIfAbsent(elementsHelper[i], elementsHelper[i + 1]) != null)
			{
				// (22.04.2016 TM)EXCP: proper exception
				throw new RuntimeException(
					"Element hashing inconsistency in " + XChars.systemString(castedInstance)
				);
			}
		}
	}

	@Override
	public final void iterateInstanceReferences(final LinkedHashMap<?, ?> instance, final PersistenceFunction iterator)
	{
		Persistence.iterateReferencesMap(iterator, instance);
	}

	@Override
	public final void iteratePersistedReferences(final Binary bytes, final PersistenceObjectIdAcceptor iterator)
	{
		bytes.iterateListElementReferences(BINARY_OFFSET_ELEMENTS, iterator);
	}
	
}

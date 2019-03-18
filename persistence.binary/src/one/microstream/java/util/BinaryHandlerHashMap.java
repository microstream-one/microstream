package one.microstream.java.util;

import java.util.HashMap;
import java.util.Map;

import one.microstream.X;
import one.microstream.chars.XChars;
import one.microstream.collections.old.JavaUtilMapEntrySetFlattener;
import one.microstream.memory.XMemory;
import one.microstream.persistence.binary.internal.AbstractBinaryHandlerCustomCollection;
import one.microstream.persistence.binary.types.Binary;
import one.microstream.persistence.binary.types.BinaryCollectionHandling;
import one.microstream.persistence.types.PersistenceFunction;
import one.microstream.persistence.types.PersistenceLoadHandler;
import one.microstream.persistence.types.PersistenceObjectIdAcceptor;
import one.microstream.persistence.types.PersistenceStoreHandler;


public final class BinaryHandlerHashMap extends AbstractBinaryHandlerCustomCollection<HashMap<?, ?>>
{
	///////////////////////////////////////////////////////////////////////////
	// constants        //
	/////////////////////

	static final long BINARY_OFFSET_LOAD_FACTOR =           0; // 1 float at offset 0
	static final long BINARY_OFFSET_ELEMENTS    = Float.BYTES; // sized array at offset 0 + float size



	///////////////////////////////////////////////////////////////////////////
	// static methods    //
	/////////////////////

	@SuppressWarnings({"unchecked", "rawtypes"})
	private static Class<HashMap<?, ?>> typeWorkaround()
	{
		return (Class)HashMap.class; // no idea how to get ".class" to work otherwise
	}

	static final float getLoadFactor(final Binary bytes)
	{
		return bytes.get_float(BINARY_OFFSET_LOAD_FACTOR);
	}

	static final int getElementCount(final Binary bytes)
	{
		return X.checkArrayRange(bytes.getListElementCountReferences(BINARY_OFFSET_ELEMENTS));
	}



	///////////////////////////////////////////////////////////////////////////
	// constructors     //
	/////////////////////

	public BinaryHandlerHashMap()
	{
		super(
			typeWorkaround(),
			BinaryCollectionHandling.elementsPseudoFields(
				pseudoField(float.class, "loadFactor")
			)
		);
	}



	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////

	@Override
	public final void store(
		final Binary                  bytes   ,
		final HashMap<?, ?>           instance,
		final long                    oid     ,
		final PersistenceStoreHandler handler
	)
	{
		// store elements simply as array binary form
		final long contentAddress = bytes.storeSizedIterableAsList(
			this.typeId()         ,
			oid                   ,
			BINARY_OFFSET_ELEMENTS,
			() ->
				JavaUtilMapEntrySetFlattener.New(instance),
			instance.size() * 2   ,
			handler
		);

		// store load factor as (sole) header value
		bytes.store_float(
			contentAddress + BINARY_OFFSET_LOAD_FACTOR,
			XMemory.accessLoadFactor(instance)
		);
	}

	@Override
	public final HashMap<?, ?> create(final Binary bytes)
	{
		return new HashMap<>(
			getElementCount(bytes) / 2,
			getLoadFactor(bytes)
		);
	}

	@Override
	public final void update(final Binary bytes, final HashMap<?, ?> instance, final PersistenceLoadHandler builder)
	{
		final int      elementCount   = getElementCount(bytes);
		final Object[] elementsHelper = new Object[elementCount];
		
		bytes.collectElementsIntoArray(BINARY_OFFSET_ELEMENTS, builder, elementsHelper);
		bytes.registerHelper(instance, elementsHelper);
	}

	@Override
	public void complete(final Binary rawData, final HashMap<?, ?> instance, final PersistenceLoadHandler loadHandler)
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
		final HashMap<Object, Object> castedInstance = (HashMap<Object, Object>)instance;
		
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
	public final void iterateInstanceReferences(final HashMap<?, ?> instance, final PersistenceFunction iterator)
	{
		for(final Map.Entry<?, ?> entry : instance.entrySet())
		{
			iterator.apply(entry.getKey());
			iterator.apply(entry.getValue());
		}
	}

	@Override
	public final void iteratePersistedReferences(final Binary bytes, final PersistenceObjectIdAcceptor iterator)
	{
		bytes.iterateListElementReferences(BINARY_OFFSET_ELEMENTS, iterator);
	}
	
}

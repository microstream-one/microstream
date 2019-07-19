package one.microstream.jdk8.java.util;

import java.util.HashMap;

import one.microstream.X;
import one.microstream.collections.old.KeyValueFlatCollector;
import one.microstream.collections.old.OldCollections;
import one.microstream.memory.XMemoryJDK8;
import one.microstream.persistence.binary.internal.AbstractBinaryHandlerCustomCollection;
import one.microstream.persistence.binary.types.Binary;
import one.microstream.persistence.types.Persistence;
import one.microstream.persistence.types.PersistenceFunction;
import one.microstream.persistence.types.PersistenceLoadHandler;
import one.microstream.persistence.types.PersistenceObjectIdAcceptor;
import one.microstream.persistence.types.PersistenceStoreHandler;


public final class BinaryHandlerHashMap extends AbstractBinaryHandlerCustomCollection<HashMap<?, ?>>
{
	///////////////////////////////////////////////////////////////////////////
	// constants //
	//////////////

	static final long BINARY_OFFSET_LOAD_FACTOR =                                       0;
	static final long BINARY_OFFSET_ELEMENTS    = BINARY_OFFSET_LOAD_FACTOR + Float.BYTES;



	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////

	@SuppressWarnings({"unchecked", "rawtypes"})
	private static Class<HashMap<?, ?>> handledType()
	{
		return (Class)HashMap.class; // no idea how to get ".class" to work otherwise
	}

	static final float getLoadFactor(final Binary bytes)
	{
		return bytes.get_float(BINARY_OFFSET_LOAD_FACTOR);
	}

	static final int getElementCount(final Binary bytes)
	{
		return X.checkArrayRange(bytes.getListElementCountKeyValue(BINARY_OFFSET_ELEMENTS));
	}



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public BinaryHandlerHashMap()
	{
		super(
			handledType(),
			keyValuesFields(
				CustomField(float.class, "loadFactor")
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
		final long                    objectId,
		final PersistenceStoreHandler handler
	)
	{
		// store elements simply as array binary form
		final long contentAddress = bytes.storeMapEntrySet(
			this.typeId()         ,
			objectId              ,
			BINARY_OFFSET_ELEMENTS,
			instance.entrySet()   ,
			handler
		);

		// store load factor as (sole) header value
		bytes.store_float(
			contentAddress + BINARY_OFFSET_LOAD_FACTOR,
			XMemoryJDK8.getLoadFactor(instance)
		);
	}

	@Override
	public final HashMap<?, ?> create(final Binary bytes, final PersistenceLoadHandler handler)
	{
		return new HashMap<>(
			getElementCount(bytes),
			getLoadFactor(bytes)
		);
	}

	@Override
	public final void update(final Binary bytes, final HashMap<?, ?> instance, final PersistenceLoadHandler handler)
	{
		instance.clear();
		final int elementCount = getElementCount(bytes);
		final KeyValueFlatCollector<Object, Object> collector = KeyValueFlatCollector.New(elementCount);
		bytes.collectKeyValueReferences(BINARY_OFFSET_ELEMENTS, elementCount, handler, collector);
		bytes.registerHelper(instance, collector.yield());
	}

	@Override
	public void complete(final Binary bytes, final HashMap<?, ?> instance, final PersistenceLoadHandler handler)
	{
		OldCollections.populateMapFromHelperArray(instance, bytes.getHelper(instance));
	}
	
	@Override
	public final void iterateInstanceReferences(final HashMap<?, ?> instance, final PersistenceFunction iterator)
	{
		Persistence.iterateReferencesMap(iterator, instance);
	}

	@Override
	public final void iterateLoadableReferences(final Binary bytes, final PersistenceObjectIdAcceptor iterator)
	{
		bytes.iterateKeyValueEntriesReferences(BINARY_OFFSET_ELEMENTS, iterator);
	}
	
}

package one.microstream.java.util;

import java.util.LinkedHashMap;

import one.microstream.X;
import one.microstream.collections.old.KeyValueFlatCollector;
import one.microstream.collections.old.OldCollections;
import one.microstream.memory.XMemory;
import one.microstream.persistence.binary.internal.AbstractBinaryHandlerCustomCollection;
import one.microstream.persistence.binary.types.Binary;
import one.microstream.persistence.types.Persistence;
import one.microstream.persistence.types.PersistenceFunction;
import one.microstream.persistence.types.PersistenceLoadHandler;
import one.microstream.persistence.types.PersistenceObjectIdAcceptor;
import one.microstream.persistence.types.PersistenceStoreHandler;


public final class BinaryHandlerLinkedHashMap extends AbstractBinaryHandlerCustomCollection<LinkedHashMap<?, ?>>
{
	///////////////////////////////////////////////////////////////////////////
	// constants //
	//////////////

	static final long BINARY_OFFSET_LOAD_FACTOR  =                                        0;
	static final long BINARY_OFFSET_ACCESS_ORDER = BINARY_OFFSET_LOAD_FACTOR  + Float.BYTES;
	static final long BINARY_OFFSET_ELEMENTS     = BINARY_OFFSET_ACCESS_ORDER + Byte.BYTES ; // actually Boolean.BYTES



	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////

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
		return X.checkArrayRange(bytes.getListElementCountKeyValue(BINARY_OFFSET_ELEMENTS));
	}



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public BinaryHandlerLinkedHashMap()
	{
		super(
			typeWorkaround(),
			keyValuesPseudoFields(
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
		final long contentAddress = bytes.storeMapEntrySet(
			this.typeId()         ,
			objectId              ,
			BINARY_OFFSET_ELEMENTS,
			instance.entrySet()   ,
			handler
		);
		bytes.store_float(
			contentAddress + BINARY_OFFSET_LOAD_FACTOR,
			XMemory.getLoadFactor(instance)
		);
		bytes.store_boolean(
			contentAddress + BINARY_OFFSET_ACCESS_ORDER,
			XMemory.getAccessOrder(instance)
		);
	}

	@Override
	public final LinkedHashMap<?, ?> create(final Binary bytes, final PersistenceLoadHandler handler)
	{
		return new LinkedHashMap<>(
			getElementCount(bytes),
			getLoadFactor(bytes),
			getAccessOrder(bytes)
		);
	}

	@Override
	public final void update(final Binary bytes, final LinkedHashMap<?, ?> instance, final PersistenceLoadHandler handler)
	{
		instance.clear();
		final int elementCount = getElementCount(bytes);
		final KeyValueFlatCollector<Object, Object> collector = KeyValueFlatCollector.New(elementCount);
		bytes.collectKeyValueReferences(BINARY_OFFSET_ELEMENTS, elementCount, handler, collector);
		bytes.registerHelper(instance, collector.yield());
	}

	@Override
	public void complete(final Binary bytes, final LinkedHashMap<?, ?> instance, final PersistenceLoadHandler handler)
	{
		OldCollections.populateMapFromHelperArray(instance, bytes.getHelper(instance));
	}

	@Override
	public final void iterateInstanceReferences(final LinkedHashMap<?, ?> instance, final PersistenceFunction iterator)
	{
		Persistence.iterateReferencesMap(iterator, instance);
	}

	@Override
	public final void iteratePersistedReferences(final Binary bytes, final PersistenceObjectIdAcceptor iterator)
	{
		bytes.iterateKeyValueEntriesReferences(BINARY_OFFSET_ELEMENTS, iterator);
	}
	
}

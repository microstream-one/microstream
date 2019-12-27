package one.microstream.java.util.concurrent;

import java.util.Comparator;
import java.util.concurrent.ConcurrentSkipListMap;

import one.microstream.X;
import one.microstream.collections.old.KeyValueFlatCollector;
import one.microstream.collections.old.OldCollections;
import one.microstream.persistence.binary.internal.AbstractBinaryHandlerCustomCollection;
import one.microstream.persistence.binary.types.Binary;
import one.microstream.persistence.types.Persistence;
import one.microstream.persistence.types.PersistenceFunction;
import one.microstream.persistence.types.PersistenceLoadHandler;
import one.microstream.persistence.types.PersistenceReferenceLoader;
import one.microstream.persistence.types.PersistenceStoreHandler;


public final class BinaryHandlerConcurrentSkipListMap
extends AbstractBinaryHandlerCustomCollection<ConcurrentSkipListMap<?, ?>>
{
	///////////////////////////////////////////////////////////////////////////
	// constants //
	//////////////

	static final long BINARY_OFFSET_COMPARATOR =                                                      0;
	static final long BINARY_OFFSET_ELEMENTS   = BINARY_OFFSET_COMPARATOR + Binary.objectIdByteLength();
	
	

	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////

	@SuppressWarnings({"unchecked", "rawtypes"})
	private static Class<ConcurrentSkipListMap<?, ?>> handledType()
	{
		return (Class)ConcurrentSkipListMap.class; // no idea how to get ".class" to work otherwise
	}

	static final int getElementCount(final Binary bytes)
	{
		return X.checkArrayRange(bytes.getListElementCountKeyValue(BINARY_OFFSET_ELEMENTS));
	}
		
	@SuppressWarnings("unchecked")
	private static <E> Comparator<? super E> getComparator(
		final Binary                 bytes  ,
		final PersistenceLoadHandler handler
	)
	{
		return (Comparator<? super E>)handler.lookupObject(bytes.read_long(BINARY_OFFSET_COMPARATOR));
	}
	
	public static BinaryHandlerConcurrentSkipListMap New()
	{
		return new BinaryHandlerConcurrentSkipListMap();
	}



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	BinaryHandlerConcurrentSkipListMap()
	{
		super(
			handledType(),
			keyValuesFields(
				CustomField(Comparator.class, "comparator")
			)
		);
	}



	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////

	@Override
	public final void store(
		final Binary                      bytes   ,
		final ConcurrentSkipListMap<?, ?> instance,
		final long                        objectId,
		final PersistenceStoreHandler     handler
	)
	{
		// store elements simply as array binary form
		bytes.storeMapEntrySet(
			this.typeId()         ,
			objectId              ,
			BINARY_OFFSET_ELEMENTS,
			instance.entrySet()   ,
			handler
		);
		
		bytes.store_long(
			BINARY_OFFSET_COMPARATOR,
			handler.apply(instance.comparator())
		);
	}
	
	@Override
	public final ConcurrentSkipListMap<?, ?> create(
		final Binary                 bytes  ,
		final PersistenceLoadHandler handler
	)
	{
		return new ConcurrentSkipListMap<>(
			getComparator(bytes, handler)
		);
	}

	@Override
	public final void update(
		final Binary                      bytes   ,
		final ConcurrentSkipListMap<?, ?> instance,
		final PersistenceLoadHandler      handler
	)
	{
		instance.clear();
		
		/*
		 * Tree collections don't use hashing, but their comparing logic still uses the elements' state,
		 * which might not yet be available when this method is called. Hence the detour to #complete.
		 */
		final int elementCount = getElementCount(bytes);
		final KeyValueFlatCollector<Object, Object> collector = KeyValueFlatCollector.New(elementCount);
		bytes.collectKeyValueReferences(BINARY_OFFSET_ELEMENTS, elementCount, handler, collector);
		bytes.registerHelper(instance, collector.yield());
	}

	@Override
	public final void complete(
		final Binary                      bytes   ,
		final ConcurrentSkipListMap<?, ?> instance,
		final PersistenceLoadHandler      builder
	)
	{
		OldCollections.populateMapFromHelperArray(instance, bytes.getHelper(instance));
	}

	@Override
	public final void iterateInstanceReferences(
		final ConcurrentSkipListMap<?, ?> instance,
		final PersistenceFunction         iterator
	)
	{
		iterator.apply(instance.comparator());
		Persistence.iterateReferencesMap(iterator, instance);
	}

	@Override
	public final void iterateLoadableReferences(final Binary bytes, final PersistenceReferenceLoader iterator)
	{
		iterator.acceptObjectId(bytes.read_long(BINARY_OFFSET_COMPARATOR));
		bytes.iterateKeyValueEntriesReferences(BINARY_OFFSET_ELEMENTS, iterator);
	}
	
}

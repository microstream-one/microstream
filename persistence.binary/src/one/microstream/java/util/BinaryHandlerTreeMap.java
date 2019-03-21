package one.microstream.java.util;

import java.util.Comparator;
import java.util.TreeMap;

import one.microstream.X;
import one.microstream.collections.old.KeyValueFlatCollector;
import one.microstream.collections.old.OldCollections;
import one.microstream.persistence.binary.internal.AbstractBinaryHandlerCustomCollection;
import one.microstream.persistence.binary.types.Binary;
import one.microstream.persistence.binary.types.BinaryCollectionHandling;
import one.microstream.persistence.types.Persistence;
import one.microstream.persistence.types.PersistenceFunction;
import one.microstream.persistence.types.PersistenceLoadHandler;
import one.microstream.persistence.types.PersistenceObjectIdAcceptor;
import one.microstream.persistence.types.PersistenceStoreHandler;


public final class BinaryHandlerTreeMap extends AbstractBinaryHandlerCustomCollection<TreeMap<?, ?>>
{
	///////////////////////////////////////////////////////////////////////////
	// constants //
	//////////////

	static final long BINARY_OFFSET_COMPARATOR =                           0;
	static final long BINARY_OFFSET_ELEMENTS   = Binary.objectIdByteLength();
	
	

	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////

	@SuppressWarnings({"unchecked", "rawtypes"})
	private static Class<TreeMap<?, ?>> typeWorkaround()
	{
		return (Class)TreeMap.class; // no idea how to get ".class" to work otherwise
	}

	static final int getElementCount(final Binary bytes)
	{
		return X.checkArrayRange(bytes.getListElementCountReferences(BINARY_OFFSET_ELEMENTS));
	}
		
	@SuppressWarnings("unchecked")
	private static <E> Comparator<? super E> getComparator(
		final Binary                 bytes  ,
		final PersistenceLoadHandler handler
	)
	{
		return (Comparator<? super E>)handler.lookupObject(bytes.get_long(BINARY_OFFSET_COMPARATOR));
	}



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public BinaryHandlerTreeMap()
	{
		super(
			typeWorkaround(),
			BinaryCollectionHandling.keyValuesPseudoFields(
				pseudoField(Comparator.class, "comparator")
			)
		);
	}



	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////

	@Override
	public final void store(
		final Binary                  bytes   ,
		final TreeMap<?, ?>           instance,
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
		
		bytes.store_long(
			contentAddress + BINARY_OFFSET_COMPARATOR,
			handler.apply(instance.comparator())
		);
	}
	
	@Override
	public final TreeMap<?, ?> create(final Binary bytes, final PersistenceLoadHandler handler)
	{
		return new TreeMap<>(
			getComparator(bytes, handler)
		);
	}

	@Override
	public final void update(final Binary bytes, final TreeMap<?, ?> instance, final PersistenceLoadHandler handler)
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
	public final void iterateInstanceReferences(final TreeMap<?, ?> instance, final PersistenceFunction iterator)
	{
		iterator.apply(instance.comparator());
		Persistence.iterateReferencesMap(iterator, instance);
	}

	@Override
	public final void complete(final Binary bytes, final TreeMap<?, ?> instance, final PersistenceLoadHandler builder)
	{
		OldCollections.populateMapFromHelperArray(instance, bytes.getHelper(instance));
	}

	@Override
	public final void iteratePersistedReferences(final Binary bytes, final PersistenceObjectIdAcceptor iterator)
	{
		iterator.acceptObjectId(bytes.get_long(BINARY_OFFSET_COMPARATOR));
		bytes.iterateListElementReferences(BINARY_OFFSET_ELEMENTS, iterator);
	}
	
}

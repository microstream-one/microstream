package one.microstream.persistence.binary.jdk8.java.util;

import java.util.Hashtable;

import one.microstream.X;
import one.microstream.collections.old.KeyValueFlatCollector;
import one.microstream.collections.old.OldCollections;
import one.microstream.persistence.binary.internal.AbstractBinaryHandlerCustomCollection;
import one.microstream.persistence.binary.jdk8.types.SunJdk8Internals;
import one.microstream.persistence.binary.types.Binary;
import one.microstream.persistence.types.Persistence;
import one.microstream.persistence.types.PersistenceFunction;
import one.microstream.persistence.types.PersistenceLoadHandler;
import one.microstream.persistence.types.PersistenceReferenceLoader;
import one.microstream.persistence.types.PersistenceStoreHandler;


public final class BinaryHandlerHashtable extends AbstractBinaryHandlerCustomCollection<Hashtable<?, ?>>
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
	private static Class<Hashtable<?, ?>> handledType()
	{
		return (Class)Hashtable.class; // no idea how to get ".class" to work otherwise
	}

	static final float getLoadFactor(final Binary bytes)
	{
		return bytes.read_float(BINARY_OFFSET_LOAD_FACTOR);
	}

	static final int getElementCount(final Binary bytes)
	{
		return X.checkArrayRange(bytes.getListElementCountKeyValue(BINARY_OFFSET_ELEMENTS));
	}
	
	public static BinaryHandlerHashtable New()
	{
		return new BinaryHandlerHashtable();
	}



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	BinaryHandlerHashtable()
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
		final Binary                          bytes   ,
		final Hashtable<?, ?>                 instance,
		final long                            objectId,
		final PersistenceStoreHandler<Binary> handler
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

		// store load factor as (sole) header value
		bytes.store_float(
			BINARY_OFFSET_LOAD_FACTOR,
			SunJdk8Internals.getLoadFactor(instance)
		);
	}
	

	@Override
	public final Hashtable<?, ?> create(final Binary bytes, final PersistenceLoadHandler idResolver)
	{
		return new Hashtable<>(
			getElementCount(bytes),
			getLoadFactor(bytes)
		);
	}

	@Override
	public final void updateState(final Binary bytes, final Hashtable<?, ?> instance, final PersistenceLoadHandler idResolver)
	{
		instance.clear();
		final int elementCount = getElementCount(bytes);
		final KeyValueFlatCollector<Object, Object> collector = KeyValueFlatCollector.New(elementCount);
		bytes.collectKeyValueReferences(BINARY_OFFSET_ELEMENTS, elementCount, idResolver, collector);
		bytes.registerHelper(instance, collector.yield());
	}

	@Override
	public void complete(final Binary bytes, final Hashtable<?, ?> instance, final PersistenceLoadHandler idResolver)
	{
		OldCollections.populateMapFromHelperArray(instance, bytes.getHelper(instance));
	}

	@Override
	public final void iterateInstanceReferences(final Hashtable<?, ?> instance, final PersistenceFunction iterator)
	{
		Persistence.iterateReferencesMap(iterator, instance);
	}

	@Override
	public final void iterateLoadableReferences(final Binary bytes, final PersistenceReferenceLoader iterator)
	{
		bytes.iterateKeyValueEntriesReferences(BINARY_OFFSET_ELEMENTS, iterator);
	}
	
}

package one.microstream.java.util;

import java.util.HashMap;

import one.microstream.X;
import one.microstream.collections.old.JavaUtilMapEntrySetFlattener;
import one.microstream.collections.old.OldCollections;
import one.microstream.memory.XMemory;
import one.microstream.persistence.binary.internal.AbstractBinaryHandlerCustomCollection;
import one.microstream.persistence.binary.types.Binary;
import one.microstream.persistence.binary.types.BinaryCollectionHandling;
import one.microstream.persistence.types.Persistence;
import one.microstream.persistence.types.PersistenceFunction;
import one.microstream.persistence.types.PersistenceLoadHandler;
import one.microstream.persistence.types.PersistenceObjectIdAcceptor;
import one.microstream.persistence.types.PersistenceStoreHandler;


public final class BinaryHandlerHashMapFlattened extends AbstractBinaryHandlerCustomCollection<HashMap<?, ?>>
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

	public BinaryHandlerHashMapFlattened()
	{
		super(
			typeWorkaround(),
			BinaryCollectionHandling.simpleArrayPseudoFields(
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

		// store load factor as (sole) header value
		bytes.store_float(
			contentAddress + BINARY_OFFSET_LOAD_FACTOR,
			XMemory.getLoadFactor(instance)
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
		final Object[] elementsHelper = new Object[getElementCount(bytes)];
		bytes.collectElementsIntoArray(BINARY_OFFSET_ELEMENTS, builder, elementsHelper);
		bytes.registerHelper(instance, elementsHelper);
	}

	@Override
	public void complete(final Binary bytes, final HashMap<?, ?> instance, final PersistenceLoadHandler loadHandler)
	{
		OldCollections.populateMapFromHelperArray(instance, bytes.getHelper(instance));
	}

	@Override
	public final void iterateInstanceReferences(final HashMap<?, ?> instance, final PersistenceFunction iterator)
	{
		Persistence.iterateReferencesMap(iterator, instance);
	}

	@Override
	public final void iteratePersistedReferences(final Binary bytes, final PersistenceObjectIdAcceptor iterator)
	{
		bytes.iterateListElementReferences(BINARY_OFFSET_ELEMENTS, iterator);
	}
	
}

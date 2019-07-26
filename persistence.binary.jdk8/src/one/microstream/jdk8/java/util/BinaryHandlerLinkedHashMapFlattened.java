package one.microstream.jdk8.java.util;

import java.util.LinkedHashMap;

import one.microstream.X;
import one.microstream.collections.old.JavaUtilMapEntrySetFlattener;
import one.microstream.collections.old.OldCollections;
import one.microstream.memory.XMemoryJDK8;
import one.microstream.persistence.binary.internal.AbstractBinaryHandlerCustomCollection;
import one.microstream.persistence.binary.types.Binary;
import one.microstream.persistence.types.Persistence;
import one.microstream.persistence.types.PersistenceFunction;
import one.microstream.persistence.types.PersistenceLoadHandler;
import one.microstream.persistence.types.PersistenceObjectIdAcceptor;
import one.microstream.persistence.types.PersistenceStoreHandler;


/**
 * Premature prototype implementation that has to be kept for live projects using it.
 * <p>
 * Do not use! Use {@link BinaryHandlerLinkedHashMap} instead.
 * 
 * @author FH
 * @author TM
 */
public final class BinaryHandlerLinkedHashMapFlattened extends AbstractBinaryHandlerCustomCollection<LinkedHashMap<?, ?>>
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
	private static Class<LinkedHashMap<?, ?>> handledType()
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
	
	public static BinaryHandlerLinkedHashMapFlattened New()
	{
		return new BinaryHandlerLinkedHashMapFlattened();
	}



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	BinaryHandlerLinkedHashMapFlattened()
	{
		super(
			handledType(),
			SimpleArrayFields(
				CustomField(float.class,   "loadFactor"),
				CustomField(boolean.class, "accessOrder")
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
		final long contentAddress = bytes.storeIterableAsList(
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
			XMemoryJDK8.getLoadFactor(instance)
		);
		bytes.store_boolean(
			contentAddress + BINARY_OFFSET_ACCESS_ORDER,
			XMemoryJDK8.getAccessOrder(instance)
		);
	}

	@Override
	public final LinkedHashMap<?, ?> create(final Binary bytes, final PersistenceLoadHandler handler)
	{
		return new LinkedHashMap<>(
			getElementCount(bytes) / 2,
			getLoadFactor(bytes),
			getAccessOrder(bytes)
		);
	}

	@Override
	public final void update(final Binary bytes, final LinkedHashMap<?, ?> instance, final PersistenceLoadHandler handler)
	{
		instance.clear();
		final Object[] elementsHelper = new Object[getElementCount(bytes)];
		bytes.collectElementsIntoArray(BINARY_OFFSET_ELEMENTS, handler, elementsHelper);
		bytes.registerHelper(instance, elementsHelper);
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
	public final void iterateLoadableReferences(final Binary bytes, final PersistenceObjectIdAcceptor iterator)
	{
		bytes.iterateListElementReferences(BINARY_OFFSET_ELEMENTS, iterator);
	}
	
}

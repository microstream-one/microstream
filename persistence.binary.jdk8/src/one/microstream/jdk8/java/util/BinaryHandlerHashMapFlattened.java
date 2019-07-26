package one.microstream.jdk8.java.util;

import java.util.HashMap;

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
 * Do not use! Use {@link BinaryHandlerHashMap} instead.
 * 
 * @author FH
 * @author TM
 */
public final class BinaryHandlerHashMapFlattened extends AbstractBinaryHandlerCustomCollection<HashMap<?, ?>>
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
		return X.checkArrayRange(bytes.getListElementCountReferences(BINARY_OFFSET_ELEMENTS));
	}
	
	public static BinaryHandlerHashMapFlattened New()
	{
		return new BinaryHandlerHashMapFlattened();
	}



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	BinaryHandlerHashMapFlattened()
	{
		super(
			handledType(),
			SimpleArrayFields(
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
		final long contentAddress = bytes.storeIterableAsList(
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
			XMemoryJDK8.getLoadFactor(instance)
		);
	}

	@Override
	public final HashMap<?, ?> create(final Binary bytes, final PersistenceLoadHandler handler)
	{
		return new HashMap<>(
			getElementCount(bytes) / 2,
			getLoadFactor(bytes)
		);
	}

	@Override
	public final void update(final Binary bytes, final HashMap<?, ?> instance, final PersistenceLoadHandler handler)
	{
		instance.clear();
		final Object[] elementsHelper = new Object[getElementCount(bytes)];
		bytes.collectElementsIntoArray(BINARY_OFFSET_ELEMENTS, handler, elementsHelper);
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
	public final void iterateLoadableReferences(final Binary bytes, final PersistenceObjectIdAcceptor iterator)
	{
		bytes.iterateListElementReferences(BINARY_OFFSET_ELEMENTS, iterator);
	}
	
}

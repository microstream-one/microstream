package one.microstream.jdk8.java.util;

import java.util.Hashtable;

import one.microstream.X;
import one.microstream.collections.old.JavaUtilMapEntrySetFlattener;
import one.microstream.collections.old.OldCollections;
import one.microstream.memory.sun.SunJdk8Internals;
import one.microstream.persistence.binary.internal.AbstractBinaryHandlerCustomCollection;
import one.microstream.persistence.binary.types.Binary;
import one.microstream.persistence.types.Persistence;
import one.microstream.persistence.types.PersistenceFunction;
import one.microstream.persistence.types.PersistenceReferenceLoader;
import one.microstream.persistence.types.PersistenceObjectIdResolver;
import one.microstream.persistence.types.PersistenceStoreHandler;


/**
 * Premature prototype implementation that has to be kept for live projects using it.
 * <p>
 * Do not use! Use {@link BinaryHandlerHashtable} instead.
 * 
 * @author FH
 * @author TM
 */
@Deprecated
public final class BinaryHandlerHashtableFlattened extends AbstractBinaryHandlerCustomCollection<Hashtable<?, ?>>
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
		return X.checkArrayRange(bytes.getListElementCountReferences(BINARY_OFFSET_ELEMENTS));
	}
	
	public static BinaryHandlerHashtableFlattened New()
	{
		return new BinaryHandlerHashtableFlattened();
	}



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	BinaryHandlerHashtableFlattened()
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
		final Hashtable<?, ?>         instance,
		final long                    objectId,
		final PersistenceStoreHandler handler
	)
	{
		// store elements simply as array binary form
		bytes.storeIterableAsList(
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
			BINARY_OFFSET_LOAD_FACTOR,
			SunJdk8Internals.getLoadFactor(instance)
		);
	}
	

	@Override
	public final Hashtable<?, ?> create(final Binary bytes, final PersistenceObjectIdResolver idResolver)
	{
		return new Hashtable<>(
			getElementCount(bytes) / 2,
			getLoadFactor(bytes)
		);
	}

	@Override
	public final void update(final Binary bytes, final Hashtable<?, ?> instance, final PersistenceObjectIdResolver idResolver)
	{
		instance.clear();
		final Object[] elementsHelper = new Object[getElementCount(bytes)];
		bytes.collectElementsIntoArray(BINARY_OFFSET_ELEMENTS, idResolver, elementsHelper);
		bytes.registerHelper(instance, elementsHelper);
	}

	@Override
	public void complete(final Binary bytes, final Hashtable<?, ?> instance, final PersistenceObjectIdResolver idResolver)
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
		bytes.iterateListElementReferences(BINARY_OFFSET_ELEMENTS, iterator);
	}
	
}

package one.microstream.java.util;

import java.util.Comparator;
import java.util.PriorityQueue;

import one.microstream.X;
import one.microstream.collections.old.OldCollections;
import one.microstream.persistence.binary.internal.AbstractBinaryHandlerCustomIterable;
import one.microstream.persistence.binary.types.Binary;
import one.microstream.persistence.types.PersistenceFunction;
import one.microstream.persistence.types.PersistenceObjectIdAcceptor;
import one.microstream.persistence.types.PersistenceObjectIdResolver;
import one.microstream.persistence.types.PersistenceStoreHandler;


public final class BinaryHandlerPriorityQueue
extends AbstractBinaryHandlerCustomIterable<PriorityQueue<?>>
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
	private static Class<PriorityQueue<?>> handledType()
	{
		return (Class)PriorityQueue.class; // no idea how to get ".class" to work otherwise
	}
	
	@SuppressWarnings("unchecked")
	private static <E> Comparator<? super E> getComparator(
		final Binary                      bytes     ,
		final PersistenceObjectIdResolver idResolver
	)
	{
		return (Comparator<? super E>)idResolver.lookupObject(bytes.read_long(BINARY_OFFSET_COMPARATOR));
	}

	static final int getElementCount(final Binary bytes)
	{
		return X.checkArrayRange(bytes.getListElementCountReferences(BINARY_OFFSET_ELEMENTS));
	}
	
	public static BinaryHandlerPriorityQueue New()
	{
		return new BinaryHandlerPriorityQueue();
	}


	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	BinaryHandlerPriorityQueue()
	{
		super(
			handledType(),
			SimpleArrayFields(
				CustomField(Comparator.class, "comparator")
			)
		);
		
	}



	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////

	@Override
	public final void store(
		final Binary                  bytes   ,
		final PriorityQueue<?>        instance,
		final long                    objectId,
		final PersistenceStoreHandler handler
	)
	{
		// store elements simply as array binary form
		bytes.storeIterableAsList(
			this.typeId()         ,
			objectId              ,
			BINARY_OFFSET_ELEMENTS,
			instance              ,
			instance.size()       ,
			handler
		);
		
		bytes.store_long(
			BINARY_OFFSET_COMPARATOR,
			handler.apply(instance.comparator())
		);
	}
	
	@Override
	public final PriorityQueue<?> create(
		final Binary                      bytes     ,
		final PersistenceObjectIdResolver idResolver
	)
	{
		return new PriorityQueue<>(
			X.checkArrayRange(getElementCount(bytes)),
			getComparator(bytes, idResolver)
		);
	}

	@Override
	public final void update(
		final Binary                      bytes     ,
		final PriorityQueue<?>            instance  ,
		final PersistenceObjectIdResolver idResolver
	)
	{
		instance.clear();
		
		/*
		 * Tree collections don't use hashing, but their comparing logic still uses the elements' state,
		 * which might not yet be available when this method is called. Hence the detour to #complete.
		 */
		final Object[] elementsHelper = new Object[getElementCount(bytes)];
		bytes.collectElementsIntoArray(BINARY_OFFSET_ELEMENTS, idResolver, elementsHelper);
		bytes.registerHelper(instance, elementsHelper);
	}
	
	@Override
	public final void complete(
		final Binary                 bytes   ,
		final PriorityQueue<?>       instance,
		final PersistenceObjectIdResolver idResolver
	)
	{
		OldCollections.populateCollectionFromHelperArray(instance, bytes.getHelper(instance));
	}

	@Override
	public final void iterateInstanceReferences(
		final PriorityQueue<?>    instance,
		final PersistenceFunction iterator
	)
	{
		iterator.apply(instance.comparator());
		super.iterateInstanceReferences(instance, iterator);
	}

	@Override
	public final void iterateLoadableReferences(final Binary bytes, final PersistenceObjectIdAcceptor iterator)
	{
		iterator.acceptObjectId(bytes.read_long(BINARY_OFFSET_COMPARATOR));
		bytes.iterateListElementReferences(BINARY_OFFSET_ELEMENTS, iterator);
	}
	
}

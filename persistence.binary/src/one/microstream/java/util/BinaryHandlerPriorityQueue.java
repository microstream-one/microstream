package one.microstream.java.util;

import java.util.Comparator;
import java.util.PriorityQueue;

import one.microstream.X;
import one.microstream.persistence.binary.internal.AbstractBinaryHandlerCustomIterable;
import one.microstream.persistence.binary.types.Binary;
import one.microstream.persistence.types.PersistenceFunction;
import one.microstream.persistence.types.PersistenceLoadHandler;
import one.microstream.persistence.types.PersistenceObjectIdAcceptor;
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
		final Binary                 bytes  ,
		final PersistenceLoadHandler handler
	)
	{
		return (Comparator<? super E>)handler.lookupObject(bytes.get_long(BINARY_OFFSET_COMPARATOR));
	}

	static final int getElementCount(final Binary bytes)
	{
		return X.checkArrayRange(bytes.getListElementCountReferences(BINARY_OFFSET_ELEMENTS));
	}



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public BinaryHandlerPriorityQueue()
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
		final long contentAddress = bytes.storeIterableAsList(
			this.typeId()         ,
			objectId              ,
			BINARY_OFFSET_ELEMENTS,
			instance              ,
			instance.size()       ,
			handler
		);
		
		bytes.store_long(
			contentAddress + BINARY_OFFSET_COMPARATOR,
			handler.apply(instance.comparator())
		);
	}
	
	@Override
	public final PriorityQueue<?> create(
		final Binary                 bytes  ,
		final PersistenceLoadHandler handler
	)
	{
		return new PriorityQueue<>(
			X.checkArrayRange(getElementCount(bytes)),
			getComparator(bytes, handler)
		);
	}

	@Override
	public final void update(
		final Binary                 bytes   ,
		final PriorityQueue<?>       instance,
		final PersistenceLoadHandler handler
	)
	{
		instance.clear();
		
		/*
		 * Tree collections don't use hashing, but their comparing logic still uses the elements' state,
		 * which might not yet be available when this method is called. Hence the detour to #complete.
		 */
		final Object[] elementsHelper = new Object[getElementCount(bytes)];
		bytes.collectElementsIntoArray(BINARY_OFFSET_ELEMENTS, handler, elementsHelper);
		bytes.registerHelper(instance, elementsHelper);
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
		iterator.acceptObjectId(bytes.get_long(BINARY_OFFSET_COMPARATOR));
		bytes.iterateListElementReferences(BINARY_OFFSET_ELEMENTS, iterator);
	}
	
}

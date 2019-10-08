package one.microstream.jdk8.java.util;

import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.Queue;

import one.microstream.X;
import one.microstream.memory.XMemoryJDK8;
import one.microstream.persistence.binary.internal.AbstractBinaryHandlerCustomCollection;
import one.microstream.persistence.binary.types.Binary;
import one.microstream.persistence.types.Persistence;
import one.microstream.persistence.types.PersistenceFunction;
import one.microstream.persistence.types.PersistenceObjectIdAcceptor;
import one.microstream.persistence.types.PersistenceObjectIdResolver;
import one.microstream.persistence.types.PersistenceStoreHandler;


public final class BinaryHandlerPriorityQueue extends AbstractBinaryHandlerCustomCollection<PriorityQueue<?>>
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

	static final int getElementCount(final Binary bytes)
	{
		return X.checkArrayRange(bytes.getListElementCountReferences(BINARY_OFFSET_ELEMENTS));
	}
	
	@SuppressWarnings("unchecked")
	private static <E> Comparator<? super E> getComparator(
		final Binary                      bytes     ,
		final PersistenceObjectIdResolver idResolver
	)
	{
		return (Comparator<? super E>)idResolver.lookupObject(bytes.get_long(BINARY_OFFSET_COMPARATOR));
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
	public final PriorityQueue<?> create(final Binary bytes, final PersistenceObjectIdResolver idResolver)
	{
		return new PriorityQueue<>(
			bytes.getSizedArrayLength(BINARY_OFFSET_ELEMENTS),
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
		// instance must be cleared in case an existing one is updated
		instance.clear();
		
		@SuppressWarnings("unchecked")
		final Queue<Object> castedInstance = (Queue<Object>)instance;
		
		bytes.collectObjectReferences(
			BINARY_OFFSET_ELEMENTS,
			X.checkArrayRange(getElementCount(bytes)),
			idResolver,
			e ->
				castedInstance.add(e)
		);
	}

	@Override
	public final void iterateInstanceReferences(final PriorityQueue<?> instance, final PersistenceFunction iterator)
	{
		iterator.apply(instance.comparator());
		Persistence.iterateReferences(iterator, XMemoryJDK8.accessArray(instance), 0, instance.size());
	}

	@Override
	public final void iterateLoadableReferences(final Binary bytes, final PersistenceObjectIdAcceptor iterator)
	{
		iterator.acceptObjectId(bytes.get_long(BINARY_OFFSET_COMPARATOR));
		bytes.iterateListElementReferences(BINARY_OFFSET_ELEMENTS, iterator);
	}
	
}

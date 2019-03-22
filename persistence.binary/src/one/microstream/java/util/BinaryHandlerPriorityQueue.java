package one.microstream.java.util;

import java.util.Comparator;
import java.util.PriorityQueue;

import one.microstream.memory.XMemory;
import one.microstream.persistence.binary.internal.AbstractBinaryHandlerCustomCollection;
import one.microstream.persistence.binary.types.Binary;
import one.microstream.persistence.binary.types.BinaryCollectionHandling;
import one.microstream.persistence.types.Persistence;
import one.microstream.persistence.types.PersistenceFunction;
import one.microstream.persistence.types.PersistenceLoadHandler;
import one.microstream.persistence.types.PersistenceObjectIdAcceptor;
import one.microstream.persistence.types.PersistenceStoreHandler;


public final class BinaryHandlerPriorityQueue extends AbstractBinaryHandlerCustomCollection<PriorityQueue<?>>
{
	///////////////////////////////////////////////////////////////////////////
	// constants //
	//////////////

	static final long BINARY_OFFSET_COMPARATOR  =                                                      0;
	static final long BINARY_OFFSET_SIZED_ARRAY = BINARY_OFFSET_COMPARATOR + Binary.objectIdByteLength();
	
	

	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////

	@SuppressWarnings({"unchecked", "rawtypes"})
	private static Class<PriorityQueue<?>> typeWorkaround()
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
	


	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public BinaryHandlerPriorityQueue()
	{
		super(
			typeWorkaround(),
			BinaryCollectionHandling.sizedArrayPseudoFields(
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
		final PriorityQueue<?>        instance,
		final long                    objectId,
		final PersistenceStoreHandler handler
	)
	{
		final long contentAddress = bytes.storeSizedArray(
			this.typeId()                ,
			objectId                     ,
			BINARY_OFFSET_COMPARATOR     ,
			XMemory.accessArray(instance),
			instance.size()              ,
			handler
		);
		bytes.store_long(
			contentAddress + BINARY_OFFSET_COMPARATOR,
			handler.apply(instance.comparator())
		);
	}

	@Override
	public final PriorityQueue<?> create(final Binary bytes, final PersistenceLoadHandler handler)
	{
		return new PriorityQueue<>(
			bytes.getSizedArrayLength(BINARY_OFFSET_SIZED_ARRAY),
			getComparator(bytes, handler)
		);
	}

	@Override
	public final void update(final Binary bytes, final PriorityQueue<?> instance, final PersistenceLoadHandler handler)
	{
		// clear to avoid remnant references to logically unreachable instances of an updated existing instance.
		instance.clear();
		
		final Object[] array = XMemory.accessArray(instance);
		final long requiredArrayLength = bytes.getSizedArrayLength(BINARY_OFFSET_SIZED_ARRAY);

		// since PriorityQueue has no #ensureCapacity,
		if(array.length >= requiredArrayLength)
		{
			final int size = bytes.updateSizedArrayObjectReferences(BINARY_OFFSET_SIZED_ARRAY, array, handler);
			XMemory.setSize(instance, size);
		}
		else
		{
			@SuppressWarnings("unchecked")
			final PriorityQueue<Object> castedInstance = (PriorityQueue<Object>)instance;
			
			// (22.03.2019 TM)FIXME: MS-76: check if adding in order fits the implementation's add logic
			bytes.iterateSizedArrayElementReferences(BINARY_OFFSET_SIZED_ARRAY, objectId ->
			{
				final Object element = handler.lookupObject(objectId);
				castedInstance.add(element);
			});
		}
	}

	@Override
	public final void iterateInstanceReferences(final PriorityQueue<?> instance, final PersistenceFunction iterator)
	{
		iterator.apply(instance.comparator());
		Persistence.iterateReferences(iterator, XMemory.accessArray(instance), 0, instance.size());
	}

	@Override
	public final void iteratePersistedReferences(final Binary bytes, final PersistenceObjectIdAcceptor iterator)
	{
		iterator.acceptObjectId(bytes.get_long(BINARY_OFFSET_COMPARATOR));
		bytes.iterateSizedArrayElementReferences(BINARY_OFFSET_SIZED_ARRAY, iterator);
	}
	
}

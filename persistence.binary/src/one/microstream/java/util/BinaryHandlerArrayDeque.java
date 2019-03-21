package one.microstream.java.util;

import java.util.ArrayDeque;

import one.microstream.memory.XMemory;
import one.microstream.persistence.binary.internal.AbstractBinaryHandlerCustomCollectionSizedArray;
import one.microstream.persistence.binary.types.Binary;
import one.microstream.persistence.binary.types.BinaryCollectionHandling;
import one.microstream.persistence.types.Persistence;
import one.microstream.persistence.types.PersistenceFunction;
import one.microstream.persistence.types.PersistenceLoadHandler;
import one.microstream.persistence.types.PersistenceObjectIdAcceptor;
import one.microstream.persistence.types.PersistenceSizedArrayLengthController;
import one.microstream.persistence.types.PersistenceStoreHandler;


public final class BinaryHandlerArrayDeque extends AbstractBinaryHandlerCustomCollectionSizedArray<ArrayDeque<?>>
{
	///////////////////////////////////////////////////////////////////////////
	// constants        //
	/////////////////////

	static final long BINARY_OFFSET_HEAD        =                                  0;
	static final long BINARY_OFFSET_TAIL        = BINARY_OFFSET_HEAD + Integer.BYTES;
	static final long BINARY_OFFSET_SIZED_ARRAY = BINARY_OFFSET_TAIL + Integer.BYTES;

	

	///////////////////////////////////////////////////////////////////////////
	// static methods    //
	/////////////////////

	@SuppressWarnings({"unchecked", "rawtypes"})
	private static Class<ArrayDeque<?>> typeWorkaround()
	{
		return (Class)ArrayDeque.class; // no idea how to get ".class" to work otherwise
	}

	static final int getHead(final Binary bytes)
	{
		return bytes.get_int(BINARY_OFFSET_HEAD);
	}

	static final int getTail(final Binary bytes)
	{
		return bytes.get_int(BINARY_OFFSET_TAIL);
	}

	static final int getHead(final ArrayDeque<?> instance)
	{
		// "head" (actually headIndex, JDK Pros) is nothing more than a starting index and tail is offset + size.
		return XMemory.getHead(instance);
	}

	static final int getTail(final ArrayDeque<?> instance)
	{
		// "tail" (actually tailIndex, JDK Pros) is nothing more than a bounding index equalling headIndex + size.
		return XMemory.getTail(instance);
	}



	///////////////////////////////////////////////////////////////////////////
	// constructors     //
	/////////////////////

	public BinaryHandlerArrayDeque(final PersistenceSizedArrayLengthController controller)
	{
		super(
			typeWorkaround(),
			BinaryCollectionHandling.simpleArrayPseudoFields(
			    pseudoField(int.class, "head"),
			    pseudoField(int.class, "tail")
			),
			controller
		);
	}


	
	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////
	
	@Override
	public final void store(
		final Binary                  bytes   ,
		final ArrayDeque<?>           instance,
		final long                    objectId,
		final PersistenceStoreHandler handler
	)
	{
		final int head = getHead(instance);
		final int tail = getTail(instance);
		
		final long contentAddress = bytes.storeSizedArray(
			this.typeId()                  ,
			objectId                       ,
			BINARY_OFFSET_SIZED_ARRAY      ,
			XMemory.accessArray(instance),
			head                           ,
			instance.size()                ,
			handler
		);
		bytes.store_int(contentAddress + BINARY_OFFSET_HEAD, head);
		bytes.store_int(contentAddress + BINARY_OFFSET_TAIL, tail);
	}

	@Override
	public final ArrayDeque<?> create(final Binary bytes, final PersistenceLoadHandler handler)
	{
		final int arrayLength = this.determineArrayLength(bytes, BINARY_OFFSET_SIZED_ARRAY);
		return new ArrayDeque<>(arrayLength);
	}

	@Override
	public final void update(final Binary bytes, final ArrayDeque<?> instance, final PersistenceLoadHandler handler)
	{
		// (18.03.2019 TM)FIXME: MS-76: must ensure ArrayDeque capacity or add elements externally.

		instance.clear();
		bytes.updateSizedArrayObjectReferences(
			BINARY_OFFSET_SIZED_ARRAY,
			XMemory.accessArray(instance),
			handler
		);
		XMemory.setHead(instance, getHead(bytes));
		XMemory.setTail(instance, getTail(bytes));
	}

	@Override
	public final void iterateInstanceReferences(final ArrayDeque<?> instance, final PersistenceFunction iterator)
	{
		Persistence.iterateReferences(iterator, XMemory.accessArray(instance), getHead(instance), instance.size());
	}

	@Override
	public final void iteratePersistedReferences(final Binary bytes, final PersistenceObjectIdAcceptor iterator)
	{
		bytes.iterateSizedArrayElementReferences(BINARY_OFFSET_SIZED_ARRAY, iterator);
	}
	
}

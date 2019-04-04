package one.microstream.java.util;

import java.util.Stack;

import one.microstream.memory.XMemory;
import one.microstream.persistence.binary.internal.AbstractBinaryHandlerCustomCollectionSizedArray;
import one.microstream.persistence.binary.types.Binary;
import one.microstream.persistence.types.Persistence;
import one.microstream.persistence.types.PersistenceFunction;
import one.microstream.persistence.types.PersistenceLoadHandler;
import one.microstream.persistence.types.PersistenceObjectIdAcceptor;
import one.microstream.persistence.types.PersistenceSizedArrayLengthController;
import one.microstream.persistence.types.PersistenceStoreHandler;


public final class BinaryHandlerStack extends AbstractBinaryHandlerCustomCollectionSizedArray<Stack<?>>
{
	///////////////////////////////////////////////////////////////////////////
	// constants //
	//////////////

	static final long BINARY_OFFSET_CAPACITY_INCREMENT =                                                0;
	static final long BINARY_OFFSET_SIZED_ARRAY        = BINARY_OFFSET_CAPACITY_INCREMENT + Integer.BYTES;

	

	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////

	@SuppressWarnings({"unchecked", "rawtypes"})
	private static Class<Stack<?>> typeWorkaround()
	{
		return (Class)Stack.class; // no idea how to get ".class" to work otherwise
	}



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public BinaryHandlerStack(final PersistenceSizedArrayLengthController controller)
	{
		super(
			typeWorkaround(),
			sizedArrayPseudoFields(
			    pseudoField(int.class, "capacityIncrement")
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
		final Stack<?>                instance,
		final long                    objectId,
		final PersistenceStoreHandler handler
	)
	{
		final long contentAddress = bytes.storeSizedArray(
			this.typeId()                ,
			objectId                     ,
			BINARY_OFFSET_SIZED_ARRAY    ,
			XMemory.accessArray(instance),
			instance.size()              ,
			handler
		);
		bytes.store_int(
		    contentAddress + BINARY_OFFSET_CAPACITY_INCREMENT,
		    XMemory.getCapacityIncrement(instance)
		);
	}

	@Override
	public final Stack<?> create(final Binary bytes, final PersistenceLoadHandler handler)
	{
		return new Stack<>();
	}

	@Override
	public final void update(final Binary bytes, final Stack<?> instance, final PersistenceLoadHandler handler)
	{
		// instance must be cleared and capacity-ensured in case an existing instance gets updated.
		instance.clear();
		instance.ensureCapacity(bytes.getSizedArrayLength(BINARY_OFFSET_SIZED_ARRAY));
		
		final int size = bytes.updateSizedArrayObjectReferences(
			BINARY_OFFSET_SIZED_ARRAY,
			XMemory.accessArray(instance),
			handler
		);
		XMemory.setElementCount(instance, size);
	}

	@Override
	public final void iterateInstanceReferences(final Stack<?> instance, final PersistenceFunction iterator)
	{
		Persistence.iterateReferences(iterator, XMemory.accessArray(instance), 0, instance.size());
	}

	@Override
	public final void iteratePersistedReferences(final Binary bytes, final PersistenceObjectIdAcceptor iterator)
	{
		bytes.iterateSizedArrayElementReferences(BINARY_OFFSET_SIZED_ARRAY, iterator);
	}
	
}

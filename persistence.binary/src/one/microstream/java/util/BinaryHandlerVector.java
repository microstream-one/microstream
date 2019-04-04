package one.microstream.java.util;

import java.util.Vector;

import one.microstream.memory.XMemory;
import one.microstream.persistence.binary.internal.AbstractBinaryHandlerCustomCollectionSizedArray;
import one.microstream.persistence.binary.types.Binary;
import one.microstream.persistence.types.Persistence;
import one.microstream.persistence.types.PersistenceFunction;
import one.microstream.persistence.types.PersistenceLoadHandler;
import one.microstream.persistence.types.PersistenceObjectIdAcceptor;
import one.microstream.persistence.types.PersistenceSizedArrayLengthController;
import one.microstream.persistence.types.PersistenceStoreHandler;


public final class BinaryHandlerVector extends AbstractBinaryHandlerCustomCollectionSizedArray<Vector<?>>
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
	private static Class<Vector<?>> typeWorkaround()
	{
		return (Class)Vector.class; // no idea how to get ".class" to work otherwise
	}



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public BinaryHandlerVector(final PersistenceSizedArrayLengthController controller)
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
		final Vector<?>               instance,
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
	public final Vector<?> create(final Binary bytes, final PersistenceLoadHandler handler)
	{
		// capacityIncrement can be any int value, even negative. So no validation can be done here.
		return new Vector<>(
			1,
			bytes.get_int(BINARY_OFFSET_CAPACITY_INCREMENT)
		);
	}

	@Override
	public final void update(final Binary bytes, final Vector<?> instance, final PersistenceLoadHandler handler)
	{
		// instance must be cleared and capacity-ensured in case an existing instance gets updated.
		instance.clear();
		instance.ensureCapacity(this.determineArrayLength(bytes, BINARY_OFFSET_SIZED_ARRAY));
		
		final int size = bytes.updateSizedArrayObjectReferences(
			BINARY_OFFSET_SIZED_ARRAY    ,
			XMemory.accessArray(instance),
			handler
		);
		XMemory.setElementCount(instance, size);
	}

	@Override
	public final void iterateInstanceReferences(final Vector<?> instance, final PersistenceFunction iterator)
	{
		Persistence.iterateReferences(iterator, XMemory.accessArray(instance), 0, instance.size());
	}

	@Override
	public final void iteratePersistedReferences(final Binary bytes, final PersistenceObjectIdAcceptor iterator)
	{
		bytes.iterateSizedArrayElementReferences(BINARY_OFFSET_SIZED_ARRAY, iterator);
	}
}

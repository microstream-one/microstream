package one.microstream.persistence.binary.jdk8.java.util;

import static one.microstream.X.notNull;

import java.util.Vector;

import one.microstream.persistence.binary.internal.AbstractBinaryHandlerCustomIterableSizedArray;
import one.microstream.persistence.binary.jdk8.types.SunJdk8Internals;
import one.microstream.persistence.binary.types.Binary;
import one.microstream.persistence.types.Persistence;
import one.microstream.persistence.types.PersistenceFunction;
import one.microstream.persistence.types.PersistenceLoadHandler;
import one.microstream.persistence.types.PersistenceReferenceLoader;
import one.microstream.persistence.types.PersistenceSizedArrayLengthController;
import one.microstream.persistence.types.PersistenceStoreHandler;


public final class BinaryHandlerVector extends AbstractBinaryHandlerCustomIterableSizedArray<Vector<?>>
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
	private static Class<Vector<?>> handledType()
	{
		return (Class)Vector.class; // no idea how to get ".class" to work otherwise
	}
	
	public static BinaryHandlerVector New(final PersistenceSizedArrayLengthController controller)
	{
		return new BinaryHandlerVector(
			notNull(controller)
		);
	}



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	BinaryHandlerVector(final PersistenceSizedArrayLengthController controller)
	{
		super(
			handledType(),
			SizedArrayFields(
			    CustomField(int.class, "capacityIncrement")
			),
			controller
		);
	}


	
	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////
	
	@Override
	public final void store(
		final Binary                          bytes   ,
		final Vector<?>                       instance,
		final long                            objectId,
		final PersistenceStoreHandler<Binary> handler
	)
	{
		bytes.storeSizedArray(
			this.typeId()                    ,
			objectId                         ,
			BINARY_OFFSET_SIZED_ARRAY        ,
			SunJdk8Internals.accessArray(instance),
			instance.size()                  ,
			handler
		);
		bytes.store_int(
		    BINARY_OFFSET_CAPACITY_INCREMENT,
		    SunJdk8Internals.getCapacityIncrement(instance)
		);
	}

	@Override
	public final Vector<?> create(final Binary bytes, final PersistenceLoadHandler idResolver)
	{
		// capacityIncrement can be any int value, even negative. So no validation can be done here.
		return new Vector<>(
			1,
			bytes.read_int(BINARY_OFFSET_CAPACITY_INCREMENT)
		);
	}

	@Override
	public final void updateState(final Binary bytes, final Vector<?> instance, final PersistenceLoadHandler idResolver)
	{
		// instance must be cleared and capacity-ensured in case an existing instance gets updated.
		instance.clear();
		instance.ensureCapacity(this.determineArrayLength(bytes, BINARY_OFFSET_SIZED_ARRAY));
		
		final int size = bytes.updateSizedArrayObjectReferences(
			BINARY_OFFSET_SIZED_ARRAY    ,
			idResolver,
			SunJdk8Internals.accessArray(instance)
		);
		SunJdk8Internals.setElementCount(instance, size);
	}

	@Override
	public final void iterateInstanceReferences(final Vector<?> instance, final PersistenceFunction iterator)
	{
		Persistence.iterateReferences(iterator, SunJdk8Internals.accessArray(instance), 0, instance.size());
	}

	@Override
	public final void iterateLoadableReferences(final Binary bytes, final PersistenceReferenceLoader iterator)
	{
		bytes.iterateSizedArrayElementReferences(BINARY_OFFSET_SIZED_ARRAY, iterator);
	}
	
}

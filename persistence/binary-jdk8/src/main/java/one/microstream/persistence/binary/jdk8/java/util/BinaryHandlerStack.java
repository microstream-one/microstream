package one.microstream.persistence.binary.jdk8.java.util;

import static one.microstream.X.notNull;

import java.util.Stack;

import one.microstream.persistence.binary.internal.AbstractBinaryHandlerCustomIterableSizedArray;
import one.microstream.persistence.binary.jdk8.types.SunJdk8Internals;
import one.microstream.persistence.binary.types.Binary;
import one.microstream.persistence.types.Persistence;
import one.microstream.persistence.types.PersistenceFunction;
import one.microstream.persistence.types.PersistenceLoadHandler;
import one.microstream.persistence.types.PersistenceReferenceLoader;
import one.microstream.persistence.types.PersistenceSizedArrayLengthController;
import one.microstream.persistence.types.PersistenceStoreHandler;


public final class BinaryHandlerStack extends AbstractBinaryHandlerCustomIterableSizedArray<Stack<?>>
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
	private static Class<Stack<?>> handledType()
	{
		return (Class)Stack.class; // no idea how to get ".class" to work otherwise
	}
	
	public static BinaryHandlerStack New(final PersistenceSizedArrayLengthController controller)
	{
		return new BinaryHandlerStack(
			notNull(controller)
		);
	}



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	BinaryHandlerStack(final PersistenceSizedArrayLengthController controller)
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
		final Stack<?>                        instance,
		final long                            objectId,
		final PersistenceStoreHandler<Binary> handler
	)
	{
		bytes.storeSizedArray(
			this.typeId()                ,
			objectId                     ,
			BINARY_OFFSET_SIZED_ARRAY    ,
			SunJdk8Internals.accessArray(instance),
			instance.size()              ,
			handler
		);
		bytes.store_int(
		    BINARY_OFFSET_CAPACITY_INCREMENT,
		    SunJdk8Internals.getCapacityIncrement(instance)
		);
	}

	@Override
	public final Stack<?> create(final Binary bytes, final PersistenceLoadHandler idResolver)
	{
		return new Stack<>();
	}

	@Override
	public final void updateState(final Binary bytes, final Stack<?> instance, final PersistenceLoadHandler idResolver)
	{
		// instance must be cleared and capacity-ensured in case an existing instance gets updated.
		instance.clear();
		instance.ensureCapacity(bytes.getSizedArrayLength(BINARY_OFFSET_SIZED_ARRAY));
		
		final int size = bytes.updateSizedArrayObjectReferences(
			BINARY_OFFSET_SIZED_ARRAY,
			idResolver,
			SunJdk8Internals.accessArray(instance)
		);
		SunJdk8Internals.setElementCount(instance, size);
	}

	@Override
	public final void iterateInstanceReferences(final Stack<?> instance, final PersistenceFunction iterator)
	{
		Persistence.iterateReferences(iterator, SunJdk8Internals.accessArray(instance), 0, instance.size());
	}

	@Override
	public final void iterateLoadableReferences(final Binary bytes, final PersistenceReferenceLoader iterator)
	{
		bytes.iterateSizedArrayElementReferences(BINARY_OFFSET_SIZED_ARRAY, iterator);
	}
	
}

package one.microstream.java.util;

import java.util.ArrayList;

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


public final class BinaryHandlerArrayList
extends AbstractBinaryHandlerCustomCollectionSizedArray<ArrayList<?>>
{
	///////////////////////////////////////////////////////////////////////////
	// constants        //
	/////////////////////

	static final long BINARY_OFFSET_SIZED_ARRAY = 0; // binary form is 100% just a sized array, so offset 0



	///////////////////////////////////////////////////////////////////////////
	// static methods    //
	/////////////////////

	@SuppressWarnings({"unchecked", "rawtypes"})
	private static Class<ArrayList<?>> typeWorkaround()
	{
		return (Class)ArrayList.class; // no idea how to get ".class" to work otherwise
	}



	///////////////////////////////////////////////////////////////////////////
	// constructors     //
	/////////////////////

	public BinaryHandlerArrayList(final PersistenceSizedArrayLengthController controller)
	{
		super(
			typeWorkaround(),
			BinaryCollectionHandling.sizedArrayPseudoFields(),
			controller
		);
	}


	
	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////
	
	@Override
	public final void store(
		final Binary                  bytes   ,
		final ArrayList<?>            instance,
		final long                    objectId,
		final PersistenceStoreHandler handler
	)
	{
		bytes.storeSizedArray(
			this.typeId()                  ,
			objectId                       ,
			BINARY_OFFSET_SIZED_ARRAY      ,
			XMemory.accessArray(instance),
			instance.size()                ,
			handler
		);
	}

	@Override
	public final ArrayList<?> create(final Binary bytes)
	{
		/*
		 * InitialCapacity 1 instead of default constructor is a workaround for yet another JDK moronity bug.
		 * Using the default constructor causes #ensureCapacity to yield incorrect behavior for values of
		 * 10 or below, which causes a subsequent array length validation exception.
		 * Also see https://bugs.openjdk.java.net/browse/JDK-8206945
		 */
		return new ArrayList<>(1);
	}

	@Override
	public final void update(final Binary bytes, final ArrayList<?> instance, final PersistenceLoadHandler builder)
	{
		// length must be checked for consistency reasons
		instance.ensureCapacity(this.determineArrayLength(bytes, BINARY_OFFSET_SIZED_ARRAY));
		final int size = bytes.updateSizedArrayObjectReferences(
			BINARY_OFFSET_SIZED_ARRAY,
			XMemory.accessArray(instance),
			builder
		);
		XMemory.setSize(instance, size);
	}

	@Override
	public final void iterateInstanceReferences(final ArrayList<?> instance, final PersistenceFunction iterator)
	{
		Persistence.iterateReferences(iterator, XMemory.accessArray(instance), 0, instance.size());
	}

	@Override
	public final void iteratePersistedReferences(final Binary bytes, final PersistenceObjectIdAcceptor iterator)
	{
		bytes.iterateSizedArrayElementReferences(BINARY_OFFSET_SIZED_ARRAY, iterator);
	}

}

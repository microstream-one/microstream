package net.jadoth.persistence.binary.internal;

import java.util.ArrayList;

import net.jadoth.functional._longProcedure;
import net.jadoth.memory.XMemory;
import net.jadoth.persistence.binary.types.Binary;
import net.jadoth.persistence.binary.types.BinaryCollectionHandling;
import net.jadoth.persistence.types.Persistence;
import net.jadoth.persistence.types.PersistenceFunction;
import net.jadoth.persistence.types.PersistenceLoadHandler;
import net.jadoth.persistence.types.PersistenceSizedArrayLengthController;
import net.jadoth.persistence.types.PersistenceStoreHandler;


public final class BinaryHandlerArrayList
extends AbstractBinaryHandlerNativeCustomCollectionSizedArray<ArrayList<?>>
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
		final long                    oid     ,
		final PersistenceStoreHandler handler
	)
	{
		bytes.storeSizedArray(
			this.typeId(),
			oid,
			BINARY_OFFSET_SIZED_ARRAY,
			XMemory.accessStorage(instance),
			instance.size(),
			handler
		);
	}

	@Override
	public final ArrayList<?> create(final Binary bytes)
	{
		return new ArrayList<>();
	}

	@Override
	public final void update(final Binary bytes, final ArrayList<?> instance, final PersistenceLoadHandler builder)
	{
		// length must be checked for consistency reasons
		instance.ensureCapacity(this.determineArrayLength(bytes, BINARY_OFFSET_SIZED_ARRAY));
		final int size = bytes.updateSizedArrayObjectReferences(
			BINARY_OFFSET_SIZED_ARRAY,
			XMemory.accessStorage(instance),
			builder
		);
		XMemory.setSize(instance, size);
	}

	@Override
	public final void iterateInstanceReferences(final ArrayList<?> instance, final PersistenceFunction iterator)
	{
		Persistence.iterateReferences(iterator, XMemory.accessStorage(instance), 0, instance.size());
	}

	@Override
	public final void iteratePersistedReferences(final Binary bytes, final _longProcedure iterator)
	{
		bytes.iterateSizedArrayElementReferences(BINARY_OFFSET_SIZED_ARRAY, iterator);
	}

}

package net.jadoth.persistence.binary.internal;

import java.util.ArrayList;

import net.jadoth.functional._longProcedure;
import net.jadoth.memory.XMemory;
import net.jadoth.persistence.binary.types.Binary;
import net.jadoth.persistence.binary.types.BinaryCollectionHandling;
import net.jadoth.persistence.types.Persistence;
import net.jadoth.persistence.types.PersistenceFunction;
import net.jadoth.persistence.types.PersistenceLoadHandler;
import net.jadoth.persistence.types.PersistenceStoreHandler;


public final class BinaryHandlerArrayList extends AbstractBinaryHandlerNativeCustomCollection<ArrayList<?>>
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
	
	private static int getBuildItemArrayLength(final Binary bytes)
	{
		return BinaryCollectionHandling.getSizedArrayLength(bytes, BINARY_OFFSET_SIZED_ARRAY);
	}



	///////////////////////////////////////////////////////////////////////////
	// constructors     //
	/////////////////////

	public BinaryHandlerArrayList()
	{
		super(
			typeWorkaround(),
			BinaryCollectionHandling.sizedArrayPseudoFields()
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
		BinaryCollectionHandling.storeSizedArray(
			bytes,
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
		return new ArrayList<>(
			getBuildItemArrayLength(bytes)
		);
	}

	@Override
	public final void update(final Binary bytes, final ArrayList<?> instance, final PersistenceLoadHandler builder)
	{
		instance.ensureCapacity(getBuildItemArrayLength(bytes));
		final int size = BinaryCollectionHandling.updateSizedArrayObjectReferences(
			bytes,
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
		BinaryCollectionHandling.iterateSizedArrayElementReferences(bytes, BINARY_OFFSET_SIZED_ARRAY, iterator);
	}

}

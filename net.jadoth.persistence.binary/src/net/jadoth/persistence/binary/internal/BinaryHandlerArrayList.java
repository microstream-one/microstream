package net.jadoth.persistence.binary.internal;

import java.util.ArrayList;

import net.jadoth.functional._longProcedure;
import net.jadoth.low.XMemory;
import net.jadoth.persistence.binary.types.Binary;
import net.jadoth.persistence.binary.types.BinaryCollectionHandling;
import net.jadoth.persistence.binary.types.BinaryValueAccessor;
import net.jadoth.persistence.types.Persistence;
import net.jadoth.persistence.types.PersistenceFunction;
import net.jadoth.persistence.types.PersistenceLoadHandler;
import net.jadoth.persistence.types.PersistenceStoreHandler;


public final class BinaryHandlerArrayList extends AbstractBinaryHandlerNativeCustomCollection<ArrayList<?>>
{
	///////////////////////////////////////////////////////////////////////////
	// constants        //
	/////////////////////

	static final long SIZED_ARRAY_BINARY_OFFSET = 0; // binary form is 100% just a sized array, so offset 0



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

	public BinaryHandlerArrayList(final BinaryValueAccessor binaryValueAccessor)
	{
		super(
			typeWorkaround(),
			binaryValueAccessor,
			BinaryCollectionHandling.sizedArrayPseudoFields()
		);
	}


	
	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////
	
	@Override
	public final void store(
		final Binary         bytes   ,
		final ArrayList<?>   instance,
		final long           oid     ,
		final PersistenceStoreHandler handler
	)
	{
		BinaryCollectionHandling.storeSizedArray(
			bytes,
			this.typeId(),
			oid,
			SIZED_ARRAY_BINARY_OFFSET,
			XMemory.accessStorage(instance),
			instance.size(),
			handler
		);
	}

	@Override
	public final ArrayList<?> create(final Binary bytes)
	{
		return new ArrayList<>(
			BinaryCollectionHandling.getSizedArrayLength(bytes, SIZED_ARRAY_BINARY_OFFSET)
		);
	}

	@Override
	public final void update(final Binary bytes, final ArrayList<?> instance, final PersistenceLoadHandler builder)
	{
		instance.ensureCapacity(BinaryCollectionHandling.getSizedArrayLength(bytes, SIZED_ARRAY_BINARY_OFFSET));
		final int size = BinaryCollectionHandling.updateSizedArrayObjectReferences(
			bytes,
			SIZED_ARRAY_BINARY_OFFSET,
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
		BinaryCollectionHandling.iterateSizedArrayElementReferences(bytes, SIZED_ARRAY_BINARY_OFFSET, iterator);
	}

}

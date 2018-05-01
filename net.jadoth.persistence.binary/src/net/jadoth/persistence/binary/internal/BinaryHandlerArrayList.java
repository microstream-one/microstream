package net.jadoth.persistence.binary.internal;

import java.util.ArrayList;

import net.jadoth.functional._longProcedure;
import net.jadoth.memory.Memory;
import net.jadoth.persistence.binary.types.Binary;
import net.jadoth.persistence.binary.types.BinaryCollectionHandling;
import net.jadoth.swizzling.types.PersistenceStoreFunction;
import net.jadoth.swizzling.types.Swizzle;
import net.jadoth.swizzling.types.SwizzleBuildLinker;
import net.jadoth.swizzling.types.SwizzleFunction;


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

	public BinaryHandlerArrayList(final long typeId)
	{
		super(
			typeId,
			typeWorkaround(),
			BinaryCollectionHandling.sizedArrayPseudoFields()
		);
	}


	
	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////
	
	@Override
	public final void store(
		final Binary             bytes   ,
		final ArrayList<?>       instance,
		final long               oid     ,
		final PersistenceStoreFunction linker
	)
	{
		BinaryCollectionHandling.storeSizedArray(
			bytes,
			this.typeId(),
			oid,
			SIZED_ARRAY_BINARY_OFFSET,
			Memory.accessStorage(instance),
			instance.size(),
			linker
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
	public final void update(final Binary bytes, final ArrayList<?> instance, final SwizzleBuildLinker builder)
	{
		instance.ensureCapacity(BinaryCollectionHandling.getSizedArrayLength(bytes, SIZED_ARRAY_BINARY_OFFSET));
		final int size = BinaryCollectionHandling.updateSizedArrayObjectReferences(
			bytes,
			SIZED_ARRAY_BINARY_OFFSET,
			Memory.accessStorage(instance),
			builder
		);
		Memory.setSize(instance, size);
	}

	@Override
	public final void iterateInstanceReferences(final ArrayList<?> instance, final SwizzleFunction iterator)
	{
		Swizzle.iterateReferences(iterator, Memory.accessStorage(instance), 0, instance.size());
	}

	@Override
	public final void iteratePersistedReferences(final Binary bytes, final _longProcedure iterator)
	{
		BinaryCollectionHandling.iterateSizedArrayElementReferences(bytes, SIZED_ARRAY_BINARY_OFFSET, iterator);
	}

}

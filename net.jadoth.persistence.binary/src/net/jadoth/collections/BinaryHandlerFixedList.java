package net.jadoth.collections;

import net.jadoth.Jadoth;
import net.jadoth.functional._longProcedure;
import net.jadoth.memory.Memory;
import net.jadoth.memory.objectstate.ObjectState;
import net.jadoth.memory.objectstate.ObjectStateHandlerLookup;
import net.jadoth.persistence.binary.internal.AbstractBinaryHandlerNativeCustomCollection;
import net.jadoth.persistence.binary.types.Binary;
import net.jadoth.persistence.binary.types.BinaryCollectionHandling;
import net.jadoth.persistence.binary.types.BinaryPersistence;
import net.jadoth.swizzling.types.Swizzle;
import net.jadoth.swizzling.types.SwizzleBuildLinker;
import net.jadoth.swizzling.types.SwizzleFunction;
import net.jadoth.swizzling.types.PersistenceStoreFunction;


/**
 *
 * @author Thomas Muenz
 */
public final class BinaryHandlerFixedList
extends AbstractBinaryHandlerNativeCustomCollection<FixedList<?>>
{
	///////////////////////////////////////////////////////////////////////////
	// constants        //
	/////////////////////

	private static final int  BITS_3                    = 3;
	private static final long BINARY_OFFSET_SIZED_ARRAY = 0; // binary form is 100% just a simple array, so offset 0



	///////////////////////////////////////////////////////////////////////////
	// static methods    //
	/////////////////////

	@SuppressWarnings({"unchecked",  "rawtypes"})
	private static Class<FixedList<?>> typeWorkaround()
	{
		// no idea how to get ".class" to work otherwise
		return (Class)FixedList.class;
	}



	///////////////////////////////////////////////////////////////////////////
	// constructors     //
	/////////////////////

	public BinaryHandlerFixedList(final long typeId)
	{
		// binary layout definition
		super(
			typeId,
			typeWorkaround(),
			BinaryCollectionHandling.simpleArrayPseudoFields()
		);
	}



	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////

	@Override
	public final void store(
		final Binary          bytes    ,
		final FixedList<?>    instance ,
		final long            oid      ,
		final PersistenceStoreFunction linker
	)
	{
		final Object[] arrayInstance = instance.data;
		final long contentAddress = bytes.storeEntityHeader(
			BinaryPersistence.calculateReferenceListTotalBinaryLength(arrayInstance.length),
			this.typeId(),
			oid
		);
		BinaryPersistence.storeArrayContentAsList(contentAddress, linker, arrayInstance, 0, arrayInstance.length);
	}

	@Override
	public final FixedList<?> create(final Binary bytes)
	{
		return new FixedList<>(Jadoth.checkArrayRange(BinaryPersistence.getListElementCount(bytes)));
	}

	@Override
	public final void update(final Binary bytes, final FixedList<?> instance, final SwizzleBuildLinker builder)
	{
		final Object[] arrayInstance = instance.data;

		// length must be checked for consistency reasons
		BinaryCollectionHandling.validateArrayLength(arrayInstance, bytes, BINARY_OFFSET_SIZED_ARRAY);

		final long binaryRefOffset = BinaryPersistence.getListElementsAddress(bytes);
		for(int i = 0; i < arrayInstance.length; i++)
		{
			// bounds-check eliminated array setting has about equal performance as manual unsafe putting
			arrayInstance[i] = builder.lookupObject(Memory.get_long(binaryRefOffset + (i << BITS_3)));
		}
	}

	@Override
	public final void iterateInstanceReferences(final FixedList<?> instance, final SwizzleFunction iterator)
	{
		Swizzle.iterateReferences(iterator, instance.data, 0, instance.data.length);
	}

	@Override
	public final void iteratePersistedReferences(final Binary bytes, final _longProcedure iterator)
	{
		BinaryPersistence.iterateListElementReferences(bytes, BINARY_OFFSET_SIZED_ARRAY, iterator);
	}

	@Override
	public final boolean isEqual(
		final FixedList<?>             source            ,
		final FixedList<?>             target            ,
		final ObjectStateHandlerLookup stateHandlerLookup
	)
	{
		return source.data.length == target.data.length
			&& ObjectState.isEqual(source.data, target.data, 0, source.data.length, stateHandlerLookup)
		;
	}

//	@Override
//	public final void copy(final FixedList<?> source, final FixedList<?> target)
//	{
//		if(source.data.length > target.data.length)
//		{
//			throw new RuntimeException(); // (23.10.2013 TM)EXCP: proper exception
//		}
//		BinaryCollectionHandling.copyContent(source.data, target.data, source.data.length);
//	}

}

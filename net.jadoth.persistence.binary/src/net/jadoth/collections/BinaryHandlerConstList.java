package net.jadoth.collections;

import net.jadoth.X;
import net.jadoth.functional._longProcedure;
import net.jadoth.low.XVM;
import net.jadoth.persistence.binary.internal.AbstractBinaryHandlerNativeCustomCollection;
import net.jadoth.persistence.binary.types.Binary;
import net.jadoth.persistence.binary.types.BinaryCollectionHandling;
import net.jadoth.persistence.binary.types.BinaryPersistence;
import net.jadoth.swizzling.types.PersistenceStoreFunction;
import net.jadoth.swizzling.types.Swizzle;
import net.jadoth.swizzling.types.SwizzleBuildLinker;
import net.jadoth.swizzling.types.SwizzleFunction;


/**
 *
 * @author Thomas Muenz
 */
public final class BinaryHandlerConstList
extends AbstractBinaryHandlerNativeCustomCollection<ConstList<?>>
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
	private static Class<ConstList<?>> typeWorkaround()
	{
		// no idea how to get ".class" to work otherwise
		return (Class)ConstList.class;
	}



	///////////////////////////////////////////////////////////////////////////
	// constructors     //
	/////////////////////

	public BinaryHandlerConstList()
	{
		// binary layout definition
		super(
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
		final ConstList<?>    instance ,
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
	public final ConstList<?> create(final Binary bytes)
	{
		return ConstList.New(X.checkArrayRange(BinaryPersistence.getListElementCount(bytes)));
	}

	@Override
	public final void update(final Binary bytes, final ConstList<?> instance, final SwizzleBuildLinker builder)
	{
		final Object[] arrayInstance = instance.data;

		// length must be checked for consistency reasons
		BinaryCollectionHandling.validateArrayLength(arrayInstance, bytes, BINARY_OFFSET_SIZED_ARRAY);

		final long binaryRefOffset = BinaryPersistence.getListElementsAddress(bytes);
		for(int i = 0; i < arrayInstance.length; i++)
		{
			// bounds-check eliminated array setting has about equal performance as manual unsafe putting
			arrayInstance[i] = builder.lookupObject(XVM.get_long(binaryRefOffset + (i << BITS_3)));
		}
	}

	@Override
	public final void iterateInstanceReferences(final ConstList<?> instance, final SwizzleFunction iterator)
	{
		Swizzle.iterateReferences(iterator, instance.data, 0, instance.data.length);
	}

	@Override
	public final void iteratePersistedReferences(final Binary bytes, final _longProcedure iterator)
	{
		BinaryPersistence.iterateListElementReferences(bytes, BINARY_OFFSET_SIZED_ARRAY, iterator);
	}

}

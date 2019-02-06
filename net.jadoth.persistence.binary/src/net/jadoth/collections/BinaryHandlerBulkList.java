package net.jadoth.collections;

import net.jadoth.persistence.binary.internal.AbstractBinaryHandlerNativeCustomCollectionSizedArray;
import net.jadoth.persistence.binary.types.Binary;
import net.jadoth.persistence.binary.types.BinaryCollectionHandling;
import net.jadoth.persistence.types.Persistence;
import net.jadoth.persistence.types.PersistenceFunction;
import net.jadoth.persistence.types.PersistenceLoadHandler;
import net.jadoth.persistence.types.PersistenceObjectIdAcceptor;
import net.jadoth.persistence.types.PersistenceSizedArrayLengthController;
import net.jadoth.persistence.types.PersistenceStoreHandler;


/**
 *
 * @author Thomas Muenz
 */
public final class BinaryHandlerBulkList
extends AbstractBinaryHandlerNativeCustomCollectionSizedArray<BulkList<?>>
{
	///////////////////////////////////////////////////////////////////////////
	// constants        //
	/////////////////////

	static final long BINARY_OFFSET_SIZED_ARRAY = 0; // binary form is 100% just a sized array, so offset 0



	///////////////////////////////////////////////////////////////////////////
	// static methods   //
	/////////////////////

	@SuppressWarnings({"unchecked",  "rawtypes"})
	private static Class<BulkList<?>> typeWorkaround()
	{
		// no idea how to get ".class" to work otherwise
		return (Class)BulkList.class;
	}
	


	///////////////////////////////////////////////////////////////////////////
	// constructors     //
	/////////////////////

	public BinaryHandlerBulkList(final PersistenceSizedArrayLengthController controller)
	{
		// binary layout definition
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
		final BulkList<?>             instance,
		final long                    oid     ,
		final PersistenceStoreHandler handler
	)
	{
		bytes.storeSizedArray(
			this.typeId()            ,
			oid                      ,
			BINARY_OFFSET_SIZED_ARRAY,
			instance.data            ,
			instance.size            ,
			handler
		);
	}

	@Override
	public final BulkList<?> create(final Binary bytes)
	{
		return new BulkList<>();
	}

	@Override
	public final void update(final Binary bytes, final BulkList<?> instance, final PersistenceLoadHandler handler)
	{
		// length must be checked for consistency reasons
		instance.ensureCapacity(this.determineArrayLength(bytes, BINARY_OFFSET_SIZED_ARRAY));
		instance.size = bytes.updateSizedArrayObjectReferences(
			BINARY_OFFSET_SIZED_ARRAY,
			instance.data            ,
			handler
		);
	}

	@Override
	public final void iterateInstanceReferences(final BulkList<?> instance, final PersistenceFunction iterator)
	{
		Persistence.iterateReferences(iterator, instance.data, 0, instance.size);
	}

	@Override
	public final void iteratePersistedReferences(final Binary bytes, final PersistenceObjectIdAcceptor iterator)
	{
		bytes.iterateSizedArrayElementReferences(BINARY_OFFSET_SIZED_ARRAY, iterator);
	}
	
}

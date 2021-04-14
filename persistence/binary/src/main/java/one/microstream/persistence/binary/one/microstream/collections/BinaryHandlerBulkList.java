package one.microstream.persistence.binary.one.microstream.collections;

import static one.microstream.X.notNull;

import one.microstream.collections.BulkList;
import one.microstream.persistence.binary.internal.AbstractBinaryHandlerCustomIterableSizedArray;
import one.microstream.persistence.binary.types.Binary;
import one.microstream.persistence.types.Persistence;
import one.microstream.persistence.types.PersistenceFunction;
import one.microstream.persistence.types.PersistenceLoadHandler;
import one.microstream.persistence.types.PersistenceReferenceLoader;
import one.microstream.persistence.types.PersistenceSizedArrayLengthController;
import one.microstream.persistence.types.PersistenceStoreHandler;


/**
 *
 * 
 */
public final class BinaryHandlerBulkList
extends AbstractBinaryHandlerCustomIterableSizedArray<BulkList<?>>
{
	///////////////////////////////////////////////////////////////////////////
	// constants //
	//////////////

	static final long BINARY_OFFSET_SIZED_ARRAY = 0; // binary form is 100% just a sized array, so offset 0



	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////

	@SuppressWarnings({"unchecked",  "rawtypes"})
	private static Class<BulkList<?>> handledType()
	{
		// no idea how to get ".class" to work otherwise
		return (Class)BulkList.class;
	}
	
	public static BinaryHandlerBulkList New(final PersistenceSizedArrayLengthController controller)
	{
		return new BinaryHandlerBulkList(
			notNull(controller)
		);
	}
	


	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////
	
	BinaryHandlerBulkList(final PersistenceSizedArrayLengthController controller)
	{
		// binary layout definition
		super(
			handledType(),
			SizedArrayFields(),
			controller
		);
	}
	


	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////

	@Override
	public final void store(
		final Binary                          data    ,
		final BulkList<?>                     instance,
		final long                            objectId,
		final PersistenceStoreHandler<Binary> handler
	)
	{
		data.storeSizedArray(
			this.typeId()                          ,
			objectId                               ,
			BINARY_OFFSET_SIZED_ARRAY              ,
			XCollectionsInternals.getData(instance),
			instance.intSize()                     ,
			handler
		);
	}

	@Override
	public final BulkList<?> create(final Binary data, final PersistenceLoadHandler handler)
	{
		return new BulkList<>();
	}

	@Override
	public final void updateState(final Binary data, final BulkList<?> instance, final PersistenceLoadHandler handler)
	{
		// must clear to avoid memory leaks due to residual references beyond the new size in existing instances.
		instance.clear();
		
		// length must be checked for consistency reasons
		instance.ensureCapacity(this.determineArrayLength(data, BINARY_OFFSET_SIZED_ARRAY));
		
		XCollectionsInternals.setSize(instance, data.updateSizedArrayObjectReferences(
			BINARY_OFFSET_SIZED_ARRAY,
			handler,
			XCollectionsInternals.getData(instance)
		));
	}

	@Override
	public final void iterateInstanceReferences(final BulkList<?> instance, final PersistenceFunction iterator)
	{
		Persistence.iterateReferences(iterator, XCollectionsInternals.getData(instance), 0, instance.intSize());
	}

	@Override
	public final void iterateLoadableReferences(final Binary data, final PersistenceReferenceLoader iterator)
	{
		data.iterateSizedArrayElementReferences(BINARY_OFFSET_SIZED_ARRAY, iterator);
	}
	
}

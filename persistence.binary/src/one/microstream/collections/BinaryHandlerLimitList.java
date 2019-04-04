package one.microstream.collections;

import one.microstream.persistence.binary.internal.AbstractBinaryHandlerCustomCollectionSizedArray;
import one.microstream.persistence.binary.types.Binary;
import one.microstream.persistence.types.Persistence;
import one.microstream.persistence.types.PersistenceFunction;
import one.microstream.persistence.types.PersistenceLoadHandler;
import one.microstream.persistence.types.PersistenceObjectIdAcceptor;
import one.microstream.persistence.types.PersistenceSizedArrayLengthController;
import one.microstream.persistence.types.PersistenceStoreHandler;


/**
 *
 * @author Thomas Muenz
 */
public final class BinaryHandlerLimitList
extends AbstractBinaryHandlerCustomCollectionSizedArray<LimitList<?>>
{
	///////////////////////////////////////////////////////////////////////////
	// constants //
	//////////////

	static final long BINARY_OFFSET_SIZED_ARRAY = 0; // binary form is 100% just a sized array, so offset 0



	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////

	@SuppressWarnings({"unchecked",  "rawtypes"})
	private static Class<LimitList<?>> typeWorkaround()
	{
		// no idea how to get ".class" to work otherwise
		return (Class)LimitList.class;
	}



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public BinaryHandlerLimitList(final PersistenceSizedArrayLengthController controller)
	{
		// binary layout definition
		super(
			typeWorkaround(),
			sizedArrayPseudoFields(),
			controller
		);
	}



	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////

	@Override
	public final void store(
		final Binary                  bytes   ,
		final LimitList<?>            instance,
		final long                    objectId,
		final PersistenceStoreHandler handler
	)
	{
		bytes.storeSizedArray(
			this.typeId()            ,
			objectId                 ,
			BINARY_OFFSET_SIZED_ARRAY,
			instance.data            ,
			instance.size            ,
			handler
		);
	}

	@Override
	public final LimitList<?> create(final Binary bytes, final PersistenceLoadHandler handler)
	{
		return new LimitList<>(this.determineArrayLength(bytes, BINARY_OFFSET_SIZED_ARRAY));
	}

	@Override
	public final void update(final Binary bytes, final LimitList<?> instance, final PersistenceLoadHandler handler)
	{
		// must clear to avoid memory leaks due to residual references beyond the new size in existing instances.
		instance.clear();
		
		// length must be checked for consistency reasons
		instance.ensureCapacity(this.determineArrayLength(bytes, BINARY_OFFSET_SIZED_ARRAY));
		instance.size = bytes.updateSizedArrayObjectReferences(
			BINARY_OFFSET_SIZED_ARRAY,
			instance.data            ,
			handler
		);
	}

	@Override
	public final void iterateInstanceReferences(final LimitList<?> instance, final PersistenceFunction iterator)
	{
		Persistence.iterateReferences(iterator, instance.data, 0, instance.size);
	}

	@Override
	public final void iteratePersistedReferences(final Binary bytes, final PersistenceObjectIdAcceptor iterator)
	{
		bytes.iterateSizedArrayElementReferences(BINARY_OFFSET_SIZED_ARRAY, iterator);
	}

}

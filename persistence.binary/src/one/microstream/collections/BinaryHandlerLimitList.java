package one.microstream.collections;

import static one.microstream.X.notNull;

import one.microstream.persistence.binary.internal.AbstractBinaryHandlerCustomIterableSizedArray;
import one.microstream.persistence.binary.types.Binary;
import one.microstream.persistence.types.Persistence;
import one.microstream.persistence.types.PersistenceFunction;
import one.microstream.persistence.types.PersistenceObjectIdAcceptor;
import one.microstream.persistence.types.PersistenceObjectIdResolver;
import one.microstream.persistence.types.PersistenceSizedArrayLengthController;
import one.microstream.persistence.types.PersistenceStoreHandler;


/**
 *
 * @author Thomas Muenz
 */
public final class BinaryHandlerLimitList
extends AbstractBinaryHandlerCustomIterableSizedArray<LimitList<?>>
{
	///////////////////////////////////////////////////////////////////////////
	// constants //
	//////////////

	static final long BINARY_OFFSET_SIZED_ARRAY = 0; // binary form is 100% just a sized array, so offset 0



	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////

	@SuppressWarnings({"unchecked",  "rawtypes"})
	private static Class<LimitList<?>> handledType()
	{
		// no idea how to get ".class" to work otherwise
		return (Class)LimitList.class;
	}
	
	public static BinaryHandlerLimitList New(final PersistenceSizedArrayLengthController controller)
	{
		return new BinaryHandlerLimitList(
			notNull(controller)
		);
	}



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	BinaryHandlerLimitList(final PersistenceSizedArrayLengthController controller)
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
	public final LimitList<?> create(final Binary bytes, final PersistenceObjectIdResolver idResolver)
	{
		return new LimitList<>(this.determineArrayLength(bytes, BINARY_OFFSET_SIZED_ARRAY));
	}

	@Override
	public final void update(final Binary bytes, final LimitList<?> instance, final PersistenceObjectIdResolver idResolver)
	{
		// must clear to avoid memory leaks due to residual references beyond the new size in existing instances.
		instance.clear();
		
		// length must be checked for consistency reasons
		instance.ensureCapacity(this.determineArrayLength(bytes, BINARY_OFFSET_SIZED_ARRAY));
		instance.size = bytes.updateSizedArrayObjectReferences(
			BINARY_OFFSET_SIZED_ARRAY,
			instance.data,
			idResolver
		);
	}

	@Override
	public final void iterateInstanceReferences(final LimitList<?> instance, final PersistenceFunction iterator)
	{
		Persistence.iterateReferences(iterator, instance.data, 0, instance.size);
	}

	@Override
	public final void iterateLoadableReferences(final Binary bytes, final PersistenceObjectIdAcceptor iterator)
	{
		bytes.iterateSizedArrayElementReferences(BINARY_OFFSET_SIZED_ARRAY, iterator);
	}

}

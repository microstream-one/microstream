package one.microstream.collections;

import one.microstream.X;
import one.microstream.persistence.binary.internal.AbstractBinaryHandlerCustomCollection;
import one.microstream.persistence.binary.types.Binary;
import one.microstream.persistence.types.Persistence;
import one.microstream.persistence.types.PersistenceFunction;
import one.microstream.persistence.types.PersistenceLoadHandler;
import one.microstream.persistence.types.PersistenceObjectIdAcceptor;
import one.microstream.persistence.types.PersistenceStoreHandler;


/**
 *
 * @author Thomas Muenz
 */
public final class BinaryHandlerFixedList
extends AbstractBinaryHandlerCustomCollection<FixedList<?>>
{
	///////////////////////////////////////////////////////////////////////////
	// constants //
	//////////////

	private static final long BINARY_OFFSET_LIST = 0;



	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////

	@SuppressWarnings({"unchecked",  "rawtypes"})
	private static Class<FixedList<?>> typeWorkaround()
	{
		// no idea how to get ".class" to work otherwise
		return (Class)FixedList.class;
	}



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public BinaryHandlerFixedList()
	{
		// binary layout definition
		super(
			typeWorkaround(),
			simpleArrayPseudoFields()
		);
	}



	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////

	@Override
	public final void store(
		final Binary                  bytes   ,
		final FixedList<?>            instance,
		final long                    objectId,
		final PersistenceStoreHandler handler
	)
	{
		bytes.storeReferences(
			this.typeId(),
			objectId     ,
			0            ,
			handler      ,
			instance.data
		);
	}

	@Override
	public final FixedList<?> create(final Binary bytes, final PersistenceLoadHandler handler)
	{
		return new FixedList<>(
			X.checkArrayRange(bytes.getListElementCountReferences(0))
		);
	}

	@Override
	public final void update(final Binary bytes, final FixedList<?> instance, final PersistenceLoadHandler handler)
	{
		final Object[] arrayInstance = instance.data;

		// Length must be checked for consistency reasons. No clearing required.
		bytes.validateArrayLength(arrayInstance, BINARY_OFFSET_LIST);
		bytes.collectElementsIntoArray(BINARY_OFFSET_LIST, handler, arrayInstance);
	}

	@Override
	public final void iterateInstanceReferences(final FixedList<?> instance, final PersistenceFunction iterator)
	{
		Persistence.iterateReferences(iterator, instance.data, 0, instance.data.length);
	}

	@Override
	public final void iteratePersistedReferences(final Binary bytes, final PersistenceObjectIdAcceptor iterator)
	{
		bytes.iterateListElementReferences(BINARY_OFFSET_LIST, iterator);
	}

}

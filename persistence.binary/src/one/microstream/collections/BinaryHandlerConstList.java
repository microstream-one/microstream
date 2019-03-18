package one.microstream.collections;

import one.microstream.X;
import one.microstream.collections.ConstList;
import one.microstream.persistence.binary.internal.AbstractBinaryHandlerCustomCollection;
import one.microstream.persistence.binary.types.Binary;
import one.microstream.persistence.binary.types.BinaryCollectionHandling;
import one.microstream.persistence.types.Persistence;
import one.microstream.persistence.types.PersistenceFunction;
import one.microstream.persistence.types.PersistenceLoadHandler;
import one.microstream.persistence.types.PersistenceObjectIdAcceptor;
import one.microstream.persistence.types.PersistenceStoreHandler;


/**
 *
 * @author Thomas Muenz
 */
public final class BinaryHandlerConstList
extends AbstractBinaryHandlerCustomCollection<ConstList<?>>
{
	///////////////////////////////////////////////////////////////////////////
	// constants        //
	/////////////////////

	private static final long BINARY_OFFSET_LIST = 0; // binary form is 100% just a simple list, so offset 0



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
		final Binary                  bytes   ,
		final ConstList<?>            instance,
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
	public final ConstList<?> create(final Binary bytes)
	{
		return ConstList.New(X.checkArrayRange(bytes.getListElementCountReferences(0)));
	}

	@Override
	public final void update(final Binary bytes, final ConstList<?> instance, final PersistenceLoadHandler builder)
	{
		final Object[] arrayInstance = instance.data;

		// length must be checked for consistency reasons
		bytes.validateArrayLength(arrayInstance, BINARY_OFFSET_LIST);
		bytes.collectElementsIntoArray(BINARY_OFFSET_LIST, builder, arrayInstance);
	}

	@Override
	public final void iterateInstanceReferences(final ConstList<?> instance, final PersistenceFunction iterator)
	{
		Persistence.iterateReferences(iterator, instance.data, 0, instance.data.length);
	}

	@Override
	public final void iteratePersistedReferences(final Binary bytes, final PersistenceObjectIdAcceptor iterator)
	{
		bytes.iterateListElementReferences(BINARY_OFFSET_LIST, iterator);
	}

}

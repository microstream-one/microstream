package one.microstream.persistence.binary.one.microstream.collections;

import one.microstream.X;
import one.microstream.collections.FixedList;
import one.microstream.persistence.binary.internal.AbstractBinaryHandlerCustomCollection;
import one.microstream.persistence.binary.types.Binary;
import one.microstream.persistence.types.Persistence;
import one.microstream.persistence.types.PersistenceFunction;
import one.microstream.persistence.types.PersistenceLoadHandler;
import one.microstream.persistence.types.PersistenceReferenceLoader;
import one.microstream.persistence.types.PersistenceStoreHandler;


/**
 *
 * 
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
	private static Class<FixedList<?>> handledType()
	{
		// no idea how to get ".class" to work otherwise
		return (Class)FixedList.class;
	}
	
	public static BinaryHandlerFixedList New()
	{
		return new BinaryHandlerFixedList();
	}



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	BinaryHandlerFixedList()
	{
		// binary layout definition
		super(
			handledType(),
			SimpleArrayFields()
		);
	}



	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////

	@Override
	public final void store(
		final Binary                          data    ,
		final FixedList<?>                    instance,
		final long                            objectId,
		final PersistenceStoreHandler<Binary> handler
	)
	{
		data.storeReferences(
			this.typeId()                          ,
			objectId                               ,
			0                                      ,
			handler                                ,
			XCollectionsInternals.getData(instance)
		);
	}

	@Override
	public final FixedList<?> create(final Binary data, final PersistenceLoadHandler handler)
	{
		return new FixedList<>(
			X.checkArrayRange(data.getListElementCountReferences(0))
		);
	}

	@Override
	public final void updateState(final Binary data, final FixedList<?> instance, final PersistenceLoadHandler handler)
	{
		final Object[] arrayInstance = XCollectionsInternals.getData(instance);

		// Length must be checked for consistency reasons. No clearing required.
		data.validateArrayLength(arrayInstance, BINARY_OFFSET_LIST);
		data.collectElementsIntoArray(BINARY_OFFSET_LIST, handler, arrayInstance);
	}

	@Override
	public final void iterateInstanceReferences(final FixedList<?> instance, final PersistenceFunction iterator)
	{
		final Object[] arrayInstance = XCollectionsInternals.getData(instance);
		Persistence.iterateReferences(iterator, arrayInstance, 0, arrayInstance.length);
	}

	@Override
	public final void iterateLoadableReferences(final Binary data, final PersistenceReferenceLoader iterator)
	{
		data.iterateListElementReferences(BINARY_OFFSET_LIST, iterator);
	}

}

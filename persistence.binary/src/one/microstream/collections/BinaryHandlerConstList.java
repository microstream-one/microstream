package one.microstream.collections;

import one.microstream.X;
import one.microstream.persistence.binary.internal.AbstractBinaryHandlerCustomCollection;
import one.microstream.persistence.binary.types.Binary;
import one.microstream.persistence.types.Persistence;
import one.microstream.persistence.types.PersistenceFunction;
import one.microstream.persistence.types.PersistenceLoadHandler;
import one.microstream.persistence.types.PersistenceReferenceLoader;
import one.microstream.persistence.types.PersistenceStoreHandler;


/**
 *
 * @author Thomas Muenz
 */
public final class BinaryHandlerConstList
extends AbstractBinaryHandlerCustomCollection<ConstList<?>>
{
	///////////////////////////////////////////////////////////////////////////
	// constants //
	//////////////

	private static final long BINARY_OFFSET_LIST = 0; // binary form is 100% just a simple list, so offset 0



	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////

	@SuppressWarnings({"unchecked",  "rawtypes"})
	private static Class<ConstList<?>> handledType()
	{
		// no idea how to get ".class" to work otherwise
		return (Class)ConstList.class;
	}
	
	public static BinaryHandlerConstList New()
	{
		return new BinaryHandlerConstList();
	}



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	BinaryHandlerConstList()
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
		final Binary                  data    ,
		final ConstList<?>            instance,
		final long                    objectId,
		final PersistenceStoreHandler handler
	)
	{
		data.storeReferences(
			this.typeId(),
			objectId     ,
			0            ,
			handler      ,
			instance.data
		);
	}

	@Override
	public final ConstList<?> create(final Binary data, final PersistenceLoadHandler handler)
	{
		return ConstList.New(X.checkArrayRange(data.getListElementCountReferences(0)));
	}

	@Override
	public final void updateState(final Binary data, final ConstList<?> instance, final PersistenceLoadHandler handler)
	{
		final Object[] arrayInstance = instance.data;

		// Length must be checked for consistency reasons. No clear required.
		data.validateArrayLength(arrayInstance, BINARY_OFFSET_LIST);
		data.collectElementsIntoArray(BINARY_OFFSET_LIST, handler, arrayInstance);
	}

	@Override
	public final void iterateInstanceReferences(final ConstList<?> instance, final PersistenceFunction iterator)
	{
		Persistence.iterateReferences(iterator, instance.data, 0, instance.data.length);
	}

	@Override
	public final void iterateLoadableReferences(final Binary data, final PersistenceReferenceLoader iterator)
	{
		data.iterateListElementReferences(BINARY_OFFSET_LIST, iterator);
	}

}

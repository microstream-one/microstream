package one.microstream.java.util;

import java.util.LinkedList;

import one.microstream.X;
import one.microstream.persistence.binary.internal.AbstractBinaryHandlerCustomCollection;
import one.microstream.persistence.binary.types.Binary;
import one.microstream.persistence.binary.types.BinaryCollectionHandling;
import one.microstream.persistence.types.Persistence;
import one.microstream.persistence.types.PersistenceFunction;
import one.microstream.persistence.types.PersistenceLoadHandler;
import one.microstream.persistence.types.PersistenceObjectIdAcceptor;
import one.microstream.persistence.types.PersistenceStoreHandler;


public final class BinaryHandlerLinkedList extends AbstractBinaryHandlerCustomCollection<LinkedList<?>>
{
	///////////////////////////////////////////////////////////////////////////
	// constants //
	//////////////

	static final long BINARY_OFFSET_ELEMENTS = 0;



	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////

	@SuppressWarnings({"unchecked", "rawtypes"})
	private static Class<LinkedList<?>> typeWorkaround()
	{
		return (Class)LinkedList.class; // no idea how to get ".class" to work otherwise
	}

	static final int getElementCount(final Binary bytes)
	{
		return X.checkArrayRange(bytes.getListElementCountReferences(BINARY_OFFSET_ELEMENTS));
	}



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public BinaryHandlerLinkedList()
	{
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
		final LinkedList<?>           instance,
		final long                    objectId,
		final PersistenceStoreHandler handler
	)
	{
		// store elements simply as array binary form
		bytes.storeIterableAsList(
			this.typeId()         ,
			objectId              ,
			BINARY_OFFSET_ELEMENTS,
			instance              ,
			instance.size()       ,
			handler
		);
	}

	@Override
	public final LinkedList<?> create(final Binary bytes, final PersistenceLoadHandler handler)
	{
		return new LinkedList<>();
	}

	@Override
	public final void update(final Binary bytes, final LinkedList<?> instance, final PersistenceLoadHandler handler)
	{
		// must clear in case an existing instance is updated.
		instance.clear();
		
		@SuppressWarnings("unchecked") // necessary because this handler operates on a generic technical level
		final LinkedList<Object> collectingInstance = (LinkedList<Object>)instance;
		
		// simple adding without helper is sufficient as LinkedList is not a hashing collection
		bytes.collectListObjectReferences(BINARY_OFFSET_ELEMENTS, handler, collectingInstance::addLast);
	}

	@Override
	public final void iterateInstanceReferences(final LinkedList<?> instance, final PersistenceFunction iterator)
	{
		Persistence.iterateReferencesIterable(iterator, instance);
	}

	@Override
	public final void iteratePersistedReferences(final Binary bytes, final PersistenceObjectIdAcceptor iterator)
	{
		bytes.iterateListElementReferences(BINARY_OFFSET_ELEMENTS, iterator);
	}

}

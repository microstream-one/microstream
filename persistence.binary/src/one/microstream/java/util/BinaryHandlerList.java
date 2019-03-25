package one.microstream.java.util;

import java.util.List;

import one.microstream.X;
import one.microstream.persistence.binary.internal.AbstractBinaryHandlerCustomCollection;
import one.microstream.persistence.binary.types.Binary;
import one.microstream.persistence.binary.types.BinaryCollectionHandling;
import one.microstream.persistence.types.Persistence;
import one.microstream.persistence.types.PersistenceFunction;
import one.microstream.persistence.types.PersistenceLoadHandler;
import one.microstream.persistence.types.PersistenceObjectIdAcceptor;
import one.microstream.persistence.types.PersistenceStoreHandler;


public class BinaryHandlerList<T extends List<?>> extends AbstractBinaryHandlerCustomCollection<T>
{
	///////////////////////////////////////////////////////////////////////////
	// constants //
	//////////////

	static final long BINARY_OFFSET_ELEMENTS = 0;



	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////

	static final long getElementCount(final Binary bytes)
	{
		return bytes.getListElementCountReferences(BINARY_OFFSET_ELEMENTS);
	}

	
	
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////
	
	public Instantiator<T> instantiator;
	
	

	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public BinaryHandlerList(final Class<T> type, final Instantiator<T> instantiator)
	{
		super(
			type,
			BinaryCollectionHandling.simpleArrayPseudoFields()
		);
		this.instantiator = instantiator;
	}


	
	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////
	
	@Override
	public void store(
		final Binary                  bytes   ,
		final T                       instance,
		final long                    objectId,
		final PersistenceStoreHandler handler
	)
	{
		this.internalStore(bytes, instance, objectId, handler);
	}
	
	protected long internalStore(
		final Binary                  bytes   ,
		final T                       instance,
		final long                    objectId,
		final PersistenceStoreHandler handler
	)
	{
		// store elements simply as array binary form
		return bytes.storeIterableAsList(
			this.typeId()         ,
			objectId              ,
			BINARY_OFFSET_ELEMENTS,
			instance              ,
			instance.size()       ,
			handler
		);
	}

	@Override
	public T create(final Binary bytes, final PersistenceLoadHandler handler)
	{
		return this.instantiator.instantiateList(
			getElementCount(bytes)
		);
	}

	@Override
	public void update(final Binary bytes, final T instance, final PersistenceLoadHandler handler)
	{
		// instance must be cleared in case an existing one is updated
		instance.clear();
		
		@SuppressWarnings("unchecked")
		final List<Object> castedInstance = (List<Object>)instance;
		
		bytes.collectObjectReferences(
			BINARY_OFFSET_ELEMENTS,
			X.checkArrayRange(getElementCount(bytes)),
			handler,
			e ->
				castedInstance.add(e)
		);
	}

	@Override
	public void iterateInstanceReferences(final T instance, final PersistenceFunction iterator)
	{
		Persistence.iterateReferencesIterable(iterator, instance);
	}

	@Override
	public void iteratePersistedReferences(final Binary bytes, final PersistenceObjectIdAcceptor iterator)
	{
		bytes.iterateListElementReferences(BINARY_OFFSET_ELEMENTS, iterator);
	}
	
	
	
	public interface Instantiator<T extends List<?>>
	{
		public T instantiateList(long elementCount);
	}

}

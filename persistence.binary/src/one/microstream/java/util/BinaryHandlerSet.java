package one.microstream.java.util;

import java.util.Set;

import one.microstream.X;
import one.microstream.collections.old.OldCollections;
import one.microstream.persistence.binary.internal.AbstractBinaryHandlerCustomCollection;
import one.microstream.persistence.binary.types.Binary;
import one.microstream.persistence.types.Persistence;
import one.microstream.persistence.types.PersistenceFunction;
import one.microstream.persistence.types.PersistenceLoadHandler;
import one.microstream.persistence.types.PersistenceObjectIdAcceptor;
import one.microstream.persistence.types.PersistenceStoreHandler;


public class BinaryHandlerSet<T extends Set<?>> extends AbstractBinaryHandlerCustomCollection<T>
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

	public BinaryHandlerSet(final Class<T> type, final Instantiator<T> instantiator)
	{
		super(
			type,
			simpleArrayPseudoFields()
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
		return this.instantiator.instantiateSet(
			getElementCount(bytes)
		);
	}

	@Override
	public void update(final Binary bytes, final T instance, final PersistenceLoadHandler handler)
	{
		instance.clear();
		final Object[] elementsHelper = new Object[X.checkArrayRange(getElementCount(bytes))];
		bytes.collectElementsIntoArray(BINARY_OFFSET_ELEMENTS, handler, elementsHelper);
		bytes.registerHelper(instance, elementsHelper);
	}

	@Override
	public void complete(final Binary bytes, final T instance, final PersistenceLoadHandler loadHandler)
	{
		OldCollections.populateSetFromHelperArray(instance, bytes.getHelper(instance));
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
	
	
	
	public interface Instantiator<T extends Set<?>>
	{
		public T instantiateSet(long elementCount);
	}

}

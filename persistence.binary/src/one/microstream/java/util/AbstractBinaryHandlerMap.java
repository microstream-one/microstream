package one.microstream.java.util;

import java.util.Map;

import one.microstream.X;
import one.microstream.collections.old.KeyValueFlatCollector;
import one.microstream.collections.old.OldCollections;
import one.microstream.persistence.binary.internal.AbstractBinaryHandlerCustomCollection;
import one.microstream.persistence.binary.types.Binary;
import one.microstream.persistence.types.Persistence;
import one.microstream.persistence.types.PersistenceFunction;
import one.microstream.persistence.types.PersistenceLoadHandler;
import one.microstream.persistence.types.PersistenceObjectIdAcceptor;
import one.microstream.persistence.types.PersistenceStoreHandler;


public abstract class AbstractBinaryHandlerMap<T extends Map<?, ?>>
extends AbstractBinaryHandlerCustomCollection<T>
{
	///////////////////////////////////////////////////////////////////////////
	// constants //
	//////////////

	static final long BINARY_OFFSET_ELEMENTS = 0;

	
	
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////
	
	public static final long getElementCount(final Binary bytes)
	{
		return bytes.getListElementCountKeyValue(BINARY_OFFSET_ELEMENTS);
	}
	
	

	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////
	
	public AbstractBinaryHandlerMap(final Class<T> type)
	{
		super(
			type,
			keyValuesPseudoFields()
		);
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
		// store elements simply as array binary form
		bytes.storeMapEntrySet(
			this.typeId()         ,
			objectId              ,
			BINARY_OFFSET_ELEMENTS,
			instance.entrySet()   ,
			handler
		);
	}

	@Override
	public void update(
		final Binary                 bytes   ,
		final T                      instance,
		final PersistenceLoadHandler handler
	)
	{
		instance.clear();
		final int elementCount = X.checkArrayRange(getElementCount(bytes));
		final KeyValueFlatCollector<Object, Object> collector = KeyValueFlatCollector.New(elementCount);
		bytes.collectKeyValueReferences(BINARY_OFFSET_ELEMENTS, elementCount, handler, collector);
		bytes.registerHelper(instance, collector.yield());
	}

	@Override
	public void complete(
		final Binary                  bytes   ,
		final T                       instance,
		final PersistenceLoadHandler  handler
	)
	{
		OldCollections.populateMapFromHelperArray(instance, bytes.getHelper(instance));
	}
	
	@Override
	public void iterateInstanceReferences(
		final T                       instance,
		final PersistenceFunction     iterator
	)
	{
		Persistence.iterateReferencesMap(iterator, instance);
	}

	@Override
	public void iteratePersistedReferences(
		final Binary                      bytes   ,
		final PersistenceObjectIdAcceptor iterator
	)
	{
		bytes.iterateKeyValueEntriesReferences(BINARY_OFFSET_ELEMENTS, iterator);
	}
		
}

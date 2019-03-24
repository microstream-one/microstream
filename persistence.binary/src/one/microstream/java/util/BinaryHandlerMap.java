package one.microstream.java.util;

import java.util.Map;

import one.microstream.X;
import one.microstream.collections.old.KeyValueFlatCollector;
import one.microstream.collections.old.OldCollections;
import one.microstream.persistence.binary.internal.AbstractBinaryHandlerCustomCollection;
import one.microstream.persistence.binary.types.Binary;
import one.microstream.persistence.binary.types.BinaryCollectionHandling;
import one.microstream.persistence.types.Persistence;
import one.microstream.persistence.types.PersistenceFunction;
import one.microstream.persistence.types.PersistenceLoadHandler;
import one.microstream.persistence.types.PersistenceObjectIdAcceptor;
import one.microstream.persistence.types.PersistenceStoreHandler;


public class BinaryHandlerMap<T extends Map<?, ?>> extends AbstractBinaryHandlerCustomCollection<T>
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
		return bytes.getListElementCountReferences(BINARY_OFFSET_ELEMENTS);
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////
	
	/**
	 * May be null if the extending class handles instantiation on its own.
	 */
	public Instantiator<T> instantiator;



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public BinaryHandlerMap(final Class<T> type)
	{
		this(type, null);
	}
	
	/**
	 * @param type
	 * @param instantiator May be null if the extending class handles instantiation on its own.
	 */
	public BinaryHandlerMap(final Class<T> type, final Instantiator<T> instantiator)
	{
		super(
			type,
			BinaryCollectionHandling.keyValuesPseudoFields()
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
		return bytes.storeMapEntrySet(
			this.typeId()         ,
			objectId              ,
			BINARY_OFFSET_ELEMENTS,
			instance.entrySet()   ,
			handler
		);
	}

	@Override
	public T create(
		final Binary                 bytes  ,
		final PersistenceLoadHandler handler
	)
	{
		return this.instantiator.instantiateMap(getElementCount(bytes));
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
		bytes.iterateListElementReferences(BINARY_OFFSET_ELEMENTS, iterator);
	}
	
	
	
	public interface Instantiator<T extends Map<?, ?>>
	{
		public T instantiateMap(long elementCount);
	}
	
}

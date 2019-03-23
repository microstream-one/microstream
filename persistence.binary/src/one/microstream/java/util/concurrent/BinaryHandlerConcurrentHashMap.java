package one.microstream.java.util.concurrent;

import java.util.concurrent.ConcurrentHashMap;

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


public final class BinaryHandlerConcurrentHashMap extends AbstractBinaryHandlerCustomCollection<ConcurrentHashMap<?, ?>>
{
	///////////////////////////////////////////////////////////////////////////
	// constants        //
	/////////////////////

	static final long BINARY_OFFSET_ELEMENTS = 0;
	/* note:
	 * the ConcurrentHashMap is such a crazy mess with even the simplest logic (load factor, initialCapacity)
	 * f***ed up that there is no gain in storing and using any additional metadata to reconstruct the state
	 * (like table length or such).
	 * As a consequence, this handler implementation only stores the pure elements and reconstructs the instance
	 * only using these.
	 * If anyone wants a more tailored handler for the insane mess, implement your own custom handler.
	 */



	///////////////////////////////////////////////////////////////////////////
	// static methods    //
	/////////////////////

	@SuppressWarnings({"unchecked", "rawtypes"})
	private static Class<ConcurrentHashMap<?, ?>> typeWorkaround()
	{
		return (Class)ConcurrentHashMap.class; // no idea how to get ".class" to work otherwise
	}

	static final int getElementCount(final Binary bytes)
	{
		return X.checkArrayRange(bytes.getListElementCountReferences(BINARY_OFFSET_ELEMENTS));
	}



	///////////////////////////////////////////////////////////////////////////
	// constructors     //
	/////////////////////

	public BinaryHandlerConcurrentHashMap()
	{
		super(
			typeWorkaround(),
			BinaryCollectionHandling.keyValuesPseudoFields()
		);
	}



	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////

	@Override
	public final void store(
		final Binary                  bytes   ,
		final ConcurrentHashMap<?, ?> instance,
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
	public final ConcurrentHashMap<?, ?> create(
		final Binary                 bytes  ,
		final PersistenceLoadHandler handler
	)
	{
		return new ConcurrentHashMap<>(
			getElementCount(bytes)
		);
	}

	@Override
	public final void update(
		final Binary                  bytes   ,
		final ConcurrentHashMap<?, ?> instance,
		final PersistenceLoadHandler  handler
	)
	{
		instance.clear();
		final int elementCount = getElementCount(bytes);
		final KeyValueFlatCollector<Object, Object> collector = KeyValueFlatCollector.New(elementCount);
		bytes.collectKeyValueReferences(BINARY_OFFSET_ELEMENTS, elementCount, handler, collector);
		bytes.registerHelper(instance, collector.yield());
	}

	@Override
	public void complete(
		final Binary                  bytes   ,
		final ConcurrentHashMap<?, ?> instance,
		final PersistenceLoadHandler  handler
	)
	{
		OldCollections.populateMapFromHelperArray(instance, bytes.getHelper(instance));
	}
	
	@Override
	public final void iterateInstanceReferences(
		final ConcurrentHashMap<?, ?> instance,
		final PersistenceFunction     iterator
	)
	{
		Persistence.iterateReferencesMap(iterator, instance);
	}

	@Override
	public final void iteratePersistedReferences(
		final Binary                      bytes   ,
		final PersistenceObjectIdAcceptor iterator
	)
	{
		bytes.iterateListElementReferences(BINARY_OFFSET_ELEMENTS, iterator);
	}
	
}

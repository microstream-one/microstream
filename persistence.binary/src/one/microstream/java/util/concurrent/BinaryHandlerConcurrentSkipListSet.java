package one.microstream.java.util.concurrent;

import java.util.Comparator;
import java.util.concurrent.ConcurrentSkipListSet;

import one.microstream.X;
import one.microstream.collections.old.OldCollections;
import one.microstream.persistence.binary.internal.AbstractBinaryHandlerCustomCollection;
import one.microstream.persistence.binary.types.Binary;
import one.microstream.persistence.types.Persistence;
import one.microstream.persistence.types.PersistenceFunction;
import one.microstream.persistence.types.PersistenceObjectIdAcceptor;
import one.microstream.persistence.types.PersistenceObjectIdResolver;
import one.microstream.persistence.types.PersistenceStoreHandler;


public final class BinaryHandlerConcurrentSkipListSet extends AbstractBinaryHandlerCustomCollection<ConcurrentSkipListSet<?>>
{
	///////////////////////////////////////////////////////////////////////////
	// constants //
	//////////////

	static final long BINARY_OFFSET_COMPARATOR =                                                      0;
	static final long BINARY_OFFSET_ELEMENTS   = BINARY_OFFSET_COMPARATOR + Binary.objectIdByteLength();



	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////

	@SuppressWarnings({"unchecked", "rawtypes"})
	private static Class<ConcurrentSkipListSet<?>> handledType()
	{
		return (Class)ConcurrentSkipListSet.class; // no idea how to get ".class" to work otherwise
	}
	
	@SuppressWarnings("unchecked")
	private static <E> Comparator<? super E> getComparator(
		final Binary                      bytes     ,
		final PersistenceObjectIdResolver idResolver
	)
	{
		return (Comparator<? super E>)idResolver.lookupObject(bytes.get_long(BINARY_OFFSET_COMPARATOR));
	}

	static final int getElementCount(final Binary bytes)
	{
		return X.checkArrayRange(bytes.getListElementCountReferences(BINARY_OFFSET_ELEMENTS));
	}
	
	public static BinaryHandlerConcurrentSkipListSet New()
	{
		return new BinaryHandlerConcurrentSkipListSet();
	}



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	BinaryHandlerConcurrentSkipListSet()
	{
		super(
			handledType(),
			SimpleArrayFields(
				CustomField(Comparator.class, "comparator")
			)
		);
		
	}



	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////

	@Override
	public final void store(
		final Binary                   bytes   ,
		final ConcurrentSkipListSet<?> instance,
		final long                     objectId,
		final PersistenceStoreHandler  handler
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
		
		bytes.store_long_Offset(
			BINARY_OFFSET_COMPARATOR,
			handler.apply(instance.comparator())
		);
	}
	
	@Override
	public final ConcurrentSkipListSet<?> create(
		final Binary                      bytes     ,
		final PersistenceObjectIdResolver idResolver
	)
	{
		return new ConcurrentSkipListSet<>(
			getComparator(bytes, idResolver)
		);
	}

	@Override
	public final void update(
		final Binary                      bytes     ,
		final ConcurrentSkipListSet<?>    instance  ,
		final PersistenceObjectIdResolver idResolver
	)
	{
		instance.clear();
		
		/*
		 * Tree collections don't use hashing, but their comparing logic still uses the elements' state,
		 * which might not yet be available when this method is called. Hence the detour to #complete.
		 */
		final Object[] elementsHelper = new Object[getElementCount(bytes)];
		bytes.collectElementsIntoArray(BINARY_OFFSET_ELEMENTS, idResolver, elementsHelper);
		bytes.registerHelper(instance, elementsHelper);
	}
	
	@Override
	public final void complete(
		final Binary                      bytes     ,
		final ConcurrentSkipListSet<?>    instance  ,
		final PersistenceObjectIdResolver idResolver
	)
	{
		OldCollections.populateCollectionFromHelperArray(instance, bytes.getHelper(instance));
	}

	@Override
	public final void iterateInstanceReferences(
		final ConcurrentSkipListSet<?> instance,
		final PersistenceFunction      iterator
	)
	{
		iterator.apply(instance.comparator());
		Persistence.iterateReferencesIterable(iterator, instance);
	}

	@Override
	public final void iterateLoadableReferences(final Binary bytes, final PersistenceObjectIdAcceptor iterator)
	{
		iterator.acceptObjectId(bytes.get_long(BINARY_OFFSET_COMPARATOR));
		bytes.iterateListElementReferences(BINARY_OFFSET_ELEMENTS, iterator);
	}
	
}

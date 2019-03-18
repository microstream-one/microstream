package one.microstream.java.util;

import java.util.IdentityHashMap;
import java.util.Map;

import one.microstream.X;
import one.microstream.chars.XChars;
import one.microstream.collections.old.JavaUtilMapEntrySetFlattener;
import one.microstream.persistence.binary.internal.AbstractBinaryHandlerCustomCollection;
import one.microstream.persistence.binary.types.Binary;
import one.microstream.persistence.binary.types.BinaryCollectionHandling;
import one.microstream.persistence.types.PersistenceFunction;
import one.microstream.persistence.types.PersistenceLoadHandler;
import one.microstream.persistence.types.PersistenceObjectIdAcceptor;
import one.microstream.persistence.types.PersistenceStoreHandler;


public final class BinaryHandlerIdentityHashMap extends AbstractBinaryHandlerCustomCollection<IdentityHashMap<?, ?>>
{
	///////////////////////////////////////////////////////////////////////////
	// constants        //
	/////////////////////

	static final long BINARY_OFFSET_ELEMENTS = 0;



	///////////////////////////////////////////////////////////////////////////
	// static methods    //
	/////////////////////

	@SuppressWarnings({"unchecked", "rawtypes"})
	private static Class<IdentityHashMap<?, ?>> typeWorkaround()
	{
		return (Class)IdentityHashMap.class; // no idea how to get ".class" to work otherwise
	}

	static final int getElementCount(final Binary bytes)
	{
		return X.checkArrayRange(bytes.getListElementCountReferences(BINARY_OFFSET_ELEMENTS));
	}



	///////////////////////////////////////////////////////////////////////////
	// constructors     //
	/////////////////////

	public BinaryHandlerIdentityHashMap()
	{
		super(
			typeWorkaround(),
			BinaryCollectionHandling.elementsPseudoFields()
		);
	}



	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////

	@Override
	public final void store(
		final Binary                  bytes   ,
		final IdentityHashMap<?, ?>   instance,
		final long                    oid     ,
		final PersistenceStoreHandler handler
	)
	{
		// store elements simply as array binary form
		bytes.storeSizedIterableAsList(
			this.typeId()          ,
			oid                    ,
			BINARY_OFFSET_ELEMENTS ,
			() ->
				JavaUtilMapEntrySetFlattener.New(instance),
			instance.size() * 2    ,
			handler
		);
	}
	
	@Override
	public final IdentityHashMap<?, ?> create(final Binary bytes)
	{
		return new IdentityHashMap<>(
			getElementCount(bytes) / 2
		);
	}

	@Override
	public final void update(final Binary bytes, final IdentityHashMap<?, ?> instance, final PersistenceLoadHandler builder)
	{
		final int elementCount = getElementCount(bytes);
		final Object[] elementsHelper = new Object[elementCount];
		
		bytes.collectElementsIntoArray(BINARY_OFFSET_ELEMENTS, builder, elementsHelper);
	
		bytes.registerHelper(instance, elementsHelper);
	}

	@Override
	public void complete(final Binary rawData, final IdentityHashMap<?, ?> instance, final PersistenceLoadHandler handler)
	{
		final Object helper = rawData.getHelper(instance);
		
		if(helper == null)
		{
			// (22.04.2016 TM)EXCP: proper exception
			throw new RuntimeException(
				"Missing element collection helper instance for " + XChars.systemString(instance)
			);
		}
		
		if(!(helper instanceof Object[]))
		{
			// (22.04.2016 TM)EXCP: proper exception
			throw new RuntimeException(
				"Inconsistent element collection helper instance for " + XChars.systemString(instance)
			);
		}
		
		final Object[] elementsHelper = (Object[])helper;
		@SuppressWarnings("unchecked")
		final IdentityHashMap<Object, Object> castedInstance = (IdentityHashMap<Object, Object>)instance;
		
		for(int i = 0; i < elementsHelper.length; i += 2)
		{
			if(castedInstance.putIfAbsent(elementsHelper[i], elementsHelper[i + 1]) != null)
			{
				// (22.04.2016 TM)EXCP: proper exception
				throw new RuntimeException(
					"Element hashing inconsistency in " + XChars.systemString(castedInstance)
				);
			}
		}
	}
	


	@Override
	public final void iterateInstanceReferences(final IdentityHashMap<?, ?> instance, final PersistenceFunction iterator)
	{
		for(final Map.Entry<?, ?> entry : instance.entrySet())
		{
			iterator.apply(entry.getKey());
			iterator.apply(entry.getValue());
		}
	}

	@Override
	public final void iteratePersistedReferences(final Binary bytes, final PersistenceObjectIdAcceptor iterator)
	{
		bytes.iterateListElementReferences(BINARY_OFFSET_ELEMENTS, iterator);
	}
}

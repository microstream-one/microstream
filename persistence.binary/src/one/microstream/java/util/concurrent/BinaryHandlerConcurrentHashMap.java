package one.microstream.persistence.binary.internal;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import one.microstream.X;
import one.microstream.chars.XChars;
import one.microstream.persistence.binary.types.Binary;
import one.microstream.persistence.binary.types.BinaryCollectionHandling;
import one.microstream.persistence.types.PersistenceFunction;
import one.microstream.persistence.types.PersistenceLoadHandler;
import one.microstream.persistence.types.PersistenceObjectIdAcceptor;
import one.microstream.persistence.types.PersistenceStoreHandler;


public final class BinaryHandlerConcurrentHashMap extends AbstractBinaryHandlerNativeCustomCollection<ConcurrentHashMap<?, ?>>
{
	///////////////////////////////////////////////////////////////////////////
	// constants        //
	/////////////////////

	static final long BINARY_OFFSET_ELEMENTS    = 0;



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
			BinaryCollectionHandling.elementsPseudoFields()
		);
	}



	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////

	@Override
	public final void store(
		final Binary                  bytes   ,
		final ConcurrentHashMap<?, ?>           instance,
		final long                    oid     ,
		final PersistenceStoreHandler handler
	)
	{
		// store elements simply as array binary form
		bytes.storeSizedIterableAsList(
			this.typeId()          ,
			oid                    ,
			BINARY_OFFSET_ELEMENTS ,
			keysAndValues(instance),
			instance.size() * 2    ,
			handler
		);
	}
	
	
	private final Iterable<Object> keysAndValues(ConcurrentHashMap<?, ?> map)
	{
		Iterator<?> iterator = map.entrySet().iterator();
		return () -> new Iterator<Object>() 
		{
			Map.Entry<?, ?> entry;
			
			@Override
			public boolean hasNext() 
			{
				return this.entry == null ? iterator.hasNext() : true;
			}
			
			@Override
			public Object next() 
			{
				if(this.entry == null)
				{
					this.entry = (Map.Entry<?, ?>)iterator.next();
					return this.entry.getKey();
				}
				else
				{
					Object value = entry.getValue();
					this.entry = null;
					return value;
				}
			}
		};
	}

	@Override
	public final ConcurrentHashMap<?, ?> create(final Binary bytes)
	{
		return new ConcurrentHashMap<>(
			getElementCount(bytes)
		);
	}

	@Override
	public final void update(final Binary bytes, final ConcurrentHashMap<?, ?> instance, final PersistenceLoadHandler builder)
	{
		final int elementCount = getElementCount(bytes);
		final Object[] elementsHelper = new Object[elementCount];
		
		bytes.collectElementsIntoArray(BINARY_OFFSET_ELEMENTS, builder, elementsHelper);
	
		bytes.registerHelper(instance, elementsHelper);
	}

	@Override
	public final void iterateInstanceReferences(final ConcurrentHashMap<?, ?> instance, final PersistenceFunction iterator)
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

	@Override
	public void complete(final Binary rawData, final ConcurrentHashMap<?, ?> instance, final PersistenceLoadHandler loadHandler)
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
		final ConcurrentHashMap<Object, Object> castedInstance = (ConcurrentHashMap<Object, Object>)instance;
		
		for(int i=0; i<elementsHelper.length; i+=2)
		{
			castedInstance.put(elementsHelper[i], elementsHelper[i+1]);
		}
	}
}

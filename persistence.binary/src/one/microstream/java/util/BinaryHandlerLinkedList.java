package one.microstream.java.util;

import java.util.LinkedList;

import one.microstream.X;
import one.microstream.chars.XChars;
import one.microstream.persistence.binary.internal.AbstractBinaryHandlerCustomCollection;
import one.microstream.persistence.binary.types.Binary;
import one.microstream.persistence.binary.types.BinaryCollectionHandling;
import one.microstream.persistence.types.PersistenceFunction;
import one.microstream.persistence.types.PersistenceLoadHandler;
import one.microstream.persistence.types.PersistenceObjectIdAcceptor;
import one.microstream.persistence.types.PersistenceStoreHandler;


public final class BinaryHandlerLinkedList extends AbstractBinaryHandlerCustomCollection<LinkedList<?>>
{
	///////////////////////////////////////////////////////////////////////////
	// constants        //
	/////////////////////

	static final long BINARY_OFFSET_ELEMENTS = 0;



	///////////////////////////////////////////////////////////////////////////
	// static methods    //
	/////////////////////

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
	// constructors     //
	/////////////////////

	public BinaryHandlerLinkedList()
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
		final LinkedList<?>           instance,
		final long                    oid     ,
		final PersistenceStoreHandler handler
	)
	{
		// (19.03.2019 TM)FIXME: MS-76: review LinkedList Handler
		
		// store elements simply as array binary form
		bytes.storeSizedIterableAsList(
			this.typeId()          ,
			oid                    ,
			BINARY_OFFSET_ELEMENTS ,
			instance               ,
			instance.size()        ,
			handler
		);
	}

	@Override
	public final LinkedList<?> create(final Binary bytes)
	{
		return new LinkedList<>();
	}

	@Override
	public final void update(final Binary rawData, final LinkedList<?> instance, final PersistenceLoadHandler handler)
	{
		final int      elementCount   = getElementCount(rawData);
		final Object[] elementsHelper = new Object[elementCount];
		
		rawData.collectElementsIntoArray(BINARY_OFFSET_ELEMENTS, handler, elementsHelper);
		rawData.registerHelper(instance, elementsHelper);
	}

	@Override
	public final void iterateInstanceReferences(final LinkedList<?> instance, final PersistenceFunction iterator)
	{
		for(final Object value : instance)
		{
			iterator.apply(value);
		}
	}

	@Override
	public final void iteratePersistedReferences(final Binary bytes, final PersistenceObjectIdAcceptor iterator)
	{
		bytes.iterateListElementReferences(BINARY_OFFSET_ELEMENTS, iterator);
	}

	@Override
	public void complete(final Binary rawData, final LinkedList<?> instance, final PersistenceLoadHandler handler)
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
		final LinkedList<Object> castedInstance = (LinkedList<Object>)instance;
		
		for(final Object element : elementsHelper)
		{
			castedInstance.add(element);
		}
	}
}

package one.microstream.java.util;

import java.util.Queue;

import one.microstream.X;
import one.microstream.persistence.binary.internal.AbstractBinaryHandlerCustomIterableSimpleListElements;
import one.microstream.persistence.binary.types.Binary;
import one.microstream.persistence.types.PersistenceLoadHandler;


public abstract class AbstractBinaryHandlerQueue<T extends Queue<?>>
extends AbstractBinaryHandlerCustomIterableSimpleListElements<T>
{
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public AbstractBinaryHandlerQueue(final Class<T> type)
	{
		super(type);
	}


	
	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////
	
	@Override
	protected long getElementCount(final T instance)
	{
		return instance.size();
	}

	@Override
	public void update(final Binary bytes, final T instance, final PersistenceLoadHandler handler)
	{
		// instance must be cleared in case an existing one is updated
		instance.clear();
		
		@SuppressWarnings("unchecked")
		final Queue<Object> castedInstance = (Queue<Object>)instance;
		
		bytes.collectObjectReferences(
			this.binaryOffsetElements(),
			X.checkArrayRange(getElementCount(bytes)),
			handler,
			e ->
				castedInstance.add(e)
		);
	}

}

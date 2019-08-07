package one.microstream.java.util;

import java.util.List;

import one.microstream.X;
import one.microstream.persistence.binary.internal.AbstractBinaryHandlerCustomIterableSimpleListElements;
import one.microstream.persistence.binary.types.Binary;
import one.microstream.persistence.types.PersistenceObjectIdResolver;


public abstract class AbstractBinaryHandlerList<T extends List<?>>
extends AbstractBinaryHandlerCustomIterableSimpleListElements<T>
{
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public AbstractBinaryHandlerList(final Class<T> type)
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
	public void update(final Binary bytes, final T instance, final PersistenceObjectIdResolver idResolver)
	{
		// instance must be cleared in case an existing one is updated
		instance.clear();
		
		@SuppressWarnings("unchecked")
		final List<Object> castedInstance = (List<Object>)instance;
		
		bytes.collectObjectReferences(
			this.binaryOffsetElements(),
			X.checkArrayRange(getElementCount(bytes)),
			idResolver,
			e ->
				castedInstance.add(e)
		);
	}
	
}

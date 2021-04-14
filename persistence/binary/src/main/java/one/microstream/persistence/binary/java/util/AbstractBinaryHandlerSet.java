package one.microstream.persistence.binary.java.util;

import java.util.Set;

import one.microstream.X;
import one.microstream.collections.old.OldCollections;
import one.microstream.persistence.binary.internal.AbstractBinaryHandlerCustomIterableSimpleListElements;
import one.microstream.persistence.binary.types.Binary;
import one.microstream.persistence.types.PersistenceLoadHandler;


public abstract class AbstractBinaryHandlerSet<T extends Set<?>>
extends AbstractBinaryHandlerCustomIterableSimpleListElements<T>
{
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public AbstractBinaryHandlerSet(final Class<T> type)
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
	public void updateState(final Binary data, final T instance, final PersistenceLoadHandler handler)
	{
		instance.clear();
		final Object[] elementsHelper = new Object[X.checkArrayRange(getElementCount(data))];
		data.collectElementsIntoArray(this.binaryOffsetElements(), handler, elementsHelper);
		data.registerHelper(instance, elementsHelper);
	}

	@Override
	public void complete(final Binary data, final T instance, final PersistenceLoadHandler handler)
	{
		OldCollections.populateCollectionFromHelperArray(instance, data.getHelper(instance));
	}
	
}

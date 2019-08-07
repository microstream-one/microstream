package one.microstream.java.util;

import java.util.Set;

import one.microstream.X;
import one.microstream.collections.old.OldCollections;
import one.microstream.persistence.binary.internal.AbstractBinaryHandlerCustomIterableSimpleListElements;
import one.microstream.persistence.binary.types.Binary;
import one.microstream.persistence.types.PersistenceObjectIdResolver;


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
	public void update(final Binary bytes, final T instance, final PersistenceObjectIdResolver idResolver)
	{
		instance.clear();
		final Object[] elementsHelper = new Object[X.checkArrayRange(getElementCount(bytes))];
		bytes.collectElementsIntoArray(this.binaryOffsetElements(), idResolver, elementsHelper);
		bytes.registerHelper(instance, elementsHelper);
	}

	@Override
	public void complete(final Binary bytes, final T instance, final PersistenceObjectIdResolver idResolver)
	{
		OldCollections.populateCollectionFromHelperArray(instance, bytes.getHelper(instance));
	}
	
}

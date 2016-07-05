package net.jadoth.persistence.types;

import net.jadoth.persistence.exceptions.PersistenceExceptionTypeNotPersistable;

public interface PersistenceTypeHandlerCreatorLookup<M>
{
	public PersistenceTypeHandlerCreator<M> lookupCreator(Class<?> type) throws PersistenceExceptionTypeNotPersistable;
}

package net.jadoth.persistence.types;

import net.jadoth.persistence.exceptions.PersistenceExceptionTypeNotPersistable;

public interface PersistenceTypeHandlerEnsurerLookup<M>
{
	public PersistenceTypeHandlerEnsurer<M> lookupEnsurer(Class<?> type) throws PersistenceExceptionTypeNotPersistable;
}

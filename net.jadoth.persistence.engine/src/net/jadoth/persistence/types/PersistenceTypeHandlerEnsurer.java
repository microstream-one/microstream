package net.jadoth.persistence.types;

import net.jadoth.persistence.exceptions.PersistenceExceptionTypeNotPersistable;

/**
 * Named "ensurer", because depending on the case, it creates a new type handler or it just initializes
 * already existing, pre-registered ones. So "ensuring" is the most fitting common denominator.
 * 
 * @author TM
 */
@FunctionalInterface
public interface PersistenceTypeHandlerEnsurer<M>
{
	public <T> PersistenceTypeHandler<M, T> ensureTypeHandler(Class<T> type)
		throws PersistenceExceptionTypeNotPersistable;
}

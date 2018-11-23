package net.jadoth.persistence.types;

import net.jadoth.persistence.exceptions.PersistenceExceptionTypeNotPersistable;



public interface PersistenceTypeHandlerProvider<M> extends PersistenceTypeManager, PersistenceTypeHandlerEnsurer<M>
{
	public <T> PersistenceTypeHandler<M, T> provideTypeHandler(Class<T> type) throws PersistenceExceptionTypeNotPersistable;

//	public PersistenceTypeHandler<M, ?> provideTypeHandler(long typeId);
	
	// must be able to act as a pure TypeHandlerEnsurer as well because of type refactoring type mismatch checks.
	@Override
	public <T> PersistenceTypeHandler<M, T> ensureTypeHandler(Class<T> type)
		throws PersistenceExceptionTypeNotPersistable;

}

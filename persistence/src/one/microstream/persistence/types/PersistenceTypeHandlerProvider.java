package one.microstream.persistence.types;

import one.microstream.persistence.exceptions.PersistenceExceptionTypeNotPersistable;



public interface PersistenceTypeHandlerProvider<D> extends PersistenceTypeManager, PersistenceTypeHandlerEnsurer<D>
{
	public <T> PersistenceTypeHandler<D, ? super T> provideTypeHandler(Class<T> type) throws PersistenceExceptionTypeNotPersistable;

//	public PersistenceTypeHandler<D, ?> provideTypeHandler(long typeId);
	
	// must be able to act as a pure TypeHandlerEnsurer as well because of type refactoring type mismatch checks.
	@Override
	public <T> PersistenceTypeHandler<D, ? super T> ensureTypeHandler(Class<T> type)
		throws PersistenceExceptionTypeNotPersistable;

}

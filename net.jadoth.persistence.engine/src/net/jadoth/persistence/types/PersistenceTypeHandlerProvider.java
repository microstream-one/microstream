package net.jadoth.persistence.types;

import net.jadoth.persistence.exceptions.PersistenceExceptionTypeNotPersistable;
import net.jadoth.swizzling.types.SwizzleTypeManager;



public interface PersistenceTypeHandlerProvider<M> extends SwizzleTypeManager, PersistenceTypeHandlerEnsurer<M>
{
	public <T> PersistenceTypeHandler<M, T> provideTypeHandler(Class<T> type) throws PersistenceExceptionTypeNotPersistable;

	public PersistenceTypeHandler<M, ?> provideTypeHandler(long typeId);
	
	// must be able to act as a pure TypeHandlerEnsurer as well because of type refactoring type mismatch checks.
	@Override
	public <T> PersistenceTypeHandler<M, T> ensureTypeHandler(Class<T> type)
		throws PersistenceExceptionTypeNotPersistable;

}

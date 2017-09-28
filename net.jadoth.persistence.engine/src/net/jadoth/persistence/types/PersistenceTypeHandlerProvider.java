package net.jadoth.persistence.types;

import net.jadoth.persistence.exceptions.PersistenceExceptionTypeNotPersistable;
import net.jadoth.swizzling.types.SwizzleTypeManager;



public interface PersistenceTypeHandlerProvider<M> extends PersistenceTypeHandlerEnsurer<M>, SwizzleTypeManager
{
	public <T> PersistenceTypeHandler<M, T> provideTypeHandler(
		PersistenceTypeHandlerManager<M> typeHandlerManager,
		Class<T>                         type
	) throws PersistenceExceptionTypeNotPersistable;

	public <T> PersistenceTypeHandler<M, T> provideTypeHandler(
		PersistenceTypeHandlerManager<M> typeHandlerManager,
		long                             typeId
	);
	
	
	// must be able to act as a pure TypeHandlerEnsurer as well because of type refactoring type mismatch checks.
	
	@Override
	public <T> PersistenceTypeHandler<M, T> ensureTypeHandler(Class<T> type)
		throws PersistenceExceptionTypeNotPersistable;

}

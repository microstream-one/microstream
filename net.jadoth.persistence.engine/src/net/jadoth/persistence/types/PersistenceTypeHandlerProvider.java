package net.jadoth.persistence.types;

import net.jadoth.persistence.exceptions.PersistenceExceptionTypeNotPersistable;
import net.jadoth.swizzling.types.SwizzleTypeManager;



public interface PersistenceTypeHandlerProvider<M> extends SwizzleTypeManager /*, PersistenceTypeHandlerEnsurer<M>*/
{
	public <T> PersistenceTypeHandler<M, T> provideTypeHandler(
		PersistenceTypeHandlerManager<M> typeHandlerManager,
		Class<T>                         type
	) throws PersistenceExceptionTypeNotPersistable;

	public <T> PersistenceTypeHandler<M, T> provideTypeHandler(
		PersistenceTypeHandlerManager<M> typeHandlerManager,
		long                             typeId
	);
	
	// (17.05.2018 TM)NOTE: not sure if this is still necessary with the new legacy type mapping concept.
//	// must be able to act as a pure TypeHandlerEnsurer as well because of type refactoring type mismatch checks.
//	@Override
//	public <T> PersistenceTypeHandler<M, T> ensureTypeHandler(Class<T> type)
//		throws PersistenceExceptionTypeNotPersistable;

}

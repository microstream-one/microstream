package net.jadoth.persistence.types;

import net.jadoth.persistence.exceptions.PersistenceExceptionTypeNotPersistable;
import net.jadoth.swizzling.types.SwizzleTypeManager;



public interface PersistenceTypeHandlerProvider<M> extends SwizzleTypeManager
{
	public <T> PersistenceTypeHandler<M, T> provideTypeHandler(
		PersistenceTypeHandlerManager<M> typeHandlerManager,
		Class<T> type
	) throws PersistenceExceptionTypeNotPersistable;

	public <T> PersistenceTypeHandler<M, T> provideTypeHandler(
		PersistenceTypeHandlerManager<M> typeHandlerManager,
		long typeId
	);

	@Deprecated
	public PersistenceTypeSovereignty typeSovereignty();

}

package net.jadoth.persistence.types;

import net.jadoth.persistence.exceptions.PersistenceExceptionTypeNotPersistable;
import net.jadoth.swizzling.types.SwizzleTypeManager;

public interface PersistenceTypeHandlerCreator<M>
{
	public <T> PersistenceTypeHandler<M, T> createTypeHandler(
		Class<T> type,
		long typeId,
		SwizzleTypeManager typeManager
	) throws PersistenceExceptionTypeNotPersistable;
}

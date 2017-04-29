package net.jadoth.persistence.types;

import net.jadoth.persistence.exceptions.PersistenceExceptionTypeNotPersistable;
import net.jadoth.swizzling.types.SwizzleTypeManager;

/**
 * Called "ensurer", because depending on the case, if creates new type handler or it just initializes
 * already existing, pre-registered ones. So "ensuring" is the most suited common denominator.
 * 
 * @author TM
 */
public interface PersistenceTypeHandlerEnsurer<M>
{
	public <T> PersistenceTypeHandler<M, T> ensureTypeHandler(
		Class<T>           type       ,
		long               typeId     ,
		SwizzleTypeManager typeManager
	) throws PersistenceExceptionTypeNotPersistable;
}

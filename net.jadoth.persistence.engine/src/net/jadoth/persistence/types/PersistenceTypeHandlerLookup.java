package net.jadoth.persistence.types;

import net.jadoth.swizzling.types.SwizzleTypeLookup;


public interface PersistenceTypeHandlerLookup<M> extends SwizzleTypeLookup
{
	public <T> PersistenceTypeHandler<M, T> lookupTypeHandler(T instance);

	public <T> PersistenceTypeHandler<M, T> lookupTypeHandler(Class<T> type);

	public PersistenceTypeHandler<M, ?> lookupTypeHandler(long typeId);

	public PersistenceTypeHandler<M, ?> lookupTypeHandler(long objectId, long typeId);

}

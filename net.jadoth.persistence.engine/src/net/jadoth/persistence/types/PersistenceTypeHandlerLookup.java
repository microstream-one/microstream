package net.jadoth.persistence.types;

import net.jadoth.memory.objectstate.ObjectStateHandlerLookup;
import net.jadoth.swizzling.types.SwizzleTypeLookup;


public interface PersistenceTypeHandlerLookup<M> extends ObjectStateHandlerLookup, SwizzleTypeLookup
{
	@Override
	public <T> PersistenceTypeHandler<M, T> lookupTypeHandler(T instance);

	@Override
	public <T> PersistenceTypeHandler<M, T> lookupTypeHandler(Class<T> type);

	public <T> PersistenceTypeHandler<M, T> lookupTypeHandler(long typeId);

	public <T> PersistenceTypeHandler<M, T> lookupTypeHandler(long objectId, long typeId);

}

package net.jadoth.persistence.types;

public interface PersistenceTypeHandlerLookup<M> extends PersistenceTypeLookup
{
	public <T> PersistenceTypeHandler<M, T> lookupTypeHandler(T instance);

	public <T> PersistenceTypeHandler<M, T> lookupTypeHandler(Class<T> type);

	public <T> PersistenceTypeHandler<M, T> lookupTypeHandler(long typeId);

	public <T> PersistenceTypeHandler<M, T> lookupTypeHandler(long objectId, long typeId);

}

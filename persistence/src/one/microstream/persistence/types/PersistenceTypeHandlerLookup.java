package one.microstream.persistence.types;

public interface PersistenceTypeHandlerLookup<D> extends PersistenceTypeLookup
{
	public <T> PersistenceTypeHandler<D, T> lookupTypeHandler(T instance);

	public <T> PersistenceTypeHandler<D, T> lookupTypeHandler(Class<T> type);

	public PersistenceTypeHandler<D, ?> lookupTypeHandler(long typeId);

}

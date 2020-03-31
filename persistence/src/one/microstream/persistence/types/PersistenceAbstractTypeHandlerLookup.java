package one.microstream.persistence.types;

// (31.03.2020 Paigan)FIXME: priv#187: use or delete
public interface PersistenceAbstractTypeHandlerLookup<D>
{
	public <T> PersistenceTypeHandler<D, T> lookupAbstractTypeHandler(Class<T> type);
}

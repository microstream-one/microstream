package net.jadoth.persistence.types;

public interface PersistenceSourceSupplier<M> extends PersistenceObjectRetriever
{
	@Override
	public Object getObject(long oid);

	public PersistenceSource<M> source();
}

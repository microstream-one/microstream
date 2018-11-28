package net.jadoth.persistence.types;

public interface PersistenceSourceSupplier<M> extends PersistenceObjectSupplier
{
	@Override
	public Object get(long oid);

	public PersistenceSource<M> source();
}

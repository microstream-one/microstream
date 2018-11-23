package net.jadoth.persistence.types;

public interface PersistenceSwizzleSupplier<M> extends PersistenceObjectSupplier
{
	@Override
	public Object get(long oid);

	public PersistenceSource<M> source();
}

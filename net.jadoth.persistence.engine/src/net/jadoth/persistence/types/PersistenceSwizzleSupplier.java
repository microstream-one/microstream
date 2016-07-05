package net.jadoth.persistence.types;

import net.jadoth.swizzling.types.SwizzleObjectSupplier;

public interface PersistenceSwizzleSupplier<M> extends SwizzleObjectSupplier
{
	@Override
	public Object get(long oid);

	public PersistenceSource<M> source();
}

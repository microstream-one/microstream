package one.microstream.persistence.types;

import one.microstream.reference.ObjectSwizzling;

public interface PersistenceSourceSupplier<D> extends ObjectSwizzling
{
	@Override
	public Object getObject(long objectId);

	public PersistenceSource<D> source();
}

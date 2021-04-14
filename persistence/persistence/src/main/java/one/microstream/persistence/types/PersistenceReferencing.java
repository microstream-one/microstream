package one.microstream.persistence.types;

import one.microstream.reference.Referencing;

public interface PersistenceReferencing<T> extends Referencing<T>
{
	@Override
	public T get();

	public long objectId();

}

package net.jadoth.persistence.types;

import net.jadoth.reference.Referencing;

public interface PersistenceReferencing<T> extends Referencing<T>
{
	@Override
	public T get();

	public long oid();

}

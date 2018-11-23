package net.jadoth.persistence.binary.types;

import net.jadoth.persistence.types.PersistenceObjectIdResolving;

public interface BinaryValueEqualator
{
	public boolean equalValue(Object src, long srcOffset, long address, PersistenceObjectIdResolving oidResolver);
}

package net.jadoth.persistence.binary.types;

import net.jadoth.persistence.types.PersistenceObjectIdResolver;

public interface BinaryValueEqualator
{
	public boolean equalValue(Object src, long srcOffset, long address, PersistenceObjectIdResolver oidResolver);
}

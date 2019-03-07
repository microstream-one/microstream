package one.microstream.persistence.binary.types;

import one.microstream.persistence.types.PersistenceObjectIdResolver;

public interface BinaryValueEqualator
{
	public boolean equalValue(Object src, long srcOffset, long address, PersistenceObjectIdResolver oidResolver);
}

package net.jadoth.persistence.binary.types;

import net.jadoth.swizzling.types.PersistenceStoreFunction;

public interface BinaryValueStorer
{
	public long storeValueFromMemory(Object src, long srcOffset, long address, PersistenceStoreFunction persister);
}

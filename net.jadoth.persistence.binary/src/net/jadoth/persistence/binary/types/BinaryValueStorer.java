package net.jadoth.persistence.binary.types;

import net.jadoth.swizzling.types.PersistenceStoreFunction;

public interface BinaryValueStorer
{
	public long storeValueFromMemory(Object source, long sourceOffset, long targetAddress, PersistenceStoreFunction persister);
}

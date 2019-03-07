package net.jadoth.persistence.binary.types;

import net.jadoth.persistence.types.PersistenceStoreHandler;

public interface BinaryValueStorer
{
	public long storeValueFromMemory(Object source, long sourceOffset, long targetAddress, PersistenceStoreHandler persister);
}

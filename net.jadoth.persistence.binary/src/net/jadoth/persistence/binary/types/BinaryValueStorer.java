package net.jadoth.persistence.binary.types;

import net.jadoth.persistence.types.PersistenceHandler;

public interface BinaryValueStorer
{
	public long storeValueFromMemory(Object source, long sourceOffset, long targetAddress, PersistenceHandler persister);
}

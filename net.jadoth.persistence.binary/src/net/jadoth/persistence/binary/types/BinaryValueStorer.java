package net.jadoth.persistence.binary.types;

import net.jadoth.swizzling.types.SwizzleHandler;

public interface BinaryValueStorer
{
	public long storeValueFromMemory(Object source, long sourceOffset, long targetAddress, SwizzleHandler persister);
}

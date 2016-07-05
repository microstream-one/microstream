package net.jadoth.persistence.binary.types;

import net.jadoth.swizzling.types.SwizzleObjectIdResolving;


public interface BinaryValueSetter
{
	public long setValueToMemory(long address, Object dst, long dstOffset, SwizzleObjectIdResolving idResolver);
}

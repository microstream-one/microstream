package net.jadoth.persistence.binary.types;

import net.jadoth.swizzling.types.SwizzleObjectIdResolving;

public interface BinaryValueEqualator
{
	public boolean equalValue(Object src, long srcOffset, long address, SwizzleObjectIdResolving oidResolver);
}

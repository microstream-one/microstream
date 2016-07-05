package net.jadoth.swizzling.types;

import net.jadoth.swizzling.exceptions.SwizzleExceptionConsistency;

public interface SwizzleTypeRegistry extends SwizzleTypeLookup
{
	public boolean registerType(long tid, Class<?> type) throws SwizzleExceptionConsistency;

}

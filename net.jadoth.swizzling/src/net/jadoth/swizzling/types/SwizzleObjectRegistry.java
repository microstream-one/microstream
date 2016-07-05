package net.jadoth.swizzling.types;

public interface SwizzleObjectRegistry extends SwizzleObjectLookup
{
	public Object optionalRegisterObject(long oid, long tid, Object object);

	public boolean registerObject(long oid, final long tid, Object object);
}

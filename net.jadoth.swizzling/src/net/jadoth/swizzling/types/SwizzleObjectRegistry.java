package net.jadoth.swizzling.types;

public interface SwizzleObjectRegistry extends SwizzleObjectLookup
{
	public Object optionalRegisterObject(long oid, Object object);

	public boolean registerObject(long oid, Object object);
}

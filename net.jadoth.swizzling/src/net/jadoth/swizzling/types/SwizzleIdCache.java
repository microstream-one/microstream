package net.jadoth.swizzling.types;


public interface SwizzleIdCache
{
	public long lookupTypeIdForObjectId(long oid);

	public Object registerTypeIdForObjectId(long oid, long tid);

}

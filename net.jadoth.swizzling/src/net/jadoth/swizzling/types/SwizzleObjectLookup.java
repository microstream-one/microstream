package net.jadoth.swizzling.types;

public interface SwizzleObjectLookup extends SwizzleObjectIdResolving
{
	public long lookupObjectId(Object object);
}

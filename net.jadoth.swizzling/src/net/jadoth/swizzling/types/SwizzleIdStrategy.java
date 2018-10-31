package net.jadoth.swizzling.types;

public interface SwizzleIdStrategy
{
	public SwizzleObjectIdProvider createObjectIdProvider();
	
	public SwizzleTypeIdProvider createTypeIdProvider();
}

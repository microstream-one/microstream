package net.jadoth.swizzling.types;


public interface SwizzleTypeDictionary
{
	public SwizzleTypeIdentity lookupTypeByName(String typeName);

	public SwizzleTypeIdentity lookupTypeById(long typeId);

}

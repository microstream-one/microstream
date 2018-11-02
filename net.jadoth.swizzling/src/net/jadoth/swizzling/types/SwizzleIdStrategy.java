package net.jadoth.swizzling.types;

public interface SwizzleIdStrategy extends SwizzleObjectIdStrategy, SwizzleTypeIdStrategy
{
	public default SwizzleObjectIdStrategy objectIdStragegy()
	{
		return this;
	}
	
	public default SwizzleTypeIdStrategy typeIdStragegy()
	{
		return this;
	}
	
	@Override
	public default SwizzleObjectIdProvider createObjectIdProvider()
	{
		return this.objectIdStragegy().createObjectIdProvider();
	}
	
	@Override
	public default SwizzleTypeIdProvider createTypeIdProvider()
	{
		return this.typeIdStragegy().createTypeIdProvider();
	}
}

package net.jadoth.swizzling.types;


public interface SwizzleTypeIdProvider extends SwizzleTypeIdHolder
{
	public long provideNextTypeId();

	public SwizzleTypeIdProvider initializeTypeId();

	public SwizzleTypeIdProvider updateCurrentTypeId(long currentTypeId);
}

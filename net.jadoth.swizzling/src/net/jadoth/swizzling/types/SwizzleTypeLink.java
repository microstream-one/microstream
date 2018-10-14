package net.jadoth.swizzling.types;

public interface SwizzleTypeLink extends SwizzleTypeIdOwner
{
	@Override
	public long     typeId();

	public Class<?> type();

}

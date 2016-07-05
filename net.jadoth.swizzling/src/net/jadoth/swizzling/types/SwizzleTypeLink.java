package net.jadoth.swizzling.types;

public interface SwizzleTypeLink<T> extends SwizzleTypeIdOwner
{
	@Override
	public long     typeId();

	public Class<T> type();

}

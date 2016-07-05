package net.jadoth.swizzling.types;

import net.jadoth.reference.Referencing;

public interface SwizzleReferencing<T> extends Referencing<T>
{
	@Override
	public T get();

	public long oid();

}

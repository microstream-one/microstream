package net.jadoth.persistence.binary.types;

import java.nio.ByteOrder;

public interface BinaryByteOrderTargeting<T extends BinaryByteOrderTargeting<?>>
{
	public ByteOrder getTargetByteOrder();
	
	public default boolean isByteOrderMismatch()
	{
		return BinaryPersistence.isByteOrderMismatch(this.getTargetByteOrder());
	}

	public T setTargetByteOrder(ByteOrder targetByteOrder);
	
}

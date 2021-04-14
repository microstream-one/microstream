package one.microstream.persistence.types;

import java.nio.ByteOrder;

@FunctionalInterface
public interface ByteOrderTargeting<T extends ByteOrderTargeting<?>>
{
	public ByteOrder getTargetByteOrder();
	
	public default boolean isByteOrderMismatch()
	{
		return isByteOrderMismatch(this.getTargetByteOrder());
	}
	

	
	public static boolean isByteOrderMismatch(final ByteOrder targetByteOrder)
	{
		return targetByteOrder != ByteOrder.nativeOrder();
	}
	
	
	public interface Mutable<T extends ByteOrderTargeting.Mutable<?>> extends ByteOrderTargeting<T>
	{
		public T setTargetByteOrder(ByteOrder targetByteOrder);
	}
	
}

package net.jadoth.persistence.binary.types;

import net.jadoth.low.XMemory;

/* (13.12.2018 TM)TODO: test if a direct class is faster than an interface.
 * This is a low-level operation that doesn't need to have total multiple inheritence etc.
 */
public interface BinaryValueAccessor
{
	public default byte get_byte(final long address)
	{
		return XMemory.get_byte(address);
	}
	
	public default boolean get_boolean(final long address)
	{
		return XMemory.get_boolean(address);
	}
	
	public default short get_short(final long address)
	{
		return XMemory.get_short(address);
	}
	
	public default char get_char(final long address)
	{
		return XMemory.get_char(address);
	}
	
	public default int get_int(final long address)
	{
		return XMemory.get_int(address);
	}
	
	public default float get_float(final long address)
	{
		return XMemory.get_float(address);
	}
	
	public default long get_long(final long address)
	{
		return XMemory.get_long(address);
	}
	
	public default double get_double(final long address)
	{
		return XMemory.get_double(address);
	}
	
	
	
	public default void set_byte(final long address, final byte value)
	{
		XMemory.set_byte(address, value);
	}
	
	public default void set_boolean(final long address, final boolean value)
	{
		XMemory.set_boolean(address, value);
	}
	
	public default void set_short(final long address, final short value)
	{
		XMemory.set_short(address, value);
	}
	
	public default void set_char(final long address, final char value)
	{
		XMemory.set_char(address, value);
	}
	
	public default void set_int(final long address, final int value)
	{
		XMemory.set_int(address, value);
	}
	
	public default void set_float(final long address, final float value)
	{
		XMemory.set_float(address, value);
	}
	
	public default void set_long(final long address, final long value)
	{
		XMemory.set_long(address, value);
	}
	
	public default void set_double(final long address, final double value)
	{
		XMemory.set_double(address, value);
	}
	
	
	
	public final class Implementation implements BinaryValueAccessor
	{
		// stateless interface instantiation is missing
	}
	
}



package net.jadoth.memory;

import java.nio.ByteOrder;

// (05.02.2019 TM)FIXME: JET-49: Delete RawValueHandler
/* (13.12.2018 TM)TODO: test if a direct class is faster than an interface.
 * This is a low-level operation utility that doesn't need to have totally flexible multiple inheritence etc.
 */
public interface RawValueHandler
{
	public static RawValueHandler Derive(final ByteOrder targetByteOrder)
	{
		return Derive(ByteOrder.nativeOrder(), targetByteOrder);
	}
	
	public static RawValueHandler Derive(final ByteOrder nativeByteOrder, final ByteOrder targetByteOrder)
	{
		/*
		 * It is currently assumed that there are only two occuring situations:
		 * 1.) Native and target byte orders are the same (e.g. LE/LE or BE/BE), making direct handling sufficient.
		 * 2.) Native and target byte orders are either LE/BE or BE/LE. Both can be handled by swapping the bytes.
		 * Anything else, like "mixed endian" / "middle endian", or whatever other moronities morons out there
		 * can come up with, are ignored intentionally.
		 */
		if(targetByteOrder == nativeByteOrder)
		{
			return RawValueHandler.Direct();
		}
		if(nativeByteOrder == ByteOrder.BIG_ENDIAN && targetByteOrder == ByteOrder.BIG_ENDIAN)
		{
			return RawValueHandler.Swapping();
		}
		if(nativeByteOrder == ByteOrder.LITTLE_ENDIAN && targetByteOrder == ByteOrder.LITTLE_ENDIAN)
		{
			return RawValueHandler.Swapping();
		}
		
		throw new Error("Byte order moronity encountered.");
	}
	
	
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
	
	
	
	public static RawValueHandler.Direct Direct()
	{
		return new RawValueHandler.Direct();
	}
	
	public final class Direct implements RawValueHandler
	{
		Direct()
		{
			super();
		}
	}
	
	public static RawValueHandler.Swapping Swapping()
	{
		return new RawValueHandler.Swapping();
	}
	
	public final class Swapping implements RawValueHandler
	{
		Swapping()
		{
			super();
			// FIXME BinaryValueAccessor.Swapping
			throw new net.jadoth.meta.NotImplementedYetError();
		}
		
	}
	
}



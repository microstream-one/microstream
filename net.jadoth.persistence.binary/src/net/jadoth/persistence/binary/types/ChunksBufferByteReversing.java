package net.jadoth.persistence.binary.types;

import static net.jadoth.X.notNull;

import net.jadoth.memory.XMemory;
import net.jadoth.util.BufferSizeProviderIncremental;

public class ChunksBufferByteReversing extends ChunksBuffer
{
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////
	
	public static final ChunksBufferByteReversing New(
		final ChunksBuffer[]                channelBuffers    ,
		final BufferSizeProviderIncremental bufferSizeProvider
	)
	{
		return new ChunksBufferByteReversing(
			notNull(channelBuffers),
			notNull(bufferSizeProvider)
		);
	}
	
	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////
	
	ChunksBufferByteReversing(
		final ChunksBuffer[]                channelBuffers    ,
		final BufferSizeProviderIncremental bufferSizeProvider
	)
	{
		super(
			channelBuffers    ,
			bufferSizeProvider
		);
	}
	
	

	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////
	
	@Override
	public final boolean isSwitchedByteOrder()
	{
		return true;
	}

	@Override
	final short read_short(final long address)
	{
		return Short.reverseBytes(XMemory.get_short(address));
	}

	@Override
	final char read_char(final long address)
	{
		return Character.reverseBytes(XMemory.get_char(address));
	}

	@Override
	final int read_int(final long address)
	{
		return Integer.reverseBytes(XMemory.get_int(address));
	}

	@Override
	final float read_float(final long address)
	{
		// this is tricky
		return Float.intBitsToFloat(Integer.reverseBytes(XMemory.get_int(address)));
	}

	@Override
	final long read_long(final long address)
	{
		return Long.reverseBytes(XMemory.get_long(address));
	}

	@Override
	final double read_double(final long address)
	{
		// this is tricky
		return Double.longBitsToDouble(Long.reverseBytes(XMemory.get_long(address)));
	}

	@Override
	public final void store_short(final long address, final short value)
	{
		XMemory.set_short(address, Short.reverseBytes(value));
	}

	@Override
	public final void store_char(final long address, final char value)
	{
		XMemory.set_char(address, Character.reverseBytes(value));
	}

	@Override
	public final void store_int(final long address, final int value)
	{
		XMemory.set_int(address, Integer.reverseBytes(value));
	}

	@Override
	public final void store_float(final long address, final float value)
	{
		XMemory.set_int(address, Integer.reverseBytes(Float.floatToRawIntBits(value)));
	}

	@Override
	public final void store_long(final long address, final long value)
	{
		XMemory.set_long(address, Long.reverseBytes(value));
	}

	@Override
	public final void store_double(final long address, final double value)
	{
		XMemory.set_long(address, Long.reverseBytes(Double.doubleToRawLongBits(value)));
	}

	@Override
	final void read_shorts(final long address, final short[] target)
	{
		for(int i = 0; i < target.length; i++)
		{
			target[i] = this.read_short(address + i * Short.BYTES);
		}
	}

	@Override
	final void read_chars(final long address, final char[] target)
	{
		for(int i = 0; i < target.length; i++)
		{
			target[i] = this.read_char(address + i * Character.BYTES);
		}
	}

	@Override
	final void read_chars(final long address, final char[] target, final int offset, final int length)
	{
		final int bound = offset + length;
		for(int i = offset; i < bound; i++)
		{
			target[i] = this.read_char(address + (i - offset) * Character.BYTES);
		}
	}

	@Override
	final void read_ints(final long address, final int[] target)
	{
		for(int i = 0; i < target.length; i++)
		{
			target[i] = this.read_int(address + i * Integer.BYTES);
		}
	}

	@Override
	final void read_floats(final long address, final float[] target)
	{
		for(int i = 0; i < target.length; i++)
		{
			target[i] = this.read_float(address + i * Float.BYTES);
		}
	}

	@Override
	public final void read_longs(final long address, final long[] target)
	{
		for(int i = 0; i < target.length; i++)
		{
			target[i] = this.read_long(address + i * Long.BYTES);
		}
	}

	@Override
	final void read_doubles(final long address, final double[] target)
	{
		for(int i = 0; i < target.length; i++)
		{
			target[i] = this.read_double(address + i * Double.BYTES);
		}
	}
	
	@Override
	final void store_shorts(final long address, final short[] values)
	{
		for(int i = 0; i < values.length; i++)
		{
			this.store_short(address + i * Short.BYTES, values[i]);
		}
	}
	
	@Override
	final void store_chars(final long address, final char[] values)
	{
		for(int i = 0; i < values.length; i++)
		{
			this.store_char(address + i * Character.BYTES, values[i]);
		}
	}
	
	@Override
	final void store_chars(final long address, final char[] values, final int offset, final int length)
	{
		final int bound = offset + length;
		for(int i = offset; i < bound; i++)
		{
			this.store_char(address + (i - offset) * Character.BYTES, values[i]);
		}
	}
	
	@Override
	final void store_ints(final long address, final int[] values)
	{
		for(int i = 0; i < values.length; i++)
		{
			this.store_int(address + i * Integer.BYTES, values[i]);
		}
	}
	
	@Override
	final void store_floats(final long address, final float[] values)
	{
		for(int i = 0; i < values.length; i++)
		{
			this.store_float(address + i * Float.BYTES, values[i]);
		}
	}
	
	@Override
	final void store_longs(final long address, final long[] values)
	{
		for(int i = 0; i < values.length; i++)
		{
			this.store_long(address + i * Long.BYTES, values[i]);
		}
	}
	
	@Override
	final void store_doubles(final long address, final double[] values)
	{
		for(int i = 0; i < values.length; i++)
		{
			this.store_double(address + i * Double.BYTES, values[i]);
		}
	}
	
}

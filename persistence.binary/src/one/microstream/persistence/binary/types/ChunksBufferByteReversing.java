package one.microstream.persistence.binary.types;

import static one.microstream.X.notNull;

import one.microstream.memory.XMemory;
import one.microstream.util.BufferSizeProviderIncremental;

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
	final short internalRead_short(final long address)
	{
		return Short.reverseBytes(XMemory.get_short(address));
	}

	@Override
	final char internalRead_char(final long address)
	{
		return Character.reverseBytes(XMemory.get_char(address));
	}

	@Override
	final int internalRead_int(final long address)
	{
		return Integer.reverseBytes(XMemory.get_int(address));
	}

	@Override
	final float internalRead_float(final long address)
	{
		// this is tricky
		return Float.intBitsToFloat(Integer.reverseBytes(XMemory.get_int(address)));
	}

	@Override
	final long internalRead_long(final long address)
	{
		return Long.reverseBytes(XMemory.get_long(address));
	}

	@Override
	final double internalRead_double(final long address)
	{
		// this is tricky
		return Double.longBitsToDouble(Long.reverseBytes(XMemory.get_long(address)));
	}

	@Override
	final void internalStore_short(final long address, final short value)
	{
		XMemory.set_short(address, Short.reverseBytes(value));
	}
	
	@Override
	final void internalStore_char(final long address, final char value)
	{
		XMemory.set_char(address, Character.reverseBytes(value));
	}
	
	@Override
	final void internalStore_int(final long address, final int value)
	{
		XMemory.set_int(address, Integer.reverseBytes(value));
	}
	
	@Override
	final void internalStore_float(final long address, final float value)
	{
		XMemory.set_int(address, Integer.reverseBytes(Float.floatToRawIntBits(value)));
	}
	
	@Override
	final void internalStore_long(final long address, final long value)
	{
		XMemory.set_long(address, Long.reverseBytes(value));
	}
	
	@Override
	final void internalStore_double(final long address, final double value)
	{
		XMemory.set_long(address, Long.reverseBytes(Double.doubleToRawLongBits(value)));
	}

	@Override
	final void internalRead_shorts(final long address, final short[] target)
	{
		for(int i = 0; i < target.length; i++)
		{
			target[i] = this.internalRead_short(address + i * Short.BYTES);
		}
	}

	@Override
	final void internalRead_chars(final long address, final char[] target)
	{
		for(int i = 0; i < target.length; i++)
		{
			target[i] = this.internalRead_char(address + i * Character.BYTES);
		}
	}

	@Override
	final void internalRead_chars(final long address, final char[] target, final int offset, final int length)
	{
		final int bound = offset + length;
		for(int i = offset; i < bound; i++)
		{
			target[i] = this.internalRead_char(address + (i - offset) * Character.BYTES);
		}
	}

	@Override
	final void internalRead_ints(final long address, final int[] target)
	{
		for(int i = 0; i < target.length; i++)
		{
			target[i] = this.internalRead_int(address + i * Integer.BYTES);
		}
	}

	@Override
	final void internalRead_floats(final long address, final float[] target)
	{
		for(int i = 0; i < target.length; i++)
		{
			target[i] = this.internalRead_float(address + i * Float.BYTES);
		}
	}

	@Override
	public final void internalRead_longs(final long address, final long[] target)
	{
		for(int i = 0; i < target.length; i++)
		{
			target[i] = this.internalRead_long(address + i * Long.BYTES);
		}
	}

	@Override
	final void internalRead_doubles(final long address, final double[] target)
	{
		for(int i = 0; i < target.length; i++)
		{
			target[i] = this.internalRead_double(address + i * Double.BYTES);
		}
	}
	
	@Override
	final void internalStore_shorts(final long address, final short[] values)
	{
		for(int i = 0; i < values.length; i++)
		{
			this.internalStore_short(address + i * Short.BYTES, values[i]);
		}
	}
	
	@Override
	final void internalStore_chars(final long address, final char[] values)
	{
		for(int i = 0; i < values.length; i++)
		{
			this.internalStore_char(address + i * Character.BYTES, values[i]);
		}
	}
	
	@Override
	final void internalStore_chars(final long address, final char[] values, final int offset, final int length)
	{
		final int bound = offset + length;
		for(int i = offset; i < bound; i++)
		{
			this.internalStore_char(address + (i - offset) * Character.BYTES, values[i]);
		}
	}
	
	@Override
	final void internalStore_ints(final long address, final int[] values)
	{
		for(int i = 0; i < values.length; i++)
		{
			this.internalStore_int(address + i * Integer.BYTES, values[i]);
		}
	}
	
	@Override
	final void internalStore_floats(final long address, final float[] values)
	{
		for(int i = 0; i < values.length; i++)
		{
			this.internalStore_float(address + i * Float.BYTES, values[i]);
		}
	}
	
	@Override
	final void internalStore_longs(final long address, final long[] values)
	{
		for(int i = 0; i < values.length; i++)
		{
			this.internalStore_long(address + i * Long.BYTES, values[i]);
		}
	}
	
	@Override
	final void internalStore_doubles(final long address, final double[] values)
	{
		for(int i = 0; i < values.length; i++)
		{
			this.internalStore_double(address + i * Double.BYTES, values[i]);
		}
	}
	
	@Override
	protected final void internalStoreEntityHeader(
		final long entityAddress    ,
		final long entityTotalLength,
		final long entityTypeId     ,
		final long entityObjectId
	)
	{
		setEntityHeaderRawValues(
			entityAddress,
			Long.reverseBytes(entityTotalLength),
			Long.reverseBytes(entityTypeId),
			Long.reverseBytes(entityObjectId)
		);
	}
	
}

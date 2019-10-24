package one.microstream.memory;

import java.lang.reflect.Field;

import one.microstream.exceptions.InstantiationRuntimeException;

public class MemoryAccessorReversing implements MemoryAccessor
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////
	
	private final MemoryAccessor actual;
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	MemoryAccessorReversing(final MemoryAccessor actual)
	{
		super();
		this.actual = actual;
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////
	
	// memory allocation //
	
	@Override
	public final long allocateMemory(final long bytes)
	{
		return this.actual.allocateMemory(bytes);
	}

	@Override
	public final long reallocateMemory(final long address, final long bytes)
	{
		return this.actual.reallocateMemory(address, bytes);
	}

	@Override
	public final void freeMemory(final long address)
	{
		this.actual.freeMemory(address);
	}
	
	@Override
	public final void fillMemory(final long address, final long length, final byte value)
	{
		this.actual.fillMemory(address, length, value);
	}
	
	
	
	// memory size querying logic //
	
	/**
	 * Returns the system's memory "page size" (whatever that may be exactely for a given system).
	 * Use with care (and the dependency to a system value in mind!).
	 * 
	 * @return the system's memory "page size".
	 */
	@Override
	public final int pageSize()
	{
		return this.actual.pageSize();
	}
		
	@Override
	public final int byteSizeReference()
	{
		return this.actual.byteSizeReference();
	}
	
	@Override
	public final int byteSizeInstance(final Class<?> type)
	{
		return this.actual.byteSizeInstance(type);
	}
	
	@Override
	public final int byteSizeObjectHeader(final Class<?> type)
	{
		return this.actual.byteSizeObjectHeader(type);
	}
	
	@Override
	public final int byteSizeFieldValue(final Class<?> type)
	{
		return this.actual.byteSizeFieldValue(type);
	}
	
	
	
	// field offset abstraction //
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public final long objectFieldOffset(final Field field)
	{
		return this.actual.objectFieldOffset(field);
	}

	
	
	// compare and swap logic //
	
	@Override
	public final boolean compareAndSwap_int(
		final Object subject    ,
		final long   offset     ,
		final int    expected   ,
		final int    replacement
	)
	{
		return this.actual.compareAndSwap_int(
			subject,
			offset,
			Integer.reverseBytes(expected),
			Integer.reverseBytes(replacement)
		);
	}

	@Override
	public final boolean compareAndSwap_long(
		final Object subject    ,
		final long   offset     ,
		final long   expected   ,
		final long   replacement
	)
	{
		return this.actual.compareAndSwap_long(
			subject,
			offset,
			Long.reverseBytes(expected),
			Long.reverseBytes(replacement)
		);
	}

	@Override
	public final boolean compareAndSwapObject(
		final Object subject    ,
		final long   offset     ,
		final Object expected   ,
		final Object replacement
	)
	{
		// pointers may never be byte-reversed
		return this.actual.compareAndSwapObject(subject, offset, expected, replacement);
	}
	
	
	
	// address-based getters for primitive values and references //
	
	@Override
	public final byte get_byte(final long address)
	{
		// single-byte values can be just passed through, of course.
		return this.actual.get_byte(address);
	}

	@Override
	public final boolean get_boolean(final long address)
	{
		// single-byte values can be just passed through, of course.
		return this.actual.get_boolean(address);
	}

	@Override
	public final short get_short(final long address)
	{
		return Short.reverseBytes(this.actual.get_short(address));
	}

	@Override
	public final char get_char(final long address)
	{
		return Character.reverseBytes(this.actual.get_char(address));
	}

	@Override
	public final int get_int(final long address)
	{
		return Integer.reverseBytes(this.actual.get_int(address));
	}

	@Override
	public final float get_float(final long address)
	{
		// tricky: must read the reversed bytes as an int, reverse them to form a valid float and then transform it.
		return Float.intBitsToFloat(this.get_int(address));
	}

	@Override
	public final long get_long(final long address)
	{
		return Long.reverseBytes(this.actual.get_long(address));
	}

	@Override
	public final double get_double(final long address)
	{
		return Double.longBitsToDouble(this.get_long(address));
	}

	@Override
	public final Object getObject(final long address)
	{
		// pointers may never be byte-reversed
		return this.actual.getObject(address);
	}
	
	
	
	// object-based getters for primitive values and references //
	
	@Override
	public final byte get_byte(final Object instance, final long offset)
	{
		// single-byte values can be just passed through, of course.
		return this.actual.get_byte(instance, offset);
	}

	@Override
	public final boolean get_boolean(final Object instance, final long offset)
	{
		// single-byte values can be just passed through, of course.
		return this.actual.get_boolean(instance, offset);
	}

	@Override
	public final short get_short(final Object instance, final long offset)
	{
		return Short.reverseBytes(this.actual.get_short(instance, offset));
	}

	@Override
	public final char get_char(final Object instance, final long offset)
	{
		return Character.reverseBytes(this.actual.get_char(instance, offset));
	}

	@Override
	public final int get_int(final Object instance, final long offset)
	{
		return Integer.reverseBytes(this.actual.get_int(instance, offset));
	}

	@Override
	public final float get_float(final Object instance, final long offset)
	{
		// tricky: must read the reversed bytes as an int, reverse them to form a valid float and then transform it.
		return Float.intBitsToFloat(this.get_int(instance, offset));
	}

	@Override
	public final long get_long(final Object instance, final long offset)
	{
		return Long.reverseBytes(this.actual.get_long(instance, offset));
	}

	@Override
	public final double get_double(final Object instance, final long offset)
	{
		return Double.longBitsToDouble(this.get_long(instance, offset));
	}

	@Override
	public final Object getObject(final Object instance, final long offset)
	{
		// pointers may never be byte-reversed
		return this.actual.getObject(instance, offset);
	}
	
	
	
	// address-based setters for primitive values and references //
	
	@Override
	public final void set_byte(final long address, final byte value)
	{
		// single-byte values can be just passed through, of course.
		this.actual.set_byte(address, value);
	}

	@Override
	public final void set_boolean(final long address, final boolean value)
	{
		// single-byte values can be just passed through, of course.
		this.actual.set_boolean(address, value);
	}

	@Override
	public final void set_short(final long address, final short value)
	{
		this.actual.set_short(address, Short.reverseBytes(value));
	}

	@Override
	public final void set_char(final long address, final char value)
	{
		this.actual.set_char(address, Character.reverseBytes(value));
	}

	@Override
	public final void set_int(final long address, final int value)
	{
		this.actual.set_int(address, Integer.reverseBytes(value));
	}

	@Override
	public final void set_float(final long address, final float value)
	{
		this.set_int(address, Float.floatToRawIntBits(value));
	}

	@Override
	public final void set_long(final long address, final long value)
	{
		this.actual.set_long(address, Long.reverseBytes(value));
	}

	@Override
	public final void set_double(final long address, final double value)
	{
		this.set_long(address, Double.doubleToRawLongBits(value));
	}

	// note: setting a pointer to a non-Object-relative address makes no sense.

	
	
	// object-based setters for primitive values and references //
	
	@Override
	public final void set_byte(final Object instance, final long offset, final byte value)
	{
		// single-byte values can be just passed through, of course.
		this.actual.set_byte(instance, offset, value);
	}

	@Override
	public final void set_boolean(final Object instance, final long offset, final boolean value)
	{
		// single-byte values can be just passed through, of course.
		this.actual.set_boolean(instance, offset, value);
	}

	@Override
	public final void set_short(final Object instance, final long offset, final short value)
	{
		this.actual.set_short(instance, offset, Short.reverseBytes(value));
	}

	@Override
	public final void set_char(final Object instance, final long offset, final char value)
	{
		this.actual.set_char(instance, offset, Character.reverseBytes(value));
	}

	@Override
	public final void set_int(final Object instance, final long offset, final int value)
	{
		this.actual.set_int(instance, offset, Integer.reverseBytes(value));
	}

	@Override
	public final void set_float(final Object instance, final long offset, final float value)
	{
		this.set_int(instance, offset, Float.floatToRawIntBits(value));
	}

	@Override
	public final void set_long(final Object instance, final long offset, final long value)
	{
		this.actual.set_long(instance, offset, Long.reverseBytes(value));
	}

	@Override
	public final void set_double(final Object instance, final long offset, final double value)
	{
		this.set_long(instance, offset, Double.doubleToRawLongBits(value));
	}

	@Override
	public final void setObject(final Object instance, final long offset, final Object value)
	{
		// pointers may never be byte-reversed
		this.actual.setObject(instance, offset, value);
	}
	
	
	// generic variable-length range copying //
	
	@Override
	public void copyRange(final long sourceAddress, final long targetAddress, final long length)
	{
		this.actual.copyRange(sourceAddress, targetAddress, length);
	}

	@Override
	public void copyRange(final Object source, final long sourceOffset, final Object target, final long targetOffset, final long length)
	{
		this.actual.copyRange(source, sourceOffset, target, targetOffset, length);
	}

	
	
	// address-to-array range copying //
	
	@Override
	public void copyRangeToArray(long sourceAddress, byte[] target);
	
	@Override
	public void copyRangeToArray(long sourceAddress, boolean[] target);

	@Override
	public void copyRangeToArray(long sourceAddress, short[] target);

	@Override
	public void copyRangeToArray(long sourceAddress, char[] target);
	
	@Override
	public void copyRangeToArray(long sourceAddress, int[] target);

	@Override
	public void copyRangeToArray(long sourceAddress, float[] target);

	@Override
	public void copyRangeToArray(long sourceAddress, long[] target);

	@Override
	public void copyRangeToArray(long sourceAddress, double[] target);

	
	
	// array-to-address range copying //
	
	@Override
	public void copyArrayToAddress(byte[] array, long targetAddress);
	
	@Override
	public void copyArrayToAddress(boolean[] array, long targetAddress);
	
	@Override
	public void copyArrayToAddress(short[] array, long targetAddress);

	@Override
	public void copyArrayToAddress(char[] array, long targetAddress);
	
	@Override
	public void copyArrayToAddress(int[] array, long targetAddress);
	
	@Override
	public void copyArrayToAddress(float[] array, long targetAddress);
	
	@Override
	public void copyArrayToAddress(long[] array, long targetAddress);
	
	@Override
	public void copyArrayToAddress(double[] array, long targetAddress);

	
	
	// logic to calculate the total memory requirements of arrays of a given component type and length //
	
	@Override
	public long byteSizeArray_byte(long elementCount);

	@Override
	public long byteSizeArray_boolean(long elementCount);

	@Override
	public long byteSizeArray_short(long elementCount);

	@Override
	public long byteSizeArray_char(long elementCount);

	@Override
	public long byteSizeArray_int(long elementCount);

	@Override
	public long byteSizeArray_float(long elementCount);

	@Override
	public long byteSizeArray_long(long elementCount);

	@Override
	public long byteSizeArray_double(long elementCount);

	@Override
	public long byteSizeArrayObject(long elementCount);
	
	
	
	// transformative byte array primitive value setters //
	
	// (24.10.2019 TM)FIXME: priv#111: rename all byte[] setter methods to "setXXXXInBytes" to avoid compiler ambiguity.
	
	@Override
	public void set_byte(byte[] bytes, int index, byte value);
	
	@Override
	public void set_boolean(byte[] bytes, int index, boolean value);

	@Override
	public void set_short(byte[] bytes, int index, short value);

	@Override
	public void set_char(byte[] bytes, int index, char value);

	@Override
	public void set_int(byte[] bytes, int index, int value);

	@Override
	public void set_float(byte[] bytes, int index, float value);

	@Override
	public void set_long(byte[] bytes, int index, long value);

	@Override
	public void set_double(byte[] bytes, int index, double value);
		
	
	
	// conversion to byte array //
	
	@Override
	public byte[] asByteArray(long[] longArray);

	@Override
	public byte[] asByteArray(long value);
	
	

	// special system methods, not really memory-related //
	
	@Override
	public final void ensureClassInitialized(final Class<?> c)
	{
		this.actual.ensureClassInitialized(c);
	}
	
	@Override
	public final void ensureClassInitialized(final Class<?>... classes)
	{
		this.actual.ensureClassInitialized(classes);
	}
	
	@Override
	public final <T> T instantiateBlank(final Class<T> c) throws InstantiationRuntimeException
	{
		return this.actual.instantiateBlank(c);
	}

	@Override
	public final void throwUnchecked(final Throwable t)
	{
		this.actual.throwUnchecked(t);
	}
	
	
	
	// byte order reversing logic //
	
	@Override
	public final boolean isReversing()
	{
		return true;
	}
	
	@Override
	public final MemoryAccessor toReversing()
	{
		return this.actual;
	}
	
	
}

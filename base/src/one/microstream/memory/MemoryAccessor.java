package one.microstream.memory;

import java.lang.reflect.Field;
import java.util.function.Predicate;

import one.microstream.exceptions.InstantiationRuntimeException;
import one.microstream.functional.XFunc;
import one.microstream.reflect.XReflect;


public interface MemoryAccessor
{
	// memory allocation //
	
	public long allocateMemory(long bytes);

	public long reallocateMemory(long address, long bytes);

	public void freeMemory(long address);
	
	public void fillMemory(long address, long length, byte value);
	
	
	
	// address-based getters for primitive values //
	
	public byte get_byte(long address);

	public boolean get_boolean(long address);

	public short get_short(long address);

	public char get_char(long address);

	public int get_int(long address);

	public float get_float(long address);

	public long get_long(long address);

	public double get_double(long address);

	// note: getting a pointer from a non-Object-relative address makes no sense.
	
	
	
	// object-based getters for primitive values and references //
	
	public byte get_byte(Object instance, long offset);

	public boolean get_boolean(Object instance, long offset);

	public short get_short(Object instance, long offset);

	public char get_char(Object instance, long offset);

	public int get_int(Object instance, long offset);

	public float get_float(Object instance, long offset);

	public long get_long(Object instance, long offset);

	public double get_double(Object instance, long offset);

	public Object getObject(Object instance, long offset);
	
	
	
	// address-based setters for primitive values //
	
	public void set_byte(long address, byte value);

	public void set_boolean(long address, boolean value);

	public void set_short(long address, short value);

	public void set_char(long address, char value);

	public void set_int(long address, int value);

	public void set_float(long address, float value);

	public void set_long(long address, long value);

	public void set_double(long address, double value);

	// note: setting a pointer to a non-Object-relative address makes no sense.
	
	
	// object-based setters for primitive values and references //
	
	public void set_byte(Object instance, long offset, byte value);

	public void set_boolean(Object instance, long offset, boolean value);

	public void set_short(Object instance, long offset, short value);

	public void set_char(Object instance, long offset, char value);

	public void set_int(Object instance, long offset, int value);

	public void set_float(Object instance, long offset, float value);

	public void set_long(Object instance, long offset, long value);

	public void set_double(Object instance, long offset, double value);

	public void setObject(Object instance, long offset, Object value);

		
	
	// transformative byte array primitive value setters //
	
	public void set_byteInBytes(byte[] bytes, int index, byte value);
	
	public void set_booleanInBytes(byte[] bytes, int index, boolean value);

	public void set_shortInBytes(byte[] bytes, int index, short value);

	public void set_charInBytes(byte[] bytes, int index, char value);

	public void set_intInBytes(byte[] bytes, int index, int value);

	public void set_floatInBytes(byte[] bytes, int index, float value);

	public void set_longInBytes(byte[] bytes, int index, long value);

	public void set_doubleInBytes(byte[] bytes, int index, double value);

	
	
	// generic variable-length range copying //
	
	public void copyRange(long sourceAddress, long targetAddress, long length);

	
	
	// address-to-array range copying //
	
	public void copyRangeToArray(long sourceAddress, byte[] target);
	
	public void copyRangeToArray(long sourceAddress, boolean[] target);

	public void copyRangeToArray(long sourceAddress, short[] target);

	public void copyRangeToArray(long sourceAddress, char[] target);
	
	public void copyRangeToArray(long sourceAddress, int[] target);

	public void copyRangeToArray(long sourceAddress, float[] target);

	public void copyRangeToArray(long sourceAddress, long[] target);

	public void copyRangeToArray(long sourceAddress, double[] target);

	
	
	// array-to-address range copying //
	
	public void copyArrayToAddress(byte[] array, long targetAddress);
	
	public void copyArrayToAddress(boolean[] array, long targetAddress);
	
	public void copyArrayToAddress(short[] array, long targetAddress);

	public void copyArrayToAddress(char[] array, long targetAddress);
	
	public void copyArrayToAddress(int[] array, long targetAddress);
	
	public void copyArrayToAddress(float[] array, long targetAddress);
	
	public void copyArrayToAddress(long[] array, long targetAddress);
	
	public void copyArrayToAddress(double[] array, long targetAddress);
		
	
	
	// conversion to byte array //
	
	public default byte[] asByteArray(final long[] values)
	{
		final byte[] array = new byte[values.length * Long.BYTES];
		
		for(int i = 0; i < values.length; i++)
		{
			this.set_longInBytes(array, i * Long.BYTES, values[i]);
		}
		
		return array;
	}

	public default byte[] asByteArray(final long value)
	{
		final byte[] array = new byte[Long.BYTES];
		
		this.set_longInBytes(array, 0, value);
		
		return array;
	}
	
	
	
	// field offset abstraction //
	
	/**
	 * Returns an unspecified, abstract "offset" of the passed {@link Field} to specify a generic access of the
	 * field's value for an instance of its declaring class that can be used with object-based methods like
	 * {@link #set_int(Object, long, int)}. Whether that offset is an actual low-level memory offset relative
	 * to an instance' field offset base or simply an index of the passed field in its declaring class' list
	 * of fields, is implementation-specific.
	 * 
	 * @param field the {@link Field} whose abstract offset shall be determined.
	 * 
	 * @return the passed {@link Field}'s abstract offset.
	 */
	public long objectFieldOffset(Field field);
	
	/**
	 * Array alias vor #objectFieldOffset(Field).
	 */
	public long[] objectFieldOffsets(Field... fields);
	
	/**
	 * Similar to {@link #objectFieldOffset(Field)}, but with the specific object class.<br>
	 * The difference is that the actual object's class is not the declaring class of its fields if
	 * it only extends another class but does not declare an object field itself.
	 * 
	 * @param objectClass
	 * @param field
	 * @return
	 */
	public long objectFieldOffset(Class<?> objectClass, Field field);
	

	/**
	 * Array alias vor #objectFieldOffset(Class, Field).
	 */
	public long[] objectFieldOffsets(Class<?> objectClass, Field... fields);
	
	public default long[] objectFieldOffsets(final Class<?> objectClass)
	{
		return this.objectFieldOffsets(objectClass, XFunc.all());
	}
	
	public default long[] objectFieldOffsets(final Class<?> objectClass, final Predicate<? super Field> selector)
	{
		final Field[] array = XReflect.collectInstanceFields(objectClass, selector);
		
		return this.objectFieldOffsets(objectClass, array);
	}
	
	

	// special system methods //
	
	public void ensureClassInitialized(Class<?> c);
	
	public default void ensureClassInitialized(final Class<?>... classes)
	{
		for(final Class<?> c : classes)
		{
			this.ensureClassInitialized(c);
		}
	}
	
	public <T> T instantiateBlank(Class<T> c) throws InstantiationRuntimeException;

	
	
	
	// byte order reversing logic //
	
	/* NOTE:
	 * There are only two cases to handle for byte order business:
	 * 1.) totally ignoring it (covers LE-LE and BE-BE)
	 * 2.) reversing all multi-byte values (covers LE-BE and BE-LE)
	 * There is no need for memory accessing logic to specifically know its target byte order.
	 * It only needs to know whether it shall reverse the bytes or not.
	 * This means that in the ideal case (same byte order) there is no byte order handling overhead at all.
	 */
	
	public default boolean isReversing()
	{
		return false;
	}
	
	public default MemoryAccessor toReversing()
	{
		return new MemoryAccessorReversing(this);
	}
	
}

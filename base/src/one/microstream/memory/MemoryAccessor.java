package one.microstream.memory;

import java.lang.reflect.Field;

public interface MemoryAccessor
{
	public byte get_byte(long address);

	public boolean get_boolean(long address);

	public short get_short(long address);

	public char get_char(long address);

	public int get_int(long address);

	public float get_float(long address);

	public long get_long(long address);

	public double get_double(long address);

	public Object getObject(long address);
	
	
	
	public byte get_byte(Object instance, long offset);

	public boolean get_boolean(Object instance, long offset);

	public short get_short(Object instance, long offset);

	public char get_char(Object instance, long offset);

	public int get_int(Object instance, long offset);

	public float get_float(Object instance, long offset);

	public long get_long(Object instance, long offset);

	public double get_double(Object instance, long offset);

	public Object getObject(Object instance, long offset);
	
	
	
	public void set_byte(long address, byte value);

	public void set_boolean(long address, boolean value);

	public void set_short(long address, short value);

	public void set_char(long address, char value);

	public void set_int(long address, int value);

	public void set_float(long address, float value);

	public void set_long(long address, long value);

	public void set_double(long address, double value);

	
	
	public void set_byte(Object instance, long offset, byte value);

	public void set_boolean(Object instance, long offset, boolean value);

	public void set_short(Object instance, long offset, short value);

	public void set_char(Object instance, long offset, char value);

	public void set_int(Object instance, long offset, int value);

	public void set_float(Object instance, long offset, float value);

	public void set_long(Object instance, long offset, long value);

	public void set_double(Object instance, long offset, double value);

	public void setObject(Object instance, long offset, Object value);
	
	
	
	public void copyRange(long sourceAddress, long targetAddress, long length);

	public void copyRange(Object source, long sourceOffset, Object target, long targetOffset, long length);
	
	
	public void copyRangeToArray(long sourceAddress, byte[] target);
	
	public void copyRangeToArray(long sourceAddress, boolean[] target);

	public void copyRangeToArray(long sourceAddress, short[] target);

	public void copyRangeToArray(long sourceAddress, char[] target);
	
	public void copyRangeToArray(long sourceAddress, int[] target);

	public void copyRangeToArray(long sourceAddress, float[] target);

	public void copyRangeToArray(long sourceAddress, long[] target);

	public void copyRangeToArray(long sourceAddress, double[] target);
	
	
	
	public void copyArrayToAddress(byte[] array, long targetAddress);
	
	public void copyArrayToAddress(boolean[] array, long targetAddress);
	
	public void copyArrayToAddress(short[] array, long targetAddress);

	public void copyArrayToAddress(char[] array, long targetAddress);
	
	public void copyArrayToAddress(int[] array, long targetAddress);
	
	public void copyArrayToAddress(float[] array, long targetAddress);
	
	public void copyArrayToAddress(long[] array, long targetAddress);
	
	public void copyArrayToAddress(double[] array, long targetAddress);
	
	
	
	public long byteSizeArray_byte(long elementCount);

	public long byteSizeArray_boolean(long elementCount);

	public long byteSizeArray_short(long elementCount);

	public long byteSizeArray_char(long elementCount);

	public long byteSizeArray_int(long elementCount);

	public long byteSizeArray_float(long elementCount);

	public long byteSizeArray_long(long elementCount);

	public long byteSizeArray_double(long elementCount);

	public long byteSizeArrayObject(long elementCount);
	
	
	
	public byte[] asByteArray(long[] longArray);

	public byte[] asByteArray(long value);
	
	
	public void put_byte(byte[] bytes, int index, short value);
	
	public void put_boolean(byte[] bytes, int index, char value);

	public void put_short(byte[] bytes, int index, short value);

	public void put_char(byte[] bytes, int index, char value);

	public void put_int(byte[] bytes, int index, int value);

	public void put_float(byte[] bytes, int index, float value);

	public void put_long(byte[] bytes, int index, long value);

	public void put_double(byte[] bytes, int index, double value);
	
	
	
	// (14.10.2019 TM)FIXME: priv#111: not sure if needed
//	public Object getStaticReference(Field field);
	
//	public Object getStaticFieldBase(Field field);

	// (14.10.2019 TM)FIXME: objectFieldOffset can't stay here. It has to be wrapped in a plattform-specific type using it.
	public long objectFieldOffset(Field field);
	
	// (14.10.2019 TM)FIXME: priv#111: all methods below are currently only used for debugging, not productive code
	
	public int byteSizeReference();
	
	public int byteSizeInstance(Class<?> type);
	
	public int byteSizeObjectHeader(Class<?> type);
	
	public int byteSizeFieldValue(Class<?> type);
	
	/**
	 * Returns the system's memory "page size" (whatever that may be exactely for a given system).
	 * Use with care (and the dependency to a system value in mind!).
	 * 
	 * @return the system's memory "page size".
	 */
	public int pageSize();
	
	public void ensureClassInitialized(Class<?> c);
		
}

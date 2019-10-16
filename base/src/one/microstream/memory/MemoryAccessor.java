package one.microstream.memory;

import java.lang.reflect.Field;

public interface MemoryAccessor
{
	public void set_long(long address, long value);
	
	
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
	
	public int byteSizeFieldValue(Class<?> type);
	
	/**
	 * Returns the system's memory "page size" (whatever that may be exactely for a given system).
	 * Use with care (and the dependency to a system value in mind!).
	 * 
	 * @return the system's memory "page size".
	 */
	public int pageSize();
		
}

package one.microstream.memory;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.ByteOrder;

import one.microstream.exceptions.InstantiationRuntimeException;
import one.microstream.memory.sun.MemoryAccessorSun;



/**
 * Util class for low-level VM memory operations and information that makes the call site independent of
 * a certain JVM implementation (e.g. java.misc.Unsafe).
 *
 * @author Thomas Muenz
 */
public final class XMemory
{
	///////////////////////////////////////////////////////////////////////////
	// constants //
	//////////////
	
	public static MemoryAccessor MEMORY_ACCESSOR = MemoryAccessorSun.New();
	
	public static void setMemoryAccessor(final MemoryAccessor memoryAccessor)
	{
		MEMORY_ACCESSOR = memoryAccessor;
	}
	
	public static MemoryAccessor getMemoryAccessor()
	{
		return MEMORY_ACCESSOR;
	}

	// constant names documenting that a value shall be shifted by n bits. Also to get CheckStyle off my back.
	private static final int
		BITS1 = 1,
		BITS2 = 2,
		BITS3 = 3
	;

	
	
	public static int byteSizeInstance(final Class<?> c)
	{
		return MEMORY_ACCESSOR.byteSizeInstance(c);
	}
	
	public static int byteSizeObjectHeader(final Class<?> c)
	{
		return MEMORY_ACCESSOR.byteSizeObjectHeader(c);
	}
	
	// (21.10.2019 TM)FIXME: priv#111: shouldn't this be encapsulated away?
	public static long objectFieldOffset(final Field field)
	{
		return MEMORY_ACCESSOR.objectFieldOffset(field);
	}

	public static long[] objectFieldOffsets(final Field[] fields)
	{
		final long[] offsets = new long[fields.length];
		for(int i = 0; i < fields.length; i++)
		{
			if(Modifier.isStatic(fields[i].getModifiers()))
			{
				throw new IllegalArgumentException("Not an instance field: " + fields[i]);
			}
			offsets[i] = MEMORY_ACCESSOR.objectFieldOffset(fields[i]);
		}
		return offsets;
	}
	
	static final long internalGetFieldOffset(final Class<?> type, final String declaredFieldName)
	{
		// minimal algorithm, only for local use
		for(Class<?> c = type; c != null && c != Object.class; c = c.getSuperclass())
		{
			try
			{
				for(final Field field : c.getDeclaredFields())
				{
					if(field.getName().equals(declaredFieldName))
					{
						return MEMORY_ACCESSOR.objectFieldOffset(field);
					}
				}
			}
			catch(final Exception e)
			{
				throw new Error(e); // explode and die :)
			}
		}
		throw new Error("Field not found: " + type.getName() + '#' + declaredFieldName);
	}

	// (14.10.2019 TM)FIXME: priv#111: delete if really not used
//	public static final Object getStaticReference(final Field field)
//	{
//		if(!Modifier.isStatic(field.getModifiers()))
//		{
//			throw new IllegalArgumentException();
//		}
//		return MEMORY_ACCESSOR.getObject(VM.staticFieldBase(field), VM.staticFieldOffset(field));
//	}
		
	

	
	public static final int bitSize_byte()
	{
		return Byte.SIZE;
	}

	public static final int bitSize_boolean()
	{
		return Byte.SIZE;
	}

	public static final int bitSize_short()
	{
		return Short.SIZE;
	}

	public static final int bitSize_char()
	{
		return Character.SIZE;
	}

	public static final int bitSize_int()
	{
		return Integer.SIZE;
	}

	public static final int bitSize_float()
	{
		return Float.SIZE;
	}

	public static final int bitSize_long()
	{
		return Long.SIZE;
	}

	public static final int bitSize_double()
	{
		return Double.SIZE;
	}



	///////////////////////////////////////////////////////////////////////////
	// memory byte size methods //
	/////////////////////////////

	public static final int byteSizeFieldValue(final Class<?> type)
	{
		return MEMORY_ACCESSOR.byteSizeFieldValue(type);
	}

	public static final int byteSizePrimitive(final Class<?> type)
	{
		// onec again missing JDK functionality. Roughly ordered by probability.
		if(type == int.class)
		{
			return byteSize_int();
		}
		if(type == long.class)
		{
			return byteSize_long();
		}
		if(type == double.class)
		{
			return byteSize_double();
		}
		if(type == char.class)
		{
			return byteSize_char();
		}
		if(type == boolean.class)
		{
			return byteSize_boolean();
		}
		if(type == byte.class)
		{
			return byteSize_byte();
		}
		if(type == float.class)
		{
			return byteSize_float();
		}
		if(type == short.class)
		{
			return byteSize_short();
		}
				
		throw new IllegalArgumentException(); // intentionally covers void.class
	}

	public static final int byteSize_byte()
	{
		return Byte.BYTES;
	}

	public static final int byteSize_boolean()
	{
		return Byte.BYTES; // because JDK Pros can't figure out the length of a boolean value, obviously.
	}

	public static final int byteSize_short()
	{
		return Short.BYTES;
	}

	public static final int byteSize_char()
	{
		return Character.BYTES;
	}

	public static final int byteSize_int()
	{
		return Integer.BYTES;
	}

	public static final int byteSize_float()
	{
		return Float.BYTES;
	}

	public static final int byteSize_long()
	{
		return Long.BYTES;
	}

	public static final int byteSize_double()
	{
		return Double.BYTES;
	}

	public static final int byteSizeReference()
	{
		return MEMORY_ACCESSOR.byteSizeReference();
	}

	public static final long byteSizeArray_byte(final long elementCount)
	{
		return MEMORY_ACCESSOR.byteSizeArray_byte(elementCount);
	}

	public static final long byteSizeArray_boolean(final long elementCount)
	{
		return MEMORY_ACCESSOR.byteSizeArray_boolean(elementCount);
	}

	public static final long byteSizeArray_short(final long elementCount)
	{
		return MEMORY_ACCESSOR.byteSizeArray_short(elementCount);
	}

	public static final long byteSizeArray_char(final long elementCount)
	{
		return MEMORY_ACCESSOR.byteSizeArray_char(elementCount);
	}

	public static final long byteSizeArray_int(final long elementCount)
	{
		return MEMORY_ACCESSOR.byteSizeArray_int(elementCount);
	}

	public static final long byteSizeArray_float(final long elementCount)
	{
		return MEMORY_ACCESSOR.byteSizeArray_float(elementCount);
	}

	public static final long byteSizeArray_long(final long elementCount)
	{
		return MEMORY_ACCESSOR.byteSizeArray_long(elementCount);
	}

	public static final long byteSizeArray_double(final long elementCount)
	{
		return MEMORY_ACCESSOR.byteSizeArray_double(elementCount);
	}

	public static final long byteSizeArrayObject(final long elementCount)
	{
		return MEMORY_ACCESSOR.byteSizeArrayObject(elementCount);
	}


	

	public static byte[] asByteArray(final long[] longArray)
	{
		return MEMORY_ACCESSOR.asByteArray(longArray);
	}

	public static byte[] asByteArray(final long value)
	{
		return MEMORY_ACCESSOR.asByteArray(value);
	}
	
	/**
	 * Arbitrary value that coincidently matches most hardware's page sizes
	 * without being hard-tied to an actual pageSize system value.
	 * So this value is an educated guess and most of the time (almost always)
	 * a "good" value when paged-sized-ish buffer sizes are needed, while still
	 * not being at the mercy of an OS's JVM implementation.
	 * 
	 * @return a "good" value for a paged-sized-ish default buffer size.
	 */
	public static int defaultBufferSize()
	{
		return 4096;
	}
	
	
	public static void put_byte(final byte[] bytes, final int index, final short value)
	{
		MEMORY_ACCESSOR.put_byte(bytes, index, value);
	}
	
	public static void put_boolean(final byte[] bytes, final int index, final char value)
	{
		MEMORY_ACCESSOR.put_boolean(bytes, index, value);
	}

	public static void put_short(final byte[] bytes, final int index, final short value)
	{
		MEMORY_ACCESSOR.put_short(bytes, index, value);
	}

	public static void put_char(final byte[] bytes, final int index, final char value)
	{
		MEMORY_ACCESSOR.put_char(bytes, index, value);
	}

	public static void put_int(final byte[] bytes, final int index, final int value)
	{
		MEMORY_ACCESSOR.put_int(bytes, index, value);
	}

	public static void put_float(final byte[] bytes, final int index, final float value)
	{
		MEMORY_ACCESSOR.put_float(bytes, index, value);
	}

	public static void put_long(final byte[] bytes, final int index, final long value)
	{
		MEMORY_ACCESSOR.put_long(bytes, index, value);
	}

	public static void put_double(final byte[] bytes, final int index, final double value)
	{
		MEMORY_ACCESSOR.put_double(bytes, index, value);
	}



	public static final byte get_byte(final long address)
	{
		return MEMORY_ACCESSOR.get_byte(address);
	}

	public static final boolean get_boolean(final long address)
	{
		return MEMORY_ACCESSOR.get_boolean(address);
	}

	public static final short get_short(final long address)
	{
		return MEMORY_ACCESSOR.get_short(address);
	}

	public static final char get_char(final long address)
	{
		return MEMORY_ACCESSOR.get_char(address);
	}

	public static final int get_int(final long address)
	{
		return MEMORY_ACCESSOR.get_int(address);
	}

	public static final float get_float(final long address)
	{
		return MEMORY_ACCESSOR.get_float(address);
	}

	public static final long get_long(final long address)
	{
		return MEMORY_ACCESSOR.get_long(address);
	}

	public static final double get_double(final long address)
	{
		return MEMORY_ACCESSOR.get_double(address);
	}

	public static final Object getObject(final long address)
	{
		return MEMORY_ACCESSOR.getObject(address);
	}
	
	
	
	
	
	// FIXME priv#111: baustelle


	public static final byte get_byte(final Object instance, final long offset)
	{
		return MEMORY_ACCESSOR.getByte(instance, offset);
	}

	public static final boolean get_boolean(final Object instance, final long offset)
	{
		return MEMORY_ACCESSOR.getBoolean(instance, offset);
	}

	public static final short get_short(final Object instance, final long offset)
	{
		return MEMORY_ACCESSOR.getShort(instance, offset);
	}

	public static final char get_char(final Object instance, final long offset)
	{
		return MEMORY_ACCESSOR.getChar(instance, offset);
	}

	public static final int get_int(final Object instance, final long offset)
	{
		return MEMORY_ACCESSOR.getInt(instance, offset);
	}

	public static final float get_float(final Object instance, final long offset)
	{
		return MEMORY_ACCESSOR.getInt(instance, offset);
	}

	public static final long get_long(final Object instance, final long offset)
	{
		return MEMORY_ACCESSOR.getLong(instance, offset);
	}

	public static final double get_double(final Object instance, final long offset)
	{
		return MEMORY_ACCESSOR.getInt(instance, offset);
	}

	public static final Object getObject(final Object instance, final long offset)
	{
		return MEMORY_ACCESSOR.getObject(instance, offset);
	}




	public static final void set_byte(final long address, final byte value)
	{
		MEMORY_ACCESSOR.putByte(address, value);
	}

	public static final void set_boolean(final long address, final boolean value)
	{
		// where the heck is Unsafe#putBoolean(long, boolean)? Forgot to implement? Wtf?
		MEMORY_ACCESSOR.putBoolean(null, address, value);
	}

	public static final void set_short(final long address, final short value)
	{
		MEMORY_ACCESSOR.putShort(address, value);
	}

	public static final void set_char(final long address, final char value)
	{
		MEMORY_ACCESSOR.putChar(address, value);
	}

	public static final void set_int(final long address, final int value)
	{
		MEMORY_ACCESSOR.putInt(address, value);
	}

	public static final void set_float(final long address, final float value)
	{
		MEMORY_ACCESSOR.putFloat(address, value);
	}

	public static final void set_long(final long address, final long value)
	{
		MEMORY_ACCESSOR.putLong(address, value);
	}

	public static final void set_double(final long address, final double value)
	{
		MEMORY_ACCESSOR.putDouble(address, value);
	}

	public static final void set_byte(final Object instance, final long offset, final byte value)
	{
		MEMORY_ACCESSOR.putByte(instance, offset, value);
	}

	public static final void set_boolean(final Object instance, final long offset, final boolean value)
	{
		MEMORY_ACCESSOR.putBoolean(instance, offset, value);
	}

	public static final void set_short(final Object instance, final long offset, final short value)
	{
		MEMORY_ACCESSOR.putShort(instance, offset, value);
	}

	public static final void set_char(final Object instance, final long offset, final char value)
	{
		MEMORY_ACCESSOR.putChar(instance, offset, value);
	}

	public static final void set_int(final Object instance, final long offset, final int value)
	{
		MEMORY_ACCESSOR.putInt(instance, offset, value);
	}

	public static final void set_float(final Object instance, final long offset, final float value)
	{
		MEMORY_ACCESSOR.putFloat(instance, offset, value);
	}

	public static final void set_long(final Object instance, final long offset, final long value)
	{
		MEMORY_ACCESSOR.putLong(instance, offset, value);
	}

	public static final void set_double(final Object instance, final long offset, final double value)
	{
		MEMORY_ACCESSOR.putDouble(instance, offset, value);
	}

	public static final void setObject(final Object instance, final long offset, final Object value)
	{
		MEMORY_ACCESSOR.putObject(instance, offset, value);
	}

	public static final void copyRange(final long sourceAddress, final long targetAddress, final long length)
	{
		MEMORY_ACCESSOR.copyMemory(sourceAddress, targetAddress, length);
	}

	public static final void copyRange(
		final Object source,
		final long   sourceOffset,
		final Object target,
		final long   targetOffset,
		final long   length
	)
	{
		MEMORY_ACCESSOR.copyMemory(source, sourceOffset, target, targetOffset, length);
	}

	public static final void copyRangeToArray(final long sourceAddress, final byte[] target)
	{
		MEMORY_ACCESSOR.copyMemory(null, sourceAddress, target, ARRAY_BYTE_BASE_OFFSET, target.length);
	}
	
	public static final void copyRangeToArray(
		final long   sourceAddress,
		final byte[] target       ,
		final int    targetIndex  ,
		final long   length
	)
	{
		MEMORY_ACCESSOR.copyMemory(null, sourceAddress, target, ARRAY_BYTE_BASE_OFFSET + targetIndex, length);
	}

	public static final void copyRangeToArray(final long sourceAddress, final boolean[] target)
	{
		MEMORY_ACCESSOR.copyMemory(null, sourceAddress, target, ARRAY_BOOLEAN_BASE_OFFSET, target.length);
	}

	public static final void copyRangeToArray(final long sourceAddress, final short[] target)
	{
		MEMORY_ACCESSOR.copyMemory(null, sourceAddress, target, ARRAY_SHORT_BASE_OFFSET, target.length << BITS1);
	}

	public static final void copyRangeToArray(final long sourceAddress, final char[] target)
	{
		MEMORY_ACCESSOR.copyMemory(null, sourceAddress, target, ARRAY_CHAR_BASE_OFFSET, target.length << BITS1);
	}
	
	public static final void copyRangeToArray(
		final long   sourceAddress,
		final char[] target       ,
		final int    targetIndex  ,
		final long   targetLength
	)
	{
		MEMORY_ACCESSOR.copyMemory(null, sourceAddress, target, ARRAY_CHAR_BASE_OFFSET + (targetIndex << BITS1), targetLength << BITS1);
	}

	public static final void copyRangeToArray(final long sourceAddress, final int[] target)
	{
		MEMORY_ACCESSOR.copyMemory(null, sourceAddress, target, ARRAY_INT_BASE_OFFSET, target.length << BITS2);
	}

	public static final void copyRangeToArray(final long sourceAddress, final float[] target)
	{
		MEMORY_ACCESSOR.copyMemory(null, sourceAddress, target, ARRAY_FLOAT_BASE_OFFSET, target.length << BITS2);
	}

	public static final void copyRangeToArray(final long sourceAddress, final long[] target)
	{
		MEMORY_ACCESSOR.copyMemory(null, sourceAddress, target, ARRAY_LONG_BASE_OFFSET, target.length << BITS3);
	}

	public static final void copyRangeToArray(final long sourceAddress, final double[] target)
	{
		MEMORY_ACCESSOR.copyMemory(null, sourceAddress, target, ARRAY_DOUBLE_BASE_OFFSET, target.length << BITS3);
	}

	
	
	// copyArrayToAddress //

	public static final void copyArrayToAddress(final byte[] array, final long targetAddress)
	{
		MEMORY_ACCESSOR.copyMemory(array, ARRAY_BYTE_BASE_OFFSET, null, targetAddress, array.length);
	}

	public static final void copyArrayToAddress(
		final byte[] array        ,
		final int    offset       ,
		final int    length       ,
		final long   targetAddress
	)
	{
		MEMORY_ACCESSOR.copyMemory(array, ARRAY_BYTE_BASE_OFFSET + offset, null, targetAddress, length);
	}
	
	public static final void copyArrayToAddress(final boolean[] array, final long targetAddress)
	{
		MEMORY_ACCESSOR.copyMemory(array, ARRAY_BOOLEAN_BASE_OFFSET, null, targetAddress, array.length);
	}

	public static final void copyArrayToAddress(
		final boolean[] array        ,
		final int       offset       ,
		final int       length       ,
		final long      targetAddress
	)
	{
		MEMORY_ACCESSOR.copyMemory(array, ARRAY_BOOLEAN_BASE_OFFSET + offset, null, targetAddress, length);
	}
	
	public static final void copyArrayToAddress(final short[] array, final long targetAddress)
	{
		MEMORY_ACCESSOR.copyMemory(array, ARRAY_SHORT_BASE_OFFSET, null, targetAddress, array.length << BITS1);
	}

	public static final void copyArrayToAddress(
		final short[] array        ,
		final int     offset       ,
		final int     length       ,
		final long    targetAddress
	)
	{
		MEMORY_ACCESSOR.copyMemory(array, ARRAY_SHORT_BASE_OFFSET + (offset << BITS1), null, targetAddress, length << BITS1);
	}

	public static final void copyArrayToAddress(final char[] array, final long targetAddress)
	{
		MEMORY_ACCESSOR.copyMemory(array, ARRAY_CHAR_BASE_OFFSET, null, targetAddress, array.length << BITS1);
	}
	
	public static final void copyArrayToAddress(
		final char[] array        ,
		final int    offset       ,
		final int    length       ,
		final long   targetAddress
	)
	{
		MEMORY_ACCESSOR.copyMemory(array, ARRAY_CHAR_BASE_OFFSET + (offset << BITS1), null, targetAddress, length << BITS1);
	}
	
	public static final void copyArrayToAddress(final int[] array, final long targetAddress)
	{
		MEMORY_ACCESSOR.copyMemory(array, ARRAY_INT_BASE_OFFSET, null, targetAddress, array.length << BITS2);
	}

	public static final void copyArrayToAddress(
		final int[] array        ,
		final int   offset       ,
		final int   length       ,
		final long  targetAddress
	)
	{
		MEMORY_ACCESSOR.copyMemory(array, ARRAY_INT_BASE_OFFSET + (offset << BITS2), null, targetAddress, length << BITS2);
	}
	
	public static final void copyArrayToAddress(final float[] array, final long targetAddress)
	{
		MEMORY_ACCESSOR.copyMemory(array, ARRAY_FLOAT_BASE_OFFSET, null, targetAddress, array.length << BITS2);
	}

	public static final void copyArrayToAddress(
		final float[] array        ,
		final int     offset       ,
		final int     length       ,
		final long    targetAddress
	)
	{
		MEMORY_ACCESSOR.copyMemory(array, ARRAY_FLOAT_BASE_OFFSET + (offset << BITS2), null, targetAddress, length << BITS2);
	}
	
	public static final void copyArrayToAddress(final long[] array, final long targetAddress)
	{
		MEMORY_ACCESSOR.copyMemory(array, ARRAY_LONG_BASE_OFFSET, null, targetAddress, array.length << BITS3);
	}

	public static final void copyArrayToAddress(
		final long[]   array        ,
		final int      offset       ,
		final int      length       ,
		final long     targetAddress
	)
	{
		MEMORY_ACCESSOR.copyMemory(array, ARRAY_LONG_BASE_OFFSET + (offset << BITS3), null, targetAddress, length << BITS3);
	}
	
	public static final void copyArrayToAddress(final double[] array, final long targetAddress)
	{
		MEMORY_ACCESSOR.copyMemory(array, ARRAY_DOUBLE_BASE_OFFSET, null, targetAddress, array.length << BITS3);
	}

	public static final void copyArrayToAddress(
		final double[] array        ,
		final int      offset       ,
		final int      length       ,
		final long     targetAddress
	)
	{
		MEMORY_ACCESSOR.copyMemory(array, ARRAY_DOUBLE_BASE_OFFSET + (offset << BITS3), null, targetAddress, length << BITS3);
	}

	

	public static final byte get_byteFromBytes(final byte[] data, final int offset)
	{
		return MEMORY_ACCESSOR.getByte(data, ARRAY_BYTE_BASE_OFFSET + offset);
	}

	public static final boolean get_booleanFromBytes(final byte[] data, final int offset)
	{
		return MEMORY_ACCESSOR.getBoolean(data, ARRAY_BOOLEAN_BASE_OFFSET + offset);
	}

	public static final short get_shortFromBytes(final byte[] data, final int offset)
	{
		return MEMORY_ACCESSOR.getShort(data, ARRAY_SHORT_BASE_OFFSET + offset);
	}

	public static final char get_charFromBytes(final byte[] data, final int offset)
	{
		return MEMORY_ACCESSOR.getChar(data, ARRAY_CHAR_BASE_OFFSET + offset);
	}

	public static final int get_intFromBytes(final byte[] data, final int offset)
	{
		return MEMORY_ACCESSOR.getInt(data, ARRAY_INT_BASE_OFFSET + offset);
	}

	public static final float get_floatFromBytes(final byte[] data, final int offset)
	{
		return MEMORY_ACCESSOR.getFloat(data, ARRAY_FLOAT_BASE_OFFSET + offset);
	}

	public static final long get_longFromBytes(final byte[] data, final int offset)
	{
		return MEMORY_ACCESSOR.getLong(data, ARRAY_LONG_BASE_OFFSET + offset);
	}

	public static final double get_doubleFromBytes(final byte[] data, final int offset)
	{
		return MEMORY_ACCESSOR.getDouble(data, ARRAY_DOUBLE_BASE_OFFSET + offset);
	}

	public static final long allocate(final long bytes)
	{
		return MEMORY_ACCESSOR.allocateMemory(bytes);
	}

	public static final long reallocate(final long address, final long bytes)
	{
		return MEMORY_ACCESSOR.reallocateMemory(address, bytes);
	}

	public static final void fillRange(final long address, final long length, final byte value)
	{
		MEMORY_ACCESSOR.setMemory(address, length, value);
	}

	public static final void free(final long address)
	{
		MEMORY_ACCESSOR.freeMemory(address);
	}

	public static final boolean compareAndSwap_int(
		final Object subject    ,
		final long   offset     ,
		final int    expected   ,
		final int    replacement
	)
	{
		return MEMORY_ACCESSOR.compareAndSwapInt(subject, offset, expected, replacement);
	}

	public static final boolean compareAndSwap_long(
		final Object subject    ,
		final long   offset     ,
		final long   expected   ,
		final long   replacement
	)
	{
		return MEMORY_ACCESSOR.compareAndSwapLong(subject, offset, expected, replacement);
	}

	public static final boolean compareAndSwapObject(
		final Object subject    ,
		final long   offset     ,
		final Object expected   ,
		final Object replacement
	)
	{
		return MEMORY_ACCESSOR.compareAndSwapObject(subject, offset, expected, replacement);
	}
	
	public static ByteOrder nativeByteOrder()
	{
		return ByteOrder.nativeOrder();
	}
	
	// because they (he) couldn't have implemented that where it belongs.
	public static ByteOrder resolveByteOrder(final String name)
	{
		if(name.equals(ByteOrder.BIG_ENDIAN.toString()))
		{
			return ByteOrder.BIG_ENDIAN;
		}
		if(name.equals(ByteOrder.LITTLE_ENDIAN.toString()))
		{
			return ByteOrder.LITTLE_ENDIAN;
		}
		
		// (31.10.2018 TM)EXCP: proper exception
		throw new RuntimeException("Unknown ByteOrder: \"" + name + "\"");
	}
	
	@SuppressWarnings("unchecked")
	public static final <T> T instantiate(final Class<T> c) throws InstantiationRuntimeException
	{
		try
		{
			return (T)VM.allocateInstance(c);
		}
		catch(final InstantiationException e)
		{
			throw new InstantiationRuntimeException(e);
		}
	}
	

	
	////////////////////////////////////////////////////////////////////////
	// some nasty util methods not directly related to memory operations //
	//////////////////////////////////////////////////////////////////////

	public static final void throwUnchecked(final Throwable t)
	{
		MEMORY_ACCESSOR.throwException(t);
	}
	
	public static final void ensureClassInitialized(final Class<?>... classes)
	{
		for(final Class<?> c : classes)
		{
			ensureClassInitialized(c);
		}
	}

	public static final void ensureClassInitialized(final Class<?> c)
	{
		MEMORY_ACCESSOR.ensureClassInitialized(c);
	}
	

	
	////////////////////////////////////////////////////////
	// copies of general logic to eliminate dependencies //
	//////////////////////////////////////////////////////
	
	private static final int checkArrayRange(final long capacity)
	{
		// " >= " proved to be faster in tests than ">" (probably due to simple sign checking)
		if(capacity > Integer.MAX_VALUE)
		{
			throw new IllegalArgumentException("Invalid array length: " + capacity);
		}
		
		return (int)capacity;
	}
	
	private static final <T> T notNull(final T object) throws NullPointerException
	{
		if(object == null)
		{
			// removing this method's stack trace entry is kind of a hack. On the other hand, it's not.
			throw new NullPointerException();
		}
		
		return object;
	}



	// implicitely used in #calculateByteSizeObjectHeader
	Object calculateByteSizeObjectHeaderFieldOffsetDummy;

	
	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////
	
	/**
	 * Dummy constructor to prevent instantiation of this static-only utility class.
	 * 
	 * @throws UnsupportedOperationException
	 */
	private XMemory()
	{
		// static only
		throw new UnsupportedOperationException();
	}

}


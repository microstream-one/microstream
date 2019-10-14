package one.microstream.memory;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.ByteOrder;
import java.util.Arrays;

import one.microstream.exceptions.InstantiationRuntimeException;



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
		return Memory
	}

	public static final long byteSizeArray_boolean(final long elementCount)
	{
		return ARRAY_BOOLEAN_BASE_OFFSET + elementCount;
	}

	public static final long byteSizeArray_short(final long elementCount)
	{
		return ARRAY_SHORT_BASE_OFFSET + (elementCount << BITS1);
	}

	public static final long byteSizeArray_char(final long elementCount)
	{
		return ARRAY_CHAR_BASE_OFFSET + (elementCount << BITS1);
	}

	public static final long byteSizeArray_int(final long elementCount)
	{
		return ARRAY_INT_BASE_OFFSET + (elementCount << BITS2);
	}

	public static final long byteSizeArray_float(final long elementCount)
	{
		return ARRAY_FLOAT_BASE_OFFSET + (elementCount << BITS2);
	}

	public static final long byteSizeArray_long(final long elementCount)
	{
		return ARRAY_LONG_BASE_OFFSET + (elementCount << BITS3);
	}

	public static final long byteSizeArray_double(final long elementCount)
	{
		return ARRAY_DOUBLE_BASE_OFFSET + (elementCount << BITS3);
	}

	public static final long byteSizeArrayObject(final long elementCount)
	{
		return ARRAY_OBJECT_BASE_OFFSET + elementCount * byteSizeReference();
	}


	public static Field[] collectPrimitiveFieldsByByteSize(final Field[] fields, final int byteSize)
	{
		if(byteSize != byteSize_byte()
		&& byteSize != byteSize_short()
		&& byteSize != byteSize_int()
		&& byteSize != byteSize_long()
		)
		{
			throw new IllegalArgumentException("Invalid Java primitive byte size: " + byteSize);
		}

		final Field[] primFields = new Field[fields.length];
		int primFieldsCount = 0;
		for(int i = 0; i < fields.length; i++)
		{
			if(fields[i].getType().isPrimitive() && XMemory.byteSizePrimitive(fields[i].getType()) == byteSize)
			{
				primFields[primFieldsCount++] = fields[i];
			}
		}
		return Arrays.copyOf(primFields, primFieldsCount);
	}

	public static int calculatePrimitivesLength(final Field[] primFields)
	{
		int length = 0;
		for(int i = 0; i < primFields.length; i++)
		{
			if(!primFields[i].getType().isPrimitive())
			{
				throw new IllegalArgumentException("Not a primitive field: " + primFields[i]);
			}
			length += XMemory.byteSizePrimitive(primFields[i].getType());
		}
		return length;
	}





	public static byte[] asByteArray(final long[] longArray)
	{
		final byte[] bytes = new byte[checkArrayRange((long)longArray.length << BITS3)];
		VM.copyMemory(longArray, ARRAY_LONG_BASE_OFFSET, bytes, ARRAY_BYTE_BASE_OFFSET, bytes.length);
		return bytes;
	}

	public static byte[] asByteArray(final long value)
	{
		final byte[] bytes = new byte[byteSize_long()];
		put_long(bytes, 0, value);
		return bytes;
	}
	
	/**
	 * Arbitrary value that coincidently matches most hardware's page sizes
	 * without being hard-tied to Unsafe#pageSize.
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
	
	/**
	 * Returns the system's memory "page size" (whatever that may be exactely for a given system).
	 * Use with care (and the dependency to a system value in mind!).
	 * 
	 * @return the system's memory "page size".
	 */
	public static int pageSize()
	{
		return VM.pageSize();
	}
	

	
	public static void put_byte(final byte[] bytes, final int index, final short value)
	{
		VM.putShort(bytes, ARRAY_BYTE_BASE_OFFSET + index, value);
	}
	
	public static void put_boolean(final byte[] bytes, final int index, final char value)
	{
		VM.putChar(bytes, ARRAY_BOOLEAN_BASE_OFFSET + index, value);
	}

	public static void put_short(final byte[] bytes, final int index, final short value)
	{
		VM.putShort(bytes, ARRAY_SHORT_BASE_OFFSET+ index, value);
	}

	public static void put_char(final byte[] bytes, final int index, final char value)
	{
		VM.putChar(bytes, ARRAY_CHAR_BASE_OFFSET + index, value);
	}

	public static void put_int(final byte[] bytes, final int index, final int value)
	{
		VM.putInt(bytes, ARRAY_INT_BASE_OFFSET + index, value);
	}

	public static void put_float(final byte[] bytes, final int index, final float value)
	{
		VM.putFloat(bytes, ARRAY_FLOAT_BASE_OFFSET + index, value);
	}

	public static void put_long(final byte[] bytes, final int index, final long value)
	{
		VM.putLong(bytes, ARRAY_LONG_BASE_OFFSET + index, value);
	}

	public static void put_double(final byte[] bytes, final int index, final double value)
	{
		VM.putDouble(bytes, ARRAY_DOUBLE_BASE_OFFSET + index, value);
	}

	public static void _longInByteArray(final byte[] bytes, final long value)
	{
		VM.putLong(bytes, ARRAY_BYTE_BASE_OFFSET, value);
	}

	public static long _longFromByteArray(final byte[] bytes)
	{
		return VM.getLong(bytes, ARRAY_BYTE_BASE_OFFSET);
	}



	public static final byte get_byte(final long address)
	{
		return VM.getByte(address);
	}

	public static final boolean get_boolean(final long address)
	{
		return VM.getBoolean(null, address);
	}

	public static final short get_short(final long address)
	{
		return VM.getShort(address);
	}

	public static final char get_char(final long address)
	{
		return VM.getChar(address);
	}

	public static final int get_int(final long address)
	{
		return VM.getInt(address);
	}

	public static final float get_float(final long address)
	{
		return VM.getFloat(address);
	}

	public static final long get_long(final long address)
	{
		return VM.getLong(address);
	}

	public static final double get_double(final long address)
	{
		return VM.getDouble(address);
	}

	public static final Object getObject(final long address)
	{
		return VM.getObject(null, address);
	}


	public static final byte get_byte(final Object instance, final long address)
	{
		return VM.getByte(instance, address);
	}

	public static final boolean get_boolean(final Object instance, final long address)
	{
		return VM.getBoolean(instance, address);
	}

	public static final short get_short(final Object instance, final long address)
	{
		return VM.getShort(instance, address);
	}

	public static final char get_char(final Object instance, final long address)
	{
		return VM.getChar(instance, address);
	}

	public static final int get_int(final Object instance, final long address)
	{
		return VM.getInt(instance, address);
	}

	public static final float get_float(final Object instance, final long address)
	{
		return VM.getInt(instance, address);
	}

	public static final long get_long(final Object instance, final long address)
	{
		return VM.getLong(instance, address);
	}

	public static final double get_double(final Object instance, final long address)
	{
		return VM.getInt(instance, address);
	}

	public static final Object getObject(final Object instance, final long address)
	{
		return VM.getObject(instance, address);
	}




	public static final void set_byte(final long address, final byte value)
	{
		VM.putByte(address, value);
	}

	public static final void set_boolean(final long address, final boolean value)
	{
		// where the heck is Unsafe#putBoolean(long, boolean)? Forgot to implement? Wtf?
		VM.putBoolean(null, address, value);
	}

	public static final void set_short(final long address, final short value)
	{
		VM.putShort(address, value);
	}

	public static final void set_char(final long address, final char value)
	{
		VM.putChar(address, value);
	}

	public static final void set_int(final long address, final int value)
	{
		VM.putInt(address, value);
	}

	public static final void set_float(final long address, final float value)
	{
		VM.putFloat(address, value);
	}

	public static final void set_long(final long address, final long value)
	{
		// (11.10.2019 TM)FIXME: priv#111: experimental memory accessor modularization
		MEMORY_ACCESSOR.set_long(address, value);
//		VM.putLong(address, value);
	}

	public static final void set_double(final long address, final double value)
	{
		VM.putDouble(address, value);
	}

	public static final void set_byte(final Object instance, final long offset, final byte value)
	{
		VM.putByte(instance, offset, value);
	}

	public static final void set_boolean(final Object instance, final long offset, final boolean value)
	{
		VM.putBoolean(instance, offset, value);
	}

	public static final void set_short(final Object instance, final long offset, final short value)
	{
		VM.putShort(instance, offset, value);
	}

	public static final void set_char(final Object instance, final long offset, final char value)
	{
		VM.putChar(instance, offset, value);
	}

	public static final void set_int(final Object instance, final long offset, final int value)
	{
		VM.putInt(instance, offset, value);
	}

	public static final void set_float(final Object instance, final long offset, final float value)
	{
		VM.putFloat(instance, offset, value);
	}

	public static final void set_long(final Object instance, final long offset, final long value)
	{
		VM.putLong(instance, offset, value);
	}

	public static final void set_double(final Object instance, final long offset, final double value)
	{
		VM.putDouble(instance, offset, value);
	}

	public static final void setObject(final Object instance, final long offset, final Object value)
	{
		VM.putObject(instance, offset, value);
	}

	public static final void copyRange(final long sourceAddress, final long targetAddress, final long length)
	{
		VM.copyMemory(sourceAddress, targetAddress, length);
	}

	public static final void copyRange(
		final Object source,
		final long   sourceOffset,
		final Object target,
		final long   targetOffset,
		final long   length
	)
	{
		VM.copyMemory(source, sourceOffset, target, targetOffset, length);
	}

	public static final void copyRangeToArray(final long sourceAddress, final byte[] target)
	{
		VM.copyMemory(null, sourceAddress, target, ARRAY_BYTE_BASE_OFFSET, target.length);
	}
	
	public static final void copyRangeToArray(
		final long   sourceAddress,
		final byte[] target       ,
		final int    targetIndex  ,
		final long   length
	)
	{
		VM.copyMemory(null, sourceAddress, target, ARRAY_BYTE_BASE_OFFSET + targetIndex, length);
	}

	public static final void copyRangeToArray(final long sourceAddress, final boolean[] target)
	{
		VM.copyMemory(null, sourceAddress, target, ARRAY_BOOLEAN_BASE_OFFSET, target.length);
	}

	public static final void copyRangeToArray(final long sourceAddress, final short[] target)
	{
		VM.copyMemory(null, sourceAddress, target, ARRAY_SHORT_BASE_OFFSET, target.length << BITS1);
	}

	public static final void copyRangeToArray(final long sourceAddress, final char[] target)
	{
		VM.copyMemory(null, sourceAddress, target, ARRAY_CHAR_BASE_OFFSET, target.length << BITS1);
	}
	
	public static final void copyRangeToArray(
		final long   sourceAddress,
		final char[] target       ,
		final int    targetIndex  ,
		final long   targetLength
	)
	{
		VM.copyMemory(null, sourceAddress, target, ARRAY_CHAR_BASE_OFFSET + (targetIndex << BITS1), targetLength << BITS1);
	}

	public static final void copyRangeToArray(final long sourceAddress, final int[] target)
	{
		VM.copyMemory(null, sourceAddress, target, ARRAY_INT_BASE_OFFSET, target.length << BITS2);
	}

	public static final void copyRangeToArray(final long sourceAddress, final float[] target)
	{
		VM.copyMemory(null, sourceAddress, target, ARRAY_FLOAT_BASE_OFFSET, target.length << BITS2);
	}

	public static final void copyRangeToArray(final long sourceAddress, final long[] target)
	{
		VM.copyMemory(null, sourceAddress, target, ARRAY_LONG_BASE_OFFSET, target.length << BITS3);
	}

	public static final void copyRangeToArray(final long sourceAddress, final double[] target)
	{
		VM.copyMemory(null, sourceAddress, target, ARRAY_DOUBLE_BASE_OFFSET, target.length << BITS3);
	}

	
	
	// copyArrayToAddress //

	public static final void copyArrayToAddress(final byte[] array, final long targetAddress)
	{
		VM.copyMemory(array, ARRAY_BYTE_BASE_OFFSET, null, targetAddress, array.length);
	}

	public static final void copyArrayToAddress(
		final byte[] array        ,
		final int    offset       ,
		final int    length       ,
		final long   targetAddress
	)
	{
		VM.copyMemory(array, ARRAY_BYTE_BASE_OFFSET + offset, null, targetAddress, length);
	}
	
	public static final void copyArrayToAddress(final boolean[] array, final long targetAddress)
	{
		VM.copyMemory(array, ARRAY_BOOLEAN_BASE_OFFSET, null, targetAddress, array.length);
	}

	public static final void copyArrayToAddress(
		final boolean[] array        ,
		final int       offset       ,
		final int       length       ,
		final long      targetAddress
	)
	{
		VM.copyMemory(array, ARRAY_BOOLEAN_BASE_OFFSET + offset, null, targetAddress, length);
	}
	
	public static final void copyArrayToAddress(final short[] array, final long targetAddress)
	{
		VM.copyMemory(array, ARRAY_SHORT_BASE_OFFSET, null, targetAddress, array.length << BITS1);
	}

	public static final void copyArrayToAddress(
		final short[] array        ,
		final int     offset       ,
		final int     length       ,
		final long    targetAddress
	)
	{
		VM.copyMemory(array, ARRAY_SHORT_BASE_OFFSET + (offset << BITS1), null, targetAddress, length << BITS1);
	}

	public static final void copyArrayToAddress(final char[] array, final long targetAddress)
	{
		VM.copyMemory(array, ARRAY_CHAR_BASE_OFFSET, null, targetAddress, array.length << BITS1);
	}
	
	public static final void copyArrayToAddress(
		final char[] array        ,
		final int    offset       ,
		final int    length       ,
		final long   targetAddress
	)
	{
		VM.copyMemory(array, ARRAY_CHAR_BASE_OFFSET + (offset << BITS1), null, targetAddress, length << BITS1);
	}
	
	public static final void copyArrayToAddress(final int[] array, final long targetAddress)
	{
		VM.copyMemory(array, ARRAY_INT_BASE_OFFSET, null, targetAddress, array.length << BITS2);
	}

	public static final void copyArrayToAddress(
		final int[] array        ,
		final int   offset       ,
		final int   length       ,
		final long  targetAddress
	)
	{
		VM.copyMemory(array, ARRAY_INT_BASE_OFFSET + (offset << BITS2), null, targetAddress, length << BITS2);
	}
	
	public static final void copyArrayToAddress(final float[] array, final long targetAddress)
	{
		VM.copyMemory(array, ARRAY_FLOAT_BASE_OFFSET, null, targetAddress, array.length << BITS2);
	}

	public static final void copyArrayToAddress(
		final float[] array        ,
		final int     offset       ,
		final int     length       ,
		final long    targetAddress
	)
	{
		VM.copyMemory(array, ARRAY_FLOAT_BASE_OFFSET + (offset << BITS2), null, targetAddress, length << BITS2);
	}
	
	public static final void copyArrayToAddress(final long[] array, final long targetAddress)
	{
		VM.copyMemory(array, ARRAY_LONG_BASE_OFFSET, null, targetAddress, array.length << BITS3);
	}

	public static final void copyArrayToAddress(
		final long[]   array        ,
		final int      offset       ,
		final int      length       ,
		final long     targetAddress
	)
	{
		VM.copyMemory(array, ARRAY_LONG_BASE_OFFSET + (offset << BITS3), null, targetAddress, length << BITS3);
	}
	
	public static final void copyArrayToAddress(final double[] array, final long targetAddress)
	{
		VM.copyMemory(array, ARRAY_DOUBLE_BASE_OFFSET, null, targetAddress, array.length << BITS3);
	}

	public static final void copyArrayToAddress(
		final double[] array        ,
		final int      offset       ,
		final int      length       ,
		final long     targetAddress
	)
	{
		VM.copyMemory(array, ARRAY_DOUBLE_BASE_OFFSET + (offset << BITS3), null, targetAddress, length << BITS3);
	}

	

	public static final byte get_byteFromBytes(final byte[] data, final int offset)
	{
		return VM.getByte(data, ARRAY_BYTE_BASE_OFFSET + offset);
	}

	public static final boolean get_booleanFromBytes(final byte[] data, final int offset)
	{
		return VM.getBoolean(data, ARRAY_BOOLEAN_BASE_OFFSET + offset);
	}

	public static final short get_shortFromBytes(final byte[] data, final int offset)
	{
		return VM.getShort(data, ARRAY_SHORT_BASE_OFFSET + offset);
	}

	public static final char get_charFromBytes(final byte[] data, final int offset)
	{
		return VM.getChar(data, ARRAY_CHAR_BASE_OFFSET + offset);
	}

	public static final int get_intFromBytes(final byte[] data, final int offset)
	{
		return VM.getInt(data, ARRAY_INT_BASE_OFFSET + offset);
	}

	public static final float get_floatFromBytes(final byte[] data, final int offset)
	{
		return VM.getFloat(data, ARRAY_FLOAT_BASE_OFFSET + offset);
	}

	public static final long get_longFromBytes(final byte[] data, final int offset)
	{
		return VM.getLong(data, ARRAY_LONG_BASE_OFFSET + offset);
	}

	public static final double get_doubleFromBytes(final byte[] data, final int offset)
	{
		return VM.getDouble(data, ARRAY_DOUBLE_BASE_OFFSET + offset);
	}

	public static final long allocate(final long bytes)
	{
		return VM.allocateMemory(bytes);
	}

	public static final long reallocate(final long address, final long bytes)
	{
		return VM.reallocateMemory(address, bytes);
	}

	public static final void fillRange(final long address, final long length, final byte value)
	{
		VM.setMemory(address, length, value);
	}

	public static final void free(final long address)
	{
		VM.freeMemory(address);
	}

	public static final boolean compareAndSwap_int(
		final Object subject    ,
		final long   offset     ,
		final int    expected   ,
		final int    replacement
	)
	{
		return VM.compareAndSwapInt(subject, offset, expected, replacement);
	}

	public static final boolean compareAndSwap_long(
		final Object subject    ,
		final long   offset     ,
		final long   expected   ,
		final long   replacement
	)
	{
		return VM.compareAndSwapLong(subject, offset, expected, replacement);
	}

	public static final boolean compareAndSwapObject(
		final Object subject    ,
		final long   offset     ,
		final Object expected   ,
		final Object replacement
	)
	{
		return VM.compareAndSwapObject(subject, offset, expected, replacement);
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
		VM.throwException(t);
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
		VM.ensureClassInitialized(c);
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


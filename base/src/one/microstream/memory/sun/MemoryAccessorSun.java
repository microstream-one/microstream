package one.microstream.memory.sun;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import one.microstream.X;
import one.microstream.exceptions.InstantiationRuntimeException;
import one.microstream.memory.MemoryAccessor;
import one.microstream.memory.XMemory;
import sun.misc.Unsafe;

public final class MemoryAccessorSun implements MemoryAccessor
{
	///////////////////////////////////////////////////////////////////////////
	// system access //
	//////////////////
	
	// used by other classes in other projects but same package, so do not change to private.
	static final Unsafe VM = getMemoryAccess();
	
	/*
	 * If magic values should be represented by constants and constants should be encapsulated by methods
	 * like instance fields should, then why use the code and memory detour of constants in the first place?
	 * Direct "Constant Methods" are the logical conclusion and they get jitted away, anyway.
	 */
	static final String fieldNameUnsafe()
	{
		return "theUnsafe";
	}
	
	public static final Unsafe getMemoryAccess()
	{
		// all that clumsy detour ... x_x
		if(XMemory.class.getClassLoader() == null)
		{
			return Unsafe.getUnsafe(); // Not on bootclasspath
		}
		try
		{
			final Field theUnsafe = Unsafe.class.getDeclaredField(fieldNameUnsafe());
			theUnsafe.setAccessible(true);
			return (Unsafe)theUnsafe.get(null); // static field, no argument needed, may be null (see #get JavaDoc)
		}
		catch(final Exception e)
		{
			throw new Error("Could not obtain access to \"" + fieldNameUnsafe() + "\"", e);
		}
	}
	
	

	///////////////////////////////////////////////////////////////////////////
	// constants //
	//////////////

	// better calculate it instead of making wild assumptions that can change (e.g. 64 bit coops has only 12 byte)
	private static final int BYTE_SIZE_OBJECT_HEADER = calculateByteSizeObjectHeader();

	// According to tests and investigation, memory alignment is always 8 bytes, even for 32 bit JVMs.
	// (04.07.2019 TM)NOTE: since these past investigations were naively JDK-specific, that is a dangerous assumption.
	private static final int
		MEMORY_ALIGNMENT_FACTOR =                           8,
		MEMORY_ALIGNMENT_MODULO = MEMORY_ALIGNMENT_FACTOR - 1,
		MEMORY_ALIGNMENT_MASK   = ~MEMORY_ALIGNMENT_MODULO
	;
	
	// constant names documenting that a value shall be shifted by n bits. Also to get CheckStyle off my back.
	private static final int
		BITS1 = 1,
		BITS2 = 2,
		BITS3 = 3
	;
	
	/*
	 * Rationale for these local constants:
	 * For Unsafe putting methods like Unsafe#putInt etc, there were two versions before Java 9:
	 * One with an int offset (deprecated) and one with a long offset.
	 * The base offset constants are ints, so they have to be casted for the compiler to select the corrent
	 * method option.
	 * However, in Java 9, the int variant disappeared (finally). That now causes an "unnecessary cast" warning.
	 * But removing it would mean in Java 8 and below, the int variant would be chosen and a deprecation warning would
	 * be displayed.
	 * So the only way to use those methods without warnings in either version is to have a constant that is
	 * naturally of type long.
	 */
	private static final long
		ARRAY_BYTE_BASE_OFFSET    = Unsafe.ARRAY_BYTE_BASE_OFFSET   ,
		ARRAY_BOOLEAN_BASE_OFFSET = Unsafe.ARRAY_BOOLEAN_BASE_OFFSET,
		ARRAY_SHORT_BASE_OFFSET   = Unsafe.ARRAY_SHORT_BASE_OFFSET  ,
		ARRAY_CHAR_BASE_OFFSET    = Unsafe.ARRAY_CHAR_BASE_OFFSET   ,
		ARRAY_INT_BASE_OFFSET     = Unsafe.ARRAY_INT_BASE_OFFSET    ,
		ARRAY_FLOAT_BASE_OFFSET   = Unsafe.ARRAY_FLOAT_BASE_OFFSET  ,
		ARRAY_LONG_BASE_OFFSET    = Unsafe.ARRAY_LONG_BASE_OFFSET   ,
		ARRAY_DOUBLE_BASE_OFFSET  = Unsafe.ARRAY_DOUBLE_BASE_OFFSET ,
		ARRAY_OBJECT_BASE_OFFSET  = Unsafe.ARRAY_OBJECT_BASE_OFFSET
	;
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////
	
	public static int staticPageSize()
	{
		return VM.pageSize();
	}
	
	public static Object staticGetStaticFieldBase(final Field field)
	{
		return VM.staticFieldBase(notNull(field)); // throws IllegalArgumentException, so no need to check here
	}
	
	public static final int staticByteSizeReference()
	{
		return Unsafe.ARRAY_OBJECT_INDEX_SCALE;
	}

	public static final int staticByteSizeObjectHeader()
	{
		return BYTE_SIZE_OBJECT_HEADER;
	}
	
	private static final int calculateByteSizeObjectHeader()
	{
		// min logic should be unnecessary but better exclude any source for potential errors
		long minOffset = Long.MAX_VALUE;
		final Field[] declaredFields = ObjectHeaderSizeDummy.class.getDeclaredFields();
		for(final Field field : declaredFields)
		{
			// just in case
			if(Modifier.isStatic(field.getModifiers()))
			{
				continue;
			}
			
			// requires the dummy field calculateByteSizeObjectHeaderFieldOffsetDummy
			if(VM.objectFieldOffset(field) < minOffset)
			{
				minOffset = VM.objectFieldOffset(field);
			}
		}
		if(minOffset == Long.MAX_VALUE)
		{
			throw new Error("Could not find object header dummy field in class " + ObjectHeaderSizeDummy.class);
		}
		
		return (int)minOffset; // offset of first instance field is guaranteed to be in int range ^^.
	}
	
	public static int staticByteSizeInstance(final Class<?> type)
	{
		if(type.isPrimitive())
		{
			throw new IllegalArgumentException();
		}
		if(type.isArray())
		{
			// instance byte size accounts only array header (object header plus length field plus overhead)
			return VM.arrayBaseOffset(type);
		}
		if(type == Object.class)
		{
			// required because Object's super class is null (see below)
			return staticByteSizeObjectHeader();
		}

		// declared fields suffice as all super class fields are positioned before them
		final Field[] declaredFields = type.getDeclaredFields();
		long maxInstanceFieldOffset = 0;
		Field maxInstanceField = null;
		for(int i = 0; i < declaredFields.length; i++)
		{
			if(Modifier.isStatic(declaredFields[i].getModifiers()))
			{
				continue;
			}
			final long fieldOffset = VM.objectFieldOffset(declaredFields[i]);
//			XDebug.debugln(fieldOffset + "\t" + declaredFields[i]);
			if(fieldOffset >= maxInstanceFieldOffset)
			{
				maxInstanceField = declaredFields[i];
				maxInstanceFieldOffset = fieldOffset;
			}
		}

		// no declared instance field at all, fall back to super class fields recursively
		if(maxInstanceField == null)
		{
			return staticByteSizeInstance(type.getSuperclass());
		}

		// memory alignment is a wild assumption at this point. Hopefully it will always be true. Otherwise it's a bug.
		return (int)alignAddress(maxInstanceFieldOffset + staticByteSizeFieldValue(maxInstanceField.getType()));
	}
	
	public static final long alignAddress(final long address)
	{
		if((address & MEMORY_ALIGNMENT_MODULO) == 0)
		{
			return address; // already aligned
		}
		// According to tests and investigation, memory alignment is always 8 bytes, even for 32 bit JVMs.
		return (address & MEMORY_ALIGNMENT_MASK) + MEMORY_ALIGNMENT_FACTOR;
	}
	
	public static long[] staticFieldOffsets(final Field[] fields)
	{
		final long[] offsets = new long[fields.length];
		for(int i = 0; i < fields.length; i++)
		{
			if(!Modifier.isStatic(fields[i].getModifiers()))
			{
				throw new IllegalArgumentException("Not a static field: " + fields[i]);
			}
			offsets[i] = (int)VM.staticFieldOffset(fields[i]);
		}
		return offsets;
	}
	
	public static final int staticByteSizeFieldValue(final Class<?> type)
	{
		return type.isPrimitive()
			? XMemory.byteSizePrimitive(type)
			: staticByteSizeReference()
		;
	}
	
	public static final long staticObjectFieldOffset(final Field field)
	{
		return VM.objectFieldOffset(field);
	}
	
	public static void staticEnsureClassInitialized(final Class<?> c)
	{
		VM.ensureClassInitialized(c);
	}
	
	public static void staticEnsureClassInitialized(final Class<?>... classes)
	{
		for(final Class<?> c : classes)
		{
			staticEnsureClassInitialized(c);
		}
	}
	
	@SuppressWarnings("unchecked")
	public static final <T> T staticInstantiateBlank(final Class<T> c) throws InstantiationRuntimeException
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
	
	
	
	public static final void staticFillMemory(final long address, final long length, final byte value)
	{
		VM.setMemory(address, length, value);
	}
	
	public static final long staticAllocateMemory(final long bytes)
	{
		return VM.allocateMemory(bytes);
	}

	public static final long staticReallocateMemory(final long address, final long bytes)
	{
		return VM.reallocateMemory(address, bytes);
	}

	public static final void staticFreeMemory(final long address)
	{
		VM.freeMemory(address);
	}

	public static final boolean staticCompareAndSwap_int(
		final Object subject    ,
		final long   offset     ,
		final int    expected   ,
		final int    replacement
	)
	{
		return VM.compareAndSwapInt(subject, offset, expected, replacement);
	}

	public static final boolean staticCompareAndSwap_long(
		final Object subject    ,
		final long   offset     ,
		final long   expected   ,
		final long   replacement
	)
	{
		return VM.compareAndSwapLong(subject, offset, expected, replacement);
	}

	public static final boolean staticCompareAndSwapObject(
		final Object subject    ,
		final long   offset     ,
		final Object expected   ,
		final Object replacement
	)
	{
		return VM.compareAndSwapObject(subject, offset, expected, replacement);
	}
	
	
	
	public static final byte staticGet_byte(final long address)
	{
		return VM.getByte(address);
	}

	public static final boolean staticGet_boolean(final long address)
	{
		return VM.getBoolean(null, address);
	}

	public static final short staticGet_short(final long address)
	{
		return VM.getShort(address);
	}

	public static final char staticGet_char(final long address)
	{
		return VM.getChar(address);
	}

	public static final int staticGet_int(final long address)
	{
		return VM.getInt(address);
	}

	public static final float staticGet_float(final long address)
	{
		return VM.getFloat(address);
	}

	public static final long staticGet_long(final long address)
	{
		return VM.getLong(address);
	}

	public static final double staticGet_double(final long address)
	{
		return VM.getDouble(address);
	}

	public static final Object staticGetObject(final long address)
	{
		return VM.getObject(null, address);
	}
	
	

	public static byte staticGet_byte(final Object instance, final long offset)
	{
		return VM.getByte(instance, offset);
	}

	public static boolean staticGet_boolean(final Object instance, final long offset)
	{
		return VM.getBoolean(instance, offset);
	}

	public static short staticGet_short(final Object instance, final long offset)
	{
		return VM.getShort(instance, offset);
	}

	public static char staticGet_char(final Object instance, final long offset)
	{
		return VM.getChar(instance, offset);
	}

	public static int staticGet_int(final Object instance, final long offset)
	{
		return VM.getInt(instance, offset);
	}

	public static float staticGet_float(final Object instance, final long offset)
	{
		return VM.getFloat(instance, offset);
	}

	public static long staticGet_long(final Object instance, final long offset)
	{
		return VM.getLong(instance, offset);
	}

	public static double staticGet_double(final Object instance, final long offset)
	{
		return VM.getDouble(instance, offset);
	}

	public static Object staticGetObject(final Object instance, final long offset)
	{
		return VM.getObject(instance, offset);
	}
	
	
	
	public static void staticSet_byte(final long address, final byte value)
	{
		VM.putByte(address, value);
	}

	public static void staticSet_boolean(final long address, final boolean value)
	{
		// where the heck is Unsafe#putBoolean(long, boolean)? Forgot to implement? Wtf?
		VM.putBoolean(null, address, value);
	}

	public static void staticSet_short(final long address, final short value)
	{
		VM.putShort(address, value);
	}

	public static void staticSet_char(final long address, final char value)
	{
		VM.putChar(address, value);
	}

	public static void staticSet_int(final long address, final int value)
	{
		VM.putInt(address, value);
	}

	public static void staticSet_float(final long address, final float value)
	{
		VM.putFloat(address, value);
	}

	public static void staticSet_long(final long address, final long value)
	{
		VM.putLong(address, value);
	}

	public static void staticSet_double(final long address, final double value)
	{
		VM.putDouble(address, value);
	}

	public static void staticSet_byte(final Object instance, final long offset, final byte value)
	{
		VM.putByte(instance, offset, value);
	}

	public static void staticSet_boolean(final Object instance, final long offset, final boolean value)
	{
		VM.putBoolean(instance, offset, value);
	}

	public static void staticSet_short(final Object instance, final long offset, final short value)
	{
		VM.putShort(instance, offset, value);
	}

	public static void staticSet_char(final Object instance, final long offset, final char value)
	{
		VM.putChar(instance, offset, value);
	}

	public static void staticSet_int(final Object instance, final long offset, final int value)
	{
		VM.putInt(instance, offset, value);
	}

	public static void staticSet_float(final Object instance, final long offset, final float value)
	{
		VM.putFloat(instance, offset, value);
	}

	public static void staticSet_long(final Object instance, final long offset, final long value)
	{
		VM.putLong(instance, offset, value);
	}

	public static void staticSet_double(final Object instance, final long offset, final double value)
	{
		VM.putDouble(instance, offset, value);
	}

	public static void staticSetObject(final Object instance, final long offset, final Object value)
	{
		VM.putObject(instance, offset, value);
	}
	
		
	
	public static final long staticByteSizeArray_byte(final long elementCount)
	{
		return ARRAY_BYTE_BASE_OFFSET + elementCount;
	}

	public static final long staticByteSizeArray_boolean(final long elementCount)
	{
		return ARRAY_BOOLEAN_BASE_OFFSET + elementCount;
	}

	public static final long staticByteSizeArray_short(final long elementCount)
	{
		return ARRAY_SHORT_BASE_OFFSET + (elementCount << BITS1);
	}

	public static final long staticByteSizeArray_char(final long elementCount)
	{
		return ARRAY_CHAR_BASE_OFFSET + (elementCount << BITS1);
	}

	public static final long staticByteSizeArray_int(final long elementCount)
	{
		return ARRAY_INT_BASE_OFFSET + (elementCount << BITS2);
	}

	public static final long staticByteSizeArray_float(final long elementCount)
	{
		return ARRAY_FLOAT_BASE_OFFSET + (elementCount << BITS2);
	}

	public static final long staticByteSizeArray_long(final long elementCount)
	{
		return ARRAY_LONG_BASE_OFFSET + (elementCount << BITS3);
	}

	public static final long staticByteSizeArray_double(final long elementCount)
	{
		return ARRAY_DOUBLE_BASE_OFFSET + (elementCount << BITS3);
	}

	public static final long staticByteSizeArrayObject(final long elementCount)
	{
		return ARRAY_OBJECT_BASE_OFFSET + elementCount * staticByteSizeReference();
	}
	
	
	
	public static byte[] staticAsByteArray(final long[] longArray)
	{
		final byte[] bytes = new byte[X.checkArrayRange((long)longArray.length << BITS3)];
		VM.copyMemory(longArray, ARRAY_LONG_BASE_OFFSET, bytes, ARRAY_BYTE_BASE_OFFSET, bytes.length);
		return bytes;
	}

	public static byte[] staticAsByteArray(final long value)
	{
		final byte[] bytes = new byte[XMemory.byteSize_long()];
		staticSet_long(bytes, 0, value);
		return bytes;
	}
	
	

	public static long static_longFromByteArray(final byte[] bytes)
	{
		return VM.getLong(bytes, ARRAY_BYTE_BASE_OFFSET);
	}
	
	
	
	public static void staticSet_byte(final byte[] bytes, final int index, final byte value)
	{
		VM.putByte(bytes, ARRAY_BYTE_BASE_OFFSET + index, value);
	}
	
	public static void staticSet_boolean(final byte[] bytes, final int index, final boolean value)
	{
		VM.putBoolean(bytes, ARRAY_BYTE_BASE_OFFSET + index, value);
	}

	public static void staticSet_short(final byte[] bytes, final int index, final short value)
	{
		VM.putShort(bytes, ARRAY_BYTE_BASE_OFFSET+ index, value);
	}

	public static void staticSet_char(final byte[] bytes, final int index, final char value)
	{
		VM.putChar(bytes, ARRAY_BYTE_BASE_OFFSET + index, value);
	}

	public static void staticSet_int(final byte[] bytes, final int index, final int value)
	{
		VM.putInt(bytes, ARRAY_BYTE_BASE_OFFSET + index, value);
	}

	public static void staticSet_float(final byte[] bytes, final int index, final float value)
	{
		VM.putFloat(bytes, ARRAY_BYTE_BASE_OFFSET + index, value);
	}

	public static void staticSet_long(final byte[] bytes, final int index, final long value)
	{
		VM.putLong(bytes, ARRAY_BYTE_BASE_OFFSET + index, value);
	}

	public static void staticSet_double(final byte[] bytes, final int index, final double value)
	{
		VM.putDouble(bytes, ARRAY_BYTE_BASE_OFFSET + index, value);
	}
	
	
	public static final void staticCopyRange(
		final long sourceAddress,
		final long targetAddress,
		final long length
	)
	{
		VM.copyMemory(sourceAddress, targetAddress, length);
	}

	public static final void staticCopyRange(
		final Object source      ,
		final long   sourceOffset,
		final Object target      ,
		final long   targetOffset,
		final long   length
	)
	{
		VM.copyMemory(source, sourceOffset, target, targetOffset, length);
	}
	
	
	public static final void staticCopyRangeToArray(final long sourceAddress, final byte[] target)
	{
		VM.copyMemory(null, sourceAddress, target, ARRAY_BYTE_BASE_OFFSET, target.length);
	}
	
	public static final void staticCopyRangeToArray(final long sourceAddress, final boolean[] target)
	{
		VM.copyMemory(null, sourceAddress, target, ARRAY_BOOLEAN_BASE_OFFSET, target.length);
	}

	public static final void staticCopyRangeToArray(final long sourceAddress, final short[] target)
	{
		VM.copyMemory(null, sourceAddress, target, ARRAY_SHORT_BASE_OFFSET, target.length << BITS1);
	}

	public static final void staticCopyRangeToArray(final long sourceAddress, final char[] target)
	{
		VM.copyMemory(null, sourceAddress, target, ARRAY_CHAR_BASE_OFFSET, target.length << BITS1);
	}
	
	public static final void staticCopyRangeToArray(final long sourceAddress, final int[] target)
	{
		VM.copyMemory(null, sourceAddress, target, ARRAY_INT_BASE_OFFSET, target.length << BITS2);
	}

	public static final void staticCopyRangeToArray(final long sourceAddress, final float[] target)
	{
		VM.copyMemory(null, sourceAddress, target, ARRAY_FLOAT_BASE_OFFSET, target.length << BITS2);
	}

	public static final void staticCopyRangeToArray(final long sourceAddress, final long[] target)
	{
		VM.copyMemory(null, sourceAddress, target, ARRAY_LONG_BASE_OFFSET, target.length << BITS3);
	}

	public static final void staticCopyRangeToArray(final long sourceAddress, final double[] target)
	{
		VM.copyMemory(null, sourceAddress, target, ARRAY_DOUBLE_BASE_OFFSET, target.length << BITS3);
	}
	
	
	
	public static final void staticCopyArrayToAddress(final byte[] array, final long targetAddress)
	{
		VM.copyMemory(array, ARRAY_BYTE_BASE_OFFSET, null, targetAddress, array.length);
	}
	
	public static final void staticCopyArrayToAddress(final boolean[] array, final long targetAddress)
	{
		VM.copyMemory(array, ARRAY_BOOLEAN_BASE_OFFSET, null, targetAddress, array.length);
	}
	
	public static final void staticCopyArrayToAddress(final short[] array, final long targetAddress)
	{
		VM.copyMemory(array, ARRAY_SHORT_BASE_OFFSET, null, targetAddress, array.length << BITS1);
	}

	public static final void staticCopyArrayToAddress(final char[] array, final long targetAddress)
	{
		VM.copyMemory(array, ARRAY_CHAR_BASE_OFFSET, null, targetAddress, array.length << BITS1);
	}
	
	public static final void staticCopyArrayToAddress(final int[] array, final long targetAddress)
	{
		VM.copyMemory(array, ARRAY_INT_BASE_OFFSET, null, targetAddress, array.length << BITS2);
	}
	
	public static final void staticCopyArrayToAddress(final float[] array, final long targetAddress)
	{
		VM.copyMemory(array, ARRAY_FLOAT_BASE_OFFSET, null, targetAddress, array.length << BITS2);
	}
	
	public static final void staticCopyArrayToAddress(final long[] array, final long targetAddress)
	{
		VM.copyMemory(array, ARRAY_LONG_BASE_OFFSET, null, targetAddress, array.length << BITS3);
	}
	
	public static final void staticCopyArrayToAddress(final double[] array, final long targetAddress)
	{
		VM.copyMemory(array, ARRAY_DOUBLE_BASE_OFFSET, null, targetAddress, array.length << BITS3);
	}
	
	public static final void staticThrowUnchecked(final Throwable t) // throws Throwable magic
	{
		// magic!
		VM.throwException(t);
	}
		
	
	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////
	
	public static MemoryAccessor New()
	{
		return new MemoryAccessorSun();
	}
	
	MemoryAccessorSun()
	{
		super();
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////
	
	@Override
	public final long objectFieldOffset(final Field field)
	{
		return staticObjectFieldOffset(field);
	}
	
	@Override
	public final int byteSizeReference()
	{
		return staticByteSizeReference();
	}
	
	@Override
	public final int pageSize()
	{
		return staticPageSize();
	}
		
	@Override
	public final int byteSizeObjectHeader(final Class<?> type)
	{
		return staticByteSizeObjectHeader();
	}

	@Override
	public final int byteSizeInstance(final Class<?> type)
	{
		return staticByteSizeInstance(type);
	}
	
	@Override
	public final int byteSizeFieldValue(final Class<?> type)
	{
		return staticByteSizeFieldValue(type);
	}
	
	@Override
	public final void ensureClassInitialized(final Class<?> c)
	{
		staticEnsureClassInitialized(c);
	}
	
	@Override
	public void ensureClassInitialized(final Class<?>... classes)
	{
		staticEnsureClassInitialized(classes);
	}
	
	@Override
	public final long allocateMemory(final long bytes)
	{
		return staticAllocateMemory(bytes);
	}

	@Override
	public final long reallocateMemory(final long address, final long bytes)
	{
		return staticReallocateMemory(address, bytes);
	}

	@Override
	public final void freeMemory(final long address)
	{
		staticFreeMemory(address);
	}

	@Override
	public final void fillMemory(final long address, final long length, final byte value)
	{
		staticFillMemory(address, length, value);
	}
	
	@Override
	public final <T> T instantiateBlank(final Class<T> c) throws InstantiationRuntimeException
	{
		return staticInstantiateBlank(c);
	}
	
	
	
	@Override
	public final byte get_byte(final long address)
	{
		return staticGet_byte(address);
	}

	@Override
	public final boolean get_boolean(final long address)
	{
		return staticGet_boolean(null, address);
	}

	@Override
	public final short get_short(final long address)
	{
		return staticGet_short(address);
	}

	@Override
	public final char get_char(final long address)
	{
		return staticGet_char(address);
	}

	@Override
	public final int get_int(final long address)
	{
		return staticGet_int(address);
	}

	@Override
	public final float get_float(final long address)
	{
		return staticGet_float(address);
	}

	@Override
	public final long get_long(final long address)
	{
		return staticGet_long(address);
	}

	@Override
	public final double get_double(final long address)
	{
		return staticGet_double(address);
	}
	
	
	
	@Override
	public final byte get_byte(final Object instance, final long offset)
	{
		return staticGet_byte(instance, offset);
	}

	@Override
	public final boolean get_boolean(final Object instance, final long offset)
	{
		return staticGet_boolean(instance, offset);
	}

	@Override
	public final short get_short(final Object instance, final long offset)
	{
		return staticGet_short(instance, offset);
	}

	@Override
	public final char get_char(final Object instance, final long offset)
	{
		return staticGet_char(instance, offset);
	}

	@Override
	public final int get_int(final Object instance, final long offset)
	{
		return staticGet_int(instance, offset);
	}

	@Override
	public final float get_float(final Object instance, final long offset)
	{
		return staticGet_float(instance, offset);
	}

	@Override
	public final long get_long(final Object instance, final long offset)
	{
		return staticGet_long(instance, offset);
	}

	@Override
	public final double get_double(final Object instance, final long offset)
	{
		return staticGet_double(instance, offset);
	}

	@Override
	public final Object getObject(final Object instance, final long offset)
	{
		return staticGetObject(instance, offset);
	}

	
	
	@Override
	public void set_byte(final long address, final byte value)
	{
		staticSet_byte(address, value);
	}

	@Override
	public void set_boolean(final long address, final boolean value)
	{
		// where the heck is Unsafe#putBoolean(long, boolean)? Forgot to implement? Wtf?
		staticSet_boolean(address, value);
	}

	@Override
	public void set_short(final long address, final short value)
	{
		staticSet_short(address, value);
	}

	@Override
	public void set_char(final long address, final char value)
	{
		staticSet_char(address, value);
	}

	@Override
	public void set_int(final long address, final int value)
	{
		staticSet_int(address, value);
	}

	@Override
	public void set_float(final long address, final float value)
	{
		staticSet_float(address, value);
	}

	@Override
	public void set_long(final long address, final long value)
	{
		staticSet_long(address, value);
	}

	@Override
	public void set_double(final long address, final double value)
	{
		staticSet_double(address, value);
	}
	
	// note: setting a pointer to a non-Object-relative address makes no sense.

	
	
	@Override
	public void set_byte(final Object instance, final long offset, final byte value)
	{
		staticSet_byte(instance, offset, value);
	}

	@Override
	public void set_boolean(final Object instance, final long offset, final boolean value)
	{
		staticSet_boolean(instance, offset, value);
	}

	@Override
	public void set_short(final Object instance, final long offset, final short value)
	{
		staticSet_short(instance, offset, value);
	}

	@Override
	public void set_char(final Object instance, final long offset, final char value)
	{
		staticSet_char(instance, offset, value);
	}

	@Override
	public void set_int(final Object instance, final long offset, final int value)
	{
		staticSet_int(instance, offset, value);
	}

	@Override
	public void set_float(final Object instance, final long offset, final float value)
	{
		staticSet_float(instance, offset, value);
	}

	@Override
	public void set_long(final Object instance, final long offset, final long value)
	{
		staticSet_long(instance, offset, value);
	}

	@Override
	public void set_double(final Object instance, final long offset, final double value)
	{
		staticSet_double(instance, offset, value);
	}

	@Override
	public void setObject(final Object instance, final long offset, final Object value)
	{
		staticSetObject(instance, offset, value);
	}
	
	
	
	@Override
	public final void copyRange(
		final long sourceAddress,
		final long targetAddress,
		final long length
	)
	{
		staticCopyRange(sourceAddress, targetAddress, length);
	}

	@Override
	public final void copyRange(
		final Object source      ,
		final long   sourceOffset,
		final Object target      ,
		final long   targetOffset,
		final long   length
	)
	{
		staticCopyRange(source, sourceOffset, target, targetOffset, length);
	}
	
	@Override
	public void copyRangeToArray(final long sourceAddress, final byte[] target)
	{
		staticCopyRangeToArray(sourceAddress, target);
	}
	
	@Override
	public void copyRangeToArray(final long sourceAddress, final boolean[] target)
	{
		staticCopyRangeToArray(sourceAddress, target);
	}

	@Override
	public void copyRangeToArray(final long sourceAddress, final short[] target)
	{
		staticCopyRangeToArray(sourceAddress, target);
	}

	@Override
	public void copyRangeToArray(final long sourceAddress, final char[] target)
	{
		staticCopyRangeToArray(sourceAddress, target);
	}
	
	@Override
	public void copyRangeToArray(final long sourceAddress, final int[] target)
	{
		staticCopyRangeToArray(sourceAddress, target);
	}

	@Override
	public void copyRangeToArray(final long sourceAddress, final float[] target)
	{
		staticCopyRangeToArray(sourceAddress, target);
	}

	@Override
	public void copyRangeToArray(final long sourceAddress, final long[] target)
	{
		staticCopyRangeToArray(sourceAddress, target);
	}

	@Override
	public void copyRangeToArray(final long sourceAddress, final double[] target)
	{
		staticCopyRangeToArray(sourceAddress, target);
	}
	
	
	
	@Override
	public void copyArrayToAddress(final byte[] array, final long targetAddress)
	{
		staticCopyArrayToAddress(array, targetAddress);
	}
	
	@Override
	public void copyArrayToAddress(final boolean[] array, final long targetAddress)
	{
		staticCopyArrayToAddress(array, targetAddress);
	}
	
	@Override
	public void copyArrayToAddress(final short[] array, final long targetAddress)
	{
		staticCopyArrayToAddress(array, targetAddress);
	}

	@Override
	public void copyArrayToAddress(final char[] array, final long targetAddress)
	{
		staticCopyArrayToAddress(array, targetAddress);
	}
	
	@Override
	public void copyArrayToAddress(final int[] array, final long targetAddress)
	{
		staticCopyArrayToAddress(array, targetAddress);
	}
	
	@Override
	public void copyArrayToAddress(final float[] array, final long targetAddress)
	{
		staticCopyArrayToAddress(array, targetAddress);
	}
	
	@Override
	public void copyArrayToAddress(final long[] array, final long targetAddress)
	{
		staticCopyArrayToAddress(array, targetAddress);
	}
	
	@Override
	public void copyArrayToAddress(final double[] array, final long targetAddress)
	{
		staticCopyArrayToAddress(array, targetAddress);
	}
	
	
	
	@Override
	public final long byteSizeArray_byte(final long elementCount)
	{
		return staticByteSizeArray_byte(elementCount);
	}

	@Override
	public final long byteSizeArray_boolean(final long elementCount)
	{
		return staticByteSizeArray_boolean(elementCount);
	}

	@Override
	public final long byteSizeArray_short(final long elementCount)
	{
		return staticByteSizeArray_short(elementCount);
	}

	@Override
	public final long byteSizeArray_char(final long elementCount)
	{
		return staticByteSizeArray_char(elementCount);
	}

	@Override
	public final long byteSizeArray_int(final long elementCount)
	{
		return staticByteSizeArray_int(elementCount);
	}

	@Override
	public final long byteSizeArray_float(final long elementCount)
	{
		return staticByteSizeArray_float(elementCount);
	}

	@Override
	public final long byteSizeArray_long(final long elementCount)
	{
		return staticByteSizeArray_long(elementCount);
	}

	@Override
	public final long byteSizeArray_double(final long elementCount)
	{
		return staticByteSizeArray_double(elementCount);
	}

	@Override
	public final long byteSizeArrayObject(final long elementCount)
	{
		return staticByteSizeArrayObject(elementCount);
	}
	
	
	
	@Override
	public final byte[] asByteArray(final long[] values)
	{
		return staticAsByteArray(values);
	}

	@Override
	public final byte[] asByteArray(final long value)
	{
		return staticAsByteArray(value);
	}
	
	
	
	@Override
	public final void set_byteInBytes(final byte[] bytes, final int index, final byte value)
	{
		staticSet_byte(bytes, index, value);
	}
	
	@Override
	public final void set_booleanInBytes(final byte[] bytes, final int index, final boolean value)
	{
		staticSet_boolean(bytes, index, value);
	}

	@Override
	public final void set_shortInBytes(final byte[] bytes, final int index, final short value)
	{
		staticSet_short(bytes, index, value);
	}

	@Override
	public final void set_charInBytes(final byte[] bytes, final int index, final char value)
	{
		staticSet_char(bytes, index, value);
	}

	@Override
	public final void set_intInBytes(final byte[] bytes, final int index, final int value)
	{
		staticSet_int(bytes, index, value);
	}

	@Override
	public final void set_floatInBytes(final byte[] bytes, final int index, final float value)
	{
		staticSet_float(bytes, index, value);
	}

	@Override
	public final void set_longInBytes(final byte[] bytes, final int index, final long value)
	{
		staticSet_long(bytes, index, value);
	}

	@Override
	public final void set_doubleInBytes(final byte[] bytes, final int index, final double value)
	{
		staticSet_double(bytes, index, value);
	}
	
	
	
	@Override
	public void throwUnchecked(final Throwable t)
	{
		staticThrowUnchecked(t);
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
		
	
	// extra class to keep MemoryAccessorSun instances stateless
	static final class ObjectHeaderSizeDummy
	{
		// implicitely used in #calculateByteSizeObjectHeader
		Object calculateByteSizeObjectHeaderFieldOffsetDummy;
	}
	
}

package one.microstream.memory;

import static one.microstream.X.notNull;

import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Properties;
import java.util.function.Predicate;

import one.microstream.X;
import one.microstream.exceptions.InstantiationRuntimeException;
import one.microstream.memory.android.MicroStreamAndroidAdapter;
import one.microstream.memory.sun.JdkMemoryAccessor;



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
	
	static MemoryAccessor       MEMORY_ACCESSOR         ;
	static MemoryAccessor       MEMORY_ACCESSOR_REVERSED;
	static MemorySizeProperties MEMORY_SIZE_PROPERTIES  ;
	
	static
	{
		initializeMemoryAccess();
	}
	
	private static VmInitializer[] createVmInitializers()
	{
		return X.array
		(
			// as defined by https://developer.android.com/reference/java/lang/System#getProperties()
			new VmInitializer("Android",
				properties ->
					"The Android Project".equals(properties.get("java.vendor")),
				() ->
					MicroStreamAndroidAdapter.setupFull()
			)
			
			// add additional initializers here
		);
	}
	
	private static void initializeMemoryAccess()
	{
		// no sense in permanently occupying memory with data that is only used exactly once during initialization
		final VmInitializer[] initializers = createVmInitializers();
		final Properties      properties   = System.getProperties();
		
		for(final VmInitializer initializer : initializers)
		{
			if(initializer.recognizer.test(properties))
			{
				initializer.action.run();
				return;
			}
		}
		
		/* (18.11.2019 TM)NOTE:
		 * If no specific initializer applied, the default initialization is used, assuming a fully
		 * JDK/-Unsafe-compatible JVM. It might not seem that way, but this is actually the normal case.
		 * Tests showed that almost all Java VM vendors fully support Unsafe. This is quite plausible:
		 * They want to draw Java developers/applications onto their platform, so they try to provide
		 * as much compatibility as possible, including Unsafe.
		 * So far, the only known Java VM to not fully support Unsafe is Android.
		 */
		setMemoryHandling(JdkMemoryAccessor.New());
	}

	static final class VmInitializer
	{
		final String                name      ;
		final Predicate<Properties> recognizer;
		final Runnable              action    ;
		
		VmInitializer(
			final String                name      ,
			final Predicate<Properties> recognizer,
			final Runnable              action
		)
		{
			super();
			this.name       = name      ;
			this.recognizer = recognizer;
			this.action     = action    ;
		}
	}
	
	public static final synchronized <H extends MemoryAccessor & MemorySizeProperties> void setMemoryHandling(
		final H memoryHandler
	)
	{
		setMemoryHandling(memoryHandler, memoryHandler);
	}
	
	public static final synchronized void setMemoryAccessor(
		final MemoryAccessor memoryAccessor
	)
	{
		setMemoryHandling(memoryAccessor, MemorySizeProperties.Unsupported());
	}

	public static final synchronized void setMemoryHandling(
		final MemoryAccessor       memoryAccessor      ,
		final MemorySizeProperties memorySizeProperties
	)
	{
		MEMORY_ACCESSOR          = notNull(memoryAccessor);
		MEMORY_ACCESSOR_REVERSED = notNull(memoryAccessor.toReversing());
		MEMORY_SIZE_PROPERTIES   = notNull(memorySizeProperties);
	}
	
	public static final synchronized MemoryAccessor memoryAccessor()
	{
		return MEMORY_ACCESSOR;
	}
	
	public static final synchronized MemoryAccessor memoryAccessorReversing()
	{
		return MEMORY_ACCESSOR_REVERSED;
	}
	
	public static final synchronized MemorySizeProperties memorySizeProperties()
	{
		return MEMORY_SIZE_PROPERTIES;
	}
	
	public static final void guaranteeUsability()
	{
		MEMORY_ACCESSOR.guaranteeUsability();
	}
	
	
	
	// direct byte buffer handling //
	
	public static final long getDirectByteBufferAddress(final ByteBuffer directBuffer)
	{
		return MEMORY_ACCESSOR.getDirectByteBufferAddress(directBuffer);
	}

	public static final void deallocateDirectByteBuffer(final ByteBuffer directBuffer)
	{
		MEMORY_ACCESSOR.deallocateDirectByteBuffer(directBuffer);
	}

	public static final boolean isDirectByteBuffer(final ByteBuffer byteBuffer)
	{
		return MEMORY_ACCESSOR.isDirectByteBuffer(byteBuffer);
	}

	public static final ByteBuffer guaranteeDirectByteBuffer(final ByteBuffer directBuffer)
	{
		return MEMORY_ACCESSOR.guaranteeDirectByteBuffer(directBuffer);
	}
	

	
	// memory allocation //
	
	public static final long allocate(final long bytes)
	{
		return MEMORY_ACCESSOR.allocateMemory(bytes);
	}

	public static final long reallocate(final long address, final long bytes)
	{
		return MEMORY_ACCESSOR.reallocateMemory(address, bytes);
	}

	public static final void free(final long address)
	{
		MEMORY_ACCESSOR.freeMemory(address);
	}

	public static final void fillMemory(final long address, final long length, final byte value)
	{
		MEMORY_ACCESSOR.fillMemory(address, length, value);
	}
	
	
	
	// memory size querying logic //
	
	/**
	 * Arbitrary value that coincidently matches most hardware's standard page
	 * sizes without being hard-tied to an actual pageSize system value.
	 * So this value is an educated guess and almost always a "good" value when
	 * paged-sized-ish buffer sizes are needed, while still not being at the
	 * mercy of an OS's JVM implementation.
	 * 
	 * @return a "good" value for a paged-sized-ish default buffer size.
	 */
	public static final int defaultBufferSize()
	{
		// source: https://en.wikipedia.org/wiki/Page_(computer_memory)
		return 4096;
	}
	
	public static final int pageSize()
	{
		return MEMORY_SIZE_PROPERTIES.pageSize();
	}
	
	public static final int byteSizeInstance(final Class<?> c)
	{
		return MEMORY_SIZE_PROPERTIES.byteSizeInstance(c);
	}
	
	public static final int byteSizeObjectHeader(final Class<?> c)
	{
		return MEMORY_SIZE_PROPERTIES.byteSizeObjectHeader(c);
	}
	
	public static final int byteSizeFieldValue(final Field field)
	{
		return MEMORY_SIZE_PROPERTIES.byteSizeFieldValue(field);
	}
	
	public static final int byteSizeFieldValue(final Class<?> type)
	{
		return MEMORY_SIZE_PROPERTIES.byteSizeFieldValue(type);
	}
	
	public static final long byteSizeArray_byte(final long elementCount)
	{
		return MEMORY_SIZE_PROPERTIES.byteSizeArray_byte(elementCount);
	}

	public static final long byteSizeArray_boolean(final long elementCount)
	{
		return MEMORY_SIZE_PROPERTIES.byteSizeArray_boolean(elementCount);
	}

	public static final long byteSizeArray_short(final long elementCount)
	{
		return MEMORY_SIZE_PROPERTIES.byteSizeArray_short(elementCount);
	}

	public static final long byteSizeArray_char(final long elementCount)
	{
		return MEMORY_SIZE_PROPERTIES.byteSizeArray_char(elementCount);
	}

	public static final long byteSizeArray_int(final long elementCount)
	{
		return MEMORY_SIZE_PROPERTIES.byteSizeArray_int(elementCount);
	}

	public static final long byteSizeArray_float(final long elementCount)
	{
		return MEMORY_SIZE_PROPERTIES.byteSizeArray_float(elementCount);
	}

	public static final long byteSizeArray_long(final long elementCount)
	{
		return MEMORY_SIZE_PROPERTIES.byteSizeArray_long(elementCount);
	}

	public static final long byteSizeArray_double(final long elementCount)
	{
		return MEMORY_SIZE_PROPERTIES.byteSizeArray_double(elementCount);
	}

	public static final long byteSizeArrayObject(final long elementCount)
	{
		return MEMORY_SIZE_PROPERTIES.byteSizeArrayObject(elementCount);
	}
	
	public static final int byteSizePrimitive(final Class<?> type)
	{
		// once again missing JDK functionality. Roughly ordered by probability.
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
				
		// intentionally covers void.class
		throw new IllegalArgumentException();
	}
	
	public static final int bitSize_byte()
	{
		return Byte.SIZE;
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
		return MEMORY_SIZE_PROPERTIES.byteSizeReference();
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
	
	

	// field offset abstraction //
	
	public static final long objectFieldOffset(final Field field)
	{
		return MEMORY_ACCESSOR.objectFieldOffset(field);
	}

	public static final long[] objectFieldOffsets(final Field[] fields)
	{
		return MEMORY_ACCESSOR.objectFieldOffsets(fields);
	}
	
	public static final long objectFieldOffset(final Class<?> c, final Field field)
	{
		return MEMORY_ACCESSOR.objectFieldOffset(c, field);
	}

	public static final long[] objectFieldOffsets(final Class<?> c, final Field[] fields)
	{
		return MEMORY_ACCESSOR.objectFieldOffsets(c, fields);
	}
		
	

	// address-based getters for primitive values //

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

	// note: getting a pointer from a non-Object-relative address makes no sense.

	
	
	// object-based getters for primitive values and references //
	
	public static final byte get_byte(final Object instance, final long offset)
	{
		return MEMORY_ACCESSOR.get_byte(instance, offset);
	}

	public static final boolean get_boolean(final Object instance, final long offset)
	{
		return MEMORY_ACCESSOR.get_boolean(instance, offset);
	}

	public static final short get_short(final Object instance, final long offset)
	{
		return MEMORY_ACCESSOR.get_short(instance, offset);
	}

	public static final char get_char(final Object instance, final long offset)
	{
		return MEMORY_ACCESSOR.get_char(instance, offset);
	}

	public static final int get_int(final Object instance, final long offset)
	{
		return MEMORY_ACCESSOR.get_int(instance, offset);
	}

	public static final float get_float(final Object instance, final long offset)
	{
		return MEMORY_ACCESSOR.get_float(instance, offset);
	}

	public static final long get_long(final Object instance, final long offset)
	{
		return MEMORY_ACCESSOR.get_long(instance, offset);
	}

	public static final double get_double(final Object instance, final long offset)
	{
		return MEMORY_ACCESSOR.get_double(instance, offset);
	}

	public static final Object getObject(final Object instance, final long offset)
	{
		return MEMORY_ACCESSOR.getObject(instance, offset);
	}
	
	

	// address-based setters for primitive values //

	public static final void set_byte(final long address, final byte value)
	{
		MEMORY_ACCESSOR.set_byte(address, value);
	}

	public static final void set_boolean(final long address, final boolean value)
	{
		// where the heck is Unsafe#putBoolean(long, boolean)? Forgot to implement? Wtf?
		MEMORY_ACCESSOR.set_boolean(address, value);
	}

	public static final void set_short(final long address, final short value)
	{
		MEMORY_ACCESSOR.set_short(address, value);
	}

	public static final void set_char(final long address, final char value)
	{
		MEMORY_ACCESSOR.set_char(address, value);
	}

	public static final void set_int(final long address, final int value)
	{
		MEMORY_ACCESSOR.set_int(address, value);
	}

	public static final void set_float(final long address, final float value)
	{
		MEMORY_ACCESSOR.set_float(address, value);
	}

	public static final void set_long(final long address, final long value)
	{
		MEMORY_ACCESSOR.set_long(address, value);
	}

	public static final void set_double(final long address, final double value)
	{
		MEMORY_ACCESSOR.set_double(address, value);
	}
	
	// note: setting a pointer to a non-Object-relative address makes no sense.
	
	

	// object-based setters for primitive values and references //

	public static final void set_byte(final Object instance, final long offset, final byte value)
	{
		MEMORY_ACCESSOR.set_byte(instance, offset, value);
	}

	public static final void set_boolean(final Object instance, final long offset, final boolean value)
	{
		MEMORY_ACCESSOR.set_boolean(instance, offset, value);
	}

	public static final void set_short(final Object instance, final long offset, final short value)
	{
		MEMORY_ACCESSOR.set_short(instance, offset, value);
	}

	public static final void set_char(final Object instance, final long offset, final char value)
	{
		MEMORY_ACCESSOR.set_char(instance, offset, value);
	}

	public static final void set_int(final Object instance, final long offset, final int value)
	{
		MEMORY_ACCESSOR.set_int(instance, offset, value);
	}

	public static final void set_float(final Object instance, final long offset, final float value)
	{
		MEMORY_ACCESSOR.set_float(instance, offset, value);
	}

	public static final void set_long(final Object instance, final long offset, final long value)
	{
		MEMORY_ACCESSOR.set_long(instance, offset, value);
	}

	public static final void set_double(final Object instance, final long offset, final double value)
	{
		MEMORY_ACCESSOR.set_double(instance, offset, value);
	}

	public static final void setObject(final Object instance, final long offset, final Object value)
	{
		MEMORY_ACCESSOR.setObject(instance, offset, value);
	}
	
	

	// transformative byte array primitive value setters //

	public static final void set_byteInBytes(final byte[] bytes, final int index, final byte value)
	{
		MEMORY_ACCESSOR.set_byteInBytes(bytes, index, value);
	}
	
	public static final void set_booleanInBytes(final byte[] bytes, final int index, final boolean value)
	{
		MEMORY_ACCESSOR.set_booleanInBytes(bytes, index, value);
	}

	public static final void set_shortInBytes(final byte[] bytes, final int index, final short value)
	{
		MEMORY_ACCESSOR.set_shortInBytes(bytes, index, value);
	}

	public static final void set_charInBytes(final byte[] bytes, final int index, final char value)
	{
		MEMORY_ACCESSOR.set_charInBytes(bytes, index, value);
	}

	public static final void set_intInBytes(final byte[] bytes, final int index, final int value)
	{
		MEMORY_ACCESSOR.set_intInBytes(bytes, index, value);
	}

	public static final void set_floatInBytes(final byte[] bytes, final int index, final float value)
	{
		MEMORY_ACCESSOR.set_floatInBytes(bytes, index, value);
	}

	public static final void set_longInBytes(final byte[] bytes, final int index, final long value)
	{
		MEMORY_ACCESSOR.set_longInBytes(bytes, index, value);
	}

	public static final void set_doubleInBytes(final byte[] bytes, final int index, final double value)
	{
		MEMORY_ACCESSOR.set_doubleInBytes(bytes, index, value);
	}

	

	// generic variable-length range copying //

	public static final void copyRange(final long sourceAddress, final long targetAddress, final long length)
	{
		MEMORY_ACCESSOR.copyRange(sourceAddress, targetAddress, length);
	}
	
	

	// address-to-array range copying //

	public static final void copyRangeToArray(final long sourceAddress, final byte[] target)
	{
		MEMORY_ACCESSOR.copyRangeToArray(sourceAddress, target);
	}
	
	public static final void copyRangeToArray(final long sourceAddress, final boolean[] target)
	{
		MEMORY_ACCESSOR.copyRangeToArray(sourceAddress, target);
	}

	public static final void copyRangeToArray(final long sourceAddress, final short[] target)
	{
		MEMORY_ACCESSOR.copyRangeToArray(sourceAddress, target);
	}

	public static final void copyRangeToArray(final long sourceAddress, final char[] target)
	{
		MEMORY_ACCESSOR.copyRangeToArray(sourceAddress, target);
	}
	
	public static final void copyRangeToArray(final long sourceAddress, final int[] target)
	{
		MEMORY_ACCESSOR.copyRangeToArray(sourceAddress, target);
	}

	public static final void copyRangeToArray(final long sourceAddress, final float[] target)
	{
		MEMORY_ACCESSOR.copyRangeToArray(sourceAddress, target);
	}

	public static final void copyRangeToArray(final long sourceAddress, final long[] target)
	{
		MEMORY_ACCESSOR.copyRangeToArray(sourceAddress, target);
	}

	public static final void copyRangeToArray(final long sourceAddress, final double[] target)
	{
		MEMORY_ACCESSOR.copyRangeToArray(sourceAddress, target);
	}
		
	

	// array-to-address range copying //

	public static final void copyArrayToAddress(final byte[] array, final long targetAddress)
	{
		MEMORY_ACCESSOR.copyArrayToAddress(array, targetAddress);
	}
	
	public static final void copyArrayToAddress(final boolean[] array, final long targetAddress)
	{
		MEMORY_ACCESSOR.copyArrayToAddress(array, targetAddress);
	}
	
	public static final void copyArrayToAddress(final short[] array, final long targetAddress)
	{
		MEMORY_ACCESSOR.copyArrayToAddress(array, targetAddress);
	}

	public static final void copyArrayToAddress(final char[] array, final long targetAddress)
	{
		MEMORY_ACCESSOR.copyArrayToAddress(array, targetAddress);
	}
	
	public static final void copyArrayToAddress(final int[] array, final long targetAddress)
	{
		MEMORY_ACCESSOR.copyArrayToAddress(array, targetAddress);
	}
	
	public static final void copyArrayToAddress(final float[] array, final long targetAddress)
	{
		MEMORY_ACCESSOR.copyArrayToAddress(array, targetAddress);
	}
	
	public static final void copyArrayToAddress(final long[] array, final long targetAddress)
	{
		MEMORY_ACCESSOR.copyArrayToAddress(array, targetAddress);
	}
	
	public static final void copyArrayToAddress(final double[] array, final long targetAddress)
	{
		MEMORY_ACCESSOR.copyArrayToAddress(array, targetAddress);
	}
	
	
	
	// conversion to byte array //

	public static final byte[] asByteArray(final long[] longArray)
	{
		return MEMORY_ACCESSOR.asByteArray(longArray);
	}

	public static final byte[] asByteArray(final long value)
	{
		return MEMORY_ACCESSOR.asByteArray(value);
	}
	


	// special system methods, not really memory-related //

	public static final void ensureClassInitialized(final Class<?> c)
	{
		MEMORY_ACCESSOR.ensureClassInitialized(c);
	}
	
	public static final void ensureClassInitialized(final Class<?> c, final Iterable<Field> usedFields)
	{
		MEMORY_ACCESSOR.ensureClassInitialized(c, usedFields);
	}
		
	public static final <T> T instantiateBlank(final Class<T> c) throws InstantiationRuntimeException
	{
		return MEMORY_ACCESSOR.instantiateBlank(c);
	}

	
	public static final ByteOrder nativeByteOrder()
	{
		return ByteOrder.nativeOrder();
	}
	
	public static final boolean isBigEndianNativeOrder()
	{
		return ByteOrder.nativeOrder() == ByteOrder.BIG_ENDIAN;
	}
	
	public static final boolean isLittleEndianNativeOrder()
	{
		return ByteOrder.nativeOrder() == ByteOrder.LITTLE_ENDIAN;
	}
	
	/**
	 * Parses a {@link String} instance to a {@link ByteOrder} instance according to {@code ByteOrder#toString()}
	 * or throws an {@link IllegalArgumentException} if the passed string does not match exactely one of the
	 * {@link ByteOrder} constant instances' string representation.
	 *
	 * @param byteOrder the string representing the {@link ByteOrder} instance to be parsed.
	 * @return the recognized {@link ByteOrder}
	 * @throws IllegalArgumentException if the string can't be recognized as a {@link ByteOrder} constant instance.
	 * @see ByteOrder#toString()
	 */
	// because they (he) couldn't have implemented that where it belongs.
	public static final ByteOrder parseByteOrder(final String name)
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
	
	/**
	 * Alias for {@code ByteBuffer.allocateDirect(capacity).order(ByteOrder.nativeOrder())}.
	 * See {@link ByteBuffer#allocateDirect(int)} for details.
	 * 
	 * @param capacity
	 *         The new buffer's capacity, in bytes
	 * 
	 * @return a newly created direct byte buffer with the specified capacity and the platform's native byte order.
	 * 
     * @throws IllegalArgumentException
     *         If the {@code capacity} is a negative integer.
     * 
	 * @see ByteBuffer#allocateDirect(int)
	 * @see ByteBuffer#order(ByteOrder)
	 */
	public static final ByteBuffer allocateDirectNative(final int capacity) throws IllegalArgumentException
	{
		return ByteBuffer
			.allocateDirect(capacity)
			.order(ByteOrder.nativeOrder())
		;
	}
	
	public static final ByteBuffer allocateDirectNative(final long capacity) throws IllegalArgumentException
	{
		return allocateDirectNative(
			X.checkArrayRange(capacity)
		);
	}
	
	public static final ByteBuffer allocateDirectNativeDefault()
	{
		return allocateDirectNative(XMemory.defaultBufferSize());
	}
	
	
	
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

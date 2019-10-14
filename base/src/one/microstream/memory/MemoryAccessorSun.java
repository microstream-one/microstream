package one.microstream.memory;

import static one.microstream.X.notNull;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import one.microstream.X;
import sun.misc.Unsafe;

public final class MemoryAccessorSun implements MemoryAccessor
{
	///////////////////////////////////////////////////////////////////////////
	// system access //
	//////////////////
	
	// used by other classes in other projects but same package
	static final Unsafe VM = (Unsafe)getSystemInstance();
	
	static final String fieldNameUnsafe()
	{
		return "theUnsafe";
	}
	
	// return type not specified to avoid public API dependencies to sun implementation details
	public static final Object getSystemInstance()
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
			return theUnsafe.get(null); // static field, no argument needed, may be null (see #get JavaDoc)
		}
		catch(final Exception e)
		{
			throw new Error("Could not obtain access to \"" + fieldNameUnsafe() + "\"", e);
		}
	}
	
	

	///////////////////////////////////////////////////////////////////////////
	// constants //
	//////////////

	// better calculate it once instead of making wild assumptions that can change (e.g. 64 bit coops has only 12 byte)
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
			

	
	public static Object staticGetStaticFieldBase(final Field field)
	{
		return VM.staticFieldBase(notNull(field)); // throws IllegalArgumentException, so no need to check here
	}
	
	public static final int staticByteSizeReference()
	{
		return Unsafe.ARRAY_OBJECT_INDEX_SCALE;
	}
	
	public static final void staticSet_long(final long address, final long value)
	{
		VM.putLong(address, value);
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
		put_long(bytes, 0, value);
		return bytes;
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////
	
	// no one knows why this method is called Sun ... shhhhh...
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
		return VM.objectFieldOffset(field);
	}
	
	@Override
	public final int byteSizeReference()
	{
		return staticByteSizeReference();
	}
	

	@Override
	public final void set_long(final long address, final long value)
	{
		VM.putLong(address, value);
	}


	public static final int byteSizeObjectHeader()
	{
		return BYTE_SIZE_OBJECT_HEADER;
	}

	private static final int calculateByteSizeObjectHeader()
	{
		// min logic should be unnecessary but better exclude any source for potential errors
		long minOffset = Long.MAX_VALUE;
		final Field[] declaredFields = XMemory.class.getDeclaredFields();
		for(final Field field : declaredFields)
		{
			if(Modifier.isStatic(field.getModifiers()))
			{
				continue;
			}
			if(VM.objectFieldOffset(field) < minOffset)
			{
				minOffset = VM.objectFieldOffset(field);
			}
		}
		if(minOffset == Long.MAX_VALUE)
		{
			throw new Error("Could not find object header dummy field in class " + XMemory.class);
		}
		return (int)minOffset; // offset of first instance field is guaranteed to be in int range ^^.
	}

	@Override
	public final int byteSizeInstance(final Class<?> type)
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
			return byteSizeObjectHeader();
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
			return this.byteSizeInstance(type.getSuperclass());
		}

		// memory alignment is a wild assumption at this point. Hopefully it will always be true. Otherwise it's a bug.
		return (int)alignAddress(maxInstanceFieldOffset + this.byteSizeFieldValue(maxInstanceField.getType()));
	}
	
	public static final int staticByteSizeFieldValue(final Class<?> type)
	{
		return type.isPrimitive()
			? XMemory.byteSizePrimitive(type)
			: staticByteSizeReference()
		;
	}
	
	@Override
	public final int byteSizeFieldValue(final Class<?> type)
	{
		return staticByteSizeFieldValue(type);
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
	
	
	@Override
	public long byteSizeArray_byte(final long elementCount)
	{
		return staticByteSizeArray_byte(elementCount);
	}

	@Override
	public long byteSizeArray_boolean(final long elementCount)
	{
		return staticByteSizeArray_boolean(elementCount);
	}

	@Override
	public long byteSizeArray_short(final long elementCount)
	{
		return staticByteSizeArray_short(elementCount);
	}

	@Override
	public long byteSizeArray_char(final long elementCount)
	{
		return staticByteSizeArray_char(elementCount);
	}

	@Override
	public long byteSizeArray_int(final long elementCount)
	{
		return staticByteSizeArray_int(elementCount);
	}

	@Override
	public long byteSizeArray_float(final long elementCount)
	{
		return staticByteSizeArray_float(elementCount);
	}

	@Override
	public long byteSizeArray_long(final long elementCount)
	{
		return staticByteSizeArray_long(elementCount);
	}

	@Override
	public long byteSizeArray_double(final long elementCount)
	{
		return staticByteSizeArray_double(elementCount);
	}

	@Override
	public long byteSizeArrayObject(final long elementCount)
	{
		return staticByteSizeArrayObject(elementCount);
	}
		
}

package one.microstream.memory;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import one.microstream.collections.BulkList;
import one.microstream.collections.HashTable;
import one.microstream.collections.XArrays;
import one.microstream.exceptions.InstantiationRuntimeException;
import one.microstream.reflect.XReflect;

public final class MemoryAccessorGeneric implements MemoryAccessor
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////
	
	private final HashTable<Class<?>, Field[]> objectFieldsRegistry = HashTable.New();
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// API methods //
	////////////////

	// memory allocation //
	
	@Override
	public final long allocateMemory(final long bytes)
	{
		
	}

	@Override
	public final long reallocateMemory(final long address, final long bytes)
	{
		
	}

	@Override
	public final void freeMemory(final long address)
	{
		
	}
	
	@Override
	public final void fillMemory(final long address, final long length, final byte value)
	{
		
	}
	
	
	
	// memory size querying logic //
	
	// (29.10.2019 TM)FIXME: priv#111: not sure if these really belong in a memory accessor logic
	
	@Override
	public final int pageSize()
	{
		
	}
		
	@Override
	public final int byteSizeReference()
	{
		
	}
	
	@Override
	public final int byteSizeInstance(final Class<?> type)
	{
		
	}
	
	@Override
	public final int byteSizeObjectHeader(final Class<?> type)
	{
		
	}

	@Override
	public final int byteSizeFieldValue(final Class<?> type)
	{
		
	}
	
	@Override
	public final long byteSizeArray_byte(final long elementCount)
	{
		
	}

	@Override
	public final long byteSizeArray_boolean(final long elementCount)
	{
		
	}

	@Override
	public final long byteSizeArray_short(final long elementCount)
	{
		
	}

	@Override
	public final long byteSizeArray_char(final long elementCount)
	{
		
	}

	@Override
	public final long byteSizeArray_int(final long elementCount)
	{
		
	}

	@Override
	public final long byteSizeArray_float(final long elementCount)
	{
		
	}

	@Override
	public final long byteSizeArray_long(final long elementCount)
	{
		
	}

	@Override
	public final long byteSizeArray_double(final long elementCount)
	{
		
	}

	@Override
	public final long byteSizeArrayObject(final long elementCount)
	{
		
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
	@Override
	public final synchronized long objectFieldOffset(final Field field)
	{
		return this.objectFieldOffset(field.getDeclaringClass(), field);
	}
	
	public static final Class<?> determineMostSpecificClass(final Field[] fields)
	{
		if(XArrays.hasNoContent(fields))
		{
			return null;
		}
		
		Class<?> c = fields[0].getDeclaringClass();
		for(int i = 1; i < fields.length; i++)
		{
			// if the current declaring class is not c, but c is a super class, then the current must be more specific.
			if(fields[i].getDeclaringClass() != c && c.isAssignableFrom(fields[i].getDeclaringClass()))
			{
				c = fields[i].getDeclaringClass();
			}
		}
		
		// at this point, c point to the most specific ("most childish"? :D) class of all fields' declaring classes.
		return c;
	}
	
	/**
	 * Array alias vor #objectFieldOffset(Field).
	 */
	@Override
	public final synchronized long[] objectFieldOffsets(final Field... fields)
	{
		final Class<?> mostSpecificClass = determineMostSpecificClass(fields);
		
		return this.objectFieldOffsets(mostSpecificClass, fields);
	}

	@Override
	public final synchronized long objectFieldOffset(final Class<?> objectClass, final Field field)
	{
		final Field[] objectFields = this.ensureRegisteredObjectFields(objectClass);

		return objectFieldOffset(objectFields, field);
	}
	
	private Field[] ensureRegisteredObjectFields(final Class<?> objectClass)
	{
		final Field[] objectFields = this.objectFieldsRegistry.get(objectClass);
		if(objectFields != null)
		{
			return objectFields;
		}
		
		return this.registerObjectFields(objectClass);
	}
	
	private Field[] registerObjectFields(final Class<?> objectClass)
	{
		/*
		 * Note on algorithm:
		 * Each class in a class hierarchy gets its own registry entry, even if that means redundancy.
		 * This is necessary to make the offset-to-field lookup quick
		 */
		
		final BulkList<Field> objectFields = BulkList.New(20);
		XReflect.iterateDeclaredFieldsUpwards(objectClass, field ->
		{
			// non-instance fields are always discarded
			if(!XReflect.isInstanceField(field))
			{
				return;
			}
			
			objectFields.add(field);
		});
		
		final Field[] array = XArrays.reverse(objectFields.toArray(Field.class));
		
		if(!this.objectFieldsRegistry.add(objectClass, array))
		{
			// (29.10.2019 TM)EXCP: proper exception
			throw new RuntimeException("Object fields already registered for " + objectClass);
		}
		
		return array;
	}
	
	final static long objectFieldOffset(final Field[] objectFields, final Field field)
	{
		final Class<?> declaringClass = field.getDeclaringClass();
		final String   fieldName      = field.getName();
		
		for(int i = 0; i < objectFields.length; i++)
		{
			if(objectFields[i].getDeclaringClass() == declaringClass && objectFields[i].getName().equals(fieldName))
			{
				return i;
			}
		}
		
		// (29.10.2019 TM)EXCP: proper exception
		throw new RuntimeException(
			"Inconsistent object fields registration for " + declaringClass.getName() + "#" + fieldName
		);
	}
	
	@Override
	public final synchronized long[] objectFieldOffsets(final Class<?> objectClass, final Field... fields)
	{
		final Field[] objectFields = this.ensureRegisteredObjectFields(objectClass);

		final long[] offsets = new long[fields.length];
		for(int i = 0; i < fields.length; i++)
		{
			if(Modifier.isStatic(fields[i].getModifiers()))
			{
				throw new IllegalArgumentException("Not an object field: " + fields[i]);
			}
			offsets[i] = objectFieldOffset(objectFields, fields[i]);
		}
		
		return offsets;
	}

	
	
	// address-based getters for primitive values //
	
	@Override
	public final byte get_byte(final long address)
	{
		
	}

	@Override
	public final boolean get_boolean(final long address)
	{
		
	}

	@Override
	public final short get_short(final long address)
	{
		
	}

	@Override
	public final char get_char(final long address)
	{
		
	}

	@Override
	public final int get_int(final long address)
	{
		
	}

	@Override
	public final float get_float(final long address)
	{
		
	}

	@Override
	public final long get_long(final long address)
	{
		
	}

	@Override
	public final double get_double(final long address)
	{
		
	}

	// note: getting a pointer from a non-Object-relative address makes no sense.
	
	
	
	// object-based getters for primitive values and references //
	
	@Override
	public final byte get_byte(final Object instance, final long offset)
	{
		
	}

	@Override
	public final boolean get_boolean(final Object instance, final long offset)
	{
		
	}

	@Override
	public final short get_short(final Object instance, final long offset)
	{
		
	}

	@Override
	public final char get_char(final Object instance, final long offset)
	{
		
	}

	@Override
	public final int get_int(final Object instance, final long offset)
	{
		
	}

	@Override
	public final float get_float(final Object instance, final long offset)
	{
		
	}

	@Override
	public final long get_long(final Object instance, final long offset)
	{
		
	}

	@Override
	public final double get_double(final Object instance, final long offset)
	{
		
	}

	@Override
	public final Object getObject(final Object instance, final long offset)
	{
		
	}
	
	
	
	// address-based setters for primitive values //
	
	@Override
	public final void set_byte(final long address, final byte value)
	{
		
	}

	@Override
	public final void set_boolean(final long address, final boolean value)
	{
		
	}

	@Override
	public final void set_short(final long address, final short value)
	{
		
	}

	@Override
	public final void set_char(final long address, final char value)
	{
		
	}

	@Override
	public final void set_int(final long address, final int value)
	{
		
	}

	@Override
	public final void set_float(final long address, final float value)
	{
		
	}

	@Override
	public final void set_long(final long address, final long value)
	{
		
	}

	@Override
	public final void set_double(final long address, final double value)
	{
		
	}

	// note: setting a pointer to a non-Object-relative address makes no sense.
	
	
	// object-based setters for primitive values and references //
	
	@Override
	public final void set_byte(final Object instance, final long offset, final byte value)
	{
		
	}

	@Override
	public final void set_boolean(final Object instance, final long offset, final boolean value)
	{
		
	}

	@Override
	public final void set_short(final Object instance, final long offset, final short value)
	{
		
	}

	@Override
	public final void set_char(final Object instance, final long offset, final char value)
	{
		
	}

	@Override
	public final void set_int(final Object instance, final long offset, final int value)
	{
		
	}

	@Override
	public final void set_float(final Object instance, final long offset, final float value)
	{
		
	}

	@Override
	public final void set_long(final Object instance, final long offset, final long value)
	{
		
	}

	@Override
	public final void set_double(final Object instance, final long offset, final double value)
	{
		
	}

	@Override
	public final void setObject(final Object instance, final long offset, final Object value)
	{
		
	}

		
	
	// transformative byte array primitive value setters //
	
	@Override
	public final void set_byteInBytes(final byte[] bytes, final int index, final byte value)
	{
		
	}
	
	@Override
	public final void set_booleanInBytes(final byte[] bytes, final int index, final boolean value)
	{
		
	}

	@Override
	public final void set_shortInBytes(final byte[] bytes, final int index, final short value)
	{
		
	}

	@Override
	public final void set_charInBytes(final byte[] bytes, final int index, final char value)
	{
		
	}

	@Override
	public final void set_intInBytes(final byte[] bytes, final int index, final int value)
	{
		
	}

	@Override
	public final void set_floatInBytes(final byte[] bytes, final int index, final float value)
	{
		
	}

	@Override
	public final void set_longInBytes(final byte[] bytes, final int index, final long value)
	{
		
	}

	@Override
	public final void set_doubleInBytes(final byte[] bytes, final int index, final double value)
	{
		
	}

	
	
	// generic variable-length range copying //
	
	@Override
	public final void copyRange(final long sourceAddress, final long targetAddress, final long length)
	{
		
	}

	@Override
	public final void copyRange(final Object source, final long sourceOffset, final Object target, final long targetOffset, final long length)
	{
		
	}

	
	
	// address-to-array range copying //
	
	@Override
	public final void copyRangeToArray(final long sourceAddress, final byte[] target)
	{
		
	}
	
	@Override
	public final void copyRangeToArray(final long sourceAddress, final boolean[] target)
	{
		
	}

	@Override
	public final void copyRangeToArray(final long sourceAddress, final short[] target)
	{
		
	}

	@Override
	public final void copyRangeToArray(final long sourceAddress, final char[] target)
	{
		
	}
	
	@Override
	public final void copyRangeToArray(final long sourceAddress, final int[] target)
	{
		
	}

	@Override
	public final void copyRangeToArray(final long sourceAddress, final float[] target)
	{
		
	}

	@Override
	public final void copyRangeToArray(final long sourceAddress, final long[] target)
	{
		
	}

	@Override
	public final void copyRangeToArray(final long sourceAddress, final double[] target)
	{
		
	}

	
	
	// array-to-address range copying //
	
	@Override
	public final void copyArrayToAddress(final byte[] array, final long targetAddress)
	{
		
	}
	
	@Override
	public final void copyArrayToAddress(final boolean[] array, final long targetAddress)
	{
		
	}
	
	@Override
	public final void copyArrayToAddress(final short[] array, final long targetAddress)
	{
		
	}

	@Override
	public final void copyArrayToAddress(final char[] array, final long targetAddress)
	{
		
	}
	
	@Override
	public final void copyArrayToAddress(final int[] array, final long targetAddress)
	{
		
	}
	
	@Override
	public final void copyArrayToAddress(final float[] array, final long targetAddress)
	{
		
	}
	
	@Override
	public final void copyArrayToAddress(final long[] array, final long targetAddress)
	{
		
	}
	
	@Override
	public final void copyArrayToAddress(final double[] array, final long targetAddress)
	{
		
	}
		
	
	
	// conversion to byte array //
	
	@Override
	public final byte[] asByteArray(final long[] values)
	{
		
	}

	@Override
	public final byte[] asByteArray(final long value)
	{
		// (29.10.2019 TM)FIXME: priv#111: is this the right byte order? Does it depend?
		final byte[] array = new byte[Long.BYTES];
		
		for(int i = 0; i < array.length; i++)
		{
			array[i] = (byte)(value >> 8*i & 0xFFL);
		}
		
		return array;
	}
	
	

	// special system methods, not really memory-related //
	
	@Override
	public final void ensureClassInitialized(final Class<?> c)
	{
		
	}
	
	@Override
	public final void ensureClassInitialized(final Class<?>... classes)
	{
		for(final Class<?> c : classes)
		{
			this.ensureClassInitialized(c);
		}
	}
	
	@Override
	public final <T> T instantiateBlank(final Class<T> c) throws InstantiationRuntimeException
	{
		
	}

	@Override
	public final void throwUnchecked(final Throwable t)
	{
		
	}
}

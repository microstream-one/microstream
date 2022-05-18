package one.microstream.memory;

/*-
 * #%L
 * microstream-base
 * %%
 * Copyright (C) 2019 - 2022 MicroStream Software
 * %%
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 * 
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the Eclipse
 * Public License, v. 2.0 are satisfied: GNU General Public License, version 2
 * with the GNU Classpath Exception which is
 * available at https://www.gnu.org/software/classpath/license.html.
 * 
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 * #L%
 */

import java.lang.reflect.Field;
import java.nio.ByteBuffer;

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
	
	@Override
	public final void guaranteeUsability()
	{
		this.actual.guaranteeUsability();
	}
	
	
	
	// direct byte buffer handling //

	@Override
	public final long getDirectByteBufferAddress(final ByteBuffer directBuffer)
	{
		return this.actual.getDirectByteBufferAddress(directBuffer);
	}

	@Override
	public final boolean deallocateDirectByteBuffer(final ByteBuffer directBuffer)
	{
		return this.actual.deallocateDirectByteBuffer(directBuffer);
	}

	@Override
	public final boolean isDirectByteBuffer(final ByteBuffer byteBuffer)
	{
		return this.actual.isDirectByteBuffer(byteBuffer);
	}

	@Override
	public final ByteBuffer guaranteeDirectByteBuffer(final ByteBuffer directBuffer)
	{
		return this.actual.guaranteeDirectByteBuffer(directBuffer);
	}
	
	
	
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
		// tricky: must read the reversed bytes as a long, reverse them to form a valid double and then transform it.
		return Double.longBitsToDouble(this.get_long(address));
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
		// tricky: must read the reversed bytes as a long, reverse them to form a valid double and then transform it.
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
	public final void copyRange(final long sourceAddress, final long targetAddress, final long length)
	{
		this.actual.copyRange(sourceAddress, targetAddress, length);
	}

	
	
	// address-to-array range copying //
	
	@Override
	public final void copyRangeToArray(final long sourceAddress, final byte[] target)
	{
		this.actual.copyRangeToArray(sourceAddress, target);
	}
	
	@Override
	public final void copyRangeToArray(final long sourceAddress, final boolean[] target)
	{
		this.actual.copyRangeToArray(sourceAddress, target);
	}

	@Override
	public final void copyRangeToArray(final long sourceAddress, final short[] target)
	{
		this.actual.copyRangeToArray(sourceAddress, target);
		
		for(int i = 0; i < target.length; i++)
		{
			target[i] = Short.reverseBytes(target[i]);
		}
	}

	@Override
	public final void copyRangeToArray(final long sourceAddress, final char[] target)
	{
		this.actual.copyRangeToArray(sourceAddress, target);
		
		for(int i = 0; i < target.length; i++)
		{
			target[i] = Character.reverseBytes(target[i]);
		}
	}
	
	@Override
	public final void copyRangeToArray(final long sourceAddress, final int[] target)
	{
		this.actual.copyRangeToArray(sourceAddress, target);
		
		for(int i = 0; i < target.length; i++)
		{
			target[i] = Integer.reverseBytes(target[i]);
		}
	}

	@Override
	public final void copyRangeToArray(final long sourceAddress, final float[] target)
	{
		this.actual.copyRangeToArray(sourceAddress, target);
		
		for(int i = 0; i < target.length; i++)
		{
			target[i] = Float.intBitsToFloat(Integer.reverseBytes(Float.floatToRawIntBits(target[i])));
		}
	}

	@Override
	public final void copyRangeToArray(final long sourceAddress, final long[] target)
	{
		this.actual.copyRangeToArray(sourceAddress, target);
		
		for(int i = 0; i < target.length; i++)
		{
			target[i] = Long.reverseBytes(target[i]);
		}
	}

	@Override
	public final void copyRangeToArray(final long sourceAddress, final double[] target)
	{
		this.actual.copyRangeToArray(sourceAddress, target);
		
		for(int i = 0; i < target.length; i++)
		{
			target[i] = Double.longBitsToDouble(Long.reverseBytes(Double.doubleToRawLongBits(target[i])));
		}
	}

	
	
	// array-to-address range copying //
	
	@Override
	public final void copyArrayToAddress(final byte[] array, final long targetAddress)
	{
		this.actual.copyArrayToAddress(array, targetAddress);
	}
	
	@Override
	public final void copyArrayToAddress(final boolean[] array, final long targetAddress)
	{
		this.actual.copyArrayToAddress(array, targetAddress);
	}
	
	@Override
	public final void copyArrayToAddress(final short[] array, final long targetAddress)
	{
		final short[] byteReversedArray = new short[array.length];
		for(int i = 0; i < array.length; i++)
		{
			byteReversedArray[i] = Short.reverseBytes(array[i]);
		}
		
		this.actual.copyArrayToAddress(byteReversedArray, targetAddress);
	}

	@Override
	public final void copyArrayToAddress(final char[] array, final long targetAddress)
	{
		final char[] byteReversedArray = new char[array.length];
		for(int i = 0; i < array.length; i++)
		{
			byteReversedArray[i] = Character.reverseBytes(array[i]);
		}
		
		this.actual.copyArrayToAddress(byteReversedArray, targetAddress);
	}
	
	@Override
	public final void copyArrayToAddress(final int[] array, final long targetAddress)
	{
		final int[] byteReversedArray = new int[array.length];
		for(int i = 0; i < array.length; i++)
		{
			byteReversedArray[i] = Integer.reverseBytes(array[i]);
		}
		
		this.actual.copyArrayToAddress(byteReversedArray, targetAddress);
	}
	
	@Override
	public final void copyArrayToAddress(final float[] array, final long targetAddress)
	{
		final int[] byteReversedArray = new int[array.length];
		for(int i = 0; i < array.length; i++)
		{
			// both operations in one step to avoid
			byteReversedArray[i] = Integer.reverseBytes(Float.floatToRawIntBits(array[i]));
		}
		
		this.actual.copyArrayToAddress(byteReversedArray, targetAddress);
	}
	
	@Override
	public final void copyArrayToAddress(final long[] array, final long targetAddress)
	{
		final long[] byteReversedArray = new long[array.length];
		for(int i = 0; i < array.length; i++)
		{
			byteReversedArray[i] = Long.reverseBytes(array[i]);
		}
		
		this.actual.copyArrayToAddress(byteReversedArray, targetAddress);
	}
	
	@Override
	public final void copyArrayToAddress(final double[] array, final long targetAddress)
	{
		final long[] byteReversedArray = new long[array.length];
		for(int i = 0; i < array.length; i++)
		{
			// both operations in one step to avoid
			byteReversedArray[i] = Long.reverseBytes(Double.doubleToRawLongBits(array[i]));
		}
		
		this.actual.copyArrayToAddress(byteReversedArray, targetAddress);
	}

	
	
	// transformative byte array primitive value setters //
	
	@Override
	public final void set_byteInBytes(final byte[] bytes, final int index, final byte value)
	{
		this.actual.set_byteInBytes(bytes, index, value);
	}
	
	@Override
	public final void set_booleanInBytes(final byte[] bytes, final int index, final boolean value)
	{
		this.actual.set_booleanInBytes(bytes, index, value);
	}

	@Override
	public final void set_shortInBytes(final byte[] bytes, final int index, final short value)
	{
		this.actual.set_shortInBytes(bytes, index, Short.reverseBytes(value));
	}

	@Override
	public final void set_charInBytes(final byte[] bytes, final int index, final char value)
	{
		this.actual.set_charInBytes(bytes, index, Character.reverseBytes(value));
	}

	@Override
	public final void set_intInBytes(final byte[] bytes, final int index, final int value)
	{
		this.actual.set_intInBytes(bytes, index, Integer.reverseBytes(value));
	}

	@Override
	public final void set_floatInBytes(final byte[] bytes, final int index, final float value)
	{
		this.set_intInBytes(bytes, index, Float.floatToRawIntBits(value));
	}

	@Override
	public final void set_longInBytes(final byte[] bytes, final int index, final long value)
	{
		this.actual.set_longInBytes(bytes, index, Long.reverseBytes(value));
	}

	@Override
	public final void set_doubleInBytes(final byte[] bytes, final int index, final double value)
	{
		this.set_longInBytes(bytes, index, Double.doubleToRawLongBits(value));
	}
		
	
	
	// conversion to byte array //
	
	@Override
	public final byte[] asByteArray(final long[] values)
	{
		final long[] byteReversedArray = new long[values.length];
		for(int i = 0; i < values.length; i++)
		{
			byteReversedArray[i] = Long.reverseBytes(values[i]);
		}
		
		return this.actual.asByteArray(byteReversedArray);
	}

	@Override
	public final byte[] asByteArray(final long value)
	{
		return this.actual.asByteArray(Long.reverseBytes(value));
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
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public final long[] objectFieldOffsets(final Field... fields)
	{
		return this.actual.objectFieldOffsets(fields);
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public final long objectFieldOffset(final Class<?> objectClass, final Field field)
	{
		return this.actual.objectFieldOffset(objectClass, field);
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public final long[] objectFieldOffsets(final Class<?> objectClass, final Field... fields)
	{
		return this.actual.objectFieldOffsets(objectClass, fields);
	}
	
	

	// special system methods, not really memory-related, but needed //
	
	@Override
	public final void ensureClassInitialized(final Class<?> c)
	{
		this.actual.ensureClassInitialized(c);
	}
	
	@Override
	public void ensureClassInitialized(final Class<?> c, final Iterable<Field> usedFields)
	{
		this.actual.ensureClassInitialized(c, usedFields);
	}
	
	@Override
	public final <T> T instantiateBlank(final Class<T> c) throws InstantiationRuntimeException
	{
		return this.actual.instantiateBlank(c);
	}
	
	
	// memory statistics creation //
	
	@Override
	public MemoryStatistics createHeapMemoryStatistics()
	{
		return this.actual.createHeapMemoryStatistics();
	}
	
	@Override
	public MemoryStatistics createNonHeapMemoryStatistics()
	{
		return this.actual.createNonHeapMemoryStatistics();
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

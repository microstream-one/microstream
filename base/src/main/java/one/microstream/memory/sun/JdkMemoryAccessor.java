package one.microstream.memory.sun;

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
import one.microstream.memory.MemoryAccessor;
import one.microstream.memory.MemorySizeProperties;
import one.microstream.memory.MemoryStatistics;
import one.microstream.typing.XTypes;

public final class JdkMemoryAccessor implements MemoryAccessor, MemorySizeProperties
{
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////
	
	public static JdkMemoryAccessor New()
	{
		return new JdkMemoryAccessor();
	}
	
	JdkMemoryAccessor()
	{
		super();
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////
	
	@Override
	public final void guaranteeUsability()
	{
		JdkInternals.guaranteeUsability();
	}
	
	
	
	// direct byte buffer handling //
	
	@Override
	public final long getDirectByteBufferAddress(final ByteBuffer directBuffer)
	{
		return JdkInternals.getDirectByteBufferAddress(directBuffer);
	}

	@Override
	public final boolean deallocateDirectByteBuffer(final ByteBuffer directBuffer)
	{
		return JdkInternals.deallocateDirectBuffer(directBuffer);
	}

	@Override
	public final boolean isDirectByteBuffer(final ByteBuffer byteBuffer)
	{
		return XTypes.isDirectByteBuffer(byteBuffer);
	}

	@Override
	public final ByteBuffer guaranteeDirectByteBuffer(final ByteBuffer directBuffer)
	{
		return XTypes.guaranteeDirectByteBuffer(directBuffer);
	}
	
	
	
	// memory allocation //
	
	@Override
	public final long allocateMemory(final long bytes)
	{
		return JdkInternals.allocateMemory(bytes);
	}

	@Override
	public final long reallocateMemory(final long address, final long bytes)
	{
		return JdkInternals.reallocateMemory(address, bytes);
	}

	@Override
	public final void freeMemory(final long address)
	{
		JdkInternals.freeMemory(address);
	}

	@Override
	public final void fillMemory(final long address, final long length, final byte value)
	{
		JdkInternals.fillMemory(address, length, value);
	}

	

	// address-based getters for primitive values //
	
	@Override
	public final byte get_byte(final long address)
	{
		return JdkInternals.get_byte(address);
	}

	@Override
	public final boolean get_boolean(final long address)
	{
		return JdkInternals.get_boolean(null, address);
	}

	@Override
	public final short get_short(final long address)
	{
		return JdkInternals.get_short(address);
	}

	@Override
	public final char get_char(final long address)
	{
		return JdkInternals.get_char(address);
	}

	@Override
	public final int get_int(final long address)
	{
		return JdkInternals.get_int(address);
	}

	@Override
	public final float get_float(final long address)
	{
		return JdkInternals.get_float(address);
	}

	@Override
	public final long get_long(final long address)
	{
		return JdkInternals.get_long(address);
	}

	@Override
	public final double get_double(final long address)
	{
		return JdkInternals.get_double(address);
	}
	
	// note: getting a pointer from a non-Object-relative address makes no sense.
	

	
	// object-based getters for primitive values and references //
	
	@Override
	public final byte get_byte(final Object instance, final long offset)
	{
		return JdkInternals.get_byte(instance, offset);
	}

	@Override
	public final boolean get_boolean(final Object instance, final long offset)
	{
		return JdkInternals.get_boolean(instance, offset);
	}

	@Override
	public final short get_short(final Object instance, final long offset)
	{
		return JdkInternals.get_short(instance, offset);
	}

	@Override
	public final char get_char(final Object instance, final long offset)
	{
		return JdkInternals.get_char(instance, offset);
	}

	@Override
	public final int get_int(final Object instance, final long offset)
	{
		return JdkInternals.get_int(instance, offset);
	}

	@Override
	public final float get_float(final Object instance, final long offset)
	{
		return JdkInternals.get_float(instance, offset);
	}

	@Override
	public final long get_long(final Object instance, final long offset)
	{
		return JdkInternals.get_long(instance, offset);
	}

	@Override
	public final double get_double(final Object instance, final long offset)
	{
		return JdkInternals.get_double(instance, offset);
	}

	@Override
	public final Object getObject(final Object instance, final long offset)
	{
		return JdkInternals.getObject(instance, offset);
	}

	

	// address-based setters for primitive values //
	
	@Override
	public final void set_byte(final long address, final byte value)
	{
		JdkInternals.set_byte(address, value);
	}

	@Override
	public final void set_boolean(final long address, final boolean value)
	{
		// where the heck is Unsafe#putBoolean(long, boolean)? Forgot to implement? Wtf?
		JdkInternals.set_boolean(address, value);
	}

	@Override
	public void set_short(final long address, final short value)
	{
		JdkInternals.set_short(address, value);
	}

	@Override
	public final void set_char(final long address, final char value)
	{
		JdkInternals.set_char(address, value);
	}

	@Override
	public final void set_int(final long address, final int value)
	{
		JdkInternals.set_int(address, value);
	}

	@Override
	public final void set_float(final long address, final float value)
	{
		JdkInternals.set_float(address, value);
	}

	@Override
	public final void set_long(final long address, final long value)
	{
		JdkInternals.set_long(address, value);
	}

	@Override
	public final void set_double(final long address, final double value)
	{
		JdkInternals.set_double(address, value);
	}
	
	// note: setting a pointer to a non-Object-relative address makes no sense.

	

	// object-based setters for primitive values and references //
	
	@Override
	public final void set_byte(final Object instance, final long offset, final byte value)
	{
		JdkInternals.set_byte(instance, offset, value);
	}

	@Override
	public final void set_boolean(final Object instance, final long offset, final boolean value)
	{
		JdkInternals.set_boolean(instance, offset, value);
	}

	@Override
	public final void set_short(final Object instance, final long offset, final short value)
	{
		JdkInternals.set_short(instance, offset, value);
	}

	@Override
	public final void set_char(final Object instance, final long offset, final char value)
	{
		JdkInternals.set_char(instance, offset, value);
	}

	@Override
	public final void set_int(final Object instance, final long offset, final int value)
	{
		JdkInternals.set_int(instance, offset, value);
	}

	@Override
	public final void set_float(final Object instance, final long offset, final float value)
	{
		JdkInternals.set_float(instance, offset, value);
	}

	@Override
	public final void set_long(final Object instance, final long offset, final long value)
	{
		JdkInternals.set_long(instance, offset, value);
	}

	@Override
	public final void set_double(final Object instance, final long offset, final double value)
	{
		JdkInternals.set_double(instance, offset, value);
	}

	@Override
	public final void setObject(final Object instance, final long offset, final Object value)
	{
		JdkInternals.setObject(instance, offset, value);
	}
	
	
	
	// transformative byte array primitive value setters //
	
	@Override
	public final void set_byteInBytes(final byte[] bytes, final int index, final byte value)
	{
		JdkInternals.set_byteInBytes(bytes, index, value);
	}
	
	@Override
	public final void set_booleanInBytes(final byte[] bytes, final int index, final boolean value)
	{
		JdkInternals.set_booleanInBytes(bytes, index, value);
	}

	@Override
	public final void set_shortInBytes(final byte[] bytes, final int index, final short value)
	{
		JdkInternals.set_shortInBytes(bytes, index, value);
	}

	@Override
	public final void set_charInBytes(final byte[] bytes, final int index, final char value)
	{
		JdkInternals.set_charInBytes(bytes, index, value);
	}

	@Override
	public final void set_intInBytes(final byte[] bytes, final int index, final int value)
	{
		JdkInternals.set_intInBytes(bytes, index, value);
	}

	@Override
	public final void set_floatInBytes(final byte[] bytes, final int index, final float value)
	{
		JdkInternals.set_floatInBytes(bytes, index, value);
	}

	@Override
	public final void set_longInBytes(final byte[] bytes, final int index, final long value)
	{
		JdkInternals.set_longInBytes(bytes, index, value);
	}

	@Override
	public final void set_doubleInBytes(final byte[] bytes, final int index, final double value)
	{
		JdkInternals.set_doubleInBytes(bytes, index, value);
	}
	
	

	// generic variable-length range copying //

	@Override
	public final void copyRange(
		final long sourceAddress,
		final long targetAddress,
		final long length
	)
	{
		JdkInternals.copyRange(sourceAddress, targetAddress, length);
	}
	

	
	// address-to-array range copying //
	
	@Override
	public final void copyRangeToArray(final long sourceAddress, final byte[] target)
	{
		JdkInternals.copyRangeToArray(sourceAddress, target);
	}
	
	@Override
	public final void copyRangeToArray(final long sourceAddress, final boolean[] target)
	{
		JdkInternals.copyRangeToArray(sourceAddress, target);
	}

	@Override
	public final void copyRangeToArray(final long sourceAddress, final short[] target)
	{
		JdkInternals.copyRangeToArray(sourceAddress, target);
	}

	@Override
	public final void copyRangeToArray(final long sourceAddress, final char[] target)
	{
		JdkInternals.copyRangeToArray(sourceAddress, target);
	}
	
	@Override
	public final void copyRangeToArray(final long sourceAddress, final int[] target)
	{
		JdkInternals.copyRangeToArray(sourceAddress, target);
	}

	@Override
	public final void copyRangeToArray(final long sourceAddress, final float[] target)
	{
		JdkInternals.copyRangeToArray(sourceAddress, target);
	}

	@Override
	public final void copyRangeToArray(final long sourceAddress, final long[] target)
	{
		JdkInternals.copyRangeToArray(sourceAddress, target);
	}

	@Override
	public final void copyRangeToArray(final long sourceAddress, final double[] target)
	{
		JdkInternals.copyRangeToArray(sourceAddress, target);
	}
	
	

	// array-to-address range copying //
	
	@Override
	public final void copyArrayToAddress(final byte[] array, final long targetAddress)
	{
		JdkInternals.copyArrayToAddress(array, targetAddress);
	}
	
	@Override
	public final void copyArrayToAddress(final boolean[] array, final long targetAddress)
	{
		JdkInternals.copyArrayToAddress(array, targetAddress);
	}
	
	@Override
	public final void copyArrayToAddress(final short[] array, final long targetAddress)
	{
		JdkInternals.copyArrayToAddress(array, targetAddress);
	}

	@Override
	public final void copyArrayToAddress(final char[] array, final long targetAddress)
	{
		JdkInternals.copyArrayToAddress(array, targetAddress);
	}
	
	@Override
	public final void copyArrayToAddress(final int[] array, final long targetAddress)
	{
		JdkInternals.copyArrayToAddress(array, targetAddress);
	}
	
	@Override
	public final void copyArrayToAddress(final float[] array, final long targetAddress)
	{
		JdkInternals.copyArrayToAddress(array, targetAddress);
	}
	
	@Override
	public final void copyArrayToAddress(final long[] array, final long targetAddress)
	{
		JdkInternals.copyArrayToAddress(array, targetAddress);
	}
	
	@Override
	public final void copyArrayToAddress(final double[] array, final long targetAddress)
	{
		JdkInternals.copyArrayToAddress(array, targetAddress);
	}
	
	
	
	// conversion to byte array //
	
	@Override
	public final byte[] asByteArray(final long[] values)
	{
		return JdkInternals.asByteArray(values);
	}

	@Override
	public final byte[] asByteArray(final long value)
	{
		return JdkInternals.asByteArray(value);
	}
	
	

	// field offset abstraction //
	
	@Override
	public final long objectFieldOffset(final Field field)
	{
		return JdkInternals.objectFieldOffset(field);
	}
	
	@Override
	public final long[] objectFieldOffsets(final Field... fields)
	{
		return JdkInternals.objectFieldOffsets(fields);
	}

	@Override
	public final long objectFieldOffset(final Class<?> objectClass, final Field field)
	{
		// for the sun "Unsafe" implementation, the specific objectClass makes no sense/difference.
		return this.objectFieldOffset(field);
	}
	
	@Override
	public final long[] objectFieldOffsets(final Class<?> objectClass, final Field... fields)
	{
		// for the sun "Unsafe" implementation, the specific objectClass makes no sense/difference.
		return this.objectFieldOffsets(fields);
	}
	
	

	// special system methods, not really memory-related //
	
	@Override
	public final void ensureClassInitialized(final Class<?> c)
	{
		JdkInternals.ensureClassInitialized(c);
	}
	
	@Override
	public final void ensureClassInitialized(final Class<?> c, final Iterable<Field> usedFields)
	{
		// used fields are not relevant for JDK-internal class initialization.
		JdkInternals.ensureClassInitialized(c);
	}
	
	@Override
	public final <T> T instantiateBlank(final Class<T> c) throws InstantiationRuntimeException
	{
		return JdkInternals.instantiateBlank(c);
	}
	
	

	// memory size querying logic //
	
	@Override
	public final int pageSize()
	{
		return JdkInternals.pageSize();
	}
	
	@Override
	public final int byteSizeReference()
	{
		return JdkInternals.byteSizeReference();
	}

	@Override
	public final int byteSizeInstance(final Class<?> type)
	{
		return JdkInternals.byteSizeInstance(type);
	}
	
	@Override
	public final int byteSizeObjectHeader(final Class<?> type)
	{
		return JdkInternals.byteSizeObjectHeader();
	}
	
	@Override
	public final int byteSizeFieldValue(final Class<?> type)
	{
		return JdkInternals.byteSizeFieldValue(type);
	}
	
	@Override
	public final long byteSizeArray_byte(final long elementCount)
	{
		return JdkInternals.byteSizeArray_byte(elementCount);
	}

	@Override
	public final long byteSizeArray_boolean(final long elementCount)
	{
		return JdkInternals.byteSizeArray_boolean(elementCount);
	}

	@Override
	public final long byteSizeArray_short(final long elementCount)
	{
		return JdkInternals.byteSizeArray_short(elementCount);
	}

	@Override
	public final long byteSizeArray_char(final long elementCount)
	{
		return JdkInternals.byteSizeArray_char(elementCount);
	}

	@Override
	public final long byteSizeArray_int(final long elementCount)
	{
		return JdkInternals.byteSizeArray_int(elementCount);
	}

	@Override
	public final long byteSizeArray_float(final long elementCount)
	{
		return JdkInternals.byteSizeArray_float(elementCount);
	}

	@Override
	public final long byteSizeArray_long(final long elementCount)
	{
		return JdkInternals.byteSizeArray_long(elementCount);
	}

	@Override
	public final long byteSizeArray_double(final long elementCount)
	{
		return JdkInternals.byteSizeArray_double(elementCount);
	}

	@Override
	public final long byteSizeArrayObject(final long elementCount)
	{
		return JdkInternals.byteSizeArrayObject(elementCount);
	}

	
	// memory statistics creation //
		
	@Override
	public final MemoryStatistics createHeapMemoryStatistics()
	{
		return JdkInternals.createHeapMemoryStatistics();
	}
	
	@Override
	public MemoryStatistics createNonHeapMemoryStatistics()
	{
		return JdkInternals.createNonHeapMemoryStatistics();
	}
	
}

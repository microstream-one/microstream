package one.microstream.bytes;

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

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.function.Consumer;

import one.microstream.exceptions.ArrayCapacityException;
import one.microstream.exceptions.IndexBoundsException;
import one.microstream.functional._byteProcedure;
import one.microstream.math.XMath;
import one.microstream.memory.XMemory;



public final class VarByte implements Externalizable
{
	// (24.07.2013 TM)FIXME: Overhaul VarByte via VarString implementation

	///////////////////////////////////////////////////////////////////////////
	// constants //
	//////////////

	//have to be 2^n values
	private static final int
		CAPACITY_MIN   =  4, //needed for appendNull algorithm (and performance)
		CAPACITY_SMALL = 64
	;
	
	private static final int
		BITS_1_BYTE  =  8,
		BITS_2_BYTES = 16,
		BITS_3_BYTES = 24,
		BITS_4_BYTES = 32,
		BITS_5_BYTES = 40,
		BITS_6_BYTES = 48,
		BITS_7_BYTES = 56
	;
	
	private static final int
		BYTE_LENGTH_BYTE    = 1,
		BYTE_LENGTH_BOOLEAN = 1,
		BYTE_LENGTH_SHORT   = 2,
		BYTE_LENGTH_CHAR    = 2,
		BYTE_LENGTH_INT     = 4,
		BYTE_LENGTH_FLOAT   = 4,
		BYTE_LENGTH_LONG    = 8,
		BYTE_LENGTH_DOUBLE  = 8
	;

	private static final byte
		TRUE  = 1,
		FALSE = 0
	;



	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////

	private static final int boundPow2(final int n)
	{
		//if desired capacity is not boundable by shifting, max capacity is required
		if(XMath.isGreaterThanHighestPowerOf2(n))
		{
			return Integer.MAX_VALUE;
		}

		//normal case: start at min capacity and double it until it fits the desired capacity
		int p2 = CAPACITY_MIN;
		while(p2 < n)
		{
			p2 <<= 1;
		}
		return p2;
	}

	public static VarByte New()
	{
		return new VarByte(CAPACITY_SMALL);
	}
	
	
	/**
	 * Use this constructor only if really a specific size is needed or list of bytes to be handled is huge.<br>
	 * Otherwise, use the factory methods as they are faster due to skipping capacity checks and bounds adjustment.<br>
	 * <p>
	 * Note that the given {@code initialCapacity} will still be adjusted to the next higher 2^n bounding value.
	 * @param initialCapacity the initial size of the buffer
	 * @return a new VarByte instance
	 */
	public static VarByte New(final int initialCapacity)
	{
		if(initialCapacity < 0)
		{
			throw new IllegalArgumentException("initial capacity may not be negative: " + initialCapacity);
		}

		return new VarByte(boundPow2(initialCapacity));
	}
	


	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	byte[] data;
	int    size;



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////
	
	// to satisfy requirements of Externalizable
	private VarByte()
	{
		this(CAPACITY_MIN);
	}

	private VarByte(final int uncheckedInitialCapacity)
	{
		super();
		this.data = new byte[uncheckedInitialCapacity];
		this.size = 0;
	}



	///////////////////////////////////////////////////////////////////////////
	// override methods //
	/////////////////////

	public byte get(final int index)
	{
		if(index < 0 || index >= this.size)
		{
			throw new StringIndexOutOfBoundsException(index);
		}

		return this.data[index];
	}

	public byte lastByte()
	{
		if(this.size == 0)
		{
			throw new StringIndexOutOfBoundsException(0);
		}
		return this.data[this.size - 1];
	}

	public byte firstByte()
	{
		if(this.size == 0)
		{
			throw new StringIndexOutOfBoundsException(0);
		}
		return this.data[0];
	}

	public int length()
	{
		return this.size;
	}

	public VarByte subSequence(final int start, final int end)
	{
		final VarByte subSequence = new VarByte(end - start);
		System.arraycopy(this.data, start, subSequence.data, 0, end - start);
		return subSequence;
	}

	@Override
	public void readExternal(final ObjectInput in) throws IOException, ClassNotFoundException
	{
		final int size;
		final byte[] data = new byte[boundPow2(size = in.read())];

		for(int i = 0; i < size; i++)
		{
			data[i] = in.readByte();
		}
		this.data = data;
		this.size = size;
	}

	@Override
	public void writeExternal(final ObjectOutput out) throws IOException
	{
		final int size;
		final byte[] data = this.data;

		out.write(size = this.size);
		for(int i = 0; i < size; i++)
		{
			out.writeByte(data[i]);
		}
	}

	@Override
	public String toString()
	{
		return new String(this.data, 0, this.size);
	}

	public String toString(final Charset charset) throws UnsupportedEncodingException
	{
		return new String(this.data, 0, this.size, charset);
	}



	///////////////////////////////////////////////////////////////////////////
	// declared methods //
	/////////////////////

	public VarByte add(final boolean b)
	{
		if(this.size >= this.data.length)
		{
			System.arraycopy(this.data, 0, this.data = new byte[(int)(this.data.length * 2.0f)], 0, this.size);
		}
		this.data[this.size++] = b ? TRUE : FALSE;
		return this;
	}

	public VarByte add(final byte c)
	{
		if(this.size >= this.data.length)
		{
			if(this.size >= Integer.MAX_VALUE)
			{
				throw new ArrayCapacityException();
			}
			System.arraycopy(this.data, 0, this.data = new byte[(int)(this.data.length * 2.0f)], 0, this.size);
		}
		this.data[this.size++] = c;
		return this;
	}

	public VarByte add(final short s)
	{
		this.ensureFreeCapacity(BYTE_LENGTH_SHORT);
		this.data[this.size    ]   = (byte)(s >> BITS_1_BYTE);
		this.data[this.size + 1] = (byte)s;
		this.size += BYTE_LENGTH_SHORT;
		return this;
	}

	public VarByte add(final int i)
	{
		this.ensureFreeCapacity(BYTE_LENGTH_INT);
		
		// CHECKSTYLE.OFF: MagicNumber: No more constants. Those are arithmetical values. I won't replace "1" by "ONE"
		this.data[this.size    ] = (byte)(i >> BITS_3_BYTES);
		this.data[this.size + 1] = (byte)(i >> BITS_2_BYTES);
		this.data[this.size + 2] = (byte)(i >> BITS_1_BYTE );
		this.data[this.size + 3] = (byte)i;
		// CHECKSTYLE.ON: MagicNumber
		
		this.size += BYTE_LENGTH_INT;
		return this;
	}

	public VarByte add(final long l)
	{
		this.ensureFreeCapacity(BYTE_LENGTH_LONG);
		
		// CHECKSTYLE.OFF: MagicNumber: No more constants. Those are arithmetical values. I won't replace "1" by "ONE"
		this.data[this.size    ] = (byte)(l >> BITS_7_BYTES);
		this.data[this.size + 1] = (byte)(l >> BITS_6_BYTES);
		this.data[this.size + 2] = (byte)(l >> BITS_5_BYTES);
		this.data[this.size + 3] = (byte)(l >> BITS_4_BYTES);
		this.data[this.size + 4] = (byte)(l >> BITS_3_BYTES);
		this.data[this.size + 5] = (byte)(l >> BITS_2_BYTES);
		this.data[this.size + 6] = (byte)(l >> BITS_1_BYTE );
		this.data[this.size + 7] = (byte)l;
		// CHECKSTYLE.ON: MagicNumber
		
		this.size += BYTE_LENGTH_LONG;
		return this;
	}

	public VarByte add(final float f)
	{
		return this.add(Float.floatToIntBits(f));
	}

	public VarByte add(final double d)
	{
		return this.add(Double.doubleToLongBits(d));
	}

	public VarByte add(final byte[] bytes)
	{
		this.internalAppend(bytes);
		return this;
	}

	public VarByte add(final String s)
	{
		this.internalAppend(s.getBytes());
		return this;
	}


	public VarByte add(final VarByte varByte)
	{
		if(varByte.size == 0)
		{
			// provoke NPE
			return this;
		}
		this.internalAppend(varByte.data, 0, varByte.size);

		return this;
	}

	// copied from BulkList. Maintain there!
	public void ensureFreeCapacity(final int requiredFreeCapacity)
	{
		// as opposed to ensureCapacity(size + requiredFreeCapacity), this subtraction is overflow-safe
		if(this.data.length - this.size >= requiredFreeCapacity)
		{
			return; // already enough free capacity
		}

		// overflow-safe check for unreachable capacity
		if(Integer.MAX_VALUE - this.size < requiredFreeCapacity)
		{
			throw new ArrayCapacityException((long)requiredFreeCapacity + this.size);
		}

		// calculate new capacity
		final int newSize = this.size + requiredFreeCapacity;
		int newCapacity;
		if(XMath.isGreaterThanHighestPowerOf2(newSize))
		{
			// JVM technical limit
			newCapacity = Integer.MAX_VALUE;
		}
		else
		{
			newCapacity = this.data.length;
			while(newCapacity < newSize)
			{
				newCapacity <<= 1;
			}
		}

		// rebuild storage
		final byte[] data = new byte[newCapacity];
		System.arraycopy(this.data, 0, data, 0, this.size);
		this.data = data;
	}


	private void internalAppend(final byte[] bytes, final int offset, final int length)
	{
		this.ensureFreeCapacity(length);
		System.arraycopy(bytes, offset, this.data, this.size, length);
		this.size += length;
	}
	private void internalAppend(final byte[] bytes)
	{
		this.ensureFreeCapacity(bytes.length);
		System.arraycopy(bytes, 0, this.data, this.size, bytes.length);
		this.size += bytes.length;
	}

	public VarByte appendArray(final byte... bytes)
	{
		this.internalAppend(bytes, 0, bytes.length);
		return this;
	}
	public VarByte append(final byte[] bytes)
	{
		this.internalAppend(bytes);
		return this;
	}

	public VarByte append(final byte[] bytes, final int offset, final int length)
	{
		this.internalAppend(bytes, offset, length);
		return this;
	}

	public VarByte append(final byte value)
	{
		this.ensureFreeCapacity(BYTE_LENGTH_BYTE);
		this.data[this.size++] = value;
		return this;
	}

	public VarByte append(final boolean value)
	{
		this.ensureFreeCapacity(BYTE_LENGTH_BOOLEAN);
		this.data[this.size++] = value ? TRUE : FALSE;
		return this;
	}

	public VarByte append(final short value)
	{
		this.ensureFreeCapacity(BYTE_LENGTH_SHORT);
		XMemory.set_shortInBytes(this.data, this.size, value);
		this.size += BYTE_LENGTH_SHORT;
		return this;
	}

	public VarByte append(final char value)
	{
		this.ensureFreeCapacity(BYTE_LENGTH_CHAR);
		XMemory.set_charInBytes(this.data, this.size, value);
		this.size += BYTE_LENGTH_CHAR;
		return this;
	}

	public VarByte append(final int value)
	{
		this.ensureFreeCapacity(BYTE_LENGTH_INT);
		XMemory.set_intInBytes(this.data, this.size, value);
		this.size += BYTE_LENGTH_INT;
		return this;
	}

	public VarByte append(final float value)
	{
		this.ensureFreeCapacity(BYTE_LENGTH_FLOAT);
		XMemory.set_floatInBytes(this.data, this.size, value);
		this.size += BYTE_LENGTH_FLOAT;
		return this;
	}

	public VarByte append(final long value)
	{
		this.ensureFreeCapacity(BYTE_LENGTH_LONG);
		XMemory.set_longInBytes(this.data, this.size, value);
		this.size += BYTE_LENGTH_LONG;
		return this;
	}

	public VarByte append(final double value)
	{
		this.ensureFreeCapacity(BYTE_LENGTH_DOUBLE);
		XMemory.set_doubleInBytes(this.data, this.size, value);
		this.size += BYTE_LENGTH_DOUBLE;
		return this;
	}


	public VarByte setByte(final int index, final byte c)
	{
		if(index < 0 || index >= this.size)
		{
			throw new StringIndexOutOfBoundsException(index);
		}

		this.data[index] = c;
		return this;
	}

	public VarByte setBytes(final int index, final byte... c)
	{
		if(index + c.length >= this.size)
		{
			throw new IndexBoundsException(this.size, index);
		}
		System.arraycopy(c, 0, this.data, index, c.length);
		return this;
	}

	public VarByte setLastByte(final byte c)
	{
		this.data[this.size - 1] = c;
		return this;
	}


	public VarByte reverse()
	{
		final byte[] data = this.data;

		byte loopSwapByte;
		//only swap until size/2 (rounded down, because center element can remain untouched)
		for(int i = this.size >> 1, last = this.size - 1; i != 0; i--)
		{
			loopSwapByte = data[i];
			data[i] = data[last - i];
			data[last - i] = loopSwapByte;
		}
		return this;
	}

	/**
	 * Not implemented yet.
	 * @return currenty exactely what {@link #reverse()} returns.
	 * @deprecated not implemented yet. Currently just does {@link #reverse()}.
	 * @see #reverse()
	 */
	@Deprecated
	public VarByte surrogateByteReverse()
	{
		return this.reverse();
	}


	public int indexOf(final byte c)
	{
		final byte[] data = this.data;
		for(int i = 0, size = this.size; i < size; i++)
		{
			if(data[i] == c)
			{
				return i;
			}
		}
		return -1;
	}

	public int indexOf(final byte c, final int fromIndex)
	{
		final int size = this.size;
		if(fromIndex < 0 || fromIndex >= size)
		{
			throw new StringIndexOutOfBoundsException(fromIndex);
		}

		final byte[] data = this.data;
		for(int i = fromIndex; i < size; i++)
		{
			if(data[i] == c)
			{
				return i;
			}
		}
		return -1;
	}

	// local implementation for best performance in low level method
	public int indexOf(final byte[] bytes)
	{
		if(bytes.length == 0)
		{
			return 0;
		}

		final byte   firstByte = bytes[0] ;
		final byte[] data      = this.data;
		final int    scanBound = this.size - bytes.length + 1; // normalized array index bound

		scan: // scan for first byte. If matched, check the rest, continue on mismatch
		for(int s = 0; s < scanBound; s++)
		{
			if(data[s] != firstByte)
			{
				continue scan;
			}
			for(int c = 1, j = s; c < bytes.length; c++)
			{
				if(data[++j] != bytes[c])
				{
					continue scan;
				}
			}
			return s;
		}
		return -1;
	}

	public int indexOf(final byte[] bytes, final int offset)
	{
		if(offset < 0 || offset >= this.data.length)
		{
			throw new ArrayIndexOutOfBoundsException(offset);
		}
		if(bytes.length == 0)
		{
			return offset;
		}

		final byte   firstByte = bytes[0] ;
		final byte[] data      = this.data;
		final int    scanBound = this.size - bytes.length + 1; // normalized array index bound

		scan: // scan for first byte. If matched, check the rest, continue on mismatch
		for(int s = offset; s < scanBound; s++)
		{
			if(data[s] != firstByte)
			{
				continue scan;
			}
			for(int c = 1, j = s; c < bytes.length; c++)
			{
				if(data[++j] != bytes[c])
				{
					continue scan;
				}
			}
			return s;
		}
		return -1;
	}

	public boolean contains(final byte c)
	{
		final byte[] data = this.data;
		for(int i = 0, size = this.size; i < size; i++)
		{
			if(data[i] == c)
			{
				return true;
			}
		}
		return false;
	}

	public int lastIndexOf(final byte c)
	{
		final byte[] data = this.data;
		for(int i = this.size; i-- > 0;)
		{
			if(data[i] == c)
			{
				return i;
			}
		}
		return -1;
	}

	public int lastIndexOf(final byte c, final int fromIndex)
	{
		if(fromIndex < 0 || fromIndex >= this.size)
		{
			throw new StringIndexOutOfBoundsException(fromIndex);
		}

		final byte[] data = this.data;
		for(int i = fromIndex; i > 0; i--)
		{
			if(data[i] == c)
			{
				return i;
			}
		}
		return -1;
	}



	public int count(final byte c)
	{
		final byte[] data = this.data;
		final int    size = this.size;

		int count = 0;
		for(int i = 0; i < size; i++)
		{
			if(data[i] == c)
			{
				count++;
			}
		}
		return count;
	}

	public VarByte deleteByteAt(final int index)
	{
		final int lastIndex = this.size - 1;
		if(index < 0 || index > lastIndex)
		{
			throw new StringIndexOutOfBoundsException(index);
		}
		//intentionally don't check for index != lastIndex, as there's an extra method for that.
		System.arraycopy(this.data, index + 1, this.data, index, lastIndex - index);
		this.size--;
		return this;
	}

	public VarByte deleteLastByte()
	{
		if(this.size == 0)
		{
			throw new StringIndexOutOfBoundsException("Cannot delete last byte of no bytes");
		}
		this.size--;
		return this;
	}
	public VarByte deleteLast(final int n)
	{
		if(this.size < n)
		{
			throw new StringIndexOutOfBoundsException(n + " bytes cannot be deleted from " + this.size + " bytes");
		}
		this.size -= n;
		return this;
	}

	public VarByte shrinkTo(final int n)
	{
		if(this.size < n)
		{
			throw new StringIndexOutOfBoundsException(
				"Cannot shrink to size " + n + " on with a size of only " + this.size
			);
		}
		this.size = n;
		return this;
	}

	public byte[] toByteArray()
	{
		final byte[] bytes;
		System.arraycopy(this.data, 0, bytes = new byte[this.size], 0, this.size);
		return bytes;
	}

	public void getBytes(final int srcBegin, final int srcEnd, final byte[] dst, final int dstBegin)
	{
		if(srcBegin < 0)
		{
			throw new StringIndexOutOfBoundsException(srcBegin);
		}
		if(srcEnd > this.size)
		{
			throw new StringIndexOutOfBoundsException(srcEnd);
		}
		if(srcBegin > srcEnd)
		{
			throw new StringIndexOutOfBoundsException(srcEnd - srcBegin);
		}
		System.arraycopy(this.data, srcBegin, dst, dstBegin, srcEnd - srcBegin);
	}

	public boolean isEmpty()
	{
		return this.size == 0;
	}


	public void trimToSize()
	{
		final int size = this.size;
		if(size << 1 > this.data.length || XMath.isGreaterThanHighestPowerOf2(size))
		{
			return; //not shrinkable, abort
		}

		int newCapacity = CAPACITY_MIN;
		while(newCapacity < size)
		{
			newCapacity <<= 1;
		}
		final byte[] data = new byte[newCapacity];
		System.arraycopy(this.data, 0, data, 0, size);
		this.data = data;
	}

	/**
	 *
	 * @param varByte the VarByte to check
	 * @return {@code true} if {@code varByte} is either {@code null} or empty.
	 * @see VarByte#isEmpty()
	 */
	public static final boolean hasNoContent(final VarByte varByte)
	{
		return varByte == null || varByte.size == 0;
	}

	public static final boolean hasContent(final VarByte varByte)
	{
		return varByte != null && varByte.size != 0;
	}

	public VarByte replaceFirst(final byte sample, final byte replacement)
	{
		final byte[] data = this.data;
		for(int i = 0, size = this.size; i < size; i++)
		{
			if(data[i] == sample)
			{
				data[i] = replacement;
				break;
			}
		}
		return this;
	}

	public VarByte replaceFirst(final int beginIndex, final byte sample, final byte replacement)
	{
		if(beginIndex < 0)
		{
			throw new StringIndexOutOfBoundsException(beginIndex);
		}
		if(beginIndex > this.size)
		{
			throw new StringIndexOutOfBoundsException(beginIndex);
		}

		final byte[] data = this.data;
		for(int i = beginIndex, size = this.size; i < size; i++)
		{
			if(data[i] == sample)
			{
				data[i] = replacement;
				break;
			}
		}
		return this;
	}



	public VarByte subsequence(final int beginIndex, final int endIndex)
	{
		if(beginIndex < 0)
		{
			throw new StringIndexOutOfBoundsException(beginIndex);
		}
		if(endIndex > this.size)
		{
			throw new StringIndexOutOfBoundsException(endIndex);
		}
		if(beginIndex > endIndex)
		{
			throw new StringIndexOutOfBoundsException(endIndex - beginIndex);
		}
		final int length = endIndex - beginIndex;
		final VarByte vc = new VarByte(length);
		System.arraycopy(this.data, beginIndex, vc.data, 0, length);
		vc.size = length;
		return vc;
	}

	public void process(final _byteProcedure processor)
	{
		final byte[] data = this.data;
		for(int i = 0, size = this.size; i < size; i++)
		{
			processor.accept(data[i]);
		}
	}

	/**
	 * Only preferable to {@link #reset()} for security reasons.
	 * 
	 * @return this
	 */
	public VarByte clear()
	{
		final byte[] data = this.data;
		for(int i = 0, length = data.length; i < length; i++)
		{
			data[i] = 0;
		}
		this.size = 0;
		return this;
	}

	public final VarByte reset()
	{
		// spare nulling or reinstantiating altogether
		this.size = 0;
		return this;
	}

	public void truncate()
	{
		this.data = new byte[this.data.length];
		this.size = 0;
	}

	/**
	 * Passes a copy of the internal byte array to the {@link PrintStream#println(String)} method of the passed
	 * {@link PrintStream} instance.
	 * <p>
	 * The purpose of this method is to spare the unnecessary String instantiation of the generic method
	 * {@link PrintStream#println(Object)}.
	 *
	 * @param printStream the {@link PrintStream} instance to be written to.
	 * @return this {@link VarByte} instance.
	 */
	public VarByte printlnTo(final PrintStream printStream)
	{
		final byte[] bytes = new byte[this.size];
		System.arraycopy(this.data, 0, bytes, 0, this.size);
		printStream.println(Arrays.toString(bytes));
		return this;
	}

	/**
	 * Passes a copy of the internal byte array to the {@link PrintStream#print(String)} method of the passed
	 * {@link PrintStream} instance.
	 * <p>
	 * The purpose of this method is to spare the unnecessary String instantiation of the generic method
	 * {@link PrintStream#println(Object)}.
	 *
	 * @param printStream the {@link PrintStream} instance to be written to.
	 * @return this {@link VarByte} instance.
	 */
	public VarByte printTo(final PrintStream printStream)
	{
		final byte[] bytes = new byte[this.size];
		System.arraycopy(this.data, 0, bytes, 0, this.size);
		printStream.print(Arrays.toString(bytes));
		return this;
	}



	public VarByte repeat(final int amount, final byte b)
	{
		// sanity checking stuff
		if(amount <= 0)
		{
			if(amount < 0)
			{
				throw new IllegalArgumentException("Negative amount is invalid.");
			}
			return this; // amount 0 special case
		}
		int size;
		if(Integer.MAX_VALUE - amount < (size = this.size))
		{
			throw new ArrayIndexOutOfBoundsException("Technical array capacity exceeded.");
		}

		// capacity checking stuff
		final int targetSize;
		if(this.data.length < (targetSize = size + amount))
		{
			System.arraycopy(this.data, 0, this.data = new byte[(int)(this.data.length * 2.0f)], 0, size);
		}

		// actual work
		for(final byte[] data = this.data; size < targetSize; size++)
		{
			data[size] = b;
		}
		this.size = size;
		return this;
	}



	public VarByte apply(final Consumer<? super VarByte> procedure)
	{
		procedure.accept(this);
		return this;
	}



}

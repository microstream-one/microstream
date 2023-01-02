package one.microstream.chars;

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

import static java.lang.Math.max;

import java.io.Serializable;
import java.nio.charset.Charset;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import one.microstream.X;
import one.microstream.collections.XArrays;
import one.microstream.collections.types.XGettingCollection;
import one.microstream.exceptions.ArrayCapacityException;
import one.microstream.functional._charProcedure;
import one.microstream.math.XMath;


/**
 * Faster implementation of a StringBuilder with richer functionality.
 * Note that this class is NOT synchronized and only meant for single-threaded or thread-safe
 * (i.e. read only or explicitly synchronized) use.
 */
public final class VarString implements CharSequence, Appendable, Serializable
{
	/**
	 * Implementors of this interface handle appending their specific string representation directly.<br>
	 * This is useful for preventing the instantiation and copying of potentially large temporary strings
	 * which can have unnecessary and devastatingly negative effects on performance.
	 * <br>
	 */
	public interface Appendable
	{
		/**
		 * Append a string of characters to the passed {@link VarString} instance in whatever form deemed appropriate
		 * by the implementor.
		 *
		 * @param vs the {@link VarString} instance to append to.
		 * @return the same {@link VarString} instance that has been passed to allow method chaining.
		 */
		public VarString appendTo(VarString vs);
	}

	public static final void addNonNull(final VarString vs, final Object object)
	{
		if(object == null)
		{
			return;
		}
		vs.add(object);
	}
	
	public static void commaSpace(final VarString vs, final Object element)
	{
		vs.add(element).add(',', ' ');
	}



	///////////////////////////////////////////////////////////////////////////
	// constants //
	//////////////

	private static final int
		LITERAL_LENGTH_DUAL_CHAR    = 2,
		LITERAL_LENGTH_CR_LF        = 2,
		LITERAL_LENGTH_HEX_DEC_BYTE = 2,
		LITERAL_LENGTH_TRIPLE_CHAR  = 3,
		LITERAL_LENGTH_NULL         = 4,
		LITERAL_LENGTH_TRUE         = 4,
		LITERAL_LENGTH_FALSE        = 5
	;

	private static final int
		MINIMUM_CAPACITY = 4 // required for low-count appending algorithm simplifications.
	;

	private static final char
		BLANK = ' ' ,
		CR    = '\r',
		LF    = '\n',
		TAB   = '\t'
	;



	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////

	private static int calculateNewCapacity(final int capacity, final int minimumCapacity)
	{
		// handle max int value special case
		if(XMath.isGreaterThanHighestPowerOf2(minimumCapacity))
		{
			return Integer.MAX_VALUE;
		}

		//normal case: capacity up to 1 billion will suffice
		int c = capacity;
		while(c < minimumCapacity)
		{
			c <<= 1;
		}

		return c;
	}

	public static final VarString New()
	{
		return new VarString(MINIMUM_CAPACITY);
	}

	public static final VarString New(final int initialMinimumCapacity)
	{
		return new VarString(calculateNewCapacity(MINIMUM_CAPACITY, initialMinimumCapacity));
	}

	public static final VarString New(final String s)
	{
		if(s == null)
		{
			return New().addNull();
		}
		return New(s.length()).add(s);
	}



	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	char[] data;
	int    size;



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	VarString(final int initialCapacity)
	{
		super();
		this.data = new char[initialCapacity];
		this.size = 0;
	}



	///////////////////////////////////////////////////////////////////////////
	// declared methods //
	/////////////////////

	private void internalAdd(final char c)
	{
		if(this.size >= this.data.length)
		{
			if(this.size >= Integer.MAX_VALUE)
			{
				// " >= " is unnecessary but faster, maybe due to simple sign checking
				throw new ArrayCapacityException();
			}
			// float for implicit max int capping. Proved to be faster than special case if'ing every time.
			System.arraycopy(this.data, 0, this.data = new char[(int)(this.data.length * 2.0f)], 0, this.size);
		}
		this.data[this.size++] = c;
	}

	private void internalAdd(final String s)
	{
		this.ensureFreeCapacity(s.length());
		this.size = XChars.put(s, this.data, this.size);
	}

	public final void add(final char[] chars, final int offset, final int length)
	{
		this.ensureFreeCapacity(length);
		System.arraycopy(chars, offset, this.data, this.size, length); // implicit bounds checks inside
		this.size += length;
	}

	private void internalAdd(final char[] chars)
	{
		this.ensureFreeCapacity(chars.length);
		System.arraycopy(chars, 0, this.data, this.size, chars.length);
		this.size += chars.length;
	}

	private void internalAddNull()
	{
		this.ensureFreeCapacity(LITERAL_LENGTH_NULL);
		this.size = XChars.putNull(this.data, this.size);
	}

	private void internalAddTrue()
	{
		this.ensureFreeCapacity(LITERAL_LENGTH_TRUE);
		this.size = XChars.putTrue(this.data, this.size);
	}

	private void internalAddFalse()
	{
		this.ensureFreeCapacity(LITERAL_LENGTH_FALSE);
		this.size = XChars.putFalse(this.data, this.size);
	}

	private void internalAddObject(final Object object)
	{
		/* this method has the same behavior of String.valueOf(object) but
		 * is optimized for performance concerning usual special cases.
		 */

		// handle null case
		if(object == null)
		{
			this.internalAddNull();
			return;
		}

		// optimization for CharSequence types (guaranteed non null)
		if(object instanceof CharSequence)
		{
			this.internalAdd((CharSequence)object);
			return;
		}

		// note: intentionally no special case handling for char[] or Appendable here as this would alter behavior

		// default case: create new generic String representation and add that (guaranteed non null)
		this.internalAdd(object.toString());
	}

	private void internalAdd(final CharSequence charSequence)
	{
		if(charSequence.length() == 0)
		{
			return; // effectively no-op, so return
		}
		if(charSequence instanceof VarString)
		{
			this.add(((VarString)charSequence).data, 0, ((VarString)charSequence).size);
			return;
		}
		if(charSequence instanceof StringBuilder)
		{
			final StringBuilder sb = (StringBuilder)charSequence;
			sb.getChars(0, charSequence.length(), this.data, this.size);
			this.size += charSequence.length();
			return;
		}
		if(charSequence instanceof StringBuffer)
		{
			final StringBuffer sb = (StringBuffer)charSequence;
			sb.getChars(0, charSequence.length(), this.data, this.size);
			this.size += charSequence.length();
			return;
		}
		// any other implementations fall through to default handling as is makes no difference

		// default case: create new generic String representation and add that.
		this.internalAdd(charSequence.toString()); // applies to String efficiently by simply returning itself
	}

	private void rebuild(final int newCapacity)
	{
		System.arraycopy(this.data, 0, this.data = new char[newCapacity], 0, this.size);
	}

	public final void validateIndex(final int index)
	{
		if(index < 0 || index >= this.size)
		{
			throw new StringIndexOutOfBoundsException(index);
		}
	}

	public final void validateRange(final int offset, final int length)
	{
		this.validateIndex(offset);
		if(length != 0)
		{
			// length 0 is always a valid range as long as the offset is valid
			this.validateIndex(offset + length - 1);
		}
	}

	public final char last()
	{
		this.validateIndex(0); // last can only be invalid if size is 0.
		return this.data[this.size - 1];
	}

	public final char first()
	{
		this.validateIndex(0);
		return this.data[0];
	}

	public final VarString add(final char c)
	{
		this.internalAdd(c);
		return this;
	}

	public final VarString add(final Character c)
	{
		if(c == null)
		{
			this.internalAddNull();
		}
		else
		{
			this.internalAdd(c.charValue());
		}
		return this;
	}

	public final VarString add(final char c1, final char c2)
	{
		this.ensureFreeCapacity(LITERAL_LENGTH_DUAL_CHAR);
		this.data[this.size    ] = c1;
		this.data[this.size + 1] = c2;
		this.size += LITERAL_LENGTH_DUAL_CHAR;
		return this;
	}

	public final VarString add(final char c1, final char c2, final char c3)
	{
		this.ensureFreeCapacity(LITERAL_LENGTH_TRIPLE_CHAR);
		this.data[this.size    ] = c1;
		this.data[this.size + 1] = c2;
		this.data[this.size + 2] = c3;
		this.size += LITERAL_LENGTH_TRIPLE_CHAR;
		return this;
	}

	public final VarString add(final byte value)
	{
		this.ensureFreeCapacity(XChars.maxCharCount_byte());
		this.size = XChars.put(value, this.data, this.size);
		return this;
	}

	public final VarString add(final boolean value)
	{
		if(value)
		{
			this.internalAddTrue();
		}
		else
		{
			this.internalAddFalse();
		}
		return this;
	}
	
	public final VarString addMapped(final boolean value, final String trueValue, final String falseValue)
	{
		return this.add(value ? trueValue : falseValue);
	}
	
	public final VarString addMapped(final Boolean value, final String trueValue, final String falseValue)
	{
		return value != null
			? this.add(value ? trueValue : falseValue)
			: this
		;
	}

	public final VarString add(final short value)
	{
		this.ensureFreeCapacity(XChars.maxCharCount_short());
		this.size = XChars.put(value, this.data, this.size);
		return this;
	}

	public final VarString add(final int value)
	{
		// around 6 times faster than using Integer#toString() and requires no GC runs at all
		this.ensureFreeCapacity(XChars.maxCharCount_int());
		this.size = XChars.put(value, this.data, this.size);
		return this;
	}

	public final VarString add(final float value)
	{
		this.ensureFreeCapacity(XChars.maxCharCount_float());
		this.size = XChars.put(value, this.data, this.size);
		return this;
	}

	public final VarString add(final long value)
	{
		this.ensureFreeCapacity(XChars.maxCharCount_long());
		this.size = XChars.put(value, this.data, this.size);
		return this;
	}

	public final VarString add(final double value)
	{
		this.ensureFreeCapacity(XChars.maxCharCount_double());
		this.size = XChars.put(value, this.data, this.size);
		return this;
	}

	public final VarString add(final char[] chars)
	{
		this.internalAdd(chars);
		return this;
	}

	public final VarString add(final String s)
	{
		if(s == null)
		{
			this.internalAddNull();
		}
		else
		{
			this.internalAdd(s);
		}
		return this;
	}

	public final VarString add(final VarString vs)
	{
		if(vs == null)
		{
			this.internalAddNull();
		}
		else
		{
			this.add(vs.data, 0, vs.size);
		}
		return this;
	}

	public final VarString add(final CharSequence cs)
	{
		if(cs == null)
		{
			this.internalAddNull();
		}
		else
		{
			this.internalAdd(cs);
		}
		return this;
	}

	public final VarString add(final Object o)
	{
		this.internalAddObject(o);
		return this;
	}

	public final VarString addHexDec(final byte value)
	{
		this.ensureFreeCapacity(LITERAL_LENGTH_HEX_DEC_BYTE);
		this.size = XChars.putHexDec(value, this.data, this.size);
		return this;
	}

	public final VarString addHexDec(final byte... bytes)
	{
		this.ensureFreeCapacity(bytes.length << 1);
		int size = this.size;
		final char[] data = this.data;
		for(int i = 0; i < bytes.length; i++)
		{
			size = XChars.putHexDec(bytes[i], data, size);
		}
		this.size = size;
		return this;
	}

	public final VarString addObjects(final Object... objects)
	{
		for(final Object o : objects)
		{
			this.add(o);
		}
		return this;
	}

	public final void ensureFreeCapacity(final int minimumFreeCapacity)
	{
		if(Integer.MAX_VALUE - minimumFreeCapacity < this.size && minimumFreeCapacity > 0)
		{
			throw new ArrayCapacityException((long)Integer.MAX_VALUE + minimumFreeCapacity);
		}
		this.ensureCapacity(this.size + minimumFreeCapacity);
	}

	public final void ensureCapacity(final int minimumCapacity)
	{
		// negative value is not directly an error for a minimum capacity, so no validation for that case

		if(this.data.length >= minimumCapacity)
		{
			return; // already enough capacity, abort
		}
		this.rebuild(calculateNewCapacity(this.data.length, minimumCapacity));
	}

	public final VarString addChars(final char... chars)
	{
		this.add(chars, 0, chars.length);
		return this;
	}

	public final VarString addCharSequences(final CharSequence... csqs)
	{
		for(final CharSequence csq : csqs)
		{
			this.add(csq);
		}
		return this;
	}

	public final VarString addNull()
	{
		this.internalAddNull();
		return this;
	}

	public final VarString addTrue()
	{
		this.internalAddTrue();
		return this;
	}

	public final VarString addFalse()
	{
		this.internalAddFalse();
		return this;
	}

	public final VarString append(final VarString.Appendable appendable)
	{
		if(appendable == null)
		{
			this.internalAddNull();
		}
		else
		{
			appendable.appendTo(this);
		}
		return this;
	}

	public final <E> VarString add(
		final E                                element,
		final BiConsumer<VarString, ? super E> joiner
	)
	{
		joiner.accept(this, element);
		
		return this;
	}
	
	public final <E> VarString addAll(
		final E[]                              elements,
		final BiConsumer<VarString, ? super E> joiner
	)
	{
		this.uncheckedAddAll(elements, 0, elements.length, joiner);
		
		return this;
	}
	
	public final <E> VarString addAll(
		final E[]                              elements,
		final int                              offset  ,
		final int                              length  ,
		final BiConsumer<VarString, ? super E> joiner
	)
	{
		XArrays.validateArrayRange(elements, offset, length);
		this.uncheckedAddAll(elements, offset, length, joiner);
		
		return this;
	}
	
	private <E> void uncheckedAddAll(
		final E[]                              elements,
		final int                              offset  ,
		final int                              length  ,
		final BiConsumer<VarString, ? super E> joiner
	)
	{
		final int bound = offset + length;
		for(int i = offset; i < bound; i++)
		{
			joiner.accept(this, elements[i]);
		}
	}
	
	public final <E> VarString addAll(
		final Iterable<? extends E>            elements,
		final BiConsumer<VarString, ? super E> joiner
	)
	{
		for(final E element : elements)
		{
			joiner.accept(this, element);
		}
		
		return this;
	}

	public final VarString set(final int index, final char c)
	{
		this.validateIndex(index);
		this.data[index] = c;
		return this;
	}

	public final VarString setChars(final int index, final char... c)
	{
		this.validateIndex(index + c.length - 1);
		System.arraycopy(c, 0, this.data, index, c.length);
		return this;
	}

	public final VarString setLast(final char c)
	{
		this.validateIndex(0); // last can only be invalid if size is 0.
		this.data[this.size - 1] = c;
		return this;
	}

	public final VarString reverse()
	{
		XChars.uncheckedReverse(this.data, this.size);
		return this;
	}

	/**
	 * Not implemented yet.
	 * @return currently exactly what {@link #reverse()} returns.
	 * @deprecated not implemented yet. Currently just calls {@link #reverse()}.
	 * @see #reverse()
	 */
	@Deprecated // actually not deprecated but work-in-progress, but there's no annotation for that
	public final VarString surrogateCharReverse()
	{
		return this.reverse();
	}

	public final int indexOf(final char c)
	{
		return XChars.uncheckedIndexOf(this.data, this.size, 0, c);
	}

	public final int indexOf(final char c, final int offset)
	{
		this.validateIndex(offset);
		return XChars.uncheckedIndexOf(this.data, this.size, offset, c);
	}

	public final int indexOf(final char[] chars)
	{
		return XChars.uncheckedIndexOf(this.data, this.size, chars);
	}

	public final int indexOf(final char[] chars, final int offset)
	{
		this.validateIndex(offset);
		return XChars.indexOf(chars, offset, this.size - offset, chars);
	}

	public final int indexOf(final String s)
	{
		return XChars.indexOf(this.data, 0, this.size, XChars.readChars(s), 0, s.length(), 0);
	}

	public final int indexOf(final String s, final int offset)
	{
		return XChars.indexOf(this.data, 0, this.size, XChars.readChars(s), 0, s.length(), offset);
	}

	public final int indexOf(final VarString vc)
	{
		return XChars.indexOf(this.data, this.size, vc.data, vc.size, 0);
	}

	public final int indexOf(final VarString vc, final int offset)
	{
		return XChars.indexOf(this.data, this.size, vc.data, vc.size, offset);
	}

	public final boolean contains(final char c)
	{
		return XChars.uncheckedContains(this.data, 0, this.size, c);
	}

	public final boolean contains(final char[] chars)
	{
		return XChars.uncheckedIndexOf(this.data, this.size, chars) != -1;
	}

	public final boolean contains(final String s)
	{
		return XChars.uncheckedIndexOf(this.data, this.size, XChars.readChars(s)) != -1;
	}

	public final boolean contains(final VarString vc)
	{
		return XChars.indexOf(this.data, this.size, vc.data, vc.size, 0) != -1;
	}

	public final int lastIndexOf(final char c)
	{
		return XChars.uncheckedLastIndexOf(this.data, this.size, c);
	}

	public final int lastIndexOf(final char c, final int offset)
	{
		this.validateIndex(offset);
		return XChars.uncheckedLastIndexOf(this.data, offset, c);
	}

	public final int count(final char c)
	{
		return XChars.count(this.data, 0, this.size, c);
	}

	public final int count(final char[] chars)
	{
		return XChars.count(this.data, 0, this.size, chars, 0, chars.length);
	}

	public final int count(final String s)
	{
		return XChars.count(this.data, 0, this.size, XChars.readChars(s), 0, s.length());
	}

	public final int count(final VarString vc)
	{
		return XChars.count(this.data, 0, this.size, vc.data, 0, vc.size);
	}

	public final VarString deleteAt(final int index)
	{
		this.validateIndex(index);
		System.arraycopy(this.data, index + 1, this.data, index, this.size - index);
		this.size--;
		return this;
	}

	public final VarString deleteLast()
	{
		this.validateIndex(0); // last can only be invalid if size is 0.
		this.size--;
		return this;
	}

	public final VarString deleteLast(final int n)
	{
		if(n != 0)
		{
			this.validateIndex(this.size - n); // validate indirectly via index by centralized validation
			this.size -= n;
		}
		return this;
	}

	public final VarString shrinkTo(final int n)
	{
		this.validateIndex(n - 1);
		this.size = n;
		return this;
	}

	public final char[] toArray()
	{
		final char[] chars;
		System.arraycopy(this.data, 0, chars = new char[this.size], 0, this.size);
		return chars;
	}

	public final VarString copyTo(final char[] target, final int targetOffset)
	{
		System.arraycopy(this.data, 0, target, targetOffset, this.size); // target bounds validation is done implicitely
		return this;
	}

	public final VarString copyTo(final int offset, final char[] target, final int targetOffset, final int length)
	{
		this.validateRange(offset, length); // must validate locally against size
		System.arraycopy(this.data, offset, target, targetOffset, length); // remaining validation is done implicitely
		return this;
	}

	public final boolean isEmpty()
	{
		return this.size == 0;
	}

	public final VarString consolidate()
	{
		final int requiredCapacity;
		if((requiredCapacity = calculateNewCapacity(1, this.size)) < this.data.length)
		{
			this.rebuild(requiredCapacity);
		}
		return this;
	}

	public final VarString trim()
	{
		int size = this.size;
		int start = 0;
		final char[] data = this.data;

		while(start < size && data[start] <= BLANK)
		{
			start++;
		}
		while(start < size && data[size - 1] <= BLANK)
		{
			size--;
		}
		if(start != 0)
		{
			System.arraycopy(data, start, data, 0, size - start);
		}
		this.size = size - start;

		return this;
	}

	public final VarString truncateTo(final int newLength)
	{
		this.validateIndex(newLength - 1);
		this.size = newLength;
		return this;
	}

	public final VarString subsequence(final int beginIndex, final int endIndex)
	{
		this.validateIndex(beginIndex);
		this.validateIndex(endIndex);

		final int length = endIndex - beginIndex;
		this.validateIndex(length); // actually not length but endIndex must be greater than beginIndex

		final VarString subsequence = VarString.New(length);
		System.arraycopy(this.data, beginIndex, subsequence.data, 0, length);
		subsequence.size = length;
		return subsequence;
	}

	public final String substring(final int beginIndex, final int endIndex)
	{
		this.validateIndex(beginIndex);
		this.validateIndex(endIndex);
		this.validateIndex(endIndex - beginIndex); // actually not length but endIndex must be greater than beginIndex
		return new String(this.data, beginIndex, endIndex - beginIndex);
	}

	public final void iterate(final _charProcedure iterator)
	{
		XChars.iterate(this.data, 0, this.size, iterator);
	}

	public final VarString list(final String separator, final XGettingCollection<?> listElements)
	{
		if(X.hasNoContent(listElements))
		{
			return this;
		}

		final char[] sepChars = XChars.readChars(separator);
		for(final Object e : listElements)
		{
			this.internalAddObject(e);
			this.internalAdd(sepChars);
		}
		return this.deleteLast(sepChars.length);
	}

	public final VarString list(final String separator, final Object... listElements)
	{
		if(XArrays.hasNoContent(listElements))
		{
			return this;
		}

		this.ensureFreeCapacity(listElements.length * separator.length());

		final char[] sepChars = XChars.readChars(separator);
		for(final Object e : listElements)
		{
			this.internalAddObject(e);
			this.internalAdd(sepChars);
		}
		return this.deleteLast(sepChars.length);
	}

	public final VarString list(final String separator, final String... listElements)
	{
		if(XArrays.hasNoContent(listElements))
		{
			return this;
		}

		this.ensureFreeCapacity(listElements.length * separator.length());

		final char[] sepChars = XChars.readChars(separator);
		for(final String e : listElements)
		{
			if(e == null)
			{
				this.internalAddNull();
			}
			else
			{
				this.internalAdd(e);
			}
			this.internalAdd(sepChars);
		}
		return this.deleteLast(sepChars.length);
	}

	public final VarString list(final String separator, final VarString... listElements)
	{
		if(XArrays.hasNoContent(listElements))
		{
			return this;
		}

		this.ensureFreeCapacity(listElements.length * separator.length());

		final char[] sepChars = XChars.readChars(separator);
		for(final VarString e : listElements)
		{
			if(e == null)
			{
				this.internalAddNull();
			}
			else
			{
				this.internalAdd(e);
			}
			this.internalAdd(sepChars);
		}
		return this.deleteLast(sepChars.length);
	}

	public final VarString list(final String separator, final Appendable... listElements)
	{
		if(XArrays.hasNoContent(listElements))
		{
			return this;
		}

		this.ensureFreeCapacity(listElements.length * separator.length());

		final char[] sepChars = XChars.readChars(separator);
		for(final Appendable e : listElements)
		{
			if(e == null)
			{
				this.addNull();
			}
			else
			{
				e.appendTo(this);
			}
			this.add(sepChars);
		}
		return this.deleteLast(sepChars.length);
	}

	public final VarString list(final String separator, final boolean... listElements)
	{
		if(listElements == null || listElements.length == 0)
		{
			return this;
		}

		this.ensureFreeCapacity(listElements.length * (XChars.maxCharCount_boolean() + separator.length()));

		final char[] sepChars = XChars.readChars(separator);
		final char[] data = this.data;
		int size = this.size;
		for(final boolean e : listElements)
		{
			size = XChars.put(sepChars, data, e
				? XChars.putTrue(data, size)
				: XChars.putFalse(data, size)
			);
		}
		this.size = size;
		return this.deleteLast(sepChars.length);
	}

	public final VarString list(final String separator, final byte... listElements)
	{
		if(listElements == null || listElements.length == 0)
		{
			return this;
		}

		this.ensureFreeCapacity(listElements.length * (XChars.maxCharCount_byte() + separator.length()));

		final char[] sepChars = XChars.readChars(separator);
		final char[] data = this.data;

		int size = this.size;
		for(final byte e : listElements)
		{
			size = XChars.put(sepChars, data, XChars.put(e, data, size));
		}
		this.size = size;
		return this.deleteLast(sepChars.length);
	}

	public final VarString list(final String separator, final short... listElements)
	{
		if(listElements == null || listElements.length == 0)
		{
			return this;
		}

		this.ensureFreeCapacity(listElements.length * (XChars.maxCharCount_short() + separator.length()));

		final char[] sepChars = XChars.readChars(separator);
		final char[] data = this.data;

		int size = this.size;
		for(final short e : listElements)
		{
			size = XChars.put(sepChars, data, XChars.put(e, data, size));
		}
		this.size = size;
		return this.deleteLast(sepChars.length);
	}

	public final VarString list(final String separator, final int... listElements)
	{
		if(listElements == null || listElements.length == 0)
		{
			return this;
		}

		this.ensureFreeCapacity(listElements.length * (XChars.maxCharCount_int() + separator.length()));

		final char[] sepChars = XChars.readChars(separator);
		final char[] data = this.data;

		int size = this.size;
		for(final int e : listElements)
		{
			size = XChars.put(sepChars, data, XChars.put(e, data, size));
		}
		this.size = size;
		return this.deleteLast(sepChars.length);
	}

	public final VarString list(final String separator, final long... listElements)
	{
		if(listElements == null || listElements.length == 0)
		{
			return this;
		}

		this.ensureFreeCapacity(listElements.length * (XChars.maxCharCount_long() + separator.length()));

		final char[] sepChars = XChars.readChars(separator);
		final char[] data = this.data;

		int size = this.size;
		for(final long e : listElements)
		{
			size = XChars.put(sepChars, data, XChars.put(e, data, size));
		}
		this.size = size;
		return this.deleteLast(sepChars.length);
	}

	public final VarString list(final String separator, final float... listElements)
	{
		if(listElements == null || listElements.length == 0)
		{
			return this;
		}

		this.ensureFreeCapacity(listElements.length * (XChars.maxCharCount_float() + separator.length()));

		final char[] sepChars = XChars.readChars(separator);
		final char[] data = this.data;

		int size = this.size;
		for(final float e : listElements)
		{
			size = XChars.put(sepChars, data, XChars.put(e, data, size));
		}
		this.size = size;
		return this.deleteLast(sepChars.length);
	}

	public final VarString list(final String separator, final double... listElements)
	{
		if(listElements == null || listElements.length == 0)
		{
			return this;
		}

		this.ensureFreeCapacity(listElements.length * (XChars.maxCharCount_double() + separator.length()));

		final char[] sepChars = XChars.readChars(separator);
		final char[] data = this.data;

		int size = this.size;
		for(final double e : listElements)
		{
			size = XChars.put(sepChars, data, XChars.put(e, data, size));
		}
		this.size = size;
		return this.deleteLast(sepChars.length);
	}

	public final VarString list(final String separator, final char... listElements)
	{
		if(listElements == null || listElements.length == 0)
		{
			return this;
		}

		this.ensureFreeCapacity(listElements.length * (1 + separator.length()));

		final char[] sepChars = XChars.readChars(separator);
		final char[] data = this.data;

		int size = this.size;
		for(final char e : listElements)
		{
			data[size] = e;
			size = XChars.put(sepChars, data, size + 1);
		}
		this.size = size;
		return this.deleteLast(sepChars.length);
	}

	/**
	 * Fills this instance's char storage completely with zeros and sets its size to 0.
	 * <p/>
	 * Only preferable to {@link #reset()} for in-memory security reasons.
	 *
	 * @return this
	 */
	public final VarString clear()
	{
		final char[] data = this.data;
		final int    length = data.length;
		for(int i = 0; i < length; i++)
		{
			data[i] = 0;
		}
		this.size = 0;
		return this;
	}

	/**
	 * Simply resets this instance by setting its size to 0 while leaving the actual char storage untouched.
	 *
	 * @return this
	 */
	public final VarString reset()
	{
		// spare nulling or reinstantiating altogether
		this.size = 0;
		return this;
	}

	public final VarString blank()
	{
		this.internalAdd(BLANK);
		return this;
	}

	public final VarString blank(final int amount)
	{
		return this.repeat(amount, BLANK);
	}

	public final VarString tab()
	{
		this.internalAdd(TAB);
		return this;
	}

	public final VarString tab(final int amount)
	{
		return this.repeat(amount, TAB);
	}

	public final VarString cr()
	{
		this.internalAdd(CR);
		return this;
	}

	public final VarString cr(final int amount)
	{
		return this.repeat(amount, CR);
	}

	public final VarString lf()
	{
		this.internalAdd(LF);
		return this;
	}

	public final VarString lf(final int amount)
	{
		return this.repeat(amount, LF);
	}

	public final VarString crlf()
	{
		this.ensureFreeCapacity(LITERAL_LENGTH_CR_LF);
		this.data[this.size    ] = CR;
		this.data[this.size + 1] = LF;
		this.size += LITERAL_LENGTH_CR_LF;
		return this;
	}

	public final VarString repeat(final int amount, final char c)
	{
		// sanity check
		if(amount <= 0)
		{
			if(amount < 0)
			{
				throw new IllegalArgumentException("Negative amount is invalid.");
			}
			return this; // amount 0 special case
		}

		// capacity ensuring
		this.ensureFreeCapacity(amount);

		// actual work
		XChars.uncheckedRepeat(this.data, this.size, amount, c);
		this.size += amount;
		return this;
	}

	public final VarString repeat(final int amount, final String string)
	{
		this.repeat(amount, XChars.readChars(string));

		return this;
	}

	public final VarString repeat(final int amount, final char[] chars)
	{
		// sanity check
		if(amount <= 0)
		{
			if(amount < 0)
			{
				throw new IllegalArgumentException("Negative amount is invalid.");
			}
			return this; // amount 0 special case
		}

		this.ensureFreeCapacity(amount * chars.length);

		// actual work
		XChars.uncheckedRepeat(this.data, this.size, amount, chars);
		this.size += amount * chars.length;
		return this;
	}

	public final VarString apply(final Consumer<? super VarString> procedure)
	{
		procedure.accept(this);
		return this;
	}

	public final boolean equalsAt(final int index, final char[] chars, final int charsOffset, final int charsLength)
	{
		// 0-length special case. Note that index == size is valid because index + length still won't exceed size
		if(charsLength == 0 && index <= this.size)
		{
			return true;
		}

		this.validateRange(index, charsLength);
		XChars.validateRange(chars, charsOffset, charsLength);
		return XChars.uncheckedEquals(this.data, index, chars, charsOffset, charsLength);
	}

	public final boolean endsWith(final char c)
	{
		this.validateIndex(0);
		return this.data[this.size - 1] == c;
	}

	public final boolean endsWith(final char[] chars)
	{
		return this.size >= chars.length && this.equalsAt(this.size - chars.length, chars, 0, chars.length);
	}

	public final boolean endsWith(final String string)
	{
		return this.endsWith(XChars.readChars(string));
	}

	private static int calculatePaddingCount(final String s, final int totalLength)
	{
		return max(totalLength - s.length(), 0);
	}

	public final VarString padLeft(final String s, final int totalLength, final char paddingChar)
	{
		return this.repeat(calculatePaddingCount(s, totalLength), paddingChar).add(s);
	}

	public final VarString padRight(final String s, final int totalLength, final char paddingChar)
	{
		return this.add(s).repeat(calculatePaddingCount(s, totalLength), paddingChar);
	}

	public final VarString replaceFirst(final char sample, final char replacement)
	{
		XChars.uncheckedReplaceFirst(this.data, 0, this.size, sample, replacement);
		return this;
	}

	public final VarString replaceFirst(final char sample, final char replacement, final int offset)
	{
		this.validateIndex(offset);
		XChars.uncheckedReplaceFirst(this.data, offset, this.size - offset, sample, replacement);
		return this;
	}

	public final VarString replaceFirst(final char sample, final char replacement, final int offset, final int length)
	{
		this.validateRange(offset, length);
		XChars.uncheckedReplaceFirst(this.data, offset, length, sample, replacement);
		return this;
	}

	public final VarString replaceAll(final char sample, final char replacement)
	{
		XChars.uncheckedReplaceAll(this.data, 0, this.size, sample, replacement);
		return this;
	}

	public final VarString replaceAll(final char sample, final char replacement, final int offset)
	{
		this.validateIndex(offset);
		XChars.uncheckedReplaceAll(this.data, offset, this.size - offset, sample, replacement);
		return this;
	}

	public final VarString replaceAll(final char sample, final char replacement, final int offset, final int length)
	{
		this.validateRange(offset, length);
		XChars.uncheckedReplaceAll(this.data, offset, length, sample, replacement);
		return this;
	}



	///////////////////////////////////////////////////////////////////////////
	// RegExp //
	///////////

	public String replaceFirst(final String regex, final String replacement)
	{
		return Pattern.compile(regex).matcher(this).replaceFirst(replacement);
	}

	public final String replaceAll(final String regex, final String replacement)
	{
		return Pattern.compile(regex).matcher(this).replaceAll(replacement);
	}

	public final String replace(final CharSequence target, final CharSequence replacement)
	{
		return Pattern
			.compile(target.toString(), Pattern.LITERAL)
			.matcher(this)
			.replaceAll(Matcher.quoteReplacement(replacement.toString()))
		;
	}

	public final String[] split(final String regex, final int limit)
	{
		return Pattern.compile(regex).split(this, limit);
	}

	public final String[] split(final String regex)
	{
		return this.split(regex, 0);
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// character encoding //
	///////////////////////
	
	/**
	 * Alias for {@code this.encodeBy(XChars.standardCharset());} for general purpose encoding, with the "standard"
	 * being UTF-8 ({@link XChars#utf8()}), because any other charset is nonsense for a general case.
	 * 
	 * @return the character data of this instance as an UTF-8 byte array.
	 * @see #encodeBy(Charset)
	 */
	public final byte[] encode()
	{
		return this.encodeBy(XChars.standardCharset());
	}
	
	public final byte[] encodeBy(final Charset charset)
	{
		return this.toString().getBytes(charset);
	}


	///////////////////////////////////////////////////////////////////////////
	// override methods //
	/////////////////////

	@Override
	public final char charAt(final int index)
	{
		this.validateIndex(index);
		return this.data[index];
	}

	@Override
	public final int length()
	{
		return this.size;
	}

	@Override
	public final VarString subSequence(final int start, final int end)
	{
		final VarString subSequence = VarString.New(end - start);
		System.arraycopy(this.data, start, subSequence.data, 0, end - start);
		return subSequence;
	}

	@Override
	public final VarString append(final CharSequence csq)
	{
		if(csq == null)
		{
			this.internalAddNull();
			return this;
		}
		return this.add(csq.toString());
	}

	@Override
	public final VarString append(final char c)
	{
		return this.add(c);
	}

	@Override
	public final VarString append(final CharSequence csq, final int start, final int end)
	{
		if(csq == null)
		{
			this.internalAddNull();
			return this;
		}
		return this.append(csq.subSequence(start, end));
	}

	@Override
	public final String toString()
	{
		return new String(this.data, 0, this.size);
	}

}

/*
 * Copyright (c) 2008-2010, Thomas Muenz
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the <organization> nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package net.jadoth.math;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import net.jadoth.branching.ThrowBreak;
import net.jadoth.chars.VarString;
import net.jadoth.exceptions.NumberRangeException;
import net.jadoth.functional._intProcedure;
import net.jadoth.typing.JadothTypes;

/**
 *
 * @author Thomas Muenz
 */
public class _intRange implements Set<Integer>
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	int to, from, direction;



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public _intRange(final int from, final int to)
	{
		super();
		this.from = from;
		this.to = to;
		this.direction = this.to < this.from ? -1 : 1;
	}



	///////////////////////////////////////////////////////////////////////////
	// override methods //
	/////////////////////

	@Override
	public boolean add(final Integer e)
	{
		if(e == null)
		{
			return false;
		}

		final int value = e;
		if(this.direction == 1)
		{
			if(value < this.from)
			{
				this.from = value;
				return true;
			}
			else if(value > this.to)
			{
				this.to = value;
				return true;
			}
		}
		else
		{
			if(value > this.from)
			{
				this.from = value;
				return true;
			}
			else if(value < this.to)
			{
				this.to = value;
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean addAll(final Collection<? extends Integer> c)
	{
		int to = this.to;
		int from = this.from;

		if(this.direction == 1)
		{
			for(final Integer i : c)
			{
				final int iVal = i.intValue();
				if(iVal < from)
				{
					from = iVal;
				}
				else if(iVal > to)
				{
					to = iVal;
				}
			}
		}
		else
		{
			for(final Integer i : c)
			{
				final int iVal = i.intValue();
				if(iVal > from)
				{
					from = iVal;
				}
				else if(iVal < to)
				{
					to = iVal;
				}
			}
		}
		final boolean hasChanged = this.to != to || this.from != from;
		this.from = from;
		this.to = to;
		return hasChanged;
	}

	@Override
	public void clear()
	{
		this.from = 0;
		this.to = 0;
		this.direction = 1;
	}

	@Override
	public boolean contains(final Object o)
	{
		if(o instanceof Number)
		{
			return this.containsInt(((Number)o).intValue());
		}
		return false; //all non-number objects can't be removed at all, so return false
	}

	/**
	 * Contains all.
	 *
	 * @param c the c
	 * @return true, if successful
	 * @return
	 */
	@Override
	public boolean containsAll(final Collection<?> c)
	{
		for(final Object o : c)
		{
			if(!this.contains(o))
			{
				return false;
			}
		}
		return true;
	}

	/**
	 * Checks if is empty.
	 *
	 * @return true, if is empty
	 * @return
	 */
	@Override
	public boolean isEmpty()
	{
		return this.from == this.to;
	}

	/**
	 * Iterator.
	 *
	 * @return the iterator
	 * @return
	 */
	@Override
	public Iterator<Integer> iterator()
	{
		return new RangeIterator();
	}

	/**
	 * Removes the.
	 *
	 * @param o the o
	 * @return true, if successful
	 * @return
	 */
	@Override
	public boolean remove(final Object o)
	{
		if(o == null)
		{
			return false;
		}

		//handle common case first
		int i = 0;
		if(o instanceof Integer)
		{
			i = (Integer)o;
		}
		else if(o instanceof Short)
		{
			i = (Short)o;
		}
		else if(o instanceof Byte)
		{
			i = (Byte)o;
		}
		else if(o instanceof Character)
		{
			i = (Character)o;
		}
		else
		{
			if(o instanceof Double)
			{
				i = JadothTypes.to_int((Double)o);
			}
			else if(o instanceof Long)
			{
				i = JadothTypes.to_int((Long)o);
			}
			else if(o instanceof Float)
			{
				i = JadothTypes.to_int((Float)o);
			}
			else if(o instanceof AtomicInteger)
			{
				i = JadothTypes.to_int((AtomicInteger)o);
			}
			else if(o instanceof AtomicLong)
			{
				i = JadothTypes.to_int((AtomicLong)o);
			}
			else if(o instanceof BigDecimal)
			{
				i = JadothTypes.to_int((BigDecimal)o);
			}
			else if(o instanceof BigInteger)
			{
				i = JadothTypes.to_int((BigInteger)o);
			}
			else
			{
				return false; //all non-number objects can't be removed at all, so return false
			}
		}
		return this.internalRemove(i);
	}

	/**
	 * Removes the all.
	 *
	 * @param c the c
	 * @return true, if successful
	 * @return
	 */
	@Override
	public boolean removeAll(final Collection<?> c)
	{
		boolean changed = false;
		for(final Object o : c)
		{
			changed |= this.remove(o);
		}
		return changed;
	}

	/**
	 * Retain all.
	 *
	 * @param c the c
	 * @return true, if successful
	 * @return
	 */
	@Override
	public boolean retainAll(final Collection<?> c)
	{
		int collectionMin = 0;
		int collectionMax = 0;

		//determine min and max values in collection c
		for(final Object o : c)
		{
			if(o == null || !(o instanceof Number))
			{
				continue;
			}

			int i = 0;
			try
			{
				i = JadothTypes.to_int((Number)o);
			}
			catch(final NumberRangeException e)
			{
				final long l = ((Number)o).longValue();
				if(l > Integer.MAX_VALUE)
				{
					collectionMax = Integer.MAX_VALUE;
				}
				else if(l < Integer.MIN_VALUE)
				{
					collectionMax = Integer.MIN_VALUE;
				}
				continue;
			}
			if(i < collectionMin)
			{
				collectionMin = i;
			}
			else if(i > collectionMax)
			{
				collectionMax = i;
			}
		}

		//depending on direction, determine if min and/or max is inside the current range
		boolean changed = false;
		if(this.direction == 1)
		{
			if(collectionMin > this.from)
			{
				this.from = collectionMin;
				changed = true;
			}
			if(collectionMax < this.to)
			{
				this.to = collectionMax;
				changed = true;
			}
		}
		else
		{
			if(collectionMin < this.from)
			{
				this.from = collectionMin;
				changed = true;
			}
			if(collectionMax > this.to)
			{
				this.to = collectionMax;
				changed = true;
			}
		}
		return changed;
	}

	/**
	 * Size.
	 *
	 * @return the int
	 * @return
	 */
	@Override
	public int size()
	{
		return Math.abs(this.to - this.from) + 1;
	}

	/**
	 * To array.
	 *
	 * @return the integer[]
	 * @return
	 */
	@Override
	public Integer[] toArray()
	{
		final Integer[] array = new Integer[this.size()];
		final int to = this.to;
		final int dir = this.direction;

		int i = 0;
		int loopValue = this.from;
		while(true)
		{
			array[i++] = loopValue;
			if(loopValue == to)
			{
				break;
			}
			loopValue += dir;
		}
		return array;
	}

	/**
	 * To array.
	 *
	 * @param <T> the generic type
	 * @param a the a
	 * @return the t[]
	 */
	@SuppressWarnings("unchecked")
	@Override
	public <T> T[] toArray(final T[] a)
	{
		if(!(a instanceof Integer[]))
		{
			throw new IllegalArgumentException("Array must be of type " + Integer.class.getSimpleName() + "[]");
		}
		final int size = this.size();
		final Integer[] array = a.length >= size ? (Integer[])a : new Integer[size];
		final int to = this.to;
		final int dir = this.direction;

		int i = 0;
		int loopValue = this.from;
		while(true)
		{
			array[i++] = loopValue;
			if(loopValue == to)
			{
				break;
			}
			loopValue += dir;
		}
		return (T[])array;
	}

	@Override
	public String toString()
	{
		return VarString.New().add('[').add(this.from).add(';').add(this.to).add(']').toString();
	}




	///////////////////////////////////////////////////////////////////////////
	// declared methods //
	/////////////////////

	public boolean contains(final int i)
	{
		return this.containsInt(i);
	}

	private boolean containsInt(final int i)
	{
		return this.direction > 0 && i >= this.from && i <= this.to
		    || this.direction < 0 && i <= this.from && i >= this.to
		;
	}

	public int[] toArray_int()
	{
		final int[] array = new int[this.size()];
		final int to = this.to;
		final int dir = this.direction;

		int i = 0;
		int loopValue = this.from;
		while(true)
		{
			array[i++] = loopValue;
			if(loopValue == to)
			{
				break;
			}
			loopValue += dir;
		}
		return array;
	}

	public void process(final _intProcedure procedure)
	{
		final int d = this.direction;
		final int bound = this.to + d;
		try
		{
			for(int i = this.from - d; i != bound;)
			{
				procedure.accept(i += d);
			}
		}
		catch(final ThrowBreak b)
		{
			// abort iteration
		}
	}


	private boolean internalRemove(final int value)
	{
		if(this.from == value)
		{
			if(this.direction == 1)
			{
				this.from++;
			}
			else
			{
				this.from--;
			}
			return true;
		}
		else if(this.to == value)
		{
			if(this.direction == 1)
			{
				this.to--;
			}
			else
			{
				this.to++;
			}
			return true;
		}
		return false;
	}


	///////////////////////////////////////////////////////////////////////////
	// inner classes //
	//////////////////

	/**
	 * The Class RangeIterator.
	 */
	class RangeIterator implements Iterator<Integer>
	{

		/** The cursor. */
		private int cursor = _intRange.this.from;

		/**
		 * Checks for next.
		 *
		 * @return true, if successful
		 * @return
		 */
		@Override
		public boolean hasNext()
		{
			if(_intRange.this.direction == 1)
			{
				return this.cursor <= _intRange.this.to;
			}
			return this.cursor >= _intRange.this.to;
		}

		/**
		 * Next.
		 *
		 * @return the integer
		 * @return
		 */
		@Override
		public Integer next()
		{
			//funny, isn't it?
			try
			{
				return this.cursor;
			}
			finally
			{
				this.cursor += _intRange.this.direction;
			}
		}

		/**
		 * Makes no sense as arbitrary element of a range cannot be removed
		 * @see java.util.Iterator#remove()
		 */
		@Override
		public void remove()
		{
			//Makes no sense as arbitrary element of a range cannot be removed
		}

	}
}



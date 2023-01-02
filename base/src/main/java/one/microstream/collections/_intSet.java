package one.microstream.collections;

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

import one.microstream.chars.VarString;
import one.microstream.exceptions.ArrayCapacityException;
import one.microstream.functional._intProcedure;
import one.microstream.math.XMath;
import one.microstream.typing.Composition;


/**
 * Simple primitive int set implementation.
 */
public final class _intSet implements Composition
{
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////

	public static final _intSet New()
	{
		return new _intSet();
	}

	public static final _intSet New(final int... values)
	{
		// values are assumed to be already (roughly) unique, so length is a good capacity indicator.
		return NewCustom(values.length).addAll(values);
	}

	public static final _intSet NewCustom(final int initialCapacity)
	{
		return new _intSet(XMath.pow2BoundCapped(initialCapacity));
	}

	public static final _intSet NewCustom(final int initialCapacity, final int... values)
	{
		return NewCustom(initialCapacity).addAll(values);
	}

	private static void internalIncreaseLine(final int[][] lines, final int range, final int[] line, final int value)
	{
		// note that it CANNOT happen mathematically that line has max int length
		final int[] newLine;
		System.arraycopy(line, 0, newLine = new int[line.length << 1], 0, line.length);
		newLine[line.length] = value;
		lines[value & range] = newLine;
	}

	private static int[][] internalCreateLines()
	{
		return new int[1][]; // prefer tiny default footprint
	}

	private static int[][] internalCreateLines(final int length)
	{
		return new int[length][];
	}

	private static int[] internalCreateLine(final int initialValue)
	{
		return new int[]{initialValue}; // prefer tiny default footprint
	}



	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	private int     size, range, capLower, capUpper;
	private int[][] lines                          ;

	// 0 is treated as "null" in algorithms, so 0 as value has to be special-cased.
	private boolean has0                           ;



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	private _intSet()
	{
		super();
		this.size     = 0;
		this.lines    = internalCreateLines();
		this.range    = 0;
		this.capLower = 0; // can never be undercut by size
		this.capUpper = 1;
	}

	private _intSet(final int uncheckedInitialCapacity)
	{
		super();
		this.capLower = uncheckedInitialCapacity >>> 1; // capacity 1 yields 0, which is correct.
		this.capUpper = XMath.isGreaterThanOrEqualHighestPowerOf2(uncheckedInitialCapacity)
			? Integer.MAX_VALUE
			: uncheckedInitialCapacity
		;
		this.range    = uncheckedInitialCapacity - 1;
		this.lines    = internalCreateLines(uncheckedInitialCapacity);
		this.size     = 0;
	}



	///////////////////////////////////////////////////////////////////////////
	// declared methods //
	/////////////////////

	private void internalRebuildStorage(final int newLength)
	{
		final int[][] oldLines = this.lines, newLines = internalCreateLines(newLength);
		final int newRange = newLength - 1;

		for(final int[] oldLine : oldLines)
		{
			if(oldLine == null)
			{
				continue;
			}
			oldLine:
			for(final int value : oldLine)
			{
				final int[] newLine;
				if((newLine = newLines[value & newRange]) == null)
				{
					newLines[value & newRange] = internalCreateLine(value);
					continue oldLine;
				}
				for(int i = 0; i < newLine.length; i++)
				{
					if(newLine[i] == 0)
					{
						newLine[i] = value;
						continue oldLine;
					}
				}
				internalIncreaseLine(newLines, newRange, newLine, value);
			}
		}

		this.lines    = newLines;
		this.range    = newRange;
		this.capLower = newLength >> 1;
		this.capUpper = XMath.isGreaterThanOrEqualHighestPowerOf2(newLength)
			? Integer.MAX_VALUE
			: newLength
		;
	}

	private void internalIncreaseLine(final int[] line, final int value)
	{
		this.internalCheckSize();
		internalIncreaseLine(this.lines, this.range, line, value);
		this.internalIncrementSize();
	}

	private void internalAddToLine(final int[] line, final int index, final int value)
	{
		this.internalCheckSize();
		line[index] = value;
		this.internalIncrementSize();
	}

	private void internalAddNewLine(final int value)
	{
		this.internalCheckSize();
		this.lines[value & this.range] = internalCreateLine(value);
		this.internalIncrementSize();
	}

	private boolean internalAdd0()
	{
		if(this.has0)
		{
			return false;
		}
		this.internalCheckSize();
		this.has0 = true;
		this.internalIncrementSize();
		return true;
	}

	private boolean internalRemove0()
	{
		if(this.has0)
		{
			this.has0 = false;
			this.internalDecrementSize(); // for consistency of size checks
			return true;
		}
		return false;
	}

	private void internalRemoveFromLine(final int[] line, final int index)
	{
		if(index < line.length - 1)
		{
			System.arraycopy(line, index + 1, line, index, line.length - index - 1);
		}
		line[line.length - 1] = 0;
		this.internalDecrementSize();
	}

	private void internalCheckSize()
	{
		if(this.size >= Integer.MAX_VALUE)
		{
			throw new ArrayCapacityException();
		}
	}

	private void internalIncrementSize()
	{
		if(this.size++ >= this.capUpper)
		{
			this.internalRebuildStorage(this.capUpper << 1);
		}
	}

	private void internalDecrementSize()
	{
		if(--this.size < this.capLower)
		{
			this.internalRebuildStorage(this.capLower);
		}
	}



	public final int size()
	{
		return this.size;
	}

	public final boolean contains(final int value)
	{
		if(value == 0)
		{
			return this.has0;
		}
		final int[] line;
		if((line = this.lines[value & this.range]) == null)
		{
			return false;
		}
		for(final int i : line)
		{
			if(i == value)
			{
				return true;
			}
			if(i == 0)
			{
				break;
			}
		}
		return false;
	}

	public final _intSet addAll(final int... values)
	{
		for(final int i : values)
		{
			this.add(i);
		}
		return this;
	}

	public final boolean add(final int value)
	{
		if(value == 0)
		{
			// case: 0 value
			return this.internalAdd0();
		}

		final int[] line;
		if((line = this.lines[value & this.range]) == null)
		{
			// case: no hash line at all, yet
			this.internalAddNewLine(value);
			return true;
		}
		for(final int i : line)
		{
			if(i == value)
			{
				// case: value already contained
				return false;
			}
			if(i == 0)
			{
				// case: new value and hash line has still enough room
				this.internalAddToLine(line, i, value);
				return true;
			}
		}
		this.internalIncreaseLine(line, value); // case: new value and hash line requires increase
		return true;
	}

	public final boolean remove(final int value)
	{
		if(value == 0)
		{
			return this.internalRemove0();
		}

		final int[] line;
		if((line = this.lines[value & this.range]) == null)
		{
			return false;
		}
		for(int i = 0; i < line.length; i++)
		{
			if(line[i] == value)
			{
				this.internalRemoveFromLine(line, i);
				return true;
			}
			if(line[0] == 0)
			{
				break;
			}
		}
		return false;
	}

	public final void clear()
	{
		final int[][] lines = this.lines;
		for(int i = 0; i < lines.length; i++)
		{
			lines[i] = null;
		}
		this.size = 0;
	}

	public final _intSet ensureFreeCapacity(final int freeCapacity)
	{
		if(Integer.MAX_VALUE - freeCapacity < this.size)
		{
			throw new ArrayCapacityException();
		}
		if(this.capUpper - freeCapacity < this.size)
		{
			this.internalRebuildStorage(XMath.pow2BoundCapped(this.size + freeCapacity));
		}
		return this;
	}

	public final <P extends _intProcedure> P iterate(final P procedure)
	{
		if(this.has0)
		{
			procedure.accept(0);
		}
		for(final int[] line : this.lines)
		{
			if(line == null)
			{
				continue;
			}
			for(final int value : line)
			{
				if(value == 0)
				{
					break;
				}
				procedure.accept(value);
			}
		}
		return procedure;
	}

	public final int[] toArray()
	{
		final int[] array = new int[this.size];
		int a = 0;

		if(this.has0)
		{
			array[a++] = 0;
		}
		for(final int[] line : this.lines)
		{
			if(line == null)
			{
				continue;
			}
			for(final int value : line)
			{
				if(value == 0)
				{
					break;
				}
				array[a++] = value;
			}
		}

		return array;
	}

	@Override
	public final String toString()
	{
		if(this.size == 0)
		{
			return "[]";
		}

		final VarString vs = VarString.New((int)(this.size * 2.0f)).add('[');
		if(this.has0)
		{
			vs.add(0).add(',');
		}
		for(final int[] line : this.lines)
		{
			if(line == null)
			{
				continue;
			}
			for(final int value : line)
			{
				if(value == 0)
				{
					break;
				}
				vs.add(value).add(',');
			}
		}
		return vs.setLast(']').toString();
	}

}

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

final class CharConversionIntegers
{
	///////////////////////////////////////////////////////////////////////////
	// constants //
	//////////////

	private static final int
		V001G = 1_000_000_000,
		V100M =   100_000_000,
		V010M =    10_000_000,
		V001M =     1_000_000,
		V100K =       100_000,
		V010K =        10_000,
		V001K =         1_000,
		V100  =           100,
		V010  =            10
	;
	private static final long
		V001Z = 1_000_000_000_000_000_000L,
		V100P =   100_000_000_000_000_000L,
		V010P =    10_000_000_000_000_000L,
		V001P =     1_000_000_000_000_000L,
		V100T =       100_000_000_000_000L,
		V010T =        10_000_000_000_000L,
		V001T =         1_000_000_000_000L,
		V100G =           100_000_000_000L,
		V010G =            10_000_000_000L
	;



	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////

	static final int put_byte(final byte value, final char[] target, final int offset)
	{
		// pure algorithm method intentionally without array bounds check. Use VarString etc. for that.
		if(value < 0)
		{
			return put_intPositive5(-value, target, putMinus(target, offset));
		}
		return put_intPositive5(value, target, offset); // normal case
	}

	static final int put_short(final short value, final char[] target, final int offset)
	{
		// pure algorithm method intentionally without array bounds check. Use VarString etc. for that.
		if(value < 0)
		{
			return put_intPositive5(-value, target, putMinus(target, offset));
		}
		return put_intPositive5(value, target, offset); // normal case
	}

	static final int put_int(final int value, final char[] target, final int offset)
	{
		// pure algorithm method intentionally without array bounds check. Use VarString etc. for that.
		if(value < 0)
		{
			return put_intNegative(value, target, offset);
		}
		return put_intPositive(value, target, offset); // normal case
	}

	static final int put_long(final long value, final char[] target, final int offset)
	{
		// pure algorithm method intentionally without array bounds check. Use VarString etc. for that.
		if(value < 0)
		{
			return put_longNegative(value, target, offset);
		}
		return put_longPositive(value, target, offset); // normal case
	}

	private static int putMinus(final char[] target, final int offset)
	{
		target[offset] = '-';
		return offset + 1;
	}

	private static int put1Char(final int singleDigitValue, final char[] target, final int offset)
	{
		target[offset] = (char)('0' + singleDigitValue);
		return offset + 1;
	}

	private static int put2Chars(final int doubleDigitValue, final char[] target, final int offset)
	{
		target[offset    ] = XChars.DECIMAL_CHAR_TABLE_10S[doubleDigitValue];
		target[offset + 1] = XChars.DECIMAL_CHAR_TABLE_01S[doubleDigitValue];
		return offset + 2;
	}

	private static int put_longNegative(final long value, final char[] target, final int offset)
	{
		if(value == Long.MIN_VALUE)
		{
			// unnegatable special negative case
			System.arraycopy(XChars.CHARS_MIN_VALUE_long, 0, target, offset, XChars.MAX_CHAR_COUNT_long);
			return offset + XChars.MAX_CHAR_COUNT_long;
		}
		return put_longPositive(-value, target, putMinus(target, offset)); // standard negative case normalization
	}

	private static int put_intNegative(final int value, final char[] target, final int offset)
	{
		if(value == Integer.MIN_VALUE)
		{
			// unnegatable special negative case
			System.arraycopy(XChars.CHARS_MIN_VALUE_int, 0, target, offset, XChars.MAX_CHAR_COUNT_int);
			return offset + XChars.MAX_CHAR_COUNT_int;
		}
		return put_intPositive(-value, target, putMinus(target, offset)); // standard negative case normalization
	}

	static final int put_longPositive(final long value, final char[] target, final int offset)
	{
		if(value >= V001G)
		{
			if(value >= V001Z)
			{
				return put_longPositiveHI(value % V001Z, target, put1Char((int)(value / V001Z), target, offset));
			}
			if(value >= V100P)
			{
				return put_longPositiveHI(value, target, offset);
			}
			if(value >= V010P)
			{
				return put_longPositiveFG(value % V010P, target, put1Char((int)(value / V010P), target, offset));
			}
			if(value >= V001P)
			{
				return put_longPositiveFG(value, target, offset);
			}
			if(value >= V100T)
			{
				return put_longPositiveDE(value % V100T, target, put1Char((int)(value / V100T), target, offset));
			}
			if(value >= V010T)
			{
				return put_longPositiveDE(value, target, offset);
			}
			if(value >= V001T)
			{
				return put_longPositiveBC(value % V001T, target, put1Char((int)(value / V001T), target, offset));
			}
			if(value >= V100G)
			{
				return put_longPositiveBC(value, target, offset);
			}
			if(value >= V010G)
			{
				return put_longPositive9A(value % V010G, target, put1Char((int)(value / V010G), target, offset));
			}
			return put_longPositive9A(value, target, offset);
		}
		return put_intPositive((int)value, target, offset);
	}

	static final int put_intPositive(final int value, final char[] target, final int offset)
	{
		/* notes:
		 * - this algorithm is about twice as fast as the one in JDK in all situations
		 *   (even in worst case range [10000; 100000[)
		 * - using redundant % instead of calculating quotient and rest is suprisingly marginally faster
		 * - any more tinkering in either direction of more inlining or more abstraction
		 *   always yielded worse performance
		 * - a three-digit cache (3x 1000 chars) algorithm might improve assembly performance,
		 *   but complicates the intermediate cases and costs a lot of memory (~6kb)
		 * - a full binary decision tree instead of simple bisection made overall performance worse
		 * - separation in sub-methods surprisingly makes performance a lot worse
		 */
		if(value >= V100K)
		{
			if(value >= V001G)
			{
				return put_intPositive9m(value, target, offset);
			}
			if(value >= V100M)
			{
				return put_intPositive78(value % V100M, target, put1Char(value / V100M, target, offset));
			}
			if(value >= V010M)
			{
				return put_intPositive78(value, target, offset);
			}
			if(value >= V001M)
			{
				return put_intPositive56(value % V001M, target, put1Char(value / V001M, target, offset));
			}
			return put_intPositive56(value, target, offset);
		}
		return put_intPositive5(value, target, offset);
	}

	private static int put_intPositive5(final int value, final char[] target, final int offset)
	{
		if(value >= V100)
		{
			if(value >= V010K)
			{
				return put_intPositive43(value % V010K, target, put1Char(value / V010K, target, offset));
			}
			if(value >= V001K)
			{
				return put_intPositive43(value, target, offset);
			}
			return put2Chars(value % V100, target, put1Char(value / V100, target, offset));
		}
		return put_intPositive21(value, target, offset);
	}

	private static int put_longPositiveHI(final long value, final char[] target, final int offset)
	{
		return put_longPositiveFG(value % V010P, target, put2Chars((int)(value / V010P), target, offset));
	}

	private static int put_longPositiveFG(final long value, final char[] target, final int offset)
	{
		return put_longPositiveDE(value % V100T, target, put2Chars((int)(value / V100T), target, offset));
	}

	private static int put_longPositiveDE(final long value, final char[] target, final int offset)
	{
		return put_longPositiveBC(value % V001T, target, put2Chars((int)(value / V001T), target, offset));
	}

	private static int put_longPositiveBC(final long value, final char[] target, final int offset)
	{
		return put_longPositive9A(value % V010G, target, put2Chars((int)(value / V010G), target, offset));
	}

	private static int put_longPositive9A(final long value, final char[] target, final int offset)
	{
		return put_intPositive78((int)(value % V100M), target, put2Chars((int)(value / V100M), target, offset));
	}

	private static int put_intPositive9m(final int value, final char[] target, final int offset)
	{
		return put_intPositive78(value % V100M, target, put2Chars(value / V100M, target, offset));
	}

	private static int put_intPositive78(final int value, final char[] target, final int offset)
	{
		return put_intPositive56(value % V001M, target, put2Chars(value / V001M, target, offset));
	}

	private static int put_intPositive56(final int value, final char[] target, final int offset)
	{
		return put_intPositive43(value % V010K, target, put2Chars(value / V010K, target, offset));
	}

	private static int put_intPositive43(final int value, final char[] target, final int offset)
	{
		return put2Chars(value % V100, target, put2Chars(value / V100, target, offset));
	}

	private static int put_intPositive21(final int value, final char[] target, final int offset)
	{
		return value >= V010
			? put2Chars(value, target, offset)
			: put1Char (value, target, offset)
		;
	}

	static final int put_int3(final int value, final char[] target, final int offset)
	{
		if(value >= V100)
		{
			return put2Chars(value % V100, target, put1Char(value / V100, target, offset));
		}
		return value >= V010
			? put2Chars(value, target, offset)
			: put1Char (value, target, offset)
		;
	}

	

	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	/**
	 * Dummy constructor to prevent instantiation of this static-only utility class.
	 * 
	 * @throws UnsupportedOperationException when called
	 */
	private CharConversionIntegers()
	{
		// static only
		throw new UnsupportedOperationException();
	}
}

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

import one.microstream.memory.XMemory;

public final class MemoryCharConversionIntegers
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

	// CHECKSTYLE.OFF: ConstantName: type names are intentionally unchanged

	static final transient int
		BYTE_SIZE_CHAR       = XMemory.byteSize_char()                           ,
		BYTE_SIZE_DOUBLECHAR = 2 * BYTE_SIZE_CHAR                               ,
		MAX_BYTE_COUNT_byte  = BYTE_SIZE_CHAR * XChars.MAX_CHAR_COUNT_byte ,
		MAX_BYTE_COUNT_short = BYTE_SIZE_CHAR * XChars.MAX_CHAR_COUNT_short,
		MAX_BYTE_COUNT_int   = BYTE_SIZE_CHAR * XChars.MAX_CHAR_COUNT_int  ,
		MAX_BYTE_COUNT_long  = BYTE_SIZE_CHAR * XChars.MAX_CHAR_COUNT_long
	;

	// CHECKSTYLE.ON: ConstantName



	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////

	public static final long put_byte(final byte value, final long address)
	{
		// pure algorithm method intentionally without array bounds check. Use VarString etc. for that.
		if(value < 0)
		{
			return put_intPositive5(-value, putMinus(address));
		}
		return put_intPositive5(value, address); // normal case
	}

	public static final long put_short(final short value, final long address)
	{
		// pure algorithm method intentionally without array bounds check. Use VarString etc. for that.
		if(value < 0)
		{
			return put_intPositive5(-value, putMinus(address));
		}
		return put_intPositive5(value, address); // normal case
	}

	public static final long put_int(final int value, final long address)
	{
		// pure algorithm method intentionally without array bounds check. Use VarString etc. for that.
		if(value < 0)
		{
			return put_intNegative(value, address);
		}
		return put_intPositive(value, address); // normal case
	}

	public static final long put_long(final long value, final long address)
	{
		// pure algorithm method intentionally without array bounds check. Use VarString etc. for that.
		if(value < 0)
		{
			return put_longNegative(value, address);
		}
		return put_longPositive(value, address); // normal case
	}

	private static long putMinus(final long address)
	{
		XMemory.set_char(address, '-');
		return address + BYTE_SIZE_CHAR;
	}

	private static long put1Char(final int singleDigitValue, final long address)
	{
		XMemory.set_char(address, (char)('0' + singleDigitValue));
		return address + BYTE_SIZE_CHAR;
	}

	private static long put2Chars(final int doubleDigitValue, final long address)
	{
		XMemory.set_char(address                 , XChars.DECIMAL_CHAR_TABLE_10S[doubleDigitValue]);
		XMemory.set_char(address + BYTE_SIZE_CHAR, XChars.DECIMAL_CHAR_TABLE_01S[doubleDigitValue]);
		return address + BYTE_SIZE_DOUBLECHAR;
	}

	private static long put_longNegative(final long value, final long address)
	{
		if(value == Long.MIN_VALUE)
		{
			// unnegatable special negative case
			XMemory.copyArrayToAddress(XChars.CHARS_MIN_VALUE_long, address);
			return address + MAX_BYTE_COUNT_long;
		}
		return put_longPositive(-value, putMinus(address)); // standard negative case normalization
	}

	private static long put_intNegative(final int value, final long address)
	{
		if(value == Integer.MIN_VALUE)
		{
			// unnegatable special negative case
			XMemory.copyArrayToAddress(XChars.CHARS_MIN_VALUE_int, address);
			return address + MAX_BYTE_COUNT_int;
		}
		return put_intPositive(-value, putMinus(address)); // standard negative case normalization
	}

	public static final long put_longPositive(final long value, final long address)
	{
		if(value >= V001G)
		{
			if(value >= V001Z
				)
			{
				return put_longPositiveHI(value % V001Z, put1Char((int)(value / V001Z), address));
			}
			if(value >= V100P)
			{
				return put_longPositiveHI(value, address);
			}
			if(value >= V010P)
			{
				return put_longPositiveFG(value % V010P, put1Char((int)(value / V010P), address));
			}
			if(value >= V001P)
			{
				return put_longPositiveFG(value, address);
			}
			if(value >= V100T)
			{
				return put_longPositiveDE(value % V100T, put1Char((int)(value / V100T), address));
			}
			if(value >= V010T)
			{
				return put_longPositiveDE(value, address);
			}
			if(value >= V001T)
			{
				return put_longPositiveBC(value % V001T, put1Char((int)(value / V001T), address));
			}
			if(value >= V100G)
			{
				return put_longPositiveBC(value, address);
			}
			if(value >= V010G)
			{
				return put_longPositive9A(value % V010G, put1Char((int)(value / V010G), address));
			}
			return put_longPositive9A(value, address);
		}
		return put_intPositive((int)value, address);
	}

	private static long put_intPositive(final int value, final long address)
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
				return put_intPositive9m(value, address);
			}
			if(value >= V100M)
			{
				return put_intPositive78(value % V100M, put1Char(value / V100M, address));
			}
			if(value >= V010M)
			{
				return put_intPositive78(value, address);
			}
			if(value >= V001M)
			{
				return put_intPositive56(value % V001M, put1Char(value / V001M, address));
			}
			return put_intPositive56(value, address);
		}
		return put_intPositive5(value, address);
	}

	private static long put_intPositive5(final int value, final long address)
	{
		if(value >= V100)
		{
			if(value >= V010K)
			{
				return put_intPositive43(value % V010K, put1Char(value / V010K, address));
			}
			if(value >= V001K)
			{
				return put_intPositive43(value, address);
			}
			return put2Chars(value % V100, put1Char(value / V100, address));
		}
		return put_intPositive21(value, address);
	}

	private static long put_longPositiveHI(final long value, final long address)
	{
		return put_longPositiveFG(value % V010P, put2Chars((int)(value / V010P), address));
	}

	private static long put_longPositiveFG(final long value, final long address)
	{
		return put_longPositiveDE(value % V100T, put2Chars((int)(value / V100T), address));
	}

	private static long put_longPositiveDE(final long value, final long address)
	{
		return put_longPositiveBC(value % V001T, put2Chars((int)(value / V001T), address));
	}

	private static long put_longPositiveBC(final long value, final long address)
	{
		return put_longPositive9A(value % V010G, put2Chars((int)(value / V010G), address));
	}

	private static long put_longPositive9A(final long value, final long address)
	{
		return put_intPositive78((int)(value % V100M), put2Chars((int)(value / V100M), address));
	}

	private static long put_intPositive9m(final int value, final long address)
	{
		return put_intPositive78(value % V100M, put2Chars(value / V100M, address));
	}

	private static long put_intPositive78(final int value, final long address)
	{
		return put_intPositive56(value % V001M, put2Chars(value / V001M, address));
	}

	private static long put_intPositive56(final int value, final long address)
	{
		return put_intPositive43(value % V010K, put2Chars(value / V010K, address));
	}

	private static long put_intPositive43(final int value, final long address)
	{
		return put2Chars(value % V100, put2Chars(value / V100, address));
	}

	private static long put_intPositive21(final int value, final long address)
	{
		return value >= V010
			? put2Chars(value, address)
			: put1Char (value, address)
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
	private MemoryCharConversionIntegers()
	{
		// static only
		throw new UnsupportedOperationException();
	}

}

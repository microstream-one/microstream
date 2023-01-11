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


public final class MemoryCharConversion_doubleUTF8
{
	// CHECKSTYLE.OFF: MagicNumber: Arithmetics are better readable with direct values
	
	///////////////////////////////////////////////////////////////////////////
	// constants //
	//////////////

	private static final transient double
		DOUBLE_NORMALIZATION_THRESHOLD_HIGH = 10_000_000.0  ,
		DOUBLE_NORMALIZATION_THRESHOLD_LOW  =          0.001,
		DOUBLE_ONE                          =          1.0  ,
		DOUBLE_ZERO                         =          0.0  ,
		DOUBLE_E100                         =          1E100,
		DOUBLE_E10                          =          1E10 ,
		DOUBLE_LAST_DIGIT0                  =          1E-19,
		DOUBLE_LAST_DIGIT1                  =          1E-18,
		DOUBLE_LAST_DIGIT2                  =          1E-17
	;

	private static final transient byte[]
		CHARS_ZERO              = toSingleCharBytes(XChars.CHARS_ZERO             ),
		CHARS_ONE               = toSingleCharBytes(XChars.CHARS_ONE              ),
		CHARS_NAN               = toSingleCharBytes(XChars.CHARS_NAN              ),
		CHARS_NEGATIVE_INFINITY = toSingleCharBytes(XChars.CHARS_NEGATIVE_INFINITY),
		CHARS_POSITIVE_INFINITY = toSingleCharBytes(XChars.CHARS_POSITIVE_INFINITY),
		CHARS_NORM_THRESH_HIGH  = toSingleCharBytes(XChars.CHARS_NORM_THRESH_HIGH )
	;

	private static final transient int
		DOUBLE_DIGITS_MAX   = 17,
		DOUBLE_DIGITS_BOUND = 16
	;

	private static final transient byte
		ZERO  = (byte)'0',
		ONE   = (byte)'1',
		TWO   = (byte)'2',
		FIVE  = (byte)'5',
		SEVEN = (byte)'7',
		EIGHT = (byte)'8',
		NINE  = (byte)'9',
		MINUS = (byte)'-',
		DOT   = (byte)'.',
		E     = (byte)'E'
	;



	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////

	private static byte[] toSingleCharBytes(final char[] chars)
	{
		final byte[] bytes = new byte[chars.length];
		for(int i = 0; i < chars.length; i++)
		{
			bytes[i] = (byte)chars[i];
		}
		return bytes;
	}

	/**
	 * This algorithm is somewhere from 3 times to 25 times (depending on exponent) faster than the
	 * code used in JDK. It generates discrepancies of +/- 1 at the 16th digit and +/- 8 in the
	 * 17th digit compared to the stricter JDK algorithm. As digits 16 and 17 cannot be relied upon anyway due to
	 * the technical floating point inaccuracy (e.g. the JDK double parser generates comparable discrepancies),
	 * the algorithm is still deemed correct ("enough").
	 * <p>
	 * Otherwise, the behavior of the algorithm is the same as that of the JDK implementation (i.e. decimal point
	 * character '.', denormalized representation in range ]1E7; 1E-3], exponent character 'E', etc.
	 * <p>
	 * Note that this method is intended as an implementation detail and a "know-what-you-are-doing" tool that does
	 * not perform array bound checks. If array bound checking is desired, use {@link VarString#add(double)} explicitly.
	 *
	 * @param value the value to be represented as a character sequence.
	 * @param address the memory address where the character sequence shall start.
	 * @return the address pointing to after the last character of the assembled sequence.
	 */
	public static final long put(final double value, final long address)
	{
		// pure algorithm method intentionally without array bounds check. Use VarString etc. for that.

		// head method with a bunch of special case handling
		if(Double.isNaN(value))
		{
			XMemory.copyArrayToAddress(CHARS_NAN, address);
			return address + CHARS_NAN.length;
		}
		if(value < DOUBLE_ZERO)
		{
			if(value == Double.NEGATIVE_INFINITY)
			{
				XMemory.copyArrayToAddress(CHARS_NEGATIVE_INFINITY, address);
				return address + CHARS_NEGATIVE_INFINITY.length;
			}
			XMemory.set_byte(address, MINUS);
			return put_doublePositive(-value, address + 1);
		}
		if(value == DOUBLE_ZERO)
		{
			// this case is so common that is pays off to handle it specifically
			XMemory.copyArrayToAddress(CHARS_ZERO, address);
			return address + CHARS_ZERO.length;
		}
		if(value == DOUBLE_ONE)
		{
			// this case is so common that is pays off to handle it specifically
			XMemory.copyArrayToAddress(CHARS_ONE, address);
			return address + CHARS_ONE.length;
		}
		if(value == Double.POSITIVE_INFINITY)
		{
			XMemory.copyArrayToAddress(CHARS_POSITIVE_INFINITY, address);
			return address + CHARS_POSITIVE_INFINITY.length;
		}
		return put_doublePositive(value, address);
	}

	private static long put_doublePositive(final double value, final long address)
	{
		return value < DOUBLE_ONE
			? put_doubleLt1(value, address)
			: put_doubleGte1(value, address)
		;
	}

	private static long put_doubleLt1(final double value, final long address)
	{
		return value < DOUBLE_NORMALIZATION_THRESHOLD_LOW
			? put_doubleLt1Normalized(value, address)
			: put_doubleLt1Denormalized(value, address)
		;
	}

	private static long put_doubleGte1(final double value, final long address)
	{
		return value < DOUBLE_NORMALIZATION_THRESHOLD_HIGH
			? put_doubleGte1Denormalized(value, address)
			: put_doubleGte1Normalized(value, address)
		;
	}

	private static long put_doubleGte1Denormalized(final double value, final long address)
	{
		final int exponent = exponent(value);
		XMemory.set_byte(address, ZERO); // overflow handling digit, gets replaced by decimal point

		// "extremely annoying special corner case" cannot happen in this range
		final long i = put_doubleAndCleanup(value * pow10(DOUBLE_DIGITS_BOUND - exponent), address + 1);
		if(XMemory.get_byte(address) != ZERO)
		{
			return handle_doubleGte1DenormSpecialCase(address, exponent);
		}
		XMemory.copyRange(address + 1, address, exponent + 1);
		XMemory.set_byte(address + exponent + 1, DOT);
		return Math.max(i, address + exponent + 3);
	}

	private static long handle_doubleGte1DenormSpecialCase(final long address, final int exponent)
	{
		if(exponent == 7)
		{
			XMemory.copyArrayToAddress(CHARS_NORM_THRESH_HIGH, address);
			return address + CHARS_NORM_THRESH_HIGH.length;
		}
		
		final int e = Math.max(exponent, 1) + 1;
		for(int i = 1; i <= e; i++)
		{
			XMemory.set_byte(address + 1, ZERO);
		}
		XMemory.set_byte(address + e    , DOT );
		XMemory.set_byte(address + e + 1, ZERO);

		return address + e + 2;
	}

	private static long putSimpleCharacterString(final String s, final long address)
	{
		final char[] chars = XChars.readChars(s);

		for(int i = 0; i < chars.length; i++)
		{
			XMemory.set_byte(address + i, (byte)chars[i]);
		}

		return address + chars.length;
	}

	private static long put_doubleGte1Normalized(final double value, final long address)
	{
		final int exponent = exponent(value);
		final double intedValue = value * (exponent < DOUBLE_DIGITS_MAX
			? pow10(DOUBLE_DIGITS_BOUND - exponent)
			: root10(DOUBLE_DIGITS_BOUND - exponent))
		;

		// check for extremely annoying special corner case that requires strict algorithm
		if(intedValue == Double.POSITIVE_INFINITY || intedValue == Double.NEGATIVE_INFINITY)
		{
			return putSimpleCharacterString(Double.toString(value), address);
		}

		XMemory.set_byte(address, ZERO); // overflow handling digit, gets replaced by decimal point
		long i = put_doubleAndCleanup(intedValue, address + 1);
		if(XMemory.get_byte(address) != ZERO)
		{
			XMemory.set_byte(address + 1, DOT);
			XMemory.set_byte(address + 2, ZERO);
			i = address + 3;
		}
		else
		{
			XMemory.set_byte(address    , XMemory.get_byte(address + 1));
			XMemory.set_byte(address + 1, DOT);
			if(i == address + 2)
			{
				i++;
			}
		}

		XMemory.set_byte(i, E); // put exponent symbol
		return MemoryCharConversionIntegersUTF8.put_int3(exponent, i + 1);
	}

	private static long put_doubleLt1Denormalized(final double value, final long address)
	{
		// "extremely annoying special corner case" cannot happen in this range

		// this little decimal point detour auto-handles the case of overflowing to 1.0
		XMemory.set_byte(address + 1, ZERO);
		long i = put_doubleLt1DenormAndCleanup(value, address);
		XMemory.set_byte(address    , XMemory.get_byte(address + 1));
		XMemory.set_byte(address + 1, DOT);
		if(i == address + 2)
		{
			XMemory.set_byte(i++, ZERO);
		}
		return i;
	}

	private static long put_doubleLt1DenormAndCleanup(final double value, final long address)
	{
		switch(exponent(value))
		{
			case -3:
			{
				XMemory.set_byte(address + 2, ZERO);
				XMemory.set_byte(address + 3, ZERO);
				return put_doubleAndCleanup(value / DOUBLE_LAST_DIGIT0, address + 4);
			}
			case -2:
			{
				XMemory.set_byte(address + 2, ZERO);
				return put_doubleAndCleanup(value / DOUBLE_LAST_DIGIT1, address + 3);
			}
			default:
			{
				// can only be case exponent == -1
				return put_doubleAndCleanup(value / DOUBLE_LAST_DIGIT2, address + 2);
			}
		}
	}

	private static long put_doubleLt1Normalized(final double value, final long address)
	{
		final int exponent = exponent(value);
		final double intedValue = value * pow10(DOUBLE_DIGITS_BOUND - exponent);

		// check for extremely annoying special corner case that requires strict algorithm
		if(intedValue == Double.POSITIVE_INFINITY || intedValue == Double.NEGATIVE_INFINITY)
		{
			return putSimpleCharacterString(Double.toString(value), address);
		}

		long i = put_doubleAndCleanup(intedValue, address + 1);
		XMemory.set_byte(address    , XMemory.get_byte(address + 1)); // shift first digit to the left
		XMemory.set_byte(address + 1, DOT); // insert decimal point
		if(i == address + 2)
		{
			XMemory.set_byte(i++, ZERO); // fix accidentally cleaned first 0.
		}
		XMemory.set_byte(i    , E    ); // put exponent symbol
		XMemory.set_byte(i + 1, MINUS); // lower 1 exponents always require a sign
		return MemoryCharConversionIntegersUTF8.put_int3(-exponent, i + 2);
	}

	private static int exponent(final double value)
	{
		return (int)Math.floor(Math.log10(value));
	}

	private static double pow10(final int exponent)
	{
		/* cascading causes less multiplications and therefore more speed and less error
		 * E.g.: for exponent 299 (worst case) it executes 20 multiplications instead of 299
		 */
		double result = 1;
		
		int e = exponent;
		while(e >= 100)
		{
			e -= 100;
			result *= DOUBLE_E100;
		}
		while(e >= 10)
		{
			e -= 10;
			result *= DOUBLE_E10;
		}
		while(e-- > 0)
		{
			result *= 10;
		}
		return result;
	}

	private static double root10(final int exponent)
	{
		double result = 1;
		
		int e = exponent;
		while(e < -99)
		{
			e += 100;
			result /= DOUBLE_E100;
		}
		while(e < -9)
		{
			e += 10;
			result /= DOUBLE_E10;
		}
		while(e++ <= 0)
		{
			result /= 10;
		}
		return result;
	}

	private static long put_doubleAndCleanup(final double value, final long address)
	{
		return cleanupDecimal(address, MemoryCharConversionIntegersUTF8.put_longPositive((long)value, address));
	}

	private static long cleanupDecimal(final long address, final long i)
	{
		switch((int)(i - address))
		{
			case DOUBLE_DIGITS_MAX:
			{
				return removeTrailingLast(i - 1);
			}
			case DOUBLE_DIGITS_BOUND:
			{
				return removeTrailingPreLast(i - 1);
			}
			default:
			{
				return XMemory.get_byte(i - 1) == NINE
					? removeTrailingNinesSimple(i - 1)
					: removeTrailingZerosSimple(i - 1)
				;
			}
		}
	}

	private static long removeTrailingZerosSimple(final long address)
	{
		long a = address;
		while(XMemory.get_byte(a) == ZERO)
		{
			a--;
		}
		return a + 1;
	}

	private static long removeTrailingLast(final long address)
	{
		// first condition is a workaround for some nasty special cases
		if(XMemory.get_byte(address - 3) == NINE && XMemory.get_byte(address - 2) == NINE && XMemory.get_byte(address - 1) >= SEVEN)
		{
			return removeTrailingNinesSimple(address - 3);
		}
		if(XMemory.get_byte(address - 3) == ZERO && XMemory.get_byte(address - 2) == ZERO && XMemory.get_byte(address - 1) <= TWO)
		{
			return removeTrailingZerosSimple(address - 3);
		}
		if(XMemory.get_byte(address - 2) == NINE && XMemory.get_byte(address - 1) >= EIGHT)
		{
			// '8' because 16th digit can be off by +/- 1.
			return removeTrailingNinesSimple(address - 2);
		}
		if(XMemory.get_byte(address - 2) == ZERO && XMemory.get_byte(address - 1) <= ONE)
		{
			// 1 because 16th digit can be off by +/- 1
			return removeTrailingZerosSimple(address - 2);
		}
		if(XMemory.get_byte(address - 1) == NINE && XMemory.get_byte(address) >= FIVE)
		{
			// simple rounding for 17th digit
			XMemory.set_byte(address - 2, (byte)(XMemory.get_byte(address - 2) + 1));
			return address - 1;
		}
		if(XMemory.get_byte(address - 1) == ZERO && XMemory.get_byte(address) < FIVE)
		{
			// simple rounding for 17th digit
			return address - 1;
		}
		if(XMemory.get_byte(address) >= FIVE)
		{
			// simple rounding for 17th digit
			XMemory.set_byte(address - 1, (byte)(XMemory.get_byte(address - 1) + 1));
			return address;
		}
		return address;
	}

	private static long removeTrailingPreLast(final long address)
	{
		// first condition is a workaround for some nasty special cases
		if(XMemory.get_byte(address - 2) == NINE && XMemory.get_byte(address - 1) == NINE && XMemory.get_byte(address - 1) >= SEVEN)
		{
			return removeTrailingNinesSimple(address - 2);
		}
		if(XMemory.get_byte(address - 2) == ZERO && XMemory.get_byte(address - 1) == ZERO && XMemory.get_byte(address) <= TWO)
		{
			return removeTrailingZerosSimple(address - 2);
		}
		if(XMemory.get_byte(address - 1) == NINE && XMemory.get_byte(address) >= EIGHT)
		{
			// '8' because 16th digit can be off by +/- 1.
			return removeTrailingNinesSimple(address - 1);
		}
		if(XMemory.get_byte(address - 1) == ZERO && XMemory.get_byte(address) <= ONE)
		{
			// 1 because 16th digit can be off by +/- 1
			return removeTrailingZerosSimple(address - 1);
		}
		if(XMemory.get_byte(address) >= EIGHT)
		{
			// 8 because 16th digit can be off by +/- 1
			XMemory.set_byte(address - 1, (byte)(XMemory.get_byte(address - 1) + 1));
			return address;
		}
		if(XMemory.get_byte(address) <= ONE)
		{
			// 1 because 16th digit can be off by +/- 1
			return address;
		}
		return address + 1;
	}

	private static long removeTrailingNinesSimple(final long address)
	{
		long a = address;
		while(XMemory.get_byte(a) == NINE)
		{
			a--;
		}
		XMemory.set_byte(a, (byte)(XMemory.get_byte(a) + 1));
		return a + 1;
	}

	

	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	/**
	 * Dummy constructor to prevent instantiation of this static-only utility class.
	 * 
	 * @throws UnsupportedOperationException when called
	 */
	private MemoryCharConversion_doubleUTF8()
	{
		// static only
		throw new UnsupportedOperationException();
	}

	// CHECKSTYLE.ON: MagicNumber
	
}

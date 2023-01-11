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


public final class CharConversion_double
{
	// CHECKSTYLE.OFF: MagicNumber: Arithmetics are better readable with direct values
	
	///////////////////////////////////////////////////////////////////////////
	// constants //
	//////////////

	private static final double
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

	private static final int
		DOUBLE_DIGITS_MAX   = 17,
		DOUBLE_DIGITS_BOUND = 16
	;



	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////

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
	 * @param target the array to receive the character sequence at the given offset.
	 * @param offset the offset in the target array where the character sequence shall start.
	 * @return the offset pointing to the index after the last character of the assembled sequence.
	 */
	public static final int put(final double value, final char[] target, final int offset)
	{
		// pure algorithm method intentionally without array bounds check. Use VarString etc. for that.

		// head method with a bunch of special case handling
		if(Double.isNaN(value))
		{
			return XChars.put(XChars.CHARS_NAN, target, offset);
		}
		if(value < DOUBLE_ZERO)
		{
			if(value == Double.NEGATIVE_INFINITY)
			{
				return XChars.put(XChars.CHARS_NEGATIVE_INFINITY, target, offset);
			}
			target[offset] = '-';
			return put_doublePositive(-value, target, offset + 1);
		}
		if(value == DOUBLE_ZERO)
		{
			// this case is so common that is pays off to handle it specifically
			return XChars.put(XChars.CHARS_ZERO, target, offset);
		}
		if(value == DOUBLE_ONE)
		{
			// this case is so common that is pays off to handle it specifically
			return XChars.put(XChars.CHARS_ONE, target, offset);
		}
		if(value == Double.POSITIVE_INFINITY)
		{
			return XChars.put(XChars.CHARS_POSITIVE_INFINITY, target, offset);
		}
		return put_doublePositive(value, target, offset);
	}

	private static int put_doublePositive(final double value, final char[] target, final int offset)
	{
		return value < DOUBLE_ONE
			? put_doubleLt1(value, target, offset)
			: put_doubleGte1(value, target, offset)
		;
	}

	private static int put_doubleLt1(final double value, final char[] target, final int offset)
	{
		return value < DOUBLE_NORMALIZATION_THRESHOLD_LOW
			? put_doubleLt1Normalized(value, target, offset)
			: put_doubleLt1Denormalized(value, target, offset)
		;
	}

	private static int put_doubleGte1(final double value, final char[] target, final int offset)
	{
		return value < DOUBLE_NORMALIZATION_THRESHOLD_HIGH
			? put_doubleGte1Denormalized(value, target, offset)
			: put_doubleGte1Normalized(value, target, offset)
		;
	}

	private static int put_doubleGte1Denormalized(final double value, final char[] target, final int offset)
	{
		final int exponent = exponent(value);
		target[offset] = '0'; // overflow handling digit, gets replaced by decimal point

		// "extremely annoying special corner case" cannot happen in this range
		final int i = put_doubleAndCleanup(value * pow10(DOUBLE_DIGITS_BOUND - exponent), target, offset + 1);
		if(target[offset] != '0')
		{
			return handle_doubleGte1DenormSpecialCase(target, offset, exponent);
		}
		System.arraycopy(target, offset + 1, target, offset, exponent + 1);
		target[offset + exponent + 1] = '.';
		return Math.max(i, offset + exponent + 3);
	}

	private static int handle_doubleGte1DenormSpecialCase(final char[] target, final int offset, final int exponent)
	{
		if(exponent == 7)
		{
			System.arraycopy(XChars.CHARS_NORM_THRESH_HIGH, 0, target, offset, XChars.CHARS_NORM_THRESH_HIGH.length);
			return offset + XChars.CHARS_NORM_THRESH_HIGH.length;
		}
		
		final int e = Math.max(exponent, 1) + 1;
		for(int i = 1; i <= e; i++)
		{
			target[offset + i] = '0';
		}
		target[offset + e] = '.';
		target[offset + e + 1] = '0';
		return offset + e + 2;
	}

	private static int put_doubleGte1Normalized(final double value, final char[] target, final int offset)
	{
		final int exponent = exponent(value);
		final double intedValue = value * (exponent < DOUBLE_DIGITS_MAX
			? pow10(DOUBLE_DIGITS_BOUND - exponent)
			: root10(DOUBLE_DIGITS_BOUND - exponent))
		;

		// check for extremely annoying special corner case that requires strict algorithm
		if(intedValue == Double.POSITIVE_INFINITY || intedValue == Double.NEGATIVE_INFINITY)
		{
			return XChars.put(Double.toString(value), target, offset);
		}

		target[offset] = '0'; // overflow handling digit, gets replaced by decimal point
		int i = put_doubleAndCleanup(intedValue, target, offset + 1);
		if(target[offset] != '0')
		{
			target[offset + 1] = '.';
			target[offset + 2] = '0';
			i = offset + 3;
		}
		else
		{
			target[offset    ] = target[offset + 1];
			target[offset + 1] = '.';
			if(i == offset + 2)
			{
				i++;
			}
		}

		target[i] = 'E'; // put exponent symbol
		return CharConversionIntegers.put_int3(exponent, target, i + 1);
	}

	private static int put_doubleLt1Denormalized(final double value, final char[] target, final int offset)
	{
		// "extremely annoying special corner case" cannot happen in this range

		// this little decimal point detour auto-handles the case of overflowing to 1.0
		target[offset + 1] = '0';
		int i = put_doubleLt1DenormAndCleanup(value, target, offset);
		target[offset   ]  = target[offset + 1];
		target[offset + 1] = '.';
		if(i == offset + 2)
		{
			target[i++] = '0';
		}
		return i;
	}

	private static int put_doubleLt1DenormAndCleanup(final double value, final char[] target, final int offset)
	{
		switch(exponent(value))
		{
			case -3:
			{
				target[offset + 2] = '0';
				target[offset + 3] = '0';
				return put_doubleAndCleanup(value / DOUBLE_LAST_DIGIT0, target, offset + 4);
			}
			case -2:
			{
				target[offset + 2] = '0';
				return put_doubleAndCleanup(value / DOUBLE_LAST_DIGIT1, target, offset + 3);
			}
			default:
			{
				// can only be case exponent == -1
				return put_doubleAndCleanup(value / DOUBLE_LAST_DIGIT2, target, offset + 2);
			}
		}
	}

	private static int put_doubleLt1Normalized(final double value, final char[] target, final int offset)
	{
		final int exponent = exponent(value);
		final double intedValue = value * pow10(DOUBLE_DIGITS_BOUND - exponent);

		// check for extremely annoying special corner case that requires strict algorithm
		if(intedValue == Double.POSITIVE_INFINITY || intedValue == Double.NEGATIVE_INFINITY)
		{
			return XChars.put(Double.toString(value), target, offset);
		}

		int i = put_doubleAndCleanup(intedValue, target, offset + 1);
		target[offset    ] = target[offset + 1]; // shift first digit to the left
		target[offset + 1] = '.';                // insert decimal point
		if(i == offset + 2)
		{
			target[i++] = '0'; // fix accidentally cleaned first 0.
		}
		target[i    ] = 'E'; // put exponent symbol
		target[i + 1] = '-'; // lower 1 exponents always require a sign
		return CharConversionIntegers.put_int3(-exponent, target, i + 2);
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

	private static int put_doubleAndCleanup(final double value, final char[] target, final int offset)
	{
		return cleanupDecimal(target, offset, CharConversionIntegers.put_longPositive((long)value, target, offset));
	}

	private static int cleanupDecimal(final char[] buffer, final int offset, final int i)
	{
		switch(i - offset)
		{
			case DOUBLE_DIGITS_MAX:
			{
				return removeTrailingLast(buffer, i - 1);
			}
			case DOUBLE_DIGITS_BOUND:
			{
				return removeTrailingPreLast(buffer, i - 1);
			}
			default:
			{
				return buffer[i - 1] == '9'
					? removeTrailingNinesSimple(buffer, i - 1)
					: removeTrailingZerosSimple(buffer, i - 1)
				;
			}
		}
	}

	// CHECKSTYLE.OFF: FinalParameters: tiny util method for scrolling the offset
	private static int removeTrailingZerosSimple(final char[] target, int offset)
	{
		while(target[offset] == '0')
		{
			offset--;
		}
		return offset + 1;
	}
	// CHECKSTYLE.ON: FinalParameters

	private static int removeTrailingLast(final char[] target, final int offset)
	{
		// first condition is a workaround for some nasty special cases
		if(target[offset - 3] == '9' && target[offset - 2] == '9' && target[offset - 1] >= '7')
		{
			return removeTrailingNinesSimple(target, offset - 3);
		}
		if(target[offset - 3] == '0' && target[offset - 2] == '0' && target[offset - 1] <= '2')
		{
			return removeTrailingZerosSimple(target, offset - 3);
		}
		if(target[offset - 2] == '9' && target[offset - 1] >= '8')
		{
			// '8' because 16th digit can be off by +/- 1.
			return removeTrailingNinesSimple(target, offset - 2);
		}
		if(target[offset - 2] == '0' && target[offset - 1] <= '1')
		{
			// 1 because 16th digit can be off by +/- 1
			return removeTrailingZerosSimple(target, offset - 2);
		}
		if(target[offset - 1] == '9' && target[offset] >= '5')
		{
			// simple rounding for 17th digit
			target[offset - 2]++;
			return offset - 1;
		}
		if(target[offset - 1] == '0' && target[offset] < '5')
		{
			// simple rounding for 17th digit
			return offset - 1;
		}
		if(target[offset] >= '5')
		{
			// simple rounding for 17th digit
			target[offset - 1]++;
			return offset;
		}
		return offset;
	}

	private static int removeTrailingPreLast(final char[] target, final int offset)
	{
		// first condition is a workaround for some nasty special cases
		if(target[offset - 2] == '9' && target[offset - 1] == '9' && target[offset - 1] >= '7')
		{
			return removeTrailingNinesSimple(target, offset - 2);
		}
		if(target[offset - 2] == '0' && target[offset - 1] == '0' && target[offset] <= '2')
		{
			return removeTrailingZerosSimple(target, offset - 2);
		}
		if(target[offset - 1] == '9' && target[offset] >= '8')
		{
			// '8' because 16th digit can be off by +/- 1.
			return removeTrailingNinesSimple(target, offset - 1);
		}
		if(target[offset - 1] == '0' && target[offset] <= '1')
		{
			// 1 because 16th digit can be off by +/- 1
			return removeTrailingZerosSimple(target, offset - 1);
		}
		if(target[offset] >= '8')
		{
			// 8 because 16th digit can be off by +/- 1
			target[offset - 1]++;
			return offset;
		}
		if(target[offset] <= '1')
		{
			// 1 because 16th digit can be off by +/- 1
			return offset;
		}
		return offset + 1;
	}

	// CHECKSTYLE.OFF: FinalParameters: tiny util method for scrolling the offset
	private static int removeTrailingNinesSimple(final char[] target, int offset)
	{
		while(target[offset] == '9')
		{
			offset--;
		}
		target[offset]++;
		return offset + 1;
	}
	// CHECKSTYLE.ON: FinalParameters

	

	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	/**
	 * Dummy constructor to prevent instantiation of this static-only utility class.
	 * 
	 * @throws UnsupportedOperationException when called
	 */
	private CharConversion_double()
	{
		// static only
		throw new UnsupportedOperationException();
	}
	
	// CHECKSTYLE.ON: MagicNumber
}

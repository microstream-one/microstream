package one.microstream.math;

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

import static java.lang.Math.abs;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Iterator;
import java.util.Random;

import one.microstream.exceptions.NumberRangeException;
import one.microstream.functional.To_double;

public final class XMath
{
	// CHECKSTYLE.OFF: MagicNumber: all magic numbers are intentional in this class

	///////////////////////////////////////////////////////////////////////////
	// constants //
	//////////////

	private static final transient Random RANDOM = new Random();

	// or "1 << 30" or 2^30. Highest int value that can be achieved by leftshifting 1.
	private static final transient int MAX_POW_2_INT = 1_073_741_824;

	private static final transient int PERCENT = 100;

	private static final transient Double ZERO = 0.0d;
	private static final transient Double ONE  = 1.0d;
		
	
	
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////
	
	public static Double zero()
	{
		return ZERO;
	}
	
	public static Double one()
	{
		return ONE;
	}

	/**
	 * This method is an int version of {@code Math.pow(double, double)},
	 * using only integer iteration for calculation.
	 * <p>
	 * As a rule of thumb:<br>
	 * It is faster for {@code exponent} lower than 250 (significantly faster for exponents lt 100)
	 * and slower for {@code exponent} greater than or equal 250 (significantly slower for exponents gt/e 500).<br>
	 * This may depend on the concrete system running the program, of course.
	 * <br>
	 * Note that {@code exponent} may not be negative, otherwise an {@code IllegalArgumentException} is
	 * thrown.
	 *
	 * @param base the base
	 * @param exponent my not be negative
	 * @return {@code base^exponent}
	 * @throws IllegalArgumentException if {@code exponent} is negative
	 */
	public static final int pow(final int base, final int exponent) throws IllegalArgumentException
	{
		if(exponent < 0)
		{
			throw new IllegalArgumentException("exponent may not be negative: " + exponent);
		}
		if(exponent == 0)
		{
			return 1; //return 1, even if base is 0!
		}
		if(base == 0 || base == 1)
		{
			return base;
		}

		int result = 1;
		int e = exponent;
		while(e-- > 0)
		{
			result *= base;
		}

		return result;
	}

	public static final int pow10(final int exponent) throws IllegalArgumentException
	{
		if(exponent < 0)
		{
			throw new IllegalArgumentException("exponent may not be negative: " + exponent);
		}
		if(exponent == 0)
		{
			return 1; //return 1, even if base is 0!
		}

		int result = 1;
		int e = exponent;
		while(e-- > 0)
		{
			result *= 10;
		}

		return result;
	}


	public static final double pow10_double(final int exponent) throws IllegalArgumentException
	{
		if(exponent < 0)
		{
			throw new IllegalArgumentException("exponent may not be negative: " + exponent);
		}
		if(exponent == 0)
		{
			return 1; //return 1, even if base is 0!
		}

		double result = 1;
		int e = exponent;
		while(e-- > 0)
		{
			result *= 10;
		}

		return result;
	}

	public static final long pow10_long(final int exponent) throws IllegalArgumentException
	{
		if(exponent < 0)
		{
			throw new IllegalArgumentException("exponent may not be negative: " + exponent);
		}
		if(exponent == 0)
		{
			return 1; //return 1, even if base is 0!
		}

		long result = 1;
		int e = exponent;
		while(e-- > 0)
		{
			result *= 10;
		}

		return result;
	}


	public static final int pow2BoundMaxed(final int n)
	{
		if(n > MAX_POW_2_INT)
		{
			return Integer.MAX_VALUE;
		}
		int i = 1;
		while(i < n)
		{
			i <<= 1;
		}
		return i;
	}

	public static final int pow2BoundCapped(final int n)
	{
		if(n >= MAX_POW_2_INT)
		{
			return MAX_POW_2_INT;
		}
		int i = 1;
		while(i < n)
		{
			i <<= 1;
		}
		return i;
	}

	public static final int pow2Bound(final int n)
	{
		// (23.03.2019 TM)TODO: compare to ConcurrentHashMap#tableSizeFor
		
		if(n > MAX_POW_2_INT)
		{
			throw new IllegalArgumentException();
		}
		int i = 1;
		while(i < n)
		{
			i <<= 1;
		}
		return i;
	}

	public static final int log2pow2(final int pow2Value)
	{
		switch(pow2Value)
		{
			case          1: return  0;
			case          2: return  1;
			case          4: return  2;
			case          8: return  3;
			case         16: return  4;
			case         32: return  5;
			case         64: return  6;
			case        128: return  7;
			case        256: return  8;
			case        512: return  9;
			case       1024: return 10;
			case       2048: return 11;
			case       4096: return 12;
			case       8192: return 13;
			case      16384: return 14;
			case      32768: return 15;
			case      65536: return 16;
			case     131072: return 17;
			case     262144: return 18;
			case     524288: return 19;
			case    1048576: return 20;
			case    2097152: return 21;
			case    4194304: return 22;
			case    8388608: return 23;
			case   16777216: return 24;
			case   33554432: return 25;
			case   67108864: return 26;
			case  134217728: return 27;
			case  268435456: return 28;
			case  536870912: return 29;
			case 1073741824: return 30;
			default:
				throw new IllegalArgumentException("Not a power-of-2 value: " + pow2Value);
		}
	}

	/**
	 * Determines if the passed value is a power-of-2 value.
	 * 
	 * @param value the value to be tested.
	 * 
	 * @return {@code true} for any n in [0;30] that satisfies {@code value = 2^n}.
	 */
	public static final boolean isPow2(final int value)
	{
		// lookup-switch should be faster than binary search with 4-5 ifs (I hope).
		switch(value)
		{
			case          1: return true;
			case          2: return true;
			case          4: return true;
			case          8: return true;
			case         16: return true;
			case         32: return true;
			case         64: return true;
			case        128: return true;
			case        256: return true;
			case        512: return true;
			case       1024: return true;
			case       2048: return true;
			case       4096: return true;
			case       8192: return true;
			case      16384: return true;
			case      32768: return true;
			case      65536: return true;
			case     131072: return true;
			case     262144: return true;
			case     524288: return true;
			case    1048576: return true;
			case    2097152: return true;
			case    4194304: return true;
			case    8388608: return true;
			case   16777216: return true;
			case   33554432: return true;
			case   67108864: return true;
			case  134217728: return true;
			case  268435456: return true;
			case  536870912: return true;
			case 1073741824: return true;
			default        : return false;
		}
	}

	/**
	 * Returns the integer value of base-10 logarithm of the passed value.
	 * <p>
	 * Examples:
	 * <pre>
	 * log10(1) = 0
	 * log10(8) = 0
	 * log10(10) = 1
	 * log10(99) = 1
	 * log10(100) = 2
	 * log10(1000000000) = 9
	 * log10(2147483647) = 9
	 * </pre>
	 * Note that passing a value lower than or equal 0 will throw an  {@link IllegalArgumentException}.
	 *
	 * @param value the value to calculate the logarithm on
	 * @return the integer value of base-10 logarithm of the passed value
	 */
	public static final int log10discrete(final int value) throws IllegalArgumentException
	{
		// hardcoded binary search with 3 to 4 comparisons (linear search costs 10 comparisons for small values)
		if(value >= 10000)
		{
			if(value >= 10000000)
			{
				// 3-4 comparisons
				return value >= 1000000000 ? 9 : value >= 100000000 ? 8 : 7;
			}
			// 3-4 comparisons
			return value >= 1000000 ? 6 : value >= 100000 ? 5 : 4;
		}
		if(value >= 100)
		{
			// 3 comparisons
			// values 100 to 9999 are assumed to be passed more often to this method than values 1 to 99
			return value >= 1000 ? 3 : 2;
		}
		// 3-4 comparisons
		if(value >= 10)
		{
			return 1;
		}
		else if(value >= 1)
		{
			return 0;
		}
		else
		{
			throw new IllegalArgumentException("Cannot calculate log10() of value " + value);
		}
	}

	/**
	 * Returns the amount of digits the passed values requires to be projected as a string.
	 * <p>
	 * The additional length for the minus of to represent negative values is accounted for as well.<br>
	 * Examples:
	 * <pre>
	 * stringLength(0) = 1
	 * stringLength(+6) = 1
	 * stringLength(10) = 2
	 * stringLength(+2147483647) = 10
	 * stringLength(-1) = 2
	 * stringLength(-2147483648) = 11
	 * </pre>
	 *
	 * @param value the {@code int} value whose string length shall be calculated
	 * @return the length the passed value's string representation will require.
	 */
	public static final int stringLength(final int value)
	{
		if(value > 0)
		{
			return log10discrete(+value) + 1;
		}
		else if(value < 0)
		{
			return log10discrete(-value) + 2; // additional digit for the "-"
		}
		return 1;
	}

	public static final float pow(final float base, final int exponent) throws IllegalArgumentException
	{
		if(exponent < 0)
		{
			throw new IllegalArgumentException("exponent may not be negative: " + exponent);
		}
		if(exponent == 0)
		{
			return 1; //return 1, even if base is 0!
		}
		if(base == 0 || base == 1)
		{
			return base;
		}

		float result = 1;
		int e = exponent;
		while(e-- > 0)
		{
			result *= base;
		}
		return result;
	}

	public static final double pow(final double base, final int exponent) throws IllegalArgumentException
	{
		if(exponent < 0)
		{
			throw new IllegalArgumentException("exponent may not be negative: " + exponent);
		}
		if(exponent == 0)
		{
			return 1; //return 1, even if base is 0!
		}
		if(base == 0 || base == 1)
		{
			return base;
		}

		double result = 1;
		int e = exponent;
		while(e-- > 0)
		{
			result *= base;
		}
		return result;
	}

	public static final float square(final float f)
	{
		return f * f;
	}

	public static final long square(final long l)
	{
		return l * l;
	}

	public static final int square(final int i)
	{
		return i * i;
	}

	public static final double square(final double d)
	{
		return d * d;
	}

	public static final float cube(final float f)
	{
		return f * f * f;
	}

	public static final long cube(final long l)
	{
		return l * l * l;
	}

	public static final int cube(final int i)
	{
		return i * i * i;
	}

	public static final double cube(final double d)
	{
		return d * d * d;
	}

	public static final double round0(final double value)
	{
		return StrictMath.floor(value + 0.5d);
	}

	/**
	 * Common rounding variant for 1 decimal.
	 * @param value the decimal value to be rounded.
	 * @return the passed value rounded to 1 decimal.
	 */
	public static final double round1(final double value)
	{
		return StrictMath.floor(value * 10.0d + 0.5d) / 10.0d;
	}

	/**
	 * Common rounding variant for 2 decimals.
	 * @param value the decimal value to be rounded.
	 * @return the passed value rounded to 2 decimals.
	 */
	public static final double round2(final double value)
	{
		return StrictMath.floor(value * 100.0d + 0.5d) / 100.0d;
	}

	/**
	 * Common rounding variant for 3 decimals.
	 * @param value the decimal value to be rounded.
	 * @return the passed value rounded to 3 decimals.
	 */
	public static final double round3(final double value)
	{
		return StrictMath.floor(value * 1000.0d + 0.5d) / 1000.0d;
	}

	/**
	 * Common rounding variant for 4 decimals.
	 * @param value the decimal value to be rounded.
	 * @return the passed value rounded to 4 decimals.
	 */
	public static final double round4(final double value)
	{
		return StrictMath.floor(value * 10_000.0d + 0.5d) / 10_000.0d;
	}

	/**
	 * Common rounding variant for 5 decimals.
	 * @param value the decimal value to be rounded.
	 * @return the passed value rounded to 5 decimals.
	 */
	public static final double round5(final double value)
	{
		return StrictMath.floor(value * 100_000.0d + 0.5d) / 100_000.0d;
	}

	/**
	 * Common rounding variant for 6 decimals.
	 * @param value the decimal value to be rounded.
	 * @return the passed value rounded to 6 decimals.
	 */
	public static final double round6(final double value)
	{
		return StrictMath.floor(value * 1_000_000.0d + 0.5d) / 1_000_000.0d;
	}

	/**
	 * Common rounding variant for 7 decimals.
	 * @param value the decimal value to be rounded.
	 * @return the passed value rounded to 7 decimals.
	 */
	public static final double round7(final double value)
	{
		return StrictMath.floor(value * 10_000_000.0d + 0.5d) / 10_000_000.0d;
	}

	/**
	 * Common rounding variant for 8 decimals.
	 * @param value the decimal value to be rounded.
	 * @return the passed value rounded to 8 decimals.
	 */
	public static final double round8(final double value)
	{
		return StrictMath.floor(value * 100_000_000.0d + 0.5d) / 100_000_000.0d;
	}

	/**
	 * Common rounding variant for 9 decimals.
	 * @param value the decimal value to be rounded.
	 * @return the passed value rounded to 9 decimals.
	 */
	public static final double round9(final double value)
	{
		return StrictMath.floor(value * 1_000_000_000.0d + 0.5d) / 1_000_000_000.0d;
	}

	/**
	 * Rounds {@code value} to the actual closest value for {@code decimals} decimals.<br>
	 * This is useful as well in order to "normalize" values if multiple subsequent calculations with double values
	 * accumulate rounding errors that drift the value away from the value it actually should (could) be.<br>
	 * See the "candy" example in Joshua Bloch's "Effective Java": this method fixes the problem.
	 * <p>
	 * Note that {@code decimals} may not be negative.<br>
	 * Also note that while a value of 0 for {@code decimals} will yield the correct result, it makes not much
	 * sense to call this method for it in the first place.
	 *
	 * @param value any double value
	 * @param decimals the number of decimals. May not be negative.
	 * @return the normalized value for {@code value}
	 */
	// (23.08.2010 TM)FIXME: Negative values?
	public static final double round(final double value, final int decimals)
	{
		// (02.08.2011 TM)TODO: test performance difference with server mode and high warm up
		switch(decimals)
		{
			// common cases are hardcoded for performance reasons, inlined rounding code (Math.round(value*d)/d)
			case  0: return StrictMath.floor(value              + 0.5d)             ;
			case  1: return StrictMath.floor(value * 10.0d      + 0.5d) / 10.0d     ;
			case  2: return StrictMath.floor(value * 100.0d     + 0.5d) / 100.0d    ;
			case  3: return StrictMath.floor(value * 1000.0d    + 0.5d) / 1000.0d   ;
			case  4: return StrictMath.floor(value * 10000.0d   + 0.5d) / 10000.0d  ;
			case  5: return StrictMath.floor(value * 100000.0d  + 0.5d) / 100000.0d ;
			case  6: return StrictMath.floor(value * 1000000.0d + 0.5d) / 1000000.0d;
			default: return internalRound(value, decimals);
		}
	}

	private static final double internalRound(final double value, final int decimals)
	{
		// generic algorithm, including range checks
		if(decimals < 0)
		{
			throw new IllegalArgumentException("No negative values allowed for decimals: " + decimals);
		}
		// no idea if 323 is the best choice, tbh. At least it's a check for decimals values like million etc.
		if(decimals > 323)
		{
			throw new IllegalArgumentException("decimals value out of range: " + decimals);
		}

		// inlined pow(double, int) without checks
		double factor = 1.0d;
		int d = decimals;
		while(d-- > 0)
		{
			factor *= 10.0d;
		}
		return StrictMath.floor(value * factor + 0.5d) / factor;
	}

	public static _longRange range(final int start, final int bound)
	{
		return _longRange.New(start, bound);
	}

	public static byte[] sequence(final byte from, final byte to)
	{
		byte[] range;
		if(from < to)
		{
			range = new byte[to - from + 1];
			byte f = from;
			for(int i = 0; i < range.length; i++)
			{
				range[i] = f++;
			}
		}
		else
		{
			range = new byte[from - to + 1];
			byte f = from;
			for(int i = 0; i < range.length; i++)
			{
				range[i] = f--;
			}
		}
		return range;
	}

	public static short[] sequence(final short from, final short to)
	{
		short[] range;
		if(from < to)
		{
			range = new short[to - from + 1];
			short f = from;
			for(int i = 0; i < range.length; i++)
			{
				range[i] = f++;
			}
		}
		else
		{
			range = new short[from - to + 1];
			short f = from;
			for(int i = 0; i < range.length; i++)
			{
				range[i] = f--;
			}
		}
		return range;
	}

	public static int[] sequence(final int lastValue)
	{
		final int[] sequence;
		final int size;
		if(lastValue >= 0)
		{
			size = lastValue + 1;
			sequence = new int[size];
			for(int i = 0; i < size; i++)
			{
				sequence[i] = i;
			}
		}
		else
		{
			size = -lastValue + 1;
			sequence = new int[size];
			for(int i = 0, v = 0; i < size; i++)
			{
				sequence[i] = v--;
			}
		}

		return sequence;
	}

	public static int[] randoming(final int length)
	{
		final int[] ints = new int[length];
		final int bound = length;

		int i = length;
		while(i-- > 0)
		{
			ints[i] = RANDOM.nextInt(bound);
		}

		return ints;
	}

	public static int[] randoming(final int length, final int bound)
	{
		final int[] ints = new int[length];

		int i = length;
		while(i-- > 0)
		{
			ints[i] = RANDOM.nextInt(bound);
		}

		return ints;
	}

	public static int[] randoming(final int length, final int lowestValue, final int bound)
	{
		final int[] ints = new int[length];
		final int randomBound = bound - lowestValue;

		int i = length;
		while(i-- > 0)
		{
			ints[i] = RANDOM.nextInt(randomBound) + lowestValue;
		}

		return ints;
	}

	public static Integer[] sequence(final Integer lastValue)
	{
		final Integer[] sequence;
		final int size;
		if(lastValue >= 0)
		{
			size = lastValue + 1;
			sequence = new Integer[size];
			for(int i = 0; i < size; i++)
			{
				sequence[i] = i;
			}
		}
		else
		{
			size = -lastValue + 1;
			sequence = new Integer[size];
			for(int i = 0, v = 0; i < size; i++)
			{
				sequence[i] = v--;
			}
		}
		return sequence;
	}

	public static int[] sequence(final int from, final int to)
	{
		int[] range;
		if(from < to)
		{
			range = new int[to - from + 1];
			int f = from;
			for(int i = 0; i < range.length; i++)
			{
				range[i] = f++;
			}
		}
		else
		{
			range = new int[from - to + 1];
			int f = from;
			for(int i = 0; i < range.length; i++)
			{
				range[i] = f--;
			}
		}
		return range;
	}

	public static long[] sequence(final long from, final long to) throws IllegalArgumentException
	{
		long[] range;
		if(from < to)
		{
			final long elementCount = to - from + 1;
			if(elementCount > Integer.MAX_VALUE)
			{
				throw new IllegalArgumentException(
					"Range [" + from + ";" + to + "] exceeds array range limit: "
					+ elementCount + " > " + Integer.MAX_VALUE
				);
			}
			range = new long[(int)elementCount];
			long f = from;
			for(int i = 0; i < range.length; i++)
			{
				range[i] = f++;
			}
		}
		else
		{
			final long elementCount = from - to + 1;
			if(elementCount > Integer.MAX_VALUE)
			{
				throw new IllegalArgumentException(
					"Range [" + from + ";" + to + "] exceeds array range limit: "
					+ elementCount + " > " + Integer.MAX_VALUE
				);
			}
			range = new long[(int)elementCount];
			long f = from;
			for(int i = 0; i < range.length; i++)
			{
				range[i] = f--;
			}
		}
		return range;
	}

	public static final double max(final double... values)
	{
		if(values == null)
		{
			throw new IllegalArgumentException("values may not be null");
		}
		if(values.length == 0)
		{
			throw new IllegalArgumentException("values may not be empty");
		}

		double currentMax = -Double.MIN_VALUE;
		for(final double d : values)
		{
			if(d > currentMax)
			{
				currentMax = d;
			}
		}
		return currentMax;
	}

	public static final float max(final float... values)
	{
		if(values == null)
		{
			throw new IllegalArgumentException("values may not be null");
		}
		if(values.length == 0)
		{
			throw new IllegalArgumentException("values may not be empty");
		}

		float currentMax = -Float.MIN_VALUE;
		for(final float f : values)
		{
			if(f > currentMax)
			{
				currentMax = f;
			}
		}
		return currentMax;
	}

	public static final int max(final int... values)
	{
		if(values == null)
		{
			throw new IllegalArgumentException("values may not be null");
		}
		if(values.length == 0)
		{
			throw new IllegalArgumentException("values may not be empty");
		}

		int currentMax = Integer.MIN_VALUE;
		for(final int i : values)
		{
			if(i > currentMax)
			{
				currentMax = i;
			}
		}
		return currentMax;
	}

	public static final long max(final long... values)
	{
		if(values == null)
		{
			throw new IllegalArgumentException("values may not be null");
		}
		if(values.length == 0)
		{
			throw new IllegalArgumentException("values may not be empty");
		}

		long currentMax = Long.MIN_VALUE;
		for(final long l : values)
		{
			if(l > currentMax)
			{
				currentMax = l;
			}
		}
		return currentMax;
	}

	public static final double min(final double... values)
	{
		if(values.length == 0)
		{
			throw new IllegalArgumentException("values may not be empty");
		}

		double currentMin = -Double.MAX_VALUE;
		for(final double d : values)
		{
			if(d < currentMin)
			{
				currentMin = d;
			}
		}
		return currentMin;
	}

	public static final float min(final float... values)
	{
		if(values.length == 0)
		{
			throw new IllegalArgumentException("values may not be empty");
		}

		float currentMin = -Float.MAX_VALUE;
		for(final float f : values)
		{
			if(f < currentMin)
			{
				currentMin = f;
			}
		}
		return currentMin;
	}

	public static final int min(final int... values)
	{
		if(values.length == 0)
		{
			throw new IllegalArgumentException("values may not be empty");
		}

		int currentMin = Integer.MAX_VALUE;
		for(final int i : values)
		{
			if(i < currentMin)
			{
				currentMin = i;
			}
		}
		return currentMin;
	}

	public static final long min(final long... values)
	{
		if(values.length == 0)
		{
			throw new IllegalArgumentException("values may not be empty");
		}

		long currentMin = Long.MAX_VALUE;
		for(final long l : values)
		{
			if(l < currentMin)
			{
				currentMin = l;
			}
		}
		return currentMin;
	}

	public static final long sum(final byte... values)
	{
		long sum = 0;
		for(final byte i : values)
		{
			sum += i;
		}
		
		return sum;
	}

	public static final long sum(final short... values)
	{
		long sum = 0;
		for(final short i : values)
		{
			sum += i;
		}
		
		return sum;
	}

	public static final long sum(final int... values)
	{
		long sum = 0;
		for(final int i : values)
		{
			sum += i;
		}
		
		return sum;
	}

	public static final double sum(final float... values)
	{
		double sum = 0f;
		for(final float f : values)
		{
			sum += f;
		}
		
		return sum;
	}

	public static final long sum(final long... values)
	{
		long sum = 0;
		for(final long l : values)
		{
			sum += l;
		}
		return sum;
	}

	public static final double sum(final double... values)
	{
		double sum = 0d;
		for(final double d : values)
		{
			sum += d;
		}
		return sum;
	}

	public static final double columnSum(final int columnIndex, final double[]... matrix)
	{
		final int rowCount = matrix.length;
		double sum = 0.0;
		for(int r = rowCount; r-- > 0;)
		{
			sum += matrix[r][columnIndex];
		}
		return sum;
	}

	public static final double columnSum(final int columnIndex, final Double[]... matrix)
	{
		final int rowCount = matrix.length;
		double sum = 0.0;
		for(int r = rowCount; r-- > 0;)
		{
			if(matrix[r][columnIndex] == null)
			{
				continue;
			}
			sum += matrix[r][columnIndex];
		}
		return sum;
	}

	public static final double avg(final double... values)
	{
		double sum = 0.0;
		for(final double d : values)
		{
			sum += d;
		}
		return sum / values.length;
	}

	public static final float avg(final float... values)
	{
		float sum = 0.0f;
		for(final float f : values)
		{
			sum += f;
		}
		return sum / values.length;
	}

	public static final int avg(final int... values)
	{
		int sum = 0;
		for(final int i : values)
		{
			sum += i;
		}
		return sum / values.length;
	}

	public static final long avg(final long... values)
	{
		long sum = 0;
		for(final long l : values)
		{
			sum += l;
		}
		return sum / values.length;
	}

	/**
	 * Returns abs(d1/d2) for abs(d1) lower than abs(d2), else abs(d2/d1) in order to guarantee a codomain of [0.0;1.0]
	 * @param a the first value
	 * @param b the second value
	 * @return the lower ratio of d1 and d2
	 */
	public static final double lowerRatio(final double a, final double b)
	{
		if(a > 0.0D && b > 0.0D)
		{
			return a < b ? a / b : b / a;
		}
		return abs(abs(a) < abs(b) ? a / b : b / a);
	}

	/**
	 * Use {@code factorial(long)} for n in [0;20].<br>
	 * Use {@code factorial(BigInteger)} for any n greater than 0.
	 * @param n natural number in [0;12]
	 * @return n!
	 * @throws IllegalArgumentException for n lower than 0 or n greater than 12.
	 */
	public static final int factorial(final int n) throws IllegalArgumentException
	{
		// Calculate the loop everytime for a value set of only 13 elements? Not really.
		switch(n)
		{
			case 12: return 479001600;
			case 11: return 39916800;
			case 10: return 3628800;
			case  9: return 362880;
			case  8: return 40320;
			case  7: return 5040;
			case  6: return 720;
			case  5: return 120;
			case  4: return 24;
			case  3: return 6;
			case  2: return 2;
			case  1: return 1;
			case  0: return 1;
			default: throw new IllegalArgumentException("n not in [0;12]: " + n);
		}
	}

	/**
	 * Use {@code factorial(BigInteger)} for any n greater than 0.
	 *
	 * @param n natural number in [0;20]
	 * @return n!
	 * @throws IllegalArgumentException for n lower than 0 or n greater than 20
	 */
	public static final long factorial(final long n) throws IllegalArgumentException
	{
		//honestly: calculate the loop everytime for a value set of only 21 elements?
		switch((int)n)
		{
			case 20: return 2432902008176640000L;
			case 19: return 121645100408832000L;
			case 18: return 6402373705728000L;
			case 17: return 355687428096000L;
			case 16: return 20922789888000L;
			case 15: return 1307674368000L;
			case 14: return 87178291200L;
			case 13: return 6227020800L;
			case 12: return 479001600L;
			case 11: return 39916800L;
			case 10: return 3628800L;
			case  9: return 362880L;
			case  8: return 40320L;
			case  7: return 5040L;
			case  6: return 720L;
			case  5: return 120L;
			case  4: return 24L;
			case  3: return 6L;
			case  2: return 2L;
			case  1: return 1L;
			case  0: return 1L;
			default: throw new IllegalArgumentException("n not in [0;20]: " + n);
		}
	}

	/**
	 *
	 * @param n any natural number greater than or equal 0
	 * @return n!
	 * @throws IllegalArgumentException for n lower than 0
	 */
	public static final BigInteger factorial(final BigInteger n) throws IllegalArgumentException
	{
		//recursive algorithms are nonsense here as the doy method call and range checking overhead in every step!
		final long nValue = n.longValue();
		if(nValue < 0)
		{
			throw new IllegalArgumentException("n may not be negative: " + nValue);
		}
		if(nValue <= 20)
		{
			return BigInteger.valueOf(factorial(nValue));
		}

		BigInteger result = BigInteger.valueOf(factorial(20L));
		BigInteger value  = n;

		while(n.longValue() > 20)
		{
			result = result.multiply(value);
			value = value.subtract(BigInteger.ONE);
		}
		return result;
	}

	/**
	 * Alias for {@code BigInteger.valueOf(value)}
	 * @param value any value
	 * @return a {@code BigInteger} representing {@code value}
	 */
	public static final BigInteger bigInt(final int value)
	{
		return BigInteger.valueOf(value);
	}

	/**
	 * Alias for {@code BigInteger.valueOf(value)}
	 * @param value any value
	 * @return a {@code BigInteger} representing {@code value}
	 */
	public static final BigInteger bigInt(final long value)
	{
		return BigInteger.valueOf(value);
	}

	/**
	 * Alias for {@code BigDecimal.valueOf(value)}
	 * @param value any value
	 * @return a {@code BigDecimal} representing {@code value}
	 */
	public static final BigDecimal bigDec(final long value)
	{
		return BigDecimal.valueOf(value);
	}

	/**
	 * Alias for {@code BigDecimal.valueOf(value)}
	 * @param value any value
	 * @return a {@code BigDecimal} representing {@code value}
	 */
	public static final BigDecimal bigDec(final double value)
	{
		return BigDecimal.valueOf(value);
	}

	/**
	 * @return the random
	 */
	public static final Random random()
	{
		return RANDOM;
	}

	public static final int random(final int n)
	{
		return RANDOM.nextInt(n);
	}
	
	public static int even(final int value)
	{
		if((value & 1) != 0)
		{
			throw new IllegalArgumentException("Not an even number: " + value);
		}
		
		return value;
	}
	
	public static long even(final long value)
	{
		if((value & 1L) != 0L)
		{
			throw new IllegalArgumentException("Not an even number: " + value);
		}
		
		return value;
	}
	
	public static int odd(final int value)
	{
		if((value & 1) != 1)
		{
			throw new IllegalArgumentException("Not an odd number: " + value);
		}
		
		return value;
	}
	
	public static long odd(final long value)
	{
		if((value & 1L) != 1L)
		{
			throw new IllegalArgumentException("Not an odd number: " + value);
		}
		
		return value;
	}

	public static int positive(final int value) throws NumberRangeException
	{
		if(value > 0)
		{
			return value;
		}
		
		throw new NumberRangeException("Value is not positive: " + value);
	}

	public static int notNegative(final int value) throws NumberRangeException
	{
		if(value < 0)
		{
			throw new NumberRangeException("Value is negative: " + value);
		}
		return value;
	}

	public static Integer notNegative(final Integer value) throws NumberRangeException
	{
		if(value != null && value < 0)
		{
			throw new NumberRangeException("Value is negative: " + value);
		}
		return value;
	}

	public static int negative(final int value) throws NumberRangeException
	{
		if(value < 0)
		{
			return value;
		}
		
		throw new NumberRangeException("Value is not negative: " + value);
	}

	public static long positive(final long value) throws NumberRangeException
	{
		if(value > 0)
		{
			return value;
		}
		
		throw new NumberRangeException("Value is not positive: " + value);
	}

	public static long notNegative(final long value) throws NumberRangeException
	{
		if(value < 0)
		{
			throw new NumberRangeException("Value is negative: " + value);
		}
		return value;
	}

	public static Long notNegative(final Long value) throws NumberRangeException
	{
		if(value != null && value < 0)
		{
			throw new NumberRangeException("Value is negative: " + value);
		}
		return value;
	}

	public static long negative(final long value) throws NumberRangeException
	{
		if(value < 0)
		{
			return value;
		}

		throw new NumberRangeException("Value is not negative: " + value);
	}

	public static double positive(final double value) throws NumberRangeException
	{
		if(value > 0)
		{
			return value;
		}

		throw new NumberRangeException("Value is not positive: " + value);
	}

	public static double notNegative(final double value) throws NumberRangeException
	{
		if(value < 0)
		{
			throw new NumberRangeException("Value is negative: " + value);
		}
		return value;
	}

	public static double positiveMax1(final double value) throws NumberRangeException
	{
		if(value <= 1.0)
		{
			return positive(value);
		}
		
		throw new NumberRangeException("Value is bigger than 1: " + value);
	}

	public static double notNegativeMax1(final double value) throws NumberRangeException
	{
		if(value <= 1.0)
		{
			return notNegative(value);
		}
		
		throw new NumberRangeException("Value is bigger than 1: " + value);
	}

	public static float positive(final float value) throws NumberRangeException
	{
		if(value > 0.0f)
		{
			return value;
		}

		throw new NumberRangeException("Value is not positive: " + value);
	}
	
	public static long equal(final long value1, final long value2) throws IllegalArgumentException
	{
		if(value1 == value2)
		{
			return value1;
		}
		
		throw new IllegalArgumentException("Unequal values: " + value1 + " != " + value2);
	}

	public static double[] column(final int columnIndex, final double[]... matrix)
	{
		final int rowCount;
		final double[] column = new double[rowCount = matrix.length];
		for(int r = rowCount; r-- > 0;)
		{
			column[r] = matrix[r][columnIndex];
		}
		return column;
	}

	public static final int cap_int(final long value)
	{
		return value >= Integer.MAX_VALUE
			? Integer.MAX_VALUE
			: (int)value
		;
	}
	
	public static final boolean isGreaterThanOrEqualHighestPowerOf2(final long value)
	{
		return value >= MAX_POW_2_INT;
	}

	public static final boolean isGreaterThanHighestPowerOf2(final long value)
	{
		return value > MAX_POW_2_INT;
	}

	public static final boolean isGreaterThanOrEqualHighestPowerOf2(final int value)
	{
		return value >= MAX_POW_2_INT;
	}

	public static final boolean isGreaterThanHighestPowerOf2(final int value)
	{
		return value > MAX_POW_2_INT;
	}

	public static final int highestPowerOf2_int()
	{
		return MAX_POW_2_INT;
	}

	public static final double fractionToPercent(final double decimalFractionValue)
	{
		return decimalFractionValue * PERCENT;
	}

	public static final double percentToFraction(final double decimalPercentValue)
	{
		return decimalPercentValue / PERCENT;
	}

	public static final boolean isIn(final int value, final int... searchValues)
	{
		if(searchValues == null)
		{
			return false;
		}

		for(final int s : searchValues)
		{
			if(value == s)
			{
				return true;
			}
		}

		return false;
	}
	
	public static long addCapped(final long l1, final long l2)
	{
		// does not account for negative values
		return Long.MAX_VALUE - l1 < l2
			? Long.MAX_VALUE
			: l1 + l2
		;
	}
	


	
	public static <E> Double minDouble(final Iterable<E> elements, final To_double<? super E> getter)
	{
		final double minimum = min_double(elements, getter);
		
		return Double.isNaN(minimum)
			? null
			: Double.valueOf(minimum)
		;
	}
	
	public static <E> double min_double(final Iterable<E> elements, final To_double<? super E> getter)
	{
		return min_double(elements, getter, Double.NaN);
	}
	
	public static <E> double min_double(
		final Iterable<E>          elements    ,
		final To_double<? super E> getter      ,
		final double               defaultValue
	)
	{
		final Iterator<E> iterator = elements.iterator();
		if(!iterator.hasNext())
		{
			return defaultValue;
		}
		
		double minimum = Double.MAX_VALUE;
		while(iterator.hasNext())
		{
			final double value = getter.apply(iterator.next());
			if(value < minimum)
			{
				minimum = value;
			}
		}
		
		return minimum;
	}
	
	public static boolean isMathematicalInteger(final double value)
	{
		return !Double.isNaN(value)
			&& !Double.isInfinite(value)
			&& value == Math.rint(value)
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
	private XMath()
	{
		// static only
		throw new UnsupportedOperationException();
	}

	// CHECKSTYLE.ON: MagicNumber
}

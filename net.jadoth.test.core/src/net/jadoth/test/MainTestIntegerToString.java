package net.jadoth.test;

import net.jadoth.math.XMath;



public class MainTestIntegerToString
{
	private static final int    SIZE  = 1_000_000;
	private static final int    RUNS  = 100;
	private static final long[] TIMES = new long[RUNS];
	
	static final int[] ints;
	static {
		//		ints = JaMath.sequence(SIZE);
		ints = XMath.randoming(SIZE, 1_000_000_000, 2_000_000_000);
		//		ints = JaMath.randoming(SIZE, 100);
		//		ints = JaMath.randoming(SIZE, 100_000, 1_000_000);
	}
	
	public static void main(final String[] args)
	{
		final char[] chars = new char[12];
		
		for(int r = 0; r < RUNS; r++)
		{
			final long tStart = System.nanoTime();
			for(int s = 0; s < SIZE; s++)
			{
				//				System.out.println(s);
				toString(ints[s], chars, 0);
				//				assembleDigits(ints[s], chars, 0);
				//				toStringNormalCase(ints[s], chars, 0);
				//				getChars2(ints[s], 12, chars);
				//				getChars3(12345, chars, 0, 0);
				//				getChars(ints[s], 12, chars);
				//				toStringJdk(ints[s], chars, 12);
			}
			TIMES[r] = System.nanoTime() - tStart;
		}
		double sum = 0.0;
		for(int r = 1; r < RUNS; r++)
		{
			sum += TIMES[r];
		}
		System.out.println("Average = "+new java.text.DecimalFormat("00,000,000,000").format(sum / (RUNS - 1)));
	}
	
	
	
	
	public static final int toString(final int value, final char[] target, final int targetOffset)
	{
		if(value < 0)
		{
			return toStringNegative(value, target, targetOffset);
		}
		return toStringPositiveInit(value, target, targetOffset); // normal case
	}
	
	private static final char[] MIN_VALUE_STRING = {'-', '2', '1', '4', '7', '4', '8', '3', '6', '4', '8'};
	
	
	static int toStringNegative(final int value, final char[] target, final int targetOffset)
	{
		if(value == Integer.MIN_VALUE){ // unnegatable special case
			System.arraycopy(MIN_VALUE_STRING, 0, target, targetOffset, MIN_VALUE_STRING.length);
			return targetOffset + MIN_VALUE_STRING.length;
		}
		target[targetOffset] = '-';
		return toStringPositiveInit(-value, target, targetOffset + 1); // standard negative case normalization
	}
	
	static int toStringPositive(final int value, final char[] target, int offset)
	{
		if(value < 100)
		{
			if(value >= 10)
			{
				target[offset++] = DigitTens[value];
			}
			target[offset++] = DigitOnes[value];
			return offset;
		}
		final int upper, lower = value - (upper = value / 100)*100;
		target[offset = toStringPositive(upper, target, offset)] = DigitTens[lower];
		target[offset+1] = DigitOnes[lower];
		return offset+2;
	}
	
	private static int toStringPositiveInit(final int value, final char[] target, final int offset)
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
		if(value >= 100_000)
		{
			if(value >= 1_000_000_000)
			{
				return toStringPositive100000000(value, target, offset);
			}
			if(value >= 100_000_000)
			{
				target[offset] = (char)(48 + value / 100_000_000);
				return toStringPositive1000000(value % 100_000_000, target, offset + 1);
			}
			if(value >= 10_000_000)
			{
				return toStringPositive1000000(value, target, offset);
			}
			if(value >= 1_000_000)
			{
				target[offset] = (char)(48 + value / 1_000_000);
				return toStringPositive10000(value % 1_000_000, target, offset + 1);
			}
			return toStringPositive10000(value, target, offset);
		}
		if(value >= 10_000)
		{
			target[offset] = (char)(48 + value / 10_000);
			return toStringPositive100(value % 10_000, target, offset + 1);
		}
		if(value >= 1_000)
		{
			return toStringPositive100(value, target, offset);
		}
		if(value >= 100)
		{
			target[offset] = (char)(48 + value / 100);
			return putChars(value % 100, target, offset + 1);
		}
		if(value >= 10)
		{
			return putChars(value, target, offset);
		}
		target[offset] = (char)(48 + value);
		return offset + 1;
	}
	
	private static int toStringPositive100000000(final int value, final char[] target, final int offset)
	{
		return toStringPositive1000000(value % 100_000_000, target, putChars(value / 100_000_000, target, offset));
	}
	
	private static int toStringPositive1000000(final int value, final char[] target, final int offset)
	{
		return toStringPositive10000(value % 1_000_000, target, putChars(value / 1_000_000, target, offset));
	}
	
	private static int toStringPositive10000(final int value, final char[] target, final int offset)
	{
		return toStringPositive100(value % 10_000, target, putChars(value / 10_000, target, offset));
	}
	
	private static int toStringPositive100(final int value, final char[] target, final int offset)
	{
		return putChars(value % 100, target, putChars(value / 100, target, offset));
	}
	
	private static int putChars(final int value, final char[] target, final int offset)
	{
		target[offset  ] = DigitTens[value];
		target[offset+1] = DigitOnes[value];
		return offset+2;
	}
	
	static final char [] DigitTens = {
		'0', '0', '0', '0', '0', '0', '0', '0', '0', '0',
		'1', '1', '1', '1', '1', '1', '1', '1', '1', '1',
		'2', '2', '2', '2', '2', '2', '2', '2', '2', '2',
		'3', '3', '3', '3', '3', '3', '3', '3', '3', '3',
		'4', '4', '4', '4', '4', '4', '4', '4', '4', '4',
		'5', '5', '5', '5', '5', '5', '5', '5', '5', '5',
		'6', '6', '6', '6', '6', '6', '6', '6', '6', '6',
		'7', '7', '7', '7', '7', '7', '7', '7', '7', '7',
		'8', '8', '8', '8', '8', '8', '8', '8', '8', '8',
		'9', '9', '9', '9', '9', '9', '9', '9', '9', '9',
	} ;
	
	static final char [] DigitOnes = {
		'0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
		'0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
		'0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
		'0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
		'0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
		'0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
		'0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
		'0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
		'0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
		'0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
	} ;
	
	static final char[] digits = {'0' , '1' , '2' , '3' , '4' , '5' , '6' , '7' , '8' , '9'};
	
	
	
	private static void getChars2(int i, final int index, final char[] buf)
	{
		int q, r;
		int charPos = index;
		char sign = 0;
		
		if (i < 0) {
			sign = '-';
			i = -i;
		}
		
		// Generate two digits per iteration
		while (i >= 65536) {
			q = i / 100;
			// really: r = i - (q * 100);
			r = i - ((q << 6) + (q << 5) + (q << 2));
			i = q;
			buf [--charPos] = DigitOnes[r];
			buf [--charPos] = DigitTens[r];
		}
		
		// Fall thru to fast mode for smaller numbers
		// assert(i <= 65536, i);
		for (;;) {
			q = i * 52429 >>> 16+3;
			r = i - ((q << 3) + (q << 1));  // r = i-(q*10) ...
			buf [--charPos] = digits [r];
			i = q;
			if (i == 0) break;
		}
		if (sign != 0) {
			buf [--charPos] = sign;
		}
	}
	
	
	
	static final int [] sizeTable = { 9, 99, 999, 9999, 99999, 999999, 9999999,
		99999999, 999999999, Integer.MAX_VALUE };
	
	// Requires positive x
	private static int stringSize(final int x) {
		for (int i=0; ; i++)
			if (x <= sizeTable[i])
				return i+1;
	}
	
	public static final int toStringJdk(final int i, final char[] buf, final int index) {
		if (i == Integer.MIN_VALUE)
			System.arraycopy(MIN_VALUE_STRING, 0, buf, index, MIN_VALUE_STRING.length);
		final int size = i < 0 ? stringSize(-i) + 1 : stringSize(i);
		getChars2(i, index, buf);
		return size;
	}
	
}

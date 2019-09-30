package various;






public class TestIntegerToString
{
	public static void main(final String[] args)
	{
		
		test(+Integer.MAX_VALUE);
		test(147483647);
		test(+Integer.MAX_VALUE - 1);
		test(+1_000_000_001);
		test(+1_000_000_000);
		test(+  999_999_999);
		test(+  123_456_789);
		test(+    1_234_567);
		test(+      123_456);
		test(+       10_000);
		test(+        1_337);
		test(+        1_001);
		test(+        1_000);
		test(+          999);
		test(+           11);
		test(+           10);
		test(+            9);
		test(+            3);
		test(+            2);
		test(+            1);
		test(             0);
		test(-            1);
		test(-            2);
		test(-            3);
		test(-            9);
		test(-           10);
		test(-           11);
		test(-          999);
		test(-        1_000);
		test(-        1_001);
		test(-        1_337);
		test(-       10_000);
		test(-      123_456);
		test(-    1_234_567);
		test(-  123_456_789);
		test(-  999_999_999);
		test(-1_000_000_000);
		test(-1_000_000_001);
		test(-Integer.MAX_VALUE + 1);
		test(-Integer.MAX_VALUE);
		test( Integer.MIN_VALUE);
	}
	
	
	
	static void test(final int i)
	{
		final char[] dummy = new char[MIN_VALUE_STRING.length];
		final String s = new String(dummy, 0, toString(i, dummy, 0));
		final String output = i + "\t-->\t"+s;
		if(s.equals(Integer.toString(i)))
		{
			System.out.println(output);
		}
		else
		{
			System.err.println(output);
		}
		//		toStringJdkOptimized(i, dummy, 11);
		//		System.out.println(i+"\t->\t"+Arrays.toString(dummy));
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
	
	static void toStringJdkOptimized(int value, final char[] buf, final int index)
	{
		// note: case 0 would have to be handled as special case outside (WIP)
		int q, r, charPos = index;
		while(value >= 65536)
		{
			buf[--charPos] = DigitOnes[r = value - (q = value / 100)*100];
			buf[--charPos] = DigitTens[r];
			value = q;
		}
		while(value != 0)
		{
			buf[--charPos] = digits[r = value - (q = value * 52429 >>> 19)*10];
			value = q;
		}
	}
	
	
	
	
	
	private static final char[] MIN_VALUE_STRING = {'-', '2', '1', '4', '7', '4', '8', '3', '6', '4', '8'};
	private static final int[] INT_MAGNITUDES = {1000000000, 100000000, 10000000, 1000000, 100000, 10000, 1000, 100, 10, 1, 0};
	
	public static final int toString(final int value, final char[] target, final int targetOffset)
	{
		if(value < 0)
		{
			return toStringNegative(value, target, targetOffset);
		}
		return toStringPositiveInit(value, target, targetOffset); // normal case
	}
	
	static int toStringNegative(final int value, final char[] target, final int targetOffset)
	{
		if(value == Integer.MIN_VALUE){ // unnegatable special negative case
			System.arraycopy(MIN_VALUE_STRING, 0, target, targetOffset, MIN_VALUE_STRING.length);
			return targetOffset + MIN_VALUE_STRING.length;
		}
		target[targetOffset] = '-';
		return toStringPositiveInit(-value, target, targetOffset + 1); // standard negative case normalization
	}
	
	// nice algorithm, faster than JDK. Keep and maybe investigate / test for certain value ranges
	static int toStringPositiveRecursive(final int value, final char[] target, int offset)
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
		offset = toStringPositiveRecursive(upper, target, offset);
		target[offset  ] = DigitTens[lower];
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
	
	
	//	private static int toStringPositiveInit(final int value, final char[] target, final int offset)
	//	{
	//		// note: using redundant % instead of calculating quotient and rest is suprisingly marginally faster
	//		if(value >= 1_000_000_000)
	//		{
	//			return toStringPositive100000000(value, target, offset);
	//		}
	//		if(value >= 100_000_000)
	//		{
	//			target[offset] = (char)(48 + value / 100_000_000);
	//			return toStringPositive1000000(value % 100_000_000, target, offset + 1);
	//		}
	//		if(value >= 10_000_000)
	//		{
	//			return toStringPositive1000000(value, target, offset);
	//		}
	//		if(value >= 1_000_000)
	//		{
	//			target[offset] = (char)(48 + value / 1_000_000);
	//			return toStringPositive10000(value % 1_000_000, target, offset + 1);
	//		}
	//		if(value >= 100_000)
	//		{
	//			return toStringPositive10000(value, target, offset);
	//		}
	//		if(value >= 10_000)
	//		{
	//			target[offset] = (char)(48 + value / 10_000);
	//			return toStringPositive100(value % 10_000, target, offset + 1);
	//		}
	//		if(value >= 1_000)
	//		{
	//			return toStringPositive100(value, target, offset);
	//		}
	//		if(value >= 100)
	//		{
	//			target[offset] = (char)(48 + value / 100);
	//			return putChars(value % 100, target, offset + 1);
	//		}
	//		if(value >= 10)
	//		{
	//			return putChars(value, target, offset);
	//		}
	//		target[offset] = (char)(48 + value);
	//		return offset + 1;
	//	}
	//
	//	private static int toStringPositive100000000(final int value, final char[] target, final int offset)
	//	{
	//		return toStringPositive1000000(value % 100_000_000, target, putChars(value / 100_000_000, target, offset));
	//	}
	//
	//	private static int toStringPositive1000000(final int value, final char[] target, final int offset)
	//	{
	//		return toStringPositive10000(value % 1_000_000, target, putChars(value / 1_000_000, target, offset));
	//	}
	//
	//	private static int toStringPositive10000(final int value, final char[] target, final int offset)
	//	{
	//		return toStringPositive100(value % 10_000, target, putChars(value / 10_000, target, offset));
	//	}
	//
	//	private static int toStringPositive100(final int value, final char[] target, final int offset)
	//	{
	//		return putChars(value % 100, target, putChars(value / 100, target, offset));
	//	}
	//
	//	private static int putChars(final int value, final char[] target, final int offset)
	//	{
	//		target[offset  ] = DigitTens[value];
	//		target[offset+1] = DigitOnes[value];
	//		return offset+2;
	//	}
	
	
	static int dev(int value, final char[] target, int targetOffset)
	{
		if(value >= 1000000000)
		{
			target[targetOffset++] = value >= 2_000_000_000 ? '2' : '1';
			value %= 1000000000;
		}
		if(value >=  100000000)
		{
			target[targetOffset++] = (char)(48  + value / 100000000);
			value %= 100000000;
		}
		if(value >=   10000000)
		{
			target[targetOffset++] = (char)(48  + value / 10000000);
			value %= 10000000;
		}
		if(value >=    1000000)
		{
			target[targetOffset++] = (char)(48  + value / 1000000);
			value %= 1000000;
		}
		if(value >=     100000)
		{
			target[targetOffset++] = (char)(48  + value / 100000);
			value %= 100000;
		}
		if(value >=      10000)
		{
			target[targetOffset++] = (char)(48  + value / 10000);
			value %= 10000;
		}
		if(value >=       1000)
		{
			target[targetOffset++] = (char)(48  + value / 1000);
			value %= 1000;
		}
		if(value >=        100)
		{
			target[targetOffset++] = (char)(48  + value / 100);
			value %= 100;
		}
		if(value >=         10)
		{
			target[targetOffset++] = (char)(48  + value / 10);
			value %= 10;
		}
		if(value >=         1)
		{
			target[targetOffset++] = (char)(48  + value);
		}
		return targetOffset;
	}
	
	//	private static int dev(int value, final char[] target, int targetOffset)
	//	{
	//		if(value >= 1000000000)
	//		{
	//			target[targetOffset++] = value >= 2_000_000_000 ? '2' : '1';
	//			value %= 1000000000;
	//		}
	//		if(value >=  100000000)
	//		{
	//			target[targetOffset++] = getChar(value,  100000000);
	//			value %= 100000000;
	//		}
	//		if(value >=   10000000)
	//		{
	//			target[targetOffset++] = getChar(value,   10000000);
	//			value %= 10000000;
	//		}
	//		if(value >=    1000000)
	//		{
	//			target[targetOffset++] = getChar(value,    1000000);
	//			value %= 1000000;
	//		}
	//		if(value >=     100000)
	//		{
	//			target[targetOffset++] = getChar(value,     100000);
	//			value %= 100000;
	//		}
	//		if(value >=      10000)
	//		{
	//			target[targetOffset++] = getChar(value,     10000);
	//			value %= 10000;
	//		}
	//		if(value >=       1000)
	//		{
	//			target[targetOffset++] = getChar(value,      1000);
	//			value %= 1000;
	//		}
	//		if(value >=        100)
	//		{
	//			target[targetOffset++] = getChar(value,       100);
	//			value %= 100;
	//		}
	//		if(value >=         10)
	//		{
	//			target[targetOffset++] = getChar(value,        10);
	//			value %= 10;
	//		}
	//		if(value >=         1)
	//		{
	//			target[targetOffset++] = getChar(value,         1);
	//		}
	//		return targetOffset;
	//	}
	
	static char getChar(final int value, final int factor)
	{
		if(value >= 4*factor)
		{
			if(value >= 8*factor)
			{
				return value >= 9*factor ? '9' : '8';
			}
			else if(value >= 6*factor){
				return value >= 7*factor ? '7' : '6';
			}
			return value >= 5*factor ? '5' : '4';
		}
		else if(value >= 2*factor){
			return value >= 3*factor ? '3' : '2';
		}
		return value >= factor ? '1' : '0';
	}
	
	
	
	
	// (22.02.2013)TODO byte toString
	static int toStringPositive(final byte value, final char[] target, int offset)
	{
		if(value >= 100)
		{
			target[offset++] = '1';
		}
		if(value >= 10)
		{
			target[offset  ] = DigitTens[value];
			target[offset+1] = DigitOnes[value];
			return offset+2;
		}
		target[offset] = (char)(48 + value);
		return offset+1;
	}
	
	@Deprecated
	static int toStringNormalCase(int value, final char[] target, int targetOffset)
	{
		int m = 0;
		while(value < INT_MAGNITUDES[m]){              // phase one: skipping exceeding magnitudes
			m++;
		}
		// no idea why phase 2 is so incredibly slow compared to JDK tinkering or recursive algorithm
		while(value > 0){                              // phase two: value decomposition
			final int magnitude = INT_MAGNITUDES[m++];
			char digit = '0';
			while(value >= magnitude)
			{
				digit++;
				value -= magnitude;
			}
			target[targetOffset++] = digit;
		}
		while(++m < 11){            // phase 3: supplementing decade zeros
			target[targetOffset++] = '0';
		}
		return targetOffset;
	}
	
}

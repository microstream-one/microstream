package net.jadoth.memory;

import java.nio.ByteBuffer;

import net.jadoth.low.XVM;
import net.jadoth.math.XMath;
import sun.misc.Unsafe;




public class MainTestLittleEndianStringToAddress
{
	private static final ByteBuffer bb = ByteBuffer.allocateDirect(40);
	private static final long bb_address = XVM.getDirectByteBufferAddress(bb);

	public static void main(final String[] args)
	{
//		testHexDec((byte)0x01);
//		testHexDec((byte)0x09);
//		testHexDec((byte)0x0A);
//		testHexDec((byte)0x0F);
//		testHexDec((byte)0x10);
//		testHexDec((byte)0xAA);
//		testHexDec((byte)0xBC);
		testHexDec((byte)0xFF);
		testCorrectnessLong();
		testCorrectnessInt();
		testPerformanceInt();
	}

	private static final int SIZE = 1_000_000;
	private static final int RUNS = 100;
	private static final long[] TIMES = new long[RUNS];

	static final int[] ints;
	static {
//		ints = JaMath.sequence(SIZE);
//		ints = JaMath.randoming(SIZE, 1_000_000_000, 2_000_000_000);
//		ints = JaMath.randoming(SIZE, 100);
		ints = XMath.randoming(SIZE, 10);
//		ints = JaMath.randoming(SIZE, 100_000, 1_000_000);
	}

	public static void testPerformanceInt()
	{
		for(int r = 0; r < RUNS; r++)
		{
			final long tStart = System.nanoTime();
			for(int s = 0; s < SIZE; s++)
			{
				LittleEndianStringToAddress.toString(ints[s], bb_address);
			}
			TIMES[r] = System.nanoTime() - tStart;
		}
		double sum = 0.0;
		for(int r = 1; r < RUNS; r++)
		{
			sum += TIMES[r];
		}
		System.out.println("Average = " + new java.text.DecimalFormat("00,000,000,000").format(sum / (RUNS - 1)));
	}

	static void testCorrectnessLong()
	{
		test(Long.MAX_VALUE);
		test(9223372036854775807L);
		test( 922337203685477580L);
		test(  92233720368547758L);
		test(   9223372036854775L);
		test(    922337203685477L);
		test(     92233720368547L);
		test(      9223372036854L);
		test(       922337203685L);
		test(        92233720368L);
		test(         9223372036L);
		test(1000000000000000000L);
		test( 100000000000000000L);
		test(  10000000000000000L);
		test(   1000000000000000L);
		test(    100000000000000L);
		test(     10000000000000L);
		test(      1000000000000L);
		test(       100000000000L);
		test(        10000000000L);
		test(         3000000000L);
		test(      Long.MIN_VALUE);
	}

	static void testCorrectnessInt()
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

	static void testHexDec(final byte b)
	{
		final long currentAddress = LittleEndianStringToAddress.toHexDecString(b, XVM.getDirectByteBufferAddress(bb));
		final int length = (int)(currentAddress - XVM.getDirectByteBufferAddress(bb)) / 2;
		final char[] chars = new char[length];
		XVM.copyRange(null, XVM.getDirectByteBufferAddress(bb), chars, Unsafe.ARRAY_CHAR_BASE_OFFSET, length*2);
		final String s = new String(chars);
		final String output = b + "\t-->\t"+s;
		if(s.equals(Integer.toHexString(b).toUpperCase()))
		{
			System.out.println(output);
		}
		else
		{
			System.err.println(output);
		}
	}

	static void test(final long i)
	{
		final long currentAddress = LittleEndianStringToAddress.toString(i, XVM.getDirectByteBufferAddress(bb));
		final int length = (int)(currentAddress - XVM.getDirectByteBufferAddress(bb)) / 2;
		final char[] chars = new char[length];
		XVM.copyRange(null, XVM.getDirectByteBufferAddress(bb), chars, Unsafe.ARRAY_CHAR_BASE_OFFSET, length*2);
		final String s = new String(chars);
		final String output = i + "\t-->\t"+s;
		if(s.equals(Long.toString(i)))
		{
			System.out.println(output);
		}
		else
		{
			System.err.println(output);
		}
	}

	static void test(final int i)
	{
		final long currentAddress = LittleEndianStringToAddress.toString(i, XVM.getDirectByteBufferAddress(bb));
		final int length = (int)(currentAddress - XVM.getDirectByteBufferAddress(bb)) / 2;
		final char[] chars = new char[length];
		XVM.copyRange(null, XVM.getDirectByteBufferAddress(bb), chars, Unsafe.ARRAY_CHAR_BASE_OFFSET, length*2);
		final String s = new String(chars);
		final String output = i + "\t-->\t"+s;
		if(s.equals(Integer.toString(i)))
		{
			System.out.println(output);
		}
		else
		{
			System.err.println(output);
		}
	}

}

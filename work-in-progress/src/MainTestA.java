

import java.text.DecimalFormat;

import one.microstream.chars.VarString;



public class MainTestA
{
	static final DecimalFormat FORMAT = new DecimalFormat("#,###");

	static long test(final long a, final long b)
	{
		return a * b / 10_000_000_000L;
	}

	static void printTest(final long a, final long b)
	{
//		System.out.println(FORMAT.format(a)+" * "+FORMAT.format(b)+" = "+FORMAT.format(test(a, b)));
		final VarString vs = VarString.New();
		vs
		.add("0.").padLeft(Long.toString(a), 10, '0')
		.add(" * 0.").padLeft(Long.toString(b), 10, '0')
		.add(" = 0.").padLeft(Long.toString(test(a, b)), 10, '0')
		;
		System.out.println(vs);
	}

	public static void main(final String[] args)
	{
//		System.out.println(0xFFFF_FFFFL * 0x7FFF_FFFFL);
//		System.out.println(0xFFFF_FFFFL * 0x8000_0000L);
//		System.out.println(0xFFFF_FFFFL * 0x8000_0000L);
//		System.out.println(Long.toBinaryString(9223372034707292160L));

		printTest(1_000_000_000L, 1_000_000_000L);
		printTest(  100_000_000L,   100_000_000L);
		printTest(1_000_000_000L,   100_000_000L);
		printTest(    2_000_000L,       570_000L);

		printTest(    0XFFFF_FFFFL,       0x8000_0000L);

//		final long r = 1_000_000_000L * 1_000_000_000L;
//		System.out.println(Long.toBinaryString(1_000_000_000L));
//		System.out.println(Long.toBinaryString(r));
//		System.out.println(r / 10_000_000_000L);
//		final long r1 = r >> 32;
//		System.out.println(r1);

//		final long a = Long.MAX_VALUE, b = a;
//
//		final long result = a * b;
//		System.out.println(a+" * "+b+" = "+result);
//
//
//		final BigInteger bigA = BigInteger.valueOf(a);
//		final BigInteger bigB = BigInteger.valueOf(a);
//		final BigInteger bigC = bigA.multiply(bigB);
//		System.out.println(bigA+" * "+bigB+" = "+bigC);
//
//
//		final long aHigh = a >>> 32;
//		final long aLoW  = a & 0xFFFF_FFFFL;
//		final long bHigh = b >>> 32;
//		final long bLoW  = b & 0xFFFF_FFFFL;
//		System.out.println(Long.toBinaryString(aHigh));
//		System.out.println(Long.toBinaryString(aLoW));
	}
}

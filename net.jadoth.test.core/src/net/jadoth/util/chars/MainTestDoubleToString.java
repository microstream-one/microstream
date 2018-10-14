package net.jadoth.util.chars;

import net.jadoth.chars.XChars;
import net.jadoth.math.XMath;





/**
 * @author TM
 *
 */
public class MainTestDoubleToString
{
	public static void main(final String[] args)
	{

		test(10_000_000d);
		// tests //
//		testSingleValues();
//		testPerformance();
//		testGenerated();
	}

	static void test(final double d)
	{
//		System.out.println(d);
		final String s1 = Double.toString(d);
		final String s2 = toString(d);
		if(s1.equals(s2))
		{
			System.out.println(s2);
		}
		else
		{
			System.err.println(s1+"\t"+s2);
		}
	}

	static void testSingleValues()
	{
		test(9.999999999999998);
		test(99.99999999999998);
		test(999.9999999999998);
		test(9999.999999999998);
		test(99999.99999999998);
		test(999999.9999999998);
		test(9999999.999999998);

		test(99999999.99999998);
		test(999999999.9999998);
		test(9999999999.999998);
		test(99999999999.99998);
		test(999999999999.9998);

		test(9.999999999999997E100);

		test(0.9999999999999999);
		test(1.0000000000000047);
		test(0.99999999999999956);
		test(9.999999999999996);

		test(10_000_000.6);
		test(2.1234567890123456E200);
		test(1234.453);

		test(0.1234567890d);
		test(0.9990870008);
		test(0.99908799999999872d);
		test(0.0000000000000005);
		test(10_000_000d);
		test(9_999_999.999d);
		test(0.00099d);

		test(0.9000050000000020);
		test(0.9000050000000021);
		test(0.9000050000000022);
		test(0.9000050000000023);
		test(0.9000050000000024);
		test(0.9000050000000025);
		test(0.9000050000000026);
		test(0.9000050000000027);
		test(0.9000050000000028);
		test(0.9000050000000029);
		test(0.99999999999999932);
		test(0.55);
		test(0.01);
		test(0.09);
		test(0.001);
		test(0.002);
		test(0.003);
		test(0.004);
		test(0.005);
		test(0.006);
		test(0.007);
		test(0.008);
		test(0.009);
		test(0.56);
		test(0.81);
		test(0.202626);
		test(0.07585);
		test(0.00629);
		test(0.03607);
		test(0.10225057900787393);
		test(0.049908014508558196);
		test(0.43453746803211446);
		test(0.9894239279634777);
		test(0.885738835);
		test(0.670848332);
		test(0.341619073);
		test(0.0001);
		test(0.0002);
		test(0.0003);
		test(0.0004);
		test(0.0005);
		test(0.0006);
		test(0.0007);
		test(0.0008);
		test(0.0009);
		test(1E-10);
		test(0.00011);
		test(0.000102);

		test(1.2);
		test(10.2467);
		test(1000000.0);
		test(9000000.0);
		test(9999999.0);
		test(9999.46748);

		test(1E8);
		test(2.46846E8);
		test(1.0E15);
		test(1.0E16);
		test(1.0E17);
		test(1.0E18);
		test(1.0E19);
		test(8.76846E100);

		test(9.5464564E-10);
		test(4.5792280376476205E-5);
		test(4.579228037647621E-5);

		test(1.0);
		test(152273.12);
		test(20500.91);

		// (26.08.2013)TODO: double to string errors
		test(67333.49);
		test( 4209.02);
		test( 9011.79);
		test( 4972.77);
		test( 5962.56);
		test(66518.96);
		test(67305.38);
	}

	static void testPerformance()
	{
		final int SIZE = 100_000;
		final int RUNS = 10;
		final long[] TIMES = new long[RUNS];

		final double[] doubles = new double[SIZE];
		for(int i = 0; i < doubles.length; i++)
		{
//			doubles[i] = 0.001 + Math.random();
			doubles[i] = Math.random() * 10_000_000;
//			doubles[i] = Math.random() / 1000;
		}

		for(int r = 0; r < RUNS; r++)
		{
			final long tStart = System.nanoTime();
			for(int s = 0; s < SIZE; s++)
			{
				toString(doubles[s]);
//				Double.toString(doubles[s]);
			}
			TIMES[r] = System.nanoTime() - tStart;
		}
		double sum = 0.0;
		for(int r = 1; r < RUNS; r++)
		{
			sum += TIMES[r];
		}
		System.out.println("Average = " + new java.text.DecimalFormat("00,000,000,000").format(sum / (RUNS - 1) / SIZE));
	}

	static void testGenerated()
	{
		for(int i = 1000; i --> 0;)
		{
			test(Math.random() * 1_000_000);
			test(Math.random() * 5_000);
			test(XMath.round(Math.random(), 3));
			test(XMath.round(Math.random(), 6));
			test(XMath.round(Math.random(), 8));
			test(XMath.round(Math.random(), 9));
			test(Math.random() * 5E200);
			test(Math.random() / 100);
			test(Math.random() / 1000);
			test(Math.random() / 1E290);
		}
	}

	private static String toString(final double value)
	{
		final char[] buffer;
		return new String(buffer = new char[XChars.maxCharCount_double()], 0, XChars.put(value, buffer, 0));
	}




//	private static final double[] E3 = {1, 1E100, 1E200, 1E300, Double.MAX_VALUE};
//	private static final double[] E2 = {1, 1E10, 1E20, 1E30, 1E40, 1E50, 1E60, 1E70, 1E80, 1E90, 1E100};
//	private static final double[] E1 = {1, 1E1, 1E2, 1E3, 1E4, 1E5, 1E6, 1E7, 1E8, 1E9, 1E10};
//
//	private static int exponent2(final double value)
//	{
//		int exponent = 0;
//		double referenceValue = 1;
//
//		if(value >= 1E10)
//		{
//			if(value >= 1E100)
//			{
//				for(int e3 = 0; e3 < E3.length; e3++)
//				{
//					if(referenceValue * E3[e3] >= value)
//					{
//						if(referenceValue * E3[e3] == value)
//						{
//							return exponent + e3 * 100;
//						}
//						referenceValue *= E3[e3 - 1];
//						exponent += (e3 - 1) * 100;
//						break;
//					}
//				}
//			}
//			for(int e2 = 0; e2 < E2.length; e2++)
//			{
//				if(referenceValue * E2[e2] >= value)
//				{
//					if(referenceValue * E2[e2] == value)
//					{
//						return exponent + e2 * 10;
//					}
//					referenceValue *= E2[e2 - 1];
//					exponent += (e2 - 1) * 10;
//					break;
//				}
//			}
//		}
//		for(int e1 = 0; e1 < E1.length; e1++)
//		{
//			// something like that has to be applied in all three blocks to achieve correct results
//			if(value - referenceValue * E1[e1] < referenceValue)
//			{
//				if(referenceValue * E1[e1] == value)
//				{
//					return exponent + e1;
//				}
//				referenceValue *= E1[e1 - 1];
//				exponent += e1 - 1;
//				break;
//			}
//		}
//		return referenceValue == value ?exponent :exponent + 1;
//	}
//
//	static void printExpTest(final double d)
//	{
//		final int e2 = exponent2(d);
//		final int e1 = exponent(d);
//		if(e2 != e1)
//		{
//			System.out.println(d+"\t"+e2+"\t"+e1);
//		}
//	}
//	for(int i = 1; i < 308; i++)
//	{
//	printExpTest(pow10(i));
//}

}

package one.microstream.util.chars;

import one.microstream.chars.XChars;
import one.microstream.math.XMath;






/**
 * @author TM
 *
 */
public class MainTestFloatToString
{
	public static void main(final String[] args)
	{


		// tests //
//		testSingleValues();
		testGenerated();
//		testLength();
	}

	static void test(final float d)
	{
		final String s1 = Float.toString(d);
//		System.out.println("testing "+s1);
		final String s2 = XChars.toString(d);
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
		test(178182.06f);
		
//		test(0.80000050f);
//		test(0.80000051f);
//		test(0.80000052f);
//		test(0.80000053f);
//		test(0.80000054f);
//		test(0.80000055f);
//		test(0.80000056f);
//		test(0.80000057f);
//		test(0.80000058f);
//		test(0.80000059f);
//		test(0.90000050f);
//		test(0.90000051f);
//		test(0.90000052f);
//		test(0.90000053f);
//		test(0.90000054f);
//		test(0.90000055f);
//		test(0.90000056f);
//		test(0.90000057f);
//		test(0.90000058f);
//		test(0.90000059f);
//
//
//		test(0.114111684f);
//		test(0.104427904f);
//		test(0.102989666f);
//		test(0.113238834f);
//		test(0.106401965f);
//		test(0.123006344f);

	}

	static void testGenerated()
	{
		for(int i = 1000; i --> 0;)
		{
			test((float)(Math.random() * 1_000_000));
			test((float)(Math.random() * 5_000));
			test((float)XMath.round(Math.random(), 3));
			test((float)XMath.round(Math.random(), 6));
			test((float)XMath.round(Math.random(), 8));
			test((float)XMath.round(Math.random(), 9));
			test((float)(Math.random() * 5E200));
			test((float)(Math.random() / 100));
			test((float)(Math.random() / 1000));
			test((float)(Math.random() / 1E290));
			test((float)Math.random());
		}
	}

	static void testLength()
	{
		int maxLength = 0;
		for(int i = 1000; i --> 0;)
		{
			final float f = (float)Math.random();
			final String s = Float.toString(f);
			if(s.charAt(2) != '0' && s.charAt(s.length() - 3) != 'E')
			{
				if(s.length() == 11)
				{
					System.out.println(s);
				}
				if(s.length() > maxLength)
				{
					maxLength = s.length();
				}
			}
		}
	}

//	private static String toString(final double value)
//	{
//		final char[] buffer;
//		return new String(buffer = new char[XChars.maxCharCount_double()], 0, XChars.put(value, buffer, 0));
//	}

}

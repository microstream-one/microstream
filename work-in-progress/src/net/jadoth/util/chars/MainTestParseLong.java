package net.jadoth.util.chars;

import net.jadoth.chars.XChars;

public class MainTestParseLong
{
	public static void main(final String[] args)
	{
		System.out.println("Valid cases:");

		// zero stuff
		testParse("0");
		testParse("-0");
		testParse("0000");
		testParse("+000");
		testParse("-000");

		// single digits
		testParse("1");
		testParse("+1");
		testParse("-1");
		testParse("5");
		testParse("+5");
		testParse("-5");

		// normal case
		testParse("123");
		testParse("+123");
		testParse("-123");

		// leading zeroes
		testParse("0000123");
		testParse("+000123");
		testParse("-000123");

		// number range edge cases
		testParse(Long.toString(Long.MAX_VALUE));
		testParse(Long.toString(Long.MAX_VALUE-1));
		testParse(Long.toString(Long.MIN_VALUE));
		testParse(Long.toString(Long.MIN_VALUE+1));

		// arbitrary tests
		testParse("922337203854775810");
		testParse("+922337206854775810");
		testParse("-922337206854775810");
		testParse("2682627401631922176");

		System.err.println("Invalid cases:");

		// invalid literals
		testParse("A");
		testParse("+");
		testParse("-");
		testParse("--123");
		testParse("-+123");
		testParse("+-123");

		// invalid number range literal
		testParse("9223372036854775810");
		testParse("+9223372036854775810");
		testParse("-9223372036854775810");
	}



	static void testParse(final String s)
	{
		try
		{
			final long parsedValue = XChars.parse_longDecimal(s.toCharArray(), 0, s.length());
			System.out.println(s+"\t"+parsedValue);
		}
		catch(final Exception e)
		{
			System.err.println(s+"\t"+e.getMessage());
		}

	}
}

package net.jadoth.util.chars;

import net.jadoth.chars.JadothChars;

public class MainTestParseByte
{
	public static void main(final String[] args)
	{
		System.out.println("Valid cases:");

		// zero stuff
		testParse("0");
		testParse("-0");
		testParse("000");
		testParse("+00");
		testParse("-00");

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
		testParse("000123");
		testParse("+00012");
		testParse("-000123");

		// number range edge cases
		testParse(Integer.toString(Byte.MAX_VALUE));
		testParse(Integer.toString(Byte.MAX_VALUE-1));
		testParse(Integer.toString(Byte.MIN_VALUE));
		testParse(Integer.toString(Byte.MIN_VALUE+1));

		System.err.println("Invalid cases:");

		// invalid literals
		testParse("A");
		testParse("+");
		testParse("-");
		testParse("--123");
		testParse("-+123");
		testParse("+-123");

		// invalid number range literal
		testParse("128");
		testParse("+128");
		testParse("-129");
	}



	static void testParse(final String s)
	{
		try
		{
			final byte parsedValue = JadothChars.parse_byteDecimal(s.toCharArray());
			System.out.println(s+"\t"+parsedValue);
		}
		catch(final Exception e)
		{
			System.err.println(s+"\t"+e.getMessage());
		}

	}
}

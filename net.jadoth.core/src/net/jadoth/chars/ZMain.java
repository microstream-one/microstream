package net.jadoth.chars;

public class ZMain
{
	public static void main(final String[] args)
	{
		printExtractedSimpleQuoteContent("'Stuff:moep111' lalala''");
	}
	
	
	static void printExtractedSimpleQuoteContent(final String input)
	{
		System.out.println(extractSimpleQuoteContent(input));
	}
	
	static String extractSimpleQuoteContent(final String input)
	{
		final char[] chars = input.toCharArray();
		
//		final int endIndex = XChars.skipSimpleQuote(chars, 0, chars.length);
//		return new String(chars, 1, endIndex - 2);
		
		return XChars.parseSimpleQuote(chars, 0, chars.length);
	}
}

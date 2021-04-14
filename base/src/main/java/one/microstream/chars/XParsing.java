package one.microstream.chars;

import java.util.function.Consumer;

import one.microstream.exceptions.ParsingException;
import one.microstream.exceptions.ParsingExceptionUnexpectedCharacterInArray;

public final class XParsing
{
	// generic parsing helper methods. Intentionally no bounds checks as these are meant for internal, safe, use.
	
	public static final int skipWhiteSpaces(final char[] input, final int iStart, final int iBound)
	{
		int i = iStart;
		while(i < iBound && input[i] <= ' ')
		{
			i++;
		}
		
		return i;
	}
	
	public static final int skipWhiteSpacesReversed(final char[] input, final int iStart, final int iBound)
	{
		int i = iBound - 1;
		while(i >= iStart && input[i] <= ' ')
		{
			i--;
		}
		
		return i;
	}
	
	/**
	 * Skips to the position beyond the second occurance of the current character (input[iStart]).
	 * This simple logic does NOT support escaping.
	 * 
	 * @param input
	 * @param iStart
	 * @param iBound
	 */
	public static final int skipSimpleQuote(final char[] input, final int iStart, final int iBound)
	{
		return skipToSimpleTerminator(input, iStart + 1, iBound, input[iStart]);
	}
	
	public static final int skipToSimpleTerminator(
		final char[] input     ,
		final int    iStart    ,
		final int    iBound    ,
		final char   terminator
	)
	{
		// actual start is one character behind the opening quote
		int i = iStart;
		while(i < iBound)
		{
			if(input[i++] == terminator)
			{
				return i;
			}
		}
			
		// no occurance has been found. No quote to skip. Current index is returned.
		return iStart;
	}
	
	public static final String parseSimpleQuote(final char[] input, final int iStart, final int iBound)
	{
		final int iQuoteEnd = skipSimpleQuote(input, iStart, iBound);
		if(iQuoteEnd == iStart)
		{
			throw new IllegalArgumentException("No simple quote character found at index " + iStart + ".");
		}
		
		return new String(input, iStart + 1, iQuoteEnd - iStart - 2);
	}
	
	public static final int parseSimpleQuote(
		final char[]                   input   ,
		final int                      iStart  ,
		final int                      iBound  ,
		final Consumer<? super String> receiver
	)
	{
		final int iQuoteEnd = skipSimpleQuote(input, iStart, iBound);
		if(iQuoteEnd == iStart)
		{
			throw new IllegalArgumentException("No simple quote character found at index " + iStart + ".");
		}
		
		receiver.accept(new String(input, iStart + 1, iQuoteEnd - iStart - 2));
		
		return iQuoteEnd;
	}
	
	public static final int parseToSimpleTerminator(
		final char[]                   input     ,
		final int                      iStart    ,
		final int                      iBound    ,
		final char                     terminator,
		final Consumer<? super String> receiver
	)
	{
		final int iEnd = skipToSimpleTerminator(input, iStart, iBound, terminator);
		if(iEnd == iStart)
		{
			throw new ParsingException("No terminator found in index range [" + iStart + "," + iBound + "]");
		}
		
		receiver.accept(new String(input, iStart, iEnd - iStart - 1));
		
		return iEnd;
	}
	
	public static final int checkStartsWith(
		final char[] input  ,
		final int    iStart ,
		final int    iBound ,
		final String subject
	)
	{
		return checkStartsWith(input, iStart, iBound, subject, null);
	}
	
	public static final int checkStartsWith(
		final char[] input      ,
		final int    iStart     ,
		final int    iBound     ,
		final String subject    ,
		final String contextHint
	)
	{
		if(startsWith(input, iStart, iBound, subject))
		{
			return iStart + subject.length();
		}
		
		throw new ParsingException(
			"String \"" + subject + "\" not found at index " + iStart +
			(contextHint == null ? "." : "(" + contextHint + ").")
		);
	}
	
	public static final boolean startsWith(final char[] input, final int iStart, final int iBound, final String subject)
	{
		// intentionally no length quick-check before array creation. The string is assumed to fit in.
		return startsWith(input, iStart, iBound, XChars.readChars(subject));
	}
	
	public static final boolean startsWith(final char[] input, final int iStart, final int iBound, final char[] subject)
	{
		if(iBound - iStart < subject.length)
		{
			return false;
		}
		
		for(int i = 0; i < subject.length; i++)
		{
			if(subject[i] != input[iStart + i])
			{
				return false;
			}
		}
		
		return true;
	}
	
	public static final int checkCharacter(final char[] input, final int i, final char c)
		throws ParsingExceptionUnexpectedCharacterInArray
	{
		return checkCharacter(input, i, c, null);
	}
	
	public static final int checkCharacter(final char[] input, final int i, final char c, final String contextHint)
		throws ParsingExceptionUnexpectedCharacterInArray
	{
		if(input[i] != c)
		{
			// (06.11.2018 TM)NOTE: for once, not a provisional exception, but a proper one.
			throw new ParsingExceptionUnexpectedCharacterInArray(input, i, c, input[i], contextHint);
		}
		
		return i + 1;
	}
	
	public static final void checkIncompleteInput(final int i, final int iBound)
	{
		checkIncompleteInput(i, iBound, null);
	}
	
	public static final void checkIncompleteInput(final int i, final int iBound, final String contextHint)
	{
		if(i < iBound)
		{
			return;
		}
		
		throw new ParsingException(
			"Incomplete input: reached end of characters at index " + i
			+ (contextHint == null ? "." : " (" + contextHint + ").")
		);
	}
		
	
	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	/**
	 * Dummy constructor to prevent instantiation of this static-only utility class.
	 * 
	 * @throws UnsupportedOperationException
	 */
	private XParsing()
	{
		// static only
		throw new UnsupportedOperationException();
	}
}

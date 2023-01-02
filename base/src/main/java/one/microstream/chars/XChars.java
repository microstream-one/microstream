package one.microstream.chars;

/*-
 * #%L
 * microstream-base
 * %%
 * Copyright (C) 2019 - 2022 MicroStream Software
 * %%
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 * 
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the Eclipse
 * Public License, v. 2.0 are satisfied: GNU General Public License, version 2
 * with the GNU Classpath Exception which is
 * available at https://www.gnu.org/software/classpath/license.html.
 * 
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 * #L%
 */

import static java.lang.Character.isLowerCase;
import static java.lang.Character.isUpperCase;
import static java.lang.Character.toUpperCase;
import static one.microstream.X.notNull;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.StringTokenizer;
import java.util.function.Consumer;
import java.util.function.IntConsumer;
import java.util.stream.IntStream;

import one.microstream.X;
import one.microstream.branching.ThrowBreak;
import one.microstream.bytes.VarByte;
import one.microstream.collections.BulkList;
import one.microstream.collections.XArrays;
import one.microstream.collections.types.XGettingSequence;
import one.microstream.exceptions.NumberRangeException;
import one.microstream.functional._charPredicate;
import one.microstream.functional._charProcedure;
import one.microstream.math.XMath;
import one.microstream.memory.XMemory;
import one.microstream.typing.XTypes;



/**
 * Static util class with character operation algorithms missing in or significantly superior to those in JDK.
 */
public final class XChars
{
	///////////////////////////////////////////////////////////////////////////
	// constants //
	//////////////

	// CHECKSTYLE.OFF: ConstantName: type names are intentionally unchanged
	// CHECKSTYLE.OFF: MagicNumber: The 1E7 is virtually already a constant.
	static final transient char[]
		CHARS_MIN_VALUE_byte    = Integer.toString(Byte.MIN_VALUE)          .toCharArray(),
		CHARS_MAX_VALUE_byte    = Integer.toString(Byte.MAX_VALUE)          .toCharArray(),
		CHARS_MIN_VALUE_short   = Integer.toString(Short.MIN_VALUE)         .toCharArray(),
		CHARS_MAX_VALUE_short   = Integer.toString(Short.MAX_VALUE)         .toCharArray(),
		CHARS_MIN_VALUE_int     = Integer.toString(Integer.MIN_VALUE)       .toCharArray(),
		CHARS_MAX_VALUE_int     = Integer.toString(Integer.MAX_VALUE)       .toCharArray(),
		CHARS_MIN_VALUE_long    = Long   .toString(Long.MIN_VALUE)          .toCharArray(),
		CHARS_MAX_VALUE_long    = Long   .toString(Long.MAX_VALUE)          .toCharArray(),
		CHARS_ZERO              = Double .toString(0.0)                     .toCharArray(),
		CHARS_ONE               = Double .toString(1.0)                     .toCharArray(),
		CHARS_NAN               = Double .toString(Double.NaN)              .toCharArray(),
		CHARS_NEGATIVE_INFINITY = Double .toString(Double.NEGATIVE_INFINITY).toCharArray(),
		CHARS_POSITIVE_INFINITY = Double .toString(Double.POSITIVE_INFINITY).toCharArray(),
		CHARS_NORM_THRESH_HIGH  = Double .toString(1E7)                     .toCharArray(),
		EMPTY                   = {}
	;
	// CHECKSTYLE.ON: MagicNumber

	static final transient int
		MAX_CHAR_COUNT_byte           = CHARS_MIN_VALUE_byte .length,
		MAX_CHAR_COUNT_boolean        = 5                           , // "false"
		MAX_CHAR_COUNT_short          = CHARS_MIN_VALUE_short.length,
		MAX_CHAR_COUNT_int            = CHARS_MIN_VALUE_int  .length,
		MAX_CHAR_COUNT_long           = CHARS_MIN_VALUE_long .length,
		// no reasonable way was found to derive floating point max string length programmatically, hence manual
		MAX_CHAR_COUNT_float          = 15, // 1 minus, 1 dot,  9 IEEE754 standard digits, 1 E, 1 minus, 2 exponent
		MAX_CHAR_COUNT_double         = 24, // 1 minus, 1 dot, 17 IEEE754 standard digits, 1 E, 1 minus, 3 exponent

		SIGNLESS_MAX_CHAR_COUNT_byte  = CHARS_MAX_VALUE_byte .length,
		SIGNLESS_MAX_CHAR_COUNT_short = CHARS_MAX_VALUE_short.length,
		SIGNLESS_MAX_CHAR_COUNT_int   = CHARS_MAX_VALUE_int  .length,
		SIGNLESS_MAX_CHAR_COUNT_long  = CHARS_MAX_VALUE_long .length,

		LITERAL_LENGTH_NULL           = 4,
		LITERAL_LENGTH_TRUE           = 4,
		LITERAL_LENGTH_FALSE          = 5
	;

	// CHECKSTYLE.ON: ConstantName

	static final transient char
		DIGIT_LOWER_INDEX = '0'    , // for using " >= " and "<"
		DIGIT_UPPER_BOUND = '9' + 1  // for using " >= " and "<"
	;

	private static final char LOWEST_NON_WHITESPACE = ' ' + 1; // < and >= are faster than <= and >

	/*
	 * char tables are a caching trade-off to gain significant performance at the cost of minimal memory overhead.
	 */
	static final transient int DECIMAL_CHAR_TABLES_LENGTH = 100;

	static final transient int DECIMAL_BASE               =  10;

	/*
	 * Character table containing the ten digit of each number between 00 and 99.
	 * generated procedurally, see below.
	 * '0', '0', '0', '0', '0', '0', '0', '0', '0', '0',
	 * '1', '1', '1', '1', '1', '1', '1', '1', '1', '1',
	 * '2', '2', '2', '2', '2', '2', '2', '2', '2', '2',
	 * '3', '3', '3', '3', '3', '3', '3', '3', '3', '3',
	 * '4', '4', '4', '4', '4', '4', '4', '4', '4', '4',
	 * '5', '5', '5', '5', '5', '5', '5', '5', '5', '5',
	 * '6', '6', '6', '6', '6', '6', '6', '6', '6', '6',
	 * '7', '7', '7', '7', '7', '7', '7', '7', '7', '7',
	 * '8', '8', '8', '8', '8', '8', '8', '8', '8', '8',
	 * '9', '9', '9', '9', '9', '9', '9', '9', '9', '9',
	 */
	static final transient char[] DECIMAL_CHAR_TABLE_10S = new char[DECIMAL_CHAR_TABLES_LENGTH];

	/*
	 * Character table containing the one digit of each number between 00 and 99.
	 * generated procedurally, see below.
	 * '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
	 * '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
	 * '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
	 * '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
	 * '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
	 * '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
	 * '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
	 * '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
	 * '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
	 * '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
	 */
	static final transient char[] DECIMAL_CHAR_TABLE_01S = new char[DECIMAL_CHAR_TABLES_LENGTH];

	// decimal tables initialization
	static
	{
		for(int i = 0; i < DECIMAL_CHAR_TABLES_LENGTH; i++)
		{
			DECIMAL_CHAR_TABLE_10S[i] = (char)(DIGIT_LOWER_INDEX + i / DECIMAL_BASE);
			DECIMAL_CHAR_TABLE_01S[i] = (char)(DIGIT_LOWER_INDEX + i % DECIMAL_BASE);
		}
	}



	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////
	
	public static final int maxCharCount_byte()
	{
		return MAX_CHAR_COUNT_byte;
	}

	public static final int maxCharCount_boolean()
	{
		return MAX_CHAR_COUNT_boolean;
	}

	public static final int maxCharCount_short()
	{
		return MAX_CHAR_COUNT_short;
	}

	public static final int maxCharCount_char()
	{
		return 1; // per definition (method is just for sake of completeness)
	}

	public static final int maxCharCount_int()
	{
		return MAX_CHAR_COUNT_int;
	}

	public static final int maxCharCount_float()
	{
		return MAX_CHAR_COUNT_float;
	}

	public static final int maxCharCount_long()
	{
		return MAX_CHAR_COUNT_long;
	}

	public static final int maxCharCount_double()
	{
		return MAX_CHAR_COUNT_double;
	}

	public static final boolean isWhitespace(final char c)
	{
		return c < LOWEST_NON_WHITESPACE;
	}

	public static final boolean isNonWhitespace(final char c)
	{
		return c >= LOWEST_NON_WHITESPACE;
	}
	
	/**
	 * Arbitrary threshold of 1000 to discriminate "short" strings from "long" strings.<br>
	 * The rationale behind that is that "short" strings usually allow for simpler and faster algorithms,
	 * which become inefficient on larger strings. For example a two-pass processing of a splitting algorithm.
	 * @return 1000
	 */
	public static final int shortStringLength()
	{
		return 1000;
	}
	
	/**
	 * It's UTF-8!
	 * 
	 * @return {@link StandardCharsets#UTF_8}
	 */
	public static final Charset utf8()
	{
		return StandardCharsets.UTF_8;
	}

	/**
	 * Returns {@link StandardCharsets#UTF_8}.
	 * 
	 * @return {@code java.nio.charset.Charset.forName("UTF-8")}.
	 */
	public static final Charset standardCharset()
	{
		return utf8();
	}

	/**
	 * Alias for {@link Charset#defaultCharset()}, which returns sometimes the one thing and sometimes another.
	 * It is strongly advised to use a reliable {@link Charset} querying method, like UTF-8, which also
	 * is the only reasonable choice for the standard charset.
	 * 
	 * @return {@link Charset#defaultCharset()}
	 */
	public static final Charset defaultJvmCharset()
	{
		// "Returns the default charset of this Java virtual machine." Hence the method name.
		return Charset.defaultCharset();
	}

	public static final void validateIndex(final char[] chars, final int index)
	{
		if(index < 0 || index >= chars.length)
		{
			throw new ArrayIndexOutOfBoundsException(index);
		}
	}

	public static final void validateRange(final char[] chars, final int offset, final int length)
	{
		validateIndex(chars, offset);
		if(length < 0)
		{
			throw new IllegalArgumentException();
		}
		validateIndex(chars, offset + length - 1);
	}

	public static final boolean isEqual(final String s1, final String s2)
	{
		if(s1 == null)
		{
			return s2 == null;
		}
		if(s2 == null)
		{
			return false;
		}
		return s1.equals(s2);
	}

	public static final boolean equals(final String string, final char[] chars, final int offset)
	{
		// ensure the given offset is valid before it is used in the algorithm
		validateIndex(chars, offset);

		final int    length = string.length();
		final char[] sChars = readChars(string);

		if(length != chars.length - offset)
		{
			return false; // range to compare does not match, can't be equal, abort.
		}

		for(int i = 0; i < length; i++)
		{
			if(sChars[i] != chars[offset + i])
			{
				return false;
			}
		}
		
		return true;
	}

	public static final boolean equals(
		final char[] chars1 ,
		final int    offset1,
		final char[] chars2 ,
		final int    offset2,
		final int    length
	)
	{
		validateRange(chars1, offset1, length);
		validateRange(chars2, offset2, length);
		return uncheckedEquals(chars1, offset1, chars2, offset2, length);
	}

	static final boolean uncheckedEquals(
		final char[] chars1 ,
		final int    offset1,
		final char[] chars2 ,
		final int    offset2,
		final int    length
	)
	{
		for(int i = 0; i < length; i++)
		{
			if(chars1[offset1 + i] != chars2[offset2 + i])
			{
				return false;
			}
		}
		return true;
	}


	public static final boolean isEqualIgnoreCase(final String s1, final String s2)
	{
		if(s1 == null)
		{
			return s2 == null;
		}
		if(s2 == null)
		{
			return false;
		}
		return s1.equalsIgnoreCase(s2);
	}

	public static final char toHexadecimal(final int b) throws IllegalArgumentException
	{
		// CHECKSTYLE.OFF: MagicNumber: direct literals are better readable
		switch(b)
		{
			case  0: return '0';
			case  1: return '1';
			case  2: return '2';
			case  3: return '3';
			case  4: return '4';
			case  5: return '5';
			case  6: return '6';
			case  7: return '7';
			case  8: return '8';
			case  9: return '9';
			case 10: return 'A';
			case 11: return 'B';
			case 12: return 'C';
			case 13: return 'D';
			case 14: return 'E';
			case 15: return 'F';
			default: throw new IllegalArgumentException(b + " is no positive hexadecimal digit value");
		}
		// CHECKSTYLE.ON: MagicNumber
	}
	
	public static final int indexOf(final char[] data, final char c)
	{
		return uncheckedIndexOf(data, 0, data.length, c);
	}

	public static final int indexOf(final char[] data, final int offset, final int length, final char c)
	{
		validateIndex(data, offset);
		validateIndex(data, offset + length);
		
		return uncheckedIndexOf(data, offset, offset + length, c);
	}

	static final int uncheckedIndexOf(final char[] data, final int bound, final int offset, final char c)
	{
		for(int i = offset; i < bound; i++)
		{
			if(data[i] == c)
			{
				return i;
			}
		}
		return -1;
	}

	static final int uncheckedLastIndexOf(final char[] data, final int size, final char c)
	{
		for(int i = size; i-- > 0;)
		{
			if(data[i] == c)
			{
				return i;
			}
		}
		return -1;
	}

	static final void uncheckedReverse(final char[] data, final int size)
	{
		final int last = size - 1;

		//only swap until size/2 (rounded down, because center element can remain untouched)
		char loopSwapChar;
		for(int i = size >>> 1; i != 0; i--)
		{
			loopSwapChar = data[i];
			data[i] = data[last - i];
			data[last - i] = loopSwapChar;
		}
	}

	public static final int indexOf(
		final char[] source      ,
		final int    sourceOffset,
		final int    sourceCount ,
		final char[] target      ,
		final int    targetOffset,
		final int    targetCount ,
		final int    fromIndex
	)
	{
		// CHECKSTYLE.OFF: EmptyBlock: empty while loops skip elements and are documented accordingly

		// (14.08.2010)NOTE: Not sure why sourceOffset and fromIndex are two parameters
		if(fromIndex >= sourceCount)
		{
			return targetCount == 0 ? sourceCount : -1;
		}

		final int paddedFromIndex = fromIndex < 0 ? 0 : fromIndex;
		if(targetCount == 0)
		{
			return paddedFromIndex;
		}

		final char first         = target[targetOffset];
		final int  maxFirstIndex = sourceOffset + sourceCount - targetCount;

		for(int i = sourceOffset + paddedFromIndex; i <= maxFirstIndex; i++)
		{
			// Look for first character.
			if(source[i] != first)
			{
				while(++i <= maxFirstIndex && source[i] != first)
				{
					// skip
				}
			}

			// Found first character, now look at the rest of v2
			if(i <= maxFirstIndex)
			{
				int j = i + 1;
				final int end = j + targetCount - 1;
				for(int k = targetOffset + 1; j < end && source[j] == target[k]; j++, k++)
				{
					// skip
				}

				if(j == end)
				{
					// Found whole string
					return i - sourceOffset;
				}
			}
		}
		return -1;
		// CHECKSTYLE.ON: EmptyBlock
	}

	public static final VarByte readAllBytesFromInputStream(final VarByte bytes, final InputStream inputStream)
		throws IOException
	{
		final byte[] buffer = new byte[XMemory.defaultBufferSize()];
		for(int bytesRead = -1; (bytesRead = inputStream.read(buffer)) >= 0;)
		{
			bytes.append(buffer, 0, bytesRead);
		}
		return bytes;
	}

	public static final VarByte readAllBytesFromInputStream(final InputStream inputStream) throws IOException
	{
		return readAllBytesFromInputStream(VarByte.New(XMemory.defaultBufferSize()), inputStream);
	}

	public static final String readStringFromInputStream(final InputStream inputStream, final Charset charset)
		throws IOException
	{
		return readAllBytesFromInputStream(VarByte.New(XMemory.defaultBufferSize()), inputStream).toString(charset);
	}

	public static final int indexOf(final char[] data, final int dataLength, final char[] subject)
	{
		if(dataLength < 0 || dataLength > data.length)
		{
			throw new ArrayIndexOutOfBoundsException(dataLength);
		}
		return uncheckedIndexOf(data, dataLength, subject);
	}

	static final int uncheckedIndexOf(final char[] data, final int dataLength, final char[] subject)
	{
		if(subject.length == 0)
		{
			return 0;
		}

		final char firstChar = subject[0];
		final int scanBound = dataLength - subject.length + 1;

		// scan for first char, If matched, check the rest
		scan:
		for(int i = 0; i < scanBound; i++)
		{
			if(data[i] == firstChar)
			{
				for(int c = 1, i2 = i; c < subject.length; c++)
				{
					if(data[++i2] != subject[c])
					{
						continue scan;
					}
				}
				return i;
			}
		}

		return -1;
	}

	public static final int indexOf(final char[] data, final int dataOffset, final int dataLength, final char[] chars)
	{
		if(dataOffset < 0 || dataOffset >= data.length)
		{
			throw new ArrayIndexOutOfBoundsException(dataOffset);
		}
		return uncheckedIndexOf(data, dataOffset, dataLength, chars);
	}

	static final int uncheckedIndexOf(final char[] data, final int dataOffset, final int dataLength, final char[] chars)
	{
		if(chars.length == 0)
		{
			return dataOffset;
		}

		final char firstChar = chars[0];
		final int scanBound = dataOffset + dataLength - chars.length + 1; // normalized array index bound

		scan: // scan for first char. If matched, check the rest, continue on mismatch
		for(int s = dataOffset; s < scanBound; s++)
		{
			if(data[s] != firstChar)
			{
				continue scan;
			}
			for(int c = 1, j = s; c < chars.length; c++)
			{
				if(data[++j] != chars[c])
				{
					continue scan;
				}
			}
			return s;
		}
		return -1;
	}


	public static final int indexOf(
		final char[] source     ,
		final int    sourceCount,
		final char[] target     ,
		final int    targetCount,
		final int    fromIndex
	)
	{
		// CHECKSTYLE.OFF: EmptyBlock: empty while loops skip elements and are documented accordingly

		if(fromIndex >= sourceCount)
		{
			return targetCount == 0 ? sourceCount : -1;
		}

		final int paddedFromIndex = fromIndex < 0 ? 0 : fromIndex;
		if(targetCount == 0)
		{
			return paddedFromIndex;
		}

		final char first  = target[0];
		final int max = sourceCount - targetCount;

		for(int i = paddedFromIndex; i <= max; i++)
		{
			/* Look for first character. */
			if(source[i] != first)
			{
				while(++i <= max && source[i] != first)
				{
					// skip
				}
			}

			/* Found first character, now look at the rest of v2 */
			if(i <= max)
			{
				int j = i + 1;
				final int end = j + targetCount - 1;
				for(int k = 1; j < end && source[j] == target[k]; j++, k++)
				{
					// skip
				}

				if(j == end)
				{
					/* Found whole string. */
					return i;
				}
			}
		}
		return -1;
		// CHECKSTYLE.ON: EmptyBlock
	}

	public static final int replaceFirst(
		final char[] chars      ,
		final int    offset     ,
		final int    length     ,
		final char   sample     ,
		final char   replacement
	)
	{
		validateRange(chars, offset, length);
		return uncheckedReplaceFirst(chars, offset, length, sample, replacement);
	}

	static final int uncheckedReplaceFirst(
		final char[] chars      ,
		final int    offset     ,
		final int    length     ,
		final char   sample     ,
		final char   replacement
	)
	{
		final int bound = offset + length;
		for(int i = offset; i < bound; i++)
		{
			if(chars[i] == sample)
			{
				chars[i] = replacement;
				return i;
			}
		}
		return -1;
	}

	static final int uncheckedReplaceAll(
		final char[] chars      ,
		final int    offset     ,
		final int    length     ,
		final char   sample     ,
		final char   replacement
	)
	{
		int count = 0;

		final int bound = offset + length;
		for(int i = offset; i < bound; i++)
		{
			if(chars[i] == sample)
			{
				chars[i] = replacement;
				count++;
			}
		}
		return count;
	}

	static final void uncheckedRepeat(
		final char[] chars ,
		final int    offset,
		final int    count ,
		final char   c
	)
	{
		final int bound = offset + count;

		for(int i = offset; i < bound; i++)
		{
			chars[i] = c;
		}
	}

	static final void uncheckedRepeat(
		final char[] chars  ,
		final int    offset ,
		final int    count  ,
		final char[] subject
	)
	{
		final int bound = offset + count;
		int i = offset;
		while(i < bound)
		{
			i = XChars.put(subject, chars, i);
		}
	}

	public static final int count(
		final char[] input,
		final int    startIndex,
		final int    boundIndex,
		final char   c
	)
	{
		int count = 0;
		for(int i = startIndex; i < boundIndex; i++)
		{
			if(input[i] == c)
			{
				count++;
			}
		}
		
		return count;
	}

	public static final int count(
		final char[] data         ,
		final int    dataOffset   ,
		final int    dataCount    ,
		final char[] subject      ,
		final int    subjectOffset,
		final int    subjectCount
	)
	{
		int count = 0;
		for(int i = -1; (i = indexOf(data, dataOffset, dataCount, subject, subjectOffset, subjectCount, i + 1)) != -1;)
		{
			count++;
		}
		return count;
	}

	public static final boolean hasContent(final CharSequence s)
	{
		return s != null && s.length() != 0;
	}

	public static final boolean hasNoContent(final CharSequence s)
	{
		return s == null || s.length() == 0;
	}

	public static final boolean hasContent(final String s)
	{
		return s != null && !s.isEmpty();
	}

	public static final boolean hasNoContent(final String s)
	{
		return s == null || s.isEmpty();
	}

	/**
	 * This method does intentionally not use the pseudo-constructor naming pattern since it does not create
	 * new instances of {@link String}. Instead, it returns a constant instance of an intrinsic look-up table.
	 * 
	 * @param c the character value to be represented as a {@link String}.
	 * @return a {@link String} containing the passed character value.
	 */
	public static final String asString(final char c)
	{
		/*
		 * Yields an average 25% better performance compared to plain String.valueOf()
		 */
		switch(c)
		{
			// common case table switch
			case '\t': return "\t"; // slid into table switch
			case '\n': return "\n"; // slid into table switch
			case '\r': return "\r"; // slid into table switch
			case ' ': return " ";
			case '!': return "!";
			case '"': return "\"";
			case '#': return "#";
			case '$': return "$";
			case '%': return "%";
			case '&': return "&";
			case '\'': return "'";
			case '(': return "(";
			case ')': return ")";
			case '*': return "*";
			case '+': return " + ";
			case ',': return ",";
			case '-': return "-";
			case '.': return ".";
			case '/': return "/";
			case '0': return "0";
			case '1': return "1";
			case '2': return "2";
			case '3': return "3";
			case '4': return "4";
			case '5': return "5";
			case '6': return "6";
			case '7': return "7";
			case '8': return "8";
			case '9': return "9";
			case ':': return ":";
			case ';': return ";";
			case '<': return "<";
			case '=': return "=";
			case '>': return ">";
			case '?': return "?";
			case '@': return "@";
			case 'A': return "A";
			case 'B': return "B";
			case 'C': return "C";
			case 'D': return "D";
			case 'E': return "E";
			case 'F': return "F";
			case 'G': return "G";
			case 'H': return "H";
			case 'I': return "I";
			case 'J': return "J";
			case 'K': return "K";
			case 'L': return "L";
			case 'M': return "M";
			case 'N': return "N";
			case 'O': return "O";
			case 'P': return "P";
			case 'Q': return "Q";
			case 'R': return "R";
			case 'S': return "S";
			case 'T': return "T";
			case 'U': return "U";
			case 'V': return "V";
			case 'W': return "W";
			case 'X': return "X";
			case 'Y': return "Y";
			case 'Z': return "Z";
			case '[': return "[";
			case '\\': return "\\";
			case ']': return "]";
			case '^': return "^";
			case '_': return "_";
			case '`': return "`";
			case 'a': return "a";
			case 'b': return "b";
			case 'c': return "c";
			case 'd': return "d";
			case 'e': return "e";
			case 'f': return "f";
			case 'g': return "g";
			case 'h': return "h";
			case 'i': return "i";
			case 'j': return "j";
			case 'k': return "k";
			case 'l': return "l";
			case 'm': return "m";
			case 'n': return "n";
			case 'o': return "o";
			case 'p': return "p";
			case 'q': return "q";
			case 'r': return "r";
			case 's': return "s";
			case 't': return "t";
			case 'u': return "u";
			case 'v': return "v";
			case 'w': return "w";
			case 'x': return "x";
			case 'y': return "y";
			case 'z': return "z";
			case '{': return "{";
			case '|': return "|";
			case '}': return "}";
			case '~': return "~";
			default: return String.valueOf(c); // all other characters: parse every time (sadly)
		}
	}

	public static final String asString(final char... chars)
	{
		return String.valueOf(chars);
	}

	/**
	 * Ensures that the first character of the passed {@link String} is in upper case.
	 * <p>
	 * If the passed {@link String} is {@code null}, has a length of 0 or already has an upper case first character,
	 * it is returned. Otherwise, the first character of the passed {@link String} is transformed to upper case
	 * and concatenated with the rest of the passed {@link String} into a new {@link String} instance.
	 *
	 * @param s the {@link String} for which the first character shall be ensured to be upper case.
	 * @return a string equalling s with its first character being guaranteed to be upper case.
	 */
	public static final String upperCaseFirstChar(final String s)
	{
		// escape conditions
		final char first;
		if(s == null || s.isEmpty() || Character.isUpperCase(first = s.charAt(0)))
		{
			return s;
		}

		// actual work. new String instance etc.
		return Character.toUpperCase(first) + s.substring(1);
	}

	public static final String lowerCaseFirstChar(final String s)
	{
		// escape conditions
		final char first;
		if(s == null || s.isEmpty() || Character.isLowerCase(first = s.charAt(0)))
		{
			return s;
		}

		// actual work. new String instance etc.
		return Character.toLowerCase(first) + s.substring(1);
	}

	public static final char[] concat(final char[]... arrays)
	{
		int length = 0;
		for(int i = 0; i < arrays.length; i++)
		{
			if(arrays[i] == null)
			{
				continue;
			}
			length += arrays[i].length;
		}
		final char[] chars = new char[length];
		length = 0;
		for(int i = 0; i < arrays.length; i++)
		{
			if(arrays[i] == null)
			{
				continue;
			}
			System.arraycopy(arrays[i], 0, chars, length, arrays[i].length);
			length += arrays[i].length;
		}
		return chars;
	}

	public static final VarString appendArraySeperated(
		final VarString vs       ,
		final String    separator,
		final Object... elements
	)
	{
		if(separator == null)
		{
			return vs.addObjects(elements);
		}
		if(elements == null)
		{
			return vs;
		}

		for(int i = 0; i < elements.length; i++)
		{
			vs.add(elements[i]).add(separator);
		}
		return vs.deleteLast(separator.length());
	}

	public static final VarString appendArraySeperated(
		final VarString vc,
		final char separator,
		final Object... elements
	)
	{
		if(elements == null)
		{
			return vc;
		}
		for(int i = 0; i < elements.length; i++)
		{
			vc.add(elements[i]).add(separator);
		}
		return vc.deleteLast();
	}

	public static final char[] toCharArray(final CharSequence c)
	{
		if(c instanceof String)
		{
			return ((String)c).toCharArray();
		}
		if(c instanceof VarString)
		{
			return ((VarString)c).toArray();
		}
		if(c instanceof StringBuilder)
		{
			return toCharArray((StringBuilder)c);
		}
		if(c instanceof StringBuffer)
		{
			return toCharArray((StringBuffer)c);
		}

		// default case: fall back to double instantiation
		return c.toString().toCharArray();
	}

	public static final char[] toCharArray(final StringBuilder asb)
	{
		final char[] charArray;
		asb.getChars(0, asb.length(), charArray = new char[asb.length()], 0);
		return charArray;
	}

	public static final char[] toCharArray(final StringBuffer asb)
	{
		final char[] charArray;
		asb.getChars(0, asb.length(), charArray = new char[asb.length()], 0);
		return charArray;
	}

	/**
	 * Returns {@code true} if the two given character arrays have at least one character in common.
	 *
	 * @param chars1 first char array
	 * @param chars2 second char array
	 * @return {@code true} if the two given character arrays intersect each other.
	 */
	public static final boolean intersects(final char[] chars1, final char[] chars2)
	{
		for(int i = 0; i < chars1.length; i++)
		{
			for(int j = 0; j < chars2.length; j++)
			{
				if(chars1[i] == chars2[j])
				{
					return true;
				}
			}
		}
		return false;
	}

	public static final char[] append(final char c, final char[] string)
	{
		final char[] result; // sanity checks are completely done by JVM
		System.arraycopy(string, 0, result = new char[string.length + 1], 1, string.length);
		result[0] = c;
		return result;
	}

	public static final char[] append(final char[] string, final char c)
	{
		final char[] result; // sanity checks are completely done by JVM
		System.arraycopy(string, 0, result = new char[string.length + 1], 0, string.length);
		result[result.length - 1] = c;
		return result;
	}

	public static final char[] append(final char[] a, final char[] b)
	{
		final char[] result; // sanity checks are completely done by JVM
		System.arraycopy(a, 0, result = new char[a.length + b.length],        0, a.length);
		System.arraycopy(b, 0, result                                , a.length, b.length);
		return result;
	}

	private static int lcsLength(final String a, final String b)
	{
		final int lenA = a.length();
		final int lenB = b.length();
		int[] p = new int[lenB];
		int[] d = new int[lenB];
		int maxLen = 0;

		for(int i = 0; i < lenA; i++)
		{
			for(int j = 0; j < lenB; j++)
			{
				if((d[j] = a.charAt(i) != b.charAt(j) ? 0 : i == 0 || j == 0 ? 1 : p[j - 1] + 1) > maxLen)
				{
					maxLen = d[j];
				}
			}
			final int[] swap = p;
			p = d;
			d = swap;
		}
		return maxLen;
	}

	private static int lcsLength(final char[] a, final char[] b)
	{
		final int lenA = a.length;
		final int lenB = b.length;
		int[] p = new int[lenB];
		int[] d = new int[lenB];
		int maxLen = 0;

		for(int i = 0; i < lenA; i++)
		{
			for(int j = 0; j < lenB; j++)
			{
				if((d[j] = a[i] != b[j] ? 0 : i == 0 || j == 0 ? 1 : p[j - 1] + 1) > maxLen)
				{
					maxLen = d[j];
				}
			}
			final int[] swap = p;
			p = d;
			d = swap;
		}
		return maxLen;
	}

	public static final int longestCommonSubstringLength(final String a, final String b)
	{
		if(a.isEmpty() || b.isEmpty())
		{
			return 0;
		}
		// move shorter string to second position due to buffer array dependency
		return a.length() > b.length() ? lcsLength(a, b) : lcsLength(b, a);
	}

	public static final int commonSubstringLength(final char[] a, final char[] b)
	{
		if(a.length == 0 || b.length == 0)
		{
			return 0;
		}
		// move shorter string to second position due to buffer array dependency
		return a.length > b.length ? lcsLength(a, b) : lcsLength(b, a);
	}

	public static final String longestCommonSubstring(final String a, final String b)
	{
		if(a.isEmpty() || b.isEmpty())
		{
			return "";
		}

		final int lenPreA, lenPreB;
		final String[] prefixesA = new String[lenPreA = a.length() + 1];
		final String[] prefixesB = new String[lenPreB = b.length() + 1];

		for(int i = 0; i < lenPreA; i++)
		{
			prefixesA[i] = a.substring(0, i);
		}
		for(int i = 0; i < lenPreB; i++)
		{
			prefixesB[i] = b.substring(0, i);
		}

		final String[][] suffixes = new String[lenPreA][];
		String longestCommonSubstring = "";

		for(int i = 0; i < lenPreA; i++)
		{
			suffixes[i] = new String[lenPreB];
			for(int j = 0; j < lenPreB; j++)
			{
				suffixes[i][j] = "";
				for(int k1 = prefixesA[i].length() - 1, k2 = prefixesB[j].length() - 1; k1 >= 0 && k2 >= 0; k1--, k2--)
				{
					if(prefixesA[i].charAt(k1) != prefixesB[j].charAt(k2))
					{
						break;
					}
					suffixes[i][j] = String.valueOf(prefixesA[i].charAt(k1)) + suffixes[i][j];
				}
				if(suffixes[i][j].length() > longestCommonSubstring.length())
				{
					longestCommonSubstring = suffixes[i][j];
				}
			}
		}

		return longestCommonSubstring;
	}

	public static final char[] longestCommonSubstring(final char[] a, final char[] b)
	{
		if(a.length == 0 || b.length == 0)
		{
			return new char[0];
		}

		final int lenPreA, lenPreB;
		final char[][] prefixesA = new char[lenPreA = a.length + 1][];
		final char[][] prefixesB = new char[lenPreB = b.length + 1][];

		for(int i = 0; i < lenPreA; i++)
		{
			prefixesA[i] = substring(a, 0, i);
		}
		for(int i = 0; i < lenPreB; i++)
		{
			prefixesB[i] = substring(b, 0, i);
		}

		final char[][][] suffixes = new char[lenPreA][][];
		char[] longestCommonSubstring = EMPTY;

		for(int i = 0; i < lenPreA; i++)
		{
			suffixes[i] = new char[lenPreB][];
			for(int j = 0; j < lenPreB; j++)
			{
				suffixes[i][j] = EMPTY;
				for(int k1 = prefixesA[i].length - 1, k2 = prefixesB[j].length - 1; k1 >= 0 && k2 >= 0; k1--, k2--)
				{
					if(prefixesA[i][k1] != prefixesB[j][k2])
					{
						break;
					}
					suffixes[i][j] = append(prefixesA[i][k1], suffixes[i][j]);
				}
				if(suffixes[i][j].length > longestCommonSubstring.length)
				{
					longestCommonSubstring = suffixes[i][j];
				}
			}
		}

		return longestCommonSubstring;
	}

	public static final char[] substring(final char[] string, final int offset, final int bound)
	{
		if(offset == bound)
		{
			if(offset < 0 || offset >= string.length)
			{
				throw new ArrayIndexOutOfBoundsException(offset);
			}
			return new char[0];
		}

		// sanity checks are completely done by JVM
		final char[] substring;
		System.arraycopy(
			string,
			offset,
			substring = new char[bound - offset],
			0,
			substring.length
		);
		return substring;
	}

	public static final String convertUnderscoresToCamelCase(final String parameterName)
	{
		return convertUnderscoresToCamelCase(VarString.New(parameterName.length()), parameterName).toString();
	}

	public static final VarString convertUnderscoresToCamelCase(final VarString vs, final String s)
	{
		if(XChars.hasNoContent(s) || s.indexOf('_') < 0)
		{
			return vs.add(s);
		}
		final int vcStartLength = vs.length();

		final int lastIndex = s.length() - 1;
		for(int i = 0; i <= lastIndex; i++)
		{
			if(s.charAt(i) != '_')
			{
				vs.add(s.charAt(i));
				continue;
			}
			if(i == 0)
			{
				// skip all leading underscores
				continue;
			}
			if(i < lastIndex)
			{
				final char next = s.charAt(i + 1);
				if(next == '_')
				{
					continue;
				}
				if(isLowerCase(next))
				{
					if(!vs.isEmpty() && isUpperCase(vs.last()))
					{
						vs.add('_');
					}
					vs.add(toUpperCase(next));
				}
				else if(isDigit(next))
				{
					if(!vs.isEmpty() && isDigit(vs.last()))
					{
						vs.add('_');
					}
					vs.add(next);
				}
				else if(isUpperCase(next) && !vs.isEmpty() && isUpperCase(vs.last()))
				{
					vs.add('_').add(next);
				}
				else if(next != vs.last())
				{
					vs.add(toUpperCase(next));
				}
				else
				{
					vs.add('_', next);
				}
				i++;
			}
			// else drop trailing underscore
		}

		if(vcStartLength == vs.length())
		{
			// mean special case: string consisted only of underscores. Keep completely.
			vs.add(s);
		}
		return vs;
	}

	public static final int longestCommonPrefixLength(final String a, final String b)
	{
		final int len = a.length() < b.length() ? a.length() : b.length();
		for(int i = 0; i < len; i++)
		{
			if(a.charAt(i) != b.charAt(i))
			{
				return i;
			}
		}
		return len;
	}

	public static final int longestCommonSuffixLength(final String a, final String b)
	{
		final int lenA, lenB, len = (lenA = a.length()) < (lenB = b.length()) ? lenA : lenB;
		for(int i = 1; i <= len; i++)
		{
			if(a.charAt(lenA - i) != b.charAt(lenB - i))
			{
				return i - 1;
			}
		}
		return len;
	}

	public static final String longestCommonSuffix(final String a, final String b)
	{
		final int len;
		if((len = longestCommonSuffixLength(a, b)) == 0)
		{
			return "";
		}

		return a.substring(a.length() - len, a.length());
	}

	public static final String longestCommonPrefix(final String a, final String b)
	{
		final int len;
		if((len = longestCommonPrefixLength(a, b)) == 0)
		{
			return "";
		}

		return a.substring(0, len);
	}

	public static final int commonPrefixLength(final char[] a, final char[] b)
	{
		final int len = a.length < b.length ? a.length : b.length;
		for(int i = 0; i < len; i++)
		{
			if(a[i] != b[i])
			{
				return i;
			}
		}
		return len;
	}

	public static final int commonSuffixLength(final char[] a, final char[] b)
	{
		final int lenA, lenB, len = (lenA = a.length) < (lenB = b.length) ? lenA : lenB;
		for(int i = 1; i <= len; i++)
		{
			if(a[lenA - i] != b[lenB - i])
			{
				return i - 1;
			}
		}
		return len;
	}

	public static final char[] longestCommonSuffix(final char[] a, final char[] b)
	{
		final int len;
		if((len = commonSuffixLength(a, b)) == 0)
		{
			return new char[0];
		}

		final char[] prefix;
		System.arraycopy(a, a.length - len, prefix = new char[len], 0, len);
		return prefix;
	}

	public static final char[] longestCommonPrefix(final char[] a, final char[] b)
	{
		final int len;
		if((len = commonPrefixLength(a, b)) == 0)
		{
			return new char[0];
		}

		final char[] prefix;
		System.arraycopy(a, 0, prefix = new char[len], 0, len);
		return prefix;
	}

	public static final String padLeft(final String s, final int totalLength, final char paddingChar)
	{
		return VarString.New(totalLength).padLeft(s, totalLength, paddingChar).toString();
	}

	public static final String padRight(final String s, final int totalLength, final char paddingChar)
	{
		return VarString.New(totalLength).padRight(s, totalLength, paddingChar).toString();
	}

	public static final String padLeft(final String s, final int totalLength)
	{
		return VarString.New(totalLength).padLeft(s, totalLength, ' ').toString();
	}

	public static final String padRight(final String s, final int totalLength)
	{
		return VarString.New(totalLength).padRight(s, totalLength, ' ').toString();
	}

	public static final String padLeft0(final String s, final int totalLength)
	{
		return VarString.New(totalLength).padLeft(s, totalLength, '0').toString();
	}

	public static final String padRight0(final String s, final int totalLength)
	{
		return VarString.New(totalLength).padRight(s, totalLength, '0').toString();
	}

	/**
	 * Parses the char escape sequence Strings "\n" etc. (without the "") to the single char value represented by
	 * those strings.
	 *
	 * @param s the char escape sequence
	 * @return the resulting char
	 */
	public static final char parseChar(final String s)
	{
		if(s.charAt(0) != '\\' && s.length() != 1 || s.charAt(0) == '\\' && s.length() != 2)
		{
			throw new IllegalArgumentException(s); // intentionally throws for a single '\' as well!
		}
		if(s.length() == 1)
		{
			return s.charAt(0);
		}
		// based on http://docs.oracle.com/javase/tutorial/java/data/characters.html
		switch(s.charAt(1))
		{
			case 't' : return '\t';
			case 'b' : return '\b';
			case 'n' : return '\n';
			case 'r' : return '\r';
			case 'f' : return '\f';
			case '\'': return '\'';
			case '"' : return '"' ; // gets recognized optionally
			case '\\': return '\\';
			default  : throw new IllegalArgumentException(s);
		}
	}

	public static final VarString escapeChar(final VarString vc, final char c)
	{
		switch(c)
		{
			case '\t': return vc.add('\\', 't' );
			case '\b': return vc.add('\\', 'b' );
			case '\n': return vc.add('\\', 'n' );
			case '\r': return vc.add('\\', 'r' );
			case '\f': return vc.add('\\', 'f' );
			case '\'': return vc.add('\\', '\'');
			case '\\': return vc.add('\\', '\\');
			default  : return vc.append(c);
		}
	}

	public static final String escapeChar(final char c)
	{
		switch(c)
		{
			case '\t': return "\\t";
			case '\b': return "\\b";
			case '\n': return "\\n";
			case '\r': return "\\r";
			case '\f': return "\\f";
			case '\'': return "\\'";
//			case '\"': return "\\""; // does not have to be escaped, only for compatibility
			case '\\': return "\\\\";
			default  : return String.valueOf(c);
		}
	}

	public static final String createMedialCapitalsString(final String... elements)
	{
		if(elements == null)
		{
			return null;
		}
		if(elements.length == 0)
		{
			return "";
		}
		if(elements.length == 1)
		{
			return elements[0];
		}

		final VarString sb = VarString.New(512); //should normally be sufficient for most identifiers
		sb.add(elements[0]);

		for(int i = 1; i < elements.length; i++)
		{
			final String element = elements[i];
			if(element == null || element.isEmpty())
			{
				continue;
			}

			final char firstLetter = element.charAt(0);
			if(Character.isUpperCase(firstLetter))
			{
				//nothing to "camelize"
				sb.add(element.toString());
				continue;
			}

			sb.append(Character.toUpperCase(firstLetter)).add(element.substring(1));
		}
		return sb.toString();
	}

	public static final String ensureCharAtEnd(final String s, final char c)
	{
		if(s.charAt(s.length() - 1) == c)
		{
			return s;
		}
		return s + c;
	}

	public static final String toSingleLine(final String multiLineString, final String lineBreakReplacement)
	{
		return multiLineString.replaceAll("((\\r)?\\n) + ", lineBreakReplacement);
	}

	public static final String padSpace(final String s, final int chars)
	{
		final VarString sb = VarString.New(chars).add(s);
		for(int i = s.length(); i < chars; i++)
		{
			sb.add(' ');
		}
		return sb.toString();
	}

	public static final String concat(final char separator, final Object... parts)
	{
		if(parts == null)
		{
			return null;
		}

		final VarString sb = VarString.New(128);
		final int lastIndex = parts.length - 1;
		for(int i = 0; i <= lastIndex ; i++)
		{
			if(parts[i] == null)
			{
				continue;
			}
			if(sb.length() > 0)
			{
				sb.append(separator);
			}
			sb.add(parts[i]);
		}
		return sb.toString();
	}

	public static final <C extends CharSequence> C notEmpty(final C charSequence)
	{
		// implicit NPE
		if(charSequence.length() == 0)
		{
			throw new IllegalArgumentException();
		}
		return charSequence;
	}

	public static final boolean isEmpty(final CharSequence cs)
	{
		return cs == null || cs.length() == 0;
	}

	public static final void iterate(final CharSequence chars, final _charProcedure iterator)
	{
		// optimization checks
		if(chars instanceof String)
		{
			iterate((String)chars, iterator);
			return;
		}
		if(chars instanceof VarString)
		{
			((VarString)chars).iterate(iterator);
			return;
		}
		// could add JDK string builder implementations here

		// generic default algorithm. However slow that may be for implementation of chars
		try
		{
			final int length = chars.length();
			for(int i = 0; i < length; i++)
			{
				iterator.accept(chars.charAt(i));
			}
		}
		catch(final ThrowBreak b)
		{
			// abort iteration
		}
	}

	public static final void iterate(final String chars, final _charProcedure iterator)
	{
		iterate(readChars(chars), iterator);
	}

	public static final void iterate(final char[] chars, final _charProcedure iterator)
	{
		iterate(chars, 0, chars.length, iterator);
	}

	public static final void iterate(
		final char[]         chars   ,
		final int            offset  ,
		final int            length  ,
		final _charProcedure iterator
	)
	{
		try
		{
			for(int i = offset; i < length;)
			{
				iterator.accept(chars[i++]);
			}
		}
		catch(final ThrowBreak b)
		{
			// abort iteration
		}
	}



	///////////////////////////////////////////////////////////////////////////
	// toChars methods //
	////////////////////

	public static final int put(final byte value, final char[] target, final int offset)
	{
		return CharConversionIntegers.put_byte(value, target, offset);
	}

	public static final int putHexDec(final byte value, final char[] target, final int offset)
	{
		// pure algorithm method intentionally without array bounds check. Use VarString etc. for that.

		// negative values are covered implicitly via >>> operator
		// CHECKSTYLE.OFF: MagicNumber: HexDec arithmetic
		target[offset    ] = XChars.toHexadecimal(value >>> 4 & 0b1111);
		target[offset + 1] = XChars.toHexadecimal(value       & 0b1111);
		// CHECKSTYLE.ON: MagicNumber
		return offset + 2;
	}

	public static final int put(final boolean value, final char[] target, final int offset)
	{
		// pure algorithm method intentionally without array bounds check. Use VarString etc. for that.
		return value ? putTrue(target, offset) : putFalse(target, offset);
	}

	public static final int put(final short value, final char[] target, final int offset)
	{
		return CharConversionIntegers.put_short(value, target, offset);
	}

	public static final int put(final int value, final char[] target, final int offset)
	{
		return CharConversionIntegers.put_int(value, target, offset);
	}

	public static final int put(final float value, final char[] target, final int offset)
	{
		return CharConversion_float.put(value, target, offset);
	}

	public static final int put(final long value, final char[] target, final int offset)
	{
		return CharConversionIntegers.put_long(value, target, offset);
	}

	public static final int put(final double value, final char[] target, final int offset)
	{
		return CharConversion_double.put(value, target, offset);
	}

	public static final int put(final String s, final char[] target, final int offset)
	{
		// pure algorithm method intentionally without array bounds check. Use VarString etc. for that.

		s.getChars(0, s.length(), target, offset);
		return offset + s.length();
	}

	public static final int put(final char[] value, final char[] target, final int offset)
	{
		// pure algorithm method intentionally without array bounds check. Use VarString etc. for that.

		System.arraycopy(value, 0, target, offset, value.length);
		return offset + value.length;
	}

	public static final int putNull(final char[] target, final int offset)
	{
		// pure algorithm method intentionally without array bounds check. Use VarString etc. for that.

		// significantly faster than arraycopy
		// CHECKSTYLE.OFF: MagicNumber: No more constants. Those are arithmetical values. I won't replace "1" by "ONE"
		target[offset    ] = 'n';
		target[offset + 1] = 'u';
		target[offset + 2] = 'l';
		target[offset + 3] = 'l';
		// CHECKSTYLE.ON: MagicNumber

		return offset + LITERAL_LENGTH_NULL;
	}

	public static final int putTrue(final char[] target, final int offset)
	{
		// pure algorithm method intentionally without array bounds check. Use VarString etc. for that.

		// significantly faster than arraycopy
		// CHECKSTYLE.OFF: MagicNumber: No more constants. Those are arithmetical values. I won't replace "1" by "ONE"
		target[offset    ] = 't';
		target[offset + 1] = 'r';
		target[offset + 2] = 'u';
		target[offset + 3] = 'e';
		// CHECKSTYLE.ON: MagicNumber

		return offset + LITERAL_LENGTH_TRUE;
	}

	public static final int putFalse(final char[] target, final int offset)
	{
		// pure algorithm method intentionally without array bounds check. Use VarString etc. for that.

		// significantly faster than arraycopy
		// CHECKSTYLE.OFF: MagicNumber: No more constants. Those are arithmetical values. I won't replace "1" by "ONE"
		target[offset    ] = 'f';
		target[offset + 1] = 'a';
		target[offset + 2] = 'l';
		target[offset + 3] = 's';
		target[offset + 4] = 'e';
		// CHECKSTYLE.ON: MagicNumber

		return offset + LITERAL_LENGTH_FALSE;
	}

	public static final String String(final byte value)
	{
		// performance-optimized version with minimal instantiating and pointer indirection
		final char[] chars;
		return new String(
			chars = new char[MAX_CHAR_COUNT_byte],
			0,
			CharConversionIntegers.put_byte(value, chars, 0)
		);
	}

	public static final String String(final boolean value)
	{
		return Boolean.toString(value); // just for completeness
	}

	public static final String String(final short value)
	{
		// performance-optimized version with minimal instantiating and pointer indirection
		final char[] chars;
		return new String(
			chars = new char[MAX_CHAR_COUNT_short],
			0,
			CharConversionIntegers.put_short(value, chars, 0)
		);
	}

	public static final String String(final char value)
	{
		return String.valueOf(value); // just for completeness
	}

	public static final String String(final int value)
	{
		// performance-optimized version with minimal instantiating and pointer indirection
		final char[] chars;
		return new String(
			chars = new char[MAX_CHAR_COUNT_int],
			0,
			CharConversionIntegers.put_int(value, chars, 0)
		);
	}

	public static final String String(final float value)
	{
		return String((double)value);
	}

	public static final String String(final long value)
	{
		final char[] chars;
		return new String(
			chars = new char[MAX_CHAR_COUNT_long],
			0,
			CharConversionIntegers.put_long(value, chars, 0)
		);
	}

	public static final String String(final double value)
	{
		final char[] chars;
		return new String(
			chars = new char[MAX_CHAR_COUNT_double],
			0,
			CharConversion_double.put(value, chars, 0)
		);
	}
	
	public static final String String(final char[] chars)
	{
		return new String(chars);
	}
	
	public static final String String(
		final char[] chars  ,
		final int    offset ,
		final int    length
	)
	{
		return String(chars, offset, length);
	}
	
	public static final String String(final byte[] chars)
	{
		return String(chars, standardCharset());
	}
	
	public static final String String(final byte[] chars, final Charset charset)
	{
		return String(chars, 0, chars.length, charset);
	}
	
	public static final String String(
		final byte[] chars  ,
		final int    offset ,
		final int    length
	)
	{
		return String(chars, offset, length, standardCharset());
	}
	
	public static final String String(
		final byte[]  chars  ,
		final int     offset ,
		final int     length ,
		final Charset charset
	)
	{
		return new String(chars, offset, length, charset);
	}
	
	public static final String String(final ByteBuffer chars)
	{
		return String(chars, standardCharset());
	}
	
	public static final String String(final ByteBuffer chars, final Charset charset)
	{
		return new String(XMemory.toArray(chars), charset);
	}
	
	public static final String String(
		final ByteBuffer chars  ,
		final int        offset ,
		final int        length
	)
	{
		return String(chars, offset, length, standardCharset());
	}
	
	public static final String String(
		final ByteBuffer chars  ,
		final int        offset ,
		final int        length ,
		final Charset    charset
	)
	{
		return new String(XMemory.toArray(chars, offset, length), charset);
	}


	public static final StringBuilder StringBuilder(final VarString vs)
	{
		return new StringBuilder(vs.size).append(vs.data, 0, vs.size);
	}

	public static final StringBuilder StringBuilder(final VarString vs, final int offset, final int length)
	{
		vs.validateRange(offset, length);
		return new StringBuilder(length).append(vs.data, offset, length);
	}

	public static final StringBuffer StringBuffer(final VarString vs)
	{
		return new StringBuffer(vs.size).append(vs.data, 0, vs.size);
	}

	public static final StringBuffer StringBuffer(final VarString vs, final int offset, final int length)
	{
		vs.validateRange(offset, length);
		return new StringBuffer(length).append(vs.data, offset, length);
	}

	public static final char[] readChars(final String s)
	{
		/*
		 * In ages past, this method called low-level mechanisms to access the string's internal char[] array
		 * in a safe read-only fashion to process the chars efficiently without the need to
		 * copy the complete string content just to read it.
		 */
		return s.toCharArray();
	}


	/**
	 * This method rebuilds the {@link String} hashing algorithm.
	 * <p>
	 * Returns a hash code for the passed character range. The hash code is computed as
	 * <blockquote><pre>
	 * s[0]*31^(n - 1) + s[1]*31^(n-2) + ... + s[n - 1]
	 * </pre></blockquote>
	 * using {@code int} arithmetic, where {@code s[i]} is the
	 * <i>i</i>th character counted from the given offset, {@code n} is the length
	 * and {@code ^} indicates exponentiation.
	 * The hash value of an empty range is zero.
	 *
	 * @see String#hashCode()
	 *
	 * @param chars the character array
	 * @param offset the start offset
	 * @param length the length
	 * @return a hash code value for this object.
	 */
	public static final int hashCode(final char[] chars, final int offset, final int length)
	{
		validateRange(chars, offset, length);
		return internalHashCode(chars, offset, length);
	}


	static final int internalHashCode(final char[] chars, final int offset, final int length)
	{
		if(length == 0)
		{
			return 0;
		}

		int hashCode = 0;
		final int bound = offset + length;
		for(int i = offset; i < bound; i++)
		{
			// CHECKSTYLE.OFF: MagicNumber: copied from JDK hashcode methods
			hashCode = 31 * hashCode + chars[i];
			// CHECKSTYLE.ON: MagicNumber
		}

		return hashCode;
	}

	/**
	 * High-performance implementation of the very common case to split a string by a single character
	 * and trim all elements.
	 *
	 * @see #trimToString(char[], int, int)
	 *
	 * @param input the String to split
	 * @param separator the separator char to split on
	 * @param collector the result collector
	 * @param <C> the collector type
	 * @return the collector
	 */
	public static final <C extends Consumer<String>> C splitAndTrimToStrings(
		final String input    ,
		final char   separator,
		final C      collector
	)
	{
		return splitAndTrimToStrings(readChars(input), separator, collector);
	}

	/**
	 * High-performance implementation of the very common case to split a character sequence by a single character
	 * and trim all elements.
	 *
	 * @see #trimToString(char[], int, int)
	 *
	 * @param input the char array to split
	 * @param separator the separator char to split on
	 * @param collector the result collector
	 * @param <C> the collector type
	 * @return the collector
	 */
	public static final <C extends Consumer<String>> C splitAndTrimToStrings(
		final char[] input    ,
		final char   separator,
		final C      collector
	)
	{
		int lowerIndex = 0;
		for(int i = 0; i < input.length; i++)
		{
			if(input[i] == separator)
			{
				collector.accept(trimToString(input, lowerIndex, i - lowerIndex));
				lowerIndex = i + 1;
			}
		}

		// trailing empty element special case
		if(lowerIndex <= input.length)
		{
			collector.accept(trimToString(input, lowerIndex, input.length - lowerIndex));
		}

		return collector;
	}
	
	public static String[] splitSimple(final String s, final String separator)
	{
		if(s.length() > shortStringLength())
		{
			// one-pass processing, but requires the detour of allocating a collection and copying stuff around.
			return splitSimple(s, separator, BulkList.New()).toArray(String.class);
		}
		
		final StringTokenizer pathTokenizer = new StringTokenizer(s, separator);

		// the StringTokenizer discards leading separators. This is the manual workaround
		final boolean startWithSeparator = s.startsWith(separator);
		final int     swsValue           = startWithSeparator ? 1 : 0;
		
		// quick token counting (two-pass) for short strings to spare the collection allocation detour.
		final int tokenCount = pathTokenizer.countTokens() + swsValue;
		final String[] pathParts = new String[tokenCount];
		if(startWithSeparator)
		{
			pathParts[0] = "";
		}

		for(int i = swsValue; pathTokenizer.hasMoreTokens(); i++)
		{
			pathParts[i] = pathTokenizer.nextToken();
		}
		
		return pathParts;
	}
	
	public static <C extends Consumer<? super String>> C splitSimple(
		final String s        ,
		final String separator,
		final C      collector
	)
	{
		final StringTokenizer pathTokenizer = new StringTokenizer(s, separator);
		
		// the StringTokenizer discards leading separators. This is the manual workaround
		if(s.startsWith(separator))
		{
			collector.accept("");
		}
		
		while(pathTokenizer.hasMoreTokens())
		{
			final String token = pathTokenizer.nextToken();
			collector.accept(token);
		}
		
		return collector;
	}

	/**
	 * Creates a {@link String} instance with trimmed content directly from a character sequence without
	 * unnecessary intermediate instances.
	 *
	 * @param input the source char array to trim
	 * @param lowerOffset the start offset
	 * @param length the length
	 * @return the trimmed String
	 */
	public static final String trimToString(final char[] input, final int lowerOffset, final int length)
	{
		if(length <= 0)
		{
			if(length < 0)
			{
				throw new one.microstream.meta.NotImplementedYetError(); // FIXME XChars#trimToString() negative length
			}
			return ""; // prefer efficiency over referential uniqueness
		}

		final int bound = lowerOffset + length;
		int low = lowerOffset;
		while(low < bound && XChars.isWhitespace(input[low]))
		{
			low++;
		}

		if(low >= lowerOffset + length)
		{
			return ""; // prefer efficiency over referential uniqueness
		}

		int upper = lowerOffset + length - 1;
		while(XChars.isWhitespace(input[upper]))
		{
			upper--;
		}

		return new String(input, low, upper - low + 1);
	}
	
	
	/**
	 * Calls {@link String#trim()} on a non-null argument, returns {@code null} otherwise.
	 * (this is nothing but a static {@link String#trim()})
	 * 
	 * @param s the {@link String} instance to be trimmed, potentially {@code null}.
	 * @return a potentially {@code null} trimmed {@link String} instance.
	 */
	public static String trim(final String s)
	{
		return s == null
			? null
			: s.trim()
		;
	}

	public static final int[] parseTo_intArray(final String... intStrings)
	{
		if(intStrings == null)
		{
			return null; // pass through
		}

		final int[] ints = new int[intStrings.length];
		for(int i = 0; i < intStrings.length; i++)
		{
			ints[i] = Integer.parseInt(intStrings[i]);
		}

		return ints;
	}

	public static final Integer[] parseToIntegerArray(final String... intStrings)
	{
		if(intStrings == null)
		{
			return null; // pass through
		}

		final Integer[] ints = new Integer[intStrings.length];
		for(int i = 0; i < intStrings.length; i++)
		{
			ints[i] = Integer.parseInt(intStrings[i]);
		}

		return ints;
	}

	public static final VarString assembleNewLinedTabbed(
		final VarString       vs      ,
		final CharSequence... elements
	)
	{
		vs.lf().add(elements[0]);
		for(int i = 1; i < elements.length; i++)
		{
			vs.tab().add(elements[i]);
		}
		
		return vs;
	}
	
	public static final VarString assembleSeparated(
		final VarString       vs       ,
		final char            separator,
		final CharSequence... elements
	)
	{
		if(XArrays.hasNoContent(elements))
		{
			return vs;
		}
		
		vs.add(elements[0]);
		for(int i = 1; i < elements.length; i++)
		{
			vs.add(separator).add(elements[i]);
		}
		
		return vs;
	}

	public static final byte parse_byteDecimal(final char[] input)
	{
		return uncheckedParse_byteDecimal(input, 0, input.length);
	}

	public static final short parse_shortDecimal(final char[] input)
	{
		return uncheckedParse_shortDecimal(input, 0, input.length);
	}

	public static final int parse_intLiteral(final char[] input)
	{
		return uncheckedParse_intLiteral(input, 0, input.length);
	}

	public static final long parse_longDecimal(final char[] input)
	{
		return uncheckedParse_longLiteral(input, 0, input.length);
	}

	public static final byte parse_byteDecimal(final char[] input, final int offset, final int length)
	{
		validateRange(input, offset, length);
		return uncheckedParse_byteDecimal(input, offset, length);
	}

	public static final short parse_shortDecimal(final char[] input, final int offset, final int length)
	{
		validateRange(input, offset, length);
		return uncheckedParse_shortDecimal(input, offset, length);
	}

	public static final int parse_intLiteral(final char[] input, final int offset, final int length)
	{
		validateRange(input, offset, length);
		return uncheckedParse_intLiteral(input, offset, length);
	}

	public static final long parse_longDecimal(final char[] input, final int offset, final int length)
	{
		validateRange(input, offset, length);
		return uncheckedParse_longLiteral(input, offset, length);
	}

	public static final float parse_float(final char[] input, final int offset, final int length)
	{
		validateRange(input, offset, length);

		// (12.10.2014 TM)TODO: implement efficient float parser
		return Float.parseFloat(String.valueOf(input, offset, length));
	}

	public static final double parse_double(final char[] input, final int offset, final int length)
	{
		validateRange(input, offset, length);

		// (12.10.2014 TM)TODO: implement efficient double parser
		return Double.parseDouble(String.valueOf(input, offset, length));
	}

	public static final byte uncheckedParse_byteDecimal(final char[] input, final int offset, final int length)
	{
		// lots of special case checking, but only executed for max length literals, so hardly relevant performancewise.
		if(length >= SIGNLESS_MAX_CHAR_COUNT_byte)
		{
			checkNumberRanges(input, offset, length, CHARS_MIN_VALUE_byte, CHARS_MAX_VALUE_byte);
		}

		try
		{
			// checks above guarantee that the parsed long value is in range
			return (byte)internalParse_longLiteral(input, offset, length);
		}
		catch(final NumberFormatException e)
		{
			// Use Exception with indication of initial value that failed parsing
			throw new NumberFormatException(String.copyValueOf(input, offset, length));
		}
	}

	public static final short uncheckedParse_shortDecimal(final char[] input, final int offset, final int length)
	{
		// lots of special case checking, but only executed for max length literals, so hardly relevant performancewise.
		if(length >= SIGNLESS_MAX_CHAR_COUNT_short)
		{
			checkNumberRanges(input, offset, length, CHARS_MIN_VALUE_short, CHARS_MAX_VALUE_short);
		}

		try
		{
			// checks above guarantee that the parsed long value is in range
			return (short)internalParse_longLiteral(input, offset, length);
		}
		catch(final NumberFormatException e)
		{
			// Use Exception with indication of initial value that failed parsing
			throw new NumberFormatException(String.copyValueOf(input, offset, length));
		}
	}

	public static final int uncheckedParse_intLiteral(final char[] input, final int offset, final int length)
	{
		// lots of special case checking, but only executed for max length literals, so hardly relevant performancewise.
		if(length >= SIGNLESS_MAX_CHAR_COUNT_int)
		{
			checkNumberRanges(input, offset, length, CHARS_MIN_VALUE_int, CHARS_MAX_VALUE_int);
		}

		try
		{
			// checks above guarantee that the parsed long value is in range
			return (int)internalParse_longLiteral(input, offset, length);
		}
		catch(final NumberFormatException e)
		{
			// Use Exception with indication of initial value that failed parsing
			throw new NumberFormatException(String.copyValueOf(input, offset, length));
		}
	}

	public static final long uncheckedParse_longLiteral(final char[] input, final int offset, final int length)
	{
		// lots of special case checking, but only executed for max length literals, so hardly relevant performancewise.
		if(length >= SIGNLESS_MAX_CHAR_COUNT_long)
		{
			checkNumberRanges(input, offset, length, CHARS_MIN_VALUE_long, CHARS_MAX_VALUE_long);
		}

		try
		{
			return internalParse_longLiteral(input, offset, length);
		}
		catch(final NumberFormatException e)
		{
			// Use Exception with indication of initial value that failed parsing
			throw new NumberFormatException(String.copyValueOf(input, offset, length));
		}
	}

	private static void checkNumberRanges(
		final char[] input          ,
		final int    offset         ,
		final int    length         ,
		final char[] minValueLiteral,
		final char[] maxValueLiteral
	)
	{
		final int maxCharCount         = minValueLiteral.length;
		final int signlessMaxCharCount = maxCharCount - 1;

		// tricky special case: " + 0000000127" is a valid byte literal despite being very long.
		int pos = offset;
		if(length > maxCharCount)
		{
			if(input[pos] == '-' || input[pos] == '+')
			{
				pos++;
			}
			while(input[pos] == '0')
			{
				pos++;
			}
		}

		// oh those special cases :-[
		final int len = length - (pos - offset) - (input[pos] == '-' || input[pos] == '+' ? 1 : 0);

		// if there are more actual value digits than the possible maximum, the literal must be out of range.
		if(len > signlessMaxCharCount)
		{
			throw new NumberRangeException(String.copyValueOf(input, pos, len));
		}

		// if there are less actual value digits than the possible maximum, the literal can't be out of range.
		if(len < signlessMaxCharCount)
		{
			return;
		}

		// if there are as much actual value digits than the possible maximum, the literal must be checked in detail.
		if(input[offset] == '-')
		{
			if(len == signlessMaxCharCount && isNumericalLessThan(minValueLiteral, 1, input, pos, len))
			{
				throw new NumberRangeException(String.copyValueOf(input, pos, len));
			}
			// abort
		}
		else if(input[offset] == '+')
		{
			if(len == signlessMaxCharCount && isNumericalLessThan(maxValueLiteral, 0, input, pos, len))
			{
				throw new NumberRangeException(String.copyValueOf(input, pos, len));
			}
			// abort
		}
		else if(len == signlessMaxCharCount && isNumericalLessThan(maxValueLiteral, 0, input, pos, len))
		{
			throw new NumberRangeException(String.copyValueOf(input, pos, len));
		}
	}

	/**
	 * Special case higher performance implementation of decimal integer literal parsing.
	 * Because as usual, the JDK implementation strategies are not acceptable when dealing with non-trivial
	 * amounts of data.
	 * Properly executed performance tests (large loop sizes, averages, nanosecond precision, etc.) showed
	 * that this algorithms is more than twice as fast as the one used in JDK
	 * (average of ~33µs vs ~75µs for long literals on same machine with measuring overhead of ~1.5µs)
	 *
	 * @param input the source char array
	 * @param offset the start offset
	 * @param length the length
	 * @return the parsed long value
	 */
	public static final long internalParse_longLiteral(final char[] input, final int offset, final int length)
	{
		// special cased trivial case and invalid single character cases (like letter or sole '+')
		if(length == 1)
		{
			return to_int(input[offset]);
		}

		int i;
		final int bound = (i = offset) + length;

		// handle sign
		final boolean negative;
		if((negative = input[i] == '-') || input[i] == '+')
		{
			/*
			 * Special case handling of asymmetric min value
			 * Note that the char array comparison is done in only very rare cases and aborts quickly on mismatch.
			 */
			if(length == CHARS_MIN_VALUE_long.length && uncheckedEquals(input, offset, CHARS_MIN_VALUE_long, 0, length))
			{
				return Long.MIN_VALUE;
			}
			i++;
		}

		// actual value parsing (quite trivial and efficient if done properly)
		long value = 0;
		while(i < bound)
		{
			value = value * DECIMAL_BASE + to_int(input[i++]);
		}

		// adjust sign and return resulting value
		return negative ? -value : value;
	}

	/**
	 * Two number literals of equal length can efficiently compared to each other by comparing the digits
	 * from most significant to less significant place. The first pair of digits determines the result.
	 * As every decimal digit has a 90% chance of being differen to another decimal digit when comparing random
	 * numbers, this algorithm terminates very quickly in the common case. The worst case (equal value literals) is
	 * a usual full equality check to the last digit.
	 *
	 */
	static final boolean isNumericalLessThan(
		final char[] chars1 ,
		final int    offset1,
		final char[] chars2 ,
		final int    offset2,
		final int    length
	)
	{
		for(int i = 0; i < length; i++)
		{
			if(chars1[offset1 + i] != chars2[offset2 + i])
			{
				// not equal must either be less or greater than
				if(chars1[offset1 + i] < chars2[offset2 + i])
				{
					return true;
				}

				// greater it is, return false
				return false;
			}
		}

		// completely equal, so not less, hence return false
		return false;
	}

	public static final int to_int(final char digit)
	{
		if(digit < DIGIT_LOWER_INDEX || digit >= DIGIT_UPPER_BOUND)
		{
			throw new NumberFormatException(String.valueOf(digit));
		}
		return digit - DIGIT_LOWER_INDEX;
	}

	public static final char[][] toArrays(final XGettingSequence<String> strings)
	{
		final char[][] arrays = new char[X.checkArrayRange(strings.size())][];
		strings.iterateIndexed((e, i) -> arrays[X.checkArrayRange(i)] = e.toCharArray());
		return arrays;
	}



	public static final boolean contains(final String[] strings, final String subject)
	{
		notNull(subject);
		for(final String string : strings)
		{
			if(subject.equals(string))
			{
				return true;
			}
		}

		return false;
	}

	public static final boolean contains(final String s, final char c)
	{
		return s.indexOf(c) >= 0;
	}

	public static final boolean contains(
		final char[] subject      ,
		final int    subjectOffset,
		final int    subjectLength,
		final char[] sample       ,
		final int    sampleOffset ,
		final int    sampleLength
	)
	{
		final int startBound = subjectOffset + subjectLength - sampleLength;

		for(int i = subjectOffset; i < startBound; i++)
		{
			if(equals(subject, i, sample, sampleOffset, sampleLength))
			{
				return true;
			}
		}
		return false;
	}
	
	public static final boolean contains(
		final char[] data,
		final char   c
	)
	{
		return uncheckedContains(data, 0, data.length, c);
	}
	
	public static final boolean contains(
		final char[] data      ,
		final int    dataOffset,
		final int    dataLength,
		final char   c
	)
	{
		XArrays.checkBounds(data.length, dataOffset, dataOffset + XMath.notNegative(dataLength));
		
		return uncheckedContains(data, dataLength, dataOffset, c);
	}
	
	static final boolean uncheckedContains(
		final char[] data      ,
		final int    dataOffset,
		final int    dataLength,
		final char   c
	)
	{
		final int dataBound = dataOffset + dataLength;
		for(int i = dataOffset; i < dataBound; i++)
		{
			if(data[i] == c)
			{
				return true;
			}
		}
		
		return false;
	}

	/**
	 * Utility method that replicates the JVM's intrinsic system string as defined in {@link Object#toString()}
	 * 
	 * @param instance the instance whose system string shall be generated.
	 * @return the system string for the passed instance.
	 */
	public static String systemString(final Object instance)
	{
		return instance == null
			? null
			: instance.getClass().getName() + '@' + Integer.toHexString(System.identityHashCode(instance))
		;
	}
	
	public static VarString addSystemString(final Object instance, final VarString vs)
	{
		return instance == null
			? vs.addNull()
			: vs
				.add(instance.getClass().getName())
				.add('@')
				.add(Integer.toHexString(System.identityHashCode(instance)))
		;
	}

	public static String nonNullString(final Object object)
	{
		return object == null ? "" : object.toString();
	}

	/**
	 * Returns {@code value.toString()} if the passed value is not {@literal null}, otherwise {@literal null}.
	 * <p>
	 * Note that this is a different behavior than {@link String#valueOf(Object)} has, as the latter returns
	 * the string {@code "null"} for a passed {@literal null} reference.
	 * <p>
	 * The behavior of this method is needed for example for converting values in a generic data structure
	 * (e.g. a Object[] array) to string values but have the actual values, including {@literal null}
	 *  (information about a missing value), maintained.
	 *
	 * @param value the value to be projected to its string representation if not null.
	 * @return a string representation of an actual passed value or a transient {@literal null}.
	 *
	 * @see Object#toString()
	 * @see String#valueOf(Object)
	 */
	public static String valueString(final Object value)
	{
		return value == null ? null : value.toString();
	}
	
	public static String trimEmptyToNull(final String s)
	{
		// if the string is null in the first place, null is returned.
		if(s == null)
		{
			return null;
		}
		
		// if the string contains only one non-whitespace, a trimmed string (potentially itself) is returned
		final int length = s.length();
		for(int i = 0; i < length; i++)
		{
			if(s.charAt(i) > ' ')
			{
				return s.trim();
			}
		}
		
		// string contains solely whitespaces, hence return null
		return null;
	}
	
	/**
	 * Assembles the passed instance "cautiously" in the sense that only types recognizable via
	 * {@link XTypes#isValueType(Object)} get assembled using their {@link Object#toString()} method,
	 * while all others are assembled using {@link #systemString(Object)}
	 * (which behaves identical to the actual implementation of {@link Object#toString()}).
	 * 
	 * @param vs the {@link VarString} instance to hold the assembled string.
	 * @param object the instance to be assembled "cautiously" as described above.
	 * @return the passed vs instance (method-chaining viable).
	 * 
	 * @see VarString#add(Object, java.util.function.BiConsumer)
	 * @see XTypes#isValueType(Object)
	 * @see Object#toString()
	 * @see XChars#systemString(Object)
	 */
	public static final VarString assembleCautiously(final VarString vs, final Object object)
	{
		return vs.add(XTypes.isValueType(object)
			? object.toString()
			: XChars.systemString(object)
		);
	}
	
	public static final boolean applies(final CharSequence chars, final _charPredicate predicate)
	{
		return applies(chars, 0, predicate);
	}
	
	public static final boolean applies(
		final CharSequence   chars     ,
		final int            startIndex,
		final _charPredicate predicate
	)
	{
		final int length = chars.length();
		if(length == 0)
		{
			// must check for the special case of no entries (predicate cannot apply).
			return false;
		}
		
		for(int i = startIndex; i < length; i++)
		{
			if(!predicate.test(chars.charAt(i)))
			{
				return false;
			}
		}
		
		return true;
	}
	
	public static boolean isDigit(final char c)
	{
		return c >= '0' && c <= '9';
	}
	
	public static char randomReadable_char()
	{
		return (char) (33 + XMath.random(94));
	}
	
	
	static final class CodePointCounter implements IntConsumer
	{
		int codePoint;
		int count;

		CodePointCounter(final int codePoint)
		{
			super();
			this.codePoint = codePoint;
		}

		@Override
		public void accept(final int value)
		{
			if(value == this.codePoint)
			{
				this.count++;
			}
		}
		
		public final int yield()
		{
			return this.count;
		}
		
	}
	
	public static int countCharacter(final String data, final char c)
	{
		return countCodePoint(data, c);
	}
	
	public static int countCodePoint(final String data, final int codePoint)
	{
		final CodePointCounter counter = new CodePointCounter(codePoint);
		final IntStream codePoints = data.codePoints();
		codePoints.forEach(counter);
		
		return counter.yield();
	}
	
	
	public static String mathRangeIncInc(final long minimum, final long maximum)
	{
		return mathRangeIncInc(
			Long.toString(minimum),
			Long.toString(maximum)
		);
	}
	
	public static String mathRangeIncExc(final long minimum, final long upperBound)
	{
		return mathRangeIncExc(
			Long.toString(minimum),
			Long.toString(upperBound)
		);
	}
	
	public static String mathRangeExcInc(final long lowerBound, final long maximum)
	{
		return mathRangeExcInc(
			Long.toString(lowerBound),
			Long.toString(maximum)
		);
	}
	
	public static String mathRangeExcExc(final long lowerBound, final long upperBound)
	{
		return mathRangeExcExc(
			Long.toString(lowerBound),
			Long.toString(upperBound)
		);
	}
	
	public static String mathRangeIncInc(final double minimum, final double maximum)
	{
		return mathRangeIncInc(
			Double.toString(minimum),
			Double.toString(maximum)
		);
	}
	
	public static String mathRangeIncExc(final double minimum, final double upperBound)
	{
		return mathRangeIncExc(
			Double.toString(minimum),
			Double.toString(upperBound)
		);
	}
	
	public static String mathRangeExcInc(final double lowerBound, final double maximum)
	{
		return mathRangeExcInc(
			Double.toString(lowerBound),
			Double.toString(maximum)
		);
	}
	
	public static String mathRangeExcExc(final double lowerBound, final double upperBound)
	{
		return mathRangeExcExc(
			Double.toString(lowerBound),
			Double.toString(upperBound)
		);
	}
	
	public static String mathRangeIncInc(final String minimum, final String maximum)
	{
		return "[" + minimum + "; " + maximum + "]";
	}
	
	public static String mathRangeIncExc(final String minimum, final String upperBound)
	{
		return "[" + minimum + "; " + upperBound + "[";
	}
	
	public static String mathRangeExcInc(final String lowerBound, final String maximum)
	{
		return "]" + lowerBound + "; " + maximum + "]";
	}
	
	public static String mathRangeExcExc(final String lowerBound, final String upperBound)
	{
		return "]" + lowerBound + "; " + upperBound + "[";
	}
		
	
	public static String decapitalize(final String name)
	{
        if(name == null || name.length() == 0)
        {
            return name;
        }
        if(name.length() > 1
        && Character.isUpperCase(name.charAt(1))
        && Character.isUpperCase(name.charAt(0))
        )
        {
            return name;
        }
        final char chars[] = name.toCharArray();
        chars[0] = Character.toLowerCase(chars[0]);
        return new String(chars);
    }

	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	/**
	 * Dummy constructor to prevent instantiation of this static-only utility class.
	 * 
	 * @throws UnsupportedOperationException when called
	 */
	private XChars()
	{
		// static only
		throw new UnsupportedOperationException();
	}
	
}

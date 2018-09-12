package net.jadoth.chars;

import net.jadoth.low.XVM;


public final class MemoryCharConversionUTF8
{
	// CHECKSTYLE.OFF: MagicNumber: Arithmetics are better readable with direct values
	
	static final int
		THRESHOLD_1_BYTE = 0x80 , // 128
		THRESHOLD_2_BYTE = 0x800, // 2048
		MAX_CHAR_LENGTH  = 3
	;

	public static final int maxCharacterLength()
	{
		return MAX_CHAR_LENGTH;
	}

	public static final boolean isSingleByte(final char c)
	{
		return c < THRESHOLD_1_BYTE;
	}

	public static final byte toSingleByte(final char c)
	{
		if(isSingleByte(c))
		{
			return (byte)c;
		}
		throw new RuntimeException("Not a simple UTF-8 character: " + c); // (20.10.2014 TM)EXCP: proper exception
	}

	public static final int utf8Length(final char c)
	{
		return isSingleByte(c) ? 1 : c < THRESHOLD_2_BYTE ? 2 : 3;
	}

	public static final int utf8Length(final char... chars)
	{
		int length = 0;

		for(final char c : chars)
		{
			length = length + utf8Length(c);
		}

		return length;
	}

	public static final byte[] toUTF8(final char... chars)
	{
		/* two-pass is necessary to determine the exact length.
		 * If a growing buffer would be used instead, all bytes would have to be copied several times over
		 * instead of just one additional chars scan.
		 */
		final byte[] array = new byte[utf8Length(chars)];

		int a = 0;
		for(final char c : chars)
		{
			if(isSingleByte(c))
			{
				array[a++] = (byte)c;
			}
			else if(c < THRESHOLD_2_BYTE)
			{
				array[a    ] = (byte)(0xC0 | c >> 6       );
				array[a + 1] = (byte)(0x80 | c      & 0x3F);
				a += 2;
			}
			else
			{
				array[a    ] = (byte)(0xE0 | c >> 12       );
				array[a + 1] = (byte)(0x80 | c >>  6 & 0x3F);
				array[a + 2] = (byte)(0x80 | c       & 0x3F);
				a += 3;
			}
		}


		return array;
	}

	public static final long writeUTF8(final long address, final char c)
	{
		if(isSingleByte(c))
		{
			// simple case: single byte character with a value of < 128 (or 0x80)
			XVM.set_byte(address, (byte)c);
			return address + 1;
		}
		else if(c < THRESHOLD_2_BYTE)
		{
			// intermediate case: double byte character with a value of [128;2048[ (or [0x80;0x800[)
			XVM.set_byte(address    , (byte)(0xC0 | c >> 6       ));
			XVM.set_byte(address + 1, (byte)(0x80 | c      & 0x3F));
			return address + 2;
		}
		/* would actually have to check for Surrogates here, but, well, surrogate hacking stuff...
		 * If 65K symbols are not enough to cover your language, you are definitely doing something wrong.
		 */
		else
		{
			// all other characters get encoded as 3 bytes.
			XVM.set_byte(address    , (byte)(0xE0 | c >> 12       ));
			XVM.set_byte(address + 1, (byte)(0x80 | c >>  6 & 0x3F));
			XVM.set_byte(address + 2, (byte)(0x80 | c       & 0x3F));
			return address + 3;
		}
	}

	public static final long writeUTF8(final long address, final char... chars)
	{
		long a = address;

		for(final char c : chars)
		{
			a = writeUTF8(a, c);
		}

		return a;
	}

	public static final long writeUTF8(final long address, final char[] chars, final int offset, final int length)
	{
		long a = address;

		final int bound = offset + length;
		for(int i = offset; i < bound; i++)
		{
			a = writeUTF8(a, chars[i]);
		}

		return a;
	}

	public static final long writeUTF8(final long address, final String string)
	{
		// avoid copying potentially huge amounts of data repeatedly like the last noob.
		return writeUTF8(address, XVM.accessChars(string));
	}

	public static final long writeUTF8(final long address, final VarString vs)
	{
		return writeUTF8(address, vs.data, 0, vs.size);
	}



	private MemoryCharConversionUTF8()
	{
		// static only
		throw new UnsupportedOperationException();
	}
	
	// CHECKSTYLE.ON: MagicNumber

}

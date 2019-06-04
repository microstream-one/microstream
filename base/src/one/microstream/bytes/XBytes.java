package one.microstream.bytes;

import java.nio.ByteOrder;

public final class XBytes
{
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////

	/**
	 * Parses a {@link String} instance to a {@link ByteOrder} instance according to {@code ByteOrder#toString()}
	 * or throws an {@link IllegalArgumentException} if the passed string does not match exactely one of the
	 * {@link ByteOrder} constant instances' string representation.
	 *
	 * @param byteOrder the string representing the {@link ByteOrder} instance to be parsed.
	 * @return the recognized {@link ByteOrder}
	 * @throws IllegalArgumentException if the string can't be recognized as a {@link ByteOrder} constant instance.
	 * @see ByteOrder#toString()
	 */
	// missing in JDK, omfg.
	public static final ByteOrder parseByteOrder(final String byteOrder) throws IllegalArgumentException
	{
		if(ByteOrder.LITTLE_ENDIAN.toString().equals(byteOrder))
		{
			return ByteOrder.LITTLE_ENDIAN;
		}
		else if(ByteOrder.BIG_ENDIAN.toString().equals(byteOrder))
		{
			return ByteOrder.BIG_ENDIAN;
		}
		throw new IllegalArgumentException();
	}

	

	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	/**
	 * Dummy constructor to prevent instantiation of this static-only utility class.
	 * 
	 * @throws UnsupportedOperationException
	 */
	private XBytes()
	{
		// static only
		throw new UnsupportedOperationException();
	}
}

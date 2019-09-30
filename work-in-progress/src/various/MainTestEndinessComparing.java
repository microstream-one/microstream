package various;


import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import one.microstream.chars.VarString;

public class MainTestEndinessComparing
{
	private static final ByteBuffer bb = ByteBuffer.allocate(8);


	private static final long flip(final long value)
	{
		bb.order(ByteOrder.LITTLE_ENDIAN);
		bb.putLong(value);
		bb.order(ByteOrder.BIG_ENDIAN);
		bb.flip();
		return bb.getLong(0);
	}

	private static final void printLittleBigValue(final long value)
	{
		final long flipped = flip(value);
		System.out.println("Input  decimal: "+VarString.New().padLeft(Long.toString(value)  , 20, ' '));
		System.out.println("Output decimal: "+VarString.New().padLeft(Long.toString(flipped), 20, ' '));
		System.out.println();
		System.out.println("Output hexdec : "+VarString.New().padLeft(Long.toHexString(flipped).toUpperCase(), 16, '0'));
		System.out.println("Input  hexdec : "+VarString.New().padLeft(Long.toHexString(value  ).toUpperCase(), 16, '0'));
		System.out.println();
		System.out.println("Output binary : "+VarString.New().padLeft(Long.toBinaryString(flipped), 64, '0'));
		System.out.println("Input  binary : "+VarString.New().padLeft(Long.toBinaryString(value  ), 64, '0'));
		System.out.println("--------------------------------");
	}

	public static void main(final String[] args)
	{
		printLittleBigValue(Long.MIN_VALUE);
		printLittleBigValue(Long.MAX_VALUE);
		printLittleBigValue(0xFF00_0000_0000_0000L);
		printLittleBigValue(Long.MAX_VALUE/2);
		printLittleBigValue(Long.MAX_VALUE/2 + 1);
		printLittleBigValue(Long.MAX_VALUE/2 - 1);
		printLittleBigValue(0x0F0F_0F0F_0F0F_0F0FL);
	}
}

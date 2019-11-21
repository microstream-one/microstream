package one.microstream.collections;

import one.microstream.chars.VarString;

public class MainTestSetPrimitiveInBytes
{
	public static void main(final String[] args)
	{
		test((short)27548);
		test((short)567);
		test((short)-789);
	}
	
	static void test(final short value)
	{
		final byte[] bytes = new byte[2];
		System.out.println("original     : " + value);
		System.out.println("binary       : " + VarString.New().padLeft(Integer.toBinaryString(value & 0xFFFF), Short.SIZE, '0'));
		System.out.println();
		
		XArrays.set_shortInBytes(bytes, 0, value);
		System.out.println("bytes[1]     : " + bytes[1]);
		System.out.println("bytes[0]     : " + "        " + bytes[0]);
		System.out.println("bytes[1] bin : " + VarString.New().padLeft(Integer.toBinaryString(bytes[1] & 0xFF), Byte.SIZE, '0'));
		System.out.println("bytes[0] bin : " + VarString.New().repeat(8, ' ').padLeft(Integer.toBinaryString(bytes[0] & 0xFF), Byte.SIZE, '0'));
		System.out.println();
		
		final short reconstructed = (short)(bytes[1] << 8 | bytes[0] & 0xFF);
		
//		final int reconstructed = (bytes[1]<<8 | bytes[0] & 0xFF) & 0xFFFF;
		System.out.println("reconstructed: " + reconstructed);
		System.out.println("binary       : " + VarString.New().padLeft(Integer.toBinaryString(reconstructed & 0xFFFF), Short.SIZE, '0'));
		System.out.println("--------------\n");
	}
	
}

package one.microstream.memory;

import one.microstream.chars.VarString;

public class MainTestAsByteArray
{
	public static void main(final String[] args)
	{
		test(0, 1, 3, 65, 127, 128, 675641561, Long.MAX_VALUE, -34 , -645634);
//		test(128);
	}
	
	static void test(final long... value)
	{
		for(final long l : value)
		{
			test(l);
		}
	}
	
	static void test(final long value)
	{
		final byte[] array = asByteArray(value);

		System.out.println(value);
		System.out.println(VarString.New().padLeft(Long.toHexString(value), 16, '0'));
		for(int i = array.length; i --> 0;)
		{
			System.out.print(VarString.New().padLeft(Integer.toHexString(array[i] & 0xFF), 2, '0'));
		}
		System.out.println("\n---\n");
	}
	
	public static final byte[] asByteArray(final long value)
	{
		final byte[] array = new byte[Long.BYTES];
		
		for(int i = 0; i < array.length; i++)
		{
//			System.err.println(">"+VarString.New().padLeft(Long.toBinaryString(value), 64, '0'));
//			System.err.println(">"+VarString.New().padLeft(Long.toBinaryString(value >> 8*i), 64, '0'));
//			System.err.println(">"+VarString.New().padLeft(Long.toBinaryString(0xFFL), 64, '0'));
//			System.err.println(">"+VarString.New().padLeft(Long.toBinaryString(value >> 8*i & 0xFFL), 64, '0'));
//			System.err.println();
			array[i] = (byte)(value >> 8*i & 0xFFL);
		}
		
		return array;
	}
}

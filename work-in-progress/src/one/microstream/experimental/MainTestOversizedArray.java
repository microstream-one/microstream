package one.microstream.experimental;

import java.util.Arrays;

import one.microstream.memory.XMemory;
import sun.misc.Unsafe;

public class MainTestOversizedArray
{
	static final Unsafe vm = (Unsafe)XMemory.getSystemInstance();
	static final long BABO = Unsafe.ARRAY_BYTE_BASE_OFFSET;
	static final long LABO = Unsafe.ARRAY_LONG_BASE_OFFSET;

	public static void main(final String[] args)
	{
		test2();

	}


	static void test1()
	{
		final byte[] bytes0 = new byte[]{1, 2, 3, 4, 5, 6, 7, 8};
		final long[] longs = new long[1];

		vm.copyMemory(bytes0, BABO, longs, LABO, bytes0.length);

		final byte[] bytes1 = new byte[bytes0.length];
		vm.copyMemory(longs, LABO, bytes1, BABO, bytes1.length);

		System.out.println(Arrays.equals(bytes0, bytes1));
	}

	static int calculate8alignedBlockCount(final int size)
	{
		return (size & 7) != 0 ? (size>>>3)+1 : size>>>3;
	}

	static void test2()
	{
		final int size1, size2;
		size1 = Integer.MAX_VALUE;
//		size1 = Integer.MAX_VALUE/8*8; // align size to 8 bytes (simple case for testing)
		size2 = 32; // size1 + size2 exceeds max int.

		final byte[] bytesS1 = new byte[size1];
		final byte[] bytesS2 = new byte[size2];
		for(int i = 0; i < size1; i++)
		{
			bytesS1[i] = 17;
		}
		for(int i = 0; i < size2; i++)
		{
			bytesS2[i] = 34;
		}
		System.out.println("Total array  size: "+((long)bytesS1.length + (long)bytesS2.length));

		final long[] longs = new long[calculate8alignedBlockCount(size1) + calculate8alignedBlockCount(size2)];
		System.out.println("Total memory size: "+(long)longs.length * 8);

		vm.copyMemory(bytesS1, BABO, longs, LABO +     0, size1);
		vm.copyMemory(bytesS2, BABO, longs, LABO + size1, size2);
//		System.out.print("...");
//		for(int i = longs.length - size2/8 - 2; i < longs.length; i++)
//		{
//			System.out.print(i+"\t");
//		}
//		System.out.println();
		System.out.print("...");
		for(int i = longs.length - size2/8 - 2; i < longs.length; i++)
		{
			System.out.print(Long.toHexString(longs[i])+"\t");
		}
		System.out.println();

		final byte[] bytesT1 = new byte[size1];
		final byte[] bytesT2 = new byte[size2];

		vm.copyMemory(longs, LABO +     0, bytesT1, BABO, size1);
		vm.copyMemory(longs, LABO + size1, bytesT2, BABO, size2);

		System.out.println(Arrays.equals(bytesS1, bytesT1));
		System.out.println(Arrays.equals(bytesS2, bytesT2));

		if(size2 < 10000)
		{
			System.out.println(Arrays.toString(bytesS2));
			System.out.println(Arrays.toString(bytesT2));
		}
	}

}

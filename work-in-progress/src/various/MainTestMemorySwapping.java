package various;

import java.nio.ByteBuffer;
import java.text.DecimalFormat;

import one.microstream.memory.XMemory;
import one.microstream.memory.XMemory;

public class MainTestMemorySwapping
{
	static final DecimalFormat FORMAT = new DecimalFormat(",###");
	
	public static void main(final String[] args)
	{
		// (07.10.2019 TM)NOTE: /!\ important: requires "-XX:MaxDirectMemorySize=20G" JVM argument.
		testOffHeapAllocationSwapping(2_000_000_000, 10);
//		testSwapping(2_000_000_000, 100); // will crash due to JVM bug, probably @HotSpotIntrinsicCandidate stuff.
	}
	
	static void testOffHeapAllocationSwapping(final int memorySize, final int amount)
	{
		final ByteBuffer[] dbbs = new ByteBuffer[amount];
		
		long totalMemory = 0;
		for(int i = 0; i < dbbs.length; i++)
		{
			System.out.print("Allocating " + format(memorySize) + " # " + (i + 1) + "... ");
			dbbs[i] = ByteBuffer.allocateDirect(memorySize);
			dbbs[i].position(memorySize);
			totalMemory += memorySize;
			System.out.println("Total memory = " + totalMemory);
		}
		
		System.out.println("Final total memory = " + totalMemory);
	}
	
	static void testSwapping(final int memorySize, final int valueOffset)
	{
		final ByteBuffer dbb = ByteBuffer.allocateDirect(memorySize);
		final long memoryStartAddress = XMemory.getDirectByteBufferAddress(dbb);
		final long memoryBoundAddress = memoryStartAddress + dbb.capacity();
				
		System.out.println(
			"Setup:"
			+ "\nmemorySize = " + format(memorySize)
			+ "\nmemoryStartAddress = " + memoryStartAddress
			+ "\nmemoryBoundAddress = " + memoryBoundAddress
		);
		System.out.println();
		

		System.out.println("Filling the complete buffer with content values ... ");
		System.out.println("(" + memorySize/Integer.BYTES + " values starting at value " + valueOffset + ")");
		int v = valueOffset;
		for(long i = memoryStartAddress; i < memoryBoundAddress; i += Integer.BYTES)
		{
			XMemory.set_int(i, v++);
		}
//		dbb.position(memorySize); // has no effect on the error
		System.out.println("Done.");
		System.out.println();
		
		

		System.out.println("Calculating sum accross the complete buffer content ...");
		long sum = 0;
		for(long i = memoryStartAddress; i < memoryBoundAddress; i += Integer.BYTES)
		{
			sum += XMemory.get_int(i);
//			System.out.println("Memory at offset " + (i - memoryStartAddress) + " = " + XMemory.get_int(i));
		}
		System.out.println("sum = " + sum);
		System.out.println("---");
		
		

		System.out.println("Reading every 10000th value over the complete buffer content ...");
		for(long i = memoryStartAddress; i < memoryBoundAddress; i += Integer.BYTES*10000)
		{
			System.out.println("Memory at offset " + (i - memoryStartAddress) + " = " + XMemory.get_int(i));
		}
		System.out.println("---");
		
		
		
		System.out.println("Reading EVERY value over the complete buffer content ... ");
		System.out.println("(this will cause the StringBuilder(!) (NOT this user custom code) to crash the JVM)");
		for(long i = memoryStartAddress; i < memoryBoundAddress; i += Integer.BYTES)
		{
			System.out.println("Memory at offset " + (i - memoryStartAddress) + " = " + XMemory.get_int(i));
		}
		System.out.println("---");
	}
	
	private static String format(final long value)
	{
		return FORMAT.format(value);
	}
	
}

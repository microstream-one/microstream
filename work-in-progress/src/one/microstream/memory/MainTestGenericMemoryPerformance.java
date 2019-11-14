package one.microstream.memory;

import one.microstream.memory.sun.JdkInternals;

public class MainTestGenericMemoryPerformance
{
	static final Setup S1 = Setup(100, 1_000_000, 10, 100, 1000, 10_000, 100_000, 1_000_000);
	
//	static final String NUMBER_FORMAT = "00,000,000,000"; // direct reading
	static final String NUMBER_FORMAT = "00000000000"; // excel
	
	public static void main(final String[] args)
	{
		XMemory.setMemoryAccessor(
			MemoryAccessorGeneric.New(
				JdkInternals.InstantiatorBlank(),
				JdkInternals.DirectBufferDeallocator()
			)
		);
		
		testAll();
	}
	
	static void testAll()
	{
		testAll(S1);
	}
	
	static void testAll(final Setup setup)
	{
		test(setup.runs, setup.totalSize, setup.chunkSizes);
	}
	
	static void test(final int runs, final int totalSize, final int... chunkSizes)
	{
		final long[][] addressesses = new long[chunkSizes.length][];
		
		for(int r = 0; r < runs; r++)
		{
			printHeader();
			for(int s = 0; s < chunkSizes.length; s++)
			{
				final int size = chunkSizes[s];
				final int count = totalSize / size;
				if(count == 0)
				{
					printSkippedSize(size);
					continue;
				}
				
				final long[] addresses = new long[count];
				addressesses[s] = addresses;
				
				testAllocationLoop(count, size, addresses);
			}
			
			printHeader();
			for(int s = 0; s < chunkSizes.length; s++)
			{
				final int size = chunkSizes[s];
				final int count = totalSize / size;
				if(count == 0)
				{
					printSkippedSize(size);
					continue;
				}
				
				testWritingLoop(count, size, addressesses[s]);
			}
			
			printHeader();
			for(int s = 0; s < chunkSizes.length; s++)
			{
				final int size = chunkSizes[s];
				final int count = totalSize / size;
				if(count == 0)
				{
					printSkippedSize(size);
					continue;
				}
				
				testDeallocationLoop(count, size, addressesses[s]);
			}
			
			// cleanup, actually pretty useless
			for(int s = 0; s < chunkSizes.length; s++)
			{
				addressesses[s] = null;
			}
		}
	}
	
	private static void testAllocationLoop(final int count, final int size, final long[] addresses)
	{
		final long tStart = System.nanoTime();
		for(int i = 0; i < count; i++)
		{
			addresses[i] = XMemory.allocate(size);
		}
		final long tStop = System.nanoTime();
		printCountSize("Allocation\t", count, size, tStop - tStart);
	}
		
	private static void testWritingLoop(final int count, final int size, final long[] addresses)
	{
		final long tStart = System.nanoTime();
		for(int i = 0; i < count; i++)
		{
			final long address = addresses[i];
			final int sizeBound = size - Long.BYTES;
			for(int b = 0; b < sizeBound; b++)
			{
				XMemory.set_long(address + b, Long.MAX_VALUE);
			}
		}
		final long tStop = System.nanoTime();
		printCountSize("Filling\t", count, size, tStop - tStart);
	}
	
	private static void testDeallocationLoop(final int count, final int size, final long[] addresses)
	{
		final long tStart = System.nanoTime();
		for(int i = 0; i < count; i++)
		{
			 XMemory.free(addresses[i]);
		}
		final long tStop = System.nanoTime();
		printCountSize("Deallocation\t", count, size, tStop - tStart);
	}
	
	static void printCountSize(final String label, final int count, final int size, final long duration)
	{
		System.out.println(
			label + count + " * " + size + "\t" + new java.text.DecimalFormat(NUMBER_FORMAT).format(duration)
			+ "\taverage:\t" + new java.text.DecimalFormat(NUMBER_FORMAT).format(duration / count)
		);
	}
	
	static void printSkippedSize(final int size)
	{
		System.out.println("[Skipped]\t[" + size + "]");
	}
	
	static void printHeader()
	{
		System.out.println("\n"
			+ XMemory.memoryAccessor().getClass().getSimpleName()
			+"\tcount*size"
			+"\tduration"
			+"\t"
			+"\taverage"
		);
	}
	
	
	static Setup Setup(final int runs, final int totalSize, final int... chunkSizes)
	{
		return new Setup(runs, totalSize, chunkSizes);
	}
	
	static class Setup
	{
		int   runs      ;
		int   totalSize ;
		int[] chunkSizes;
		
		Setup(final int runs, final int totalSize, final int[] chunkSizes)
		{
			super();
			this.runs       = runs      ;
			this.totalSize  = totalSize ;
			this.chunkSizes = chunkSizes;
		}
		
	}
	
}

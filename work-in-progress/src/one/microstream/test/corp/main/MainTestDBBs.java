package one.microstream.test.corp.main;

import one.microstream.concurrency.XThreads;
import one.microstream.memory.XMemory;

public class MainTestDBBs
{
	public static void main(final String[] args)
	{
		long totalCapacity = 0;

		long i = 0;
		
		XThreads.sleep(3000);
		while(true)
		{
			totalCapacity += doit();
			if(i++ % 1000 == 0)
			{
				System.out.println("Allocated " + totalCapacity);
			}
			XThreads.sleep(1);
		}
	}
	
	static long doit()
	{
		final long address = XMemory.allocate(100000);
		XMemory.free(address);
		
		return XMemory.allocateDirectNative(100000).capacity();
	}
}

package one.microstream.memory;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryUsage;

public class MainTestMemoryMXBeanPerformance
{
	public static void main(final String[] args)
	{
		for(int i = 0; i < 10000; i++)
		{
			doTest();
		}
	}
	
	
	static final void doTest()
	{
		long used = 0;
		
		final long tStart = System.nanoTime();
		
		for(int i = 0; i < 1000; i++)
		{
			// querying the usage instance is the expensive part.
			final MemoryUsage usage = ManagementFactory.getMemoryMXBean().getHeapMemoryUsage();
			
			// these are basically "free", no matter how many.
			used += usage.getUsed();
//			used += usage.getCommitted();
		}
		
		final long tStop = System.nanoTime();
		System.out.println(new java.text.DecimalFormat("00,000,000,000").format(tStop - tStart) + " " + used);
	}
	
	
}

package one.microstream.memory;

import one.microstream.math.XMath;

public class MainTestInterfacePerformance
{
	static final int warmup = 10_000;
	static final int runs = 10_000;
	static final int count = 1_000_000;
	static final long range = count * Long.BYTES;
	
	static
	{
		XMemory.setMemoryAccessor(MemoryAccessorSun.New());
	}
	
	public static void main(final String[] args)
	{
		final long address = XMemory.allocate(range);
		
		final java.text.DecimalFormat formatter = new java.text.DecimalFormat("00,000,000,000");

		final long bound = address + range;
		System.out.println("warmup...");
		for(int w = 0; w < warmup; w++)
		{
			final long tStart = System.nanoTime();
			hot(address);
			final long tStop = System.nanoTime();
			System.out.println(formatter.format(tStop - tStart));
		}
		
		System.out.println("\n\n\nhot ...\n\n\n");
		
		long sum = 0, min = Long.MAX_VALUE;

		for(int r = 0; r < runs; r++)
		{
			final long tStart = System.nanoTime();
			hot(address);
			final long tStop = System.nanoTime();
//			System.out.println(formatter.format(tStop - tStart));
			
			final long time = tStop - tStart;
			sum += time;
			if(time < min)
			{
				min = time;
			}
			
			// force JVM to use the values so that it can't optimize something away.
			System.out.println(XMemory.get_long(address + XMath.random(count) * Long.BYTES));
		}

		System.out.println("\n\n\n");
		System.out.println("average: " + formatter.format(sum / runs));
		System.out.println("minimum: " + formatter.format(min));
	}
	
	static final void hot(final long address)
	{
		final long bound = address + range;
		for(long a = address; a < bound; a += Long.BYTES)
		{
			// Tests on a 4.20 GHz desktop PC say stastically ~1.3 CPU cycles for this operation. How can that be?
			XMemory.set_long(a, bound);
		}
	}
	
}


package net.jadoth.persistence.test.setter;

import java.text.DecimalFormat;

import net.jadoth.low.XMemory;
import net.jadoth.math.XMath;

public class MainTestSetterPerformance
{
	private static final int TOTAL_RUNS = 1000;
	private static final int COUNT = 10_000_000;
	
	static
	{
		Setters.setHandler(new Handler());
	}
	
	public static void main(final String[] args)
	{
		final DecimalFormat countFormat = new java.text.DecimalFormat("000");
		final DecimalFormat timeFormat = new java.text.DecimalFormat("00,000,000,000");
		
		final Handler handler = Setters.getHandler();
		
		final long address = XMemory.allocate(COUNT * 8);
		
		final long[] data = createData(COUNT);
		
		long totalTime = 0;
		for(int r = 1; r <= TOTAL_RUNS; r++)
		{
			long tStart, tStop;
			
			// the actual work (storing all entities) that is being measured.
			tStart = System.nanoTime();
			handler.setValues(address, data);
			tStop = System.nanoTime();

			totalTime += tStop - tStart;
			final long averagePerRun = totalTime / r;
			
			// printing and statistics
			System.out.println(
				"#" + countFormat.format(r)
				+ " Elapsed Time (ns): " + timeFormat.format(tStop - tStart)
				+ ", Average (ns): " + timeFormat.format(averagePerRun)
			);
		}
		
		// result
		System.out.println(
			"\nResult:\n"
			+ "Average per run     : " + timeFormat.format(totalTime / TOTAL_RUNS) + " ns.\n"
			+ "Average per instance: " + timeFormat.format(totalTime / TOTAL_RUNS / COUNT) + " ns.\n"
		);
	}
	
	static long[] createData(final int length)
	{
		final long[] array = new long[length];
		
		for(int i = 0; i < array.length; i++)
		{
			array[i] = XMath.random(Integer.MAX_VALUE);
		}
		
		return array;
	}
}

package net.jadoth.util.chars;

import net.jadoth.memory.Memory;

public class MainTestParseIntegerPerformance
{
	static final int SIZE = 1_000_000;
	static final int RUNS = 1_000;
	static final int AVG  = 100;

	static final String[] STRINGS = new String[SIZE];
	static final char[][] CHARS   = new char[STRINGS.length][];
	static
	{
		long totalLength = 0;
		for(int i = 0; i < SIZE; i++)
		{
			final long value = (long)(Math.random() * Math.pow(10, Math.random() * 12));
//			final long value = (long)(Math.random() * Long.MAX_VALUE);
			CHARS[i] = Memory.accessChars(STRINGS[i] = Long.toString(value));

			totalLength += CHARS[i].length;
		}
		System.out.println("average literal length: "+totalLength / SIZE);
	}


	public static void main(final String[] args)
	{
		long totalTime  = 0;
		long totalCount = 0;

		while(true)
		{
			if(totalCount % AVG == 0)
			{
				System.out.println("resetting");
				totalTime  = 0;
				totalCount = 0;
			}

			final long tStart = System.nanoTime();
			for(int i = 0; i < SIZE; i++)
			{
				Long.parseLong(STRINGS[i]);
//				JadothChars.parse_longDecimal(CHARS[i]);
			}
			final long tStop = System.nanoTime();
			totalCount++;
			totalTime += tStop - tStart;
			System.out.println("Elapsed Time: " + new java.text.DecimalFormat("00,000,000,000").format(totalTime / totalCount / RUNS));
		}


	}


}

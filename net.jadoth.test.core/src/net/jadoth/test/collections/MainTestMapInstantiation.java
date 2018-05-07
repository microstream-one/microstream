package net.jadoth.test.collections;

import net.jadoth.collections.HashTable;

public class MainTestMapInstantiation
{
	public static void main(final String[] args)
	{
		final int size = 1000*100;
		final Object[] array = new Object[size];

		long tStart, tStop;

		for(int r = 10000; r --> 0;)
		{
			tStart = System.nanoTime();
			for(int i = 0; i < size; i++)
			{
				array[i] = HashTable.New();
			}
			tStop = System.nanoTime();
			System.out.println("Elapsed Time: " + new java.text.DecimalFormat("00,000,000,000").format(tStop - tStart));
		}


	}
}

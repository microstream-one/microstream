package net.jadoth.test.collections;

import net.jadoth.collections.BulkList;

/**
 * @author Thomas Muenz
 *
 */
public class MainTestListHasUnique
{
	static final int SIZE = 500;


	public static void main(final String[] args)
	{
		final BulkList<String> strings = new BulkList<>(SIZE);
		for(int i = 0; i < SIZE; i++)
		{
			strings.add(Integer.toString(i));
		}

		boolean hasUniqueValues;
		long tStart, tStop;

		for(int k = 0; k < 20; k++)
		{
			tStart = System.nanoTime();
			hasUniqueValues = strings.hasDistinctValues();
			tStop = System.nanoTime();
			System.out.println("Elapsed Time: " + new java.text.DecimalFormat("00,000,000,000").format(tStop - tStart));
			System.out.println(hasUniqueValues);
		}

	}

}

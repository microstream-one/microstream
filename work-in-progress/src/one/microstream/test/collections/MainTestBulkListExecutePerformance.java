package one.microstream.test.collections;

import one.microstream.collections.BulkList;
import one.microstream.functional.XFunc;

public class MainTestBulkListExecutePerformance
{
	static final int SIZE = 1<<20;
	static final Integer ints[] = new Integer[SIZE];
	static {
		for(int i = 0; i < SIZE; i++)
		{
			ints[i] = i;
		}
	}
	static final int RUNS = 50;



	public static void main(final String[] args)
	{
		final BulkList<Integer> intList = new BulkList<>(SIZE);
		intList.addAll(ints);

		long tStart, tStop;
		for(int k = 0; k < RUNS; k++)
		{
			tStart = System.nanoTime();
			intList.iterate(XFunc::noOp);
			tStop = System.nanoTime();
			System.out.println("Elapsed Time: " + new java.text.DecimalFormat("00,000,000,000").format(tStop - tStart));
		}

	}
}

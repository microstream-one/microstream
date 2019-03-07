package net.jadoth.test.collections;

import net.jadoth.collections.BulkList;


/**
 * @author Thomas Muenz
 *
 */
public class MainTestBulkListAddPerformance
{
	static final int SIZE = 1000*1000*1;
	static final Integer ints[] = new Integer[SIZE];
	static {
		for(int i = 0; i < SIZE; i++)
		{
			ints[i] = i;
		}
	}

	static final int RUNS = 1000*1000*10;

	public static void main(final String[] args)
	{
//		final LimitList<Integer> intList = new LimitList<Integer>(SIZE);
		final BulkList<Integer> intList = new BulkList<>(SIZE);
//		ArrayList<Integer> intList = new ArrayList<Integer>();
//		final XAddingCollection<Integer> intList = new BulkList<Integer>(SIZE);
//		final XAddingCollection<Integer> intList = new Adder<Integer>(gl);

//		final LinkedList<Integer> intList2 = new LinkedList<Integer>();
//		final LimitList<Integer> intList = new LimitList<Integer>(SIZE); // amazingly significantly slower than BulkList

		long tStart, tStop;
		final java.text.DecimalFormat df = new java.text.DecimalFormat("00,000,000,000");

		for(int k = 0; k < RUNS; k++)
		{
			tStart = System.nanoTime();
			for(int i = 0, len = ints.length; i < len; i++)
			{
				intList.add(ints[i]);
			}

			tStop = System.nanoTime();
			System.out.println("GL Elapsed Time: " + df.format(tStop - tStart));
			intList.clear();
//			intList = new BulkList<Integer>();
//			intList = new ArrayList<Integer>();
		}

//		for(int k = 0; k < RUNS; k++)
//		{
//			tStart = System.nanoTime();
//			for(int i = 0, len = ints.length; i < len; i++)
//			{
//				intList2.add(ints[i]);
//			}
//			tStop = System.nanoTime();
//			System.out.println("LL Elapsed Time: " + new java.text.DecimalFormat("00,000,000,000").format(tStop - tStart));
//			intList2.clear();
//		}

	}

}

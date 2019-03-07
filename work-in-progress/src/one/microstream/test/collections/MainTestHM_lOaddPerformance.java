package one.microstream.test.collections;

import one.microstream.collections.HashMapIdObject;


/**
 * @author Thomas Muenz
 *
 */
public class MainTestHM_lOaddPerformance
{
	static final int SIZE = 1000*1000;
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
		final HashMapIdObject<Integer> map = HashMapIdObject.New();
		long tStart, tStop;

		for(int k = 0; k < RUNS; k++)
		{
			tStart = System.nanoTime();
			for(int i = SIZE; i --> 0;)
			{
				map.add(i  , ints[i]);
				map.add(i-1, ints[i]);
				map.add(i+1, ints[i]);
			}

			tStop = System.nanoTime();
			System.out.println("Elapsed Time: " + new java.text.DecimalFormat("00,000,000,000").format(tStop - tStart));
//			System.out.println(map.size()+": "+map);
			map.clear();
		}

	}

}

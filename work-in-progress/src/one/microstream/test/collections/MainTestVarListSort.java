package one.microstream.test.collections;

import java.util.Collections;
import java.util.LinkedList;

import one.microstream.collections.BulkList;
import one.microstream.collections.XSort;
import one.microstream.math.XMath;

/**
 * @author Thomas Muenz
 *
 */
public class MainTestVarListSort
{
	static final int SIZE = 1000000;

	static final Integer[] createInts()
	{
		final Integer[] ints = new Integer[SIZE];
		for(int i = 0; i < SIZE; i++)
		{
			ints[i] = XMath.random(SIZE);
		}
		return ints;
	}


	/**
	 * @param args
	 */
	public static void main(final String[] args)
	{


//		System.out.println(vl);
//		vl.sort(ORDER_INTS);
//		System.out.println(vl);


		for(int n = 10; n --> 0;)
		{
			final BulkList<Integer> fl = new BulkList<>();
			final LinkedList<Integer> ll = new LinkedList<>();
			final Integer[] ints = createInts();
			for(final Integer i : ints)
			{
				fl.add(i);
				ll.add(i);
			}
			long tStart, tStop;


			tStart = System.nanoTime();
			Collections.sort(ll, XSort::compare);
			tStop = System.nanoTime();
			System.out.println("LL Elapsed Time: " + new java.text.DecimalFormat("00,000,000,000").format(tStop - tStart));

			tStart = System.nanoTime();
			fl.sort(XSort::compare);
			tStop = System.nanoTime();
			System.out.println("FL Elapsed Time: " + new java.text.DecimalFormat("00,000,000,000").format(tStop - tStart));


			System.out.println("");
		}

	}

}

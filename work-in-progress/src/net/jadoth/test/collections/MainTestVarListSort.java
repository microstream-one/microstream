package net.jadoth.test.collections;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;

import net.jadoth.collections.BulkList;
import net.jadoth.math.XMath;

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


	static final Comparator<Integer> ORDER_INTS = new Comparator<Integer>(){
		@Override
		public int compare(final Integer o1, final Integer o2){
			if(o1 == null)
			{
				return o2 == null ?0 :1;
			}
			if(o2 == null)
			{
				return -1;
			}
			if(o1.intValue() < o2.intValue())
			{
				return -1;
			}
			else if(o1.intValue() > o2.intValue()){
				return 1;
			}
			return 0;
		}
	};

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
			Collections.sort(ll, ORDER_INTS);
			tStop = System.nanoTime();
			System.out.println("LL Elapsed Time: " + new java.text.DecimalFormat("00,000,000,000").format(tStop - tStart));

			tStart = System.nanoTime();
			fl.sort(ORDER_INTS);
			tStop = System.nanoTime();
			System.out.println("FL Elapsed Time: " + new java.text.DecimalFormat("00,000,000,000").format(tStop - tStart));


			System.out.println("");
		}

	}

}

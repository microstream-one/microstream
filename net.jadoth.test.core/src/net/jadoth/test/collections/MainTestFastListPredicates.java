package net.jadoth.test.collections;

import java.util.function.Consumer;

import net.jadoth.collections.BulkList;
import net.jadoth.math._intRange;
import net.jadoth.typing.XTypes;

/**
 * @author Thomas Muenz
 *
 */
public class MainTestFastListPredicates
{
	private static final int SIZE = 1000000;

	/**
	 * @param args
	 */
	public static void main(final String[] args)
	{
		// TODO Auto-generated method stub

		final Consumer<Integer> CHECK_MAX_INTEGER = new Consumer<Integer>(){

			@Override
			public void accept(final Integer element)
			{
				if(element != null && element.intValue() >= Integer.MAX_VALUE)
				{
					throw new RuntimeException();
				}
			}

		};

		final BulkList<Integer> intList = new BulkList<>(SIZE);
		for(final Integer i : range(0, SIZE-1))
		{
			intList.add(i);
		}
		System.out.println("list filled: "+XTypes.to_int(intList.size())+" elements.");


		long tStart, tStop, min = Integer.MAX_VALUE;

		for(int i = 20; i -->0;)
		{
			tStart = System.nanoTime();
			intList.iterate(CHECK_MAX_INTEGER);
			tStop = System.nanoTime();
//			System.out.println("Elapsed Time: " + new java.text.DecimalFormat("00,000,000,000").format(tStop - tStart));
			if(tStop - tStart < min)
			{
				min = tStop - tStart;
			}
		}
		System.out.println("Min: "+new java.text.DecimalFormat("00,000,000,000").format(min));
	}

	public static _intRange range(final int from, final int to)
	{
		return _intRange.New(from, to);
	}
	
}

package net.jadoth.test.collections;

import static net.jadoth.math.XMath.sequence;

import net.jadoth.collections.BulkList;
import net.jadoth.equality.Equalator;
import net.jadoth.functional.XFunc;

public class MainTestEqualatorAsPredicate
{
	static final int SIZE = 1000*1000;
	static final int RUNS = 1000;



	public static void main(final String[] args)
	{
		final Integer lastValue = SIZE - 1;
		final BulkList<Integer> ints = BulkList.New(sequence(lastValue));

		boolean found = false;
		for(int r = 0; r < RUNS; r++)
		{
			found = false;
			long tStart, tStop;
			tStart = System.nanoTime();
//			found = ints.contains(lastValue, Jadoth.equals);
			found = ints.containsSearched(XFunc.isEqual(lastValue, Equalator.value())); // about as fast as equalator
			tStop = System.nanoTime();
			System.out.print(found);
			System.out.println(" Elapsed Time: " + new java.text.DecimalFormat("00,000,000,000").format(tStop - tStart));
		}
	}
}


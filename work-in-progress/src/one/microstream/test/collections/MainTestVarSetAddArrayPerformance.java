package one.microstream.test.collections;

import static one.microstream.X.box;
import static one.microstream.math.XMath.sequence;

import one.microstream.collections.EqHashEnum;

public class MainTestVarSetAddArrayPerformance
{
	static final int SIZE = 1<<20;
	static final int LOOPS = 1<<5;
	static final int RUNS = 50;
	static final Integer[] ints = box(sequence(SIZE-1));



	public static void main(final String[] args)
	{
		final EqHashEnum<Integer> set = EqHashEnum.New(SIZE);

		final Integer[] ints = MainTestVarSetAddArrayPerformance.ints;
		long tStart;
		long tStop;

		for(int r = 0; r < RUNS; r++)
		{
			tStart = System.nanoTime();
			set.addAll(ints);
			tStop = System.nanoTime();
			System.out.println("Elapsed Time: " + new java.text.DecimalFormat("00,000,000,000").format(tStop - tStart));
			set.clear();
		}



	}
}

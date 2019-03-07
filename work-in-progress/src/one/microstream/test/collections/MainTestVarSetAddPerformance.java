package one.microstream.test.collections;

import static one.microstream.X.box;
import static one.microstream.math.XMath.sequence;

import one.microstream.collections.EqHashEnum;
import one.microstream.math.XMath;

public class MainTestVarSetAddPerformance
{
	static final int SIZE = 1<<20;
	static final int LOOPS = 1000;
	static final int RUNS = 50;
	static final Integer[] ints = box(sequence(SIZE-1));



	public static void main(final String[] args)
	{
		final EqHashEnum<Integer> set = EqHashEnum.New(SIZE);

		final Integer[] ints = MainTestVarSetAddPerformance.ints;
		for(int i = 0; i < SIZE; i++)
		{
			ints[i] = XMath.random(Integer.MAX_VALUE);
		}
		long tStart;
		long tStop;

//		Integer i = 0;
//		for(int r = 0; r < RUNS; r++)
//		{
//			tStart = System.nanoTime();
//			for(int s = 0; s < SIZE; s++)
//			{
//				i = ints[s];
//			}
//			tStop = System.nanoTime();
//			System.out.println("d Elapsed Time: " + new java.text.DecimalFormat("00,000,000,000").format(tStop - tStart));
//			set.clear();
//		}
//		System.out.println(i);

		for(int r = 0; r < RUNS; r++)
		{
			tStart = System.nanoTime();
			for(int s = 0; s < SIZE; s++)
			{
				set.add(ints[s]);
			}
			tStop = System.nanoTime();
			System.out.println(r+"\tElapsed Time: " + new java.text.DecimalFormat("00,000,000,000").format(tStop - tStart));
			set.clear();
//			System.gc();
		}



	}
}

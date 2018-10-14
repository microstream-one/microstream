package net.jadoth.test.collections;

import net.jadoth.collections.EqHashEnum;
import net.jadoth.math.XMath;

public class MainTestEnumPerformance
{
	static final int SIZE = 10;
	static final int RUNS = 1000*10;
	static final Integer[] ints = XMath.sequence((Integer)(SIZE-1));

	public static void main(final String[] args)
	{
//		final GrowEnum<Integer> ints1 = new GrowEnum<Integer>(100);
//		ints1.add(ints);
//		System.out.println(ints1);

		final EqHashEnum<Integer> ints2 = EqHashEnum.New(100);
		ints2.addAll(ints);
		System.out.println(ints2);

		final Integer subject = SIZE/2;

		boolean lastCheck = false;
		for(int r = RUNS; r --> 0;)
		{
			long tStart, tStop;
			tStart = System.nanoTime();
			for(int r2 = RUNS; r2 --> 0;)
			{
//				lastCheck = ints1.contains(subject);
				lastCheck = ints2.contains(subject);
			}
			tStop = System.nanoTime();
			System.out.println("Elapsed Time: " + new java.text.DecimalFormat("00,000,000,000").format(tStop - tStart));
		}
		System.out.println(lastCheck);
	}
}

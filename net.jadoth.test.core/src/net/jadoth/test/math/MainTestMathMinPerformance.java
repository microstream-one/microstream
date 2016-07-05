package net.jadoth.test.math;

import net.jadoth.math.JadothMath;

public class MainTestMathMinPerformance
{
	public static void main(final String[] args)
	{
		final int[] ints = JadothMath.randoming(1_000_000);

		for(int r = 1000; r --> 0;)
		{
			final long tStart = System.nanoTime();

			int count = 0;
			for(int i = 1; i < ints.length; i++)
			{
				/* Math.min is CONSIDERABLY faster than the local min() even though the ">=" proved to be
				 * faster than <= (see Math.min) in many cases, probably due to ">=" being able to check more
				 * efficiently against 0, simple sign bit checking or so.
				 * This implies that the JDK Math static methodsare some kind of optimized/replaced at runtime.
				 *
				 * See http://stackoverflow.com/questions/22752198/java-math-min-max-performance
				 * (I've read a bit about how HotSpot has some "intrinsics" that injects in the code, specially for Java standard Math libs)
				 *
				 */
				if(Math.max(ints[i - 1], ints[i]) == ints[i])
				{
					count++;
				}
			}

			final long tStop = System.nanoTime();
			System.out.println(count +" Elapsed Time: " + new java.text.DecimalFormat("00,000,000,000").format(tStop - tStart));
		}

	}


	static final int min(final int a, final int b)
	{
        return a >= b ? b : a;
    }
}

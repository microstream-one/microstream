package one.microstream.test.collections;

import static one.microstream.math.XMath.sequence;

/**
 * @author Thomas Muenz
 *
 */
public class MainTestIntComparison
{

	/**
	 * @param args
	 */
	public static void main(final String[] args)
	{
		final int[] ints = sequence(0, 100000);

		int r = 0;

		long tStart, tStop;


		for(int n = 10; n --> 0;)
		{
			tStart = System.nanoTime();
			for(int i : ints)
			{
				if(i > 100000)
				{
					r = i;
				}
			}
			tStop = System.nanoTime();
			System.out.println("Elapsed Time: " + new java.text.DecimalFormat("00,000,000,000").format(tStop - tStart));
			System.out.println(r);
		}
	}

}

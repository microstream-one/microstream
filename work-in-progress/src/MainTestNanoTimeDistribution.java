import one.microstream.math.FastRandom;



public class MainTestNanoTimeDistribution
{

	/**
	 * @param args
	 */
	public static void main(final String[] args)
	{
//		final int avg = 0;
//		System.out.println(avg = avg+(int)System.nanoTime() >>> 2);
//		System.out.println(System.nanoTime());

//		final Random rnd = XMath.random();

		for(int k = 20; k --> 0;)
		{
			int value = 0;
			long tStart;
			long tStop;

			tStart = System.nanoTime();
			final FastRandom lr = new FastRandom();
			for(int i = 0; i < 10000000; i++)
			{
//				value += rnd.nextInt(1000);
//				value += StaticLowRandom.nextInt(1000);
				value += lr.nextInt(1000);
			}
			tStop = System.nanoTime();
			System.out.println("Elapsed Time: " + new java.text.DecimalFormat("00,000,000,000").format(tStop - tStart));
			System.out.println(value);
		}


//		for(int i = 0; i < 1000000; i++)
//		{
////			value += rnd.nextInt(1000);
////			System.out.println(LowRandom.nextInt(1024));
//			final int r = LowRandom.nextInt(1024);
//			if(r < 0)
//				System.out.println(r);
//		}

	}

}

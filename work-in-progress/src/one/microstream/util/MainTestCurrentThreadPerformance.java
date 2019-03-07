package one.microstream.util;

public class MainTestCurrentThreadPerformance
{
	static final int LOOPS = Integer.MAX_VALUE;
	static final int RUNS = 1;

	public static void main(final String[] args)
	{

		Thread t;

		t = Thread.currentThread();

		int val = 0;
		int count = 0;
		int i = LOOPS;
		long tStart;
		long tStop;




		for(int omfg = 0; omfg < 10; omfg++)
		{
			val = 0;
			i = LOOPS;
			count = 0;
			for(int r = 0; r < RUNS; r++)
			{
				tStart = System.nanoTime();
				while(i --> 0)
				{
					val += t.getPriority();
					count++;
				}
				tStop = System.nanoTime();
				System.out.println("t  Elapsed Time: " + new java.text.DecimalFormat("00,000,000,000").format(tStop - tStart));
			}
			System.out.println(val);
			System.out.println("count: "+count);



			val = 0;
			i = LOOPS;
			count = 0;
			for(int r = 0; r < RUNS; r++)
			{
				tStart = System.nanoTime();
				while(i --> 0)
				{
					val += Thread.currentThread().getPriority();
					count++;
				}
				tStop = System.nanoTime();
				System.out.println("() Elapsed Time: " + new java.text.DecimalFormat("00,000,000,000").format(tStop - tStart));
			}
			System.out.println(val);
			System.out.println("count: "+count);


			val = 0;
			i = LOOPS;
			count = 0;
			final int dummyValue = (int)System.currentTimeMillis();
			for(int r = 0; r < RUNS; r++)
			{
				tStart = System.nanoTime();
				while(i --> 0)
				{
					val += dummyValue;
					count++;
				}
				tStop = System.nanoTime();
				System.out.println("d  Elapsed Time: " + new java.text.DecimalFormat("00,000,000,000").format(tStop - tStart));
			}
			System.out.println(val);
			System.out.println("count: "+count);
		}
	}
}

package one.microstream.persistence.test;

import one.microstream.persistence.internal.DefaultObjectRegistry;


public class MainTestObjectRegistry
{
	private static final int RUNS = 100;
	private static final int COUNT = 1_000_000;



	public static void main(final String[] args)
	{
		final DefaultObjectRegistry reg = DefaultObjectRegistry.New(COUNT);

		final Object[] objects = new Object[COUNT];
		for(int i = 0; i < objects.length; i++)
		{
			objects[i] = new Object();
		}


		for(int r = RUNS; r --> 0;)
		{
			long objectId = 1_000_000_000_000L;
			long tStart, tStop;
			reg.clear();
			System.gc();
//			reg.shrink();
			tStart = System.nanoTime();
			for(int i = 0; i < objects.length; i++)
			{
				reg.registerObject(++objectId, objects[i]);
			}
			tStop = System.nanoTime();
			System.out.println("Elapsed Time: " + new java.text.DecimalFormat("00,000,000,000").format(tStop - tStart));
//			reg.analyze();
//			System.out.println(reg.analyzeOidSlots());
//			System.out.println(reg.analyzeRefSlots());

		}


//		r.iterateEntries(e.key()+" -> "+e.value());

	}
}

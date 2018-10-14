package net.jadoth.persistence.test;

import net.jadoth.swizzling.internal.SwizzleRegistryGrowingRange;


public class MainTestSwizzleRegistry
{
	private static final int RUNS = 100;
	private static final int COUNT = 1_000_000;



	public static void main(final String[] args)
	{
		final SwizzleRegistryGrowingRange reg = new SwizzleRegistryGrowingRange(COUNT);

		final Object[] objects = new Object[COUNT];
		for(int i = 0; i < objects.length; i++)
		{
			objects[i] = new Object();
		}


		for(int r = RUNS; r --> 0;)
		{
			long oid = 1_000_000_000_000L;
			long tStart, tStop;
			reg.clear();
			System.gc();
//			reg.shrink();
			reg.registerType(10L, Object.class);
			tStart = System.nanoTime();
			for(int i = 0; i < objects.length; i++)
			{
				reg.registerObject(++oid, objects[i]);
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

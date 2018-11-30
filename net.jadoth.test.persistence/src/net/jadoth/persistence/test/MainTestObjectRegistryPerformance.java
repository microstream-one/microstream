package net.jadoth.persistence.test;

import net.jadoth.collections.types.XGettingTable;
import net.jadoth.hashing.HashStatistics;
import net.jadoth.low.XVM;
import net.jadoth.persistence.internal.ObjectRegistryCrazyArrays;
import net.jadoth.persistence.types.Persistence;
import net.jadoth.persistence.types.PersistenceObjectRegistry;


public class MainTestObjectRegistryPerformance
{
	private static final int COUNT = 10_000_000;

	// -XX:-UseCompressedOops -XX:+PrintGC

	public static void main(final String[] args)
	{
		final ObjectRegistryCrazyArrays reg = ObjectRegistryCrazyArrays.New(0.5f);
//		final ObjectRegistryGrowingRange reg = ObjectRegistryGrowingRange.New();

		final Object[] objects = new Object[COUNT];
		for(int i = 0; i < objects.length; i++)
		{
			objects[i] = new Object();
		}
		
		System.out.println(
			"Reference byte size = " + XVM.byteSizeReference() + ", array header size = " + XVM.byteSizeArrayObject(0)
		);

		long r = 0;
		while(true)
//		for(int j = 1; j --> 0;)
		{
			long oid = Persistence.defaultStartObjectId();
			long tStart, tStop;
			reg.truncate();
			System.gc();
			tStart = System.nanoTime();
			for(int i = 0; i < objects.length; i++)
			{
				reg.registerObject(++oid, objects[i]);
			}
			tStop = System.nanoTime();
			System.out.println("#" + (++r) + " Elapsed Time: " + new java.text.DecimalFormat("00,000,000,000").format(tStop - tStart));
			System.gc();
//			printObjectRegistryStatistics(reg);
		}

	}
	
	static void printObjectRegistryStatistics(final PersistenceObjectRegistry reg)
	{
		final XGettingTable<String, ? extends HashStatistics> stats = reg.createHashStatistics();
		stats.iterate(e ->
		{
			System.out.println(e.key());
			System.out.println(e.value());
		});
	}
}

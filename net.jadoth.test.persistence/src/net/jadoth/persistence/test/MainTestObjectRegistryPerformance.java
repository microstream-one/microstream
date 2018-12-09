package net.jadoth.persistence.test;

import net.jadoth.collections.types.XGettingTable;
import net.jadoth.hashing.HashStatistics;
import net.jadoth.persistence.internal.DefaultObjectRegistry;
import net.jadoth.persistence.types.Persistence;
import net.jadoth.persistence.types.PersistenceObjectRegistry;


public class MainTestObjectRegistryPerformance
{
	private static final int RUNS  = 100;
	private static final int COUNT = 1_000_000;

	// -XX:-UseCompressedOops -XX:+PrintGC

	public static void main(final String[] args)
	{
		DefaultObjectRegistry.printEntryInstanceSizeInfo();
		final DefaultObjectRegistry reg = DefaultObjectRegistry.New(); // [0.75f; 2.0f] Rest is nonsense.
		
		final Object[] objects = new Object[COUNT];
		for(int i = 0; i < objects.length; i++)
		{
			objects[i] = new Object();
		}

		for(int r = 1; r <= RUNS; r++)
		{
			long oid = Persistence.defaultStartObjectId();
			long tStart, tStop;
			reg.clear();
			System.gc();
			tStart = System.nanoTime();
			for(int i = 0; i < objects.length; i++)
			{
				reg.registerObject(++oid, objects[i]);
			}
			tStop = System.nanoTime();
			System.out.println(
				"#" + r
				+ " Elapsed Time: " + new java.text.DecimalFormat("00,000,000,000").format(tStop - tStart)
				+ " (" + (tStop - tStart) / COUNT + " per entry)"
			);
			System.gc();
			printObjectRegistryStatistics(reg);
		}

	}
	
	static void printObjectRegistryStatistics(final PersistenceObjectRegistry reg)
	{
		final XGettingTable<String, ? extends HashStatistics> stats = reg.createHashStatistics();
		System.out.println("---");
		System.out.println(HashStatistics.class.getSimpleName());
		System.out.println("---");
		stats.iterate(e ->
		{
			System.out.println(e.key());
			System.out.println(e.value());
		});
	}
}

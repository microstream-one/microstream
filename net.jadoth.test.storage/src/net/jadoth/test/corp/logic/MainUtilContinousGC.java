package net.jadoth.test.corp.logic;

import static net.jadoth.meta.XDebug.debugln;

import net.jadoth.concurrency.XThreads;
import net.jadoth.math.XMath;
import net.jadoth.storage.types.EmbeddedStorage;

public class MainUtilContinousGC extends MainTestStorageExample
{
	static final int  RUNS = 1000;

	public static void main(final String[] args)
	{
		final Object cc = EmbeddedStorage.root();
		for(int i = 0; i < RUNS; i++)
		{
			// (24.06.2015 TM)TODO: adjust times according to entity count and housekeeping budgets
			XThreads.sleep((2 + XMath.random(4)) * 1000);
			debugln(i+" storing ...");
			STORAGE.store(cc);
			debugln(i+" done.");
		}
		System.exit(0);
	}

}

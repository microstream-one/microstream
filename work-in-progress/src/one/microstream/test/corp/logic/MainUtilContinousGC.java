package one.microstream.test.corp.logic;

import one.microstream.concurrency.XThreads;
import one.microstream.math.XMath;
import one.microstream.meta.XDebug;
import one.microstream.storage.types.EmbeddedStorage;
import one.microstream.storage.types.EmbeddedStorageManager;

public class MainUtilContinousGC
{
	// creates and start an embedded storage manager with all-default-settings.
	static final EmbeddedStorageManager STORAGE = EmbeddedStorage.start();
	
	static final int  RUNS = 1000;

	public static void main(final String[] args)
	{
		final Object root = STORAGE.root();
		for(int i = 0; i < RUNS; i++)
		{
			// (24.06.2015 TM)TODO: adjust times according to entity count and housekeeping budgets
			XThreads.sleep((2 + XMath.random(4)) * 1000);
			XDebug.println(i+" storing ...");
			STORAGE.store(root);
			XDebug.println(i+" done.");
		}
		System.exit(0);
	}

}

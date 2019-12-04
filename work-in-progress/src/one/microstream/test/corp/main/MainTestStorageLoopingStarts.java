package one.microstream.test.corp.main;

import one.microstream.concurrency.XThreads;
import one.microstream.meta.XDebug;
import one.microstream.storage.types.EmbeddedStorage;
import one.microstream.storage.types.EmbeddedStorageManager;
import one.microstream.test.corp.logic.Test;


public class MainTestStorageLoopingStarts
{
	public static void main(final String[] args)
	{
		Test.clearDefaultStorageDirectory();
		
		runLoopingStarts(200, 10, 400);
		
		System.exit(0);
	}
	
	static void runLoopingStarts(final int amount, final int gcModulo, final int sleepTime)
	{
		final Object root = Test.generateModelData(20000);
		
		EmbeddedStorageManager storage;
		for(int i = 1; i <= amount; i++)
		{
			storage = EmbeddedStorage.start(root);
			storage.shutdown();
			
			if(i % gcModulo == 0)
			{
				XDebug.print("#" + i + "/" + amount + " GC...");
				System.gc();
				System.out.println(" done.");
			}
			if(sleepTime > 0)
			{
				XThreads.sleep(sleepTime);
			}
		}
	}
	
}

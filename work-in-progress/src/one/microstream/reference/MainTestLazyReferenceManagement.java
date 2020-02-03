package one.microstream.reference;

import java.util.Date;

import one.microstream.collections.BulkList;
import one.microstream.collections.types.XList;
import one.microstream.math.XMath;
import one.microstream.storage.types.EmbeddedStorage;
import one.microstream.storage.types.EmbeddedStorageManager;
import one.microstream.test.corp.logic.Test;


public class MainTestLazyReferenceManagement
{
	static
	{
		Test.clearDefaultStorageDirectory();
		
		LazyReferenceManager.set(LazyReferenceManager.New(
			Lazy.Checker(
				Lazy.Checker.Defaults.defaultTimeout() / 10,
				Lazy.Checker.Defaults.defaultMemoryQuota()
			),
			10_000,
			1_000_000
		));
	}
	
	//!\\ values must fit the configured JVM memory limit!
	static final int ROOT_SIZE         = 100;
	static final int ELEMENT_BYTE_SIZE = 1<<20;
	static final int ELEMENT_LENGTH    = ELEMENT_BYTE_SIZE / Long.BYTES;
	
	static final XList<Lazy<long[]>>    APP_ROOT = BulkList.New();
	static final EmbeddedStorageManager STORAGE  = EmbeddedStorage.start(APP_ROOT);

	public static void main(final String[] args)
	{
		System.out.println("Start at " + new Date());
		if(APP_ROOT.isEmpty())
		{
			// first execution enters here (database creation)
			Test.print("Model data required.");
			fillRootAndStore();
			
//			Test.print("Exporting data ...");
//			TestImportExport.testExport(STORAGE, Test.provideTimestampedDirectory("testExport"));
//			Test.print("Data export completed.");
		}
		else
		{
			// subsequent executions enter here (database reading)
			Test.printInitializationTime(STORAGE);
			Test.printOperationModeTime(STORAGE);
			Test.print("Model data loaded.");
			Test.print("Root instance: " + APP_ROOT);
			
//			Test.print("Exporting data ...");
//			TestImportExport.testExport(STORAGE, Test.provideTimestampedDirectory("testExport"));
//			Test.print("Data export completed.");
		}
		
		test();
		
		// no shutdown required, the storage concept is inherently crash-safe
		System.exit(0);
	}
	
	static void fillRootAndStore()
	{
		for(int i = 0; i < ROOT_SIZE; i++)
		{
			LazyReferenceManager.get().cleanUp();
			
			APP_ROOT.add(Lazy.Reference(createElement(i)));
			Test.print("Storing " + i + " ...");
			STORAGE.storeRoot();
			Test.print("Storing completed.");
		}
		
	}
	
	static long[] createElement(final int e)
	{
		final long[] array = new long[ELEMENT_LENGTH];
		for(int i = 0; i < array.length; i++)
		{
			array[i] = e * 1_000_000 + i;
		}
		
		return array;
	}
	
	static void test()
	{
		for(int r = 0; r < 100; r++)
		{
			final int i = XMath.random(ROOT_SIZE);
			final long[] element = APP_ROOT.at(i).get();
			System.out.println("Element at " + i + " first value = " + element[0]);
		}
	}
	
}

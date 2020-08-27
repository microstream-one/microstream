package one.microstream.test.corp.main;

import one.microstream.collections.BulkList;
import one.microstream.collections.types.XList;
import one.microstream.storage.types.EmbeddedStorage;
import one.microstream.storage.types.EmbeddedStorageManager;
import one.microstream.storage.types.StorageEntityCache;
import one.microstream.test.corp.logic.Test;
import one.microstream.test.corp.logic.TestImportExport;


public class MainTestStorageIssuedGC
{
	static
	{
//		Test.clearDefaultStorageDirectory();
	}
	
	static final XList<String> ROOT = BulkList.New();
	
	// creates and starts an embedded storage manager with all-default-settings.
	static final EmbeddedStorageManager STORAGE = EmbeddedStorage.start(ROOT);

	public static void main(final String[] args)
	{
		// object graph with root either loaded on startup from an existing DB or required to be generated.
		if(ROOT.isEmpty())
		{
			// first execution enters here (database creation)

			Test.print("Model data required.");
			
			populate(ROOT, 100_000);
			
			Test.print("Storing 1 ...");
			STORAGE.storeRoot();
			Test.print("Storing 1 completed.");
			
			ROOT.clear();
			ROOT.add("Just one element");

			Test.print("Storing 2 ...");
			STORAGE.storeRoot();
			Test.print("Storing 2 completed.");
			
			
			Test.print("Exporting data ...");
			TestImportExport.testExport(STORAGE, Test.provideTimestampedDirectory("testExport"));
			Test.print("Data export completed.");
		}
		else
		{
			// subsequent executions enter here (database reading)

			Test.printInitializationTime(STORAGE);
			Test.printOperationModeTime(STORAGE);
			Test.print("Model data loaded.");
			Test.print("Root instance: " + STORAGE.root());

			Test.print("Issuing full garbage collection.");
			StorageEntityCache.Default.DEBUG_setGarbageCollectionEnabled(true);
			STORAGE.issueFullGarbageCollection();
			StorageEntityCache.Default.DEBUG_setGarbageCollectionEnabled(false);
			
			Test.print("Issuing full file check.");
			STORAGE.issueFullFileCheck();
			
			Test.print("Exporting data ...");
			TestImportExport.testExport(STORAGE, Test.provideTimestampedDirectory("testExport"));
			Test.print("Data export completed.");
		}
		
		System.exit(0);
	}
		
	static void populate(final XList<String> strings, final int count)
	{
		for(int i = 0; i < count; i++)
		{
			strings.add("SomeStringValue_" + i);
		}
	}
		
}

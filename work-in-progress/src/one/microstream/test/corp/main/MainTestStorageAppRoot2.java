package one.microstream.test.corp.main;

import one.microstream.storage.types.EmbeddedStorage;
import one.microstream.storage.types.EmbeddedStorageManager;
import one.microstream.test.corp.logic.Test;
import one.microstream.test.corp.logic.TestImportExport;


public class MainTestStorageAppRoot2
{
	static
	{
//		Test.clearDefaultStorageDirectory();
	}

	// Option 2: Implicit application root set after startup (Object typed)
	static final EmbeddedStorageManager STORAGE = EmbeddedStorage.start();

	public static void main(final String[] args)
	{
		// object graph with root either loaded on startup from an existing DB or required to be generated.
		if(STORAGE.root() == null)
		{
			// first execution enters here (database creation)
			Test.print("Model data required.");
			STORAGE.setRoot(new AppRoot().set(new Value(5)));
			
			Test.print("Storing ...");
			STORAGE.storeRoot();
			Test.print("Storing completed.");
			
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
			
			Test.print("Exporting data ...");
			TestImportExport.testExport(STORAGE, Test.provideTimestampedDirectory("testExport"));
			Test.print("Data export completed.");
		}
		
		STORAGE.shutdown();
		
		// no shutdown required, the storage concept is inherently crash-safe
		System.exit(0);
	}
	
}

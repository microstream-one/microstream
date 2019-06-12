package one.microstream.test.corp.logic;

import one.microstream.storage.types.EmbeddedStorage;
import one.microstream.storage.types.EmbeddedStorageManager;


public class MainTestStorageExample
{
	// creates and start an embedded storage manager with all-default-settings.
	static final EmbeddedStorageManager STORAGE = EmbeddedStorage.start();

	public static void main(final String[] args)
	{
		// object graph with root either loaded on startup from an existing DB or required to be generated.
		if(STORAGE.root().get() == null)
		{
			// first execution enters here (database creation)

			Test.print("Model data required.");
			STORAGE.root().set(Test.generateModelData(1_000));
			
			Test.print("Storing ...");
			STORAGE.store(STORAGE.root());
			Test.print("Storing completed.");
		}
		else
		{
			// subsequent executions enter here (database reuse)

			Test.printInitializationTime(STORAGE);
			Test.printOperationModeTime(STORAGE);
			Test.print("Model data loaded.");
			Test.print("Root instance: " + STORAGE.root().get());
			
			Test.print("Exporting data ...");
			TestImportExport.testExport(STORAGE, Test.provideTimestampedDirectory("testExport"));
			Test.print("Data export completed.");
		}
		
		// no shutdown required, the storage concept is inherently crash-safe
		System.exit(0);
	}
	
}

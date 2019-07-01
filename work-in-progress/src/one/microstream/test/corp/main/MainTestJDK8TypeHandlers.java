package one.microstream.test.corp.main;

import one.microstream.collections.old.OldCollections;
import one.microstream.jdk8.java.util.BinaryHandlersJDK8;
import one.microstream.storage.types.EmbeddedStorage;
import one.microstream.storage.types.EmbeddedStorageManager;
import one.microstream.test.corp.logic.Test;
import one.microstream.test.corp.logic.TestImportExport;


public class MainTestJDK8TypeHandlers
{
	// creates and start an embedded storage manager with all-default-settings.
	static final EmbeddedStorageManager STORAGE = EmbeddedStorage.Foundation()
		.onConnectionFoundation(f -> BinaryHandlersJDK8.registerJDK8TypeHandlers(f))
		.createEmbeddedStorageManager()
	;

	public static void main(final String[] args)
	{
		// object graph with root either loaded on startup from an existing DB or required to be generated.
		if(STORAGE.root() == null)
		{
			// first execution enters here (database creation)

			Test.print("Model data required.");
			STORAGE.setRoot(OldCollections.ArrayList("A", "B", "C"));
			
			Test.print("Storing ...");
			STORAGE.storeRoot();
			Test.print("Storing completed.");
		}
		else
		{
			// subsequent executions enter here (database reuse)

			Test.printInitializationTime(STORAGE);
			Test.printOperationModeTime(STORAGE);
			Test.print("Model data loaded.");
			Test.print("Root instance: " + STORAGE.root());
			
			Test.print("Exporting data ...");
			TestImportExport.testExport(STORAGE, Test.provideTimestampedDirectory("testExport"));
			Test.print("Data export completed.");
		}
		
		// no shutdown required, the storage concept is inherently crash-safe
		System.exit(0);
	}
	
}

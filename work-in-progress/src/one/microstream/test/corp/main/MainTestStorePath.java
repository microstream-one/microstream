package one.microstream.test.corp.main;

import java.nio.file.Paths;

import one.microstream.storage.types.EmbeddedStorage;
import one.microstream.storage.types.EmbeddedStorageManager;
import one.microstream.test.corp.logic.Test;
import one.microstream.test.corp.logic.TestImportExport;


public class MainTestStorePath
{
	static
	{
//		XDebug.deleteAllFiles(new File(StorageFileProvider.Defaults.defaultStorageDirectory()), true);
//		XMemory.setMemoryAccessor(MemoryAccessorGeneric.New(JdkInternals.InstantiatorBlank()));
	}
	
	// creates and starts an embedded storage manager with all-default-settings.
	static final EmbeddedStorageManager STORAGE = EmbeddedStorage.start();

	public static void main(final String[] args)
	{
		// object graph with root either loaded on startup from an existing DB or required to be generated.
		if(STORAGE.root() == null)
		{
			// first execution enters here (database creation)

			Test.print("Model data required.");
			// (27.11.2019 TM)FIXME: Won't work for now! See comment in BinaryHandlerPath.
			STORAGE.setRoot(Paths.get("D:/downloads/Java/testString.txt"));
			
			Test.print("Storing ...");
			STORAGE.storeRoot();
			Test.print("Storing completed.");
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
		
		// no shutdown required, the storage concept is inherently crash-safe
		System.exit(0);
	}
	
}

package one.microstream.test.corp.main;

import java.util.Locale;

import one.microstream.storage.types.EmbeddedStorage;
import one.microstream.storage.types.EmbeddedStorageManager;
import one.microstream.test.corp.logic.Test;
import one.microstream.test.corp.logic.TestImportExport;


public class MainTestStorageLocaleHandler
{
	static
	{
//		XDebug.deleteAllFiles(XIO.Path(StorageFileProvider.Defaults.defaultStorageDirectory()), true);
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
			STORAGE.setRoot(generateModelData());
			printData();
			
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
			printData();
			
			Test.print("Exporting data ...");
			TestImportExport.testExport(STORAGE, Test.provideTimestampedDirectory("testExport"));
			Test.print("Data export completed.");
		}
		
		// no shutdown required, the storage concept is inherently crash-safe
		System.exit(0);
	}
	
	static Locale[] generateModelData()
	{
		return Locale.getAvailableLocales();
	}
	
	static void printData()
	{
		final Locale[] root = (Locale[])STORAGE.root();
		
		System.out.println(root);
		for(int i = 0; i < root.length; i++)
		{
			System.out.println("#" + i + ": " + root[i]);
		}
	}
	
}

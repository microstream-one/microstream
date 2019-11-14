package one.microstream.test.corp.main;

import one.microstream.memory.MemoryAccessorGeneric;
import one.microstream.memory.XMemory;
import one.microstream.memory.sun.JdkInternals;
import one.microstream.storage.types.EmbeddedStorage;
import one.microstream.storage.types.EmbeddedStorageManager;
import one.microstream.test.corp.logic.Test;
import one.microstream.test.corp.logic.TestImportExport;


public class MainTestStorageExampleBigArrays
{
	static
	{
//		XDebug.deleteAllFiles(new File(StorageFileProvider.Defaults.defaultStorageDirectory()), true);
		XMemory.setMemoryAccessor(
			MemoryAccessorGeneric.New(
				JdkInternals.InstantiatorBlank(),
				JdkInternals.DirectBufferDeallocator()
			)
		);
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
			STORAGE.setRoot(generateModelData(100, 1_000_000));
			
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
	
	static Object[] generateModelData(final int arrayCount, final int arraySize)
	{
		final Object[] arrays = new Object[arrayCount];
		for(int i = 0; i < arrayCount; i++)
		{
			final int[] array = new int[arraySize / Integer.BYTES];
			for(int b = 0; b < array.length; b++)
			{
				array[b] = b;
			}
			arrays[i] = array;
		}
		
		return arrays;
	}
	
}

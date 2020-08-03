package one.microstream.test.corp.main;

import one.microstream.afs.ADirectory;
import one.microstream.afs.AFS;
import one.microstream.afs.nio.NioFileSystem;
import one.microstream.collections.HashEnum;
import one.microstream.io.XIO;
import one.microstream.reference.Lazy;
import one.microstream.storage.types.EmbeddedStorage;
import one.microstream.storage.types.EmbeddedStorageManager;
import one.microstream.storage.types.StorageFileNameProvider;
import one.microstream.test.corp.logic.Test;
import one.microstream.test.corp.logic.TestImportExport;


public class MainTestStorageImportData
{
	static
	{
//		XDebug.deleteAllFiles(XIO.Path(StorageFileProvider.Defaults.defaultStorageDirectory()), true);
	}
	
	/* (14.11.2019 TM)NOTE:
	 * This is a little tricky to use, but it's only a development bugfix validation test,
	 * not a nicely written test scenario or a user example.
	 * 
	 * Use as follow:
	 * - Delete all "storage" and "export"-related directories in the working directory, if present.
	 * - Run the program for the first time (#A1). The array is created, printed and stored (Ids #0 to #99)
	 * - Run #A2: The data gets exported and converted to CSV (the CSVs are irrelevant for the test)
	 * - copy the two .dat files for "SomeData" (class itself and array of it) to the directory "export"
	 * - delete the storage directory
	 * - Change the ID_OFFSET to something easily recognizable. Like 1000 or so.
	 * - Run the program again (#B1). The array is created, printed and stored (Ids #1000 to #1099)
	 * - Run #B2: The data gets imported and printed.
	 * 
	 * If the console shows Ids #0 to #99, it means the import of has replaced the B-generation entities
	 * with the import A-generation entities.
	 * If the console shows Ids #1000 to #1099 (and no exception), it means the import did nothing.
	 */
	
	static final EmbeddedStorageManager STORAGE = EmbeddedStorage.start();
	
	static final ADirectory EXPORT_DIRECTORY = NioFileSystem.New().ensureDirectory(XIO.Path("export")); // root is working directory
	
	static final int ID_OFFSET = 0; // change to 1000 or so
	

	static final String DAT = StorageFileNameProvider.Defaults.defaultDataFileSuffix();

	public static void main(final String[] args)
	{
		// object graph with root either loaded on startup from an existing DB or required to be generated.
		if(STORAGE.root() == null)
		{
			Test.print("Model data required.");
			STORAGE.setRoot(generateModelData(100));
			
			printData();
			
			Test.print("Storing ...");
			STORAGE.storeRoot();
			Test.print("Storing completed.");
		}
		else if(!EXPORT_DIRECTORY.listFiles().isEmpty())
		{
			Test.print("Importing data ...");
			STORAGE.importFiles(
				AFS.listFiles(EXPORT_DIRECTORY, f -> DAT.equals(f.type()), HashEnum.New())
			);
			Test.print("Data import completed.");
			
			printData();
		}
		else
		{
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
	
	static void printData()
	{
		@SuppressWarnings("unchecked")
		final SomeData[] root = ((Lazy<SomeData[]>)STORAGE.root()).get();
		
		System.out.println(root);
		for(int i = 0; i < root.length; i++)
		{
			System.out.println("#" + i + ": " + root[i]);
		}
	}
	
	static Lazy<SomeData[]> generateModelData(final int count)
	{
		final SomeData[] arrays = new SomeData[count];
		for(int i = 0; i < count; i++)
		{
			arrays[i] = new SomeData(ID_OFFSET + i);
		}
		
		return Lazy.Reference(arrays);
	}
	
}

final class SomeData
{
	int id;

	SomeData(final int id)
	{
		super();
		this.id = id;
	}
	
	@Override
	public String toString()
	{
		return this.getClass().getSimpleName() + " #" + this.id;
	}
	
}
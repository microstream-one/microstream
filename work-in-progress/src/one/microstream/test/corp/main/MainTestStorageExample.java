package one.microstream.test.corp.main;

import one.microstream.io.XIO;
import one.microstream.meta.XDebug;
import one.microstream.persistence.internal.PersistenceTypeDictionaryFileHandlerArchiving;
import one.microstream.storage.types.EmbeddedStorage;
import one.microstream.storage.types.EmbeddedStorageManager;
import one.microstream.storage.types.Storage;
import one.microstream.storage.types.StorageBackupSetup;
import one.microstream.storage.types.StorageFileProvider;
import one.microstream.test.corp.logic.Test;
import one.microstream.test.corp.logic.TestImportExport;


public class MainTestStorageExample
{
	static final String DIRECTORY_STORAGE   = StorageFileProvider.Defaults.defaultStorageDirectory();
	static final String DIRECTORY_BACKUP    = DIRECTORY_STORAGE + "/backup";
	static final String DIRECTORY_DELETED   = DIRECTORY_BACKUP  + "/deleted";
	static final String DIRECTORY_TRUNCATED = DIRECTORY_BACKUP  + "/truncated";
	
	static
	{
		XDebug.deleteAllFiles(XIO.Path(StorageFileProvider.Defaults.defaultStorageDirectory()), true);
//		XMemory.setMemoryAccessor(MemoryAccessorGeneric.New(JdkInternals.InstantiatorBlank()));
	}
	
	// creates and starts an embedded storage manager with all-default-settings.
	static final EmbeddedStorageManager STORAGE = EmbeddedStorage
		.Foundation(
			Storage.ConfigurationBuilder()
			.setDataFileEvaluator(Storage.DataFileEvaluator(1000, 200_000))
			.setBackupSetup(
				// the only necessary part to activate and configure backupping.
				StorageBackupSetup.New(
					Storage
					.FileProviderBuilder()
					.setBaseDirectory(DIRECTORY_BACKUP)
					.setDeletionDirectory(DIRECTORY_DELETED)
					.setTruncationDirectory(DIRECTORY_TRUNCATED)
					.setFileHandlerCreator(PersistenceTypeDictionaryFileHandlerArchiving::New)
					.createFileProvider()
				)
			)
		)
		.start()
	;

	public static void main(final String[] args)
	{
		// object graph with root either loaded on startup from an existing DB or required to be generated.
		if(STORAGE.root() == null)
		{
			// first execution enters here (database creation)

			Test.print("Model data required.");
			STORAGE.setRoot(Test.generateModelData(10000));
			
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
		
		STORAGE.shutdown();
		
//		while(STORAGE.isActive())
//		{
//			XThreads.sleep(10);
//		}
//		System.err.println(STORAGE.isActive());
//
//		STORAGE = null;
//		System.gc();
		
//		XThreads.sleep(500);
		
		XDebug.deleteAllFiles(XIO.Path(StorageFileProvider.Defaults.defaultStorageDirectory()), true);
		
		// no shutdown required, the storage concept is inherently crash-safe
		System.exit(0);
	}
	
}

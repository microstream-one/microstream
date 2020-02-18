package one.microstream.test.corp.main;

import one.microstream.X;
import one.microstream.concurrency.XThreads;
import one.microstream.io.XIO;
import one.microstream.storage.types.EmbeddedStorage;
import one.microstream.storage.types.EmbeddedStorageManager;
import one.microstream.storage.types.Storage;
import one.microstream.storage.types.StorageBackupSetup;
import one.microstream.test.corp.logic.Test;


public class MainTestBackupStoringMinimal
{
	static
	{
		Test.clearDefaultStorageDirectory();
	}
		
	static final EmbeddedStorageManager STORAGE = EmbeddedStorage
		.Foundation(
			Storage.ConfigurationBuilder()
			.setDataFileEvaluator(
				// just to make testing more convenient. Not necessary for the backup itself.
				Storage.DataFileEvaluator(1024, 2048, 0.7)
			)
			.setBackupSetup(
				// the only necessary part to activate and configure backupping.
				StorageBackupSetup.New(
					XIO.Path("backup")
//					Storage
//					.FileProviderBuilder()
//					.setBaseDirectory("storageBackup")
//					.setFileHandlerCreator(PersistenceTypeDictionaryFileHandlerArchiving::New)
//					.createFileProvider()
				)
			)
		)
		.start()
	;
	
	static Object[] createArray(final int size)
	{
		return X.Array(Object.class, size, i -> "Element" + i);
	}

	public static void main(final String[] args)
	{
//		printTransactionsFiles();
		final Object[] array = createArray(100);
		STORAGE.setRoot(array);
		Test.print("STORAGE: storing ...");
		STORAGE.storeRoot();
		
		for(int i = 0; i < 10; i++)
		{
			XThreads.sleep(500);
			STORAGE.store(array);
		}
		STORAGE.issueFullFileCheck();
		XThreads.sleep(1000);
		System.exit(0); // no shutdown required, the storage concept is inherently crash-safe
	}
	
}

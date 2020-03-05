package one.microstream.test.corp.main;

import one.microstream.X;
import one.microstream.concurrency.XThreads;
import one.microstream.io.XIO;
import one.microstream.meta.XDebug;
import one.microstream.persistence.internal.PersistenceTypeDictionaryFileHandler;
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
		XDebug.deleteAllFiles(XIO.Path("storageBackup"));
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
				StorageBackupSetup.New(XIO.Path("storageBackup"))
			)
		)
		// priv#227 testing
		.onConnectionFoundation(cf ->
			cf.setTypeDictionaryIoHandler(PersistenceTypeDictionaryFileHandler.New(
				XIO.Path("storage", "ExplicitTypeDictionary.ptd")
			))
		)
		.start()
	;
	
	static Object[] createArray(final int size)
	{
		return X.Array(Object.class, size, i -> "Element" + i);
	}

	public static void main(final String[] args)
	{
		final Object[] array = createArray(100);
		STORAGE.setRoot(array);
		Test.print("STORAGE: storing root ...");
		STORAGE.storeRoot();
		
		for(int i = 1; i <= 10; i++)
		{
			XThreads.sleep(300);
			Test.print("STORAGE: storing array run #" + i + " / " + 10);
			STORAGE.store(array);
		}

		XThreads.sleep(500);
		STORAGE.issueFullFileCheck();
		XThreads.sleep(500);
		System.exit(0);
	}
	
}

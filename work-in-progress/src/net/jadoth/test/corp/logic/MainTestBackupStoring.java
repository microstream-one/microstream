package net.jadoth.test.corp.logic;

import net.jadoth.X;
import net.jadoth.concurrency.XThreads;
import net.jadoth.persistence.internal.PersistenceTypeDictionaryFileHandlerArchiving;
import net.jadoth.storage.types.EmbeddedStorage;
import net.jadoth.storage.types.EmbeddedStorageManager;
import net.jadoth.storage.types.Storage;
import net.jadoth.storage.types.StorageBackupSetup;
import net.jadoth.storage.types.StorageDataFileValidator;
import net.jadoth.storage.types.StorageFileProvider;


public class MainTestBackupStoring
{
	static final String DIRECTORY_STORAGE   = StorageFileProvider.Defaults.defaultStorageDirectory();
	static final String DIRECTORY_BACKUP    = DIRECTORY_STORAGE + "/backup";
	static final String DIRECTORY_DELETED   = DIRECTORY_BACKUP  + "/deleted";
	static final String DIRECTORY_TRUNCATED = DIRECTORY_BACKUP  + "/truncated";
	
	// creates and start an embedded storage manager with all-default-settings.
	static final EmbeddedStorageManager STORAGE = EmbeddedStorage
		.Foundation(
			Storage.ConfigurationBuilder()
			.setFileEvaluator(
				// just to make testing more convenient. Not necessary for the backup itself.
				Storage.DataFileEvaluator(100, 1_000, 0.7)
			)
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
		.setDataFileValidatorCreator(
			// just to make testing more convenient. Not necessary for the backup itself.
			StorageDataFileValidator.CreatorDebugLogging()
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
		STORAGE.root().set(array);
		Test.print("STORAGE: storing ...");
		STORAGE.store(STORAGE.root());
		
		for(int i = 0; i < 1; i++)
		{
			XThreads.sleep(500);
			STORAGE.store(array);
		}
//		STORAGE.issueFullFileCheck();
		XThreads.sleep(1000);
		System.exit(0); // no shutdown required, the storage concept is inherently crash-safe
	}
	

		
}

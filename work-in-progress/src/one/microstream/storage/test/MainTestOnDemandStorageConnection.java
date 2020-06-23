package one.microstream.storage.test;

import one.microstream.afs.ADirectory;
import one.microstream.afs.nio.NioFileSystem;
import one.microstream.chars.XChars;
import one.microstream.storage.types.EmbeddedStorageFoundation;
import one.microstream.storage.types.EmbeddedStorageManager;
import one.microstream.storage.types.StorageBackupSetup;
import one.microstream.storage.types.StorageConfiguration;
import one.microstream.storage.types.StorageLiveFileProvider;


// derived from https://forum.microstream.one/?qa=23/storagefoundation-doesnt-default-storageconnfoundation
public class MainTestOnDemandStorageConnection
{
	public static void main(final String[] args)
	{
		final EmbeddedStorageManager storageManager = createEmbeddedStorageManager(
			NioFileSystem.Directory("testStorage"),
			NioFileSystem.Directory("testBackup")
		);
		
		System.out.println("Created: " + XChars.systemString(storageManager));
	}
	
	static EmbeddedStorageManager createEmbeddedStorageManager(final ADirectory backupPath, final ADirectory dbPath)
	{
		final EmbeddedStorageManager storageManager = EmbeddedStorageFoundation.New()
			.setConfiguration(
				StorageConfiguration.Builder()
					.setBackupSetup(StorageBackupSetup.New(backupPath))
					.setStorageFileProvider(StorageLiveFileProvider.New(dbPath))
					.createConfiguration()
			)
			.createEmbeddedStorageManager()
		;
		
		return storageManager;
	}
	
}

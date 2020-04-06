package one.microstream.storage.test;

import java.nio.file.Path;

import one.microstream.chars.XChars;
import one.microstream.io.XIO;
import one.microstream.storage.types.EmbeddedStorageFoundation;
import one.microstream.storage.types.EmbeddedStorageManager;
import one.microstream.storage.types.StorageBackupSetup;
import one.microstream.storage.types.StorageConfiguration;
import one.microstream.storage.types.StorageFileProvider;


// derived from https://forum.microstream.one/?qa=23/storagefoundation-doesnt-default-storageconnfoundation
public class MainTestOnDemandStorageConnection
{
	public static void main(final String[] args)
	{
		final EmbeddedStorageManager storageManager = createEmbeddedStorageManager(
			XIO.Path("testStorage"),
			XIO.Path("testBackup")
		);
		
		System.out.println("Created: " + XChars.systemString(storageManager));
	}
	
	static EmbeddedStorageManager createEmbeddedStorageManager(final Path backupPath, final Path dbPath)
	{
		final EmbeddedStorageManager storageManager = EmbeddedStorageFoundation.New()
			.setConfiguration(
				StorageConfiguration.Builder()
					.setBackupSetup(StorageBackupSetup.New(backupPath))
					.setStorageFileProvider(StorageFileProvider.New(dbPath))
					.createConfiguration()
			)
			.createEmbeddedStorageManager()
		;
		
		return storageManager;
	}
	
}

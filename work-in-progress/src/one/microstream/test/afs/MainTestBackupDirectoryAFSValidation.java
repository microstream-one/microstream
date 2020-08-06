package one.microstream.test.afs;

import java.nio.file.Paths;

import one.microstream.afs.nio.NioFileSystem;
import one.microstream.storage.types.StorageBackupSetup;
import one.microstream.storage.types.StorageConfiguration;
import one.microstream.storage.types.StorageLiveFileProvider;

public class MainTestBackupDirectoryAFSValidation
{
	public static void main(final String[] args)
	{
		final NioFileSystem fileSystem = NioFileSystem.New();

		final StorageConfiguration configuration = StorageConfiguration.Builder()
			.setStorageFileProvider(StorageLiveFileProvider.New(fileSystem.ensureDirectory(
				Paths.get(System.getProperty("user.home"), "logger-storage"))
			))
			.setBackupSetup(StorageBackupSetup.New(fileSystem.ensureDirectory(
				Paths.get(System.getProperty("user.home"), "logger-storage_backup"))
			))
			.createConfiguration()
		;
	}
}

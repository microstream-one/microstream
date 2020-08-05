package one.microstream.test.corp.main;

import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import one.microstream.afs.nio.NioFileSystem;
import one.microstream.storage.types.EmbeddedStorage;
import one.microstream.storage.types.EmbeddedStorageFoundation;
import one.microstream.storage.types.EmbeddedStorageManager;
import one.microstream.storage.types.StorageBackupSetup;
import one.microstream.storage.types.StorageConfiguration;
import one.microstream.storage.types.StorageLiveFileProvider;

public class MainTestStorageLoggingExperimental
{
	public static void main(final String[] args)
	{
		System.setProperty("flogger.backend_factory", "com.google.common.flogger.backend.log4j2.Log4j2BackendFactory#getInstance");
		//System.setProperty("log4j2.debug", "true");

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

		final EmbeddedStorageFoundation<?> foundation = EmbeddedStorage.Foundation(configuration);
		final EmbeddedStorageManager storage = foundation.start();
		
		final List<Object> list = new ArrayList<>();
		list.add("bla");
		list.add(LocalDateTime.now());
		list.add(new Blub("blub",13));

		storage.setRoot(list);
		storage.storeRoot();
		storage.shutdown();
	}


	static class Blub
	{
		String str;
		int val;

		Blub(final String str, final int val)
		{
			this.str = str;
			this.val = val;
		}
	}
	
}


package one.microstream.examples.filesystems.nio;

import java.nio.file.FileSystem;

import com.google.common.jimfs.Jimfs;

import one.microstream.afs.nio.types.NioFileSystem;
import one.microstream.storage.embedded.types.EmbeddedStorage;
import one.microstream.storage.embedded.types.EmbeddedStorageManager;
import one.microstream.storage.types.StorageLiveFileProvider;


public class NioFilesystemJimfs
{
	@SuppressWarnings("unused")
	public static void main(
		final String[] args
	)
	{
		// create jimfs filesystem
		final FileSystem             jimfs   = Jimfs.newFileSystem();
		
		// start storage with jimfs path
		final EmbeddedStorageManager storage = EmbeddedStorage.start(jimfs.getPath("storage"));
		storage.shutdown();
		
		// or create file provider with jimsfs filesytem for further configuration
		final NioFileSystem           fileSystem   = NioFileSystem.New(jimfs);
		final StorageLiveFileProvider fileProvider = StorageLiveFileProvider.Builder(fileSystem)
			.setDirectory(fileSystem.ensureDirectoryPath("storage"))
			.createFileProvider()
		;
	}
}

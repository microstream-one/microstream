package one.microstream.test.corp.main;

import java.io.File;

import one.microstream.meta.XDebug;
import one.microstream.storage.types.EmbeddedStorage;
import one.microstream.storage.types.EmbeddedStorageManager;


public class MainTestShutdownLockedFiles
{
	public static void main(final String[] args)
	{
		final File storageDirectory = new File("testStorage#167");
		final Root root = new Root();
		
		final EmbeddedStorageManager storage = EmbeddedStorage
			.Foundation(storageDirectory)
			.start(root)
		;
		
		root.name = "Root changed";
		storage.storeRoot();
		
		storage.shutdown();
		
		System.out.println("Storage is shut down.");
		
		// or delete manually via OS file browser instead
		XDebug.deleteAllFiles(storageDirectory, true);
		
		// priv#167: not reproducible
	}
	
	static class Root
	{
		String name;
	}
	
}

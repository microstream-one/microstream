package one.microstream.test.corp.main;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;

import one.microstream.storage.types.EmbeddedStorage;
import one.microstream.storage.types.EmbeddedStorageManager;

public class MainTestFailedSecondStart
{
	static final Path WORKING_DIR = Paths.get("storage");
	
	public static void main(final String[] args) throws IOException
	{
		startStorage();
		
		cleanup();
		
		startStorage();
	}
	
	private static void cleanup() throws IOException
	{
		Files.walk(WORKING_DIR)
	      .sorted(Comparator.reverseOrder())
	      .map(Path::toFile)
	      .forEach(File::delete);
	}

	public static void startStorage()
	{
		final EmbeddedStorageManager storage = EmbeddedStorage.start(WORKING_DIR);
		final String myRoot = new String("Root");
		
		storage.store(myRoot);
		
		final Object root = storage.root();
		System.out.println("stored string: " + root);
		
		storage.shutdown();
	}
	
}

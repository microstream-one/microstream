
package one.microstream.examples.helloworld;

import java.nio.file.Paths;
import java.util.Date;

import one.microstream.storage.embedded.types.EmbeddedStorage;
import one.microstream.storage.embedded.types.EmbeddedStorageManager;


public class HelloWorld
{
	public static void main(final String[] args)
	{
		// Application-specific root instance
		final DataRoot root = new DataRoot();

		// Initialize a storage manager ("the database") with the given directory and defaults for everything else.
		final EmbeddedStorageManager storageManager = EmbeddedStorage.start(root, Paths.get("data"));
		
		// print the root to show its loaded content (stored in the last execution).
		System.out.println(root);

		// Set content data to the root element, including the time to visualize changes on the next execution.
		root.setContent("Hello World! @ " + new Date());

		// Store the modified root and its content.
		storageManager.storeRoot();

		// Shutdown is optional as the storage concept is inherently crash-safe
//		storageManager.shutdown();
		System.exit(0);
	}
}

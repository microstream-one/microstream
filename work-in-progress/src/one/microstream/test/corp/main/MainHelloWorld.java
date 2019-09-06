
package one.microstream.test.corp.main;

import java.util.Date;

import one.microstream.storage.types.EmbeddedStorage;
import one.microstream.storage.types.EmbeddedStorageManager;


public class MainHelloWorld
{
	public static void main(final String[] args)
	{
		// Initialize a storage manager ("the database") with purely defaults.
		final EmbeddedStorageManager storageManager = EmbeddedStorage.start();
		
		// print the root to show its loaded content (stored in the last execution).
		System.out.println(storageManager.root());

		// Set content data to the root element, including the time to visualize changes on the next execution.
		storageManager.setRoot("Hello World! @ " + new Date());
		
		// Store the modified root and its content.
		storageManager.storeRoot();

		// Shutdown is optional as the storage concept is inherently crash-safe
//		storageManager.shutdown();
		System.exit(0);
	}
}

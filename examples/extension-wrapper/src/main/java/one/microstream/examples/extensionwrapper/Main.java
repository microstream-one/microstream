
package one.microstream.examples.extensionwrapper;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import one.microstream.storage.embedded.types.EmbeddedStorage;
import one.microstream.storage.embedded.types.EmbeddedStorageFoundation;
import one.microstream.storage.embedded.types.EmbeddedStorageManager;

/**
 * Example which shows how to use the instance dispatching in foundations,
 * in order to extend certain parts of the storage engine.
 *
 */
public class Main
{
	private static List<LocalDateTime> ROOT = new ArrayList<>();
	
	
	public static void main(final String[] args)
	{
		// Create default storage foundation
		final EmbeddedStorageFoundation<?> foundation = EmbeddedStorage.Foundation();
		
		// Add extender as dispatcher
		foundation.getConnectionFoundation().setInstanceDispatcher(new StorageExtender());
				
		// Start storage
		final EmbeddedStorageManager storage = foundation.start(ROOT);
		
		// See extensions in action
		ROOT.add(LocalDateTime.now());
		storage.storeRoot();
		
		storage.shutdown();
		System.exit(0);
	}
	
}

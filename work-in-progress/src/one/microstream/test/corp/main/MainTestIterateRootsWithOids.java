package one.microstream.test.corp.main;

import one.microstream.chars.XChars;
import one.microstream.persistence.types.PersistenceObjectRegistry;
import one.microstream.persistence.types.PersistenceRootsView;
import one.microstream.storage.types.EmbeddedStorage;
import one.microstream.storage.types.EmbeddedStorageManager;


public class MainTestIterateRootsWithOids
{
	public static void main(final String[] args)
	{
		final EmbeddedStorageManager storage = EmbeddedStorage.start();
		iterateRootEntries(storage);
		
		System.exit(0);
	}
	
	static void iterateRootEntries(final EmbeddedStorageManager storage)
	{
		final PersistenceObjectRegistry registry = storage.persistenceManager().objectRegistry();
		
		final PersistenceRootsView roots = storage.viewRoots();
		roots.iterateEntries((id, root) ->
		{
			System.out.println(
				registry.lookupObjectId(root) + ": " + id + " -> " + XChars.systemString(root)
			);
		});
	}
	
	
}

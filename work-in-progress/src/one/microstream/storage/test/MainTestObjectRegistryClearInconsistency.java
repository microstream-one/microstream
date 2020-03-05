package one.microstream.storage.test;

import one.microstream.reference.Lazy;
import one.microstream.storage.types.EmbeddedStorage;
import one.microstream.storage.types.EmbeddedStorageManager;


public class MainTestObjectRegistryClearInconsistency
{
	public static void main(final String[] args)
	{
		final EmbeddedStorageManager storage = EmbeddedStorage.start();
		            
		final Lazy<String> lazy = Lazy.Reference("1");
		storage.setRoot(lazy);
		storage.storeRoot();

		/*
		 * The .objectRegistry().clear() call below only works correctly if all lazy references
		 * have been cleared prior to it. Otherwise, the next store (see below) will cause
		 * inconsistencies and a corresponding exception.
		 * Reason:
		 * Such a usage is an inversion of the intended workflow.
		 * Normally, all clears AND loads are caused by lazy references (or stores) and the object
		 * registry only follows accordingly to make the object<->objectId associations available to
		 * future processes (loads and stores) as a kind of "memory" of the application to remember objects
		 * from.
		 * If that "memory" is cleared but the lazy references are not, the next store will assign new objectIds
		 * to the "forgotten" objects, but when trying to set those new ids to a lazy reference, it will
		 * most probably cause an exception since the lazy reference still remembers the old id.
		 * 
		 */
//		lazy.clear();
//		LazyReferenceManager.get().clear();
		
		storage.persistenceManager().objectRegistry().clear();
		System.gc();

		//
		storage.storeRoot();
	}
}

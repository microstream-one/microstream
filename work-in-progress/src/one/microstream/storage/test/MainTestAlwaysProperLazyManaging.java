package one.microstream.storage.test;

import java.util.Date;

import one.microstream.collections.HashTable;
import one.microstream.reference.Lazy;
import one.microstream.reference.LazyReferenceManager;
import one.microstream.storage.types.EmbeddedStorage;
import one.microstream.storage.types.EmbeddedStorageManager;
import one.microstream.test.corp.logic.Test;


public class MainTestAlwaysProperLazyManaging
{
	static
	{
		Test.clearDefaultStorageDirectory();
	}

	public static void main(final String[] args)
	{
		System.out.println("Start at " + new Date());
		
		final HashTable<String, Lazy<String>> root = HashTable.New();
		
		// registered BEFORE the LazyRef manager thread is started by the storage. Must work nevertheless!
		root.put("foo", Lazy.Reference("bar"));

		final EmbeddedStorageManager storage = EmbeddedStorage.start(root);
		storage.storeRoot();
		
		// just to check what a certain objectId refers to in the error case
//		Test.print("Exporting data ...");
//		TestImportExport.testExport(storage, Test.provideTimestampedDirectory("testExport"));
//		Test.print("Data export completed.");

		LazyReferenceManager.get().clear();
		storage.persistenceManager().objectRegistry().clear();
		System.gc();

		root.put("wait", Lazy.Reference("what"));
		
		// this line throws an "ObjectId already set" exception if the "bar" Lazy Reference was not properly registered
		storage.storeRoot();
		
		System.exit(0);
	}
		
}

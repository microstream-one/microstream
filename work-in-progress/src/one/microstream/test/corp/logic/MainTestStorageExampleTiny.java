package one.microstream.test.corp.logic;

import one.microstream.storage.types.EmbeddedStorage;
import one.microstream.storage.types.EmbeddedStorageManager;

public class MainTestStorageExampleTiny
{
	// create a storage manager, link the root, start the "embedded" database
	static final EmbeddedStorageManager STORAGE = EmbeddedStorage.start();

	public static void main(final String[] args)
	{
		if(STORAGE.root().get() == null)
		{
			Test.print("TEST: model data required." );
			STORAGE.root().set(Test.generateModelData(100_000));

			Test.print("STORAGE: storing ...");
			STORAGE.store(STORAGE.root());
			Test.print("STORAGE: storing completed.");
		}
		System.exit(0); // no shutdown required, the storage concept is inherently crash-safe
	}
			
}

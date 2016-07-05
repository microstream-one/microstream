package net.jadoth.test.corp.logic;

import net.jadoth.reference.Reference;
import net.jadoth.storage.types.EmbeddedStorage;
import net.jadoth.storage.types.EmbeddedStorageManager;
import net.jadoth.storage.types.Storage;
import net.jadoth.test.corp.model.ClientCorporation;

public class MainTestStorageExampleTiny
{
	// root of the application's data model graph (initially empty)
	static final Reference<ClientCorporation> ROOT = Reference.New(null);

	// create a storage manager, link the root, start the "embedded" database
	static final EmbeddedStorageManager STORAGE = EmbeddedStorage
		.createStorageManager(Storage.RootResolver(ROOT))
		.start()
	;

	public static void main(final String[] args)
	{
		if(ROOT.get() == null)
		{
			Test.print("TEST: model data required." );
			ROOT.set(Test.generateModelData(100_000));

			Test.print("STORAGE: storing ...");
			STORAGE.storeFull(ROOT);
			Test.print("STORAGE: storing completed.");
		}

//		STORAGE.shutdown();
		System.exit(0); // no shutdown required, storage concept is inherently crash-safe
	}
}

package net.jadoth.test.corp.logic;

import net.jadoth.reference.Reference;
import net.jadoth.storage.types.EmbeddedStorage;
import net.jadoth.storage.types.EmbeddedStorageManager;
import net.jadoth.storage.types.Storage;
import net.jadoth.test.corp.model.ClientCorporation;


public class MainTestStorageExample
{
	// root of the application's data model graph. Initially empty.
	static final Reference<ClientCorporation> ROOT = Reference.New(null);

	// create a storage manager, link the root, start the "embedded" database
	static final EmbeddedStorageManager STORAGE = EmbeddedStorage
		.createStorageManager(Storage.RootResolver(ROOT))
		.start()
	;





	public static void main(final String[] args)
	{
		// either loaded on startup from existing DB via STORAGE.start() or required to be generated for empty DB
		if(ROOT.get() == null)
		{
			// first execution enters here

			Test.print("TEST: model data required." );
			ROOT.set(Test.generateModelData(100_000));
//			ROOT.set(Test.generateHashSet(3));

			Test.print("STORAGE: storing ...");
			STORAGE.storeRequired(ROOT);
			Test.print("STORAGE: storing completed.");
		}
		else
		{
			// subsequent executions enter here

			Test.print("TEST: model data loaded." );
			Test.print(ROOT.get());
			TestImportExport.testExport(STORAGE, Test.provideTimestampedDirectory("testCorpExport"));
		}

//		STORAGE.shutdown();
		System.exit(0); // no shutdown required, storage concept is inherently crash-safe
	}
}

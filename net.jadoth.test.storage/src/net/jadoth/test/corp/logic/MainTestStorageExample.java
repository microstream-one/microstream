package net.jadoth.test.corp.logic;

import net.jadoth.storage.types.EmbeddedStorage;
import net.jadoth.storage.types.EmbeddedStorageManager;


public class MainTestStorageExample
{
	// creates and start an embedded storage manager with all-default-settngs.
	static final EmbeddedStorageManager STORAGE = EmbeddedStorage.start();

	public static void main(final String[] args)
	{
		// either loaded on startup from existing DB via STORAGE.start() or required to be generated for empty DB
		if(EmbeddedStorage.root() == null)
		{
			// first execution enters here

			Test.print("TEST: model data required." );
			EmbeddedStorage.root(Test.generateModelData(100_000));
//			EmbeddedStorage.root(Test.generateHashSet(3));

			Test.print("STORAGE: storing ...");
			STORAGE.store(EmbeddedStorage.root());
			Test.print("STORAGE: storing completed.");
		}
		else
		{
			// subsequent executions enter here

			Test.print("TEST: model data loaded." );
			Test.print(EmbeddedStorage.root());
			TestImportExport.testExport(STORAGE, Test.provideTimestampedDirectory("testCorpExport"));
		}

//		STORAGE.shutdown();
		System.exit(0); // no shutdown required, storage concept is inherently crash-safe
	}
}




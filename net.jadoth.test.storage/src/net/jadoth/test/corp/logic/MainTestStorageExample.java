package net.jadoth.test.corp.logic;

import net.jadoth.storage.types.EmbeddedStorage;
import net.jadoth.storage.types.EmbeddedStorageManager;


public class MainTestStorageExample
{
	// creates and start an embedded storage manager with all-default-settings.
	static final EmbeddedStorageManager STORAGE = EmbeddedStorage.start();

	public static void main(final String[] args)
	{
		// either loaded on startup from an existing DB or required to be generated.
		if(EmbeddedStorage.root().get() == null)
		{
			// first execution enters here

			Test.print("TEST: model data required." );
			EmbeddedStorage.root().set(Test.generateModelData(100_000));
			Test.print("STORAGE: storing ...");
			STORAGE.store(EmbeddedStorage.root());
			Test.print("STORAGE: storing completed.");
		}
		else
		{
			// subsequent executions enter here

			Test.print("TEST: model data loaded." );
			Test.print(EmbeddedStorage.root().get());
			Test.print("TEST: exporting data ..." );
			TestImportExport.testExport(STORAGE, Test.provideTimestampedDirectory("testCorpExport"));
			Test.print("TEST: data export completed.");
		}

//		STORAGE.shutdown();
		System.exit(0); // no shutdown required, the storage concept is inherently crash-safe
	}
}

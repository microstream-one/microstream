package one.microstream.test.corp.main;

import one.microstream.storage.types.EmbeddedStorage;
import one.microstream.storage.types.EmbeddedStorageManager;
import one.microstream.test.corp.logic.Test;
import one.microstream.test.corp.model.LegacyTypeHandlerPerson;

public class MainTestStorageExampleLegacyTypeHandler
{
	// create a storage manager, link the root, start the "embedded" database
	static final EmbeddedStorageManager STORAGE = EmbeddedStorage
		.Foundation()
		.onConnectionFoundation(f ->
			f
			.getCustomTypeHandlerRegistry()
			.registerLegacyTypeHandler(
				new LegacyTypeHandlerPerson()
			)
		)
		.start()
	;
	
	public static void main(final String[] args)
	{
		// either loaded on startup from an existing DB or required to be generated.
		if(STORAGE.root() == null)
		{
			// first execution enters here

			Test.print("TEST: model data required." );
			STORAGE.setRoot(Test.generateModelData(100_000));

			Test.print("STORAGE: storing ...");
			STORAGE.storeRoot();
			Test.print("STORAGE: storing completed.");
		}
		else
		{
			// subsequent executions enter here

			Test.print("TEST: model data loaded." );
			Test.print(STORAGE.root());
		}

		System.exit(0); // no shutdown required, the storage concept is inherently crash-safe
	}
}

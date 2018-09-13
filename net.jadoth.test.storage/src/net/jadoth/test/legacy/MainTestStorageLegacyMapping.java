package net.jadoth.test.legacy;

import net.jadoth.X;
import net.jadoth.storage.types.EmbeddedStorage;
import net.jadoth.storage.types.EmbeddedStorageManager;
import net.jadoth.test.corp.logic.Test;

public class MainTestStorageLegacyMapping
{
	// create a storage manager, link the root, start the "embedded" database
	static final EmbeddedStorageManager STORAGE = EmbeddedStorage.start();

	public static void main(final String[] args)
	{
		if(EmbeddedStorage.root().get() == null)
		{
			Test.print("TEST: model data required." );
			EmbeddedStorage.root().set(X.List(new NewClass(), new ChangedClass()));

			Test.print("STORAGE: storing ...");
			STORAGE.store(EmbeddedStorage.root());
			Test.print("STORAGE: storing completed.");
		}
		System.exit(0); // no shutdown required, the storage concept is inherently crash-safe
	}
			
}

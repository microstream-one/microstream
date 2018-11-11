package net.jadoth.persistence.test;

import java.io.File;

import net.jadoth.X;
import net.jadoth.reference.Reference;
import net.jadoth.storage.types.EmbeddedStorage;
import net.jadoth.storage.types.EmbeddedStorageManager;
import net.jadoth.storage.types.StorageConnection;


public class StorageTestSimple extends TestStorage
{
	// the application's persistable object graph's root
	static final Reference<Object> ROOT = Reference.New(null);

	// configure, create and start embedded storage manager (roughly equivalent to an "embedded object database")
	static final EmbeddedStorageManager STORAGE =
		EmbeddedStorage.Foundation(                // create manager building foundation with mostly defaults
			new File("c:/simpleTestStorage")             // set storage directory (instead of using working directory)
		)
		.start(ROOT) // binding between graph's root instance and the storage
	;

	public static void main(final String[] args)
	{
		// print what the root references (null on first start, stored and loaded reference on later starts)
		System.out.println("Root initial state: " + ROOT.get());

		// thread-local light-weight relaying instance to embedded storage manager (= Storage PersistenceManager)
		final StorageConnection storageConnection = STORAGE.createConnection();

		// set arbitrary object graph (simple list of lists of integers in the example)
		ROOT.set(X.List(X.List(11, 12, 13), X.List(21, 22, 23), X.List(31, 32, 33)));

		// store whole graph recursively, starting at root
		storageConnection.store(ROOT);

		// shutdown is moreless optional, only to stop threads. Storage will always recover from incomplete states.
		STORAGE.shutdown();
	}

}

package net.jadoth.persistence.test;

import java.util.Date;

import net.jadoth.reference.Reference;
import net.jadoth.storage.types.EmbeddedStorage;
import net.jadoth.storage.types.EmbeddedStorageManager;
import net.jadoth.storage.types.Storage;


/**
 * Very basic example for setting up and starting an Object Graph Storage database, connecting it to an application's
 * persistable object graph root and storing something (and loading it at the next start).
 *
 * @author Thomas Muenz
 */
public class StorageTestTiny
{
	// the application's persistable object graph's root
	static final Reference<Object> ROOT = Reference.New(null);

	/*
	 * Configure, create and start embedded storage manager (roughly equivalent to an "embedded object database").
	 *
	 * The bare minimum to be configured is to specify the root instance for the to be persisted object graph.
	 * Additional configuration ranges from specifying the target directory (instead of using the working directory),
	 * custom configuration instance, up to replacing internal parts of the storage logic via the foundation type.
	 *
	 * By default, no meta information, custom handlers or such are required.
	 *
	 * Start means to start the storage worker threads, effectively "the database".
	 */
	static final EmbeddedStorageManager STORAGE =
		EmbeddedStorage
		.createStorageManager(Storage.RootResolver(ROOT)/*, new File("c:/tinyTestStorage")/**/)
		.start()
	;

	// the tiny application's logic (store graph and exit), check root on second start to see the loaded "graph".
	public static void main(final String[] args)
	{
		ROOT.set(new Date());    // set object graph. Trivial "now" instance in this example. Could be arbitrary complex.
		STORAGE.storeFull(ROOT); // store complete graph recursively
		System.exit(0);          // exit, not caring about shutting down (automatically safe)

		/*
		 * Note that this is already a full-grown application in principle. Everything else like using
		 * lazy references in the domain model or selective storing of instances below the root are mere
		 * memory and performance optimizations tailored to the application's spcific requirements.
		 * As are a more complex object graph and complex business logic.
		 */
	}

}

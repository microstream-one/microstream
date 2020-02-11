package hg.deepcopy;

import java.nio.file.Path;
import java.nio.file.Paths;

import one.microstream.meta.XDebug;
import one.microstream.reference.Lazy;
import one.microstream.reference.LazyReferenceManager;
import one.microstream.storage.types.EmbeddedStorage;
import one.microstream.storage.types.EmbeddedStorageManager;

/*

 in this issue example a "StorageException: Storage is shut down" is thrown when working with two storages
 and an LazyReferenceManager instance

 Run this twice to get the exception ....


Exception in thread "main" one.microstream.storage.exceptions.StorageException: Storage is shut down.
at one.microstream.storage.types.StorageTaskBroker$Default.enqueueTask(StorageTaskBroker.java:192)
at one.microstream.storage.types.StorageTaskBroker$Default.enqueueTask(StorageTaskBroker.java:176)
at one.microstream.storage.types.StorageTaskBroker$Default.enqueueTaskAndNotifyAll(StorageTaskBroker.java:166)
at one.microstream.storage.types.StorageTaskBroker$Default.enqueueLoadTaskByOids(StorageTaskBroker.java:390)
at one.microstream.storage.types.StorageRequestAcceptor$Default.queryByObjectIds(StorageRequestAcceptor.java:169)
at one.microstream.storage.types.EmbeddedStorageBinarySource$Default.readByObjectIds(EmbeddedStorageBinarySource.java:84)
at one.microstream.persistence.binary.types.BinaryLoader$Default.readLoadOidData(BinaryLoader.java:759)
at one.microstream.persistence.binary.types.BinaryLoader$Default.getObject(BinaryLoader.java:824)
at one.microstream.persistence.types.PersistenceManager$Default.getObject(PersistenceManager.java:330)
at one.microstream.storage.types.StorageConnection.getObject(StorageConnection.java:175)
at one.microstream.reference.Lazy$Default.load(Lazy.java:386)
at one.microstream.reference.Lazy$Default.get(Lazy.java:369)
at hg.deepcopy.LazyManagerIssue.lambda$0(LazyManagerIssue.java:31)
at one.microstream.reference.LazyReferenceManager$Default.iterate(LazyReferenceManager.java:609)
at hg.deepcopy.LazyManagerIssue.main(LazyManagerIssue.java:30)
*/

public class LazyManagerIssue
{
	public static void main(final String args[])
	{

		final Path directory = Paths.get("c:/temp/test");

		final EmbeddedStorageManager storage1 = EmbeddedStorage.start(directory);

		final Lazy<String> dataObject1 = Lazy.Reference(new String("Hallo"));
		storage1.setRoot(dataObject1);
		storage1.storeRoot();
		storage1.shutdown();


		final EmbeddedStorageManager storage2 = EmbeddedStorage.start(directory);

		final LazyReferenceManager lm = LazyReferenceManager.get();
		lm.iterate( p -> {
			p.get();
			System.out.println(p.isLoaded());
		});

		final Lazy<String> dataObject2 = (Lazy<String>) storage2.root();
		storage2.shutdown();

		XDebug.println("done");

	}
}

package one.microstream.persistence.test;

import one.microstream.chars.XChars;
import one.microstream.concurrency.XThreads;
import one.microstream.math.XMath;
import one.microstream.persistence.lazy.Lazy;
import one.microstream.storage.types.DebugStorage;
import one.microstream.storage.types.Storage;
import one.microstream.storage.types.StorageConnection;
import one.microstream.storage.types.StorageDataFileEvaluator;


/*
 * Storage GC Stresstest
 *
 * Creates a test graph
 * Stored it in several steps to create multiple files
 * Clear the
 *
 * Storage.HousekeepingController(10, 7_000_000)
 * Storage.FileDissolver(100, 10_000, 0.75)
 */
public class MainTestStorageGCStresstest extends TestStorage
{
	static final int  RUNS      = 10000;
	static final long WAIT_TIME = 400;

	static final StorageDataFileEvaluator fileEvaluatorHard = Storage.DataFileEvaluator(100, 10_000_000, 0.99999);

	@SuppressWarnings("unchecked")
	public static void main(final String[] args)
	{
		final Lazy<Object[]> ref;
		if(ROOT.get() == null)
		{
			ref = Lazy.Reference(testGraphEvenMoreManyType());
			ROOT.set(ref);
		}
		else
		{
			ref = (Lazy<Object[]>)ROOT.get();
			ref.get();
		}

		final int size = ref.get().length;

		final StorageConnection connection = STORAGE.createConnection();
		connection.store(ROOT);

//		storageCleanup(connection);

		for(int i = 0; i < RUNS; i++)
		{
//			if(Math.random() < 0.1)
//			{
//				DEBUGStorage.println("#### GC #### (#"+i+") @ " + System.currentTimeMillis());
//				storageCleanup(connection);
//			}

			// (25.04.2018 TM)FIXME: actually requires a FullStorer#store call now
			connection.store(ref.get()[XMath.random(size)]);
//			connection.storeFull(ref.get()[XMath.random(size)]);
			DebugStorage.println("stored #"+i);

			ref.clear();
			XThreads.sleep(WAIT_TIME);
//			connection.issueFullCacheCheck((a, b, c) -> true);
//			connection.issueFullFileCheck(fileEvaluatorHard);
			final Object o = ref.get();
			DebugStorage.println("loaded: "+XChars.systemString(o));
		}
		exit();
	}

	public static void storageCleanup(final StorageConnection connection, final Double dissolveRatio)
	{
		DebugStorage.println("GC#1");
		connection.issueFullGarbageCollection();
		DebugStorage.println("GC#2");
		connection.issueFullGarbageCollection();

		DebugStorage.println("cache check");
		connection.issueFullCacheCheck();

		DebugStorage.println("file check");
		connection.issueFullFileCheck(
			Storage.DataFileEvaluator(100, 10_000, 0.99999)
		);
		DebugStorage.println("Done cleanup");
	}

}

package one.microstream.persistence.test;

import java.io.File;
import java.util.Date;

import one.microstream.X;
import one.microstream.afs.AFile;
import one.microstream.afs.nio.NioFileSystem;
import one.microstream.chars.VarString;
import one.microstream.collections.BulkList;
import one.microstream.collections.EqConstHashTable;
import one.microstream.collections.HashEnum;
import one.microstream.collections.types.XEnum;
import one.microstream.io.XIO;
import one.microstream.meta.XDebug;
import one.microstream.persistence.binary.types.BinaryPersistence;
import one.microstream.persistence.types.PersistenceTypeDictionary;
import one.microstream.reference.Lazy;
import one.microstream.storage.types.StorageConnection;
import one.microstream.storage.types.StorageRawFileStatistics;
import one.microstream.storage.types.StorageTransactionsAnalysis;
import one.microstream.test.Person;
import one.microstream.time.XTime;
import one.microstream.typing.XTypes;


public class MainTestStorage extends TestStorage
{

	public static void main(final String[] args) throws Throwable
	{
		// print what the root references (null on first start, stored reference on later starts)
//		final Object root = ROOT.get();
//		System.out.println(System.identityHashCode(root));


//		ROOT.set(new TEST());

//		STORAGE.createConnection().storeFull(ROOT);

		testBig2();
//		testBigGraph();
//		testExport();
//		testCleanUp();
//		testCleanUp();
//		testImport();
		System.exit(0);
	}



	static void testBigGraph()
	{
		final StorageConnection connection = STORAGE.createConnection();
//		createBigGraph(10, connection);
		testContinuousHouseKeeping(connection, ROOT);
	}

	static void createBigGraph(final int p6size, final StorageConnection connection)
	{
		XDebug.println("big graph initialization ("+p6size+")");
		final Integer[][][][][][] ints0 = new Integer[p6size][p6size][p6size][p6size][p6size][p6size];
		for(int i0 = 0; i0 < ints0.length; i0++)
		{
			final Integer[][][][][] ints1 = ints0[i0];
			for(int i1 = 0; i1 < ints1.length; i1++)
			{
				final Integer[][][][] ints2 = ints1[i1];
				for(int i2 = 0; i2 < ints2.length; i2++)
				{
					final Integer[][][] ints3 = ints2[i2];
					for(int i3 = 0; i3 < ints3.length; i3++)
					{
						final Integer[][] ints4 = ints3[i3];
						for(int i4 = 0; i4 < ints4.length; i4++)
						{
							final Integer[] ints5 = ints4[i4];
							for(int i5 = 0; i5 < ints5.length; i5++)
							{
								ints5[i5] = new Integer(i5);
							}
						}
					}
				}
			}
		}
		ROOT.set(Lazy.Reference(ints0));
		XDebug.println("store big graph ...");
		connection.store(ROOT);
		XDebug.println("store big graph done");
	}


	static void testContinuousHouseKeeping(final StorageConnection connection, final Object instance)
	{
//		XThreads.sleep(2000);
		for(int i = 1000; i --> 0;)
		{
			XDebug.println("round "+i);

			// do one round of explicitly issued house keeping
			connection.store(instance);
			storageCleanup(connection);

//			if(Math.random() < 0.5)
//			{
//				storageCleanup(connection);
//			}
//			else if(Math.random() < 0.2)
//			{
//				XDebug.debugln("long");
//				XThreads.sleep(16_000);
//			}
//			else
//			{
//				XDebug.debugln("short");
//				XThreads.sleep(100 + (int)(200d*Math.random()));
//			}
		}
	}

	public static void storageCleanup(final StorageConnection connection)
	{
		XDebug.println("GC#1");
		connection.issueFullGarbageCollection();
		XDebug.println("GC#2");
		connection.issueFullGarbageCollection();
		XDebug.println("cache check");
		connection.issueFullCacheCheck();
		XDebug.println("file check");
		connection.issueFullFileCheck();
		XDebug.println("Done cleanup");
	}

	static void oldTestStuff()
	{
//		testRawFileStatistics();


		// thread-local light-weight relaying instance to embedded storage manager (= Storage PersistenceManager)
//		final StorageConnection storageConnection = STORAGE.createConnection();

//		final Object complexStuff = complexStuff();
//		final long[][] largeStuff = largeStuff(20_000);

		// simple storing test
//		ROOT.set(EqConstHashTable.New(keyValue("A", "One"), keyValue("B", "Two"), keyValue("C", "Schnitzel")));
//		ROOT.set(X.List(
//			new Person("Alice", "A", null, XTime.timestamp(1990, 9, 19), 23, 52.1f, 1.70, 'F', 1234, true, false, (short)20000, (byte)112),
//			new Person("Bob", "B", null, XTime.timestamp(1980, 8, 18), 33, 69.8f, 1.80, 'M', 1337, true, false, (short)30000, (byte)127)
//		));
//		ROOT.set(X.List(
//			complexStuff,
//			largeStuff
//		));
//		ROOT.set(testObjects());
//		ROOT.set(X.Enum(1, 2, 3));
//		ROOT.set(X.Enum("A", "B", "C"));
//		ROOT.set(X.Enum(X.Enum(1, 2, 3), 1));
//		ROOT.set("Hello World");       // trivial "graph" of only one String instance for simple example

//		storageConnection.storeFull(ROOT);


//		storageConnection.exportTypes(new StorageEntityTypeExportFileProvider() {
//			@Override
//			public Path provideExportFile(final StorageEntityTypeHandler entityType)
//			{
//				return XIO.Path(
//					XIO.unchecked.ensureDirectory(XIO.Path("c:/Files/export/")),
//					entityType.typeName()+".bin"
//				);
//			}
//		});

//		for(int i = 100; i --> 0;)
//		{
//			createIntegers(ints, 1000);
//			Thread.sleep(10);
//			storageConnection.storeOnDemand(ints);
//			Thread.sleep(10);
//			System.out.println("saved "+(count + ints.size()));
//		}

//		final BulkList<Integer[]> ints = BulkList.New();
//		ROOT.set(ints);
//		storageConnection.store(ROOT); // save whole graph recursively, starting at root
//		for(int i = 1_00; i --> 0;)
//		{
//			createIntegers2(ints, 3_00);
//			storageConnection.storeOnDemand(ints);
//		}

//		while(true)
//		{
//			Thread.sleep(1000);
//			storageConnection.store(largeStuff);
//		}

//		System.out.println(ROOT.get());



//		System.exit(0); // no need to explictely "shutdown". Storage is robust enough to guarantee consistency

//		final StorageRoot<Object[][][]> root = new StorageRoot<>(testGraph());
//		final long rootId = storageConnection.store(root);
//		System.out.println(rootId);

//		final StorageRoot<Object[][][]> root = (StorageRoot<Object[][][]>)storageConnection.get(1000000000000000001L);
//		System.out.println(root.get()[10][10][10]);
//		root.clear();
//		storageConnection.store(root);

//		ROOT.set("rootstesthohoho");
////	ROOT.set(testGraph());
//		for(int i = 1; i <= 1; i++)
//		{
//			final long tStart   = System.nanoTime();
//			final long objectId = storageConnection.store(ROOT);
//			final long tStop    = System.nanoTime();
//			System.out.println("Stored " + objectId + " Elapsed Time: " + new java.text.DecimalFormat("00,000,000,000").format(tStop - tStart));
//		}

//		STORAGE.shutdown();

//		System.exit(0);

//		storageConnection.exportChannels(new StorageExportFileProvider.Default(new File("D:/storageexport"), "channelBackup_"));
//		storageConnection.exportChannels(Storage.ExportFileProvider(new File("D:/storageexport"), "channelBackup_"));
	}


	static void testImport()
	{
		final NioFileSystem nfs = NioFileSystem.New();
		
		final StorageConnection         connection = STORAGE.createConnection();
		final PersistenceTypeDictionary dictionary = BinaryPersistence.provideTypeDictionaryFromFile(
			nfs.ensureFile(XIO.Path("C:/FilesImport/PersistenceTypeDictionary.ptd"))
		);
		final XEnum<AFile> dataFiles  = nfs.ensureDirectory(XIO.Path("C:/FilesImport/channel_0")).iterateFiles(HashEnum.New())
			.sort((f1, f2) -> Long.compare(parseStorageFileNumber(f1), parseStorageFileNumber(f2)))
		;

		connection.persistenceManager().updateMetadata(dictionary);
		connection.importFiles(dataFiles);
	}

	static long parseStorageFileNumber(final AFile file)
	{
		final String filename = file.name();
		return Long.valueOf(
			filename.substring(
				filename.lastIndexOf('_') + 1
			)
		);
	}


	static void testCleanUp() throws InterruptedException
	{
		STORAGE.createConnection().issueFullGarbageCollection();
		STORAGE.createConnection().issueFullCacheCheck((a, b, e) -> {
//			System.out.println(a+" Clearing "+e.objectId()+" "+e.type().typeHandler().typeName());
			return true;
		});

		STORAGE.createConnection().issueCacheCheck(System.nanoTime()+100_000_000);
		STORAGE.createConnection().issueFullFileCheck(
//			f -> {
////				System.out.println("evaluating file "+f);
//				return fileEvaluator.needsDissolving(f);
//			}
		);

		for(int i = 1; i --> 0;)
		{
			STORAGE.createConnection().issueCacheCheck(System.nanoTime()+100_000_000);
			Thread.sleep(1000);
		}
		System.out.println("Done");
	}


	static void testBig() throws Throwable
	{
		final StorageConnection storageConnection = STORAGE.createConnection();

		final BulkList<Integer> ints = BulkList.New();
		ROOT.set(ints);
		XDebug.println("initial storing root...");
		storageConnection.store(ROOT); // save whole graph recursively, starting at root

		createIntegers(ints, 100_000);
		for(int i = 0; i < 1; i++)
		{
//			ints.clear();
//			createIntegers(ints, 10000);
//			Thread.sleep(987);
//			XDebug.debugln("storing ints #"+i);
			final long tStart = System.nanoTime();
			storageConnection.store(ints);
			final long tStop = System.nanoTime();
			System.out.println("Elapsed Time: " + new java.text.DecimalFormat("00,000,000,000").format(tStop - tStart));
//			Thread.sleep(1000);
//			System.out.println("saved "+(count + ints.size()));
		}
	}

	static void testBig2() throws Throwable
	{
		/* Import testdata:
		 * 1  channel, 100, 10_000, 0.75
		 * 5*100 Integer
		 */

		System.out.println("testing big 2");
		final StorageConnection storageConnection = STORAGE.createConnection();

		final BulkList<BulkList<Integer>> ints = BulkList.New();
		ROOT.set(ints);
		XDebug.println("initial storing root...");
		storageConnection.store(ROOT); // save whole graph recursively, starting at root

		for(int i = 0; i < 100; i++)
		{
			final BulkList<Integer> subInts = BulkList.New();
			createIntegers(subInts, 1000);
			ints.add(subInts);

			final long tStart = System.nanoTime();
			storageConnection.store(ints);
			final long tStop = System.nanoTime();
			System.out.println("Elapsed Time: " + new java.text.DecimalFormat("00,000,000,000").format(tStop - tStart));
		}
	}

	static void testRawFileStatistics()
	{
		final StorageConnection storageConnection = STORAGE.createConnection();

		final StorageRawFileStatistics stats = storageConnection.createStorageStatistics();
		System.out.println(stats);

		final BulkList<BulkList<Integer>> ints = BulkList.New();
		ROOT.set(ints);
		storageConnection.store(ROOT);

		for(int i = 100; i --> 0;)
		{
			ints.add(createIntegers(BulkList.New(), 1_000));
			storageConnection.store(ints);
		}

//		final StorageRawFileStatistics stats = storageConnection.createStorageStatitics();
//		System.out.println(stats);
	}



	static BulkList<Integer> createIntegers(final BulkList<Integer> ints, final int size)
	{
		final int first = 5001 + XTypes.to_int(ints.size());
		final int bound = first + size;
		for(int i = first; i < bound; i++)
		{
			ints.add(i);
		}
		return ints;
	}

	static void createIntegers2(final BulkList<Integer[]> ints, final int size)
	{
		final Integer[] array = new Integer[size];
		for(int i = 0; i < size; i++)
		{
			array[i] = 10_001 + i;
		}
		ints.add(array);
	}


	static File toCsvFile(final File file, final String ending)
	{
		final File dir = file.getParentFile();
		final String name = file.getName();

		return new File(dir, name.substring(0, name.lastIndexOf(ending))+".csv");
	}

	static Object complexStuff()
	{
		return X.List(
			X.List(
				new Person("Alice", "A", null, XTime.timestamp(1990, 9, 19), 23, 52.1f, 1.70, 'F', 1234, true, false, (short)20000, (byte)112),
				new Person("Bob"  , "B", null, XTime.timestamp(1980, 8, 18), 33, 69.8f, 1.80, 'M', 1337, true, false, (short)30000, (byte)127)
				),
				new Date[][][]{
				{
					{new Date(), new Date()},
					{new Date(), new Date()}
				},
				null,
				{
					{new Date()},
					{null},
					null
				},
			},
			EqConstHashTable.New(X.KeyValue("A", "One"), X.KeyValue("B", "Two"), X.KeyValue("C", "Schnitzel")),
			5,
			1337L,
			X.Enum("A", "B", "C")
			);
	}

	static long[][] largeStuff(final int size)
	{
		final long[][] array = new long[channelCount][size];
		for(int a = 0; a < array.length; a++)
		{
			for(int i = 0; i < size; i++)
			{
				array[a][i] = i;
			}
		}
		return array;
	}

	static void printTransactionsFiles(final AFile... files)
	{
		for(final AFile file : files)
		{
			printTransactionsFile(file);
		}
	}

	static void printTransactionsFile(final AFile file)
	{
		final VarString vs = VarString.New(file.toString()).lf();
		StorageTransactionsAnalysis.EntryAssembler.assembleHeader(vs, "\t").lf();
		final VarString s = StorageTransactionsAnalysis.Logic.parseFile(file, vs)
			.lf().lf()
		;
		XDebug.println(s.toString());
	}

}

//-server -Xms4g -Xmx4g -XX:+UseG1GC -XX:MaxGCPauseMillis=100 -XX:ParallelGCThreads=1 -XX:ConcGCThreads=1 -verbose:gc
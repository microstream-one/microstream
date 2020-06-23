package one.microstream.persistence.test;

import java.io.File;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;
import java.util.function.Predicate;
import java.util.function.Supplier;

import one.microstream.X;
import one.microstream.afs.ADirectory;
import one.microstream.afs.AFS;
import one.microstream.afs.AFile;
import one.microstream.collections.BulkList;
import one.microstream.collections.EqHashEnum;
import one.microstream.collections.EqHashTable;
import one.microstream.collections.HashTable;
import one.microstream.collections.types.XGettingCollection;
import one.microstream.collections.types.XSequence;
import one.microstream.functional.XFunc;
import one.microstream.meta.XDebug;
import one.microstream.reference.Reference;
import one.microstream.storage.types.EmbeddedStorage;
import one.microstream.storage.types.EmbeddedStorageConnectionFoundation;
import one.microstream.storage.types.EmbeddedStorageManager;
import one.microstream.storage.types.Storage;
import one.microstream.storage.types.StorageConnection;
import one.microstream.storage.types.StorageDataConverterCsvConfiguration;
import one.microstream.storage.types.StorageDataConverterTypeBinaryToCsv;
import one.microstream.storage.types.StorageDataConverterTypeCsvToBinary;
import one.microstream.storage.types.StorageEntityTypeConversionFileProvider;
import one.microstream.storage.types.StorageEntityTypeExportFileProvider;
import one.microstream.storage.types.StorageEntityTypeExportStatistics;
import one.microstream.storage.types.StorageLiveFileProvider;
import one.microstream.util.cql.CQL;

public class TestStorage extends TestComponentProvider
{
	// trivial root instance as an example and for testing
	protected static final Reference<Object> ROOT = Reference.New(null);

	protected static final int channelCount = 2;

	protected static final File DIRECTORY = new File("c:/Files");

	static
	{
		deleteOutput();
	}

	// configure and start embedded storage manager (=~ "embedded object database")
	protected static final EmbeddedStorageManager STORAGE = EmbeddedStorage
		.createFoundation()
		.setConfiguration(
			Storage.ConfigurationBuilder()
			.setStorageFileProvider   (createTestFileProvider())
			.setChannelCountProvider  (Storage.ChannelCountProvider(channelCount))       // storage channel/thread count (default 1)
			.setHousekeepingController(Storage.HousekeepingController(100, 7_000_000))   // time configuration for housekeeping, caching, etc.
			.setDataFileEvaluator         (Storage.DataFileEvaluator(100, 10_000_000, 0.75)) // evalutator for dissolving old files
			.setEntityCacheEvaluator  (Storage.EntityCacheEvaluator(10_000))             // evalutator for removing entities from the cache
			.createConfiguration()
		)
		.setConnectionFoundation(createTestConnectionFoundation()) // config and files for persistence layer
		.createEmbeddedStorageManager(ROOT) // binding between the application graph's root  and the storage
//		.start() // start storage threads and load all non-lazy referenced instances starting at root
	;


	static void deleteOutput()
	{
		deleteOutput(DIRECTORY);
	}

	static void deleteOutput(final File dir)
	{
		System.out.println("Resetting " + dir);
		XDebug.deleteAllFiles(dir.toPath(), false);
		System.out.println("done");
	}

	static final StorageLiveFileProvider createTestFileProvider()
	{
		return Storage.FileProvider(TEST_DIRECTORY);
	}

	static final EmbeddedStorageConnectionFoundation<?> createTestConnectionFoundation()
	{
		return TEST.initialize(EmbeddedStorageConnectionFoundation.New());
	}

	protected static ADirectory convertBinToCsv(final AFile... binaryFiles)
	{
		return convertBinToCsv(EqHashEnum.New(binaryFiles));
	}

	protected static ADirectory convertBinToCsv(final XGettingCollection<AFile> binaryFiles)
	{
		return convertBinToCsv(binaryFiles, XFunc.all());
	}

	protected static ADirectory convertBinToCsv(
		final XGettingCollection<AFile> binaryFiles,
		final Predicate<? super AFile>  filter
	)
	{
		final ADirectory dir = binaryFiles.get().parent().parent().ensureDirectory("csv");
		final StorageDataConverterTypeBinaryToCsv converter = new StorageDataConverterTypeBinaryToCsv.UTF8(
			StorageDataConverterCsvConfiguration.defaultConfiguration(),
			new StorageEntityTypeConversionFileProvider.Default(dir, "csv"),
			STORAGE.typeDictionary(),
			null,
			4096,
			4096
		);

		for(final AFile file : binaryFiles)
		{
			if(!filter.test(file))
			{
				continue;
			}
			
			AFS.execute(file, rf -> converter.convertDataFile(rf));
		}
		return dir;
	}

	protected static void convertCsvToBin(final AFile... binaryFiles)
	{
		convertCsvToBin(X.List(binaryFiles), XFunc.all());
	}

	protected static void convertCsvToBin(
		final XGettingCollection<AFile> binaryFiles,
		final Predicate<? super AFile> filter
	)
	{
		final ADirectory directory = binaryFiles.get().parent().parent().ensureDirectory("bin2");
		final StorageDataConverterTypeCsvToBinary<AFile> converter = StorageDataConverterTypeCsvToBinary.New(
			StorageDataConverterCsvConfiguration.defaultConfiguration(),
			STORAGE.typeDictionary(),
			new StorageEntityTypeConversionFileProvider.Default(directory, "dat2")
		);

		for(final AFile file : binaryFiles)
		{
			if(!filter.test(file))
			{
				continue;
			}

			AFS.execute(file, rf -> converter.convertCsv(rf));
		}
	}

	static final XSequence<AFile> exportTypes(
		final StorageConnection storageConnection,
		final ADirectory        targetDirectory  ,
		final String            fileSuffix
)
	{
		final StorageEntityTypeExportStatistics result = storageConnection.exportTypes(
			new StorageEntityTypeExportFileProvider.Default(targetDirectory, fileSuffix)
		);
		System.out.println(result);

		final XSequence<AFile> exportFiles = CQL
			.from(result.typeStatistics().values())
			.project(s -> s.file())
			.execute()
		;

//		final BulkList<File> exportFiles = CQL.projectInto(
//			result.typeStatistics().values(),
//			TypeStatistic.toFile,
//			new BulkList<File>()
//		);

//		for(final File file : exportFiles)
//		{
//			System.out.println(file+"\t"+toCsvFile(file, ".dat"));
//		}

		return exportFiles;
	}

	public static final void exit()
	{
		System.exit(0);
	}

	static Object[][][] testGraph()
	{
		final int size = 10; // 4/10/22/46/100

		final Object[][][] root = new Object[size][size][size];
		for(int i = 0; i < root.length; i++)
		{
			for(int j = 0; j < root.length; j++)
			{
				for(int j2 = 0; j2 < root.length; j2++)
				{
					root[i][j][j2] = new Date();
				}
			}
		}
		return root;
	}

	static byte[] create_bytes(final int size)
	{
		final byte[] array = new byte[size];
		for(int i = 0; i < array.length; i++)
		{
			array[i] = (byte)i;
		}
		return array;
	}

	static boolean[] create_booleans(final int size)
	{
		final boolean[] array = new boolean[size];
		for(int i = 0; i < array.length; i++)
		{
			array[i] = i < array.length/2;
		}
		return array;
	}

	static short[] create_shorts(final int size)
	{
		final short[] array = new short[size];
		for(int i = 0; i < array.length; i++)
		{
			array[i] = (short)i;
		}
		return array;
	}

	static char[] create_chars(final int size)
	{
		final char[] array = new char[size];
		for(int i = 0; i < array.length; i++)
		{
			array[i] = (char)i;
		}
		return array;
	}

	static int[] create_ints(final int size)
	{
		final int[] array = new int[size];
		for(int i = 0; i < array.length; i++)
		{
			array[i] = i;
		}
		return array;
	}

	static float[] create_floats(final int size)
	{
		final float[] array = new float[size];
		for(int i = 0; i < array.length; i++)
		{
			array[i] = i;
		}
		return array;
	}

	static long[] create_longs(final int size)
	{
		final long[] array = new long[size];
		for(int i = 0; i < array.length; i++)
		{
			array[i] = i;
		}
		return array;
	}

	static double[] create_doubles(final int size)
	{
		final double[] array = new double[size];
		for(int i = 0; i < array.length; i++)
		{
			array[i] = i;
		}
		return array;
	}

	static Object[] createArray(final int size, final Supplier<Object> supplier)
	{
		final Object[] array = new Object[size];
		for(int i = 0; i < array.length; i++)
		{
			array[i] = supplier.get();
		}
		return array;
	}

	static Object[] createPrimitiveArrays(final int size)
	{
		final Object[] prims = {
			create_bytes(size),
			create_booleans(size),
			create_shorts(size),
			create_chars(size),
			create_ints(size),
			create_floats(size),
			create_longs(size),
			create_doubles(size)
		};
		return prims;
	}

	static Object testGraphManyType()
	{
//		return new Object[]{
//			new Integer(1),
//			new Integer(2),
//			new Integer(3),
//			new Integer(4),
//			new Integer(5),
//			new Integer(6),
//			new Integer(7),
//			new Integer(8),
//			new Integer(9),
//			new Integer(10),
//			new Integer(11),
//			new Integer(12)
//		};

		final int SIZE = 5;
		final HashTable<Object, Object> root = HashTable.New();

		root.add(createPrimitiveArrays(SIZE), createArray(SIZE, () ->
			BigInteger.valueOf(Long.MAX_VALUE).add(BigInteger.valueOf(Long.MAX_VALUE)))
		);
		root.add(createPrimitiveArrays(SIZE), createArray(SIZE, () -> BigDecimal.valueOf(1337.101)));
		root.add(createPrimitiveArrays(SIZE), createArray(SIZE, () -> creatIntegers(5000, SIZE)));
		root.add(createPrimitiveArrays(SIZE), createArray(SIZE, () -> Double.toString(808.99)));
		root.add(createPrimitiveArrays(SIZE), createArray(SIZE, () -> EqHashTable.New()));
		root.add(createPrimitiveArrays(SIZE), createArray(SIZE, () -> BulkList.New(1, 2, 3)));
		root.add(createPrimitiveArrays(SIZE), createArray(SIZE, () -> createPersons(SIZE)));

		return root;
	}

	static Integer[] creatIntegers(final int offset, final int amount)
	{
		final Integer[] array = new Integer[amount];

		for(int i = 0; i < array.length; i++)
		{
			array[i] = offset + i;
		}

		return array;
	}

	static BulkList<SimplePerson> createPersons(final int amount)
	{
		final BulkList<SimplePerson> persons = BulkList.New(amount);

		for(int i = 0; i < amount; i++)
		{
			persons.add(SimplePerson.New(i + 1));
		}

		return persons;
	}
	
	static final class SimplePerson
	{
		///////////////////////////////////////////////////////////////////////////
		// static methods //
		///////////////////
		
		
		public static SimplePerson New(final int id)
		{
			return new SimplePerson(id, "P_"+id, SimplePerson.class.getSimpleName()+"_"+id, null);
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		int id;
		String firstName, lastName;
		SimplePerson friend;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		SimplePerson(final int id, final String firstName, final String lastName, final SimplePerson friend)
		{
			super();
			this.id = id;
			this.firstName = firstName;
			this.lastName = lastName;
			this.friend = friend;
		}
		
	}



	static Object[] testGraphEvenMoreManyType()
	{
//		return new Object();
//		return BulkList.New(
//			100000000,
//			100000001,
//			100000002,
//			100000003,
//			100000004,
//			100000005,
//			100000006,
//			100000007,
//			100000008,
//			100000009
//		);
		final int SIZE = 1000;
		final Object[] objects = new Object[SIZE];
		for(int i = 0; i < SIZE; i++)
		{
			objects[i] = testGraphManyType();
		}
		return objects;
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

	static void testContinuousHouseKeeping()
	{
		final StorageConnection connection = STORAGE.createConnection();

		for(int i = 0; i < 100; i++)
		{
			XDebug.println("Continuous Call #" + i);
			connection.store(ROOT);
			storageCleanup(connection);
		}
//		ROOT.set(new Object());
//		for(int i = 3; i --> 0;)
//		{
//			connection.store(ROOT);
//			storageCleanup(connection);
//		}
	}

}

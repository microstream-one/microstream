package net.jadoth.persistence.test;

import java.io.File;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;
import java.util.function.Predicate;
import java.util.function.Supplier;

import net.jadoth.X;
import net.jadoth.collections.BulkList;
import net.jadoth.collections.EqHashEnum;
import net.jadoth.collections.EqHashTable;
import net.jadoth.collections.HashTable;
import net.jadoth.collections.types.XGettingCollection;
import net.jadoth.collections.types.XSequence;
import net.jadoth.functional.XFunc;
import net.jadoth.meta.XDebug;
import net.jadoth.reference.Reference;
import net.jadoth.storage.types.EmbeddedStorage;
import net.jadoth.storage.types.EmbeddedStorageConnectionFoundation;
import net.jadoth.storage.types.EmbeddedStorageManager;
import net.jadoth.storage.types.Storage;
import net.jadoth.storage.types.StorageConnection;
import net.jadoth.storage.types.StorageDataConverterCsvConfiguration;
import net.jadoth.storage.types.StorageDataConverterTypeBinaryToCsv;
import net.jadoth.storage.types.StorageDataConverterTypeCsvToBinary;
import net.jadoth.storage.types.StorageEntityTypeConversionFileProvider;
import net.jadoth.storage.types.StorageEntityTypeExportFileProvider;
import net.jadoth.storage.types.StorageEntityTypeExportStatistics;
import net.jadoth.storage.types.StorageFileProvider;
import net.jadoth.util.cql.CQL;

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
		.createFoundationBlank()
		.setConfiguration(
			Storage.Configuration(
				createTestFileProvider()                        ,
				Storage.ChannelCountProvider(channelCount)      , // storage channel/thread count (default 1)
				Storage.HousekeepingController(100, 7_000_000)  , // time configuration for housekeeping, caching, etc.
				Storage.DataFileEvaluator(100, 10_000_000, 0.75), // evalutator for dissolving old files
				Storage.EntityCacheEvaluatorCustomTimeout(10_000) // evalutator for removing entities from the cache
			)
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
		XDebug.deleteAllFiles(dir, false);
		System.out.println("done");
	}


	static final StorageFileProvider createTestFileProvider()
	{
		return Storage.FileProvider(TEST_DIRECTORY, "channel_", "dat");
	}

	static final EmbeddedStorageConnectionFoundation<?> createTestConnectionFoundation()
	{
		return TEST.initialize(EmbeddedStorageConnectionFoundation.New());
	}

	protected static File convertBinToCsv(final File... binaryFiles)
	{
		return convertBinToCsv(EqHashEnum.New(binaryFiles));
	}

	protected static File convertBinToCsv(final XGettingCollection<File> binaryFiles)
	{
		return convertBinToCsv(binaryFiles, XFunc.all());
	}

	protected static File convertBinToCsv(final XGettingCollection<File> binaryFiles, final Predicate<? super File> filter)
	{
		final File dir = new File(binaryFiles.get().getParentFile().getParentFile(), "csv");
		final StorageDataConverterTypeBinaryToCsv converter = new StorageDataConverterTypeBinaryToCsv.ImplementationUTF8(
			StorageDataConverterCsvConfiguration.defaultConfiguration(),
			new StorageEntityTypeConversionFileProvider.Implementation(dir, "csv"),
			STORAGE.typeDictionary(),
			null,
			4096,
			4096
		);

		for(final File file : binaryFiles)
		{
			if(!filter.test(file))
			{
				continue;
			}
			try
			{
				converter.convertDataFile(file);
			}
			catch(final Exception e)
			{
				throw new RuntimeException("Exception while converting file "+file, e);
			}
		}
		return dir;
	}

	protected static void convertCsvToBin(final File... binaryFiles)
	{
		convertCsvToBin(X.List(binaryFiles), XFunc.all());
	}

	protected static void convertCsvToBin(final XGettingCollection<File> binaryFiles, final Predicate<? super File> filter)
	{
		final StorageDataConverterTypeCsvToBinary<File> converter = StorageDataConverterTypeCsvToBinary.New(
			StorageDataConverterCsvConfiguration.defaultConfiguration(),
			STORAGE.typeDictionary(),
			new StorageEntityTypeConversionFileProvider.Implementation(
				new File(binaryFiles.get().getParentFile().getParentFile(), "bin2"), "dat2"
			)
		);

		for(final File file : binaryFiles)
		{
			if(!filter.test(file))
			{
				continue;
			}
			converter.convertCsv(file);
		}
	}

	static final XSequence<File> exportTypes(
		final StorageConnection storageConnection,
		final File              targetDirectory  ,
		final String            fileSuffix
)
	{
		final StorageEntityTypeExportStatistics result = storageConnection.exportTypes(
			new StorageEntityTypeExportFileProvider.Implementation(targetDirectory, fileSuffix)
		);
		System.out.println(result);

		final XSequence<File> exportFiles = CQL
			.from(result.typeStatistics().values())
			.project(s -> s.file().file())
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
		XDebug.debugln("GC#1");
		connection.issueFullGarbageCollection();
		XDebug.debugln("GC#2");
		connection.issueFullGarbageCollection();
		XDebug.debugln("cache check");
		connection.issueFullCacheCheck();
		XDebug.debugln("file check");
		connection.issueFullFileCheck();
		XDebug.debugln("Done cleanup");
	}

	static void testContinuousHouseKeeping()
	{
		final StorageConnection connection = STORAGE.createConnection();

		for(int i = 0; i < 100; i++)
		{
			XDebug.debugln("Continuous Call #" + i);
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

package net.jadoth.persistence.test;

import java.util.Arrays;

import net.jadoth.chars.XChars;
import net.jadoth.collections.EqHashEnum;
import net.jadoth.meta.XDebug;
import net.jadoth.persistence.binary.types.Binary;
import net.jadoth.persistence.binary.types.BinaryPersistenceFoundation;
import net.jadoth.persistence.internal.DebugGraphPrinter;
import net.jadoth.persistence.types.PersistenceFoundation;
import net.jadoth.persistence.types.PersistenceManager;
import net.jadoth.persistence.types.PersistenceObjectRegistry;
import net.jadoth.persistence.types.PersistenceStorer;
import net.jadoth.persistence.types.PersistenceTypeDictionary;
import net.jadoth.persistence.types.PersistenceTypeDictionaryManager;
import net.jadoth.persistence.types.PersistenceTypeHandlerRegistry;
import net.jadoth.persistence.types.Persistence;

public class TestBinaryPersistenceTests extends TestComponentProvider
{
	///////////////////////////////////////////////////////////////////////////
	// tests             //
	//////////////////////

	static void testPersist(final PersistenceManager<Binary> persistenceManager)
	{
		XDebug.println("Persisting...");
//		new TestPerson(5);
		for(int i = 1; i --> 0;)
		{
			long tStart, tStop;
			tStart = System.nanoTime();
			final long oid = persistenceManager.store(testObject());

//			persistenceManager.staticStore(
//				TestPerson.class,
//				AbstractPerson.class
//			);
			tStop = System.nanoTime();
			System.out.println(oid+" Elapsed Time: " + new java.text.DecimalFormat("00,000,000,000").format(tStop - tStart));
		}
	}

//	static <T> T loadOneByType(final PersistenceManager<Binary> persistenceManager, final Class<T> type)
//	{
//		final BulkList<T> result = persistenceManager.collectByType(new BulkList<T>(), type);
//		return result.isEmpty() ?null :result.get();
//	}

	static void debugPrintGraph(
		final Object                      root              ,
		final PersistenceManager<Binary>  persistenceManager,
		final PersistenceFoundation<?, ?> foundation
	)
	{
		new DebugGraphPrinter(persistenceManager, foundation.getTypeHandlerManager()).apply(root);
	}


	static void testPersisterDirect(final PersistenceManager<Binary> persistenceManager)
	{
		final PersistenceStorer<Binary> persister = persistenceManager.createStorer();

//		persister.persist(array(TestBinaryObjects.indices, TestBinaryObjects.objects));

//		persister.persist(TestBinaryObjects.createTestPersons());
//		persister.persist(Integer.MAX_VALUE);
//		persister.persist(stupidArrayList(1,2,3,4));
//		persister.persist("Hallo");
//		persister.persist(new Bytie(1, 2, 3, 4));
//		persister.persist(new B());
//		persister.persist(new boolean[]{true, true, true, false, false, false, true, true, true});
//		persister.persist(new boolean[0]);
//		persister.persist(new int[]{0x55667788, 0x11223344, 0x11223344});
//		persister.persist(new Object[]{new Object(), new Object()});
//		persister.persist(new Object());
//		persister.persist(new Object());

//		final Object o1 = new Object();
//		final Object o2 = new Object();
//		persister.persist(o1);
//		persister.persist(new Object[]{o1, o2, o2});
//		persister.persist(new Object());

		persister.commit();
	}


	static void testFileLoading()
	{
		TEST.persistenceStorage().read();
	}


	static Object testBuilding(final PersistenceManager<Binary> persistenceManager)
	{
		XDebug.println("Loading...");
		final Object loaded = persistenceManager.getObject(1100000000000070001L);
		System.out.println(loaded);
//		System.out.println(Arrays.toString((int[])loaded));
//		System.out.println(Arrays.toString((double[])loaded));
//		System.out.println(Arrays.toString((Object[])loaded));

//		objRegistry.iterate(System_out_println);
//
		XDebug.println("printing arrays:");
//		System.out.println("---");
//		System.out.println("orignl: "+Arrays.toString(TestBinaryObjects.indices));
//		System.out.println("loaded: "+Arrays.toString((int[])((Object[])loaded)[0]));

		System.out.println("---");
		System.out.println("orignl: "+toString(TestBinaryObjects.objects));
		System.out.println("loaded: "+toString(((Object[])loaded)[0]));
		System.out.println("loaded: "+toString(loaded));
		System.out.println("loaded: "+loaded);
		System.out.println("---");
		TestPerson.DEBUG_printStaticState();

		return loaded;
	}

	static String toString(final Object object)
	{
		if(object instanceof Object[])
		{
			Arrays.toString((Object[])object);
		}
		return XChars.valueString(object);
	}

	static void testWriteStateDefs(final BinaryPersistenceFoundation<?> factory)
	{
		((PersistenceTypeDictionaryManager.Exporting)factory.getTypeDictionaryManager())
			.exportTypeDictionary()
		;
	}

	static void testReadStateDefs(final BinaryPersistenceFoundation<?> factory)
	{
		final PersistenceTypeDictionary typeDictionary = factory.getTypeDictionaryManager().provideTypeDictionary();
		XDebug.println('\n'+typeDictionary.toString());
	}

	static void resetRegistries(final BinaryPersistenceFoundation<?> factory)
	{
		final PersistenceObjectRegistry registry = factory.getObjectRegistry();
		final PersistenceTypeHandlerRegistry.Implementation<Binary> typeHandlerRegistry =
			(PersistenceTypeHandlerRegistry.Implementation<Binary>)factory.getTypeHandlerRegistry()
		;
		registry.clear();
		typeHandlerRegistry.clear();
//		Persistence.registerJavaBasicTypes(registry); // no more need to re-register types with separated TypeRegistry
		Persistence.registerJavaConstants(registry);
	}

	static void testRegisterer(final PersistenceManager<Binary> persistenceManager)
	{
		persistenceManager.createRegisterer().apply(testObject());
	}

	static Object testObject()
	{
//		final int size = 200_000;
//		final String[] strings = new String[size];
//		for(int i = 0; i < strings.length; i++)
//		{
//			strings[i] = Integer.toString(i);
//		}
//
//		return strings;


//		return X.array(
//			"a",
//			"b",
//			"c",
//			"d",
//			"e",
//			"f",
//			"g",
//			"h",
//			"i",
//			"j"
//		);

//		final Object[] longs = new Object[100_000];
//		for(int i = 0; i < longs.length; i++)
//		{
//			longs[i] = Long.valueOf(0xAAAA_AAAA_AAAA_0000L+i);
//		}
//		return longs;
		
//		return strings;
//		return XTime.now();
//		return 5;
//		return new int[]{1,2,3};
//		return new Object[]{5,6};
//		return "huhu";
//		return Object.class;
//		return new Object[]{Object.class, String.class};
//		return new double[]{880.5, 881.5, 882.5};
//		return new Object[]{new int[]{1,2,3}, new Object[]{'A', 1234L, "huhu", new Object()}};
//		return TestBinaryObjects.createTestPersons();
//		return stupidArrayList("Tick", "Trick", "Track");

//		return X.array(
//			 "hallo"
//			,TestBinaryObjects.indices
//			,TestBinaryObjects.objects
//			,Object.class
//			,"hallo2"
//			,new java.util.Date()
//			,new Object[]{Object.class, String.class}
//			,new TestPerson[3]
//			,new TestPerson[1][2]
//			,OldCollections.ArrayList("Tick", "Trick", "Track")
//			,TestBinaryObjects.createTestPersons()
//		);
		
		return EqHashEnum.New("A", "B", "C", "D");

	}

}

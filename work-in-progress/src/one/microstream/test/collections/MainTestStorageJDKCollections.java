package one.microstream.test.collections;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Properties;
import java.util.Stack;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.UUID;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.ConcurrentSkipListSet;

import one.microstream.reference.Reference;
import one.microstream.storage.types.EmbeddedStorage;
import one.microstream.storage.types.EmbeddedStorageManager;
import one.microstream.test.corp.logic.Test;
import one.microstream.test.corp.logic.TestImportExport;

public class MainTestStorageJDKCollections
{
	static final Reference<Object[]>    ROOT    = Reference.New(null)        ;
	static final EmbeddedStorageManager STORAGE = EmbeddedStorage.start(ROOT);

	public static void main(final String[] args)
	{
		if(ROOT.get() == null)
		{
			System.out.println("Storing collections ...");
			ROOT.set(createRoot());
			STORAGE.storeRoot();
			System.out.println("Stored collections.");
		}
		else
		{
			System.out.println("Loaded collections.");
			Test.print("Exporting collection data ..." );
			TestImportExport.testExport(STORAGE, Test.provideTimestampedDirectory("testCollections"));
		}
		
		for(final Object e : ROOT.get())
		{
			System.out.println(e.getClass().getSimpleName() + ":");
			System.out.println(e);
			System.out.println("\n");
		}
		
		System.exit(0);
	}
	
	
	private static Object[] createRoot()
	{
		final ArrayList<String>        arrayList  = populate(new ArrayList<>());
		final HashSet<String>          hashSet    = populate(new HashSet<>());
		final HashMap<Integer, String> hashMap    = populate(new HashMap<>());
		final LinkedList<String>       linkedList = populate(new LinkedList<>());

		return new Object[]{
			arrayList                              ,
			hashSet                                ,
			hashMap                                ,
			populate(new ArrayDeque<>())           ,
			populate(new Hashtable<>())            ,
			populate(new LinkedHashSet<>())        ,
			populate(new LinkedHashMap<>())        ,
			populate(new IdentityHashMap<>())      ,
			linkedList                             ,
			populate(new PriorityQueue<>())        ,
			populate(new TreeMap<>())              ,
			populate(new TreeSet<>())              ,
			populate(new Vector<>())               ,
			populate(new Stack<>())                ,
			populate(new Properties())             ,
			populate(new ConcurrentHashMap<>())    ,
			populate(new ConcurrentLinkedQueue<>()),
			populate(new ConcurrentLinkedDeque<>()),
			populate(new ConcurrentSkipListSet<>()),
			populate(new ConcurrentSkipListMap<Integer, String>(new IntegerComparator())),

			Collections.emptyList(),
			Collections.emptyMap(),
			Collections.emptyNavigableMap(),
			Collections.emptyNavigableSet(),
			Collections.emptySet(),
			Collections.emptySortedMap(),
			Collections.emptySortedSet(),

			Collections.synchronizedList(arrayList),  // RandomAccess
			Collections.synchronizedList(linkedList), // normal
			Collections.synchronizedSet(hashSet),
			Collections.synchronizedMap(hashMap)
		};
	}
	
	private static <C extends Collection<String>> C populate(final C collection)
	{
		for(int i = 0; i < 50; i++)
		{
			collection.add(UUID.randomUUID().toString());
		}
		
		return collection;
	}
	
	private static <M extends Map<Integer, String>> M populate(final M map)
	{
		for(int i = 0; i < 50; i++)
		{
			map.put(i, UUID.randomUUID().toString());
		}
		
		return map;
	}
	
	private static Properties populate(final Properties properties)
	{
		for(int i = 0; i < 50; i++)
		{
			properties.put("" + i, UUID.randomUUID().toString());
		}
		
		return properties;
	}
	
	// cannot be a lambda in order to be persistable
	static final class IntegerComparator implements Comparator<Integer>
	{
		@Override
		public int compare(final Integer o1, final Integer o2)
		{
			// nulls ignored for simple example
			return o1.compareTo(o2);
		}
	}
	
}


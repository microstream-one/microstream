package one.microstream.storage.restclient.app;

import java.awt.Dimension;
import java.io.File;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;
import java.util.TreeSet;

import one.microstream.X;
import one.microstream.chars.VarString;
import one.microstream.collections.EqHashTable;
import one.microstream.collections.types.XList;
import one.microstream.collections.types.XTable;
import one.microstream.storage.restservice.RestServiceResolver;
import one.microstream.storage.restservice.StorageRestService;
import one.microstream.storage.types.EmbeddedStorage;
import one.microstream.storage.types.EmbeddedStorageManager;

public class TestServer
{
	public static void main(
		final String[] args
	)
	{
		final EmbeddedStorageManager storage = EmbeddedStorage.start(new File("c:/data/rest-data").toPath());
		if(storage.root() == null)
		{
			storage.setRoot(new Root());
			storage.storeRoot();
		}
		
		final StorageRestService service    = RestServiceResolver.getFirst(
			storage
		);
		service.start();
	}
	
	
	static class Root
	{
		String aString = "blub";
		String bigString = VarString.New().repeat(100, "Lorem Ipsum ").toString();
		BigDecimal bigD = new BigDecimal("349858904375874.293874983");
		XList<String> stringXList = X.List("a","b",null,"d");
		List<String> stringList = new ArrayList<>(Arrays.asList("a","b",null,"d"));
		String[] strings = new String[] {"a","b",null,"d","a\tb"};
		int[] ints = new int[] {1,2,3,4,5};
		int[][] ints2 = new int[][] {{1,2,3,4,5},{10,20,30,40,50}};
		int aint = 1;
		char[] chars = {'a','b','c','\t'};
		char achar = '?';
		LocalDateTime now = LocalDateTime.now();
		LocalDateTime nullDate = null;
		Double doubleWrapper = Math.PI;
		Integer intWrapper = 123;
		List<Dimension> list = new ArrayList<>();
		{
			this.list.add(new Dimension(100, 150));
			this.list.add(new Dimension(200, 30));
		}
		Map<String, Double> map = new HashMap<>();
		{
			this.map.put("a", 1.2);
			this.map.put("b", 3.4);
			this.map.put("c", 5.6);
		}
		XTable<String, Double> table = EqHashTable.New();
		{
			this.table.put("a", 1.2);
			this.table.put("b", 3.4);
			this.table.put("c", 5.6);
		}
		TreeMap<String, Double> treeMap = new TreeMap<>(new StringComparator());
		{
			this.treeMap.put("a", 1.2);
			this.treeMap.put("b", 3.4);
			this.treeMap.put("c", 5.6);
		}
		TreeSet<String> treeSet = new TreeSet<>(new StringComparator());
		{
			this.treeSet.add("a");
			this.treeSet.add("b");
			this.treeSet.add("c");
			this.treeSet.add("d");
			this.treeSet.add("e");
		}
		List<Double> hugeList = new ArrayList<>();
		{
			final Random random = new Random();
			for(int i=0; i<1234567; i++)
			{
				this.hugeList.add(random.nextDouble());
			}
		}
		Map<Object, Object> allInOne = new HashMap<>();
		{
			this.allInOne.put(0.0, this.treeMap);
			this.allInOne.put(1.1f, this.list);
			this.allInOne.put(2, this.now);
			this.allInOne.put(3L, this.hugeList);
		}
		String nullString = null;
		Color red = Color.RED;
		Color blue = Color.BLUE;
		Color[] colors = Color.values();
	}
	
	static enum Color
	{
		RED, GREEN, BLUE{
			int i = 5;
			double pi = 3.13;
			Dimension d = new Dimension(20,30);
		}
	}
	
	static class StringComparator implements Comparator<String>
	{
		@Override
		public int compare(
			final String o1,
			final String o2
		)
		{
			return o1.compareTo(o2);
		}
	}
}

package storage.restservice.sparkjava;

import java.awt.Dimension;
import java.io.File;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import one.microstream.X;
import one.microstream.collections.types.XList;
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
		BigDecimal bigD = new BigDecimal("349858904375874.293874983");
		XList<String> stringXList = X.List("a","b",null,"d");
		int[] ints = new int[] {1,2,3,4,5};
		int[][] ints2 = new int[][] {{1,2,3,4,5},{10,20,30,40,50}};
		String[] strings = new String[] {"a","b","c"};
		int aint = 1;
		LocalDateTime now = LocalDateTime.now();
		LocalDateTime nullDate = null;
		Double doubleWrapper = Math.PI;
		List<Dimension> list = new ArrayList<>();
		{
			this.list.add(new Dimension(100, 150));
			this.list.add(new Dimension(200, 30));
		}
		Map<String, Double> map = new HashMap<>();
		{
			this.map.put("a", 1.2);
			this.map.put("b", 3.4);
		}
		List<Double> hugeList = new ArrayList<>();
		{
			final Random random = new Random();
			for(int i=0; i<1234567; i++)
			{
				this.hugeList.add(random.nextDouble());
			}
		}
		String nullString = null;
		Color color = Color.RED;
		Color[] colors = Color.values();
	}
	
	static enum Color
	{
		RED, GREEN, BLUE
	}
}

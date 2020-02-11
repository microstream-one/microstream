package storage.restservice.sparkjava;

import java.awt.Dimension;
import java.io.File;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

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
		final EmbeddedStorageManager storage = EmbeddedStorage.start(new Root(), new File("c:/data/rest-data").toPath());
		storage.storeRoot();
		
		final Object root = storage.root();
		
		final StorageRestService service    = RestServiceResolver.getFirst(
			storage
		);
		service.start();
	}
	
	
	static class Root
	{
		int[] ints = new int[] {1,2,3,4,5};
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
			for(int i=0; i<12345; i++)
			{
				this.hugeList.add(random.nextDouble());
			}
		}
	}
}

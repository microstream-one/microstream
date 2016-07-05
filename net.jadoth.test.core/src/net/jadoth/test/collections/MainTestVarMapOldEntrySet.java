package net.jadoth.test.collections;

import java.util.Set;

import net.jadoth.collections.EqHashTable;
import net.jadoth.collections.types.XMap;

public class MainTestVarMapOldEntrySet
{
	public static void main(final String[] args)
	{
		final XMap<Integer,String> map = EqHashTable.<Integer,String>New();
		map.put(5, "fünf");
		map.put(3, "drei");
		map.put(7, "sieben");
		map.put(10, "zehn");

		System.out.println(map);

		final Set<java.util.Map.Entry<Integer, String>> mapEntriesOldMapEntriesSet = map.oldMap().entrySet();
		System.out.println(mapEntriesOldMapEntriesSet);

		mapEntriesOldMapEntriesSet.clear();
		System.out.println(mapEntriesOldMapEntriesSet);
		System.out.println(map);
	}
}

package one.microstream.experimental.collections;
import java.util.Arrays;

import one.microstream.typing.KeyValue;


public class MainTestConcMap
{
	public static void main(final String[] args)
	{
		final ExperimentalLockFreeConcurrentHashMap<String, String> map = new ExperimentalLockFreeConcurrentHashMap<>();
		map.add("1", "one");
		map.add("2", "two");
		map.add("3", "three");
		map.add("4", "four");
		map.add("5", "five");
		map.add("6", "six");
		map.add("7", "seven");

		KeyValue<String, String>[] array;
		System.out.println(Arrays.toString(array = map.toArray()));
		System.out.println(map.size()+" == "+array.length + " ("+(map.size() == array.length)+")");
	}
}

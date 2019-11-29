package one.microstream.test;

import static one.microstream.X.array;

import java.util.Arrays;

import one.microstream.collections.XArrays;

public class MainTestArrayRemoveFromIndex
{
	public static void main(final String[] args)
	{
		remove(array("A", null, null, null), 1, 0);
		remove(array("A",  "B",  "C", null), 3, 0);
		remove(array("A",  "B",  "C", null), 3, 1);
		remove(array("A",  "B",  "C", null), 3, 2);
		remove(array("A",  "B",  "C",  "D"), 4, 3);
		remove(array("A",  "B",  "C",  "D"), 4, 2);
	}
	
	static void remove(final String[] strings, final int size, final int i)
	{
		XArrays.removeFromIndex(strings, size, i);
		System.out.println(Arrays.toString(strings));
	}
	
}

package one.microstream.test.corp.logic;

import java.util.ArrayList;

import one.microstream.memory.XMemory;

public class MainTestArrayLolst
{
	public static void main(final String[] args)
	{
		// bug
		final ArrayList<Integer> ints1 = new ArrayList<>();
		ints1.ensureCapacity(10);
		System.out.println(XMemory.accessArray(ints1).length);
		
		// works
		final ArrayList<Integer> ints2 = new ArrayList<>();
		ints2.ensureCapacity(11);
		System.out.println(XMemory.accessArray(ints2).length);
		
		// works
		final ArrayList<Integer> ints3 = new ArrayList<>(1);
		ints3.ensureCapacity(10);
		System.out.println(XMemory.accessArray(ints3).length);
	}
}

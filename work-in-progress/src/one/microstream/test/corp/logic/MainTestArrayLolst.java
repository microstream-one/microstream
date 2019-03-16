package one.microstream.test.corp.logic;

import java.util.ArrayList;

import one.microstream.memory.XMemory;

public class MainTestArrayLolst
{
	public static void main(final String[] args)
	{
		final ArrayList<Integer> ints = new ArrayList<>();
		ints.ensureCapacity(3);
		System.out.println(XMemory.accessStorage(ints).length);
		
		final ArrayList<Integer> ints2 = new ArrayList<>(1);
		ints2.ensureCapacity(3);
		System.out.println(XMemory.accessStorage(ints2).length);
	}
}

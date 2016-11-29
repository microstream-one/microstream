package net.jadoth.util.iterables;

import java.util.ArrayList;
import java.util.ListIterator;


public class MainLolz
{
	public static void main(final String[] args)
	{
		final ArrayList<String> s = new ArrayList<>();
		s.add("A");
		s.add("B");
		s.add("C");


		for(final ListIterator<String> i = s.listIterator(3); i.hasNext();)
		{
			System.out.println(i.next());
		}
	}
}

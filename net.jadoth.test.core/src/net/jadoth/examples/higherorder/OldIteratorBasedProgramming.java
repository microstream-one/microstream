package net.jadoth.examples.higherorder;

import java.util.ArrayList;
import java.util.List;

public class OldIteratorBasedProgramming
{
	static List<Person> persons = new ArrayList<>(); // add elements, etc.

	static boolean isAdult(final Person p)
	{
		return p.age >= 18;
	}

	static void doStuff(final Person p)
	{
		// do stuff
	}

	public static void main(final String[] args)
	{
		for(final Person p : persons)
		{
			if(isAdult(p))
			{
				doStuff(p);
			}
		}
	}

}

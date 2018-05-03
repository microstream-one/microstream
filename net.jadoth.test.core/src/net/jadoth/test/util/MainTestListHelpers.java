package net.jadoth.test.util;

import java.util.Map;

import net.jadoth.X;
import net.jadoth.collections.old.OldCollections;


/**
 * @author Thomas Muenz
 *
 */
public class MainTestListHelpers
{

	/**
	 * @param args
	 */
	public static void main(final String[] args)
	{
		for(final int i : X.ints(1,2,3,4))
		{
			System.out.println(i);
		}

		for(final char c : X.chars('a','b','c','d'))
		{
			System.out.println(c);
		}

//		for(int i : sequence(1,4))
//		{
//			System.out.println(i);
//		}

//		for(int i : range(1,4))
//		{
//			System.out.println(i);
//		}

		final Map<String, Integer> siMap = OldCollections.OldHashMap(X.KeyValue("hallo", 1), X.KeyValue("bla", 2));
		System.out.println(siMap);

	}

}

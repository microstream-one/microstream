package net.jadoth.test.util;

import static net.jadoth.Jadoth.chars;
import static net.jadoth.Jadoth.ints;
import static net.jadoth.Jadoth.keyValue;

import java.util.Map;

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
		for(final int i : ints(1,2,3,4))
		{
			System.out.println(i);
		}

		for(final char c : chars('a','b','c','d'))
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

		final Map<String, Integer> siMap = OldCollections.OldHashMap(keyValue("hallo", 1), keyValue("bla", 2));
		System.out.println(siMap);

	}

}

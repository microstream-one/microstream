/**
 *
 */
package net.jadoth.test.lang;

import static net.jadoth.Jadoth.array;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.jadoth.util.iterables.JadothIterables;

/**
 * @author Thomas Muenz
 *
 */
public class MainTestChainedIterables
{

	/**
	 * @param args
	 */
	public static void main(final String[] args)
	{
		testIterablesChaining();
//		testArraysChaining();
//		testArrayIterable();
	}


	static void testIterablesChaining()
	{
		final List<String> strings1 = Arrays.asList("A", "B", "C");
		final List<String> strings2 = Arrays.asList("D", "E", "F");

		for(final String s : JadothIterables.iterate(strings1, null, null, strings2, null))
		{
			System.out.println(s);
		}
		System.out.println("");
		for(final String s : JadothIterables.iterate(new ArrayList<String>(), null, new ArrayList<String>(), null))
		{
			System.out.println(s);
		}
	}

	static void testArraysChaining()
	{
		final String[] strings1 = array("A", "B", "C");
		final String[] strings2 = array("D", "E", "F");

		for(final String s : JadothIterables.iterate(strings1, null, null, strings2, null))
		{
			System.out.println(s);
		}
		System.out.println("");
		for(final String s : JadothIterables.iterate(new String[0], null, new String[0], null))
		{
			System.out.println(s);
		}
	}

	static void testArrayIterable()
	{
		for(final String s : JadothIterables.iterate(array("A", "B", "C")))
		{
			System.out.println(s);
		}
		System.out.println("");
		for(final String s : JadothIterables.iterate(new String[0], null, new String[0], null))
		{
			System.out.println(s);
		}
	}

}

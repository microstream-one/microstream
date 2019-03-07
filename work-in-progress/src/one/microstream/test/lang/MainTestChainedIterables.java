/**
 *
 */
package one.microstream.test.lang;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import one.microstream.X;
import one.microstream.util.iterables.ArrayIterable;
import one.microstream.util.iterables.ChainedArraysIterable;
import one.microstream.util.iterables.ChainedIterables;

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

		for(final String s : new ChainedIterables<>(strings1, null, null, strings2, null))
		{
			System.out.println(s);
		}
		System.out.println("");
		for(final String s : new ChainedIterables<>(new ArrayList<String>(), null, new ArrayList<String>(), null))
		{
			System.out.println(s);
		}
	}

	static void testArraysChaining()
	{
		final String[] strings1 = X.array("A", "B", "C");
		final String[] strings2 = X.array("D", "E", "F");

		for(final String s : new ChainedArraysIterable<>(strings1, null, null, strings2, null))
		{
			System.out.println(s);
		}
		System.out.println("");
		for(final String s : new ChainedArraysIterable<>(new String[0], null, new String[0], null))
		{
			System.out.println(s);
		}
	}

	static void testArrayIterable()
	{
		for(final String s : new ArrayIterable<>(X.array("A", "B", "C")))
		{
			System.out.println(s);
		}
		System.out.println("");
		for(final String s : new ChainedArraysIterable<>(new String[0], null, new String[0], null))
		{
			System.out.println(s);
		}
	}

}

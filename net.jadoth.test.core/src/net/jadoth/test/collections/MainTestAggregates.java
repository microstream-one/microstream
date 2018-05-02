package net.jadoth.test.collections;

import static net.jadoth.collections.JadothCollections.sum;

import net.jadoth.X;
import net.jadoth.collections.types.XList;
import net.jadoth.functional.SumInteger;

/**
 *
 * @author Thomas Muenz
 *
 */
public class MainTestAggregates
{
	/**
	 * @param args
	 */
	public static void main(final String[] args)
	{
		final XList<Integer> ints = X.List(1,2,3,4,5);
		System.out.println(ints.iterate(new SumInteger()));

		System.out.println(sum(ints));

		System.out.println(sum(X.List(1,2,3,4,5)));
	}










}

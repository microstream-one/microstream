package net.jadoth.test.collections;

import net.jadoth.collections.BulkList;

/**
 * @author Thomas Muenz
 *
 */
public class MainTestBulkListIterator
{
	public static void main(final String[] args)
	{
//		final BulkList<Integer> ints = list(1,2,3,4);
		final BulkList<Integer> ints = new BulkList<>();

		for(final Integer i : ints)
		{
			System.out.println(i);
		}
	}
}

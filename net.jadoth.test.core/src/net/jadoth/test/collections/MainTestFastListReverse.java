package net.jadoth.test.collections;

import net.jadoth.collections.BulkList;


/**
 * @author Thomas Muenz
 *
 */
public class MainTestFastListReverse
{

	/**
	 * @param args
	 */
	public static void main(final String[] args)
	{
		final BulkList<Integer> fl = BulkList.New(0,1,2,3,4,5,6,7,8,9/*,10*/);

		System.out.println(fl.toReversed());
//		System.out.println(fl.rngCopyTo(9,0, new BulkList<Integer>(10)));
//		System.out.println(fl.rngCopyTo(5,2, new BulkList<Integer>(4)));
		System.out.println(fl.toReversed().reverse());
//		System.out.println(fl.rngReverse(5,2));

	}

}

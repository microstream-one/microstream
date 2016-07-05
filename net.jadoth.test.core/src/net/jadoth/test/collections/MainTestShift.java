package net.jadoth.test.collections;

import net.jadoth.collections.BulkList;

public class MainTestShift
{
	public static void main(final String[] args)
	{
		final BulkList<Integer> ints = BulkList.New(0,1,2,3,4,5,6,7,8,9);
		System.out.println(ints);


//		ints.shiftTo(4, 2, 5);
//		System.out.println(ints);

		ints.shiftTo(2, 4, 5);
		System.out.println(ints);
	}
}

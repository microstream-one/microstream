package net.jadoth.test.collections;

import net.jadoth.collections.EqHashEnum;
import net.jadoth.collections.XUtilsCollection;

public class MainTestVarSetChain
{
	static final EqHashEnum<Integer> ints = EqHashEnum.<Integer>New().addAll(0, 1, 2, 3, 4, 5, 6, 7, 8, 9);

	/**
	 * @param args
	 */
	public static void main(final String[] args)
	{
//		testSwap(ints);
		testShuffle(ints);

	}


	static void testShuffle(final EqHashEnum<Integer> ints)
	{
		System.out.println(ints);
		XUtilsCollection.shuffle(ints);
		System.out.println(ints);
	}

	static void testSwap(final EqHashEnum<Integer> ints)
	{
		System.out.println(ints);

		ints.swap(3, 5);
		System.out.println(ints);

		ints.swap(0, 3);
		System.out.println(ints);

		ints.swap(9, 5);
		System.out.println(ints);

		ints.swap(0, 1);
		System.out.println(ints);
	}

}

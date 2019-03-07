package net.jadoth.test.collections;

import java.util.Arrays;

import net.jadoth.collections._intSet;
import net.jadoth.functional._intSum;

public class MainTest_intSet
{

	// testing
	public static void main(final String[] args)
	{
		System.out.println(_intSet.New());
		System.out.println(_intSet.New(1));
		System.out.println(_intSet.New(1, 2, 3, 4));
		System.out.println(_intSet.New(1, 2, 2, 3, 4));
		System.out.println(_intSet.New(0));
		System.out.println(_intSet.New(0, 1));
		System.out.println(_intSet.New(0, 1, 2, 3, 4));
		System.out.println(_intSet.New(0, 1, 2, 2, 3, 4));
		System.out.println(_intSet.New(0, 1, 2, 2, 3, 4).contains(3));
		System.out.println(_intSet.New(0, 1, 2, 2, 3, 4).contains(5));
		System.out.println(Arrays.toString(_intSet.New(0, 1, 2, 2, 3, 4, 4, 4, 4).toArray()));

		final _intSet ints = _intSet.New(0, 1, 2, 2, 3, 4);
		ints.remove(3);
		System.out.println(ints);
		ints.remove(0);
		System.out.println(ints);
		ints.remove(1);
		ints.remove(2);
		System.out.println(ints);
		ints.remove(4);
		System.out.println(ints);

		final int sum = _intSet.New(0, 1, 2, 2, 3, 4).iterate(new _intSum()).yield();
		System.out.println("sum = " + sum);
	}
}

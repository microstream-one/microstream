package net.jadoth.test.sorting;

import net.jadoth.TestSortUtils;
import net.jadoth.math.FastRandom;
import net.jadoth.math.JadothMath;

public class Sort
{
	static int[] alreadySorted(final int size)
	{
		return JadothMath.sequence(1, size);
	}
	static int[] reverseSorted(final int size)
	{
		return JadothMath.sequence(size, 1);
	}

	static int[] randomUniques(final int size)
	{
		final FastRandom rnd = new FastRandom();
		final int[] values = JadothMath.sequence(1, size);
		for(int i = 0; i < size; i++) swap(values, i, rnd.nextInt(size));
		return values;
	}
	static int[] random(final int size)
	{
		return JadothMath.randoming(size);
	}
	static int[] fewUnique(final int size, final int distinctAmount)
	{
		return JadothMath.randoming(size, 1, distinctAmount+1);
	}
	static int[] fewUniqueBulk(final int size, final int distinctAmount)
	{
		final int[] values = new int[size];
		final int distinctSegment = size/distinctAmount;
		final FastRandom rnd = new FastRandom();
		int value = 0;
		for(int i = 0, d = 0; i < size; i++)
		{
			if(d == 0) value = rnd.nextInt(distinctAmount);
			values[i] = value;
			if(++d == distinctSegment) d = 0;
		}

		return values;
	}
	static int[] uniques(final int size, final int distinctAmount)
	{
		return JadothMath.randoming(size, 1, distinctAmount);
	}
	static int[] multipleEquals(final int size, final int roughEqualAmount)
	{
		return JadothMath.randoming(size, 1, size/roughEqualAmount);
	}

	static int[] sawtoothForward(final int size, final int toothCount)
	{
		final int[] ints = new int[size];
		final int max = size / toothCount;
		for(int i = 0, s = 0; i < size; i++)
		{
			ints[i] = ++s == max ?s=0 :s;
		}
		return ints;
	}

	static int[] sawtoothForwardLifted(final int size, final int toothCount)
	{
		final FastRandom rnd = new FastRandom();
		final int[] ints = new int[size];
		final int max = size / toothCount;
		for(int i = 0, s = 0, offset=0; i < size; i++)
		{
			if(s == 0) offset = rnd.nextInt(101);
			ints[i] = (++s == max ?s=0 :s) + offset;
		}
		return ints;
	}

	static int[] nearlySorted(final int size)
	{
		final int[] ints = JadothMath.sequence(1, size);
		final FastRandom rnd = new FastRandom();

		for(int i = 1, len = size-3; i < len; i+=3)
		{
			final int t = ints[i], r = i+(rnd.nextInt(100)<50?-1:+1);
			ints[i] = ints[r];
			ints[r] = t;
		}
		return ints;
	}




	static boolean isSorted(final int[] values)
	{
		for(int i = 0, len = values.length - 1; i < len;)
		{
			if(values[i++] > values[i])
			{
				return false;
			}
		}
		return true;
	}

	private static void swap(final int[] values, final int l, final int r)
	{
		final int t = values[l];
		values[l] = values[r];
		values[r] = t;
	}


	static void print(final int[] values)
	{
		System.out.println(TestSortUtils.toString(30, values) + (isSorted(values) ?"" :" (NOT sorted)"));
	}
	static void print(final String s, final int[] values)
	{
		System.out.print(s+" ");
		print(values);
	}
}

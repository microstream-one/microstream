package net.jadoth.test;

import java.util.Arrays;

import net.jadoth.X;
import net.jadoth.collections.XArrays;

/**
 * @author Thomas Muenz
 *
 */
public class MainTestRemoveAllFromArray
{
	static final String[] array = {
		null, "A", "B", null, null, "C", "X", "D", "X", "X", null, "E", "X", null, null, null, "F", null, "X", null
	};


	public static void main(final String[] args)
	{
		final String[] array1 = array.clone();
		print(array1);

//		remove_X_then_null(array1);

//		int count = Jadoth.removeAllFromArray("X", array, 6, 15, array, 6, 15);
//		System.out.print(count+": ");
//		print(array);

		remove_X_and_null(array1);
	}


	static int remove(final String element, final String[] array)
	{
		final int count = XArrays.removeAllFromArray(array, 0, array.length, element);
		System.out.print(count+": ");
		print(array);
		return count;
	}

	static int remove_X(final String[] array)
	{
		return remove("X", array);
	}
	static int remove_null(final String[] array)
	{
		return remove(null, array);
	}

	static int remove_X_then_null(final String[] array)
	{
		remove_X(array);
		return remove(null, array);
	}

	static int remove_X_and_null(final String[] array)
	{
		final int count = XArrays.removeAllFromArray(X.List("X", null), array, 0, array.length);
		System.out.print(count+": ");
		print(array);
		return count;
	}


	static void print(final Object[] array)
	{
		System.out.println(Arrays.toString(array));
	}

}

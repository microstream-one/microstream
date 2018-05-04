package net.jadoth.test;

import java.util.Arrays;

import net.jadoth.X;
import net.jadoth.collections.XArrays;

/**
 * @author Thomas Muenz
 *
 */
public class MainTestAddArrays
{

	/**
	 * @param args
	 */

	public static void main(final String[] args)
	{
//		String[] combined = append(array("a", "b", "c"), array("d", "e", "f"));
		final String[] combined = XArrays.add(X.array("a", "b", "c"), "d", "e", "f");
		System.out.println(Arrays.toString(combined));

	}


}

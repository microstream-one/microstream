package one.microstream.test;

import java.util.Arrays;

import one.microstream.X;
import one.microstream.collections.XArrays;

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

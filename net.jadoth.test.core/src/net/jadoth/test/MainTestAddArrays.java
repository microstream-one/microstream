package net.jadoth.test;

import static net.jadoth.Jadoth.array;

import java.util.Arrays;

import net.jadoth.collections.JadothArrays;

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
		String[] combined = JadothArrays.add(array("a", "b", "c"), "d", "e", "f");
		System.out.println(Arrays.toString(combined));

	}


}

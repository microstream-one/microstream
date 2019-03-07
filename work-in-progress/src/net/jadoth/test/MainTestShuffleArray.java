package net.jadoth.test;

import java.util.Arrays;

import net.jadoth.collections.XArrays;

/**
 * @author Thomas Muenz
 *
 */
public class MainTestShuffleArray
{

	/**
	 * @param args
	 */
	public static void main(final String[] args)
	{
		String[] strings = {"A", "B", "C", "D", "E", "F"};

		XArrays.shuffle(strings, 0, strings.length-1);
		System.out.println(Arrays.toString(strings));

	}

}

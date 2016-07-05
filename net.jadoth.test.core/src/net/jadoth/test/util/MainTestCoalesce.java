package net.jadoth.test.util;

import net.jadoth.Jadoth;

/**
 * @author Thomas Muenz
 *
 */
public class MainTestCoalesce
{

	/**
	 * @param args
	 */
	public static void main(final String[] args)
	{
		System.out.println(Jadoth.coalesce(null, 5));
		System.out.println(Jadoth.coalesce(null, 5, 6, 7));

	}

}

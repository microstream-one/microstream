package one.microstream.test.util;

import one.microstream.X;

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
		System.out.println(X.coalesce(null, 5));
		System.out.println(X.coalesce(null, 5, 6, 7));

	}

}

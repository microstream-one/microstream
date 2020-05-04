package one.microstream.test.util;

import one.microstream.time.TimeSpan;



/**
 * @author Thomas Muenz
 *
 */
public class MainTestTimeSpan
{

	public static void main(final String[] args)
	{
		// default toString()
		System.out.println(TimeSpan.New());

		// formatted toString() with formatter
		final TimeSpan ts = TimeSpan.New();
		final TimeSpan.Format format = new TimeSpan.Format("%y% years %d% days %h% hours %m% minutes %s% seconds %S% ms");
		System.out.println(ts.toString(format));
	}

}

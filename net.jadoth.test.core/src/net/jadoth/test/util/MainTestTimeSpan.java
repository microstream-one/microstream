package net.jadoth.test.util;

import net.jadoth.time.XTime;
import net.jadoth.time.TimeSpan;



/**
 * @author Thomas Muenz
 *
 */
public class MainTestTimeSpan
{

	public static void main(final String[] args)
	{
		// default toString()
		System.out.println(XTime.TimeSpan(System.currentTimeMillis()));

		// formatted toString() with formatter
		final TimeSpan ts = XTime.TimeSpan(System.currentTimeMillis());
		final TimeSpan.Format format = new TimeSpan.Format("%y% years %d% days %h% hours %m% minutes %s% seconds %S% ms");
		System.out.println(ts.toString(format));
	}

}

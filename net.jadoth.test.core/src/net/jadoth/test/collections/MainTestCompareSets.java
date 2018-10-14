package net.jadoth.test.collections;

import java.util.HashMap;

import net.jadoth.collections.BulkList;
import net.jadoth.math.XMath;

/**
 * @author Thomas Muenz
 *
 */
public class MainTestCompareSets
{

	/**
	 * @param args
	 */
	public static void main(final String[] args)
	{
		final BulkList<String> list = new BulkList<>(16);
		final HashMap<String, Object> set = new HashMap<>(32); //give enough room for hashing

		for(int i = 0; i < 16; i++)
		{
			final String s = Integer.toString(i);
			list.add(s);
			set.put(s, null);
		}


		int count = 1000;
		final String[] searchStrings = new String[count];
		while(count --> 0)
		{
			searchStrings[count] = Integer.toString(XMath.random(32)); //0% miss chance
		}


		long tStart;
		long tStop;


		for(int i = 0; i++ < 0;)
		{
			for(final String s : searchStrings)
			{
				list.contains(s);
			}
			for(final String s : searchStrings)
			{
				set.containsKey(s);
			}
		}

		for(int i = 0; i++ < 10;)
		{
			tStart = System.nanoTime();
			for(final String s : searchStrings)
			{
				list.contains(s);
			}
			tStop = System.nanoTime();
			System.out.println("FL Elapsed Time: " + new java.text.DecimalFormat("00,000,000,000").format(tStop - tStart));


			tStart = System.nanoTime();
			for(final String s : searchStrings)
			{
				set.containsKey(s);
			}
			tStop = System.nanoTime();
			System.out.println("HM Elapsed Time: " + new java.text.DecimalFormat("00,000,000,000").format(tStop - tStart));
		}

	}

}

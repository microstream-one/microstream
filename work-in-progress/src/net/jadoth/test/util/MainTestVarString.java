package net.jadoth.test.util;

import net.jadoth.X;
import net.jadoth.chars.VarString;

/**
 * @author Thomas Muenz
 *
 */
public class MainTestVarString
{
	public static void main(final String[] args)
	{
		testSimple();
		testAppendNullPerformance();
		testSmartListing();
		testHexByteStream();
	}



	static void testSimple()
	{
		final StringBuffer sbuf = new StringBuffer("[CharSequence]");

		final VarString vc = VarString.New();
		vc.append('a').add('b', 'c').add('b', 'c', 'd').addChars('e', 'f', 'g', 'h').add("ijklm");
		vc.append(' ');
		vc.add(vc);
		vc.append(sbuf);
		vc.add(' ');
		vc.addNull();
		vc.add(" ");
		vc.append(sbuf, 1, sbuf.length()-1);
		vc.add(" ");
		vc.add("");
		vc.append((VarString.Appendable)null);

		System.out.println(vc);
	}


	static void testSmartListing()
	{
		final VarString vc = VarString.New();

		for(final char c : X.chars('a','b','c','d','e','f','g','h'))
		{
			vc.add(c, ',');
		}
		vc.deleteLast();
		System.out.println(vc);
	}

	static void testAppendNullPerformance()
	{
		final VarString vc = VarString.New(4*1000);
		final int COUNT = 100000;
		final String s = null;

		long tStart;
		long tStop;

		int o = 10;
		int i;
		while(o --> 0)
		{
			i = COUNT;
			tStart = System.nanoTime();
			while(i --> 0)
			{
				vc.add(s);
			}
			tStop = System.nanoTime();
			System.out.println("Elapsed Time: " + new java.text.DecimalFormat("00,000,000,000").format(tStop - tStart));
		}


		System.out.println(vc.length());
	}


	static void testHexByteStream()
	{
		final VarString vc = VarString.New();
		vc.addHexDec((byte)-34, (byte)-83, (byte)-6, (byte)-50);
		System.out.println(vc);
	}

}

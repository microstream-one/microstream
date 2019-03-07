package one.microstream.test.util;

import one.microstream.chars.XChars;
import one.microstream.math.XMath;



public class MainTestCharConvertPerformance
{
	static final String text = "Java is a programming language originally developed by James Gosling at Sun Microsystems (which is now a subsidiary of Oracle Corporation) and released in 1995 as a core component of Sun Microsystems' Java platform. The language derives much of its syntax from C and C++ but has a simpler object model and fewer low-level facilities. Java applications are typically compiled to bytecode (class file) that can run on any Java Virtual Machine (JVM) regardless of computer architecture. Java is a general-purpose, concurrent, class-based, object-oriented language that is specifically designed to have as few implementation dependencies as possible. It is intended to let application developers \"write once, run anywhere\". Java is currently one of the most popular programming languages in use, and is widely used from application software to web applications.";
	static final int LOOPS = 1<<15;
	static final int RUNS = 20;
//	static final int SIZE = 512;
	static final int SIZE = text.length();
	static final char[] chars = new char[SIZE];


	static{
		for(int i = 0; i < SIZE; i++)
		{
			chars[i] = (char)(XMath.random(94)+32); // common text characters
			chars[i] = (char)(XMath.random(127)); // lower ASCII characters
//			chars[i] = ' ';
//			chars[i] = '~';
//			chars[i] = '\n';
//			chars[i] = '�';
		}
		text.getChars(0, SIZE-1, chars, 0);
	}


	public static void main(final String[] args)
	{
		final char[] chars = MainTestCharConvertPerformance.chars;
		String s = null;
		int i = 0;
		long tStart;
		long tStop;
		for(int k = 0; k < RUNS; k++)
		{
			tStart = System.nanoTime();
			for(int l = 0; l < LOOPS; l++)
			{
				for(i = 0; i < SIZE; i++)
				{
					s = XChars.string(chars[i]);
//					s = String.valueOf(chars[i]);
				}
			}
			tStop = System.nanoTime();
			System.out.println("Elapsed Time: " + new java.text.DecimalFormat("00,000,000,000").format(tStop - tStart));
		}
		System.out.println(s);
	}

}

/**
 * 
 */
package net.jadoth.test.util;

import java.text.DecimalFormat;

import net.jadoth.math.XMath;

/**
 * @author Thomas Muenz
 *
 */
public class MainTestIntPow
{
	static final int LOOPS = 10;
	static final int COUNT = 10000;
	static final int EXPONENT = 250;
	
	
	
	public static void main(final String[] args)
	{
		long tStart;
		long tStop;

		if(true)
		{
			for(int n = 0; n < LOOPS; n++)
			{
				tStart = System.nanoTime();
				for(int i = 0; i < COUNT; i++)
				{
					XMath.pow(10, EXPONENT);
				}
				tStop = System.nanoTime();
				System.out.println("Elapsed Time: " + new DecimalFormat("00,000,000,000").format(tStop - tStart));
			}
		}
		System.out.println("");
		
		if(true)
		{
			for(int n = 0; n < LOOPS; n++)
			{
				tStart = System.nanoTime();
				for(int i = 0; i < COUNT; i++)
				{
					Math.pow(10, EXPONENT);
				}
				tStop = System.nanoTime();
				System.out.println("Elapsed Time: " + new DecimalFormat("00,000,000,000").format(tStop - tStart));
			}
		}
		


	}

}

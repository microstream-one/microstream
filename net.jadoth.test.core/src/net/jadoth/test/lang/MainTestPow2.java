/**
 * 
 */
package net.jadoth.test.lang;

import net.jadoth.math.JadothMath;

/**
 * @author Thomas Muenz
 *
 */
public class MainTestPow2
{

	/**
	 * @param args
	 */
	public static void main(final String[] args)
	{
		int result = 0;
		long tStart, tStop;
		
		
		final int COUNT = 1000000;
		int c = COUNT;
		
		
		
		for(int n = 0; n < 10; n++)
		{
			tStart = System.nanoTime();
			while(c --> 0)
			{
				for(int i = 1; i < 31; i++)
				{
//					result = JaMath.log2Bound(1<<i);
					result = JadothMath.log2pow2(1<<i);
				}			
			}
			tStop = System.nanoTime();
			System.out.println("Elapsed Time: " + new java.text.DecimalFormat("00,000,000,000").format(tStop - tStart));
			
			
		}
		
		
		System.out.println(result);

	}

}

/**
 * 
 */
package net.jadoth.test.lang;


/**
 * @author Thomas Muenz
 *
 */
public class MainTestModulo
{

	/**
	 * @param args
	 */
	public static void main(final String[] args)
	{
		final int COUNT = 1000000;
		int c = COUNT;
		
		int result = 0;
		
		long tStart, tStop;
		
		
		int n = 10;
		while(n --> 0)
		{
			tStart = System.nanoTime();
			while(c --> 0)
			{
//				for(int i = 2; i < 64; i*=2)
//				{
//					result = c % i;
//				}
				for(int i = 2; i < 64; i*=2)
				{
					result = c & (i-1);
				}
//				for(int i = 2; i < 64; i*=2)
//				{
//					result = c & 3;
//				}
//				for(int i = 3; i < 729; i*=3)
//				{
//					result = c % i;
//				}
			}
			System.out.print(result);
			tStop = System.nanoTime();
			System.out.println("Elapsed Time: " + new java.text.DecimalFormat("00,000,000,000").format(tStop - tStart));
		}

	}

}

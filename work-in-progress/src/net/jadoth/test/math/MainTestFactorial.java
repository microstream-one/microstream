/**
 * 
 */
package net.jadoth.test.math;

import static net.jadoth.math.XMath.bigInt;
import static net.jadoth.math.XMath.factorial;

/**
 * @author Thomas Muenz
 *
 */
public class MainTestFactorial
{
	public static void main(String[] args)
	{
		
		for(long i = 0; i <= 20; i++)
		{
			System.out.println(i+": "+factorial(i));
		}
		System.out.println("21: "+factorial(bigInt(21)));
	}
}

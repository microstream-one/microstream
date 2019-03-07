/**
 * 
 */
package one.microstream.test;

import java.util.Arrays;

import one.microstream.math.XMath;

public class MainTestSequence
{
	public static void main(String[] args)
	{
		printTestSequence( 0,  0);
		printTestSequence( 8,  8);
		printTestSequence( 0,  5);
		printTestSequence( 5,  0);
		printTestSequence( 1,  5);
		printTestSequence( 5,  9);
		printTestSequence(-5, -9);
		printTestSequence( 5, -6);
		printTestSequence(-5,  9);
		printTestSequence( 0, -5);
		printTestSequence(-5,  0);
		

	}
	
	static void printTestSequence(int from, int to)
	{
		System.out.println(
			(from<0?"":" ")+from+" -> "+(to<0?"":" ")+to+" = "+Arrays.toString(XMath.sequence(from, to))
		);
	}

}

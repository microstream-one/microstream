package net.jadoth.test;
import static net.jadoth.math.JadothMath.sequence;

import net.jadoth.concurrent.JadothThreads;


/**
 * 
 */

/**
 * @author Thomas Muenz
 *
 */
public class MainTestStartThreads
{

	/**
	 * @param args
	 */
	public static void main(final String[] args)
	{		
		for(final int id : sequence(0, 24)) JadothThreads.start(new Runnable(){ @Override public void run()
		{
			System.out.println("Hello World from thread " + id);					
		}});
		
		//JDK 7 version
//		for(final int id : sequence(0, 24)) start(#()
//		{
//			System.out.println("Hello World from thread " + id);					
//		});
	}

}

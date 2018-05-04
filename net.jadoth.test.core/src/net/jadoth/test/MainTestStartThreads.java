package net.jadoth.test;
import static net.jadoth.math.XMath.sequence;

import net.jadoth.concurrency.XThreads;


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
		for(final int id : sequence(0, 24)) XThreads.start(new Runnable(){ @Override public void run()
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

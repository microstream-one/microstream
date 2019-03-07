package net.jadoth.experimental;

/**
 * @author Thomas Muenz
 *
 */
public class CSharp_vs_Java_GotoComparison
{
	static void doIt_Iterative()
	{
		int attempts = 5;

		attempt: while(attempts > 0)
		try
		{
			//do it
			break attempt;
		}
		catch(final Exception e)
		{
			attempts--;
		}
	}


	//note: not always applicable so easily because "do it"-code normally depends on variables in scope
	static void doIt_Recursive(int retries)
	{
		try
		{
			//do it
		}
		catch(final Exception e)
		{
			if(retries > 0) doIt_Recursive(--retries);
		}
	}


	/*
	 * C-Sharp usage of goto
	 * Advantages:
	 * + better readable (no loop, no recursion)
	 * (+ allegedly less code (no loop, no recursion). Hardly true if loop is done comparably, as shown)
	 *
	 * Disadvantages:
	 * - hidden loop, not easy to spot
	 *
	 * Conclusion:
	 * Disadvantage is much more critical than the two (minor) advantages.
	 * "goto" is and remains BAD, even in simple cases.
	 * doIt_Iterative() is exactely one line of code longer but the loop (and its scope) is much easier to spot
	 */
	static void doIt_Goto_CSharp()
	{
//		int retries = 5;
//
//		attempt:
//		try
//		{
//			//do it
//		}
//		catch(Exception e)
//		{
//			if(retries-- > 0) goto attempt;
//		}
	}
}

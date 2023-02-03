package one.microstream.math;

/*-
 * #%L
 * microstream-base
 * %%
 * Copyright (C) 2019 - 2022 MicroStream Software
 * %%
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 * 
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the Eclipse
 * Public License, v. 2.0 are satisfied: GNU General Public License, version 2
 * with the GNU Classpath Exception which is
 * available at https://www.gnu.org/software/classpath/license.html.
 * 
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 * #L%
 */

public final class StaticLowRandom
{
	/* experimental single threaded static variant of java.util.Random for ints.
	 * Tests show 3 times faster execution.
	 * For "low quality" random numbers (e.g. for use in spot testing array values), this
	 * has no disadvantage over the "proper" implementation.
	 */
	private static final long
		MULTIPLIER    = 0x5DEECE66DL             ,
		ADDEND        = 0xBL                     ,
		MASC_BIT_SIZE = 48                       ,
		MASK          = (1L << MASC_BIT_SIZE) - 1
	;
	private static long seed = (System.nanoTime() ^ MULTIPLIER) & MASK;

	private static int next(final int bits)
	{
		long oldseed, nextseed;
		do
		{
			oldseed = seed;
			nextseed = oldseed * MULTIPLIER + ADDEND & MASK;
		}
		while(!compareAndSet(oldseed, nextseed)); // faster than without private method

		return (int)(nextseed >>> MASC_BIT_SIZE - bits);
	}
	private static boolean compareAndSet(final long oldseed, final long nextseed)
	{
		// (05.04.2011 TM)FIXME: concurrency could mess this up, maybe move everything to instance code.
		if(seed == oldseed)
		{
			seed = nextseed;
			return true;
		}
		return false;
	}
	public static final int nextInt(final int n)
	{
		if(n <= 0)
		{
			throw new IllegalArgumentException("n must be positive");
		}

		int bits, val;
		do
		{
			// CHECKSTYLE.OFF: MagicNumber: honestly no idea why 31. Just copied it. All value bits?
			val = (bits = next(31)) % n;
			// CHECKSTYLE.ON:  MagicNumber
		}
		while (bits - val + n - 1 < 0);

		return val;
	}
	
	

	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////
	
	/**
	 * Dummy constructor to prevent instantiation of this static-only utility class.
	 * 
	 * @throws UnsupportedOperationException when called
	 */
	private StaticLowRandom()
	{
		// static only
		throw new UnsupportedOperationException();
	}
	
}

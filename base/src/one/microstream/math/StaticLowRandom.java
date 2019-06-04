package one.microstream.math;

public final class StaticLowRandom
{
	/* experimental singlethreaded static variant of java.util.Random for ints.
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
		while(!compareAndSet(oldseed, nextseed)); // faster than whithout private method

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

//		// this does not yield any performance gain in tests, so why do it. Stability problems with 31 below?
//		if((n & -n) == n)
//		{
//			// i.e., n is a power of 2
//			return (int)(n * (long)next(31) >> 31);
//		}

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
	 * @throws UnsupportedOperationException
	 */
	private StaticLowRandom()
	{
		// static only
		throw new UnsupportedOperationException();
	}
}

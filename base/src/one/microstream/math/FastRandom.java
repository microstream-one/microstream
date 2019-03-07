package one.microstream.math;

public final class FastRandom
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

	private long seed = (System.nanoTime() ^ MULTIPLIER) & MASK;

	private int next(final int bits)
	{
		long oldseed, nextseed;
		do
		{
			oldseed = this.seed;
			nextseed = oldseed * MULTIPLIER + ADDEND & MASK;
		}
		while(!this.compareAndSet(oldseed, nextseed)); // faster than whithout private method

		return (int)(nextseed >>> MASC_BIT_SIZE - bits);
	}

	private boolean compareAndSet(final long oldseed, final long nextseed)
	{
		if(this.seed == oldseed)
		{
			this.seed = nextseed;
			return true;
		}
		return false;
	}

	public final int nextInt(final int n)
	{
		if(n <= 0)
		{
			throw new IllegalArgumentException("n must be positive");
		}
		int bits, val;

		do
		{
			// CHECKSTYLE.OFF: MagicNumber: honestly no idea why 31. Just copied it. All value bits?
			val = (bits = this.next(31)) % n;
			// CHECKSTYLE.ON:  MagicNumber
		}
		while(bits - val + n - 1 < 0);

		return val;
	}
}

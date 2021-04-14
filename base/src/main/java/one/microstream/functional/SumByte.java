package one.microstream.functional;


/**
 * 
 *
 */
public final class SumByte implements Aggregator<Byte, Integer>
{
	private int sum;

	@Override
	public final void accept(final Byte n)
	{
		if(n != null)
		{
			this.sum += n;
		}
	}

	@Override
	public final Integer yield()
	{
		return this.sum;
	}

}

package one.microstream.functional;


/**
 * 
 *
 */
public final class SumShort implements Aggregator<Short, Integer>
{
	private int sum;

	@Override
	public final void accept(final Short n)
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

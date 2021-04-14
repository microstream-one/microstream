package one.microstream.functional;



/**
 * 
 *
 */
public final class SumInteger implements Aggregator<Integer, Integer>
{
	private int sum;

	@Override
	public final void accept(final Integer n)
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

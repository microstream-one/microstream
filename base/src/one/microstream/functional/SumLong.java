package one.microstream.functional;


/**
 * @author Thomas Muenz
 *
 */
public final class SumLong implements Aggregator<Long, Long>
{
	private long sum;

	@Override
	public final void accept(final Long n)
	{
		if(n != null)
		{
			this.sum += n;
		}
	}

	@Override
	public final Long yield()
	{
		return this.sum;
	}

}

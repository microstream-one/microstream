package one.microstream.functional;

import one.microstream.collections.types.XGettingCollection;


/**
 * 
 *
 */
public final class AvgIntegerNonNull implements Aggregator<Integer, Integer>
{
	private int sum  ;
	private int count;

	public AvgIntegerNonNull()
	{
		super();
	}

	public AvgIntegerNonNull(final XGettingCollection<Integer> c)
	{
		super();
		c.iterate(this);
	}

	@Override
	public final void accept(final Integer n)
	{
		if(n == null)
		{
			return;
		}
		this.sum += n;
		this.count++;
	}

	@Override
	public final Integer yield()
	{
		return this.count == 0 ? null : this.sum / this.count;
	}

}

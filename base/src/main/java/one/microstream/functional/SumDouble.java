package one.microstream.functional;


/**
 * 
 *
 */
public final class SumDouble implements Aggregator<Double, Double>
{
	private double sum;

	@Override
	public final void accept(final Double n)
	{
		if(n != null)
		{
			this.sum += n;
		}
	}

	@Override
	public final Double yield()
	{
		return this.sum;
	}

}

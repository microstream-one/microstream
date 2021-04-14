package one.microstream.functional;

public final class Aggregate_doubleMin<E> implements Aggregator<E, Double>
{
	private final To_double<? super E> getter;

	private double minimum = Double.MAX_VALUE;

	public Aggregate_doubleMin(final To_double<? super E> getter)
	{
		super();
		this.getter = getter;
	}

	@Override
	public final void accept(final E element)
	{
		final double value = this.getter.apply(element);
		if(value < this.minimum)
		{
			this.minimum = value;
		}
	}

	@Override
	public final Double yield()
	{
		return this.minimum;
	}

}

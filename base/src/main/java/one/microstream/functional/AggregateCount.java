package one.microstream.functional;

public final class AggregateCount<E> implements Aggregator<E, Long>
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	private long count;



	///////////////////////////////////////////////////////////////////////////
	// override methods //
	/////////////////////

	@Override
	public final void accept(final E element)
	{
		this.count++;
	}

	@Override
	public final Long yield()
	{
		return this.count;
	}

}

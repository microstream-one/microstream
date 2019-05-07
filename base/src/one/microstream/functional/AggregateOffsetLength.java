package one.microstream.functional;

import one.microstream.X;

public final class AggregateOffsetLength<E, R> implements Aggregator<E, R>
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	private long                           offset   ;
	private long                           length   ;
	private final Aggregator<? super E, R> aggregate;



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public AggregateOffsetLength(final long offset, final long length, final Aggregator<? super E, R> aggregate)
	{
		super();
		this.offset    = offset   ;
		this.length    = length   ;
		this.aggregate = aggregate;
	}



	///////////////////////////////////////////////////////////////////////////
	// override methods //
	/////////////////////

	@Override
	public final void accept(final E element)
	{
		if(this.offset > 0)
		{
			this.offset--;
			return;
		}
		this.aggregate.accept(element);
		if(--this.length == 0)
		{
			throw X.BREAK();
		}
	}

	@Override
	public final R yield()
	{
		return this.aggregate.yield();
	}

}

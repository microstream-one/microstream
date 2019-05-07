package one.microstream.functional;

import java.util.function.Consumer;
import java.util.function.Predicate;

import one.microstream.X;
import one.microstream.branching.ThrowBreak;

public final class LimitedOperationWithPredicate<E> implements Consumer<E>
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	private int skip;
	private int lim;
	private final Predicate<? super E> predicate;
	private final Consumer<? super E> procedure;



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public LimitedOperationWithPredicate(
		final int skip,
		final int limit,
		final Predicate<? super E> predicate,
		final Consumer<? super E> procedure
	)
	{
		super();
		this.skip = skip;
		this.lim = limit;
		this.predicate = predicate;
		this.procedure = procedure;
	}



	///////////////////////////////////////////////////////////////////////////
	// override methods //
	/////////////////////

	@Override
	public final void accept(final E e)
	{
		try
		{
			if(!this.predicate.test(e))
			{
				return;
			}
			if(this.skip > 0)
			{
				this.skip--;
				return;
			}
			this.procedure.accept(e);
			if(--this.lim == 0)
			{
				throw X.BREAK();
			}
		}
		catch(final ThrowBreak t)
		{
			throw t;
		}
	}

}

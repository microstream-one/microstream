package one.microstream.functional;

import static one.microstream.X.notNull;

import java.util.function.Consumer;

import one.microstream.X;


public final class LimitedProcedure<E> implements Consumer<E>
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	private final Consumer<? super E> procedure;
	private int skip;
	private int limit;



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public LimitedProcedure(final Consumer<? super E> procedure, final int skip, final int limit)
	{
		super();
		this.procedure = notNull(procedure);
		this.skip      = skip;
		this.limit     = limit;
	}



	///////////////////////////////////////////////////////////////////////////
	// override methods //
	/////////////////////

	@Override
	public final void accept(final E e)
	{
		if(this.skip > 0)
		{
			this.skip--;
			return;
		}
		if(this.limit > 0)
		{
			this.procedure.accept(e);
			this.limit--; // decrement after procedure call to let continue throw skip it? tricky... maybe too much
			return;
		}
		throw X.BREAK();
	}

}

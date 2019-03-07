package one.microstream.functional;

import java.util.function.Consumer;

public abstract class AbstractProcedureSkipLimit<I> implements Consumer<I>
{
	long skip, limit;

	AbstractProcedureSkipLimit(final long skip, final long limit)
	{
		super();
		this.skip  = skip ;
		this.limit = limit;
	}

}

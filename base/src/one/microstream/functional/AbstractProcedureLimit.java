package one.microstream.functional;

import java.util.function.Consumer;

public abstract class AbstractProcedureLimit<I> implements Consumer<I>
{
	long limit;

	AbstractProcedureLimit(final long limit)
	{
		super();
		this.limit = limit;
	}

}

package one.microstream.functional;

import java.util.function.Consumer;

public abstract class AbstractProcedureSkip<I> implements Consumer<I>
{
	long skip;

	AbstractProcedureSkip(final long skip)
	{
		super();
		this.skip = skip;
	}

}

package net.jadoth.functional;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import net.jadoth.Jadoth;


public final class JadothProcedures
{
	///////////////////////////////////////////////////////////////////////////
	// static methods    //
	/////////////////////

	public static <E> Aggregator<E, Long> counter()
	{
		return new Aggregator<E, Long>()
		{
			long count;

			@Override
			public void accept(final E element)
			{
				this.count++;
			}

			@Override
			public Long yield()
			{
				return this.count;
			}
		};
	}

	/**
	 * Constant for literally a no-op procedure.
	 */
	public static <E> void noOp(final E e)
	{
		// no-op
	}

	// all direct //

	public static <E> Consumer<E> wrapWithSkip(final Consumer<? super E> procedure, final long skip)
	{
		return new AbstractProcedureSkip<E>(skip)
		{
			@Override
			public void accept(final E e)
			{
				if(--this.skip >= 0)
				{
					return;
				}
				procedure.accept(e);
			}
		};
	}

	public static <E> Consumer<E> wrapWithLimit(final Consumer<? super E> procedure, final long limit)
	{
		return new AbstractProcedureLimit<E>(limit)
		{
			@Override
			public void accept(final E e)
			{
				procedure.accept(e);
				if(--this.limit == 0)
				{
					throw Jadoth.BREAK;
				}
			}
		};
	}

	public static <E> Consumer<E> wrapWithSkipLimit(
		final Consumer<? super E> procedure,
		final long                skip     ,
		final long                limit
	)
	{
		return new AbstractProcedureSkipLimit<E>(skip, limit)
		{
			@Override
			public void accept(final E e)
			{
				if(--this.skip >= 0)
				{
					return;
				}
				procedure.accept(e);
				if(--this.limit == 0)
				{
					throw Jadoth.BREAK;
				}
			}
		};
	}

	// predicate //

	public static final <E> Consumer<E> wrapWithPredicate(
		final Consumer<? super E> procedure,
		final Predicate<? super E> predicate
	)
	{
		return e ->
		{
			if(!predicate.test(e))
			{
				return; // debug hook
			}
			procedure.accept(e);
		};
	}

	public static <E> Consumer<E> wrapWithPredicateSkip(
		final Consumer<? super E> procedure,
		final Predicate<? super E> predicate,
		final long                 skip
	)
	{
		return new AbstractProcedureSkip<E>(skip)
		{
			@Override
			public void accept(final E e)
			{
				if(!predicate.test(e))
				{
					return; // debug hook
				}
				if(--this.skip >= 0)
				{
					return;
				}
				procedure.accept(e);
			}
		};
	}

	public static <E> Consumer<E> wrapWithPredicateLimit(
		final Consumer<? super E> procedure,
		final Predicate<? super E> predicate,
		final long                 limit
	)
	{
		return new AbstractProcedureLimit<E>(limit)
		{
			@Override
			public void accept(final E e)
			{
				if(!predicate.test(e))
				{
					return; // debug hook
				}
				procedure.accept(e);
				if(--this.limit == 0)
				{
					throw Jadoth.BREAK;
				}
			}
		};
	}

	public static <E> Consumer<E> wrapWithPredicateSkipLimit(
		final Consumer<? super E> procedure,
		final Predicate<? super E> predicate,
		final long                 skip     ,
		final long                 limit
	)
	{
		return new AbstractProcedureSkipLimit<E>(skip, limit)
		{
			@Override
			public void accept(final E e)
			{
				if(!predicate.test(e))
				{
					return; // debug hook
				}
				if(--this.skip >= 0)
				{
					return;
				}
				procedure.accept(e);
				if(--this.limit == 0)
				{
					throw Jadoth.BREAK;
				}
			}
		};
	}

	// function //

	public static final <I, O> Consumer<I> wrapWithFunction(
		final Consumer<? super O>   procedure,
		final Function<? super I, O> function
	)
	{
		return e ->
		{
			procedure.accept(function.apply(e));
		};
	}

	public static <I, O> Consumer<I> wrapWithFunctionSkip(
		final Consumer<? super O>   procedure,
		final Function<? super I, O> function ,
		final long                   skip
	)
	{
		return new AbstractProcedureSkip<I>(skip)
		{
			@Override
			public void accept(final I e)
			{
				if(--this.skip >= 0)
				{
					return;
				}
				procedure.accept(function.apply(e));
			}
		};
	}

	public static <I, O> Consumer<I> wrapWithFunctionLimit(
		final Consumer<? super O>   procedure,
		final Function<? super I, O> function ,
		final long                   limit
	)
	{
		return new AbstractProcedureLimit<I>(limit)
		{
			@Override
			public void accept(final I e)
			{
				procedure.accept(function.apply(e));
				if(--this.limit == 0)
				{
					throw Jadoth.BREAK;
				}
			}
		};
	}

	public static <I, O> Consumer<I> wrapWithFunctionSkipLimit(
		final Consumer<? super O>   procedure,
		final Function<? super I, O> function ,
		final long                   skip     ,
		final long                   limit
	)
	{
		return new AbstractProcedureSkipLimit<I>(skip, limit)
		{
			@Override
			public void accept(final I e)
			{
				if(--this.skip >= 0)
				{
					return;
				}
				procedure.accept(function.apply(e));
				if(--this.limit == 0)
				{
					throw Jadoth.BREAK;
				}
			}
		};
	}

	// predicate function //

	public static final <I, O> Consumer<I> wrapWithPredicateFunction(
		final Consumer<? super O>   procedure,
		final Predicate<? super I>   predicate,
		final Function<? super I, O> function
	)
	{
		return e ->
		{
			if(!predicate.test(e))
			{
				return; // debug hook
			}
			procedure.accept(function.apply(e));
		};
	}

	public static <I, O> Consumer<I> wrapWithPredicateFunctionSkip(
		final Consumer<? super O>   procedure,
		final Predicate<? super I>   predicate,
		final Function<? super I, O> function ,
		final long                   skip
	)
	{
		return new AbstractProcedureSkip<I>(skip)
		{
			@Override
			public void accept(final I e)
			{
				if(!predicate.test(e))
				{
					return; // debug hook
				}
				if(--this.skip >= 0)
				{
					return;
				}
				procedure.accept(function.apply(e));
			}
		};
	}

	public static <I, O> Consumer<I> wrapWithPredicateFunctionLimit(
		final Consumer<? super O>   procedure,
		final Predicate<? super I>   predicate,
		final Function<? super I, O> function ,
		final long                   limit
	)
	{
		return new AbstractProcedureLimit<I>(limit)
		{
			@Override
			public void accept(final I e)
			{
				if(!predicate.test(e))
				{
					return; // debug hook
				}
				procedure.accept(function.apply(e));
				if(--this.limit == 0)
				{
					throw Jadoth.BREAK;
				}
			}
		};
	}

	public static <I, O> Consumer<I> wrapWithPredicateFunctionSkipLimit(
		final Consumer<? super O>   procedure,
		final Predicate<? super I>   predicate,
		final Function<? super I, O> function ,
		final long                   skip     ,
		final long                   limit
	)
	{
		return new AbstractProcedureSkipLimit<I>(skip, limit)
		{
			@Override
			public void accept(final I e)
			{
				if(!predicate.test(e))
				{
					return; // debug hook
				}
				if(--this.skip >= 0)
				{
					return;
				}
				procedure.accept(function.apply(e));
				if(--this.limit == 0)
				{
					throw Jadoth.BREAK;
				}
			}
		};
	}


	private JadothProcedures()
	{
		// static only
		throw new UnsupportedOperationException();
	}
}

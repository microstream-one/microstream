package one.microstream.functional;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import one.microstream.X;
import one.microstream.equality.Equalator;


/**
 * Normally, writing "Func" instead of "Functional" is a capital sin of writing clean code.
 * However, in the sake of shortness for static util method class names AND in light of
 * "Mathematics", "Sorting" and "Characters" already being shortened to the (albeit more common)
 * names "Math", "Sort", "Chars" PLUS the unique recognizability of "Func", the shortness trumped
 * the clarity of completeness here (as well).
 * Rules are made for people and must be bendable if reasonable reflection (not laziness!)
 * concludes necessity.
 * 
 * 
 */
public final class XFunc
{
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////
	
	/**
	 * Functional alias for{@code return true;}.
	 */
	public static final <T> Predicate<T> all()
	{
		// note on performance: equal to caching an instance in a constant field (lambdas are cached internally)
		return e -> true;
	}

	/**
	 * Functional alias for{@code return true;}.
	 */
	public static final <T> Predicate<T> any()
	{
		return all();
	}

	/**
	 * Functional alias for {@code return false;}.
	 */
	public static final <T> Predicate<T> none()
	{
		// note on performance: equal to caching an instance in a constant field (lambdas are cached internally)
		return e -> false;
	}

	/**
	 * Functional alias for {@code return e != null;}.
	 */
	public static <T> Predicate<T> notNull()
	{
		// note on performance: equal to caching an instance in a constant field (lambdas are cached internally)
		return e -> e != null;
	}

	public static final <T, R> Function<T, R> toNull()
	{
		// note on performance: equal to caching an instance in a constant field (lambdas are cached internally)
		return t -> null;
	}

	public static final <T> Function<T, T> passThrough()
	{
		// note on performance: equal to caching an instance in a constant field (lambdas are cached internally)
		return t -> t;
	}


	/**
	 * Literally a no-op {@link Consumer}.
	 */
	public static <E> void noOp(final E e)
	{
		// no-op
	}

	@SafeVarargs
	public static final <T> Predicate<T> all(final Predicate<? super T>... predicates)
	{
		return e ->
		{
			for(final Predicate<? super T> predicate : predicates)
			{
				if(!predicate.test(e))
				{
					return false;
				}
			}
			return true;
		};
	}
	
	/**
	 * Required to use lambdas or method reference in conjunction with {@link Predicate#and(Predicate)} etc.
	 * 
	 * @param predicate a predicate instance
	 * @return the passed predicate instance without execution any further logic.
	 */
	public static final <T> Predicate<T> select(final Predicate<T> predicate)
	{
		return predicate;
	}

	@SafeVarargs
	public static final <T> Predicate<T> one(final Predicate<? super T>... predicates)
	{
		return e ->
		{
			final int size = predicates.length;
			int i = 0;
			while(i < size)
			{
				if(predicates[i++].test(e))
				{
					while(i < size)
					{
						if(predicates[i++].test(e))
						{
							return false;
						}
					}
					return true;
				}
			}
			return false;
		};
	}

	public static final <E> Predicate<E> isEqual(final E sample, final Equalator<? super E> equalator)
	{
		return new EqualsSample<>(sample, equalator);
	}

	public static final <T> Predicate<T> isEqualTo(final T subject)
	{
		return new Predicate<T>()
		{
			@Override
			public boolean test(final T o)
			{
				return subject == null ? o == null : subject.equals(o);
			}
		};
	}

	public static final <T> Predicate<T> isSameAs(final T subject)
	{
		return new Predicate<T>()
		{
			@Override
			public boolean test(final T o)
			{
				return subject == o;
			}
		};
	}

	public static final <T, E extends T> Predicate<T> predicate(final E subject, final Equalator<T> equalator)
	{
		return new Predicate<T>()
		{
			@Override
			public boolean test(final T t)
			{
				return equalator.equal(subject, t);
			}
		};
	}

	public static Predicate<Object> isInstanceOf(final Class<?> type)
	{
		return e ->
			type.isInstance(e)
		;
	}
	
	public static Predicate<Object> notIsInstanceOf(final Class<?> type)
	{
		return e ->
			!type.isInstance(e)
		;
	}
	
	public static final Predicate<Object> isInstanceOf(final Class<?> ... types)
	{
		return object ->
		{
			for(final Class<?> type : types)
			{
				if(type.isInstance(object))
				{
					return true;
				}
			}
			return false;
		};
	}
	
	public static final Predicate<Object> notIsInstanceOf(final Class<?> ... types)
	{
		return object ->
		{
			for(final Class<?> type : types)
			{
				if(type.isInstance(object))
				{
					return false;
				}
			}
			return true;
		};
	}

	/**
	 * Fluent alias for {@code predicate.negate()}.
	 */
	public static <T> Predicate<T> not(final Predicate<T> predicate)
	{
		return predicate.negate();
	}

	// (04.07.2011)TODO in() predicate etc.

	public static final Aggregator<Integer, Integer> max(final int initialValue)
	{
		return new XFunc.MaxInteger(initialValue);
	}

	public static final <E> AggregateCount<E> count()
	{
		return new AggregateCount<>();
	}

	public static final <E, R> Aggregator<E, R> aggregator(
		final BiConsumer<? super E, ? super R> joiner   ,
		final R                                aggregate
	)
	{
		return
			new Aggregator<E, R>()
			{
				@Override
				public void accept(final E element)
				{
					joiner.accept(element, aggregate);
				}
				
				@Override
				public R yield()
				{
					return aggregate;
				}
			}
		;
	}

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

	public static <E> Consumer<E> wrapWithSkip(final Consumer<? super E> target, final long skip)
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
				target.accept(e);
			}
		};
	}

	public static <E> Consumer<E> wrapWithLimit(final Consumer<? super E> target, final long limit)
	{
		return new AbstractProcedureLimit<E>(limit)
		{
			@Override
			public void accept(final E e)
			{
				target.accept(e);
				if(--this.limit == 0)
				{
					throw X.BREAK();
				}
			}
		};
	}

	public static <E> Consumer<E> wrapWithSkipLimit(
		final Consumer<? super E> target,
		final long                skip  ,
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
				target.accept(e);
				if(--this.limit == 0)
				{
					throw X.BREAK();
				}
			}
		};
	}

	public static final <E> Consumer<E> wrapWithPredicate(
		final Consumer<? super E>  target   ,
		final Predicate<? super E> predicate
	)
	{
		return e ->
		{
			if(!predicate.test(e))
			{
				return; // debug hook
			}
			target.accept(e);
		};
	}

	public static <E> Consumer<E> wrapWithPredicateSkip(
		final Consumer<? super E>  target   ,
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
				target.accept(e);
			}
		};
	}

	public static <E> Consumer<E> wrapWithPredicateLimit(
		final Consumer<? super E>  target   ,
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
				target.accept(e);
				if(--this.limit == 0)
				{
					throw X.BREAK();
				}
			}
		};
	}

	public static <E> Consumer<E> wrapWithPredicateSkipLimit(
		final Consumer<? super E>  target   ,
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
				target.accept(e);
				if(--this.limit == 0)
				{
					throw X.BREAK();
				}
			}
		};
	}

	public static final <I, O> Consumer<I> wrapWithFunction(
		final Consumer<? super O>    target  ,
		final Function<? super I, O> function
	)
	{
		return e ->
		{
			target.accept(function.apply(e));
		};
	}

	public static <I, O> Consumer<I> wrapWithFunctionSkip(
		final Consumer<? super O>    target  ,
		final Function<? super I, O> function,
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
				target.accept(function.apply(e));
			}
		};
	}

	public static <I, O> Consumer<I> wrapWithFunctionLimit(
		final Consumer<? super O>    target  ,
		final Function<? super I, O> function,
		final long                   limit
	)
	{
		return new AbstractProcedureLimit<I>(limit)
		{
			@Override
			public void accept(final I e)
			{
				target.accept(function.apply(e));
				if(--this.limit == 0)
				{
					throw X.BREAK();
				}
			}
		};
	}

	public static <I, O> Consumer<I> wrapWithFunctionSkipLimit(
		final Consumer<? super O>    target  ,
		final Function<? super I, O> function,
		final long                   skip    ,
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
				target.accept(function.apply(e));
				if(--this.limit == 0)
				{
					throw X.BREAK();
				}
			}
		};
	}

	public static final <I, O> Consumer<I> wrapWithPredicateFunction(
		final Consumer<? super O>    target   ,
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
			target.accept(function.apply(e));
		};
	}

	public static <I, O> Consumer<I> wrapWithPredicateFunctionSkip(
		final Consumer<? super O>    target   ,
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
				target.accept(function.apply(e));
			}
		};
	}

	public static <I, O> Consumer<I> wrapWithPredicateFunctionLimit(
		final Consumer<? super O>    target   ,
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
				target.accept(function.apply(e));
				if(--this.limit == 0)
				{
					throw X.BREAK();
				}
			}
		};
	}

	public static <I, O> Consumer<I> wrapWithPredicateFunctionSkipLimit(
		final Consumer<? super O>    target   ,
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
				target.accept(function.apply(e));
				if(--this.limit == 0)
				{
					throw X.BREAK();
				}
			}
		};
	}

	/**
	 * Pass-through function with type upcast. Can sometimes be required to correctly handle nested types.
	 * <p>
	 * Consider the following example with V1 extends V:
	 * (e.g. V is an interface and V1 is an implementation of V)
	 * <code><pre>{@code
	 *XMap<K, V1> workingCollection = ... ;
	 *XImmutableMap<K, V> finalCollection = ConstHashTable.NewProjected(input, <K>passthrough(), <V1, V>upcast());
	 * }</pre></code>
	 *
	 *
	 */
	@SuppressWarnings("unchecked")
	public static final <T extends S, S> Function<T, S> upcast()
	{
		return (Function<T, S>)passThrough();
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// member types //
	/////////////////
		
	public static final class MaxInteger implements Aggregator<Integer, Integer>
	{
		private int max;
	
		public MaxInteger(final int max)
		{
			super();
			this.max = max;
		}
	
		@Override
		public final void accept(final Integer value)
		{
			if(value != null && value > this.max)
			{
				this.max = value;
			}
		}
	
		@Override
		public final Integer yield()
		{
			return this.max;
		}
	
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////
	
	/**
	 * Dummy constructor to prevent instantiation of this static-only utility class.
	 * 
	 * @throws UnsupportedOperationException
	 */
	private XFunc()
	{
		// static only
		throw new UnsupportedOperationException();
	}

}

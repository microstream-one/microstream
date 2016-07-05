package net.jadoth.functional;

import java.util.function.Predicate;

import net.jadoth.util.Equalator;

public final class JadothPredicates
{
	///////////////////////////////////////////////////////////////////////////
	// static methods    //
	/////////////////////

	/**
	 * Functional alias for{@code return true;}.
	 */
	public static final <T> Predicate<T> all()
	{
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
		return e -> false;
	}

	/**
	 * Functional alias for {@code return e != null;}.
	 */
	public static <T> Predicate<T> notNull()
	{
		return e -> e != null;
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



	private JadothPredicates()
	{
		// static only
		throw new UnsupportedOperationException();
	}

}

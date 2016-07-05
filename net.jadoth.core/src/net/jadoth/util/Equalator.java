package net.jadoth.util;

import java.util.function.Predicate;

/**
 * @author Thomas Muenz
 *
 */
public interface Equalator<T>
{
	public boolean equal(T object1, T object2);



	public default Predicate<T> sample(final T sample)
	{
		return e -> this.equal(e, sample);
	}


	public interface Provider<T>
	{
		public Equalator<T> provideEqualator();
	}


	/**
	 * Useful for implementing SQL-like "GROUP BY" for collections.
	 *
	 * @author Thomas Muenz
	 *
	 */
	public class Sequence<T> implements Equalator<T>
	{
		private final Equalator<? super T>[] equalators;

		@SafeVarargs
		public Sequence(final Equalator<? super T>... equalators)
		{
			super();
			this.equalators = equalators;
		}

		@Override
		public boolean equal(final T o1, final T o2)
		{
			for(final Equalator<? super T> eq : this.equalators)
			{
				if(!eq.equal(o1, o2))
				{
					return false;
				}
			}
			return true;
		}

	}

}


/*
	// computer scientist's humor :D (Higher Order humor :DD)
	public static final Equalator<Human> HUMAN_RIGHTS = new Equalator<Human>()
	{
		@Override
		public boolean equal(final Human being1, final Human being2)
		{
			return true; // all human beings are equal
		}
	};
 */

package one.microstream.equality;

import static one.microstream.X.notNull;

import java.util.Comparator;
import java.util.function.Predicate;

import one.microstream.hashing.XHashing;

/**
 * A equalator function which checks if two given objects are equal or not.
 * Since it is very close to the {@link java.util.Comparator}, but has limited return value,
 * it is possible to wrap an existing comparator with the {@link #Wrap(Comparator)} method.
 * 
 * @param <T> type of objects to compare
 *
 */
public interface Equalator<T>
{
	/**
	 * Compares the two given objects (object1 and object2) depending on the implementation.
	 * 
	 * @param object1 as first object to check equality on
	 * @param object2 as second object to check equality on
	 * @return {@code true} if object1 equals object2, {@code false} if not.
	 */
	public boolean equal(T object1, T object2);


	/**
	 * Creates a {@link java.util.function.Predicate} that checks
	 * every given object with equality
	 * against the set sample.
	 * 
	 * @param sample which is compared
	 * @return function that compares the input of the function with the given sample
	 */
	public default Predicate<T> sample(final T sample)
	{
		return e -> this.equal(e, sample);
	}

	/**
	 * Wraps a given {@link java.util.Comparator} into an {@link Equalator}.
	 * Resulting Equalator returns <code>true</code> if the comparator return 0.
	 * In all other cases <code>false</code> is returned.
	 * 
	 * @param <E> type of objects to compare
	 * @param comparator to wrap insinde a new Equalator
	 * @return a new Equalator which executes the given comparator
	 */
	public static <E> Equalator<E> Wrap(final Comparator<? super E> comparator)
	{
		return new Equalator.ComparatorWrapper<>(
			notNull(comparator)
		);
	}
	
	/**
	 * @param <E> type of objects to compare
	 * @param equalators to chain together
	 * @return Chained {@link Equalator} and only if all Equalators return <code>true</code>,
	 * this created Equalator returns <code>true</code>.
	 */
	@SafeVarargs
	public static <E> Equalator<E> Chain(final Equalator<? super E>... equalators)
	{
		return new Equalator.Sequence<>(
			notNull(equalators)
		);
	}
	
	/**
	 * @param <E> type of objects to compare
	 * @return Static equalator which defines equality through the {@link Object#equals(Object)} method.
	 */
	public static <E> Equalator<E> value()
	{
		// reusing the same instances has neither memory nor performance disadvantages, only referential advantages
		return XHashing.hashEqualityValue();
	}

	/**
	 * @param <E> type of objects to compare
	 * @return Static equalator which defines equality through the  <code>'=='</code>-Operator.
	 */
	public static <E> Equalator<E> identity()
	{
		// reusing the same instances has neither memory nor performance disadvantages, only referential advantages
		return XHashing.hashEqualityIdentity();
	}


	public interface Provider<T>
	{
		public Equalator<T> provideEqualator();
	}


	/**
	 * Useful for implementing SQL-like "GROUP BY" for collections.
	 *
	 * 
	 *
	 */
	public final class Sequence<T> implements Equalator<T>
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private final Equalator<? super T>[] equalators;


		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		Sequence(final Equalator<? super T>[] equalators)
		{
			super();
			this.equalators = equalators;
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		public final boolean equal(final T instance1, final T instance2)
		{
			for(final Equalator<? super T> equalator : this.equalators)
			{
				if(!equalator.equal(instance1, instance2))
				{
					return false;
				}
			}
			
			return true;
		}

	}
	
	public final class ComparatorWrapper<T> implements Equalator<T>
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private final Comparator<? super T> comparator;

		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		ComparatorWrapper(final Comparator<? super T> comparator)
		{
			super();
			this.comparator = comparator;
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		@Override
		public boolean equal(final T object1, final T object2)
		{
			return this.comparator.compare(object1, object2) == 0;
		}
		
	}
	
}

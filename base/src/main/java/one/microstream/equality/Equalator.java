package one.microstream.equality;

import static one.microstream.X.notNull;

import java.util.Comparator;
import java.util.function.Predicate;

import one.microstream.hashing.XHashing;

/**
 * 
 *
 */
public interface Equalator<T>
{
	public boolean equal(T object1, T object2);



	public default Predicate<T> sample(final T sample)
	{
		return e -> this.equal(e, sample);
	}

	
	public static <E> Equalator<E> Wrap(final Comparator<? super E> comparator)
	{
		return new Equalator.ComparatorWrapper<>(
			notNull(comparator)
		);
	}
	
	@SafeVarargs
	public static <E> Equalator<E> Chain(final Equalator<? super E>... equalators)
	{
		return new Equalator.Sequence<>(
			notNull(equalators)
		);
	}
	

	public static <E> Equalator<E> value()
	{
		// reusing the same instances has neither memory nor performance disadvantages, only referential advantages
		return XHashing.hashEqualityValue();
	}

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

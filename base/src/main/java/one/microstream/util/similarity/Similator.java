package one.microstream.util.similarity;

import java.util.Comparator;

import one.microstream.equality.Equalator;

/**
 * Function type that is used to determine the similarity of objects with compatible types.
 * <p>
 * This is similar (pun) to {@link Comparator} or {@link Equalator}, but aims at more fine grained comparison,
 * e.g. for integrating String similarity heuristics like
 * <pre>
 * if(similarName.evaluate("Jack", "Jake") &gt; 0.5) {...}
 * </pre>
 * <p>
 * To not confound the admitted strange name "Similator" with "Simulator" or "Assimilator"
 * (the building where the Protoss get their gas from :D)
 *
 * 
 *
 */
@FunctionalInterface
public interface Similator<T>
{
	public double evaluate(T o1, T o2);



	public interface Provider<T>
	{
		public Similator<T> provideSimilator();
	}

	public final class Sequence<T> implements Similator<T>
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		private final Similator<? super T>[] similators;



		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		@SafeVarargs
		public Sequence(final Similator<? super T>... similators)
		{
			super();
			this.similators = similators;
		}



		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		public double evaluate(final T o1, final T o2)
		{
			double result = 0.0;
			// fields not cached as local variables as array is not expected to be long enough to pay off.
			for(int i = 0; i < this.similators.length; i++)
			{
				// spare foreach's unnecessary local variable
				result += this.similators[i].evaluate(o1, o2);
			}
			return result / this.similators.length;
		}

	}

}

package one.microstream.functional;

import java.util.Comparator;

/**
 * Helper class to chain multiple {@link Comparator} functions together as a super {@link Comparator}.<br>
 * Useful for implementing SQL-like "ORDER BY" for querying / processing collections.
 *
 * 
 */
public class ComparatorSequence<T> implements Comparator<T>
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	private final Comparator<? super T>[] comparators;



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	@SafeVarargs
	public ComparatorSequence(final Comparator<? super T>... comparators)
	{
		super();
		this.comparators = comparators;
	}



	///////////////////////////////////////////////////////////////////////////
	// override methods //
	/////////////////////

	@Override
	public int compare(final T o1, final T o2)
	{
		// fields not cached as local variables as array is not expected to be long enough to pay off. Or VM does it.
		for(int c, i = 0; i < this.comparators.length; i++)
		{
			// spare foreach's unnecessary local variable
			if((c = this.comparators[i].compare(o1, o2)) != 0)
			{
				return c;
			}
		}
		return 0;
	}

}

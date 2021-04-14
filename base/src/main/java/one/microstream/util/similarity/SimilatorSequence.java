package one.microstream.util.similarity;

/**
 *
 * 
 *
 * @param <T>
 */
public class SimilatorSequence<T> implements Similator<T>
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	private final Similator<? super T>[] similators;



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	@SafeVarargs
	public SimilatorSequence(final Similator<? super T>... comparators)
	{
		super();
		this.similators = comparators;
	}



	///////////////////////////////////////////////////////////////////////////
	// override methods //
	/////////////////////

	@Override
	public double evaluate(final T o1, final T o2)
	{
		double result = 0.0;
		// fields not cached as local variables as array is not expected to be long enough to pay off. Or VM does it.
		for(int i = 0; i < this.similators.length; i++)
		{
			// spare foreach's unnecessary local variable
			result += this.similators[i].evaluate(o1, o2);
		}
		return result / this.similators.length;
	}

}

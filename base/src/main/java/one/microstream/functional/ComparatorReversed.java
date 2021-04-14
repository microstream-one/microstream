package one.microstream.functional;

import java.util.Comparator;


/**
 *
 * 
 */
public class ComparatorReversed<T> implements Comparator<T>
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	private final Comparator<? super T> comparator;



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public ComparatorReversed(final Comparator<? super T> comparator)
	{
		super();
		this.comparator = comparator;
	}



	///////////////////////////////////////////////////////////////////////////
	// override methods //
	/////////////////////

	@Override
	public int compare(final T o1, final T o2)
	{
		return -this.comparator.compare(o1, o2);
	}

}

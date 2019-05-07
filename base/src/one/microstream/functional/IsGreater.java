package one.microstream.functional;

import java.util.Comparator;
import java.util.function.Predicate;


public class IsGreater<E> implements Predicate<E>
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	private final Comparator<? super E> comparator;
	private       E                     currentMax;



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public IsGreater(final Comparator<? super E> comparator)
	{
		super();
		this.comparator = comparator;
		this.currentMax = null;
	}



	///////////////////////////////////////////////////////////////////////////
	// override methods //
	/////////////////////

	@Override
	public boolean test(final E element)
	{
		if(this.comparator.compare(this.currentMax, element) < 0)
		{
			this.currentMax = element;
			return true;
		}
		return false;
	}

}

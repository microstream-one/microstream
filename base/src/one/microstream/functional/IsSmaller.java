package one.microstream.functional;

import java.util.Comparator;
import java.util.function.Predicate;


public class IsSmaller<E> implements Predicate<E>
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	private final Comparator<? super E> comparator;
	private       E                     currentMin;



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public IsSmaller(final Comparator<? super E> comparator)
	{
		super();
		this.comparator = comparator;
		this.currentMin = null;
	}



	///////////////////////////////////////////////////////////////////////////
	// override methods //
	/////////////////////

	@Override
	public boolean test(final E element)
	{
		if(this.comparator.compare(element, this.currentMin) < 0)
		{
			this.currentMin = element;
			return false;
		}
		return true;
	}

}

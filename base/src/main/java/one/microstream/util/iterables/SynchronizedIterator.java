package one.microstream.util.iterables;

import java.util.Iterator;

/**
 * 
 *
 */
public class SynchronizedIterator<E> implements Iterator<E>
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	private final Iterator<E> iterator;



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public SynchronizedIterator(final Iterator<E> iterator)
	{
		super();
		this.iterator = iterator;
	}



	///////////////////////////////////////////////////////////////////////////
	// override methods //
	/////////////////////

	@Override
	public synchronized boolean hasNext()
	{
		return this.iterator.hasNext();
	}

	@Override
	public synchronized void remove()
	{
		this.iterator.remove();
	}

	@Override
	public synchronized E next()
	{
		return this.iterator.next();
	}

}

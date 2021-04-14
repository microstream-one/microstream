package one.microstream.util.iterables;

import java.util.ListIterator;

/**
 * 
 *
 */
public class SynchronizedListIterator<E> implements ListIterator<E>
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	private final ListIterator<E> iterator;



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public SynchronizedListIterator(final ListIterator<E> iterator)
	{
		super();
		this.iterator = iterator;
	}



	///////////////////////////////////////////////////////////////////////////
	// override methods //
	/////////////////////

	@Override
	public synchronized void add(final E e)
	{
		this.iterator.add(e);
	}

	@Override
	public synchronized boolean hasNext()
	{
		return this.iterator.hasNext();
	}

	@Override
	public synchronized boolean hasPrevious()
	{
		return this.iterator.hasPrevious();
	}

	@Override
	public synchronized E next()
	{
		return this.iterator.next();
	}

	@Override
	public synchronized int nextIndex()
	{
		return this.iterator.nextIndex();
	}

	@Override
	public synchronized E previous()
	{
		return this.iterator.previous();
	}

	@Override
	public synchronized int previousIndex()
	{
		return this.iterator.previousIndex();
	}

	@Override
	public synchronized void remove()
	{
		this.iterator.remove();
	}

	@Override
	public synchronized void set(final E e)
	{
		this.iterator.set(e);
	}

}

package one.microstream.util.iterables;

import java.util.ListIterator;
import java.util.NoSuchElementException;

/**
 * @author Thomas Muenz
 *
 */
public final class ArrayListIterator<E> implements ListIterator<E>
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields  //
	/////////////////////

	private final E[] array;
	private final int length;
	private int index;
	private int lastRet;



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public ArrayListIterator(final E[] array)
	{
		super();
		this.array = array;
		this.length = array.length;
		this.index = 0;
		this.lastRet = -1;
	}

	public ArrayListIterator(final E[] array, final int index)
	{
		super();
		this.array = array;
		this.length = array.length;
		if(index < 0 || index >= this.length)
		{
			throw new ArrayIndexOutOfBoundsException(index);
		}
		this.index = index;
		this.lastRet = -1;
	}

	/**
	 *
	 * @param e
	 * @throws UnsupportedOperationException
	 * @see java.util.ListIterator#add(java.lang.Object)
	 */
	@Override
	public void add(final E e) throws UnsupportedOperationException
	{
		throw new UnsupportedOperationException();

	}

	/**
	 * @return
	 * @see java.util.ListIterator#hasNext()
	 */
	@Override
	public boolean hasNext()
	{
		return this.index < this.length;
	}

	/**
	 * @return
	 * @see java.util.ListIterator#hasPrevious()
	 */
	@Override
	public boolean hasPrevious()
	{
		return this.index > 0;
	}

	/**
	 * @return
	 * @see java.util.ListIterator#next()
	 */
	@Override
	public E next()
	{
		try
		{
			final int i;
			final E e = this.array[i = this.index];
			this.lastRet = i;
			this.index = i + 1;
			return e;
		}
		catch(final IndexOutOfBoundsException e)
		{
			throw new NoSuchElementException();
		}
	}

	/**
	 * @return
	 * @see java.util.ListIterator#nextIndex()
	 */
	@Override
	public int nextIndex()
	{
		return this.index;
	}

	/**
	 * @return
	 * @see java.util.ListIterator#previous()
	 */
	@Override
	public E previous()
	{
		if(this.index == 0)
		{
			throw new NoSuchElementException();
		}
		int i;
		final E e = this.array[i = this.index - 1];
		this.lastRet = this.index = i;
		return e;
	}

	/**
	 * @return
	 * @see java.util.ListIterator#previousIndex()
	 */
	@Override
	public int previousIndex()
	{
		return this.index - 1;
	}

	/**
	 *
	 * @throws UnsupportedOperationException
	 * @see java.util.ListIterator#remove()
	 */
	@Override
	public void remove() throws UnsupportedOperationException
	{
		throw new UnsupportedOperationException();
	}

	/**
	 * @param e
	 * @see java.util.ListIterator#set(java.lang.Object)
	 */
	@Override
	public void set(final E e)
	{
		if(this.lastRet == -1)
		{
			throw new IllegalStateException();
		}

		this.array[this.lastRet] = e;
	}

}

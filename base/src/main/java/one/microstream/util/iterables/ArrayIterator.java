package one.microstream.util.iterables;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * 
 *
 */
public final class ArrayIterator<E> implements Iterator<E>
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	private final E[] array;
	private final int length;
	private int index;



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public ArrayIterator(final E[] array)
	{
		super();
		this.array = array;
		this.length = array.length;
		this.index = 0;
	}



	///////////////////////////////////////////////////////////////////////////
	// override methods //
	/////////////////////

	/**
	 * @see java.util.Iterator#hasNext()
	 */
	@Override
	public boolean hasNext()
	{
		return this.index < this.length;
	}

	/**
	 * @see java.util.Iterator#next()
	 */
	@Override
	public E next()
	{
		try
		{
			final int i;
			final E e = this.array[i = this.index];
			this.index = i + 1;
			return e;
		}
		catch(final IndexOutOfBoundsException e)
		{
			throw new NoSuchElementException();
		}
	}

	/**
	 *
	 * @throws UnsupportedOperationException
	 * @see java.util.Iterator#remove()
	 */
	@Override
	public void remove() throws UnsupportedOperationException
	{
		throw new UnsupportedOperationException();
	}

}

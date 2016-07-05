package net.jadoth.util.iterables;

import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;

import net.jadoth.Jadoth;
import net.jadoth.collections.types.XGettingEnum;
import net.jadoth.collections.types.XGettingList;
import net.jadoth.collections.types.XGettingSequence;
import net.jadoth.collections.types.XList;
import net.jadoth.exceptions.IndexBoundsException;

/**
 * Generic (and potentially imperformant!) implementation of a {@link ListIterator}.<br>
 * Routes all modifying procedures ({@link #add(Object)}, {@link #remove()}, {@link #set(Object)}) to the wrapped
 * {@link List} which may throw an {@link UnsupportedOperationException} if it does not support the procedure.
 * <p>
 * If the use of an Iterator is not mandatory (e.g. through an API), it is strongly recommended to instead use
 * higher order programming concepts from "Collection 2.0" types like {@link XGettingList}, {@link XList}, etc.
 * and their functional procedures like {@link XGettingList#accept(net.jadoth.lang.functional.Operation)}, etc.
 * <p>
 * Note that while implementing {@link ListIterator}, this iterator can handle any sequence as it is read only,
 * meaning it can be used for iterating e.g. {@link XGettingEnum} collections as well.
 *
 * @author Thomas Muenz
 *
 */
public class ReadOnlyListIterator<E> implements ListIterator<E>
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields  //
	/////////////////////

	private final XGettingSequence<E> subject;
	private int index;



	///////////////////////////////////////////////////////////////////////////
	// constructors     //
	/////////////////////

	public ReadOnlyListIterator(final XGettingSequence<E> list) throws IndexBoundsException
	{
		super();
		this.subject = list;
		this.index   =    0;
	}

	public ReadOnlyListIterator(final XGettingSequence<E> list, final int index) throws IndexBoundsException
	{
		super();
		this.subject = list;
		if(index < 0 || index >= Jadoth.to_int(list.size()))
		{
			throw new IndexBoundsException(Jadoth.to_int(list.size()), index);
		}
		this.index = index;
	}



	///////////////////////////////////////////////////////////////////////////
	// override methods //
	/////////////////////

	@Override
	public void add(final E e) throws UnsupportedOperationException
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean hasNext()
	{
		return this.index < Jadoth.to_int(this.subject.size()); // list size could have changed meanwhile
	}

	@Override
	public boolean hasPrevious()
	{
		// list size could have changed meanwhile
		return this.index > 0 && this.index <= Jadoth.to_int(this.subject.size());
	}

	@Override
	public E next() throws NoSuchElementException
	{
		try
		{
			final int i;
			final E e = this.subject.at(i = this.index);
			this.index = i + 1;
			return e;
		}
		catch(final IndexOutOfBoundsException e)
		{
			throw new NoSuchElementException();
		}
	}

	@Override
	public int nextIndex()
	{
		return this.index;
	}

	@Override
	public E previous() throws NoSuchElementException
	{
		try
		{
			return this.subject.at(--this.index);
		}
		catch(final IndexOutOfBoundsException e)
		{
			throw new NoSuchElementException();
		}
	}

	@Override
	public int previousIndex()
	{
		return this.index - 1;
	}

	@Override
	public void remove() throws NoSuchElementException, UnsupportedOperationException
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void set(final E e) throws NoSuchElementException, UnsupportedOperationException
	{
		throw new UnsupportedOperationException();
	}

}

package net.jadoth.util.iterables;

import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;

import net.jadoth.collections.types.XGettingList;
import net.jadoth.collections.types.XList;
import net.jadoth.exceptions.IndexBoundsException;
import net.jadoth.typing.JadothTypes;

/**
 * Generic (and potentially imperformant!) implementation of a {@link ListIterator}.<br>
 * Routes all modifying procedures ({@link #add(Object)}, {@link #remove()}, {@link #set(Object)}) to the wrapped
 * {@link List} which may throw an {@link UnsupportedOperationException} if it does not support the procedure.
 * <p>
 * If the use of an Iterator is not mandatory (e.g. through an API), it is strongly recommended to instead use
 * Higher Order Programming concepts from "Collection 2.0" types like {@link XGettingList}, {@link XList}, etc.
 * and their functional procedures like {@link XGettingList#accept(net.jadoth.lang.functional.Operation)}, etc.
 *
 * @author Thomas Muenz
 *
 */
public class GenericListIterator<E> implements ListIterator<E>
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields  //
	/////////////////////

	private final XList<E> list;
	private int index;
	private int lastReturnedIndex;



	///////////////////////////////////////////////////////////////////////////
	// constructors     //
	/////////////////////

	public GenericListIterator(final XList<E> list) throws IndexBoundsException
	{
		super();
		this.list    = list;
		this.index   =    0;
		this.lastReturnedIndex =   -1;
	}

	public GenericListIterator(final XList<E> list, final int index) throws IndexBoundsException
	{
		super();
		this.list = list;
		/* (20.11.2011)NOTE:
		 * the definition of java.util.List#listIterator(int) is flawed and should not be used.
		 *
		 * The exception definition says:
		 * throws IndexOutOfBoundsException if the index is out of range ({@code index < 0 || index > size()})
		 *
		 * JDK developers don't seem to understand that size() is not a valid value for a 0-based index.
		 * Passing such an invalid value must cause an exception, otherwise it is a carried off bug.
		 * That their implemented logic aborts safely for that invalid index doesn't make the flawed interface
		 * definition correct. Some other implementation might let the invalid index value pass and create
		 * wrong behavior, despite fulfilling the defined contract.
		 * There is a huge difference between some hacky, but accidentally working implementation and a correct
		 * interface contract definition. They did the former, but not the latter. Their interface is flawed and
		 * should not be used.
		 * Not to mention their fail fast bug and their plain string exception data instead of proper structures.
		 *
		 * With all that incompetence that can be found in many other JDK classes, it's a miracle the JDK works
		 * in the first place. Somehow ... mostly. Still, if one wants proper non-moronic code, one has to rewrite
		 * everything.
		 *
		 * The extended collection's backward-compatibility #listIterator(int) throws the correct exception in this
		 * case, deliberately breaking the flawed contract.
		 */
		if(index < 0 || index >= list.size())
		{
			throw new IndexBoundsException(JadothTypes.to_int(list.size()), index);
		}
		this.index = index;
		this.lastReturnedIndex = -1;
	}



	///////////////////////////////////////////////////////////////////////////
	// override methods //
	/////////////////////

	@Override
	public void add(final E e) throws UnsupportedOperationException
	{
		this.list.add(e);
	}

	@Override
	public boolean hasNext()
	{
		return this.index < JadothTypes.to_int(this.list.size()); // list size could have changed meanwhile
	}

	@Override
	public boolean hasPrevious()
	{
		// list size could have changed meanwhile
		return this.index > 0 && this.index <= JadothTypes.to_int(this.list.size());
	}

	@Override
	public E next() throws NoSuchElementException
	{
		try
		{
			final int i;
			final E e = this.list.at(i = this.index);
			this.lastReturnedIndex = i;
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
			final int i;
			final E e = this.list.at(i = this.index - 1);
			this.lastReturnedIndex = this.index = i;
			return e;
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
		if(this.lastReturnedIndex == -1)
		{
			throw new IllegalStateException();
		}

		try
		{
			this.list.removeAt(this.lastReturnedIndex);
			if(this.lastReturnedIndex < this.index)
			{
				this.index--;
			}
			this.lastReturnedIndex = -1;
		}
		catch(final IndexOutOfBoundsException e)
		{
			throw new NoSuchElementException();
		}
	}

	@Override
	public void set(final E e) throws NoSuchElementException, UnsupportedOperationException
	{
		if(this.lastReturnedIndex == -1)
		{
			throw new IllegalStateException();
		}

		try
		{
			this.list.setGet(this.lastReturnedIndex, e);
		}
		catch(final IndexOutOfBoundsException ex)
		{
			throw new NoSuchElementException();
		}
	}

}

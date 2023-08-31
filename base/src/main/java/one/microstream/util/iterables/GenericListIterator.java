package one.microstream.util.iterables;

/*-
 * #%L
 * microstream-base
 * %%
 * Copyright (C) 2019 - 2022 MicroStream Software
 * %%
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 * 
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the Eclipse
 * Public License, v. 2.0 are satisfied: GNU General Public License, version 2
 * with the GNU Classpath Exception which is
 * available at https://www.gnu.org/software/classpath/license.html.
 * 
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 * #L%
 */

import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;

import one.microstream.collections.types.XGettingList;
import one.microstream.collections.types.XList;
import one.microstream.exceptions.IndexBoundsException;
import one.microstream.typing.XTypes;

/**
 * Generic (and potentially not performant!) implementation of a {@link ListIterator}.<br>
 * Routes all modifying procedures ({@link #add(Object)}, {@link #remove()}, {@link #set(Object)}) to the wrapped
 * {@link List} which may throw an {@link UnsupportedOperationException} if it does not support the procedure.
 * <p>
 * If the use of an Iterator is not mandatory (e.g. through an API), it is strongly recommended to instead use
 * Higher Order Programming concepts from "Collection 2.0" types like {@link XGettingList}, {@link XList}, etc.
 * and their functional procedures etc.
 *
 */
public class GenericListIterator<E> implements ListIterator<E>
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	private final XList<E> list;
	private int index;
	private int lastReturnedIndex;



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

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
		 *		 *
		 * The extended collection's backward-compatibility #listIterator(int) throws the correct exception in this
		 * case, deliberately breaking the flawed contract.
		 */
		if(index < 0 || index >= list.size())
		{
			throw new IndexBoundsException(XTypes.to_int(list.size()), index);
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
		return this.index < XTypes.to_int(this.list.size()); // list size could have changed meanwhile
	}

	@Override
	public boolean hasPrevious()
	{
		// list size could have changed meanwhile
		return this.index > 0 && this.index <= XTypes.to_int(this.list.size());
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

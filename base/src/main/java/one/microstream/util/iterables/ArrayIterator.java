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

import java.util.Iterator;
import java.util.NoSuchElementException;

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
	 * @throws UnsupportedOperationException because this operation is not supported
	 * @see java.util.Iterator#remove()
	 */
	@Override
	public void remove() throws UnsupportedOperationException
	{
		throw new UnsupportedOperationException();
	}

}

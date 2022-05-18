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

import java.util.ListIterator;
import java.util.NoSuchElementException;

import one.microstream.reference.Referencing;


public class TrivialIterator<E> implements ListIterator<E>
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////
	
	Referencing<E> parent;
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////
	
	public TrivialIterator(final Referencing<E> parent)
	{
		super();
		this.parent = parent;
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// override methods //
	/////////////////////

	@Override
	public boolean hasNext()
	{
		return this.parent != null;
	}

	@Override
	public E next()
	{
		final E element = this.parent.get();
		this.parent = null;
		return element;
	}

	@Override
	public void remove()
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean hasPrevious()
	{
		return false;
	}

	@Override
	public E previous()
	{
		throw new NoSuchElementException();
	}

	@Override
	public int nextIndex()
	{
		return 1;
	}

	@Override
	public int previousIndex()
	{
		return -1;
	}

	@Override
	public void set(final E e)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void add(final E e)
	{
		throw new UnsupportedOperationException();
	}

}

package one.microstream.collections;

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

import one.microstream.collections.types.XAddingCollection;
import one.microstream.collections.types.XGettingCollection;
import one.microstream.concurrency.Synchronized;


public final class SynchAdder<E> implements XAddingCollection<E>, Synchronized
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	private final XAddingCollection<E> subject;



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public SynchAdder(final XAddingCollection<E> collection)
	{
		super();
		this.subject = collection;
	}



	///////////////////////////////////////////////////////////////////////////
	// override methods //
	/////////////////////

	@Override
	public final synchronized boolean nullAdd()
	{
		return this.subject.nullAdd();
	}

	@Override
	public final synchronized boolean add(final E e)
	{
		return this.subject.add(e);
	}

	@SafeVarargs
	@Override
	public final synchronized SynchAdder<E> addAll(final E... elements)
	{
		this.subject.addAll(elements);
		return this;
	}

	@Override
	public final synchronized SynchAdder<E> addAll(final XGettingCollection<? extends E> elements)
	{
		this.subject.addAll(elements);
		return this;
	}

	@Override
	public final synchronized SynchAdder<E> addAll(final E[] elements, final int offset, final int length)
	{
		this.subject.addAll(elements, offset, length);
		return this;
	}

	@Override
	public final synchronized void accept(final E e)
	{
		this.subject.accept(e);
	}

	@Override
	public final synchronized SynchAdder<E> ensureFreeCapacity(final long minimalFreeCapacity)
	{
		this.subject.ensureFreeCapacity(minimalFreeCapacity);
		return this;
	}

	@Override
	public final synchronized SynchAdder<E> ensureCapacity(final long minimalCapacity)
	{
		this.subject.ensureCapacity(minimalCapacity);
		return this;
	}

	@Override
	public final synchronized long currentCapacity()
	{
		return this.subject.currentCapacity();
	}

	@Override
	public final synchronized long maximumCapacity()
	{
		return this.subject.maximumCapacity();
	}

	@Override
	public final synchronized boolean isFull()
	{
		return this.subject.isFull();
	}

	@Override
	public final synchronized long remainingCapacity()
	{
		return this.subject.remainingCapacity();
	}

	@Override
	public final synchronized long optimize()
	{
		return this.subject.optimize();
	}

	@Override
	public final synchronized boolean hasVolatileElements()
	{
		return this.subject.hasVolatileElements();
	}

	@Override
	public final synchronized boolean nullAllowed()
	{
		return this.subject.nullAllowed();
	}

	@Override
	public final synchronized boolean isEmpty() throws UnsupportedOperationException
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public final synchronized long size() throws UnsupportedOperationException
	{
		throw new UnsupportedOperationException();
	}

}

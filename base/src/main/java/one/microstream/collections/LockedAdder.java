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
import one.microstream.typing.XTypes;


public final class LockedAdder<E> implements XAddingCollection<E>, Synchronized
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	private final XAddingCollection<E> subject;
	private final Object               lock   ;



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public LockedAdder(final XAddingCollection<E> collection)
	{
		super();
		this.subject = collection;
		this.lock    = collection;
	}

	public LockedAdder(final XAddingCollection<E> collection, final Object lock)
	{
		super();
		this.subject = collection;
		this.lock    = lock      ;
	}



	///////////////////////////////////////////////////////////////////////////
	// override methods //
	/////////////////////

	@Override
	public final boolean nullAdd()
	{
		synchronized(this.lock)
		{
			return this.subject.nullAdd();
		}
	}

	@Override
	public final boolean add(final E e)
	{
		synchronized(this.lock)
		{
			return this.subject.add(e);
		}
	}

	@SafeVarargs
	@Override
	public final LockedAdder<E> addAll(final E... elements)
	{
		synchronized(this.lock)
		{
			this.subject.addAll(elements);
			return this;
		}
	}

	@Override
	public final LockedAdder<E> addAll(final E[] elements, final int offset, final int length)
	{
		synchronized(this.lock)
		{
			this.subject.addAll(elements, offset, length);
			return this;
		}
	}

	@Override
	public final LockedAdder<E> addAll(final XGettingCollection<? extends E> elements)
	{
		synchronized(this.lock)
		{
			this.subject.addAll(elements);
			return this;
		}
	}

	@Override
	public final void accept(final E e)
	{
		synchronized(this.lock)
		{
			this.subject.accept(e);
		}
	}

	@Override
	public final LockedAdder<E> ensureFreeCapacity(final long minimalFreeCapacity)
	{
		synchronized(this.lock)
		{
			this.subject.ensureFreeCapacity(minimalFreeCapacity);
			return this;
		}
	}

	@Override
	public final LockedAdder<E> ensureCapacity(final long minimalCapacity)
	{
		synchronized(this.lock)
		{
			this.subject.ensureCapacity(minimalCapacity);
			return this;
		}
	}

	@Override
	public final long currentCapacity()
	{
		synchronized(this.lock)
		{
			return this.subject.currentCapacity();
		}
	}

	@Override
	public final long maximumCapacity()
	{
		synchronized(this.lock)
		{
			return this.subject.maximumCapacity();
		}
	}

	@Override
	public final boolean isFull()
	{
		synchronized(this.lock)
		{
			return XTypes.to_int(this.subject.size()) >= this.subject.maximumCapacity();
		}
	}

	@Override
	public final long remainingCapacity()
	{
		synchronized(this.lock)
		{
			return this.subject.remainingCapacity();
		}
	}

	@Override
	public final long optimize()
	{
		synchronized(this.lock)
		{
			return this.subject.optimize();
		}
	}

	@Override
	public final boolean hasVolatileElements()
	{
		synchronized(this.lock)
		{
			return this.subject.hasVolatileElements();
		}
	}

	@Override
	public final boolean nullAllowed()
	{
		synchronized(this.lock)
		{
			return this.subject.nullAllowed();
		}
	}

	@Override
	public final boolean isEmpty() throws UnsupportedOperationException
	{
		synchronized(this.lock)
		{
			throw new UnsupportedOperationException();
		}
	}

	@Override
	public final long size() throws UnsupportedOperationException
	{
		synchronized(this.lock)
		{
			throw new UnsupportedOperationException();
		}
	}

}

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

import static java.lang.System.identityHashCode;

import java.util.function.Consumer;

import one.microstream.collections.interfaces.OptimizableCollection;
import one.microstream.collections.types.XList;
import one.microstream.equality.IdentityEqualityLogic;
import one.microstream.functional._longProcedure;
import one.microstream.math.XMath;
import one.microstream.typing.Composition;
import one.microstream.typing.KeyValue;

/**
 * Primitive (read: fast) synchronized pseudo map implementation that maps long id values to weakly referenced objects.
 *
 */
public final class HashMapObjectId<T> implements OptimizableCollection, Composition, IdentityEqualityLogic
{
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////
	
	@SuppressWarnings("unchecked")
	private static <T> Entry<T>[] newHashSlots(final int length)
	{
		return new Entry[length];
	}

	public static final <T> HashMapObjectId<T> New()
	{
		return new HashMapObjectId<>(1);
	}

	public static final <T> HashMapObjectId<T> New(final int initialSlotLength)
	{
		return new HashMapObjectId<>(XMath.pow2BoundCapped(initialSlotLength));
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	private Entry<T>[] hashSlots;
	private int        hashRange;
	private int        size     ;



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////
	
	HashMapObjectId(final int initialSlotLength)
	{
		super();
		this.hashSlots  = newHashSlots(initialSlotLength);
		this.hashRange = initialSlotLength - 1;
	}



	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////

	@Override
	public long size()
	{
		return this.size;
	}

	@Override
	public boolean isEmpty()
	{
		return this.size == 0;
	}

	@SuppressWarnings("unchecked")
	private void rebuild(final int newCapacity)
	{
		final Entry<T>[] newSlots = new Entry[newCapacity]; // potential int overflow ignored deliberately
		final int newSlotCountMinusOne = newCapacity - 1;
		int index;
		for(Entry<T> entry : this.hashSlots)
		{
			for(Entry<T> next; entry != null; entry = next)
			{
				next = entry.next;
				entry.next = newSlots[index = identityHashCode(entry.ref) & newSlotCountMinusOne];
				newSlots[index] = entry;
			}
		}
		this.hashSlots = newSlots;
		this.hashRange = newSlotCountMinusOne;
	}

	public boolean add(final T object, final long id)
	{
		final int index;
		final Entry<T> head = this.hashSlots[index = identityHashCode(object) & this.hashRange];

		// case 1: new head entry
		if(head == null)
		{
			this.hashSlots[index] = new Entry<>(id, object);
			if(this.size++ >= this.hashRange)
			{
				this.rebuild(this.hashRange + 1 << 1);
			}
			return true;
		}

		// case 2: replace registered id
		if(head.ref == object)
		{
			return false;
		}
		for(Entry<T> entry = head.next; entry != null; entry = entry.next)
		{
			if(entry.ref == object)
			{
				return false;
			}
		}

		// case 3: append new entry
		this.hashSlots[index] = new Entry<>(id, object, head);
		if(this.size++ >= this.hashRange)
		{
			this.rebuild(this.hashRange + 1 << 1);
		}
		return true;
	}

	public boolean put(final T object, final long id)
	{
		final int index;
		final Entry<T> head = this.hashSlots[index = identityHashCode(object) & this.hashRange];

		// case 1: new head entry
		if(head == null)
		{
			this.hashSlots[index] = new Entry<>(id, object);
			if(this.size++ >= this.hashRange)
			{
				this.rebuild(this.hashRange + 1 << 1);
			}
			return true;
		}

		// case 2: replace registered id
		if(head.ref == object)
		{
			head.id = id;
			return false;
		}
		for(Entry<T> entry = head.next; entry != null; entry = entry.next)
		{
			if(entry.ref == object)
			{
				entry.id = id;
				return false;
			}
		}

		// case 3: append new entry
		this.hashSlots[index] = new Entry<>(id, object, head);
		if(this.size++ >= this.hashRange)
		{
			this.rebuild(this.hashRange + 1 << 1);
		}
		return true;
	}

	public long putGet(final T object, final long id, final long noOldIdValue)
	{
		final int index;
		final Entry<T> head = this.hashSlots[index = identityHashCode(object) & this.hashRange];

		// case 1: new head entry
		if(head == null)
		{
			this.hashSlots[index] = new Entry<>(id, object);
			if(this.size++ >= this.hashRange)
			{
				this.rebuild(this.hashRange + 1 << 1);
			}
			return noOldIdValue;
		}

		// case 2: replace registered object
		if(head.ref == object)
		{
			final long oldId = head.id;
			head.id = id;
			return oldId;
		}
		for(Entry<T> entry = head.next; entry != null; entry = entry.next)
		{
			if(entry.ref == object)
			{
				final long oldId = entry.id;
				entry.id = id;
				return oldId;
			}
		}

		// case 3: append new entry
		this.hashSlots[index] = new Entry<>(id, object, head);
		if(this.size++ >= this.hashRange)
		{
			this.rebuild(this.hashRange + 1 << 1);
		}
		return noOldIdValue;
	}

	public long get(final T object, final long notFoundId)
	{
		if(object == null)
		{
			throw new NullPointerException();
		}
		for(Entry<T> entry = this.hashSlots[identityHashCode(object) & this.hashRange]; entry != null; entry = entry.next)
		{
			if(entry.ref == object)
			{
				return entry.id;
			}
		}
		return notFoundId;
	}

	public XList<T> getObjects()
	{
		final BulkList<T> list = new BulkList<>(this.size);
		for(Entry<T> entry : this.hashSlots)
		{
			for(; entry != null; entry = entry.next)
			{
				list.add(entry.ref);
			}
		}
		return list;
	}

	public XList<Long> getIds()
	{
		final BulkList<Long> list = new BulkList<>(this.size);
		for(Entry<T> entry : this.hashSlots)
		{
			for(; entry != null; entry = entry.next)
			{
				list.add(entry.id);
			}
		}
		return list;
	}

	public int iterateObjects(final Consumer<? super T> procedure)
	{
		for(Entry<T> entry : this.hashSlots)
		{
			for(; entry != null; entry = entry.next)
			{
				procedure.accept(entry.ref);
			}
		}
		return this.size;
	}

	/**
	 * Optimizes the internal storage and returns the remaining amount of entries.
	 * @return the amount of entries after the optimization is been completed.
	 */
	@Override
	public long optimize()
	{
		final int newCapacity;
		if((newCapacity = XMath.pow2BoundCapped(this.size)) != this.hashSlots.length)
		{
			this.rebuild(newCapacity);
		}
		return this.size;
	}

	public int iterateIds(final Consumer<? super Long> procedure)
	{
		for(Entry<T> entry : this.hashSlots)
		{
			for(; entry != null; entry = entry.next)
			{
				procedure.accept(entry.id);
			}
		}
		return this.size;
	}

	public int iterateIds(final _longProcedure procedure)
	{
		for(Entry<T> entry : this.hashSlots)
		{
			for(; entry != null; entry = entry.next)
			{
				procedure.accept(entry.id);
			}
		}
		return this.size;
	}

	public void clear()
	{
		final Entry<T>[] slots = this.hashSlots;
		for(int i = 0, len = slots.length; i < len; i++)
		{
			slots[i] = null;
		}
		this.size = 0;
	}


	static final class Entry<T> implements KeyValue<T, Long>, Composition
	{
		long id;
		final T ref;
		Entry<T> next;

		Entry(final long id, final T object, final Entry<T> next)
		{
			super();
			this.id = id;
			this.ref = object;
			this.next = next;
		}

		Entry(final long id, final T object)
		{
			super();
			this.id = id;
			this.ref = object;
			this.next = null;
		}

		@Override
		public T key()
		{
			return this.ref;
		}

		@Override
		public Long value()
		{
			return this.id;
		}

	}

}

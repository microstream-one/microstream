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

import java.util.function.Consumer;

import one.microstream.chars.VarString;
import one.microstream.collections.interfaces.OptimizableCollection;
import one.microstream.collections.types.XList;
import one.microstream.functional._longProcedure;
import one.microstream.math.XMath;
import one.microstream.typing.Composition;
import one.microstream.typing.KeyValue;

/**
 * Primitive (read: fast) synchronized pseudo map implementation that maps long id values to weakly referenced objects.
 *
 */
public final class HashMapIdObject<E> implements OptimizableCollection, Composition
{
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////

	@SuppressWarnings("unchecked")
	private static <T> Entry<T>[] newHashSlots(final int length)
	{
		return new Entry[length];
	}

	public static final <T> HashMapIdObject<T> New()
	{
		return new HashMapIdObject<>(1);
	}

	public static final <T> HashMapIdObject<T> New(final int initialSlotLength)
	{
		return new HashMapIdObject<>(XMath.pow2BoundCapped(initialSlotLength));
	}



	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	private Entry<E>[] hashSlots;
	private int        hashRange;
	private int        size     ;



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	HashMapIdObject(final int initialSlotLength)
	{
		super();
		this.hashSlots = newHashSlots(initialSlotLength);
		this.hashRange = initialSlotLength - 1;
	}



	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////

	@Override
	public final long size()
	{
		return this.size;
	}

	@Override
	public final boolean isEmpty()
	{
		return this.size == 0;
	}

	private void rebuild(final int newLength)
	{
		if(this.hashSlots.length >= newLength || newLength <= 0)
		{
			return;
		}

		final int newRange = newLength - 1;
		final Entry<E>[] oldSlots = this.hashSlots, newSlots = newHashSlots(newLength);
		for(int i = 0; i < oldSlots.length; i++)
		{
			if(oldSlots[i] == null)
			{
				continue;
			}
			for(Entry<E> next, entry = oldSlots[i]; entry != null; entry = next)
			{
				next = entry.link;
				entry.link = newSlots[(int)(entry.id & newRange)];
				newSlots[(int)(entry.id & newRange)] = entry;
			}
		}
		this.hashSlots = newSlots;
		this.hashRange = newRange;
	}

	// (23.11.2018 TM)TODO: why are there 2 rebuild methods?
	@SuppressWarnings("unchecked")
	private void rebuild()
	{
		final int newModulo; // potential int overflow ignored deliberately
		final Entry<E>[] newSlots = new Entry[(newModulo = (this.hashRange + 1 << 1) - 1) + 1];
		for(Entry<E> entry : this.hashSlots)
		{
			for(Entry<E> next; entry != null; entry = next)
			{
				next = entry.link;
				entry.link = newSlots[(int)(entry.id & newModulo)];
				newSlots[(int)(entry.id & newModulo)] = entry;
			}
		}
		this.hashSlots = newSlots;
		this.hashRange = newModulo;
	}

	private void putEntry(final int index, final Entry<E> entry)
	{
		this.hashSlots[index] = entry;
		if(++this.size >= this.hashRange)
		{
			this.rebuild();
		}
	}

	// (31.03.2012 TM)XXX: !!! copy optimized hash-add() to all hash~ implementations
	public final boolean add(final long id, final E object)
	{
		final int index;
		Entry<E> entry;
		if((entry = this.hashSlots[index = (int)(id & this.hashSlots.length - 1)]) == null)
		{
			// simple case: hash slot is still empty, simply put a new entry
			this.putEntry(index, new Entry<>(id, object));
			return true;
		}

		// complex case: hash slot is not empty: scan through hash chain
		do
		{
			if(entry.id == id)
			{
				return false; // collision: add logic discards new object
			}
		}
		while((entry = entry.link) != null);

		// no collision found in hash chain: prepend a new entry
		this.putEntry(index, new Entry<>(id, object, this.hashSlots[index]));
		return true;
	}

	public final boolean put(final long id, final E object)
	{
		final int index;
		Entry<E> entry;
		if((entry = this.hashSlots[index = (int)(id & this.hashSlots.length - 1)]) == null)
		{
			// simple case: hash slot is still empty, simply put a new entry
			this.putEntry(index, new Entry<>(id, object));
			return true;
		}

		// complex case: hash slot is not empty: scan through hash chain
		do
		{
			if(entry.id == id)
			{
				entry.item = object; // collision: put logic replaces new object
				return false;
			}
		}
		while((entry = entry.link) != null);

		// no collision found in hash chain: prepend a new entry
		this.putEntry(index, new Entry<>(id, object, this.hashSlots[index]));
		return true;
	}

	public final E putGet(final long id, final E object)
	{
		final int index;
		final Entry<E> head = this.hashSlots[index = (int)(id & this.hashRange)];

		// case 1: new head entry
		if(head == null)
		{
			this.hashSlots[index] = new Entry<>(id, object);
			if(this.size++ >= this.hashRange)
			{
				this.rebuild(this.hashRange + 1 << 1);
			}
			return null;
		}

		// case 2: replace registered object
		if(head.id == id)
		{
			final E oldRef = head.item;
			head.item = object;
			return oldRef;
		}
		for(Entry<E> entry = head.link; entry != null; entry = entry.link)
		{
			if(entry.id == id)
			{
				final E oldRef = entry.item;
				entry.item = object;
				return oldRef;
			}
		}

		// case 3: append new entry
		this.hashSlots[index] = new Entry<>(id, object, head);
		if(this.size++ >= this.hashRange)
		{
			this.rebuild(this.hashRange + 1 << 1);
		}
		return null;
	}

	public final E get(final long id)
	{
		// ids are assumed to be roughly sequential, hence (id ^ id >>> 32) should not be necessary for distribution.
		for(Entry<E> entry = this.hashSlots[(int)(id & this.hashRange)]; entry != null; entry = entry.link)
		{
			if(entry.id == id)
			{
				return entry.item;
			}
		}
		return null;
	}

	public final XList<E> getObjects()
	{
		final BulkList<E> list = new BulkList<>(this.size);
		for(Entry<E> entry : this.hashSlots)
		{
			for(; entry != null; entry = entry.link)
			{
				list.add(entry.item);
			}
		}
		return list;
	}

	public final XList<Long> getIds()
	{
		final BulkList<Long> list = new BulkList<>(this.size);
		for(Entry<E> entry : this.hashSlots)
		{
			for(; entry != null; entry = entry.link)
			{
				list.add(entry.id);
			}
		}
		return list;
	}

	public final int iterateValues(final Consumer<? super E> procedure)
	{
		for(Entry<E> entry : this.hashSlots)
		{
			for(; entry != null; entry = entry.link)
			{
				procedure.accept(entry.item);
			}
		}
		return this.size;
	}

	@Override
	public final String toString()
	{
		final VarString vc = VarString.New().add('{');
		for(Entry<E> entry : this.hashSlots)
		{
			for(; entry != null; entry = entry.link)
			{
				vc.add(entry.id).add(" -> ").add(entry.item).add(", ");
			}
		}
		return vc.deleteLast().setLast('}').toString();
	}

	/**
	 * Optimizes the internal storage and returns the remaining amount of entries.
	 * @return the amount of entries after the optimization is been completed.
	 */
	@Override
	public final long optimize()
	{
		this.rebuild(XMath.pow2BoundCapped(this.size));
		return this.size;
	}

	public final int iterateKeys(final Consumer<? super Long> procedure)
	{
		for(Entry<E> entry : this.hashSlots)
		{
			for(; entry != null; entry = entry.link)
			{
				procedure.accept(entry.id);
			}
		}
		return this.size;
	}

	public final int iterateIds(final _longProcedure procedure)
	{
		for(Entry<E> entry : this.hashSlots)
		{
			for(; entry != null; entry = entry.link)
			{
				procedure.accept(entry.id);
			}
		}
		return this.size;
	}

	public final int iterate(final Consumer<? super KeyValue<Long, E>> procedure)
	{
		for(Entry<E> entry : this.hashSlots)
		{
			for(; entry != null; entry = entry.link)
			{
				procedure.accept(entry);
			}
		}
		return this.size;
	}

	public final void clear()
	{
		final Entry<E>[] slots = this.hashSlots;
		for(int i = 0, len = slots.length; i < len; i++)
		{
			slots[i] = null;
		}
		this.size = 0;
	}


	static final class Entry<E> implements KeyValue<Long, E>, Composition
	{
		final long id;
		E        item;
		Entry<E> link;

		Entry(final long id, final E item, final Entry<E> next)
		{
			super();
			this.id  = id;
			this.item = item;
			this.link = next;
		}

		Entry(final long id, final E item)
		{
			super();
			this.id  = id ;
			this.item = item;
			this.link = null;
		}

		@Override
		public final Long key()
		{
			return this.id;
		}

		@Override
		public final E value()
		{
			return this.item;
		}

		@Override
		public final String toString()
		{
			return '[' + String.valueOf(this.id) + " -> " + String.valueOf(this.item) + ']';
		}

	}

}

package one.microstream.util.traversing;

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

import java.util.Comparator;
import java.util.Iterator;
import java.util.function.Consumer;
import java.util.function.Predicate;

import one.microstream.collections.CapacityExceededException;
import one.microstream.collections.HashEnum;
import one.microstream.collections.interfaces.CapacityExtendable;
import one.microstream.collections.old.OldCollection;
import one.microstream.collections.types.XGettingCollection;
import one.microstream.collections.types.XImmutableSet;
import one.microstream.collections.types.XSet;
import one.microstream.equality.Equalator;
import one.microstream.math.XMath;


/**
 * Very primitive, but very fast open addressing hash {@link XSet} implementation.
 * It is considerable faster than general purpose hash collections like {@link HashEnum}, which use
 * chains instead of open addressing for resolving hash collisions. It also scales much better with higher element count.
 * <br>
 * Drawbacks:<br>
 * - only add() implemented so far
 * - even if fully implemented, the implementation would have no order and is technically restricted to a maximum
 *   element count equal to the maximum array length. Chain-based implementations do not have such a restriction.
 *
 * @param <E> type of contained elements
 */
public final class OpenAdressingMiniSet<E> implements XSet<E>
{
	///////////////////////////////////////////////////////////////////////////
	// constants //
	//////////////

	private static final int   DEFAULT_INITIAL_CAPACITY = 32  ;
	private static final float DEFAULT_HASH_DENSITY     = 1.0f;



	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////

	private static int padHashLength(final int minimalHashLength)
	{
		// check for technical limit
		if(XMath.isGreaterThanHighestPowerOf2(minimalHashLength))
		{
			return Integer.MAX_VALUE;
		}
		int capacity = 1;
		while(capacity < minimalHashLength)
		{
			capacity <<= 1;
		}
		return capacity;
	}

	private static int calculateHashRange(final int slotLength)
	{
		return slotLength >= Integer.MAX_VALUE ? Integer.MAX_VALUE : slotLength - 1;
	}

	@SuppressWarnings("unchecked")
	private static <E> E[] newArray(final int length)
	{
		return (E[])new Object[length];
	}


	public static <E> OpenAdressingMiniSet<E> New()
	{
		return new OpenAdressingMiniSet<>(DEFAULT_INITIAL_CAPACITY, DEFAULT_HASH_DENSITY);
	}

	public static <E> OpenAdressingMiniSet<E> New(final int initialCapacity)
	{
		return new OpenAdressingMiniSet<>(initialCapacity, DEFAULT_HASH_DENSITY);
	}

	public static <E> OpenAdressingMiniSet<E> New(final XGettingCollection<? extends E> elements)
	{
		final OpenAdressingMiniSet<E> set = New(elements.intSize());

		for(final E element : elements)
		{
			set.add(element);
		}

		return set;
	}



	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	private final float hashDensity;
	private       int   size       ;
	private       int   hashRange  ;
	private       int   capacity   ;
	private       E[]   hashtable  ;



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	OpenAdressingMiniSet(final int initialCapacity, final float hashDensity)
	{
		super();
		this.size        = 0;
		this.hashtable   = newArray(padHashLength(initialCapacity));
		this.hashRange   = calculateHashRange(this.hashtable.length);
		this.hashDensity = hashDensity;
		this.capacity    = this.calculateCapacity(this.hashtable.length);
	}



	///////////////////////////////////////////////////////////////////////////
	// declared methods //
	/////////////////////

	private int calculateCapacity(final int slotLength)
	{
		return slotLength >= Integer.MAX_VALUE ? Integer.MAX_VALUE : (int)(slotLength * this.hashDensity);
	}

	private final void rebuildStorage(final int newSlotLength)
	{
		final int newHashRange = calculateHashRange(newSlotLength);
		final E[] newHashTable = newArray(newSlotLength);

		rebuild:
		for(final E element : this.hashtable)
		{
			// plus one to allow predecrementation (faster)
			int i = (System.identityHashCode(element) & newHashRange) + 1;
			while(--i >= 0)
			{
				if(newHashTable[i] == null)
				{
					newHashTable[i] = element;
					continue rebuild;
				}
			}

			// rather rehash if needed instead of storing the hashCode for just one additional use
			i = (System.identityHashCode(element) & newHashRange) - 1;
			while(++i < newSlotLength)
			{
				if(newHashTable[i] == null)
				{
					newHashTable[i] = element;
					continue rebuild;
				}
			}

			// can only happen due to a bug in this implementation
			throw new Error("Rebuilding Error");
		}

		this.capacity  = this.calculateCapacity(newSlotLength);
		this.hashtable = newHashTable;
		this.hashRange = newHashRange;
	}

	private void increaseStorage()
	{
		this.rebuildStorage((int)(this.hashtable.length * 2.0f));
	}

	/**
	 * This method can only be called if:
	 * - the element is guaranteed to be not yet contained
	 * - the hashtable has a free slot
	 *
	 * @param element the element to be added
	 */
	private void guaranteedAddNew(final E element)
	{
		final E[] hashtable = this.hashtable;

		// plus one to allow predecrementation (faster)
		int i = (System.identityHashCode(element) & this.hashRange) + 1;
		while(--i >= 0)
		{
			if(hashtable[i] == null)
			{
				this.addnew(hashtable, i, element);
				return;
			}
		}

		// rather rehash if needed instead of storing the hashCode for just one additional use
		i = (System.identityHashCode(element) & this.hashRange) - 1;
		final int length = hashtable.length;
		while(++i < length)
		{
			if(hashtable[i] == null)
			{
				this.addnew(hashtable, i, element);
				return;
			}
		}
	}

	private void enlargeForNewElement(final E element)
	{
		this.increaseStorage();
		this.guaranteedAddNew(element);
	}

	private void addnew(final E[] hashtable, final int i, final E element)
	{
		hashtable[i] = element;
		if(++this.size >= this.capacity)
		{
			if(this.size >= Integer.MAX_VALUE)
			{
				throw new CapacityExceededException();
			}
			this.increaseStorage();
		}
	}



	///////////////////////////////////////////////////////////////////////////
	// override methods //
	/////////////////////

	@Override
	public final boolean add(final E element)
	{
		final E[] hashtable = this.hashtable;

		// plus one to allow predecrementation (faster)
		int i = (System.identityHashCode(element) & this.hashRange) + 1;
		while(--i >= 0)
		{
			if(hashtable[i] == element)
			{
				return false;
			}
			if(hashtable[i] == null)
			{
				this.addnew(hashtable, i, element);
				return true;
			}
		}

		// rather rehash if needed instead of storing the hashCode for just one additional use
		i = (System.identityHashCode(element) & this.hashRange) - 1;
		final int length = hashtable.length;
		while(++i < length)
		{
			if(hashtable[i] == element)
			{
				return false;
			}
			if(hashtable[i] == null)
			{
				this.addnew(hashtable, i, element);
				return true;
			}
		}

		/*
		 * if the passed element is not already contained and there is no empty bucket at all,
		 * an enlarging rebuild (including checks) has to be done to ensure enough capacity.
		 */
		this.enlargeForNewElement(element);
		return true;
	}

	@Override
	public long size()
	{
		return this.size;
	}

	// (13.04.2016)NOTE: from here on, every method is an unimplemented generated method stub

	@Override
	public E get()
	{
		throw new one.microstream.meta.NotImplementedYetError();
	}

	@Override
	public Iterator<E> iterator()
	{
		throw new one.microstream.meta.NotImplementedYetError();
	}

	@Override
	public Object[] toArray()
	{
		throw new one.microstream.meta.NotImplementedYetError();
	}

	@Override
	public E[] toArray(final Class<E> type)
	{
		throw new one.microstream.meta.NotImplementedYetError();
	}

	@Override
	public OldCollection<E> old()
	{
		throw new one.microstream.meta.NotImplementedYetError();
	}

	@Override
	public boolean hasVolatileElements()
	{
		throw new one.microstream.meta.NotImplementedYetError();
	}

	@Override
	public Equalator<? super E> equality()
	{
		throw new one.microstream.meta.NotImplementedYetError();
	}

	@Override
	public boolean equals(final XGettingCollection<? extends E> samples, final Equalator<? super E> equalator)
	{
		throw new one.microstream.meta.NotImplementedYetError();
	}

	@Override
	public boolean equalsContent(final XGettingCollection<? extends E> samples, final Equalator<? super E> equalator)
	{
		throw new one.microstream.meta.NotImplementedYetError();
	}

	@Override
	public XGettingCollection<E> view()
	{
		throw new one.microstream.meta.NotImplementedYetError();
	}

	@Override
	public boolean nullContained()
	{
		throw new one.microstream.meta.NotImplementedYetError();
	}

	@Override
	public boolean containsId(final E element)
	{
		throw new one.microstream.meta.NotImplementedYetError();
	}

	@Override
	public boolean contains(final E element)
	{
		throw new one.microstream.meta.NotImplementedYetError();
	}

	@Override
	public boolean containsSearched(final Predicate<? super E> predicate)
	{
		throw new one.microstream.meta.NotImplementedYetError();
	}

	@Override
	public boolean containsAll(final XGettingCollection<? extends E> elements)
	{
		throw new one.microstream.meta.NotImplementedYetError();
	}

	@Override
	public boolean applies(final Predicate<? super E> predicate)
	{
		throw new one.microstream.meta.NotImplementedYetError();
	}

	@Override
	public long count(final E element)
	{
		throw new one.microstream.meta.NotImplementedYetError();
	}

	@Override
	public long countBy(final Predicate<? super E> predicate)
	{
		throw new one.microstream.meta.NotImplementedYetError();
	}

	@Override
	public E search(final Predicate<? super E> predicate)
	{
		throw new one.microstream.meta.NotImplementedYetError();
	}

	@Override
	public E seek(final E sample)
	{
		throw new one.microstream.meta.NotImplementedYetError();
	}

	@Override
	public E max(final Comparator<? super E> comparator)
	{
		throw new one.microstream.meta.NotImplementedYetError();
	}

	@Override
	public E min(final Comparator<? super E> comparator)
	{
		throw new one.microstream.meta.NotImplementedYetError();
	}

	@Override
	public <T extends Consumer<? super E>> T distinct(final T target)
	{
		throw new one.microstream.meta.NotImplementedYetError();
	}

	@Override
	public <T extends Consumer<? super E>> T distinct(final T target, final Equalator<? super E> equalator)
	{
		throw new one.microstream.meta.NotImplementedYetError();
	}

	@Override
	public <T extends Consumer<? super E>> T copyTo(final T target)
	{
		throw new one.microstream.meta.NotImplementedYetError();
	}

	@Override
	public <T extends Consumer<? super E>> T filterTo(final T target, final Predicate<? super E> predicate)
	{
		throw new one.microstream.meta.NotImplementedYetError();
	}


	@Override
	public <T extends Consumer<? super E>> T union(
		final XGettingCollection<? extends E> other    ,
		final Equalator<? super E>            equalator,
		final T                               target
	)
	{
		throw new one.microstream.meta.NotImplementedYetError();
	}

	@Override
	public <T extends Consumer<? super E>> T intersect(
		final XGettingCollection<? extends E> other    ,
		final Equalator<? super E>            equalator,
		final T                               target
	)
	{
		throw new one.microstream.meta.NotImplementedYetError();
	}

	@Override
	public <T extends Consumer<? super E>> T except(
		final XGettingCollection<? extends E> other    ,
		final Equalator<? super E>            equalator,
		final T                               target
	)
	{
		throw new one.microstream.meta.NotImplementedYetError();
	}

	@Override
	public <P extends Consumer<? super E>> P iterate(final P procedure)
	{
		throw new one.microstream.meta.NotImplementedYetError();
	}

	@Override
	public boolean nullAllowed()
	{
		throw new one.microstream.meta.NotImplementedYetError();
	}

	@Override
	public long maximumCapacity()
	{
		throw new one.microstream.meta.NotImplementedYetError();
	}

	@Override
	public boolean nullAdd()
	{
		throw new one.microstream.meta.NotImplementedYetError();
	}

	@Override
	public CapacityExtendable ensureCapacity(final long minimalCapacity)
	{
		throw new one.microstream.meta.NotImplementedYetError();
	}

	@Override
	public CapacityExtendable ensureFreeCapacity(final long minimalFreeCapacity)
	{
		throw new one.microstream.meta.NotImplementedYetError();
	}

	@Override
	public long currentCapacity()
	{
		throw new one.microstream.meta.NotImplementedYetError();
	}

	@Override
	public long optimize()
	{
		throw new one.microstream.meta.NotImplementedYetError();
	}

	@Override
	public void accept(final E t)
	{
		throw new one.microstream.meta.NotImplementedYetError();
	}

	@Override
	public boolean put(final E element)
	{
		throw new one.microstream.meta.NotImplementedYetError();
	}

	@Override
	public boolean nullPut()
	{
		throw new one.microstream.meta.NotImplementedYetError();
	}

	@Override
	public E fetch()
	{
		throw new one.microstream.meta.NotImplementedYetError();
	}

	@Override
	public E pinch()
	{
		throw new one.microstream.meta.NotImplementedYetError();
	}

	@Override
	public E retrieve(final E element)
	{
		throw new one.microstream.meta.NotImplementedYetError();
	}

	@Override
	public E retrieveBy(final Predicate<? super E> predicate)
	{
		throw new one.microstream.meta.NotImplementedYetError();
	}

	@Override
	public long removeDuplicates(final Equalator<? super E> equalator)
	{
		throw new one.microstream.meta.NotImplementedYetError();
	}

	@Override
	public long removeBy(final Predicate<? super E> predicate)
	{
		throw new one.microstream.meta.NotImplementedYetError();
	}

	@Override
	public <C extends Consumer<? super E>> C moveTo(final C target, final Predicate<? super E> predicate)
	{
		throw new one.microstream.meta.NotImplementedYetError();
	}

	@Override
	public <P extends Consumer<? super E>> P process(final P processor)
	{
		throw new one.microstream.meta.NotImplementedYetError();
	}

	@Override
	public void clear()
	{
		throw new one.microstream.meta.NotImplementedYetError();
	}

	@Override
	public void truncate()
	{
		throw new one.microstream.meta.NotImplementedYetError();
	}

	@Override
	public long consolidate()
	{
		throw new one.microstream.meta.NotImplementedYetError();
	}

	@Override
	public long nullRemove()
	{
		throw new one.microstream.meta.NotImplementedYetError();
	}

	@Override
	public boolean removeOne(final E element)
	{
		throw new one.microstream.meta.NotImplementedYetError();
	}

	@Override
	public long remove(final E element)
	{
		throw new one.microstream.meta.NotImplementedYetError();
	}

	@Override
	public long removeAll(final XGettingCollection<? extends E> elements)
	{
		throw new one.microstream.meta.NotImplementedYetError();
	}

	@Override
	public long retainAll(final XGettingCollection<? extends E> elements)
	{
		throw new one.microstream.meta.NotImplementedYetError();
	}

	@Override
	public long removeDuplicates()
	{
		throw new one.microstream.meta.NotImplementedYetError();
	}

	@Override
	public E addGet(final E element)
	{
		throw new one.microstream.meta.NotImplementedYetError();
	}

	@Override
	public E deduplicate(final E element)
	{
		throw new one.microstream.meta.NotImplementedYetError();
	}

	@Override
	public E putGet(final E element)
	{
		throw new one.microstream.meta.NotImplementedYetError();
	}

	@Override
	public E replace(final E element)
	{
		throw new one.microstream.meta.NotImplementedYetError();
	}

	@Override
	public XImmutableSet<E> immure()
	{
		throw new one.microstream.meta.NotImplementedYetError();
	}

	@SafeVarargs
	@Override
	public final XSet<E> putAll(final E... elements)
	{
		throw new one.microstream.meta.NotImplementedYetError();
	}

	@Override
	public XSet<E> putAll(final E[] elements, final int srcStartIndex, final int srcLength)
	{
		throw new one.microstream.meta.NotImplementedYetError();
	}

	@Override
	public XSet<E> putAll(final XGettingCollection<? extends E> elements)
	{
		throw new one.microstream.meta.NotImplementedYetError();
	}

	@SafeVarargs
	@Override
	public final XSet<E> addAll(final E... elements)
	{
		throw new one.microstream.meta.NotImplementedYetError();
	}

	@Override
	public XSet<E> addAll(final E[] elements, final int srcStartIndex, final int srcLength)
	{
		throw new one.microstream.meta.NotImplementedYetError();
	}

	@Override
	public XSet<E> addAll(final XGettingCollection<? extends E> elements)
	{
		throw new one.microstream.meta.NotImplementedYetError();
	}

	@Override
	public XSet<E> copy()
	{
		throw new one.microstream.meta.NotImplementedYetError();
	}

	@Override
	public String toString()
	{
		return super.toString();
	}

}

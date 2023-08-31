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


import java.util.Comparator;
import java.util.Iterator;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;

import one.microstream.chars.VarString;
import one.microstream.collections.interfaces.ChainStorage;
import one.microstream.collections.interfaces.HashCollection;
import one.microstream.collections.old.OldSet;
import one.microstream.collections.types.XGettingCollection;
import one.microstream.collections.types.XGettingEnum;
import one.microstream.collections.types.XImmutableEnum;
import one.microstream.equality.Equalator;
import one.microstream.equality.IdentityEqualityLogic;
import one.microstream.functional.IndexedAcceptor;
import one.microstream.hashing.HashEqualator;
import one.microstream.hashing.XHashing;
import one.microstream.typing.Composition;
import one.microstream.typing.XTypes;


public final class ConstHashEnum<E>
extends AbstractChainCollection<E, E, E, ChainEntryLinkedStrong<E>>
implements XImmutableEnum<E>, HashCollection<E>, Composition, IdentityEqualityLogic
{
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////

	public static final <E> ConstHashEnum<E> New()
	{
		return new ConstHashEnum<>(
			DEFAULT_HASH_LENGTH,
			DEFAULT_HASH_FACTOR
		);
	}

	public static final <E> ConstHashEnum<E> NewCustom(final int initialCapacity)
	{
		return new ConstHashEnum<>(
			XHashing.padHashLength(initialCapacity),
			DEFAULT_HASH_FACTOR
		);
	}

	public static final <E> ConstHashEnum<E> NewCustom(final int initialCapacity, final float hashDensity)
	{
		return new ConstHashEnum<>(
			XHashing.padHashLength(initialCapacity),
			XHashing.validateHashDensity(hashDensity)
		);
	}

	public static final <E> ConstHashEnum<E> NewCustom(
		final float                           hashDensity,
		final XGettingCollection<? extends E> entries
	)
	{
		final ConstHashEnum<E> newEnum = new ConstHashEnum<>(
			XHashing.padHashLength(entries.size()), // might be too big if entries contains a lot of duplicates
			XHashing.validateHashDensity(hashDensity)
		);
		newEnum.internalAddAll(entries);
		return newEnum;
	}

	@SafeVarargs
	public static final <E> ConstHashEnum<E> NewCustom(final float hashDensity, final E... entries)
	{
		final ConstHashEnum<E> newEnum = new ConstHashEnum<>(
			XHashing.calculateHashLength(entries.length, hashDensity),
			XHashing.validateHashDensity(hashDensity)
		);
		for(final E e : entries)
		{
			newEnum.internalAdd(e);
		}
		return newEnum;
	}

	public static final <E> ConstHashEnum<E> New(final XGettingCollection<? extends E> entries)
	{
		return NewCustom(DEFAULT_HASH_FACTOR, entries);
	}

	@SafeVarargs
	public static final <E> ConstHashEnum<E> New(final E... entries)
	{
		return NewCustom(DEFAULT_HASH_FACTOR, entries);
	}



	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	// data storage
	private final AbstractChainStorage<E, E, E, ChainEntryLinkedStrong<E>> chain;
	              ChainEntryLinkedStrong<E>[]                              slots;


	// hashing
	float hashDensity;

	// cached values
	transient int capacity, range;

	int size;



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	private ConstHashEnum(final ConstHashEnum<E> original)
	{
		super();
		this.hashDensity   = original.hashDensity;
		this.range         = original.range;

		// constructor only copies configuration (concern #1), not data (#2). See copy() for copying data.
		this.slots         = ChainEntryLinkedStrong.array(original.slots.length);
		this.chain         = new ChainStorageStrong<>(this, new ChainEntryLinkedStrong<E>(null, null));
		this.capacity      = original.capacity;
	}

	private ConstHashEnum(final int pow2InitialHashLength, final float positiveHashDensity)
	{
		super();
		this.hashDensity   = positiveHashDensity;
		this.range         = pow2InitialHashLength - 1;

		this.slots         = ChainEntryLinkedStrong.array(pow2InitialHashLength);
		this.chain         = new ChainStorageStrong<>(this, new ChainEntryLinkedStrong<E>(null, null));
		this.capacity      = (int)(pow2InitialHashLength * positiveHashDensity); // capped at MAX_VALUE
	}



	///////////////////////////////////////////////////////////////////////////
	// declared methods //
	/////////////////////

	final boolean internalAdd(final E element)
	{
		for(ChainEntryLinkedStrong<E> e = this.slots[System.identityHashCode(element) & this.range]; e != null; e = e.link)
		{
			if(e.element == element)
			{
				return false; // already contained
			}
		}
		this.chain.appendEntry(this.createNewEntry(element));
		return true;
	}

	final void internalAddAll(final XGettingCollection<? extends E> elements)
	{
		elements.iterate(e ->
		{
			this.internalAdd(e);
		});
	}

	private ChainEntryLinkedStrong<E> createNewEntry(final E element)
	{
		if(this.size >= this.capacity)
		{
			ensureFreeArrayCapacity(this.size); // size limit only needs to be checked if size reached capacity
			this.increaseStorage();
		}

		ChainEntryLinkedStrong<E> e;
		this.slots[System.identityHashCode(element) & this.range] = e =
			new ChainEntryLinkedStrong<>(element, this.slots[System.identityHashCode(element) & this.range])
		;
		this.size++;
		return e;
	}

	private void increaseStorage()
	{
		this.rebuildStorage((int)(this.slots.length * 2.0f));
	}

	private void rebuildStorage(final int newSlotLength)
	{
		final ChainEntryLinkedStrong<E>[] newSlots = ChainEntryLinkedStrong.array(newSlotLength);
		final int modulo = newSlotLength >= Integer.MAX_VALUE ? Integer.MAX_VALUE : newSlotLength - 1;

		// iterate through all entries and assign them to the new storage
		for(ChainEntryLinkedStrong<E> entry = this.chain.head(); (entry = entry.next) != null;)
		{
			entry.link = newSlots[System.identityHashCode(entry.element) & modulo];
			newSlots[System.identityHashCode(entry.element) & modulo] = entry;
		}

		this.capacity = newSlotLength >= Integer.MAX_VALUE ? Integer.MAX_VALUE : (int)(newSlotLength * this.hashDensity);
		this.slots = newSlots;
		this.range = modulo;
	}



	///////////////////////////////////////////////////////////////////////////
	// inheriteted ExtendedCollection methods //
	///////////////////////////////////////////

	@Override
	public boolean nullAllowed()
	{
		throw new UnsupportedOperationException();
	}

	@Override
	protected ChainStorage<E, E, E, ChainEntryLinkedStrong<E>> getInternalStorageChain()
	{
		return this.chain;
	}

	@Override
	protected int internalRemoveNullEntries()
	{
		throw new UnsupportedOperationException();
	}

	@Override
	protected int internalCountingAddAll(final E[] elements) throws UnsupportedOperationException
	{
		throw new UnsupportedOperationException();
	}

	@Override
	protected int internalCountingAddAll(final E[] elements, final int offset, final int length)
		throws UnsupportedOperationException
	{
		throw new UnsupportedOperationException();
	}

	@Override
	protected int internalCountingAddAll(final XGettingCollection<? extends E> elements)
		throws UnsupportedOperationException
	{
		throw new UnsupportedOperationException();
	}

	@Override
	protected int internalCountingPutAll(final E[] elements) throws UnsupportedOperationException
	{
		throw new UnsupportedOperationException();
	}

	@Override
	protected int internalCountingPutAll(final E[] elements, final int offset, final int length)
		throws UnsupportedOperationException
	{
		throw new UnsupportedOperationException();
	}

	@Override
	protected int internalCountingPutAll(final XGettingCollection<? extends E> elements)
		throws UnsupportedOperationException
	{
		throw new UnsupportedOperationException();
	}

	@Override
	protected void internalRemoveEntry(final ChainEntryLinkedStrong<E> entry)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	protected int internalClear()
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public final long size()
	{
		return this.size;
	}

	@Override
	public final int rehash()
	{
		/* As the object header's identity hash value of any instance can never change and an immutable
		 * collection can never have the need to optimize its storage, this method is de facto no-op.
		 * Should this VM implementation detail ever change (which is extremely doubtful as it moreless ruins
		 * the object identity), feel free to replace this method on the source or bytecode level.
		 */
		return XTypes.to_int(this.size());
	}

	@Override
	public final boolean isEmpty()
	{
		return this.size == 0;
	}

	@Override
	public final ConstHashEnum<E> copy()
	{
		final ConstHashEnum<E> newVarMap = new ConstHashEnum<>(this);
		this.chain.iterate(new Consumer<E>()
		{
			@Override
			public void accept(final E entry)
			{
				newVarMap.internalAdd(entry);
			}
		});
		return newVarMap;
	}

	@Override
	public final ConstHashEnum<E> immure()
	{
		return this;
	}

	@Override
	public final XGettingEnum<E> view()
	{
		return new EnumView<>(this);
	}

	@Override
	public final void setHashDensity(final float hashDensity)
	{
		// no-op
	}

	@Override
	public final boolean hasVolatileElements()
	{
		return false;
	}

	@Override
	public final OldSet<E> old()
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME EqHashTable#old()
	}

	@Override
	public final long maximumCapacity()
	{
		return Integer.MAX_VALUE;
	}

	@Override
	public final boolean isFull()
	{
		return this.size >= Integer.MAX_VALUE;
	}

	@Override
	public final long remainingCapacity()
	{
		return Integer.MAX_VALUE - this.size;
	}

	@Override
	public final HashCollection.Analysis<ConstHashEnum<E>> analyze()
	{
		return AbstractChainEntryLinked.analyzeSlots(this, this.slots);
	}

	@Override
	public final int hashDistributionRange()
	{
		return this.slots.length;
	}

	@Override
	public final HashEqualator<E> hashEquality()
	{
		return XHashing.hashEqualityIdentity();
	}

	@Override
	public final float hashDensity()
	{
		return this.hashDensity;
	}

	@Override
	public final boolean hasVolatileHashElements()
	{
		return this.chain.hasVolatileElements();
	}

	@Override
	public final String toString()
	{
		return this.chain.appendTo(VarString.New(this.slots.length).append('['), ",").append(']').toString();
	}



	///////////////////////////////////////////////////////////////////////////
	// getting methods //
	////////////////////

	@Override
	public final XImmutableEnum<E> range(final long lowIndex, final long highIndex)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME EqHashTable.Entries#range()
	}

	@Override
	public final XGettingEnum<E> view(final long lowIndex, final long highIndex)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME EqHashTable.Entries#view()
	}

	@Override
	public final E[] toArray(final Class<E> type)
	{
		return this.chain.toArray(type);
	}

	// executing //

	@Override
	public final <P extends Consumer<? super E>> P iterate(final P procedure)
	{
		this.chain.iterate(procedure);
		return procedure;
	}

	@Override
	public final <A> A join(final BiConsumer<? super E, ? super A> joiner, final A aggregate)
	{
		this.chain.join(joiner, aggregate);
		return aggregate;
	}

	@Override
	public final long count(final E entry)
	{
		return this.chain.count(entry);
	}

	@Override
	public final long countBy(final Predicate<? super E> predicate)
	{
		return this.chain.count(predicate);
	}

	// element querying //

	@Override
	public final E search(final Predicate<? super E> predicate)
	{
		return this.chain.search(predicate);
	}

	@Override
	public final E max(final Comparator<? super E> comparator)
	{
		return this.chain.max(comparator);
	}

	@Override
	public final E min(final Comparator<? super E> comparator)
	{
		return this.chain.min(comparator);
	}

//	/**
//	 * As per definition of a set, this method always returns true.<br>
//	 * Note that mutated elements whose hashcode has not been immuted by the employed hash logic
//	 * can be contained multiple times, effectively breaking this method (because of breaking the hashing logic in the
//	 * first place), so this information only has value if the elements' implementation is immutable or if the
//	 * hash logic compensated their mutability (e.g. by using the identity hash code or by registering a once created
//	 * hashcode, effectively "immuting" it).
//	 *
//	 * @return
//	 * @see XGettingCollection#hasDistinctValues()
//	 */
//	@Override
//	public final boolean hasDistinctValues()
//	{
//		return true;
//	}
//
//	@Override
//	public final boolean hasDistinctValues(final Equalator<? super E> equalator)
//	{
//		return this.chain.hasDistinctValues(equalator);
//	}

	// boolean querying - applies //

	@Override
	public final boolean containsSearched(final Predicate<? super E> predicate)
	{
		return this.chain.containsSearched(predicate);
	}

	@Override
	public final boolean applies(final Predicate<? super E> predicate)
	{
		return this.chain.appliesAll(predicate);
	}

	// boolean querying - contains //

	@Override
	public final boolean nullContained()
	{
		return false;
	}

	@Override
	public final boolean containsId(final E element)
	{
		// search for element by hash
		for(ChainEntryLinkedStrong<E> e = this.slots[System.identityHashCode(element) & this.range]; e != null; e = e.link)
		{
			if(element == e.element)
			{
				return true;
			}
		}
		return false;
	}

	@Override
	public final boolean contains(final E element)
	{
		// search for element by hash
		for(ChainEntryLinkedStrong<E> e = this.slots[System.identityHashCode(element) & this.range]; e != null; e = e.link)
		{
			if(e.element == element)
			{
				return true;
			}
		}
		return false;
	}

	@Override
	public final E seek(final E sample)
	{
		if(sample == null)
		{
			// null special case
			return null;
		}

		// search for element by hash
		for(ChainEntryLinkedStrong<E> e = this.slots[System.identityHashCode(sample) & this.range]; e != null; e = e.link)
		{
			if(e.element == sample)
			{
				return e.element;
			}
		}
		return null;
	}

	// boolean querying - equality //

	@Override
	public final boolean equals(final XGettingCollection<? extends E> samples, final Equalator<? super E> equalator)
	{
		if(samples == null || !(samples instanceof ConstHashEnum<?>))
		{
			return false;
		}
		if(samples == this)
		{
			return true;
		}
		return this.equalsContent(samples, equalator);
	}

	@Override
	public final boolean equalsContent(final XGettingCollection<? extends E> samples, final Equalator<? super E> equalator)
	{
		if(this.size != XTypes.to_int(samples.size()))
		{
			return false;
		}

		// if sizes are equal and all elements of collection are contained in this set, they must have equal content
		return this.chain.equalsContent(samples, equalator);
	}

	// data set procedures //

	@Override
	public final <C extends Consumer<? super E>> C intersect(
		final XGettingCollection<? extends E> other,
		final Equalator<? super E> equalator,
		final C target
	)
	{
		return this.chain.intersect(other, equalator, target);
	}

	@Override
	public final <C extends Consumer<? super E>> C except(
		final XGettingCollection<? extends E> other,
		final Equalator<? super E> equalator,
		final C target
	)
	{
		return this.chain.except(other, equalator, target);
	}

	@Override
	public final <C extends Consumer<? super E>> C union(
		final XGettingCollection<? extends E> other,
		final Equalator<? super E> equalator,
		final C target
	)
	{
		return this.chain.union(other, equalator, target);
	}

	@Override
	public final <C extends Consumer<? super E>> C copyTo(final C target)
	{
		return this.chain.copyTo(target);
	}

	@Override
	public final <C extends Consumer<? super E>> C filterTo(final C target, final Predicate<? super E> predicate)
	{
		return this.chain.copyTo(target, predicate);
	}

	@Override
	public final <C extends Consumer<? super E>> C distinct(final C target)
	{
		return this.chain.distinct(target);
	}

	@Override
	public final <C extends Consumer<? super E>> C distinct(final C target, final Equalator<? super E> equalator)
	{
		return this.chain.distinct(target, equalator);
	}

	@Override
	public final ConstHashEnum<E> toReversed()
	{
		final ConstHashEnum<E> reversedVarSet = this.copy();
		reversedVarSet.chain.reverse();
		return reversedVarSet;
	}

	@Override
	public final <T extends Consumer<? super E>> T copySelection(final T target, final long... indices)
	{
		this.chain.copySelection(target, indices);
		return target;
	}

	@Override
	public final <P extends IndexedAcceptor<? super E>> P iterateIndexed(final P procedure)
	{
		this.chain.iterateIndexed(procedure);
		return procedure;
	}

	@Override
	public final E at(final long index)
	{
		return this.chain.get(index);
	}

	@Override
	public final E get()
	{
		return this.chain.first();
	}

	@Override
	public final E first()
	{
		return this.chain.first();
	}

	@Override
	public final E last()
	{
		return this.chain.last();
	}

	@Override
	public final E poll()
	{
		return this.size == 0 ? null : this.chain.first();
	}

	@Override
	public final E peek()
	{
		return this.size == 0 ? null : this.chain.last();
	}

	@Override
	public final long indexOf(final E entry)
	{
		return this.chain.indexOf(entry);
	}

	@Override
	public final long indexBy(final Predicate<? super E> predicate)
	{
		return this.chain.indexOf(predicate);
	}

	@Override
	public final boolean isSorted(final Comparator<? super E> comparator)
	{
		return this.chain.isSorted(comparator);
	}

	@Override
	public final long lastIndexOf(final E entry)
	{
		return this.chain.lastIndexOf(entry);
	}

	@Override
	public final long lastIndexBy(final Predicate<? super E> predicate)
	{
		return this.chain.lastIndexBy(predicate);
	}

	@Override
	public final long maxIndex(final Comparator<? super E> comparator)
	{
		return this.chain.maxIndex(comparator);
	}

	@Override
	public final long minIndex(final Comparator<? super E> comparator)
	{
		return this.chain.minIndex(comparator);
	}

	@Override
	public final long scan(final Predicate<? super E> predicate)
	{
		return this.chain.scan(predicate);
	}

	@Override
	public final Iterator<E> iterator()
	{
		return this.chain.iterator();
	}

	@Override
	public final Object[] toArray()
	{
		return this.chain.toArray();
	}

	@Override
	public final HashEqualator<E> equality()
	{
		return XHashing.hashEqualityIdentity();
	}

}

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


import static one.microstream.X.notNull;

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
import one.microstream.functional.IndexedAcceptor;
import one.microstream.hashing.HashEqualator;
import one.microstream.hashing.XHashing;
import one.microstream.typing.Composition;
import one.microstream.typing.XTypes;


public final class EqConstHashEnum<E>
extends AbstractChainCollection<E, E, E, ChainEntryLinkedHashedStrong<E>>
implements XImmutableEnum<E>, HashCollection<E>, Composition
{
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////

	public static final <E> EqConstHashEnum<E> New()
	{
		return new EqConstHashEnum<>(
			DEFAULT_HASH_LENGTH,
			DEFAULT_HASH_FACTOR,
			XHashing.<E>hashEqualityValue()
		);
	}

	public static final <E> EqConstHashEnum<E> New(final int initialCapacity)
	{
		return new EqConstHashEnum<>(
			XHashing.padHashLength(initialCapacity),
			DEFAULT_HASH_FACTOR,
			XHashing.<E>hashEqualityValue()
		);
	}

	public static final <E> EqConstHashEnum<E> New(final int initialCapacity, final float hashDensity)
	{
		return new EqConstHashEnum<>(
			XHashing.padHashLength(initialCapacity),
			XHashing.validateHashDensity(hashDensity),
			XHashing.<E>hashEqualityValue()
		);
	}

	public static final <E> EqConstHashEnum<E> New(final XGettingCollection<? extends E> entries)
	{
		final EqConstHashEnum<E> newEnum = New();
		newEnum.internalAddAll(entries);
		return newEnum;
	}

	public static final <E> EqConstHashEnum<E> New(final HashEqualator<? super E> hashEqualator)
	{
		return new EqConstHashEnum<>(DEFAULT_HASH_LENGTH, DEFAULT_HASH_FACTOR, hashEqualator);
	}

	public static final <E> EqConstHashEnum<E> New(
		final HashEqualator<? super E> hashEqualator  ,
		final int                      initialCapacity
	)
	{
		return new EqConstHashEnum<>(
			XHashing.padHashLength(initialCapacity),
			DEFAULT_HASH_FACTOR,
			hashEqualator
		);
	}

	public static final <E> EqConstHashEnum<E> New(
		final HashEqualator<? super E>      hashEqualator  ,
		final float                 hashDensity
	)
	{
		return new EqConstHashEnum<>(DEFAULT_HASH_LENGTH, hashDensity, hashEqualator);
	}

	public static final <E> EqConstHashEnum<E> New(
		final HashEqualator<? super E> hashEqualator  ,
		final int                      initialCapacity,
		final float                    hashDensity
	)
	{
		return new EqConstHashEnum<>(
			XHashing.padHashLength(initialCapacity),
			XHashing.validateHashDensity(hashDensity),
			hashEqualator
		);
	}

	public static final <E> EqConstHashEnum<E> New(
		final HashEqualator<? super E>        hashEqualator,
		final XGettingCollection<? extends E> entries
	)
	{
		return New(hashEqualator, DEFAULT_HASH_FACTOR, entries);
	}

	public static final <E> EqConstHashEnum<E> New(
		final HashEqualator<? super E>        hashEqualator,
		final float                           hashDensity  ,
		final XGettingCollection<? extends E> entries
	)
	{
		final EqConstHashEnum<E> newEnum = new EqConstHashEnum<>(
			XHashing.padHashLength(entries.size()), // might be too big if entries contains a lot of duplicates
			XHashing.validateHashDensity(hashDensity),
			notNull(hashEqualator)
		);
		newEnum.internalAddAll(entries);
		return newEnum;
	}

	@SafeVarargs
	public static final <E> EqConstHashEnum<E> New(
		final E...  entries
	)
	{
		return New(XHashing.<E>hashEqualityValue(), DEFAULT_HASH_FACTOR, entries);
	}

	@SafeVarargs
	public static final <E> EqConstHashEnum<E> New(
		final float                    hashDensity  ,
		final E...                     entries
	)
	{
		return New(XHashing.<E>hashEqualityValue(), hashDensity, entries);
	}

	@SafeVarargs
	public static final <E> EqConstHashEnum<E> New(
		final HashEqualator<? super E> hashEqualator,
		final E...  entries
	)
	{
		return New(hashEqualator, DEFAULT_HASH_FACTOR, entries);
	}

	@SafeVarargs
	public static final <E> EqConstHashEnum<E> New(
		final HashEqualator<? super E> hashEqualator,
		final float                    hashDensity  ,
		final E...                     entries
	)
	{
		final EqConstHashEnum<E> newEnum = new EqConstHashEnum<>(
			XHashing.padHashLength(entries.length), // might be too big if entries contains a lot of duplicates
			XHashing.validateHashDensity(hashDensity),
			notNull(hashEqualator)
		);
		for(final E e : entries)
		{
			newEnum.internalAdd(e);
		}
		return newEnum;
	}



	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	// data storage
	private final AbstractChainStorage<E, E, E, ChainEntryLinkedHashedStrong<E>> chain;
	              ChainEntryLinkedHashedStrong<E>[]                        slots;

	// hashing
	final HashEqualator<? super E> hashEqualator;
	float                  hashDensity  ;

	// cached values
	transient int capacity, range;

	int size;



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	private EqConstHashEnum(final EqConstHashEnum<E> original)
	{
		super();
		this.hashDensity   = original.hashDensity;
		this.hashEqualator = original.hashEqualator;
		this.range         = original.range;

		// constructor only copies configuration (concern #1), not data (#2). See copy() for copying data.
		this.slots         = ChainEntryLinkedHashedStrong.array(original.slots.length);
		this.chain         = new ChainStorageStrong<>(this, new ChainEntryLinkedHashedStrong<E>(-1, null, null));
		this.capacity      = original.capacity;
	}

	private EqConstHashEnum(
		final int              pow2InitialHashLength,
		final float            positiveHashDensity  ,
		final HashEqualator<? super E> hashEqualator
	)
	{
		super();
		this.hashDensity   = positiveHashDensity;
		this.hashEqualator = hashEqualator;
		this.range         = pow2InitialHashLength - 1;

		this.slots         = ChainEntryLinkedHashedStrong.array(pow2InitialHashLength);
		this.chain         = new ChainStorageStrong<>(this, new ChainEntryLinkedHashedStrong<E>(-1, null, null));
		this.capacity      = (int)(pow2InitialHashLength * positiveHashDensity); // capped at MAX_VALUE
	}



	///////////////////////////////////////////////////////////////////////////
	// declared methods //
	/////////////////////

	final boolean internalAdd(final E element)
	{
		final int hash;
		for(ChainEntryLinkedHashedStrong<E> e = this.slots[(hash = this.hashEqualator.hash(element)) & this.range]; e != null; e = e.link)
		{
			if(e.hash == hash && this.hashEqualator.equal(e.element, element))
			{
				return false; // already contained
			}
		}
		this.chain.appendEntry(this.createNewEntry(hash, element));
		return true;
	}

	final void internalAddAll(final XGettingCollection<? extends E> elements)
	{
		elements.iterate(this::internalAdd);
	}

	private ChainEntryLinkedHashedStrong<E> createNewEntry(final int hash, final E element)
	{
		if(this.size >= this.capacity)
		{
			ensureFreeArrayCapacity(this.size); // size limit only needs to be checked if size reached capacity
			this.increaseStorage();
		}

		ChainEntryLinkedHashedStrong<E> e;
		this.slots[hash & this.range] = e = new ChainEntryLinkedHashedStrong<>(hash, element, this.slots[hash & this.range]);
		this.size++;
		return e;
	}

	private void increaseStorage()
	{
		this.rebuildStorage((int)(this.slots.length * 2.0f));
	}

	private void rebuildStorage(final int newSlotLength)
	{
		final ChainEntryLinkedHashedStrong<E>[] newSlots = ChainEntryLinkedHashedStrong.array(newSlotLength);
		final int modulo = newSlotLength >= Integer.MAX_VALUE ? Integer.MAX_VALUE : newSlotLength - 1;

		// iterate through all entries and assign them to the new storage
		for(ChainEntryLinkedHashedStrong<E> entry = this.chain.head(); (entry = entry.next) != null;)
		{
			entry.link = newSlots[entry.hash & modulo];
			newSlots[entry.hash & modulo] = entry;
		}

		this.capacity = newSlotLength >= Integer.MAX_VALUE ? Integer.MAX_VALUE : (int)(newSlotLength * this.hashDensity);
		this.slots = newSlots;
		this.range = modulo;
	}

	final void internalCollectUnhashed(final E element)
	{
		this.chain.appendEntry(new ChainEntryLinkedHashedStrong<>(0, element, null));
	}

	final int internalRehash()
	{
		// local helper variables, including capacity recalculation while at rebuilding anyway
		final int                               reqCapacity   = XHashing.padHashLength((int)(this.size / this.hashDensity));
		final ChainEntryLinkedHashedStrong<E>[] slots         = ChainEntryLinkedHashedStrong.<E>array(reqCapacity);
		final int                               range         = reqCapacity >= Integer.MAX_VALUE ? Integer.MAX_VALUE : reqCapacity - 1;
		final HashEqualator<? super E>          hashEqualator = this.hashEqualator;
		final AbstractChainStorage<E, E, E, ChainEntryLinkedHashedStrong<E>> chain = this.chain;

		// keep the old chain head for old entries iteration and clear the chain for the new entries
		ChainEntryLinkedHashedStrong<E> entry = chain.head().next;
		chain.clear();

		int size = 0;
		oldEntries:
		for(/*entry must be outside, see comment*/; entry != null; entry = entry.next)
		{
			final int hash = hashEqualator.hash(entry.element);

			// check for rehash collisions
			for(ChainEntryLinkedHashedStrong<E> e = slots[hash & range]; e != null; e = e.link)
			{
				if(e.hash == hash && hashEqualator.equal(e.element, entry.element))
				{
					continue oldEntries; // hash collision: value already contained, discard old element
				}
			}

			// register new entry for unique element
			chain.appendEntry(slots[hash & range] =
				new ChainEntryLinkedHashedStrong<>(hash, entry.element, slots[hash & range]))
			;
			size++;
		}

		// update collection state with new members
		this.slots = slots;
		this.range = range;
		this.size  = size ;
		return size;
	}



	///////////////////////////////////////////////////////////////////////////
	// inheriteted ExtendedCollection methods //
	///////////////////////////////////////////

	@Override
	public final int rehash()
	{
		/* this is a little hacky:
		 * Being an ("officially") immutable collection, this implementation may not support this method
		 * despite existing nevertheless as it is required for building (deserialization) purposes.
		 * The "clean" way would be to distinct between HashCollection and MutableHashCollection and putting
		 * this method definition into the Mutable~ variant.
		 */
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean nullAllowed()
	{
		throw new UnsupportedOperationException();
	}

	@Override
	protected ChainStorage<E, E, E, ChainEntryLinkedHashedStrong<E>> getInternalStorageChain()
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
	protected void internalRemoveEntry(final ChainEntryLinkedHashedStrong<E> entry)
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
	public final boolean isEmpty()
	{
		return this.size == 0;
	}

	@Override
	public final EqConstHashEnum<E> copy()
	{
		final EqConstHashEnum<E> newVarMap = new EqConstHashEnum<>(this);
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
	public final EqConstHashEnum<E> immure()
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
	public final HashCollection.Analysis<EqConstHashEnum<E>> analyze()
	{
		return AbstractChainEntryLinked.analyzeSlots(this, this.slots);
	}

	@Override
	public final int hashDistributionRange()
	{
		return this.slots.length;
	}

	@Override
	public final HashEqualator<? super E> hashEquality()
	{
		return this.hashEqualator;
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
		return this.chain.count(entry, this.hashEqualator);
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
		final int hash; // search for element by hash
		for(ChainEntryLinkedHashedStrong<E> e = this.slots[(hash = this.hashEqualator.hash(element)) & this.range]; e != null; e = e.link)
		{
			if(hash == e.hash && element == e.element)
			{
				return true;
			}
		}
		return false;
	}

	@Override
	public final boolean contains(final E element)
	{
		final int hash; // search for element by hash
		for(ChainEntryLinkedHashedStrong<E> e = this.slots[(hash = this.hashEqualator.hash(element)) & this.range]; e != null; e = e.link)
		{
			if(e.hash == hash && this.hashEqualator.equal(e.element, element))
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

		final int hash; // search for element by hash
		for(ChainEntryLinkedHashedStrong<E> e = this.slots[(hash = this.hashEqualator.hash(sample)) & this.range]; e != null; e = e.link)
		{
			if(e.hash == hash && this.hashEqualator.equal(e.element, sample))
			{
				return e.element;
			}
		}
		return null;
	}

	@Override
	public final boolean containsAll(final XGettingCollection<? extends E> elements)
	{
		return elements.applies(this::contains);
	}

	// boolean querying - equality //

	@Override
	public final boolean equals(final XGettingCollection<? extends E> samples, final Equalator<? super E> equalator)
	{
		if(samples == null || !(samples instanceof EqConstHashEnum<?>))
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
		return this.distinct(target, this.hashEqualator);
	}

	@Override
	public final <C extends Consumer<? super E>> C distinct(final C target, final Equalator<? super E> equalator)
	{
		return this.chain.distinct(target, equalator);
	}

	@Override
	public final EqConstHashEnum<E> toReversed()
	{
		final EqConstHashEnum<E> reversedVarSet = this.copy();
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
	public final HashEqualator<? super E> equality()
	{
		return this.hashEqualator;
	}

}

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
import java.util.function.Function;
import java.util.function.Predicate;

import one.microstream.chars.VarString;
import one.microstream.collections.interfaces.CapacityExtendable;
import one.microstream.collections.interfaces.ChainStorage;
import one.microstream.collections.interfaces.HashCollection;
import one.microstream.collections.old.AbstractBridgeXSet;
import one.microstream.collections.types.XEnum;
import one.microstream.collections.types.XGettingCollection;
import one.microstream.collections.types.XGettingEnum;
import one.microstream.collections.types.XGettingSequence;
import one.microstream.equality.Equalator;
import one.microstream.exceptions.ArrayCapacityException;
import one.microstream.functional.IndexedAcceptor;
import one.microstream.hashing.HashEqualator;
import one.microstream.hashing.XHashing;
import one.microstream.math.XMath;
import one.microstream.typing.Composition;
import one.microstream.typing.XTypes;

/**
 * Collection that is ordered and does not allow duplicates. Aims to be more efficient, logically structured
 * and with more built-in features than {@link java.util.Set}.
 * <p>
 * Additional to the {@link HashEnum}, this implementation needs an {@link HashEqualator}
 * to first define equality between elements and second define the hash method to use.
 * <p>
 * This implementation is <b>not</b> synchronized and thus should only be used by a
 * single thread or in a thread-safe manner (i.e. read-only as soon as multiple threads access it).<br>
 * See {@link SynchSet} wrapper class to use a list in a synchronized manner.
 * <p>
 * Also note that by being an extended collection, this implementation offers various functional and batch procedures
 * to maximize internal iteration potential, eliminating the need to use the external iteration
 * {@link Iterator} paradigm.
 * 
 * @param <E> type of contained elements
 */
public final class EqHashEnum<E> extends AbstractChainCollection<E, E, E, ChainEntryLinkedHashedStrong<E>>
implements XEnum<E>, HashCollection<E>, Composition
{
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////

	public static final <E> EqHashEnum<E> New()
	{
		return new EqHashEnum<>(
			DEFAULT_HASH_LENGTH,
			DEFAULT_HASH_FACTOR,
			XHashing.<E>hashEqualityValue()
		);
	}

	public static final <E> EqHashEnum<E> NewCustom(final int initialCapacity)
	{
		return new EqHashEnum<>(
			XHashing.padHashLength(initialCapacity),
			DEFAULT_HASH_FACTOR,
			XHashing.<E>hashEqualityValue()
		);
	}

	public static final <E> EqHashEnum<E> NewCustom(final int initialCapacity, final float hashDensity)
	{
		return new EqHashEnum<>(
			XHashing.padHashLength(initialCapacity),
			XHashing.validateHashDensity(hashDensity),
			XHashing.<E>hashEqualityValue()
		);
	}

	public static final <E> EqHashEnum<E> New(final HashEqualator<? super E> hashEqualator)
	{
		return new EqHashEnum<>(DEFAULT_HASH_LENGTH, DEFAULT_HASH_FACTOR, hashEqualator);
	}

	public static final <E> EqHashEnum<E> NewCustom(
		final HashEqualator<? super E> hashEqualator  ,
		final int                      initialCapacity
	)
	{
		return new EqHashEnum<>(
			XHashing.padHashLength(initialCapacity),
			DEFAULT_HASH_FACTOR,
			hashEqualator
		);
	}

	public static final <E> EqHashEnum<E> NewCustom(
		final HashEqualator<? super E> hashEqualator,
		final float                    hashDensity
	)
	{
		return new EqHashEnum<>(DEFAULT_HASH_LENGTH, hashDensity, hashEqualator);
	}

	public static final <E> EqHashEnum<E> NewCustom(
		final HashEqualator<? super E>hashEqualator  ,
		final int                     initialCapacity,
		final float                   hashDensity
	)
	{
		return new EqHashEnum<>(
			XHashing.padHashLength(initialCapacity),
			XHashing.validateHashDensity(hashDensity),
			hashEqualator
		);
	}

	@SafeVarargs
	public static final <E> EqHashEnum<E> New(final E... entries)
	{
		return NewCustom(XHashing.<E>hashEqualityValue(), DEFAULT_HASH_FACTOR, entries);
	}

	public static final <E> EqHashEnum<E> New(final XGettingCollection<? extends E> entries)
	{
		return EqHashEnum.<E>New().addAll(entries);
	}

	@SafeVarargs
	public static final <E> EqHashEnum<E> NewCustom(final float hashDensity, final E... entries)
	{
		return NewCustom(XHashing.<E>hashEqualityValue(), hashDensity, entries);
	}

	@SafeVarargs
	public static final <E> EqHashEnum<E> New(final HashEqualator<? super E> hashEqualator, final E... entries)
	{
		return NewCustom(hashEqualator, DEFAULT_HASH_FACTOR, entries);
	}

	@SafeVarargs
	public static final <E> EqHashEnum<E> NewCustom(
		final HashEqualator<? super E> hashEqualator,
		final float                    hashDensity  ,
		final E...                     entries
	)
	{
		return new EqHashEnum<E>(
			XHashing.padHashLength(entries.length), // might be too big if entries contains a lot of duplicates
			XHashing.validateHashDensity(hashDensity),
			notNull(hashEqualator)
		).addAll(entries);
	}



	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	// data storage
	private final AbstractChainStorage<E, E, E, ChainEntryLinkedHashedStrong<E>> chain;
	              ChainEntryLinkedHashedStrong<E>[]                              slots;


	// hashing
	final HashEqualator<? super E> hashEqualator;
	float                          hashDensity  ;

	// cached values
	transient int capacity, range;

	int size;



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	private EqHashEnum(final EqHashEnum<E> original)
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

	private EqHashEnum(
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
		for(ChainEntryLinkedHashedStrong<E> element = this.chain.head(); (element = element.next) != null;)
		{
			element.link = newSlots[element.hash & modulo];
			newSlots[element.hash & modulo] = element;
		}

		this.capacity = newSlotLength >= Integer.MAX_VALUE ? Integer.MAX_VALUE : (int)(newSlotLength * this.hashDensity);
		this.slots = newSlots;
		this.range = modulo;
	}

	final void internalCollectUnhashed(final E element)
	{
		this.chain.appendEntry(new ChainEntryLinkedHashedStrong<>(0, element, null));
	}



	///////////////////////////////////////////////////////////////////////////
	// inherited ExtendedCollection methods //
	///////////////////////////////////////////

	@Override
	public boolean nullAllowed()
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME ExtendedCollection<E>#nullAllowed()
	}

	@Override
	protected ChainStorage<E, E, E, ChainEntryLinkedHashedStrong<E>> getInternalStorageChain()
	{
		return this.chain;
	}

	@Override
	protected int internalRemoveNullEntries()
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME AbstractChainCollection<E>#internalRemoveNullEntries()
	}

	@Override
	protected int internalCountingAddAll(final E[] elements) throws UnsupportedOperationException
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME AbstractExtendedCollection<E>#internalCountingAddAll()
	}

	@Override
	protected int internalCountingAddAll(final E[] elements, final int offset, final int length)
		throws UnsupportedOperationException
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME AbstractExtendedCollection<E>#internalCountingAddAll()
	}

	@Override
	protected int internalCountingAddAll(final XGettingCollection<? extends E> elements)
		throws UnsupportedOperationException
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME AbstractExtendedCollection<E>#internalCountingAddAll()
	}

	@Override
	protected int internalCountingPutAll(final E[] elements) throws UnsupportedOperationException
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME AbstractExtendedCollection<E>#internalCountingPutAll()
	}

	@Override
	protected int internalCountingPutAll(final E[] elements, final int offset, final int length)
		throws UnsupportedOperationException
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME AbstractExtendedCollection<E>#internalCountingPutAll()
	}

	@Override
	protected int internalCountingPutAll(final XGettingCollection<? extends E> elements)
		throws UnsupportedOperationException
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME AbstractExtendedCollection<E>#internalCountingPutAll()
	}

	@Override
	protected void internalRemoveEntry(final ChainEntryLinkedHashedStrong<E> entry)
	{
		this.internalUnhashByEntry(entry);
		this.size--;
		this.chain.disjoinEntry(entry);
	}
	
	@Override
	protected int internalClear()
	{
		final int size = this.size;
		this.clear();
		return size;
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
	public final void clear()
	{
		// break inter-element references to ease GC
		this.chain.clear();

		// clear hash array
		final ChainEntryLinkedHashedStrong<E>[] slots = this.slots;
		for(int i = 0, length = slots.length; i < length; i++)
		{
			slots[i] = null;
		}

		// reset singleton fields
		this.size = 0;
	}

	@Override
	public final void truncate()
	{
		this.chain.clear();
		this.slots = ChainEntryLinkedHashedStrong.array(DEFAULT_HASH_LENGTH);
		this.size = 0;
		this.capacity = (int)(1 * this.hashDensity);
	}

	@Override
	public final long consolidate()
	{
		return this.chain.consolidate();
	}

	@Override
	public final CapacityExtendable ensureCapacity(final long minimalCapacity)
	{
		if(this.capacity >= minimalCapacity)
		{
			return this; // already enough free capacity
		}

		final int requiredSlotLength = (int)(minimalCapacity / this.hashDensity);
		if(XMath.isGreaterThanHighestPowerOf2(requiredSlotLength))
		{
			// (technical) magic value
			this.rebuildStorage(Integer.MAX_VALUE); // special case: maximum slots length needed ("perfect" hashing)
			return this;
		}

		// normal case: calculate new slots length and rebuild storage
		int newSlotsLength = this.slots.length;
		while(newSlotsLength < requiredSlotLength)
		{
			newSlotsLength <<= 1;
		}
		this.rebuildStorage(newSlotsLength);
		return this;
	}

	@Override
	public final CapacityExtendable ensureFreeCapacity(final long requiredFreeCapacity)
	{
		if(this.capacity - this.size >= requiredFreeCapacity)
		{
			return this; // already enough free capacity
		}

		// overflow-safe check for unreachable capacity
		if(Integer.MAX_VALUE - this.size < requiredFreeCapacity)
		{
			throw new ArrayCapacityException(requiredFreeCapacity + this.size);
		}

		final int requiredSlotLength = (int)((this.size + requiredFreeCapacity) / this.hashDensity);
		if(XMath.isGreaterThanHighestPowerOf2(requiredSlotLength))
		{
			// (technical) magic value
			this.rebuildStorage(Integer.MAX_VALUE); // special case: maximum slots length needed ("perfect" hashing)
			return this;
		}
		int newSlotsLength = this.slots.length;
		while(newSlotsLength < requiredSlotLength)
		{
			newSlotsLength <<= 1;
		}
		this.rebuildStorage(newSlotsLength);
		return this;
	}

	@Override
	public final long optimize()
	{
		final int requiredCapacity;
		if(XMath.isGreaterThanHighestPowerOf2(requiredCapacity = (int)(this.size / this.hashDensity)))
		{
			// (technical) magic value
			if(this.slots.length != Integer.MAX_VALUE)
			{
				this.rebuildStorage(Integer.MAX_VALUE);
			}
			return this.capacity;
		}

		final int newCapacity = XHashing.padHashLength(requiredCapacity);
		if(this.slots.length != newCapacity)
		{
			this.rebuildStorage(newCapacity); // rebuild storage with new capacity
		}

		return this.capacity;
	}

	@Override
	public final int rehash()
	{
		// local helper variables, including capacity recalculation while at rebuilding anyway
		final int                               reqCapacity   = XHashing.padHashLength((int)(this.size / this.hashDensity));
		final ChainEntryLinkedHashedStrong<E>[] slots         = ChainEntryLinkedHashedStrong.<E>array(reqCapacity);
		final int                               range         = reqCapacity >= Integer.MAX_VALUE ? Integer.MAX_VALUE : reqCapacity - 1;
		final HashEqualator<? super E>          hashEqualator = this.hashEqualator;
		final AbstractChainStorage<E, E, E, ChainEntryLinkedHashedStrong<E>> chain = this.chain;

		// keep the old chain head for old entries iteration and clear the chain for the new entries
		ChainEntryLinkedHashedStrong<E> element = chain.head().next;
		chain.clear();

		int size = 0;
		oldEntries:
		for(/*element must be outside, see comment*/; element != null; element = element.next)
		{
			final int hash = hashEqualator.hash(element.element);

			// check for rehash collisions
			for(ChainEntryLinkedHashedStrong<E> e = slots[hash & range]; e != null; e = e.link)
			{
				if(e.hash == hash && hashEqualator.equal(e.element, element.element))
				{
					continue oldEntries; // hash collision: value already contained, discard old element
				}
			}

			// register new element for unique element
			chain.appendEntry(slots[hash & range] =
				new ChainEntryLinkedHashedStrong<>(hash, element.element, slots[hash & range]))
			;
			size++;
		}

		// update collection state with new members
		this.slots = slots;
		this.range = range;
		this.size  = size ;
		return size;
	}

	@Override
	public final EqHashEnum<E> copy()
	{
		final EqHashEnum<E> newVarMap = new EqHashEnum<>(this);
		this.chain.iterate(new Consumer<E>()
		{
			@Override
			public void accept(final E element)
			{
				newVarMap.put(element);
			}
		});
		return newVarMap;
	}

	@Override
	public final EqConstHashEnum<E> immure()
	{
		this.consolidate();
		return EqConstHashEnum.New(this.hashEqualator, this.hashDensity, this);
	}

	@Override
	public final XGettingEnum<E> view()
	{
		return new EnumView<>(this);
	}

	@Override
	public final void setHashDensity(final float hashDensity)
	{
		this.capacity = (int)(this.slots.length * (this.hashDensity = XHashing.validateHashDensity(hashDensity))); // cast caps at max value
		this.optimize();
	}

	@Override
	public final boolean hasVolatileElements()
	{
		return false;
	}

	@Override
	public final OldVarSet<E> old()
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME EqHashTable#old()
	}

	@Override
	public final long currentCapacity()
	{
		return this.capacity;
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
	public final E addGet(final E element)
	{
		final int hash;
		for(ChainEntryLinkedHashedStrong<E> e = this.slots[(hash = this.hashEqualator.hash(element)) & this.range]; e != null; e = e.link)
		{
			if(e.hash == hash && this.hashEqualator.equal(e.element, element))
			{
				return e.element;
			}
		}
		this.chain.appendEntry(this.createNewEntry(hash, element));
		
		return null;
	}

	@Override
	public final E deduplicate(final E element)
	{
		final int hash;
		for(ChainEntryLinkedHashedStrong<E> e = this.slots[(hash = this.hashEqualator.hash(element)) & this.range]; e != null; e = e.link)
		{
			if(e.hash == hash && this.hashEqualator.equal(e.element, element))
			{
				return e.element;
			}
		}
		this.chain.appendEntry(this.createNewEntry(hash, element));
		
		return element;
	}

	@Override
	public final E putGet(final E element)
	{
		final int hash;
		for(ChainEntryLinkedHashedStrong<E> e = this.slots[(hash = this.hashEqualator.hash(element)) & this.range]; e != null; e = e.link)
		{
			if(e.hash == hash && this.hashEqualator.equal(e.element, element))
			{
				return e.setElement(element);
			}
		}
		this.chain.appendEntry(this.createNewEntry(hash, element));
		
		return null;
	}

	@Override
	public final E replace(final E element)
	{
		final int hash;
		for(ChainEntryLinkedHashedStrong<E> e = this.slots[(hash = this.hashEqualator.hash(element)) & this.range]; e != null; e = e.link)
		{
			if(e.hash == hash && this.hashEqualator.equal(e.element, element))
			{
				return e.setElement(element);
			}
		}

		return null;
	}

	/**
	 * Adds the passed element if it is not yet contained.
	 */
	@Override
	public final boolean add(final E element)
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

	/**
	 * {@inheritDoc}
	 * <p>
	 * In this implementation it overwrites equal, already contained elements.
	 */
	@Override
	public final boolean put(final E element)
	{
		final int hash;
		for(ChainEntryLinkedHashedStrong<E> e = this.slots[(hash = this.hashEqualator.hash(element)) & this.range]; e != null; e = e.link)
		{
			if(e.hash == hash && this.hashEqualator.equal(e.element, element))
			{
				e.setElement0(element); // intentionally no moving to end here to cleanly separate concerns
				return false;
			}
		}
		this.chain.appendEntry(this.createNewEntry(hash, element));
		return true;
	}

	@Override
	public final HashCollection.Analysis<EqHashEnum<E>> analyze()
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

	@Override
	public final EqHashEnum<E> sort(final Comparator<? super E> comparator)
	{
		this.chain.sort(comparator);
		return this;
	}



	///////////////////////////////////////////////////////////////////////////
	// getting methods //
	////////////////////

	@Override
	public final XEnum<E> range(final long lowIndex, final long highIndex)
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
	public final long count(final E element)
	{
		return this.chain.count(element, this.hashEqualator);
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
		if(samples == null || !(samples instanceof EqHashEnum<?>))
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
		this.consolidate();
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
		if(target == this)
		{
			return target; // copying a set logic collection to itself would be a no-op, so spare the effort
		}
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
	public final boolean nullAdd()
	{
		return this.add((E)null);
	}

	@SafeVarargs
	@Override
	public final EqHashEnum<E> addAll(final E... elements)
	{
		for(int i = 0, len = elements.length; i < len; i++)
		{
			this.add(elements[i]);
		}
		return this;
	}

	@Override
	public final EqHashEnum<E> addAll(final E[] elements, final int srcIndex, final int srcLength)
	{
		final int d;
		if((d = XArrays.validateArrayRange(elements, srcIndex, srcLength)) == 0)
		{
			return this;
		}

		final int bound = srcIndex + srcLength;
		for(int i = srcIndex; i != bound; i += d)
		{
			this.add(elements[i]);
		}

		return this;
	}

	@Override
	public final EqHashEnum<E> addAll(final XGettingCollection<? extends E> elements)
	{
		elements.iterate(this::add);
		return this;
	}

	@Override
	public final boolean nullPut()
	{
		return this.put((E)null);
	}

	@Override
	public final void accept(final E element)
	{
		this.put(element);
	}

	@SafeVarargs
	@Override
	public final EqHashEnum<E> putAll(final E... elements)
	{
		for(int i = 0, len = elements.length; i < len; i++)
		{
			this.put(elements[i]);
		}

		return this;
	}

	@Override
	public final EqHashEnum<E> putAll(final E[] elements, final int srcIndex, final int srcLength)
	{
		final int d;
		if((d = XArrays.validateArrayRange(elements, srcIndex, srcLength)) == 0)
		{
			return this;
		}

		final int bound = srcIndex + srcLength;
		for(int i = srcIndex; i != bound; i += d)
		{
			this.put(elements[i]);
		}

		return this;
	}

	@Override
	public final EqHashEnum<E> putAll(final XGettingCollection<? extends E> elements)
	{
		return elements.iterate(this);
	}

	// removing //

	@Override
	public final long remove(final E element)
	{
		final ChainEntryLinkedHashedStrong<E> e = this.internalUnhashByElement(element);
		if(e == null)
		{
			// element not contained in this collection instance.
			return 0;
		}

		// disjoinEntry entry from chain
		this.size--;
		this.chain.disjoinEntry(e);
		return 1;
	}
		
	@Override
	public long substitute(final Function<? super E, ? extends E> mapper)
	{
		return this.chain.substitute(mapper, this::replace);
	}
		
	final void replace(final ChainEntryLinkedHashedStrong<E> oldEntry, final E newElement)
	{
		final int newHash = this.hashEqualator.hash(newElement);
		for(ChainEntryLinkedHashedStrong<E> e = this.slots[newHash & this.range]; e != null; e = e.link)
		{
			if(e.hash == newHash && this.hashEqualator.equal(e.element, newElement))
			{
				if(e == oldEntry)
				{
					// simple case: the old entry's element gets replaced by a hash-equivalent new one.
					e.element = newElement;
					return;
				}
			}
		}
		
		/* complex case:
		 * Either a hash-conflicting entry's element has to be replaced with the new element
		 * or a new entry has to be created for the new element.
		 * In either case, the oldEntry has to be removed and the replacing entry has to move to
		 * the old entry's position in the sequence chain.
		 * Also, link chains have to be updated accordingly and the first case even reduces the collection's
		 * size by 1, while the second case keeps it constant.
		 * Quite the complication.
		 */
		
		throw new UnsupportedOperationException("Hash-changing replacement not supported, yet.");
	}
		
	// (21.08.2017 TM)NOTE: improved element removal
	private ChainEntryLinkedHashedStrong<E> internalUnhashByElement(final E element)
	{
		ChainEntryLinkedHashedStrong<E> last, e = this.slots[this.hashEqualator.hash(element) & this.range];

		// remove element from hashing chain
		if(e.element == element)
		{
			// head element special case
			this.slots[this.hashEqualator.hash(element) & this.range] = e.link;
			return e;
		}

		// sift through hash chain
		while((e = (last = e).link) != null)
		{
			if(e.element == element)
			{
				// element found in the hash chain
				last.link = e.link;
				return e;
			}
		}

		// element not contained in this collection instance.
		return null;
	}
	
	private void internalUnhashByEntry(final ChainEntryLinkedHashedStrong<E> entry)
	{
		// set only creates SetEntries internally, so this cast is safe.
		ChainEntryLinkedHashedStrong<E> last, e = this.slots[entry.hash & this.range];

		// remove element from hashing chain
		if(e == entry)
		{
			// head element special case
			this.slots[entry.hash & this.range] = entry.link;
			return;
		}
		
		while((e = (last = e).link) != null)
		{
			if(e == entry)
			{
				last.link = entry.link;
				return;
			}
		}
		
		throw new IllegalArgumentException("Set element inconsistency detected");
	}

	@Override
	public final long nullRemove()
	{
		return this.remove(null);
	}

	// reducing //

	@Override
	public final long removeBy(final Predicate<? super E> predicate)
	{
		return this.chain.reduce(predicate);
	}

	// retaining //

	@Override
	public final long retainAll(final XGettingCollection<? extends E> elements)
	{
		return this.chain.retainAll(elements, this.hashEqualator);
	}

	@Override
	public final <P extends Consumer<? super E>> P process(final P procedure)
	{
		this.chain.process(procedure);
		return procedure;
	}

	@Override
	public final <C extends Consumer<? super E>> C moveTo(final C target, final Predicate<? super E> predicate)
	{
		this.chain.moveTo(target, predicate);
		return target;
	}

	// removing - all //

	@Override
	public final long removeAll(final XGettingCollection<? extends E> elements)
	{
		final int oldSize = this.size;
		elements.iterate(this::remove);
		return oldSize - this.size;
	}

	// removing - duplicates //

	@Override
	public final long removeDuplicates()
	{
		return 0;
	}

	@Override
	public final long removeDuplicates(final Equalator<? super E> equalator)
	{
		// singleton null can be ignored here
		return this.chain.removeDuplicates(equalator);
	}

	@Override
	public final EqHashEnum<E> toReversed()
	{
		final EqHashEnum<E> reversedVarSet = this.copy();
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
	public final long indexOf(final E element)
	{
		return this.chain.indexOf(element, this.hashEqualator);
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
	public final long lastIndexOf(final E element)
	{
		return this.chain.lastIndexOf(element, this.hashEqualator);
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
	public final <C extends Consumer<? super E>> C moveSelection(final C target, final long... indices)
	{
		this.chain.moveSelection(target, indices);
		return target;
	}

	@Override
	public final E removeAt(final long index)
	{
		return this.chain.remove(index);
	}

	@Override
	public final E fetch()
	{
		return this.chain.remove(0);
	}

	@Override
	public final E pop()
	{
		return this.chain.remove(this.size - 1);
	}

	@Override
	public final E pinch()
	{
		return this.size == 0 ? null : this.chain.remove(0);
	}

	@Override
	public final E pick()
	{
		return this.size == 0 ? null : this.chain.remove(this.size - 1);
	}

	@Override
	public final E retrieve(final E element)
	{
		return this.chain.retrieve(element, this.hashEqualator);
	}

	@Override
	public final E retrieveBy(final Predicate<? super E> predicate)
	{
		return this.chain.retrieve(predicate);
	}

	@Override
	public final boolean removeOne(final E element)
	{
		return this.chain.removeOne(element, this.hashEqualator);
	}

	@Override
	public final EqHashEnum<E> removeRange(final long startIndex, final long length)
	{
		this.chain.removeRange(startIndex, length);
		return this;
	}

	@Override
	public final EqHashEnum<E> retainRange(final long startIndex, final long length)
	{
		this.chain.retainRange(startIndex, length);
		return this;
	}

	@Override
	public final long removeSelection(final long[] indices)
	{
		return this.chain.removeSelection(indices);
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
	public final EqHashEnum<E> reverse()
	{
		this.chain.reverse();
		return this;
	}

	@Override
	public final EqHashEnum<E> shiftTo(final long sourceIndex, final long targetIndex)
	{
		this.chain.shiftTo(sourceIndex, targetIndex);
		return this;
	}

	@Override
	public final EqHashEnum<E> shiftTo(final long sourceIndex, final long targetIndex, final long length)
	{
		this.chain.shiftTo(sourceIndex, targetIndex, length);
		return this;
	}

	@Override
	public final EqHashEnum<E> shiftBy(final long sourceIndex, final long distance)
	{
		this.chain.shiftTo(sourceIndex, distance);
		return this;
	}

	@Override
	public final EqHashEnum<E> shiftBy(final long sourceIndex, final long distance, final long length)
	{
		this.chain.shiftTo(sourceIndex, distance, length);
		return this;
	}

	@Override
	public final EqHashEnum<E> swap(final long indexA, final long indexB)
	{
		this.chain.swap(indexA, indexB);
		return this;
	}

	@Override
	public final EqHashEnum<E> swap(final long indexA, final long indexB, final long length)
	{
		this.chain.swap(indexA, indexB, length);
		return this;
	}

	@Override
	public final HashEqualator<? super E> equality()
	{
		return this.hashEqualator;
	}

	@Override
	public final boolean input(final long index, final E element)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME EqHashTable.Entries#input()
	}

	@SafeVarargs
	@Override
	public final long inputAll(final long index, final E... elements)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME EqHashTable.Entries#input()
	}

	@Override
	public final long inputAll(final long index, final E[] elements, final int offset, final int length)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME EqHashTable.Entries#inputAll()
	}

	@Override
	public final long inputAll(final long index, final XGettingCollection<? extends E> elements)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME EqHashTable.Entries#inputAll()
	}

	@Override
	public final boolean insert(final long index, final E element)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME EqHashTable.Entries#insert()
	}

	@SafeVarargs
	@Override
	public final long insertAll(final long index, final E... elements)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME EqHashTable.Entries#insert()
	}

	@Override
	public final long insertAll(final long index, final E[] elements, final int offset, final int length)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME EqHashTable.Entries#insertAll()
	}

	@Override
	public final long insertAll(final long index, final XGettingCollection<? extends E> elements)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME EqHashTable.Entries#insertAll()
	}

	@Override
	public final boolean prepend(final E element)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME EqHashTable.Entries#prepend()
	}

	@Override
	public final boolean preput(final E element)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME EqHashTable.Entries#preput()
	}

	@Override
	public final boolean nullInput(final long index)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME EqHashTable.Entries#nullInput()
	}

	@Override
	public final boolean nullInsert(final long index)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME EqHashTable.Entries#nullInsert()
	}

	@Override
	public final boolean nullPrepend()
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME EqHashTable.Entries#nullPrepend()
	}

	@SafeVarargs
	@Override
	public final EqHashEnum<E> prependAll(final E... elements)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME EqHashTable.Entries#prepend()
	}

	@Override
	public final EqHashEnum<E> prependAll(final E[] elements, final int srcStartIndex, final int srcLength)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME EqHashTable.Entries#prependAll()
	}

	@Override
	public final EqHashEnum<E> prependAll(final XGettingCollection<? extends E> elements)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME EqHashTable.Entries#prependAll()
	}

	@Override
	public final boolean nullPreput()
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME EqHashTable.Entries#nullPreput()
	}

	@SafeVarargs
	@Override
	public final EqHashEnum<E> preputAll(final E... elements)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME EqHashTable.Entries#preput()
	}

	@Override
	public final EqHashEnum<E> preputAll(final E[] elements, final int offset, final int length)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME EqHashTable.Entries#preputAll()
	}

	@Override
	public final EqHashEnum<E> preputAll(final XGettingCollection<? extends E> elements)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME EqHashTable.Entries#preputAll()
	}

	@Override
	public final boolean set(final long index, final E element)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME EqHashTable.Entries#set()
	}

	@Override
	public final E setGet(final long index, final E element)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME EqHashTable.Entries#setGet()
	}

	@Override
	public final void setFirst(final E element)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME EqHashTable.Entries#setFirst()
	}

	@Override
	public final void setLast(final E element)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME EqHashTable.Entries#setLast()
	}

	@SafeVarargs
	@Override
	public final EqHashEnum<E> setAll(final long index, final E... elements)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME EqHashTable.Entries#set()
	}

	@Override
	public final EqHashEnum<E> set(final long index, final E[] elements, final int offset, final int length)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME EqHashTable.Entries#set()
	}

	@Override
	public final EqHashEnum<E> set(final long index, final XGettingSequence<? extends E> elements, final long offset, final long length)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME EqHashTable.Entries#set()
	}



	public static final class OldVarSet<E> extends AbstractBridgeXSet<E>
	{
		OldVarSet(final EqHashEnum<E> set)
		{
			super(set);
		}

		@Override
		public EqHashEnum<E> parent()
		{
			return (EqHashEnum<E>)super.parent();
		}

	}

}

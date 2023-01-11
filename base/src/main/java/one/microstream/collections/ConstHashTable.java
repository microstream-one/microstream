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


import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import one.microstream.chars.VarString;
import one.microstream.collections.interfaces.HashCollection;
import one.microstream.collections.old.OldCollection;
import one.microstream.collections.old.OldList;
import one.microstream.collections.types.XEnum;
import one.microstream.collections.types.XGettingCollection;
import one.microstream.collections.types.XGettingEnum;
import one.microstream.collections.types.XGettingTable;
import one.microstream.collections.types.XImmutableList;
import one.microstream.collections.types.XImmutableTable;
import one.microstream.collections.types.XIterable;
import one.microstream.equality.Equalator;
import one.microstream.equality.IdentityEqualator;
import one.microstream.equality.IdentityEqualityLogic;
import one.microstream.functional.Aggregator;
import one.microstream.functional.IndexedAcceptor;
import one.microstream.functional.XFunc;
import one.microstream.hashing.HashEqualator;
import one.microstream.hashing.XHashing;
import one.microstream.typing.Composition;
import one.microstream.typing.KeyValue;
import one.microstream.typing.XTypes;


public final class ConstHashTable<K, V>
extends AbstractChainKeyValueCollection<K, V, ChainMapEntryLinkedStrongStrong<K, V>>
implements XImmutableTable<K, V>, HashCollection<K>, Composition, IdentityEqualityLogic
{
	public interface Creator<K, V>
	{
		public ConstHashTable<K, V> newInstance();
	}



	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////

	public static final <KI, VI, KO, VO> Aggregator<KeyValue<KI, VI>, ConstHashTable<KO, VO>> projector(
		final ConstHashTable<KO, VO> target        ,
		final Function<KI, KO>       keyProjector  ,
		final Function<VI, VO>       valueProjector
	)
	{
		return new Aggregator<KeyValue<KI, VI>, ConstHashTable<KO, VO>>()
		{
			@Override
			public void accept(final KeyValue<KI, VI> e)
			{
				target.internalAdd(
					keyProjector.apply(e.key()),
					valueProjector.apply(e.value())
				);
			}

			@Override
			public ConstHashTable<KO, VO> yield()
			{
				return target;
			}
		};
	}


	public static final <K, V> ConstHashTable<K, V> New()
	{
		return new ConstHashTable<>(
			DEFAULT_HASH_LENGTH,
			DEFAULT_HASH_FACTOR
		);
	}

	public static final <K, V> ConstHashTable<K, V> NewCustom(final int initialHashLength)
	{
		return new ConstHashTable<>(
			XHashing.padHashLength(initialHashLength),
			DEFAULT_HASH_FACTOR
		);
	}

	public static final <K, V> ConstHashTable<K, V> NewCustom(final float hashDensity)
	{
		return new ConstHashTable<>(
			DEFAULT_HASH_LENGTH,
			XHashing.validateHashDensity(hashDensity)
		);
	}

	public static final <K, V> ConstHashTable<K, V> NewCustom(final int initialHashLength, final float hashDensity)
	{
		return new ConstHashTable<>(
			XHashing.padHashLength(initialHashLength),
			XHashing.validateHashDensity(hashDensity)
		);
	}
	public static final <K, V> ConstHashTable<K, V> New(
		final XGettingCollection<? extends KeyValue<? extends K, ? extends V>> entries
	)
	{
		return new ConstHashTable<K, V>(
			DEFAULT_HASH_LENGTH,
			DEFAULT_HASH_FACTOR
		).internalAddEntries(entries);
	}

	public static final <K, V> ConstHashTable<K, V> NewCustom(
		final int                                                              initialHashLength,
		final float                                                            hashDensity      ,
		final XGettingCollection<? extends KeyValue<? extends K, ? extends V>> entries
	)
	{
		return new ConstHashTable<K, V>(
			XHashing.padHashLength(initialHashLength),
			XHashing.validateHashDensity(hashDensity)
		).internalAddEntries(entries);
	}

	public static final <K, V> ConstHashTable<K, V> NewSingle(final K key, final V value)
	{
		final ConstHashTable<K, V> instance = New();
		instance.internalAdd(key, value);
		return instance;
	}

	@SafeVarargs
	public static final <K, V> ConstHashTable<K, V> New(final KeyValue<? extends K, ? extends V>... entries)
	{
		return new ConstHashTable<K, V>(
			DEFAULT_HASH_LENGTH,
			DEFAULT_HASH_FACTOR
		).internalAddEntries(new ArrayView<>(entries));
	}

	@SafeVarargs
	public static final <K, V> ConstHashTable<K, V> NewCustom(
		final int                                   initialHashLength,
		final float                                 hashDensity      ,
		final KeyValue<? extends K, ? extends V>... entries
	)
	{
		return new ConstHashTable<K, V>(
			XHashing.padHashLength(initialHashLength),
			XHashing.validateHashDensity(hashDensity)
		).internalAddEntries(new ArrayView<>(entries));
	}

	public static final <KI, VI, KO, VO> ConstHashTable<KO, VO> NewProjected(
		final float                                          hashDensity  ,
		final XGettingCollection<? extends KeyValue<KI, VI>> entries      ,
		final Function<? super KI, KO>                       keyProjector ,
		final Function<? super VI, VO>                       valueProjector
	)
	{
		final ConstHashTable<KO, VO> newMap = new ConstHashTable<>(
			DEFAULT_HASH_LENGTH,
			XHashing.validateHashDensity(hashDensity)
		);
		entries.iterate(e->
		{
			newMap.internalAdd(keyProjector.apply(e.key()), valueProjector.apply(e.value()));
		});
		return newMap;
	}

	public static final <KO, VO, KI extends KO, VI extends VO> ConstHashTable<KO, VO> NewProjected(
		final XGettingCollection<? extends KeyValue<KI, VI>> entries
	)
	{
		return NewProjected(entries, XFunc.<KO>passThrough(), XFunc.<VO>passThrough());
	}

	public static final <KI, VI, KO, VO> ConstHashTable<KO, VO> NewProjected(
		final XGettingCollection<? extends KeyValue<KI, VI>> entries       ,
		final Function<? super KI, KO>                      keyProjector  ,
		final Function<? super VI, VO>                      valueProjector
	)
	{
		return NewProjected(
			entries instanceof HashCollection<?> ? ((HashCollection<?>)entries).hashDensity() : DEFAULT_HASH_FACTOR,
			entries       ,
			keyProjector  ,
			valueProjector
		);
	}



	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	// data storage
	final AbstractChainKeyValueStorage<K, V, ChainMapEntryLinkedStrongStrong<K, V>> chain;
	      ChainMapEntryLinkedStrongStrong<K, V>[]                                   slots;

	// hashing
	float hashDensity;

	// cached values
	int capacity, range, size;

	// satellite instances
	final Values values = new Values();
	final Keys   keys   = new Keys()  ;



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	private ConstHashTable(final ConstHashTable<K, V> original)
	{
		super();
		this.hashDensity   = original.hashDensity;
		this.range         = original.range;

		// constructor only copies configuration (concern #1), not data (#2). See copy() for copying data.
		this.slots         = ChainMapEntryLinkedStrongStrong.array(original.slots.length);
		this.chain         = new ChainStrongStrongStorage<>(this, new ChainMapEntryLinkedStrongStrong<K, V>(null, null, null));
		this.capacity      = original.capacity;
	}

	private ConstHashTable(
		final int              pow2InitialHashLength,
		final float            positiveHashDensity
	)
	{
		super();
		this.hashDensity   = positiveHashDensity;
		this.range         = pow2InitialHashLength - 1;

		this.slots         = ChainMapEntryLinkedStrongStrong.array(pow2InitialHashLength);
		this.chain         = new ChainStrongStrongStorage<>(this, new ChainMapEntryLinkedStrongStrong<K, V>(null, null, null));
		this.capacity      = (int)(pow2InitialHashLength * positiveHashDensity); // capped at MAX_VALUE
	}



	///////////////////////////////////////////////////////////////////////////
	// declared methods //
	/////////////////////

	private ChainMapEntryLinkedStrongStrong<K, V> createNewEntry(final K key, final V value)
	{
		if(this.size >= this.capacity)
		{
			ensureFreeArrayCapacity(this.size); // size limit only needs to be checked if size reached capacity
			this.increaseStorage();
		}

		ChainMapEntryLinkedStrongStrong<K, V> e;
		this.slots[System.identityHashCode(key) & this.range] = e =
			new ChainMapEntryLinkedStrongStrong<>(key, value, this.slots[System.identityHashCode(key) & this.range])
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
		final ChainMapEntryLinkedStrongStrong<K, V>[] newSlots =  ChainMapEntryLinkedStrongStrong.array(newSlotLength);
		final int modulo = newSlotLength >= Integer.MAX_VALUE ? Integer.MAX_VALUE : newSlotLength - 1;

		// iterate through all entries and assign them to the new storage
		for(ChainMapEntryLinkedStrongStrong<K, V> entry = this.chain.head(); (entry = entry.next) != null;)
		{
			entry.link = newSlots[System.identityHashCode(entry.key) & modulo];
			newSlots[System.identityHashCode(entry.key) & modulo] = entry;
		}

		this.capacity = newSlotLength >= Integer.MAX_VALUE ? Integer.MAX_VALUE : (int)(newSlotLength * this.hashDensity);
		this.slots = newSlots;
		this.range = modulo;
	}

	@Override
	public final KeyValue<K, V> lookup(final K key)
	{
		// search for key by hash
		for(ChainMapEntryLinkedStrongStrong<K, V> e = this.slots[System.identityHashCode(key) & this.range]; e != null; e = e.link)
		{
			if(e.key() == key)
			{
				return e;
			}
		}
		return null;
	}

	final boolean containsKey(final K key)
	{
		// search for element by hash
		for(ChainMapEntryLinkedStrongStrong<K, V> e = this.slots[System.identityHashCode(key) & this.range]; e != null; e = e.link)
		{
			if(e.key() == key)
			{
				return true;
			}
		}
		return false;
	}

	final void internalAdd(final K key, final V value)
	{
		for(ChainMapEntryLinkedStrongStrong<K, V> e = this.slots[System.identityHashCode(key) & this.range]; e != null; e = e.link)
		{
			if(e.key() == key)
			{
				return;
			}
		}
		this.chain.appendEntry(this.createNewEntry(key, value));
	}

	final ConstHashTable<K, V> internalAddEntries(final XGettingCollection<? extends KeyValue<? extends K, ? extends V>> entries)
	{
		entries.iterate(new Consumer<KeyValue<? extends K, ? extends V>>()
		{
			@Override
			public void accept(final KeyValue<? extends K, ? extends V> e)
			{
				ConstHashTable.this.internalAdd(e.key(), e.value());
			}
		});
		return this;
	}




	///////////////////////////////////////////////////////////////////////////
	// inheriteted ExtendedCollection methods //
	///////////////////////////////////////////

	@Override
	protected int internalCountingAddAll(final KeyValue<K, V>[] elements) throws UnsupportedOperationException
	{
		return this.internalCountingAddAll(elements, 0, elements.length);
	}

	@Override
	protected int internalCountingAddAll(final KeyValue<K, V>[] elements, final int offset, final int length)
		throws UnsupportedOperationException
	{
		throw new UnsupportedOperationException();
	}

	@Override
	protected int internalCountingAddAll(final XGettingCollection<? extends KeyValue<K, V>> elements)
		throws UnsupportedOperationException
	{
		throw new UnsupportedOperationException();
	}

	@Override
	protected int internalCountingPutAll(final KeyValue<K, V>[] elements) throws UnsupportedOperationException
	{
		throw new UnsupportedOperationException();
	}

	@Override
	protected int internalCountingPutAll(final KeyValue<K, V>[] elements, final int offset, final int length)
		throws UnsupportedOperationException
	{
		throw new UnsupportedOperationException();
	}

	@Override
	protected int internalCountingPutAll(final XGettingCollection<? extends KeyValue<K, V>> elements)
		throws UnsupportedOperationException
	{
		throw new UnsupportedOperationException();
	}

	@Override
	protected int internalRemoveNullEntries()
	{
		throw new UnsupportedOperationException();
	}

	@Override
	protected void internalRemoveEntry(final ChainMapEntryLinkedStrongStrong<K, V> entry)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	protected int internalClear()
	{
		throw new UnsupportedOperationException();
	}

	@Override
	protected AbstractChainKeyValueStorage<K, V, ChainMapEntryLinkedStrongStrong<K, V>> getInternalStorageChain()
	{
		return this.chain;
	}

	@Override
	public final long size()
	{
		return ConstHashTable.this.size;
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
	public final ConstHashTable<K, V> copy()
	{
		final ConstHashTable<K, V> newVarMap = new ConstHashTable<>(this);
		this.chain.iterate(new Consumer<KeyValue<K, V>>()
		{
			@Override
			public void accept(final KeyValue<K, V> entry)
			{
				newVarMap.internalAdd(entry.key(), entry.value());
			}
		});
		return newVarMap;
	}

	@Override
	public final ConstHashTable<K, V> immure()
	{
		return this;
	}

	@Override
	public final XGettingTable<K, V> view()
	{
		return new TableView<>(this);
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
	public final boolean nullAllowed()
	{
		return true;
	}

	@Override
	public final boolean nullKeyAllowed()
	{
		return true;
	}

	@Override
	public final boolean nullValuesAllowed()
	{
		return true;
	}

	@Override
	public final V get(final K key)
	{
		// search for key by hash
		for(ChainMapEntryLinkedStrongStrong<K, V> e = this.slots[System.identityHashCode(key) & this.range]; e != null; e = e.link)
		{
			if(e.key() == key)
			{
				return e.value();
			}
		}
		return null;
	}

	@Override
	public final ConstHashTable<K, V>.Keys keys()
	{
		return this.keys;
	}

	@Override
	public final XImmutableTable.EntriesBridge<K, V> old()
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME ConstHashTable#old()
	}

	@Override
	public one.microstream.collections.types.XImmutableTable.Bridge<K, V> oldMap()
	{
		return new OldVarMap();
	}

	@Override
	public final V searchValue(final Predicate<? super K> keyPredicate)
	{
		final KeyValue<K, V> foundEntry = this.chain.search(new Predicate<KeyValue<K, V>>()
		{
			@Override
			public boolean test(final KeyValue<K, V> entry)
			{
				return keyPredicate.test(entry.key());
			}
		});
		return foundEntry != null ? foundEntry.value() : null;
	}

	@Override
	public final <C extends Consumer<? super V>> C query(final XIterable<? extends K> keys, final C collector)
	{
		keys.iterate(new Consumer<K>()
		{
			@Override
			public void accept(final K key)
			{
				collector.accept(ConstHashTable.this.get(key));
			}
		});
		return collector;
	}

	@Override
	public final Values values()
	{
		return this.values;
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
	public final HashCollection.Analysis<ConstHashTable<K, V>> analyze()
	{
		return AbstractChainEntryLinked.analyzeSlots(this, this.slots);
	}

	@Override
	public final int hashDistributionRange()
	{
		return this.slots.length;
	}

	@Override
	public final HashEqualator<K> hashEquality()
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
		return this.chain.appendTo(VarString.New(this.slots.length).append('{'), ",").append('}').toString();
	}

	public final Predicate<KeyValue<K, V>> predicateContainsEntry()
	{
		return entry ->
		{
			final KeyValue<K, V> kv;
			if((kv = ConstHashTable.this.lookup(entry.key())) == null)
			{
				return false;
			}

			// equality of values is architectural restricted to simple referential equality
			return kv.key() == entry.key() && kv.value() == entry.value();
		};
	}



	///////////////////////////////////////////////////////////////////////////
	// getting methods //
	////////////////////

	@Override
	public final XEnum<KeyValue<K, V>> range(final long lowIndex, final long highIndex)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME ConstHashTable.Entries#range()
	}

	@Override
	public final XGettingEnum<KeyValue<K, V>> view(final long lowIndex, final long highIndex)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME ConstHashTable.Entries#view()
	}

	@Override
	public final KeyValue<K, V>[] toArray(final Class<KeyValue<K, V>> type)
	{
		return ConstHashTable.this.chain.toArray(type);
	}

	// executing //

	@Override
	public final <P extends Consumer<? super KeyValue<K, V>>> P iterate(final P procedure)
	{
		ConstHashTable.this.chain.iterate(procedure);
		return procedure;
	}

	@Override
	public final <A> A join(final BiConsumer<? super KeyValue<K, V>, ? super A> joiner, final A aggregate)
	{
		ConstHashTable.this.chain.join(joiner, aggregate);
		return aggregate;
	}

	@Override
	public final long count(final KeyValue<K, V> entry)
	{
		return ConstHashTable.this.chain.count(entry, this.equality());
	}

	@Override
	public final long countBy(final Predicate<? super KeyValue<K, V>> predicate)
	{
		return ConstHashTable.this.chain.count(predicate);
	}

	// element querying //

	@Override
	public final KeyValue<K, V> search(final Predicate<? super KeyValue<K, V>> predicate)
	{
		return ConstHashTable.this.chain.search(predicate);
	}

	@Override
	public final KeyValue<K, V> max(final Comparator<? super KeyValue<K, V>> comparator)
	{
		return ConstHashTable.this.chain.max(comparator);
	}

	@Override
	public final KeyValue<K, V> min(final Comparator<? super KeyValue<K, V>> comparator)
	{
		return ConstHashTable.this.chain.min(comparator);
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
//	public final boolean hasDistinctValues(final Equalator<? super KeyValue<K, V>> equalator)
//	{
//		return ConstHashTable.this.chain.hasDistinctValues(equalator);
//	}

	// boolean querying - applies //

	@Override
	public final boolean containsSearched(final Predicate<? super KeyValue<K, V>> predicate)
	{
		return ConstHashTable.this.chain.containsSearched(predicate);
	}

	@Override
	public final boolean applies(final Predicate<? super KeyValue<K, V>> predicate)
	{
		return ConstHashTable.this.chain.appliesAll(predicate);
	}

	// boolean querying - contains //

	@Override
	public final boolean nullContained()
	{
		return false;
	}

	@Override
	public final boolean containsId(final KeyValue<K, V> entry)
	{
		// search for element by hash
		for(ChainMapEntryLinkedStrongStrong<K, V> e = ConstHashTable.this.slots[System.identityHashCode(entry.key()) & ConstHashTable.this.range]; e != null; e = e.link)
		{
			if(entry == e.key())
			{
				return true;
			}
		}
		return false;
	}

	@Override
	public final boolean contains(final KeyValue<K, V> entry)
	{
		// search for element by hash
		for(ChainMapEntryLinkedStrongStrong<K, V> e = ConstHashTable.this.slots[System.identityHashCode(entry.key()) & ConstHashTable.this.range]; e != null; e = e.link)
		{
			if(e.key() == entry.key())
			{
				return true;
			}
		}
		return false;
	}

	@Override
	public final KeyValue<K, V> seek(final KeyValue<K, V> sample)
	{
		if(sample == null)
		{
			// null special case
			return null;
		}

		// search for element by hash
		for(ChainMapEntryLinkedStrongStrong<K, V> e = this.slots[System.identityHashCode(sample.key()) & this.range]; e != null; e = e.link)
		{
			if(e.key() == sample.key())
			{
				return e;
			}
		}
		return null;
	}

	@Override
	public final boolean containsAll(final XGettingCollection<? extends KeyValue<K, V>> elements)
	{
		return elements.applies(ConstHashTable.this.predicateContainsEntry());
	}

	// boolean querying - equality //

	@Override
	public final boolean equals(final XGettingCollection<? extends KeyValue<K, V>> samples, final Equalator<? super KeyValue<K, V>> equalator)
	{
		if(samples == null || !(samples instanceof ConstHashTable<?, ?>.Keys))
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
	public final boolean equalsContent(final XGettingCollection<? extends KeyValue<K, V>> samples, final Equalator<? super KeyValue<K, V>> equalator)
	{
		if(ConstHashTable.this.size != XTypes.to_int(samples.size()))
		{
			return false;
		}

		// if sizes are equal and all elements of collection are contained in this set, they must have equal content
		return ConstHashTable.this.chain.equalsContent(samples, equalator);
	}

	// data set procedures //

	@Override
	public final <C extends Consumer<? super KeyValue<K, V>>> C intersect(
		final XGettingCollection<? extends KeyValue<K, V>> other,
		final Equalator<? super KeyValue<K, V>> equalator,
		final C target
	)
	{
		return ConstHashTable.this.chain.intersect(other, equalator, target);
	}

	@Override
	public final <C extends Consumer<? super KeyValue<K, V>>> C except(
		final XGettingCollection<? extends KeyValue<K, V>> other,
		final Equalator<? super KeyValue<K, V>> equalator,
		final C target
	)
	{
		return ConstHashTable.this.chain.except(other, equalator, target);
	}

	@Override
	public final <C extends Consumer<? super KeyValue<K, V>>> C union(
		final XGettingCollection<? extends KeyValue<K, V>> other,
		final Equalator<? super KeyValue<K, V>> equalator,
		final C target
	)
	{
		return ConstHashTable.this.chain.union(other, equalator, target);
	}

	@Override
	public final <C extends Consumer<? super KeyValue<K, V>>> C copyTo(final C target)
	{
		return ConstHashTable.this.chain.copyTo(target);
	}

	@Override
	public final <C extends Consumer<? super KeyValue<K, V>>> C filterTo(final C target, final Predicate<? super KeyValue<K, V>> predicate)
	{
		return ConstHashTable.this.chain.copyTo(target, predicate);
	}

	@Override
	public final <C extends Consumer<? super KeyValue<K, V>>> C distinct(final C target)
	{
		return this.distinct(target, this.equality());
	}

	@Override
	public final <C extends Consumer<? super KeyValue<K, V>>> C distinct(final C target, final Equalator<? super KeyValue<K, V>> equalator)
	{
		return ConstHashTable.this.chain.distinct(target, equalator);
	}

	@Override
	public final ConstHashTable<K, V> toReversed()
	{
		final ConstHashTable<K, V> reversedVarSet = ConstHashTable.this.copy();
		reversedVarSet.chain.reverse();
		return reversedVarSet;
	}

	@Override
	public final <T extends Consumer<? super KeyValue<K, V>>> T copySelection(final T target, final long... indices)
	{
		ConstHashTable.this.chain.copySelection(target, indices);
		return target;
	}

	@Override
	public final <P extends IndexedAcceptor<? super KeyValue<K, V>>> P iterateIndexed(final P procedure)
	{
		ConstHashTable.this.chain.iterateIndexed(procedure);
		return procedure;
	}

	@Override
	public final KeyValue<K, V> at(final long index)
	{
		return ConstHashTable.this.chain.get(index);
	}

	@Override
	public final KeyValue<K, V> get()
	{
		return ConstHashTable.this.chain.first();
	}

	@Override
	public final KeyValue<K, V> first()
	{
		return ConstHashTable.this.chain.first();
	}

	@Override
	public final KeyValue<K, V> last()
	{
		return ConstHashTable.this.chain.last();
	}

	@Override
	public final KeyValue<K, V> poll()
	{
		return ConstHashTable.this.size == 0 ? null : ConstHashTable.this.chain.first();
	}

	@Override
	public final KeyValue<K, V> peek()
	{
		return ConstHashTable.this.size == 0 ? null : ConstHashTable.this.chain.last();
	}

	@Override
	public final long indexOf(final KeyValue<K, V> entry)
	{
		return ConstHashTable.this.chain.indexOf(entry);
	}

	@Override
	public final long indexBy(final Predicate<? super KeyValue<K, V>> predicate)
	{
		return ConstHashTable.this.chain.indexOf(predicate);
	}

	@Override
	public final boolean isSorted(final Comparator<? super KeyValue<K, V>> comparator)
	{
		return ConstHashTable.this.chain.isSorted(comparator);
	}

	@Override
	public final long lastIndexOf(final KeyValue<K, V> entry)
	{
		return ConstHashTable.this.chain.lastIndexOf(entry);
	}

	@Override
	public final long lastIndexBy(final Predicate<? super KeyValue<K, V>> predicate)
	{
		return ConstHashTable.this.chain.lastIndexBy(predicate);
	}

	@Override
	public final long maxIndex(final Comparator<? super KeyValue<K, V>> comparator)
	{
		return ConstHashTable.this.chain.maxIndex(comparator);
	}

	@Override
	public final long minIndex(final Comparator<? super KeyValue<K, V>> comparator)
	{
		return ConstHashTable.this.chain.minIndex(comparator);
	}

	@Override
	public final long scan(final Predicate<? super KeyValue<K, V>> predicate)
	{
		return ConstHashTable.this.chain.scan(predicate);
	}

	@Override
	public final Iterator<KeyValue<K, V>> iterator()
	{
		return ConstHashTable.this.chain.iterator();
	}

	@Override
	public final Object[] toArray()
	{
		return ConstHashTable.this.chain.toArray();
	}

	@Override
	public final HashEqualator<KeyValue<K, V>> equality()
	{
		return XHashing.keyValueHashEqualityKeyIdentity();
	}



	public final class Keys implements XImmutableTable.Keys<K, V>, HashCollection<K>
	{
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		public final int hashDistributionRange()
		{
			return ConstHashTable.this.slots.length;
		}

		@Override
		public final boolean hasVolatileHashElements()
		{
			return ConstHashTable.this.chain.hasVolatileElements();
		}

		@Override
		public final void setHashDensity(final float hashDensity)
		{
			ConstHashTable.this.setHashDensity(hashDensity);
		}

		@Override
		public final HashCollection.Analysis<Keys> analyze()
		{
			return AbstractChainEntryLinked.analyzeSlots(this, ConstHashTable.this.slots);
		}



		///////////////////////////////////////////////////////////////////////////
		// getting methods //
		////////////////////

		@Override
		public final Equalator<? super K> equality()
		{
			return ConstHashTable.this.hashEquality();
		}

		@Override
		public final Keys copy()
		{
			return ConstHashTable.this.copy().keys();
		}

		/**
		 * This method creates a {@link EqConstHashEnum} instance containing all (currently existing) elements
		 * of this {@link ConstHashTable}.<br>
		 * No matter which hashing logic this instance uses, the new {@link EqConstHashEnum} instance always uses
		 * a STRONG EQUALATOR logic, using this instance's logic's {@link HashEqualator}.<br>
		 * This is necessary to ensure that the {@link EqConstHashEnum} instance is really constant and does not
		 * (can not!) lose elements over time.<br>
		 * If a {@link EqConstHashEnum} with volatile elements is needed (e.g. as a "read-only weak set"),
		 * an appropriate custom behavior {@link EqConstHashEnum} instance can be created via the various
		 * copy constructors.
		 *
		 * @return a new {@link EqConstHashEnum} instance strongly referencing this set's current elements.
		 */
		@Override
		public final ConstHashTable<K, V>.Keys immure()
		{
			return this;
		}

		@Override
		public final XGettingEnum<K> view()
		{
			return new EnumView<>(this);
		}

		@Override
		public final XEnum<K> range(final long lowIndex, final long highIndex)
		{
			throw new one.microstream.meta.NotImplementedYetError(); // FIXME ConstHashTable.Keys#range()
		}

		@Override
		public final XGettingEnum<K> view(final long lowIndex, final long highIndex)
		{
			throw new one.microstream.meta.NotImplementedYetError(); // FIXME ConstHashTable.Keys#view()
		}

		@Override
		public final K[] toArray(final Class<K> type)
		{
			return ConstHashTable.this.chain.keyToArray(type);
		}

		// executing //

		@Override
		public final <P extends Consumer<? super K>> P iterate(final P procedure)
		{
			ConstHashTable.this.chain.keyIterate(procedure);
			return procedure;
		}

		@Override
		public final <A> A join(final BiConsumer<? super K, ? super A> joiner, final A aggregate)
		{
			ConstHashTable.this.chain.keyJoin(joiner, aggregate);
			return aggregate;
		}

		@Override
		public final long count(final K element)
		{
			return this.contains(element) ? 1 : 0;
		}

		@Override
		public final long countBy(final Predicate<? super K> predicate)
		{
			return ConstHashTable.this.chain.keyCount(predicate);
		}

		// element querying //

		@Override
		public final K seek(final K sample)
		{
			return ConstHashTable.this.chain.keySeek(sample);
		}

		@Override
		public final K search(final Predicate<? super K> predicate)
		{
			return ConstHashTable.this.chain.keySearch(predicate);
		}

		@Override
		public final K max(final Comparator<? super K> comparator)
		{
			return ConstHashTable.this.chain.keyMax(comparator);
		}

		@Override
		public final K min(final Comparator<? super K> comparator)
		{
			return ConstHashTable.this.chain.keyMin(comparator);
		}

		// boolean querying //

		@Override
		public final boolean hasVolatileElements()
		{
			return ConstHashTable.this.chain.hasVolatileElements();
		}

		@Override
		public final boolean nullAllowed()
		{
			return true;
		}

//		/**
//		 * As per definition of a set, this method always returns true.<br>
//		 * Note that mutated elements whose hashcode has not been immuted by the employed hash logic
//		 * can be contained multiple times, effectively breaking this method (because of breaking the hashing logic in the
//		 * first place), so this information only has value if the elements' implementation is immutable or if the
//		 * hash logic compensated their mutability (e.g. by using the identity hash code or by registering a once created
//		 * hashcode, effectively "immuting" it).
//		 *
//		 * @return
//		 * @see XGettingCollection#hasDistinctValues()
//		 */
//		@Override
//		public final boolean hasDistinctValues()
//		{
//			return true;
//		}
//
//		@Override
//		public final boolean hasDistinctValues(final Equalator<? super K> equalator)
//		{
//			if(equalator instanceof IdentityEqualator<?>)
//			{
//				return true;
//			}
//			throw new one.microstream.meta.NotImplementedYetError(); // FIXME ConstHashTable.Keys#hasDistinctValues()
//		}

		// boolean querying - applies //

		@Override
		public final boolean containsSearched(final Predicate<? super K> predicate)
		{
			return ConstHashTable.this.chain.keyApplies(predicate);
		}

		@Override
		public final boolean applies(final Predicate<? super K> predicate)
		{
			return ConstHashTable.this.chain.keyAppliesAll(predicate);
		}

		// boolean querying - contains //

		@Override
		public final boolean nullContained()
		{
			return this.contains((K)null);
		}

		@Override
		public final boolean containsId(final K element)
		{
			// search for element by hash
			for(ChainMapEntryLinkedStrongStrong<K, V> e = ConstHashTable.this.slots[System.identityHashCode(element) & ConstHashTable.this.range]; e != null; e = e.link)
			{
				if(element == e.key())
				{
					return true;
				}
			}
			return false;
		}

		@Override
		public final boolean contains(final K element)
		{
			// search for element by hash
			for(ChainMapEntryLinkedStrongStrong<K, V> e = ConstHashTable.this.slots[System.identityHashCode(element) & ConstHashTable.this.range]; e != null; e = e.link)
			{
				if(e.key() == element)
				{
					return true;
				}
			}
			return false;
		}

		@Override
		public final boolean containsAll(final XGettingCollection<? extends K> elements)
		{
			return elements.applies(this::contains);
		}

		// boolean querying - equality //

		@Override
		public final boolean equals(final XGettingCollection<? extends K> samples, final Equalator<? super K> equalator)
		{
			if(samples == null || !(samples instanceof ConstHashTable<?, ?>.Keys))
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
		public final boolean equalsContent(final XGettingCollection<? extends K> samples, final Equalator<? super K> equalator)
		{
			if(ConstHashTable.this.size != XTypes.to_int(samples.size()))
			{
				return false;
			}

			// if sizes are equal and all elements of collection are contained in this set, they must have equal content
			return ConstHashTable.this.chain.keyEqualsContent(samples, equalator);
		}

		// data set procedures //

		@Override
		public final <C extends Consumer<? super K>> C intersect(
			final XGettingCollection<? extends K> other,
			final Equalator<? super K> equalator,
			final C target
		)
		{
			return ConstHashTable.this.chain.keyIntersect(other, equalator, target);
		}

		@Override
		public final <C extends Consumer<? super K>> C except(
			final XGettingCollection<? extends K> other,
			final Equalator<? super K> equalator,
			final C target
		)
		{
			return ConstHashTable.this.chain.keyExcept(other, equalator, target);
		}

		@Override
		public final <C extends Consumer<? super K>> C union(
			final XGettingCollection<? extends K> other,
			final Equalator<? super K> equalator,
			final C target
		)
		{
			return ConstHashTable.this.chain.keyUnion(other, equalator, target);
		}

		@Override
		public final <C extends Consumer<? super K>> C copyTo(final C target)
		{
			return ConstHashTable.this.chain.keyCopyTo(target);
		}

		@Override
		public final <C extends Consumer<? super K>> C filterTo(final C target, final Predicate<? super K> predicate)
		{
			return ConstHashTable.this.chain.keyCopyTo(target, predicate);
		}

		@Override
		public final <C extends Consumer<? super K>> C distinct(final C target)
		{
			return ConstHashTable.this.chain.keyDistinct(target);
		}

		@Override
		public final <C extends Consumer<? super K>> C distinct(final C target, final Equalator<? super K> equalator)
		{
			if(equalator instanceof IdentityEqualator<?>)
			{
				return this.copyTo(target);
			}
			return ConstHashTable.this.chain.keyDistinct(target, equalator);
		}



		///////////////////////////////////////////////////////////////////////////
		// adding //
		///////////

		@Override
		public final long maximumCapacity()
		{
			return ConstHashTable.this.maximumCapacity();
		}

		@Override
		public final boolean isFull()
		{
			return ConstHashTable.this.isFull();
		}

		@Override
		public final long remainingCapacity()
		{
			return ConstHashTable.this.remainingCapacity();
		}



		///////////////////////////////////////////////////////////////////////////
		// removing //
		/////////////

		@Override
		public final Keys toReversed()
		{
			final ConstHashTable<K, V> reversedVarSet = ConstHashTable.this.copy();
			reversedVarSet.chain.reverse();
			return reversedVarSet.keys;
		}

		@Override
		public final <T extends Consumer<? super K>> T copySelection(final T target, final long... indices)
		{
			ConstHashTable.this.chain.keyCopySelection(target, indices);
			return target;
		}

		@Override
		public final <P extends IndexedAcceptor<? super K>> P iterateIndexed(final P procedure)
		{
			ConstHashTable.this.chain.keyIterateIndexed(procedure);
			return procedure;
		}

		@Override
		public final K at(final long index)
		{
			return ConstHashTable.this.chain.keyGet(index);
		}

		@Override
		public final K get()
		{
			return ConstHashTable.this.chain.keyFirst();
		}

		@Override
		public final K first()
		{
			return ConstHashTable.this.chain.keyFirst();
		}

		@Override
		public final K last()
		{
			return ConstHashTable.this.chain.keyLast();
		}

		@Override
		public final K poll()
		{
			return ConstHashTable.this.size == 0 ? null : ConstHashTable.this.chain.keyFirst();
		}

		@Override
		public final K peek()
		{
			return ConstHashTable.this.size == 0 ? null : ConstHashTable.this.chain.keyLast();
		}

		@Override
		public final long indexOf(final K element)
		{
			return ConstHashTable.this.chain.keyIndexOf(element);
		}

		@Override
		public final long indexBy(final Predicate<? super K> predicate)
		{
			return ConstHashTable.this.chain.keyIndexBy(predicate);
		}

		@Override
		public final boolean isSorted(final Comparator<? super K> comparator)
		{
			return ConstHashTable.this.chain.keyIsSorted(comparator);
		}

		@Override
		public final long lastIndexOf(final K element)
		{
			return ConstHashTable.this.chain.keyLastIndexOf(element);
		}

		@Override
		public final long lastIndexBy(final Predicate<? super K> predicate)
		{
			return ConstHashTable.this.chain.keyLastIndexBy(predicate);
		}

		@Override
		public final long maxIndex(final Comparator<? super K> comparator)
		{
			return ConstHashTable.this.chain.keyMaxIndex(comparator);
		}

		@Override
		public final long minIndex(final Comparator<? super K> comparator)
		{
			return ConstHashTable.this.chain.keyMinIndex(comparator);
		}

		@Override
		public final long scan(final Predicate<? super K> predicate)
		{
			return ConstHashTable.this.chain.keyScan(predicate);
		}

		@Override
		public final boolean isEmpty()
		{
			return ConstHashTable.this.isEmpty();
		}

		@Override
		public final Iterator<K> iterator()
		{
			return ConstHashTable.this.chain.keyIterator();
		}

		@Override
		public final long size()
		{
			return ConstHashTable.this.size;
		}

		@Override
		public final int rehash()
		{
			return ConstHashTable.this.rehash();
		}

		@Override
		public final String toString()
		{
			if(ConstHashTable.this.size == 0)
			{
				return "[]"; // array causes problems with escape condition otherwise
			}

			final VarString vc = VarString.New(ConstHashTable.this.slots.length).append('[');
			ConstHashTable.this.chain.keyAppendTo(vc, ',').append(']');
			return vc.toString();
		}

		@Override
		public final Object[] toArray()
		{
			return ConstHashTable.this.chain.keyToArray();
		}

		@Override
		public final OldCollection<K> old()
		{
			throw new one.microstream.meta.NotImplementedYetError(); // FIXME ConstHashTable.Keys#old()
		}

		@Override
		public final ConstHashTable<K, V> parent()
		{
			return ConstHashTable.this;
		}

		@Override
		public final HashEqualator<K> hashEquality()
		{
			return ConstHashTable.this.hashEquality();
		}

		@Override
		public final float hashDensity()
		{
			return ConstHashTable.this.hashDensity();
		}

	}



	public final class Values implements XImmutableTable.Values<K, V>
	{
		@Override
		public final Equalator<? super V> equality()
		{
			return Equalator.identity();
		}

		@Override
		public final XImmutableList<V> copy()
		{
			return ConstList.New(this);
		}

		@Override
		public final <P extends Consumer<? super V>> P iterate(final P procedure)
		{
			ConstHashTable.this.chain.valuesIterate(procedure);
			return procedure;
		}

		@Override
		public final <A> A join(final BiConsumer<? super V, ? super A> joiner, final A aggregate)
		{
			ConstHashTable.this.chain.valuesJoin(joiner, aggregate);
			return aggregate;
		}

		@Override
		public final <P extends IndexedAcceptor<? super V>> P iterateIndexed(final P procedure)
		{
			ConstHashTable.this.chain.valuesIterateIndexed(procedure);
			return procedure;
		}

		@Override
		public final Values toReversed()
		{
			final ConstHashTable<K, V> reversedVarSet = ConstHashTable.this.copy();
			reversedVarSet.chain.reverse();
			return reversedVarSet.values;
		}

		@Override
		public final boolean containsSearched(final Predicate<? super V> predicate)
		{
			return ConstHashTable.this.chain.valuesApplies(predicate);
		}

		@Override
		public final boolean applies(final Predicate<? super V> predicate)
		{
			return ConstHashTable.this.chain.valuesAppliesAll(predicate);
		}

		@Override
		public final boolean contains(final V value)
		{
			return ConstHashTable.this.chain.valuesContains(value);
		}

		@Override
		public final boolean containsAll(final XGettingCollection<? extends V> values)
		{
			return ConstHashTable.this.chain.valuesContainsAll(values);
		}

		@Override
		public final boolean containsId(final V value)
		{
			return ConstHashTable.this.chain.valuesContainsId(value);
		}

		@Override
		public final <T extends Consumer<? super V>> T copyTo(final T target)
		{
			ConstHashTable.this.chain.valuesCopyTo(target);
			return target;
		}

		@Override
		public final <T extends Consumer<? super V>> T filterTo(final T target, final Predicate<? super V> predicate)
		{
			ConstHashTable.this.chain.valuesCopyTo(target, predicate);
			return target;
		}

		@Override
		public final long count(final V value)
		{
			return ConstHashTable.this.chain.valuesCount(value);
		}

		@Override
		public final long countBy(final Predicate<? super V> predicate)
		{
			return ConstHashTable.this.chain.valuesCount(predicate);
		}

		@Override
		public final <T extends Consumer<? super V>> T distinct(final T target)
		{
			ConstHashTable.this.chain.valuesDistinct(target);
			return target;
		}

		@Override
		public final <T extends Consumer<? super V>> T distinct(final T target, final Equalator<? super V> equalator)
		{
			ConstHashTable.this.chain.valuesDistinct(target, equalator);
			return target;
		}

		@Override
		public final boolean equals(final XGettingCollection<? extends V> samples, final Equalator<? super V> equalator)
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
		public final boolean equalsContent(final XGettingCollection<? extends V> samples, final Equalator<? super V> equalator)
		{
			return ConstHashTable.this.chain.valuesEqualsContent(samples, equalator);
		}

		@Override
		public final <T extends Consumer<? super V>> T except(final XGettingCollection<? extends V> other, final Equalator<? super V> equalator, final T target)
		{
			ConstHashTable.this.chain.valuesExcept(other, equalator, target);
			return target;
		}

//		@Override
//		public final boolean hasDistinctValues()
//		{
//			return ConstHashTable.this.chain.valuesHasDistinctValues();
//		}
//
//		@Override
//		public final boolean hasDistinctValues(final Equalator<? super V> equalator)
//		{
//			return ConstHashTable.this.chain.valuesHasDistinctValues(equalator);
//		}

		@Override
		public final boolean hasVolatileElements()
		{
			return false;
		}

		@Override
		public final <T extends Consumer<? super V>> T intersect(final XGettingCollection<? extends V> other, final Equalator<? super V> equalator, final T target)
		{
			ConstHashTable.this.chain.valuesIntersect(other, equalator, target);
			return target;
		}

		@Override
		public final boolean isEmpty()
		{
			return ConstHashTable.this.isEmpty();
		}

		@Override
		public final Iterator<V> iterator()
		{
			return ConstHashTable.this.chain.valuesIterator();
		}

		@Override
		public final V max(final Comparator<? super V> comparator)
		{
			return ConstHashTable.this.chain.valuesMax(comparator);
		}

		@Override
		public final V min(final Comparator<? super V> comparator)
		{
			return ConstHashTable.this.chain.valuesMin(comparator);
		}

		@Override
		public final boolean nullAllowed()
		{
			return ConstHashTable.this.nullAllowed();
		}

		@Override
		public final boolean nullContained()
		{
			return ConstHashTable.this.chain.valuesContains(null);
		}

		@Override
		public final OldList<V> old()
		{
			throw new one.microstream.meta.NotImplementedYetError(); // FIXME ConstHashTable.Values#old()
		}

		@Override
		public final V seek(final V sample)
		{
			return ConstHashTable.this.chain.valuesSeek(sample);
		}

		@Override
		public final V search(final Predicate<? super V> predicate)
		{
			return ConstHashTable.this.chain.valuesSearch(predicate);
		}

		@Override
		public final long size()
		{
			return XTypes.to_int(ConstHashTable.this.size());
		}

		@Override
		public final long maximumCapacity()
		{
			return XTypes.to_int(ConstHashTable.this.size());
		}

		@Override
		public final boolean isFull()
		{
			return ConstHashTable.this.isFull();
		}

		@Override
		public final long remainingCapacity()
		{
			return ConstHashTable.this.remainingCapacity();
		}

		@Override
		public final String toString()
		{
			if(ConstHashTable.this.size == 0)
			{
				return "[]"; // array causes problems with escape condition otherwise
			}

			final VarString vc = VarString.New(ConstHashTable.this.slots.length).append('[');
			ConstHashTable.this.chain.valuesAppendTo(vc, ',').append(']');
			return vc.toString();
		}

		@Override
		public final Object[] toArray()
		{
			return ConstHashTable.this.chain.valuesToArray();
		}

		@Override
		public final V[] toArray(final Class<V> type)
		{
			return ConstHashTable.this.chain.valuesToArray(type);
		}

		@Override
		public final <T extends Consumer<? super V>> T union(
			final XGettingCollection<? extends V> other,
			final Equalator<? super V> equalator,
			final T target
		)
		{
			ConstHashTable.this.chain.valuesUnion(other, equalator, target);
			return target;
		}

		@Override
		public final ConstHashTable<K, V> parent()
		{
			return ConstHashTable.this;
		}

		@Override
		public final XImmutableList<V> view(final long fromIndex, final long toIndex)
		{
			throw new one.microstream.meta.NotImplementedYetError(); // FIXME ConstHashTable.Values#view()
		}

		@Override
		public final ListIterator<V> listIterator()
		{
			return ConstHashTable.this.chain.valuesListIterator(0);
		}

		@Override
		public final ListIterator<V> listIterator(final long index)
		{
			return ConstHashTable.this.chain.valuesListIterator(index);
		}

		@Override
		public final XImmutableList<V> range(final long fromIndex, final long toIndex)
		{
			throw new one.microstream.meta.NotImplementedYetError(); // FIXME ConstHashTable.Values#view()
		}

		@Override
		public final XImmutableList<V> immure()
		{
			return ConstList.New(this);
		}

		@Override
		public final XImmutableList<V> view()
		{
			throw new one.microstream.meta.NotImplementedYetError(); // FIXME ConstHashTable.Values#view()
		}

		@Override
		public final <T extends Consumer<? super V>> T copySelection(final T target, final long... indices)
		{
			ConstHashTable.this.chain.valuesCopySelection(target, indices);
			return target;
		}

		@Override
		public final V at(final long index)
		{
			return ConstHashTable.this.chain.valuesGet(index);
		}

		@Override
		public final V get()
		{
			return ConstHashTable.this.chain.valuesFirst();
		}

		@Override
		public final V first()
		{
			return ConstHashTable.this.chain.valuesFirst();
		}

		@Override
		public final V last()
		{
			return ConstHashTable.this.chain.valuesLast();
		}

		@Override
		public final V poll()
		{
			return ConstHashTable.this.size == 0 ? null : ConstHashTable.this.chain.valuesFirst();
		}

		@Override
		public final V peek()
		{
			return ConstHashTable.this.size == 0 ? null : ConstHashTable.this.chain.valuesLast();
		}

		@Override
		public final long indexOf(final V value)
		{
			return ConstHashTable.this.chain.valuesIndexOf(value);
		}

		@Override
		public final long indexBy(final Predicate<? super V> predicate)
		{
			return ConstHashTable.this.chain.valuesIndexBy(predicate);
		}

		@Override
		public final boolean isSorted(final Comparator<? super V> comparator)
		{
			return ConstHashTable.this.chain.valuesIsSorted(comparator);
		}

		@Override
		public final long lastIndexOf(final V value)
		{
			return ConstHashTable.this.chain.valuesLastIndexOf(value);
		}

		@Override
		public final long lastIndexBy(final Predicate<? super V> predicate)
		{
			return ConstHashTable.this.chain.valuesLastIndexBy(predicate);
		}

		@Override
		public final long maxIndex(final Comparator<? super V> comparator)
		{
			return ConstHashTable.this.chain.valuesMaxIndex(comparator);
		}

		@Override
		public final long minIndex(final Comparator<? super V> comparator)
		{
			return ConstHashTable.this.chain.valuesMinIndex(comparator);
		}

		@Override
		public final long scan(final Predicate<? super V> predicate)
		{
			return ConstHashTable.this.chain.valuesScan(predicate);
		}

	}



	public final class OldVarMap implements XImmutableTable.Bridge<K, V>
	{

		@Override
		public final void clear()
		{
			throw new UnsupportedOperationException();
		}

		@SuppressWarnings("unchecked")
		@Override
		public final boolean containsKey(final Object key)
		{
			try
			{
				return ConstHashTable.this.containsKey((K)key);
			}
			catch(final Exception e)
			{
				/* how to safely detect an exception caused by an invalid type of passed object?
				 * Can't be sure to always be a ClassCastException...
				 * God damn stupid dilettantish Object type in old Map -.-
				 * As if they really found "reasonable code that affords Object" back then, nonsense.
				 */
				return false;
			}
		}

		@SuppressWarnings("unchecked")
		@Override
		public final boolean containsValue(final Object value)
		{
			try
			{
				return ConstHashTable.this.chain.valuesContains((V)value);
			}
			catch(final Exception e)
			{
				/* how to safely detect an exception caused by an invalid type of passed object?
				 * Can't be sure to always be a ClassCastException...
				 * God damn stupid dilettantish Object type in old Map -.-
				 * As if they really found "reasonable code that affords Object" back then, nonsense.
				 */
				return false;
			}
		}

		@SuppressWarnings("unchecked")
		@Override
		public final Set<java.util.Map.Entry<K, V>> entrySet()
		{
			/* (20.05.2011 TM)NOTE:
			 * Okay this is nasty:
			 * Entry implements KeyValue and java.util.Map.Entry
			 * XCollection-architecture wise, the "old" collections cleanly use KeyValue instead of Entry.
			 * But java.util.Set<KeyValue<K, V>> cannot be cast to Set<java.util.Map.Entry<K, V>>, generics-wise.
			 * Nevertheless, the "stuff behind" the typing IS compatible.
			 * So this typingly dirty but architectural clean workaround is used.
			 */
			return (Set<java.util.Map.Entry<K, V>>)(Set<?>)ConstHashTable.this.old();
		}

		@SuppressWarnings("unchecked")
		@Override
		public final V get(final Object key)
		{
			try
			{
				return ConstHashTable.this.get((K)key);
			}
			catch(final Exception e)
			{
				/* how to safely detect an exception caused by an invalid type of passed object?
				 * Can't be sure to always be a ClassCastException...
				 * God damn stupid dilettantish Object type in old Map -.-
				 * As if they really found "reasonable code that affords Object" back then, nonsense.
				 */
				return null;
			}
		}

		@Override
		public final boolean isEmpty()
		{
			return ConstHashTable.this.isEmpty();
		}

		@Override
		public final Set<K> keySet()
		{
			throw new one.microstream.meta.NotImplementedYetError(); // FIXME ConstHashTable.OldVarMap#keySet()
		}

		@Override
		public final V put(final K key, final V value)
		{
			throw new UnsupportedOperationException();
		}

		@Override
		public final void putAll(final Map<? extends K, ? extends V> m)
		{
			throw new UnsupportedOperationException();
		}

		@Override
		public final V remove(final Object key)
		{
			throw new UnsupportedOperationException();
		}

		@Override
		public final int size()
		{
			return XTypes.to_int(ConstHashTable.this.size());
		}

		@Override
		public final Collection<V> values()
		{
			return ConstHashTable.this.values.old(); // hehehe
		}

		@Override
		public final ConstHashTable<K, V> parent()
		{
			return ConstHashTable.this;
		}

	}

}

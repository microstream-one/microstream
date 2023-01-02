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
import one.microstream.functional.IndexedAcceptor;
import one.microstream.hashing.HashEqualator;
import one.microstream.hashing.XHashing;
import one.microstream.typing.Composition;
import one.microstream.typing.KeyValue;
import one.microstream.typing.XTypes;


public final class EqConstHashTable<K, V>
extends AbstractChainKeyValueCollection<K, V, ChainMapEntryLinkedHashedStrongStrong<K, V>>
implements XImmutableTable<K, V>, HashCollection<K>, Composition
{
	public interface Creator<K, V>
	{
		public EqConstHashTable<K, V> newInstance();
	}



	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////

	public static final <K, V> EqConstHashTable<K, V> New()
	{
		return new EqConstHashTable<>(
			DEFAULT_HASH_LENGTH,
			DEFAULT_HASH_FACTOR,
			XHashing.hashEqualityValue()
		);
	}

	public static final <K, V> EqConstHashTable<K, V> NewCustom(final int initialHashLength)
	{
		return new EqConstHashTable<>(
			XHashing.padHashLength(initialHashLength),
			DEFAULT_HASH_FACTOR,
			XHashing.hashEqualityValue()
		);
	}

	public static final <K, V> EqConstHashTable<K, V> NewCustom(final float hashDensity)
	{
		return new EqConstHashTable<>(
			DEFAULT_HASH_LENGTH,
			XHashing.validateHashDensity(hashDensity),
			XHashing.hashEqualityValue()
		);
	}

	public static final <K, V> EqConstHashTable<K, V> NewCustom(
		final int              initialHashLength,
		final float            hashDensity
	)
	{
		return new EqConstHashTable<>(
			XHashing.padHashLength(initialHashLength),
			XHashing.validateHashDensity(hashDensity),
			XHashing.hashEqualityValue()
		);
	}
	
	public static final <K, V> EqConstHashTable<K, V> New(
		final XGettingCollection<? extends KeyValue<? extends K, ? extends V>> entries
	)
	{
		return new EqConstHashTable<K, V>(
			DEFAULT_HASH_LENGTH,
			DEFAULT_HASH_FACTOR,
			XHashing.hashEqualityValue()
		).internalAddEntries(entries);
	}
	
	public static final <V0, K, V> EqConstHashTable<K, V> New(
		final XGettingCollection<? extends KeyValue<? extends K, ? extends V0>> entries,
		final Function<? super V0, V>                                           mapper
	)
	{
		return new EqConstHashTable<K, V>(
			DEFAULT_HASH_LENGTH,
			DEFAULT_HASH_FACTOR,
			XHashing.hashEqualityValue()
		).internalAddEntries(entries, mapper);
	}

	public static final <K, V> EqConstHashTable<K, V> NewCustom(
		final int              initialHashLength,
		final float            hashDensity      ,
		final XGettingCollection<? extends KeyValue<? extends K, ? extends V>> entries
	)
	{
		return new EqConstHashTable<K, V>(
			XHashing.padHashLength(initialHashLength),
			XHashing.validateHashDensity(hashDensity),
			XHashing.hashEqualityValue()
		).internalAddEntries(entries);
	}

	public static final <K, V> EqConstHashTable<K, V> NewSingle(final K key, final V value)
	{
		final EqConstHashTable<K, V> instance = New();
		instance.internalAdd(key, value);
		return instance;
	}

	@SafeVarargs
	public static final <K, V> EqConstHashTable<K, V> New(final KeyValue<? extends K, ? extends V>... entries)
	{
		return new EqConstHashTable<K, V>(
			DEFAULT_HASH_LENGTH,
			DEFAULT_HASH_FACTOR,
			XHashing.hashEqualityValue()
		).internalAddEntries(new ArrayView<>(entries));
	}

	@SafeVarargs
	public static final <K, V> EqConstHashTable<K, V> NewCustom(
		final int                                   initialHashLength,
		final float                                 hashDensity      ,
		final KeyValue<? extends K, ? extends V>... entries
	)
	{
		return new EqConstHashTable<K, V>(
			XHashing.padHashLength(initialHashLength),
			XHashing.validateHashDensity(hashDensity),
			XHashing.hashEqualityValue()
		).internalAddEntries(new ArrayView<>(entries));
	}

	public static final <K, V> EqConstHashTable<K, V> New(final HashEqualator<? super K> hashEqualator)
	{
		return new EqConstHashTable<>(
			DEFAULT_HASH_LENGTH,
			DEFAULT_HASH_FACTOR,
			notNull(hashEqualator)
		);
	}

	public static final <K, V> EqConstHashTable<K, V> NewCustom(
		final HashEqualator<? super K> hashEqualator    ,
		final int                      initialHashLength
	)
	{
		return new EqConstHashTable<>(
			XHashing.padHashLength(initialHashLength),
			DEFAULT_HASH_FACTOR,
			notNull(hashEqualator)
		);
	}

	public static final <K, V> EqConstHashTable<K, V> NewCustom(
		final HashEqualator<? super K> hashEqualator,
		final float                    hashDensity
	)
	{
		return new EqConstHashTable<>(
			DEFAULT_HASH_LENGTH,
			XHashing.validateHashDensity(hashDensity),
			notNull(hashEqualator)
		);
	}

	public static final <K, V> EqConstHashTable<K, V> NewCustom(
		final HashEqualator<? super K> hashEqualator    ,
		final int              initialHashLength,
		final float            hashDensity
	)
	{
		return new EqConstHashTable<>(
			XHashing.padHashLength(initialHashLength),
			XHashing.validateHashDensity(hashDensity),
			notNull(hashEqualator)
		);
	}
	public static final <K, V> EqConstHashTable<K, V> New(
		final HashEqualator<? super K>                                         hashEqualator,
		final XGettingCollection<? extends KeyValue<? extends K, ? extends V>> entries
	)
	{
		return new EqConstHashTable<K, V>(
			DEFAULT_HASH_LENGTH,
			DEFAULT_HASH_FACTOR,
			notNull(hashEqualator)
		).internalAddEntries(entries);
	}

	public static final <K, V> EqConstHashTable<K, V> NewCustom(
		final HashEqualator<? super K>                                         hashEqualator    ,
		final int                                                              initialHashLength,
		final float                                                            hashDensity      ,
		final XGettingCollection<? extends KeyValue<? extends K, ? extends V>> entries
	)
	{
		return new EqConstHashTable<K, V>(
			XHashing.padHashLength(initialHashLength),
			XHashing.validateHashDensity(hashDensity),
			notNull(hashEqualator)
		).internalAddEntries(entries);
	}

	@SafeVarargs
	public static final <K, V> EqConstHashTable<K, V> New(
		final HashEqualator<? super K>              hashEqualator,
		final KeyValue<? extends K, ? extends V>... entries
	)
	{
		return new EqConstHashTable<K, V>(
			DEFAULT_HASH_LENGTH,
			DEFAULT_HASH_FACTOR,
			notNull(hashEqualator)
		).internalAddEntries(new ArrayView<>(entries));
	}

	@SafeVarargs
	public static final <K, V> EqConstHashTable<K, V> NewCustom(
		final HashEqualator<? super K>              hashEqualator    ,
		final int                                   initialHashLength,
		final float                                 hashDensity      ,
		final KeyValue<? extends K, ? extends V>... entries
	)
	{
		return new EqConstHashTable<K, V>(
			XHashing.padHashLength(initialHashLength),
			XHashing.validateHashDensity(hashDensity),
			notNull(hashEqualator)
		).internalAddEntries(new ArrayView<>(entries));
	}

	public static final <K, V, K1, V1> EqConstHashTable<K1, V1> NewProjected(
		final float                                       hashDensity  ,
		final XGettingCollection<? extends KeyValue<K, V>> entries      ,
		final Function<? super K, K1>                     keyProjector ,
		final Function<? super V, V1>                     valueProjector
	)
	{
		final EqConstHashTable<K1, V1> newMap = new EqConstHashTable<>(
			DEFAULT_HASH_LENGTH,
			XHashing.validateHashDensity(hashDensity),
			XHashing.<K1>hashEqualityValue()
		);
		entries.iterate(new Consumer<KeyValue<K, V>>()
		{
			@Override
			public void accept(final KeyValue<K, V> e)
			{
				newMap.internalAdd(keyProjector.apply(e.key()), valueProjector.apply(e.value()));
			}
		});
		return newMap;
	}

	public static final <K, V, K1, V1> EqConstHashTable<K1, V1> NewProjected(
		final XGettingCollection<? extends KeyValue<K, V>> entries       ,
		final Function<? super K, K1>                     keyProjector  ,
		final Function<? super V, V1>                     valueProjector
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
	final AbstractChainKeyValueStorage<K, V, ChainMapEntryLinkedHashedStrongStrong<K, V>> chain;
	      ChainMapEntryLinkedHashedStrongStrong<K, V>[]                                   slots;

	// hashing
	final HashEqualator<? super K> hashEqualator;
	      float                    hashDensity  ;

	// cached values
	int capacity, range, size;

	// satellite instances
	final Values values = new Values();
	final Keys   keys   = new Keys()  ;



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	private EqConstHashTable(final EqConstHashTable<K, V> original)
	{
		super();
		this.hashDensity   = original.hashDensity;
		this.hashEqualator = original.hashEqualator;
		this.range         = original.range;

		// constructor only copies configuration (concern #1), not data (#2). See copy() for copying data.
		this.slots         = ChainMapEntryLinkedHashedStrongStrong.array(original.slots.length);
		this.chain         = new ChainStrongStrongStorage<>(this, new ChainMapEntryLinkedHashedStrongStrong<K, V>(-1, null, null, null));
		this.capacity      = original.capacity;
	}

	private EqConstHashTable(
		final int              pow2InitialHashLength,
		final float            positiveHashDensity  ,
		final HashEqualator<? super K> hashEqualator
	)
	{
		super();
		this.hashDensity   = positiveHashDensity;
		this.hashEqualator = hashEqualator;
		this.range         = pow2InitialHashLength - 1;

		this.slots         = ChainMapEntryLinkedHashedStrongStrong.array(pow2InitialHashLength);
		this.chain         = new ChainStrongStrongStorage<>(this, new ChainMapEntryLinkedHashedStrongStrong<K, V>(-1, null, null, null));
		this.capacity      = (int)(pow2InitialHashLength * positiveHashDensity); // capped at MAX_VALUE
	}



	///////////////////////////////////////////////////////////////////////////
	// declared methods //
	/////////////////////

	private ChainMapEntryLinkedHashedStrongStrong<K, V> createNewEntry(final int hash, final K key, final V value)
	{
		if(this.size >= this.capacity)
		{
			ensureFreeArrayCapacity(this.size); // size limit only needs to be checked if size reached capacity
			this.increaseStorage();
		}

		ChainMapEntryLinkedHashedStrongStrong<K, V> e;
		this.slots[hash & this.range] = e = new ChainMapEntryLinkedHashedStrongStrong<>(hash, key, value, this.slots[hash & this.range]);
		this.size++;
		return e;
	}

	private void increaseStorage()
	{
		this.rebuildStorage((int)(this.slots.length * 2.0f));
	}

	private void rebuildStorage(final int newSlotLength)
	{
		final ChainMapEntryLinkedHashedStrongStrong<K, V>[] newSlots =  ChainMapEntryLinkedHashedStrongStrong.array(newSlotLength);
		final int modulo = newSlotLength >= Integer.MAX_VALUE ? Integer.MAX_VALUE : newSlotLength - 1;

		// iterate through all entries and assign them to the new storage
		for(ChainMapEntryLinkedHashedStrongStrong<K, V> entry = this.chain.head(); (entry = entry.next) != null;)
		{
			entry.link = newSlots[entry.hash & modulo];
			newSlots[entry.hash & modulo] = entry;
		}

		this.capacity = newSlotLength >= Integer.MAX_VALUE ? Integer.MAX_VALUE : (int)(newSlotLength * this.hashDensity);
		this.slots = newSlots;
		this.range = modulo;
	}
	
	@Override
	public final KeyValue<K, V> lookup(final K key)
	{
		final int hash; // search for key by hash
		for(ChainMapEntryLinkedHashedStrongStrong<K, V> e = this.slots[(hash = this.hashEqualator.hash(key)) & this.range]; e != null; e = e.link)
		{
			if(e.hash == hash && this.hashEqualator.equal(e.key(), key))
			{
				return e;
			}
		}
		return null;
	}

	final boolean containsKey(final K key)
	{
		final int hash; // search for element by hash
		for(ChainMapEntryLinkedHashedStrongStrong<K, V> e = this.slots[(hash = this.hashEqualator.hash(key)) & this.range]; e != null; e = e.link)
		{
			if(e.hash == hash && this.hashEqualator.equal(e.key(), key))
			{
				return true;
			}
		}
		return false;
	}

	final void internalAdd(final K key, final V value)
	{
		final int hash;
		for(ChainMapEntryLinkedHashedStrongStrong<K, V> e = this.slots[(hash = this.hashEqualator.hash(key)) & this.range]; e != null; e = e.link)
		{
			if(e.hash == hash && this.hashEqualator.equal(e.key(), key))
			{
				return;
			}
		}
		this.chain.appendEntry(this.createNewEntry(hash, key, value));
	}

	final EqConstHashTable<K, V> internalAddEntries(
		final XGettingCollection<? extends KeyValue<? extends K, ? extends V>> entries
	)
	{
		entries.iterate(kv ->
			this.internalAdd(kv.key(), kv.value())
		);

		return this;
	}
	
	final <V0> EqConstHashTable<K, V> internalAddEntries(
		final XGettingCollection<? extends KeyValue<? extends K, ? extends V0>> entries,
		final Function<? super V0, V>                                           mapper
		
	)
	{
		entries.iterate(kv ->
			this.internalAdd(kv.key(), mapper.apply(kv.value()))
		);

		return this;
	}

	final void internalCollectUnhashed(final K key, final V value)
	{
		this.chain.appendEntry(new ChainMapEntryLinkedHashedStrongStrong<>(0, key, value, null));
	}

	final int internalRehash()
	{
		// local helper variables, including capacity recalculation while at rebuilding anyway
		final int reqCapacity = XHashing.padHashLength((int)(this.size / this.hashDensity));
		
		final ChainMapEntryLinkedHashedStrongStrong<K, V>[] slots =
			ChainMapEntryLinkedHashedStrongStrong.<K, V>array(reqCapacity)
		;
		final int range = reqCapacity >= Integer.MAX_VALUE ? Integer.MAX_VALUE : reqCapacity - 1;
		final HashEqualator<? super K> hashEqualator = this.hashEqualator;
		final AbstractChainKeyValueStorage<K, V, ChainMapEntryLinkedHashedStrongStrong<K, V>> chain = this.chain;

		// keep the old chain head for old entries iteration and clear the chain for the new entries
		ChainMapEntryLinkedHashedStrongStrong<K, V> entry = chain.head().next;
		chain.clear();

		int size = 0;
		oldEntries:
		for(/*entry must be outside, see comment*/; entry != null; entry = entry.next)
		{
			final int hash = hashEqualator.hash(entry.key);

			// check for rehash collisions
			for(ChainMapEntryLinkedHashedStrongStrong<K, V> e = slots[hash & range]; e != null; e = e.link)
			{
				if(e.hash == hash && hashEqualator.equal(e.key, entry.key))
				{
					continue oldEntries; // hash collision: key already contained, discard old entry
				}
			}

			// register new entry for unique element
			chain.appendEntry(slots[hash & range] =
				new ChainMapEntryLinkedHashedStrongStrong<>(hash, entry.key, entry.value, slots[hash & range]))
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
	protected void internalRemoveEntry(final ChainMapEntryLinkedHashedStrongStrong<K, V> entry)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	protected int internalClear()
	{
		throw new UnsupportedOperationException();
	}

	@Override
	protected AbstractChainKeyValueStorage<K, V, ChainMapEntryLinkedHashedStrongStrong<K, V>> getInternalStorageChain()
	{
		return this.chain;
	}

	@Override
	public final long size()
	{
		return EqConstHashTable.this.size;
	}

	@Override
	public final boolean isEmpty()
	{
		return this.size == 0;
	}

	@Override
	public final EqConstHashTable<K, V> copy()
	{
		final EqConstHashTable<K, V> newVarMap = new EqConstHashTable<>(this);
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
	public final EqConstHashTable<K, V> immure()
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
		final int hash; // search for key by hash
		for(ChainMapEntryLinkedHashedStrongStrong<K, V> e = this.slots[(hash = this.hashEqualator.hash(key)) & this.range]; e != null; e = e.link)
		{
			if(e.hash == hash && this.hashEqualator.equal(e.key(), key))
			{
				return e.value();
			}
		}
		return null;
	}

	@Override
	public final EqConstHashTable<K, V>.Keys keys()
	{
		return this.keys;
	}

	@Override
	public final XImmutableTable.EntriesBridge<K, V> old()
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME EqConstHashTable#old()
	}

	@Override
	public XImmutableTable.Bridge<K, V> oldMap()
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
				collector.accept(EqConstHashTable.this.get(key));
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
	public final HashCollection.Analysis<EqConstHashTable<K, V>> analyze()
	{
		return AbstractChainEntryLinked.analyzeSlots(this, this.slots);
	}

	@Override
	public final int hashDistributionRange()
	{
		return this.slots.length;
	}

	@Override
	public final HashEqualator<? super K> hashEquality()
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
		return this.chain.appendTo(VarString.New(this.slots.length).append('{'), ",").append('}').toString();
	}

	public final Predicate<K> predicateContainsKey()
	{
		return key ->
		{
			return this.containsKey(key);
		};
	}

	public final Predicate<KeyValue<K, V>> predicateContainsEntry()
	{
		return entry ->
		{
			final KeyValue<K, V> kv;
			if((kv = EqConstHashTable.this.lookup(entry.key())) == null)
			{
				return false;
			}

			// equality of values is architectural restricted to simple referential equality
			return this.hashEqualator.equal(kv.key(), entry.key()) && kv.value() == entry.value();
		};
	}



	///////////////////////////////////////////////////////////////////////////
	// getting methods //
	////////////////////

	@Override
	public final XEnum<KeyValue<K, V>> range(final long lowIndex, final long highIndex)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME EqConstHashTable.Entries#range()
	}

	@Override
	public final XGettingEnum<KeyValue<K, V>> view(final long lowIndex, final long highIndex)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME EqConstHashTable.Entries#view()
	}

	@Override
	public final KeyValue<K, V>[] toArray(final Class<KeyValue<K, V>> type)
	{
		return EqConstHashTable.this.chain.toArray(type);
	}

	// executing //

	@Override
	public final <P extends Consumer<? super KeyValue<K, V>>> P iterate(final P procedure)
	{
		EqConstHashTable.this.chain.iterate(procedure);
		return procedure;
	}

	@Override
	public final <A> A join(final BiConsumer<? super KeyValue<K, V>, ? super A> joiner, final A aggregate)
	{
		EqConstHashTable.this.chain.join(joiner, aggregate);
		return aggregate;
	}

	@Override
	public final long count(final KeyValue<K, V> entry)
	{
		return EqConstHashTable.this.chain.count(entry, this.equality());
	}

	@Override
	public final long countBy(final Predicate<? super KeyValue<K, V>> predicate)
	{
		return EqConstHashTable.this.chain.count(predicate);
	}

	// element querying //

	@Override
	public final KeyValue<K, V> search(final Predicate<? super KeyValue<K, V>> predicate)
	{
		return EqConstHashTable.this.chain.search(predicate);
	}

	@Override
	public final KeyValue<K, V> max(final Comparator<? super KeyValue<K, V>> comparator)
	{
		return EqConstHashTable.this.chain.max(comparator);
	}

	@Override
	public final KeyValue<K, V> min(final Comparator<? super KeyValue<K, V>> comparator)
	{
		return EqConstHashTable.this.chain.min(comparator);
	}

	// boolean querying - applies //

	@Override
	public final boolean containsSearched(final Predicate<? super KeyValue<K, V>> predicate)
	{
		return EqConstHashTable.this.chain.containsSearched(predicate);
	}

	@Override
	public final boolean applies(final Predicate<? super KeyValue<K, V>> predicate)
	{
		return EqConstHashTable.this.chain.appliesAll(predicate);
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
		final int hash; // search for element by hash
		for(ChainMapEntryLinkedHashedStrongStrong<K, V> e = EqConstHashTable.this.slots[(hash = EqConstHashTable.this.hashEqualator.hash(entry.key())) & EqConstHashTable.this.range]; e != null; e = e.link)
		{
			if(hash == e.hash && entry == e.key())
			{
				return true;
			}
		}
		return false;
	}

	@Override
	public final boolean contains(final KeyValue<K, V> entry)
	{
		final int hash; // search for element by hash
		for(ChainMapEntryLinkedHashedStrongStrong<K, V> e = EqConstHashTable.this.slots[(hash = EqConstHashTable.this.hashEqualator.hash(entry.key())) & EqConstHashTable.this.range]; e != null; e = e.link)
		{
			if(e.hash == hash && EqConstHashTable.this.hashEqualator.equal(e.key(), entry.key()))
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

		final int hash; // search for element by hash
		for(ChainMapEntryLinkedHashedStrongStrong<K, V> e = EqConstHashTable.this.slots[(hash = EqConstHashTable.this.hashEqualator.hash(sample.key())) & EqConstHashTable.this.range]; e != null; e = e.link)
		{
			if(e.hash == hash && EqConstHashTable.this.hashEqualator.equal(e.key(), sample.key()))
			{
				return e;
			}
		}
		return null;
	}

	@Override
	public final boolean containsAll(final XGettingCollection<? extends KeyValue<K, V>> elements)
	{
		return elements.applies(EqConstHashTable.this.predicateContainsEntry());
	}

	// boolean querying - equality //

	@Override
	public final boolean equals(final XGettingCollection<? extends KeyValue<K, V>> samples, final Equalator<? super KeyValue<K, V>> equalator)
	{
		if(samples == null || !(samples instanceof EqConstHashTable<?, ?>.Keys))
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
		if(EqConstHashTable.this.size != XTypes.to_int(samples.size()))
		{
			return false;
		}

		// if sizes are equal and all elements of collection are contained in this set, they must have equal content
		return EqConstHashTable.this.chain.equalsContent(samples, equalator);
	}

	// data set procedures //

	@Override
	public final <C extends Consumer<? super KeyValue<K, V>>> C intersect(
		final XGettingCollection<? extends KeyValue<K, V>> other,
		final Equalator<? super KeyValue<K, V>> equalator,
		final C target
	)
	{
		return EqConstHashTable.this.chain.intersect(other, equalator, target);
	}

	@Override
	public final <C extends Consumer<? super KeyValue<K, V>>> C except(
		final XGettingCollection<? extends KeyValue<K, V>> other,
		final Equalator<? super KeyValue<K, V>> equalator,
		final C target
	)
	{
		return EqConstHashTable.this.chain.except(other, equalator, target);
	}

	@Override
	public final <C extends Consumer<? super KeyValue<K, V>>> C union(
		final XGettingCollection<? extends KeyValue<K, V>> other,
		final Equalator<? super KeyValue<K, V>> equalator,
		final C target
	)
	{
		return EqConstHashTable.this.chain.union(other, equalator, target);
	}

	@Override
	public final <C extends Consumer<? super KeyValue<K, V>>> C copyTo(final C target)
	{
		return EqConstHashTable.this.chain.copyTo(target);
	}

	@Override
	public final <C extends Consumer<? super KeyValue<K, V>>> C filterTo(final C target, final Predicate<? super KeyValue<K, V>> predicate)
	{
		return EqConstHashTable.this.chain.copyTo(target, predicate);
	}

	@Override
	public final <C extends Consumer<? super KeyValue<K, V>>> C distinct(final C target)
	{
		return this.distinct(target, this.equality());
	}

	@Override
	public final <C extends Consumer<? super KeyValue<K, V>>> C distinct(final C target, final Equalator<? super KeyValue<K, V>> equalator)
	{
		return EqConstHashTable.this.chain.distinct(target, equalator);
	}

	@Override
	public final EqConstHashTable<K, V> toReversed()
	{
		final EqConstHashTable<K, V> reversedVarSet = EqConstHashTable.this.copy();
		reversedVarSet.chain.reverse();
		return reversedVarSet;
	}

	@Override
	public final <T extends Consumer<? super KeyValue<K, V>>> T copySelection(final T target, final long... indices)
	{
		EqConstHashTable.this.chain.copySelection(target, indices);
		return target;
	}

	@Override
	public final <P extends IndexedAcceptor<? super KeyValue<K, V>>> P iterateIndexed(final P procedure)
	{
		EqConstHashTable.this.chain.iterateIndexed(procedure);
		return procedure;
	}

	@Override
	public final KeyValue<K, V> at(final long index)
	{
		return EqConstHashTable.this.chain.get(index);
	}

	@Override
	public final KeyValue<K, V> get()
	{
		return EqConstHashTable.this.chain.first();
	}

	@Override
	public final KeyValue<K, V> first()
	{
		return EqConstHashTable.this.chain.first();
	}

	@Override
	public final KeyValue<K, V> last()
	{
		return EqConstHashTable.this.chain.last();
	}

	@Override
	public final KeyValue<K, V> poll()
	{
		return EqConstHashTable.this.size == 0 ? null : EqConstHashTable.this.chain.first();
	}

	@Override
	public final KeyValue<K, V> peek()
	{
		return EqConstHashTable.this.size == 0 ? null : EqConstHashTable.this.chain.last();
	}

	@Override
	public final long indexOf(final KeyValue<K, V> entry)
	{
		return EqConstHashTable.this.chain.indexOf(entry);
	}

	@Override
	public final long indexBy(final Predicate<? super KeyValue<K, V>> predicate)
	{
		return EqConstHashTable.this.chain.indexOf(predicate);
	}

	@Override
	public final boolean isSorted(final Comparator<? super KeyValue<K, V>> comparator)
	{
		return EqConstHashTable.this.chain.isSorted(comparator);
	}

	@Override
	public final long lastIndexOf(final KeyValue<K, V> entry)
	{
		return this.chain.lastIndexBy(kv ->
			this.hashEqualator.equal(kv.key(), entry.key())
		);
	}

	@Override
	public final long lastIndexBy(final Predicate<? super KeyValue<K, V>> predicate)
	{
		return EqConstHashTable.this.chain.lastIndexBy(predicate);
	}

	@Override
	public final long maxIndex(final Comparator<? super KeyValue<K, V>> comparator)
	{
		return EqConstHashTable.this.chain.maxIndex(comparator);
	}

	@Override
	public final long minIndex(final Comparator<? super KeyValue<K, V>> comparator)
	{
		return EqConstHashTable.this.chain.minIndex(comparator);
	}

	@Override
	public final long scan(final Predicate<? super KeyValue<K, V>> predicate)
	{
		return EqConstHashTable.this.chain.scan(predicate);
	}

	@Override
	public final Iterator<KeyValue<K, V>> iterator()
	{
		return EqConstHashTable.this.chain.iterator();
	}

	@Override
	public final Object[] toArray()
	{
		return EqConstHashTable.this.chain.toArray();
	}

	@Override
	public final HashEqualator<KeyValue<K, V>> equality()
	{
		return XHashing.<K, V>wrapAsKeyValue(EqConstHashTable.this.hashEqualator);
	}



	public final class Keys implements XImmutableTable.Keys<K, V>, HashCollection<K>
	{
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		public final int hashDistributionRange()
		{
			return EqConstHashTable.this.slots.length;
		}

		@Override
		public final boolean hasVolatileHashElements()
		{
			return EqConstHashTable.this.chain.hasVolatileElements();
		}

		@Override
		public final void setHashDensity(final float hashDensity)
		{
			EqConstHashTable.this.setHashDensity(hashDensity);
		}

		@Override
		public final HashCollection.Analysis<Keys> analyze()
		{
			return AbstractChainEntryLinked.analyzeSlots(this, EqConstHashTable.this.slots);
		}

		@Override
		public final int rehash()
		{
			return EqConstHashTable.this.rehash();
		}



		///////////////////////////////////////////////////////////////////////////
		// getting methods //
		////////////////////

		@Override
		public final Equalator<? super K> equality()
		{
			return EqConstHashTable.this.hashEquality();
		}

		@Override
		public final Keys copy()
		{
			return EqConstHashTable.this.copy().keys();
		}

		/**
		 * This method creates a {@link EqConstHashEnum} instance containing all (currently existing) elements
		 * of this {@link EqConstHashTable}.<br>
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
		public final EqConstHashTable<K, V>.Keys immure()
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
			throw new one.microstream.meta.NotImplementedYetError(); // FIXME EqConstHashTable.Keys#range()
		}

		@Override
		public final XGettingEnum<K> view(final long lowIndex, final long highIndex)
		{
			throw new one.microstream.meta.NotImplementedYetError(); // FIXME EqConstHashTable.Keys#view()
		}

		@Override
		public final K[] toArray(final Class<K> type)
		{
			return EqConstHashTable.this.chain.keyToArray(type);
		}

		// executing //

		@Override
		public final <P extends Consumer<? super K>> P iterate(final P procedure)
		{
			EqConstHashTable.this.chain.keyIterate(procedure);
			return procedure;
		}

		@Override
		public final <A> A join(final BiConsumer<? super K, ? super A> joiner, final A aggregate)
		{
			EqConstHashTable.this.chain.keyJoin(joiner, aggregate);
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
			return EqConstHashTable.this.chain.keyCount(predicate);
		}

		// element querying //

		@Override
		public final K seek(final K sample)
		{
			return EqConstHashTable.this.chain.keySeek(sample, EqConstHashTable.this.hashEqualator);
		}

		@Override
		public final K search(final Predicate<? super K> predicate)
		{
			return EqConstHashTable.this.chain.keySearch(predicate);
		}

		@Override
		public final K max(final Comparator<? super K> comparator)
		{
			return EqConstHashTable.this.chain.keyMax(comparator);
		}

		@Override
		public final K min(final Comparator<? super K> comparator)
		{
			return EqConstHashTable.this.chain.keyMin(comparator);
		}

		// boolean querying //

		@Override
		public final boolean hasVolatileElements()
		{
			return EqConstHashTable.this.chain.hasVolatileElements();
		}

		@Override
		public final boolean nullAllowed()
		{
			return true;
		}

		// boolean querying - applies //

		@Override
		public final boolean containsSearched(final Predicate<? super K> predicate)
		{
			return EqConstHashTable.this.chain.keyApplies(predicate);
		}

		@Override
		public final boolean applies(final Predicate<? super K> predicate)
		{
			return EqConstHashTable.this.chain.keyAppliesAll(predicate);
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
			final int hash; // search for element by hash
			for(ChainMapEntryLinkedHashedStrongStrong<K, V> e = EqConstHashTable.this.slots[(hash = EqConstHashTable.this.hashEqualator.hash(element)) & EqConstHashTable.this.range]; e != null; e = e.link)
			{
				if(hash == e.hash && element == e.key())
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
			final int hash;
			for(ChainMapEntryLinkedHashedStrongStrong<K, V> e = EqConstHashTable.this.slots[(hash = EqConstHashTable.this.hashEqualator.hash(element)) & EqConstHashTable.this.range]; e != null; e = e.link)
			{
				if(e.hash == hash && EqConstHashTable.this.hashEqualator.equal(e.key(), element))
				{
					return true;
				}
			}
			return false;
		}

		@Override
		public final boolean containsAll(final XGettingCollection<? extends K> elements)
		{
			return elements.applies(EqConstHashTable.this.predicateContainsKey());
		}

		// boolean querying - equality //

		@Override
		public final boolean equals(final XGettingCollection<? extends K> samples, final Equalator<? super K> equalator)
		{
			if(samples == null || !(samples instanceof EqConstHashTable<?, ?>.Keys))
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
			if(EqConstHashTable.this.size != XTypes.to_int(samples.size()))
			{
				return false;
			}

			// if sizes are equal and all elements of collection are contained in this set, they must have equal content
			return EqConstHashTable.this.chain.keyEqualsContent(samples, equalator);
		}

		// data set procedures //

		@Override
		public final <C extends Consumer<? super K>> C intersect(
			final XGettingCollection<? extends K> other,
			final Equalator<? super K> equalator,
			final C target
		)
		{
			return EqConstHashTable.this.chain.keyIntersect(other, equalator, target);
		}

		@Override
		public final <C extends Consumer<? super K>> C except(
			final XGettingCollection<? extends K> other,
			final Equalator<? super K> equalator,
			final C target
		)
		{
			return EqConstHashTable.this.chain.keyExcept(other, equalator, target);
		}

		@Override
		public final <C extends Consumer<? super K>> C union(
			final XGettingCollection<? extends K> other,
			final Equalator<? super K> equalator,
			final C target
		)
		{
			return EqConstHashTable.this.chain.keyUnion(other, equalator, target);
		}

		@Override
		public final <C extends Consumer<? super K>> C copyTo(final C target)
		{
			return EqConstHashTable.this.chain.keyCopyTo(target);
		}

		@Override
		public final <C extends Consumer<? super K>> C filterTo(final C target, final Predicate<? super K> predicate)
		{
			return EqConstHashTable.this.chain.keyCopyTo(target, predicate);
		}

		@Override
		public final <C extends Consumer<? super K>> C distinct(final C target)
		{
			return this.distinct(target, EqConstHashTable.this.hashEqualator);
		}

		@Override
		public final <C extends Consumer<? super K>> C distinct(final C target, final Equalator<? super K> equalator)
		{
			if(EqConstHashTable.this.hashEqualator == equalator)
			{
				return this.copyTo(target);
			}
			return EqConstHashTable.this.chain.keyDistinct(target, equalator);
		}



		///////////////////////////////////////////////////////////////////////////
		// adding //
		///////////

		@Override
		public final long maximumCapacity()
		{
			return EqConstHashTable.this.maximumCapacity();
		}

		@Override
		public final boolean isFull()
		{
			return EqConstHashTable.this.isFull();
		}

		@Override
		public final long remainingCapacity()
		{
			return EqConstHashTable.this.remainingCapacity();
		}



		///////////////////////////////////////////////////////////////////////////
		// removing //
		/////////////

		@Override
		public final Keys toReversed()
		{
			final EqConstHashTable<K, V> reversedVarSet = EqConstHashTable.this.copy();
			reversedVarSet.chain.reverse();
			return reversedVarSet.keys;
		}

		@Override
		public final <T extends Consumer<? super K>> T copySelection(final T target, final long... indices)
		{
			EqConstHashTable.this.chain.keyCopySelection(target, indices);
			return target;
		}

		@Override
		public final <P extends IndexedAcceptor<? super K>> P iterateIndexed(final P procedure)
		{
			EqConstHashTable.this.chain.keyIterateIndexed(procedure);
			return procedure;
		}

		@Override
		public final K at(final long index)
		{
			return EqConstHashTable.this.chain.keyGet(index);
		}

		@Override
		public final K get()
		{
			return EqConstHashTable.this.chain.keyFirst();
		}

		@Override
		public final K first()
		{
			return EqConstHashTable.this.chain.keyFirst();
		}

		@Override
		public final K last()
		{
			return EqConstHashTable.this.chain.keyLast();
		}

		@Override
		public final K poll()
		{
			return EqConstHashTable.this.size == 0 ? null : EqConstHashTable.this.chain.keyFirst();
		}

		@Override
		public final K peek()
		{
			return EqConstHashTable.this.size == 0 ? null : EqConstHashTable.this.chain.keyLast();
		}

		@Override
		public final long indexOf(final K element)
		{
			return EqConstHashTable.this.chain.keyIndexOf(element);
		}

		@Override
		public final long indexBy(final Predicate<? super K> predicate)
		{
			return EqConstHashTable.this.chain.keyIndexBy(predicate);
		}

		@Override
		public final boolean isSorted(final Comparator<? super K> comparator)
		{
			return EqConstHashTable.this.chain.keyIsSorted(comparator);
		}

		@Override
		public final long lastIndexOf(final K element)
		{
			return EqConstHashTable.this.chain.keyLastIndexOf(element);
		}

		@Override
		public final long lastIndexBy(final Predicate<? super K> predicate)
		{
			return EqConstHashTable.this.chain.keyLastIndexBy(predicate);
		}

		@Override
		public final long maxIndex(final Comparator<? super K> comparator)
		{
			return EqConstHashTable.this.chain.keyMaxIndex(comparator);
		}

		@Override
		public final long minIndex(final Comparator<? super K> comparator)
		{
			return EqConstHashTable.this.chain.keyMinIndex(comparator);
		}

		@Override
		public final long scan(final Predicate<? super K> predicate)
		{
			return EqConstHashTable.this.chain.keyScan(predicate);
		}

		@Override
		public final boolean isEmpty()
		{
			return EqConstHashTable.this.isEmpty();
		}

		@Override
		public final Iterator<K> iterator()
		{
			return EqConstHashTable.this.chain.keyIterator();
		}

		@Override
		public final long size()
		{
			return EqConstHashTable.this.size;
		}

		@Override
		public final String toString()
		{
			if(EqConstHashTable.this.size == 0)
			{
				return "[]"; // array causes problems with escape condition otherwise
			}

			final VarString vc = VarString.New(EqConstHashTable.this.slots.length).append('[');
			EqConstHashTable.this.chain.keyAppendTo(vc, ',').append(']');
			return vc.toString();
		}

		@Override
		public final Object[] toArray()
		{
			return EqConstHashTable.this.chain.keyToArray();
		}

		@Override
		public final OldCollection<K> old()
		{
			throw new one.microstream.meta.NotImplementedYetError(); // FIXME EqConstHashTable.Keys#old()
		}

		@Override
		public final EqConstHashTable<K, V> parent()
		{
			return EqConstHashTable.this;
		}

		@Override
		public final HashEqualator<? super K> hashEquality()
		{
			return EqConstHashTable.this.hashEquality();
		}

		@Override
		public final float hashDensity()
		{
			return EqConstHashTable.this.hashDensity();
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
			EqConstHashTable.this.chain.valuesIterate(procedure);
			return procedure;
		}

		@Override
		public final <A> A join(final BiConsumer<? super V, ? super A> joiner, final A aggregate)
		{
			EqConstHashTable.this.chain.valuesJoin(joiner, aggregate);
			return aggregate;
		}

		@Override
		public final <P extends IndexedAcceptor<? super V>> P iterateIndexed(final P procedure)
		{
			EqConstHashTable.this.chain.valuesIterateIndexed(procedure);
			return procedure;
		}

		@Override
		public final Values toReversed()
		{
			final EqConstHashTable<K, V> reversedVarSet = EqConstHashTable.this.copy();
			reversedVarSet.chain.reverse();
			return reversedVarSet.values;
		}

		@Override
		public final boolean containsSearched(final Predicate<? super V> predicate)
		{
			return EqConstHashTable.this.chain.valuesApplies(predicate);
		}

		@Override
		public final boolean applies(final Predicate<? super V> predicate)
		{
			return EqConstHashTable.this.chain.valuesAppliesAll(predicate);
		}

		@Override
		public final boolean contains(final V value)
		{
			return EqConstHashTable.this.chain.valuesContains(value);
		}

		@Override
		public final boolean containsAll(final XGettingCollection<? extends V> values)
		{
			return EqConstHashTable.this.chain.valuesContainsAll(values);
		}

		@Override
		public final boolean containsId(final V value)
		{
			return EqConstHashTable.this.chain.valuesContainsId(value);
		}

		@Override
		public final <T extends Consumer<? super V>> T copyTo(final T target)
		{
			EqConstHashTable.this.chain.valuesCopyTo(target);
			return target;
		}

		@Override
		public final <T extends Consumer<? super V>> T filterTo(final T target, final Predicate<? super V> predicate)
		{
			EqConstHashTable.this.chain.valuesCopyTo(target, predicate);
			return target;
		}

		@Override
		public final long count(final V value)
		{
			return EqConstHashTable.this.chain.valuesCount(value);
		}

		@Override
		public final long countBy(final Predicate<? super V> predicate)
		{
			return EqConstHashTable.this.chain.valuesCount(predicate);
		}

		@Override
		public final <T extends Consumer<? super V>> T distinct(final T target)
		{
			EqConstHashTable.this.chain.valuesDistinct(target);
			return target;
		}

		@Override
		public final <T extends Consumer<? super V>> T distinct(final T target, final Equalator<? super V> equalator)
		{
			EqConstHashTable.this.chain.valuesDistinct(target, equalator);
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
			return EqConstHashTable.this.chain.valuesEqualsContent(samples, equalator);
		}

		@Override
		public final <T extends Consumer<? super V>> T except(final XGettingCollection<? extends V> other, final Equalator<? super V> equalator, final T target)
		{
			EqConstHashTable.this.chain.valuesExcept(other, equalator, target);
			return target;
		}

		@Override
		public final boolean hasVolatileElements()
		{
			return false;
		}

		@Override
		public final <T extends Consumer<? super V>> T intersect(final XGettingCollection<? extends V> other, final Equalator<? super V> equalator, final T target)
		{
			EqConstHashTable.this.chain.valuesIntersect(other, equalator, target);
			return target;
		}

		@Override
		public final boolean isEmpty()
		{
			return EqConstHashTable.this.isEmpty();
		}

		@Override
		public final Iterator<V> iterator()
		{
			return EqConstHashTable.this.chain.valuesIterator();
		}

		@Override
		public final V max(final Comparator<? super V> comparator)
		{
			return EqConstHashTable.this.chain.valuesMax(comparator);
		}

		@Override
		public final V min(final Comparator<? super V> comparator)
		{
			return EqConstHashTable.this.chain.valuesMin(comparator);
		}

		@Override
		public final boolean nullAllowed()
		{
			return EqConstHashTable.this.nullAllowed();
		}

		@Override
		public final boolean nullContained()
		{
			return EqConstHashTable.this.chain.valuesContains(null);
		}

		@Override
		public final OldList<V> old()
		{
			throw new one.microstream.meta.NotImplementedYetError(); // FIXME EqConstHashTable.Values#old()
		}

		@Override
		public final V seek(final V sample)
		{
			return EqConstHashTable.this.chain.valuesSeek(sample);
		}

		@Override
		public final V search(final Predicate<? super V> predicate)
		{
			return EqConstHashTable.this.chain.valuesSearch(predicate);
		}

		@Override
		public final long size()
		{
			return XTypes.to_int(EqConstHashTable.this.size());
		}

		@Override
		public final long maximumCapacity()
		{
			return XTypes.to_int(EqConstHashTable.this.size());
		}

		@Override
		public final boolean isFull()
		{
			return EqConstHashTable.this.isFull();
		}

		@Override
		public final long remainingCapacity()
		{
			return EqConstHashTable.this.remainingCapacity();
		}

		@Override
		public final String toString()
		{
			if(EqConstHashTable.this.size == 0)
			{
				return "[]"; // array causes problems with escape condition otherwise
			}

			final VarString vc = VarString.New(EqConstHashTable.this.slots.length).append('[');
			EqConstHashTable.this.chain.valuesAppendTo(vc, ',').append(']');
			return vc.toString();
		}

		@Override
		public final Object[] toArray()
		{
			return EqConstHashTable.this.chain.valuesToArray();
		}

		@Override
		public final V[] toArray(final Class<V> type)
		{
			return EqConstHashTable.this.chain.valuesToArray(type);
		}

		@Override
		public final <T extends Consumer<? super V>> T union(
			final XGettingCollection<? extends V> other,
			final Equalator<? super V> equalator,
			final T target
		)
		{
			EqConstHashTable.this.chain.valuesUnion(other, equalator, target);
			return target;
		}

		@Override
		public final EqConstHashTable<K, V> parent()
		{
			return EqConstHashTable.this;
		}

		@Override
		public final XImmutableList<V> view(final long fromIndex, final long toIndex)
		{
			throw new one.microstream.meta.NotImplementedYetError(); // FIXME EqConstHashTable.Values#view()
		}

		@Override
		public final ListIterator<V> listIterator()
		{
			return EqConstHashTable.this.chain.valuesListIterator(0);
		}

		@Override
		public final ListIterator<V> listIterator(final long index)
		{
			return EqConstHashTable.this.chain.valuesListIterator(index);
		}

		@Override
		public final XImmutableList<V> range(final long fromIndex, final long toIndex)
		{
			throw new one.microstream.meta.NotImplementedYetError(); // FIXME EqConstHashTable.Values#view()
		}

		@Override
		public final XImmutableList<V> immure()
		{
			return ConstList.New(this);
		}

		@Override
		public final XImmutableList<V> view()
		{
			throw new one.microstream.meta.NotImplementedYetError(); // FIXME EqConstHashTable.Values#view()
		}

		@Override
		public final <T extends Consumer<? super V>> T copySelection(final T target, final long... indices)
		{
			EqConstHashTable.this.chain.valuesCopySelection(target, indices);
			return target;
		}

		@Override
		public final V at(final long index)
		{
			return EqConstHashTable.this.chain.valuesGet(index);
		}

		@Override
		public final V get()
		{
			return EqConstHashTable.this.chain.valuesFirst();
		}

		@Override
		public final V first()
		{
			return EqConstHashTable.this.chain.valuesFirst();
		}

		@Override
		public final V last()
		{
			return EqConstHashTable.this.chain.valuesLast();
		}

		@Override
		public final V poll()
		{
			return EqConstHashTable.this.size == 0 ? null : EqConstHashTable.this.chain.valuesFirst();
		}

		@Override
		public final V peek()
		{
			return EqConstHashTable.this.size == 0 ? null : EqConstHashTable.this.chain.valuesLast();
		}

		@Override
		public final long indexOf(final V value)
		{
			return EqConstHashTable.this.chain.valuesIndexOf(value);
		}

		@Override
		public final long indexBy(final Predicate<? super V> predicate)
		{
			return EqConstHashTable.this.chain.valuesIndexBy(predicate);
		}

		@Override
		public final boolean isSorted(final Comparator<? super V> comparator)
		{
			return EqConstHashTable.this.chain.valuesIsSorted(comparator);
		}

		@Override
		public final long lastIndexOf(final V value)
		{
			return EqConstHashTable.this.chain.valuesLastIndexOf(value);
		}

		@Override
		public final long lastIndexBy(final Predicate<? super V> predicate)
		{
			return EqConstHashTable.this.chain.valuesLastIndexBy(predicate);
		}

		@Override
		public final long maxIndex(final Comparator<? super V> comparator)
		{
			return EqConstHashTable.this.chain.valuesMaxIndex(comparator);
		}

		@Override
		public final long minIndex(final Comparator<? super V> comparator)
		{
			return EqConstHashTable.this.chain.valuesMinIndex(comparator);
		}

		@Override
		public final long scan(final Predicate<? super V> predicate)
		{
			return EqConstHashTable.this.chain.valuesScan(predicate);
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
				return EqConstHashTable.this.containsKey((K)key);
			}
			catch(final Exception e)
			{
				/* how to safely detect an exception caused by an invalid type of passed object?
				 * Can't be sure to always be a ClassCastException...
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
				return EqConstHashTable.this.chain.valuesContains((V)value);
			}
			catch(final Exception e)
			{
				/* how to safely detect an exception caused by an invalid type of passed object?
				 * Can't be sure to always be a ClassCastException...
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
			 * So this typing is dirty but architectural clean workaround is used.
			 */
			return (Set<java.util.Map.Entry<K, V>>)(Set<?>)EqConstHashTable.this.old();
		}

		@SuppressWarnings("unchecked")
		@Override
		public final V get(final Object key)
		{
			try
			{
				return EqConstHashTable.this.get((K)key);
			}
			catch(final Exception e)
			{
				/* how to safely detect an exception caused by an invalid type of passed object?
				 * Can't be sure to always be a ClassCastException...
				 */
				return null;
			}
		}

		@Override
		public final boolean isEmpty()
		{
			return EqConstHashTable.this.isEmpty();
		}

		@Override
		public final Set<K> keySet()
		{
			throw new one.microstream.meta.NotImplementedYetError(); // FIXME EqConstHashTable.OldVarMap#keySet()
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
			return XTypes.to_int(EqConstHashTable.this.size());
		}

		@Override
		public final Collection<V> values()
		{
			return EqConstHashTable.this.values.old(); // hehehe
		}

		@Override
		public final EqConstHashTable<K, V> parent()
		{
			return EqConstHashTable.this;
		}

	}

}

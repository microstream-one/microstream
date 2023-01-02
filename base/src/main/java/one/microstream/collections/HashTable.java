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

import one.microstream.X;
import one.microstream.chars.VarString;
import one.microstream.collections.interfaces.CapacityExtendable;
import one.microstream.collections.interfaces.HashCollection;
import one.microstream.collections.old.AbstractBridgeXSet;
import one.microstream.collections.old.AbstractOldSettingList;
import one.microstream.collections.types.XEnum;
import one.microstream.collections.types.XGettingCollection;
import one.microstream.collections.types.XGettingEnum;
import one.microstream.collections.types.XGettingMap;
import one.microstream.collections.types.XGettingSequence;
import one.microstream.collections.types.XGettingTable;
import one.microstream.collections.types.XImmutableList;
import one.microstream.collections.types.XIterable;
import one.microstream.collections.types.XList;
import one.microstream.collections.types.XProcessingCollection;
import one.microstream.collections.types.XTable;
import one.microstream.equality.Equalator;
import one.microstream.equality.IdentityEqualator;
import one.microstream.equality.IdentityEqualityLogic;
import one.microstream.exceptions.ArrayCapacityException;
import one.microstream.functional.IndexedAcceptor;
import one.microstream.functional.XFunc;
import one.microstream.hashing.HashEqualator;
import one.microstream.hashing.XHashing;
import one.microstream.math.XMath;
import one.microstream.typing.Composition;
import one.microstream.typing.KeyValue;
import one.microstream.typing.XTypes;

/**
 * Collection of key-value-pairs that is ordered and does not allow duplicate keys.
 * Aims to be more efficient, logically structured
 * and with more built-in features than {@link java.util.Map}.
 * <p>
 * In contrast to {@link EqHashTable} this implementation uses the default isSame-Equalator({@link Equalator#identity()}
 * and the Java hashCode implementation {@link System#identityHashCode(Object)}.
 * <p>
 * This implementation is <b>not</b> synchronized and thus should only be used by a
 * single thread or in a thread-safe manner (i.e. read-only as soon as multiple threads access it).<br>
 * See {@link SynchSet} wrapper class to use a list in a synchronized manner.
 * <p>
 * Also note that by being an extended collection, this implementation offers various functional and batch procedures
 * to maximize internal iteration potential, eliminating the need to use the external iteration
 * {@link Iterator} paradigm.
 * 
 * @param <K> type of contained keys
 * @param <V> type of contained values
 */
public final class HashTable<K, V>
extends AbstractChainKeyValueCollection<K, V, ChainMapEntryLinkedStrongStrong<K, V>>
implements XTable<K, V>, HashCollection<K>, Composition, IdentityEqualityLogic
{
	public interface Creator<K, V>
	{
		public HashTable<K, V> newInstance();
	}



	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////

	public static final <K, V> HashTable<K, V> New()
	{
		return new HashTable<>(
			DEFAULT_HASH_LENGTH,
			DEFAULT_HASH_FACTOR
		);
	}

	public static final <K, V> HashTable<K, V> NewCustom(
		final int              initialHashLength
	)
	{
		return new HashTable<>(
			XHashing.padHashLength(initialHashLength),
			DEFAULT_HASH_FACTOR
		);
	}

	public static final <K, V> HashTable<K, V> NewCustom(
		final float            hashDensity
	)
	{
		return new HashTable<>(
			DEFAULT_HASH_LENGTH,
			XHashing.validateHashDensity(hashDensity)
		);
	}

	public static final <K, V> HashTable<K, V> NewCustom(
		final int              initialHashLength,
		final float            hashDensity
	)
	{
		return new HashTable<>(
			XHashing.padHashLength(initialHashLength),
			XHashing.validateHashDensity(hashDensity)
		);
	}
	public static final <K, V> HashTable<K, V> New(
		final XGettingCollection<? extends KeyValue<? extends K, ? extends V>> entries
	)
	{
		return HashTable.<K,V>New()
			.internalAddEntries(entries)
		;
	}

	public static final <K, V> HashTable<K, V> NewCustom(
		final int              initialHashLength,
		final float            hashDensity      ,
		final XGettingCollection<? extends KeyValue<? extends K, ? extends V>> entries
	)
	{
		return new HashTable<K, V>(
			XHashing.padHashLength(initialHashLength),
			XHashing.validateHashDensity(hashDensity)
		).internalAddEntries(entries);
	}

	public static final <K, V> HashTable<K, V> NewSingle(final K key, final V value)
	{
		final HashTable<K, V> instance = New();
		instance.internalAdd(key, value);
		return instance;
	}

	@SafeVarargs
	public static final <K, V> HashTable<K, V> New(
		final KeyValue<? extends K, ? extends V>... entries
	)
	{
		return HashTable.<K,V>New()
			.internalAddEntries(new ArrayView<>(entries))
		;
	}

	@SafeVarargs
	public static final <K, V> HashTable<K, V> NewCustom(
		final long                                  desiredCapacity,
		final float                                 hashDensity    ,
		final KeyValue<? extends K, ? extends V>... entries
	)
	{
		return new HashTable<K, V>(
			XHashing.calculateHashLength(desiredCapacity, hashDensity),
			XHashing.validateHashDensity(hashDensity)
		).internalAddEntries(new ArrayView<>(entries));
	}

	public static final <K, VK, VV> Function<K, HashTable<VK, VV>> supplier()
	{
		return new Function<K, HashTable<VK, VV>>()
		{
			@Override
			public final HashTable<VK, VV> apply(final K key)
			{
				return HashTable.New();
			}

		};
	}

	public static final <KI, VI, KO, VO> HashTable<KO, VO> NewProjected(
		final float                                         hashDensity  ,
		final XGettingCollection<? extends KeyValue<KI, VI>> entries      ,
		final Function<? super KI, KO>                      keyProjector ,
		final Function<? super VI, VO>                      valueProjector
	)
	{
		final HashTable<KO, VO> newMap = new HashTable<>(
			DEFAULT_HASH_LENGTH,
			XHashing.validateHashDensity(hashDensity)
		);
		entries.iterate(e->
		{
			newMap.internalAdd(keyProjector.apply(e.key()), valueProjector.apply(e.value()));
		});
		return newMap;
	}

	public static final <KO, VO, KI extends KO, VI extends VO> HashTable<KO, VO> NewProjected(
		final XGettingCollection<? extends KeyValue<KI, VI>> entries
	)
	{
		return NewProjected(entries, XFunc.<KO>passThrough(), XFunc.<VO>passThrough());
	}

	public static final <KI, VI, KO, VO> HashTable<KO, VO> NewProjected(
		final XGettingCollection<? extends KeyValue<KI, VI>> entries       ,
		final Function<? super KI, KO>                       keyProjector  ,
		final Function<? super VI, VO>                       valueProjector
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
	final Keys   keys   = new Keys()  ;
	final Values values = new Values();



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	private HashTable(final HashTable<K, V> original)
	{
		super();
		this.hashDensity   = original.hashDensity;
		this.range         = original.range;

		// constructor only copies configuration (concern #1), not data (#2). See copy() for copying data.
		this.slots         = ChainMapEntryLinkedStrongStrong.array(original.slots.length);
		this.chain         = new ChainStrongStrongStorage<>(this, new ChainMapEntryLinkedStrongStrong<K, V>(null, null, null));
		this.capacity      = original.capacity;
	}

	private HashTable(
		final int   pow2InitialCapacity,
		final float positiveHashDensity
	)
	{
		super();
		this.hashDensity   = positiveHashDensity;
		this.range         = pow2InitialCapacity - 1;

		this.slots         = ChainMapEntryLinkedStrongStrong.array(pow2InitialCapacity);
		this.chain         = new ChainStrongStrongStorage<>(this, new ChainMapEntryLinkedStrongStrong<K, V>(null, null, null));
		this.capacity      = (int)(pow2InitialCapacity * positiveHashDensity); // capped at MAX_VALUE
	}



	///////////////////////////////////////////////////////////////////////////
	// declared methods //
	/////////////////////

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

	final HashTable<K, V> internalAddEntries(final XGettingCollection<? extends KeyValue<? extends K, ? extends V>> entries)
	{
		entries.iterate(new Consumer<KeyValue<? extends K, ? extends V>>()
		{
			@Override
			public void accept(final KeyValue<? extends K, ? extends V> e)
			{
				HashTable.this.internalAdd(e.key(), e.value());
			}
		});
		return this;
	}

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
		final ChainMapEntryLinkedStrongStrong<K, V>[] newSlots = ChainMapEntryLinkedStrongStrong.array(newSlotLength);
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

	final boolean internalAddOnlyKey(final K key)
	{
		for(ChainMapEntryLinkedStrongStrong<K, V> e = this.slots[System.identityHashCode(key) & this.range]; e != null; e = e.link)
		{
			if(e.key() == key)
			{
				return false; // already contained
			}
		}
		this.chain.appendEntry(this.createNewEntry(key, null));
		return true;
	}

	final boolean internalPutOnlyKey(final K key)
	{
		for(ChainMapEntryLinkedStrongStrong<K, V> e = this.slots[System.identityHashCode(key) & this.range]; e != null; e = e.link)
		{
			if(e.key() == key)
			{
				e.setKey(key); // intentionally no moving to end here to cleanly separate concerns
				return false;
			}
		}
		this.chain.appendEntry(this.createNewEntry(key, null));
		return true;
	}

	final K internalPutGetKey(final K key)
	{
		for(ChainMapEntryLinkedStrongStrong<K, V> e = this.slots[System.identityHashCode(key) & this.range]; e != null; e = e.link)
		{
			if(e.key() == key)
			{
				return e.setKey(key);
			}
		}
		this.chain.appendEntry(this.createNewEntry(key, null));
		return null;
	}

	final K internalAddGetKey(final K key)
	{
		for(ChainMapEntryLinkedStrongStrong<K, V> e = this.slots[System.identityHashCode(key) & this.range]; e != null; e = e.link)
		{
			if(e.key() == key)
			{
				return key;
			}
		}
		this.chain.appendEntry(this.createNewEntry(key, null));
		
		return null;
	}

	final K internalReplaceKey(final K key)
	{
		for(ChainMapEntryLinkedStrongStrong<K, V> e = this.slots[System.identityHashCode(key) & this.range]; e != null; e = e.link)
		{
			if(e.key() == key)
			{
				// no need to replace a reference to the same instance.
				return key;
			}
		}

		return null;
	}
	
	final K internalSubstituteKey(final K key)
	{
		for(ChainMapEntryLinkedStrongStrong<K, V> e = this.slots[System.identityHashCode(key) & this.range]; e != null; e = e.link)
		{
			if(e.key() == key)
			{
				// no need to replace a reference to the same instance.
				return key;
			}
		}
		this.chain.appendEntry(this.createNewEntry(key, null));

		return key;
	}

	// only used for backwards compatibility with old collections
	final V oldPutGet(final K key, final V value)
	{
		for(ChainMapEntryLinkedStrongStrong<K, V> e = this.slots[System.identityHashCode(key) & this.range]; e != null; e = e.link)
		{
			if(e.key() == key)
			{
				// set only value, not key, according to inconsistent nonsense behavior in old collections
				return e.setValue(value);
			}
		}
		this.chain.appendEntry(this.createNewEntry(key, value));
		return null;
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

	final int removeKey(final K key)
	{
		final int hash = System.identityHashCode(key);
		ChainMapEntryLinkedStrongStrong<K, V> last, e;
		if((e = this.slots[hash & this.range]) == null)
		{
			return 0;
		}

		// head entry special case
		if(e.key() == key)
		{
			this.slots[hash & this.range] = e.link;
			this.chain.disjoinEntry(e);
			this.size--;
			return 1; // return as key can only be contained once in a set
		}

		// search entry chain
		for(e = (last = e).link; e != null; e = (last = e).link)
		{
			if(e.key() == key)
			{
				last.link = e.link;
				this.chain.disjoinEntry(e);
				this.size--;
				// no further actions necessary (like removing the key etc) as entry will get garbage collected
				return 1; // return as key can only be contained once in a set
			}
		}

		return 0;
	}

	final void removeNullEntry()
	{
		this.removeFor((K)null);
	}

	boolean nullKeyPut()
	{
		return this.internalPutOnlyKey(null);
	}

	boolean nullKeyAdd()
	{
		return this.internalAddOnlyKey(null);
	}



	///////////////////////////////////////////////////////////////////////////
	// inherited ExtendedCollection methods //
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
		final int bound = offset + length;
		int count = 0;
		for(int i = offset; i < bound; i++)
		{
			if(this.add(elements[i]))
			{
				count++;
			}
		}
		return count;
	}

	@Override
	protected int internalCountingAddAll(final XGettingCollection<? extends KeyValue<K, V>> elements)
		throws UnsupportedOperationException
	{
		return elements.iterate(new Consumer<KeyValue<K, V>>()
		{
			int count;
			@Override
			public void accept(final KeyValue<K, V> e)
			{
				if(HashTable.this.add(e))
				{
					this.count++;
				}
			}
		}).count;
	}

	@Override
	protected int internalCountingPutAll(final KeyValue<K, V>[] elements) throws UnsupportedOperationException
	{
		return this.internalCountingAddAll(elements, 0, elements.length);
	}

	@Override
	protected int internalCountingPutAll(final KeyValue<K, V>[] elements, final int offset, final int length)
		throws UnsupportedOperationException
	{
		final int bound = offset + length;
		int count = 0;
		for(int i = offset; i < bound; i++)
		{
			if(this.put(elements[i]))
			{
				count++;
			}
		}
		return count;
	}

	@Override
	protected int internalCountingPutAll(final XGettingCollection<? extends KeyValue<K, V>> elements)
		throws UnsupportedOperationException
	{
		return elements.iterate(new Consumer<KeyValue<K, V>>()
		{
			int count;
			@Override
			public void accept(final KeyValue<K, V> e)
			{
				if(HashTable.this.put(e))
				{
					this.count++;
				}
			}
		}).count;
	}

	@Override
	protected int internalRemoveNullEntries()
	{
		return this.removeKey(null);
	}

	@Override
	protected void internalRemoveEntry(final ChainMapEntryLinkedStrongStrong<K, V> entry)
	{
		final ChainMapEntryLinkedStrongStrong<K, V> setEntry = entry;
		ChainMapEntryLinkedStrongStrong<K, V> last, e = this.slots[System.identityHashCode(setEntry.key) & this.range];

		// remove entry from hashing chain
		if(e == setEntry)
		{
			// head entry special case
			this.slots[System.identityHashCode(setEntry.key) & this.range] = setEntry.link;
		}
		else
		{
			while((e = (last = e).link) != null)
			{
				if(e == setEntry)
				{
					last.link = setEntry.link;
					break;
				}
			}
			// consistency check (passed entry e may not be contained in the hash chain at all)
			if(e == null)
			{
				throw new IllegalArgumentException("Entry inconsistency detected");
			}
		}

		// remove entry e (unlink and disjoin)
		this.size--;
		this.chain.disjoinEntry(setEntry);
	}

	@Override
	protected int internalClear()
	{
		final int size = this.size;
		this.clear();
		return size;
	}

	@Override
	protected AbstractChainKeyValueStorage<K, V, ChainMapEntryLinkedStrongStrong<K, V>> getInternalStorageChain()
	{
		return this.chain;
	}

	@Override
	public final long size()
	{
		return HashTable.this.size;
	}

	@Override
	public final int rehash()
	{
		/* As the object header's identity hash value of any instance can never change, this method does
		 * nothing more than optimizing the storage.
		 */
		this.optimize();
		return XTypes.to_int(this.size());
	}

	@Override
	public final boolean isEmpty()
	{
		return this.size == 0;
	}

	@Override
	public final void clear()
	{
		// clear chain
		this.chain.clear();

		// clear hash array
		final ChainMapEntryLinkedStrongStrong<K, V>[] slots = this.slots;
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
		this.slots = ChainMapEntryLinkedStrongStrong.array(DEFAULT_HASH_LENGTH);
		this.size = 0;
		this.capacity = (int)(DEFAULT_HASH_LENGTH * this.hashDensity);
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * In a {@link HashTable} this removes all empty entries from the passed chain
	 * and returns the number of removed entries.
	 */
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

		// normal case: calculate new slots legnth and rebuild storage
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
	public final HashTable<K, V> copy()
	{
		final HashTable<K, V> newVarMap = new HashTable<>(this);
		this.chain.iterate(new Consumer<KeyValue<K, V>>()
		{
			@Override
			public void accept(final KeyValue<K, V> entry)
			{
				newVarMap.put(entry.key(), entry.value());
			}
		});
		return newVarMap;
	}

	@Override
	public final ConstHashTable<K, V> immure()
	{
		this.consolidate();
		return ConstHashTable.NewCustom(this.size, this.hashDensity, this);
	}

	@Override
	public final XGettingTable<K, V> view()
	{
		return new TableView<>(this);
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
	public final V ensure(final K key, final Function<? super K, V> valueProvider)
	{
		V value = this.get(key);
		if(value == null)
		{
			this.add(key, value = valueProvider.apply(key));
		}
		return value;
	}

	@Override
	public final HashTable<K, V>.Keys keys()
	{
		return this.keys;
	}

	@Override
	public final XTable.EntriesBridge<K, V> old()
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME HashTable#old()
	}

	@Override
	public one.microstream.collections.types.XTable.Bridge<K, V> oldMap()
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
				collector.accept(HashTable.this.get(key));
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
	public final boolean hasVolatileValues()
	{
		return this.chain.hasVolatileValues();
	}

	@Override
	public final KeyValue<K, V> addGet(final K key, final V value)
	{
		for(ChainMapEntryLinkedStrongStrong<K, V> e = this.slots[System.identityHashCode(key) & this.range]; e != null; e = e.link)
		{
			if(e.key() == key)
			{
				return e;
			}
		}
		this.chain.appendEntry(this.createNewEntry(key, value));
		return null;
	}

	@Override
	public final KeyValue<K, V> substitute(final K key, final V value)
	{
		for(ChainMapEntryLinkedStrongStrong<K, V> e = this.slots[System.identityHashCode(key) & this.range]; e != null; e = e.link)
		{
			if(e.key() == key)
			{
				return e;
			}
		}
		this.chain.appendEntry(this.createNewEntry(key, value));
		
		return X.KeyValue(key, value);
	}

	@Override
	public final KeyValue<K, V> putGet(final K key, final V value)
	{
		for(ChainMapEntryLinkedStrongStrong<K, V> e = this.slots[System.identityHashCode(key) & this.range]; e != null; e = e.link)
		{
			if(e.key() == key)
			{
				return X.KeyValue(e.setKey(key), e.setValue(value));
			}
		}
		this.chain.appendEntry(this.createNewEntry(key, value));
		
		return null;
	}
	
	@Override
	public final KeyValue<K, V> replace(final K key, final V value)
	{
		for(ChainMapEntryLinkedStrongStrong<K, V> e = this.slots[System.identityHashCode(key) & this.range]; e != null; e = e.link)
		{
			if(e.key() == key)
			{
				return X.KeyValue(e.setKey(key), e.setValue(value));
			}
		}

		return null;
	}

	@Override
	public final KeyValue<K, V> setGet(final K key, final V value)
	{
		for(ChainMapEntryLinkedStrongStrong<K, V> e = this.slots[System.identityHashCode(key) & this.range]; e != null; e = e.link)
		{
			if(e.key() == key)
			{
				return X.KeyValue(e.setKey(key), e.setValue(value));
			}
		}
		return null;
	}

	@Override
	public final boolean add(final K key, final V value)
	{
		for(ChainMapEntryLinkedStrongStrong<K, V> e = this.slots[System.identityHashCode(key) & this.range]; e != null; e = e.link)
		{
			if(e.key() == key)
			{
				return false; // already contained
			}
		}
		this.chain.appendEntry(this.createNewEntry(key, value));
		return true;
	}

	@Override
	public final boolean put(final K key, final V value)
	{
		for(ChainMapEntryLinkedStrongStrong<K, V> e = this.slots[System.identityHashCode(key) & this.range]; e != null; e = e.link)
		{
			if(e.key() == key)
			{
				e.set0(key, value); // intentionally no moving to end here to cleanly separate concerns
				return false;
			}
		}
		this.chain.appendEntry(this.createNewEntry(key, value));
		return true;
	}

	@Override
	public final boolean set(final K key, final V value)
	{
		for(ChainMapEntryLinkedStrongStrong<K, V> e = this.slots[System.identityHashCode(key) & this.range]; e != null; e = e.link)
		{
			if(e.key() == key)
			{
				e.set0(key, value);
				return true;
			}
		}
		return false;
	}

	@Override
	public final boolean valuePut(final K key, final V value)
	{
		for(ChainMapEntryLinkedStrongStrong<K, V> e = this.slots[System.identityHashCode(key) & this.range]; e != null; e = e.link)
		{
			if(e.key() == key)
			{
				e.setValue0(value);
				return false;
			}
		}
		this.chain.appendEntry(this.createNewEntry(key, value));
		return true;
	}

	@Override
	public final boolean valueSet(final K key, final V value)
	{
		for(ChainMapEntryLinkedStrongStrong<K, V> e = this.slots[System.identityHashCode(key) & this.range]; e != null; e = e.link)
		{
			if(e.key() == key)
			{
				e.setValue0(value);
				return true;
			}
		}
		return false;
	}

	@Override
	public final V valuePutGet(final K key, final V value)
	{
		for(ChainMapEntryLinkedStrongStrong<K, V> e = this.slots[System.identityHashCode(key) & this.range]; e != null; e = e.link)
		{
			if(e.key() == key)
			{
				return e.setValue(value);
			}
		}
		this.chain.appendEntry(this.createNewEntry(key, value));
		return null;
	}

	@Override
	public final V valueSetGet(final K key, final V value)
	{
		for(ChainMapEntryLinkedStrongStrong<K, V> e = this.slots[System.identityHashCode(key) & this.range]; e != null; e = e.link)
		{
			if(e.key() == key)
			{
				return e.setValue(value);
			}
		}
		return null;
	}

	@Override
	public final V removeFor(final K key)
	{
		final int hash;
		ChainMapEntryLinkedStrongStrong<K, V> last, e;
		if((e = this.slots[(hash = System.identityHashCode(key)) & this.range]) == null)
		{
			return null;
		}

		// head entry special case
		if(e.key() == key)
		{
			this.slots[hash & this.range] = e.link;
			this.chain.disjoinEntry(e);
			this.size--;
			return e.value(); // return as value can only be contained once in a set
		}

		// search entry chain
		for(e = (last = e).link; e != null; e = (last = e).link)
		{
			if(e.key() == key)
			{
				last.link = e.link;
				this.chain.disjoinEntry(e);
				this.size--;
				// no further actions necessary (like removing the value etc) as entry will get garbage collected
				return e.value(); // return as value can only be contained once in a set
			}
		}

		return null;
	}

	@Override
	public final HashCollection.Analysis<HashTable<K, V>> analyze()
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
			if((kv = HashTable.this.lookup(entry.key())) == null)
			{
				return false;
			}

			// equality of values is architectural restricted to simple referential equality
			return kv.key() == entry.key() && kv.value() == entry.value();
		};
	}

	@Override
	public final HashTable<K, V> sort(final Comparator<? super KeyValue<K, V>> comparator)
	{
		this.chain.sort(comparator);
		return this;
	}



	///////////////////////////////////////////////////////////////////////////
	// getting methods //
	////////////////////

	@Override
	public final XEnum<KeyValue<K, V>> range(final long lowIndex, final long highIndex)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME HashTable.Entries#range()
	}

	@Override
	public final XGettingEnum<KeyValue<K, V>> view(final long lowIndex, final long highIndex)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME HashTable.Entries#view()
	}

	@Override
	public final KeyValue<K, V>[] toArray(final Class<KeyValue<K, V>> type)
	{
		return HashTable.this.chain.toArray(type);
	}

	// executing //

	@Override
	public final <P extends Consumer<? super KeyValue<K, V>>> P iterate(final P procedure)
	{
		HashTable.this.chain.iterate(procedure);
		return procedure;
	}

	@Override
	public final <A> A join(final BiConsumer<? super KeyValue<K, V>, ? super A> joiner, final A aggregate)
	{
		HashTable.this.chain.join(joiner, aggregate);
		return aggregate;
	}

	@Override
	public final long count(final KeyValue<K, V> entry)
	{
		return HashTable.this.chain.count(entry);
	}

	@Override
	public final long countBy(final Predicate<? super KeyValue<K, V>> predicate)
	{
		return HashTable.this.chain.count(predicate);
	}

	// element querying //

	@Override
	public final KeyValue<K, V> search(final Predicate<? super KeyValue<K, V>> predicate)
	{
		return HashTable.this.chain.search(predicate);
	}

	@Override
	public final KeyValue<K, V> max(final Comparator<? super KeyValue<K, V>> comparator)
	{
		return HashTable.this.chain.max(comparator);
	}

	@Override
	public final KeyValue<K, V> min(final Comparator<? super KeyValue<K, V>> comparator)
	{
		return HashTable.this.chain.min(comparator);
	}

	// boolean querying - applies //

	@Override
	public final boolean containsSearched(final Predicate<? super KeyValue<K, V>> predicate)
	{
		return HashTable.this.chain.containsSearched(predicate);
	}

	@Override
	public final boolean applies(final Predicate<? super KeyValue<K, V>> predicate)
	{
		return HashTable.this.chain.appliesAll(predicate);
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
		for(ChainMapEntryLinkedStrongStrong<K, V> e = HashTable.this.slots[System.identityHashCode(entry.key()) & HashTable.this.range]; e != null; e = e.link)
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
		for(ChainMapEntryLinkedStrongStrong<K, V> e = this.slots[System.identityHashCode(entry.key()) & this.range]; e != null; e = e.link)
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
		return elements.applies(HashTable.this.predicateContainsEntry());
	}

	// boolean querying - equality //

	@Override
	public final boolean equals(final XGettingCollection<? extends KeyValue<K, V>> samples, final Equalator<? super KeyValue<K, V>> equalator)
	{
		if(samples == null || !(samples instanceof HashTable<?, ?>.Keys))
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
		this.consolidate();
		if(HashTable.this.size != XTypes.to_int(samples.size()))
		{
			return false;
		}

		// if sizes are equal and all elements of collection are contained in this set, they must have equal content
		return HashTable.this.chain.equalsContent(samples, equalator);
	}

	// data set procedures //

	@Override
	public final <C extends Consumer<? super KeyValue<K, V>>> C intersect(
		final XGettingCollection<? extends KeyValue<K, V>> other,
		final Equalator<? super KeyValue<K, V>> equalator,
		final C target
	)
	{
		return HashTable.this.chain.intersect(other, equalator, target);
	}

	@Override
	public final <C extends Consumer<? super KeyValue<K, V>>> C except(
		final XGettingCollection<? extends KeyValue<K, V>> other,
		final Equalator<? super KeyValue<K, V>> equalator,
		final C target
	)
	{
		return HashTable.this.chain.except(other, equalator, target);
	}

	@Override
	public final <C extends Consumer<? super KeyValue<K, V>>> C union(
		final XGettingCollection<? extends KeyValue<K, V>> other,
		final Equalator<? super KeyValue<K, V>> equalator,
		final C target
	)
	{
		return HashTable.this.chain.union(other, equalator, target);
	}

	@Override
	public final <C extends Consumer<? super KeyValue<K, V>>> C copyTo(final C target)
	{
		if(target == this)
		{
			return target; // copying a set logic collection to itself would be a no-op, so spare the effort
		}
		return HashTable.this.chain.copyTo(target);
	}

	@Override
	public final <C extends Consumer<? super KeyValue<K, V>>> C filterTo(final C target, final Predicate<? super KeyValue<K, V>> predicate)
	{
		return HashTable.this.chain.copyTo(target, predicate);
	}

	@Override
	public final <C extends Consumer<? super KeyValue<K, V>>> C distinct(final C target)
	{
		return HashTable.this.chain.distinct(target);
	}

	@Override
	public final <C extends Consumer<? super KeyValue<K, V>>> C distinct(final C target, final Equalator<? super KeyValue<K, V>> equalator)
	{
		return HashTable.this.chain.distinct(target, equalator);
	}

	@Override
	public final boolean nullAdd()
	{
		return HashTable.this.nullKeyAdd();
	}

	@Override
	public final boolean add(final KeyValue<K, V> entry)
	{
		return HashTable.this.add(entry.key(), entry.value());

	}

	@SafeVarargs
	@Override
	public final HashTable<K, V> addAll(final KeyValue<K, V>... elements)
	{
		final HashTable<K, V> parent = HashTable.this;
		for(int i = 0, len = elements.length; i < len; i++)
		{
			parent.add(elements[i].key(), elements[i].value());
		}
		return this;
	}

	@Override
	public final HashTable<K, V> addAll(final KeyValue<K, V>[] elements, final int srcIndex, final int srcLength)
	{
		final int d;
		if((d = XArrays.validateArrayRange(elements, srcIndex, srcLength)) == 0)
		{
			return this;
		}

		final int bound = srcIndex + srcLength;
		final HashTable<K, V> parent = HashTable.this;
		for(int i = srcIndex; i != bound; i += d)
		{
			parent.add(elements[i].key(), elements[i].value());
		}

		return this;
	}

	@Override
	public final HashTable<K, V> addAll(final XGettingCollection<? extends KeyValue<K, V>> elements)
	{
		elements.iterate(HashTable.this::add);
		return this;
	}

	@Override
	public final boolean nullPut()
	{
		return this.nullKeyPut();
	}

	@Override
	public final void accept(final KeyValue<K, V> entry)
	{
		this.put(entry.key(), entry.value());
	}

	@Override
	public final boolean put(final KeyValue<K, V> entry)
	{
		return this.put(entry.key(), entry.value());
	}

	@Override
	public final KeyValue<K, V> addGet(final KeyValue<K, V> entry)
	{
		return this.addGet(entry.key(), entry.value());
	}

	@Override
	public final KeyValue<K, V> deduplicate(final KeyValue<K, V> entry)
	{
		// can't delegate because the passed instance shall be returned, not a newly created one
		for(ChainMapEntryLinkedStrongStrong<K, V> e = this.slots[System.identityHashCode(entry.key()) & this.range]; e != null; e = e.link)
		{
			if(e.key() == entry.key())
			{
				return e;
			}
		}
		this.chain.appendEntry(this.createNewEntry(entry.key(), entry.value()));
		
		return entry;
	}

	@Override
	public final KeyValue<K, V> putGet(final KeyValue<K, V> entry)
	{
		return this.putGet(entry.key(), entry.value());
	}

	@Override
	public final KeyValue<K, V> replace(final KeyValue<K, V> entry)
	{
		return this.replace(entry.key(), entry.value());
	}

	@SafeVarargs
	@Override
	public final HashTable<K, V> putAll(final KeyValue<K, V>... elements)
	{
		final HashTable<K, V> parent = HashTable.this;
		for(int i = 0, len = elements.length; i < len; i++)
		{
			parent.put(elements[i].key(), elements[i].value());
		}
		return this;
	}

	@Override
	public final HashTable<K, V> putAll(final KeyValue<K, V>[] elements, final int srcIndex, final int srcLength)
	{
		final int d;
		if((d = XArrays.validateArrayRange(elements, srcIndex, srcLength)) == 0)
		{
			return this;
		}

		final int bound = srcIndex + srcLength;
		final HashTable<K, V> parent = HashTable.this;
		for(int i = srcIndex; i != bound; i += d)
		{
			parent.put(elements[i].key(), elements[i].value());
		}

		return this;
	}

	@Override
	public final HashTable<K, V> putAll(final XGettingCollection<? extends KeyValue<K, V>> elements)
	{
		elements.iterate(this::put);

		return this;
	}

	// removing //

	@Override
	public final long remove(final KeyValue<K, V> entry)
	{
		return HashTable.this.chain.remove(entry);
	}

	@Override
	public final long nullRemove()
	{
		return 0; // cannot remove a null entry because it can never be contained (only null key or null values)
	}

	// reducing //

	@Override
	public final long removeBy(final Predicate<? super KeyValue<K, V>> predicate)
	{
		return HashTable.this.chain.reduce(predicate);
	}

	// retaining //

	@Override
	public final long retainAll(final XGettingCollection<? extends KeyValue<K, V>> elements)
	{
		return HashTable.this.chain.retainAll(elements);
	}

	@Override
	public final <P extends Consumer<? super KeyValue<K, V>>> P process(final P procedure)
	{
		HashTable.this.chain.process(procedure);
		return procedure;
	}

	@Override
	public final <C extends Consumer<? super KeyValue<K, V>>> C moveTo(final C target, final Predicate<? super KeyValue<K, V>> predicate)
	{
		HashTable.this.chain.moveTo(target, predicate);
		return target;
	}

	// removing - all //

	@Override
	public final long removeAll(final XGettingCollection<? extends KeyValue<K, V>> elements)
	{
		final int oldSize = HashTable.this.size;
		elements.iterate(HashTable.this::remove);
		return oldSize - HashTable.this.size;
	}

	// removing - duplicates //

	@Override
	public final long removeDuplicates()
	{
		return 0;
	}

	@Override
	public final long removeDuplicates(final Equalator<? super KeyValue<K, V>> equalator)
	{
		// singleton null can be ignored here
		return HashTable.this.chain.removeDuplicates(equalator);
	}

	@Override
	public final HashTable<K, V> toReversed()
	{
		final HashTable<K, V> reversedVarSet = HashTable.this.copy();
		reversedVarSet.chain.reverse();
		return reversedVarSet;
	}

	@Override
	public final <T extends Consumer<? super KeyValue<K, V>>> T copySelection(final T target, final long... indices)
	{
		HashTable.this.chain.copySelection(target, indices);
		return target;
	}

	@Override
	public final <P extends IndexedAcceptor<? super KeyValue<K, V>>> P iterateIndexed(final P procedure)
	{
		HashTable.this.chain.iterateIndexed(procedure);
		return procedure;
	}

	@Override
	public final KeyValue<K, V> at(final long index)
	{
		return HashTable.this.chain.get(index);
	}

	@Override
	public final KeyValue<K, V> get()
	{
		return HashTable.this.chain.first();
	}

	@Override
	public final KeyValue<K, V> first()
	{
		return HashTable.this.chain.first();
	}

	@Override
	public final KeyValue<K, V> last()
	{
		return HashTable.this.chain.last();
	}

	@Override
	public final KeyValue<K, V> poll()
	{
		return HashTable.this.size == 0 ? null : HashTable.this.chain.first();
	}

	@Override
	public final KeyValue<K, V> peek()
	{
		return HashTable.this.size == 0 ? null : HashTable.this.chain.last();
	}

	@Override
	public final long indexOf(final KeyValue<K, V> entry)
	{
		return HashTable.this.chain.indexOf(entry);
	}

	@Override
	public final long indexBy(final Predicate<? super KeyValue<K, V>> predicate)
	{
		return HashTable.this.chain.indexOf(predicate);
	}

	@Override
	public final boolean isSorted(final Comparator<? super KeyValue<K, V>> comparator)
	{
		return HashTable.this.chain.isSorted(comparator);
	}

	@Override
	public final long lastIndexOf(final KeyValue<K, V> entry)
	{
		return this.chain.lastIndexOf(entry);
	}

	@Override
	public final long lastIndexBy(final Predicate<? super KeyValue<K, V>> predicate)
	{
		return this.chain.lastIndexBy(predicate);
	}

	@Override
	public final long maxIndex(final Comparator<? super KeyValue<K, V>> comparator)
	{
		return HashTable.this.chain.maxIndex(comparator);
	}

	@Override
	public final long minIndex(final Comparator<? super KeyValue<K, V>> comparator)
	{
		return HashTable.this.chain.minIndex(comparator);
	}

	@Override
	public final long scan(final Predicate<? super KeyValue<K, V>> predicate)
	{
		return HashTable.this.chain.scan(predicate);
	}

	@Override
	public final <C extends Consumer<? super KeyValue<K, V>>> C moveSelection(final C target, final long... indices)
	{
		HashTable.this.chain.moveSelection(target, indices);
		return target;
	}

	@Override
	public final KeyValue<K, V> removeAt(final long index)
	{
		return HashTable.this.chain.remove(index);
	}

	@Override
	public final KeyValue<K, V> fetch()
	{
		return HashTable.this.chain.remove(0);
	}

	@Override
	public final KeyValue<K, V> pop()
	{
		return HashTable.this.chain.remove(HashTable.this.size - 1);
	}

	@Override
	public final KeyValue<K, V> pinch()
	{
		return HashTable.this.size == 0 ? null : HashTable.this.chain.remove(0);
	}

	@Override
	public final KeyValue<K, V> pick()
	{
		return HashTable.this.size == 0 ? null : HashTable.this.chain.remove(HashTable.this.size - 1);
	}

	@Override
	public final KeyValue<K, V> retrieve(final KeyValue<K, V> entry)
	{
		return HashTable.this.chain.retrieve(entry);
	}

	@Override
	public final KeyValue<K, V> retrieveBy(final Predicate<? super KeyValue<K, V>> predicate)
	{
		return HashTable.this.chain.retrieve(predicate);
	}

	@Override
	public final boolean removeOne(final KeyValue<K, V> entry)
	{
		return HashTable.this.chain.removeOne(entry);
	}

	@Override
	public final HashTable<K, V> removeRange(final long startIndex, final long length)
	{
		HashTable.this.chain.removeRange(startIndex, length);
		return this;
	}

	@Override
	public final HashTable<K, V> retainRange(final long startIndex, final long length)
	{
		HashTable.this.chain.retainRange(startIndex, length);
		return this;
	}

	@Override
	public final long removeSelection(final long[] indices)
	{
		return HashTable.this.chain.removeSelection(indices);
	}

	@Override
	public final Iterator<KeyValue<K, V>> iterator()
	{
		return HashTable.this.chain.iterator();
	}

	@Override
	public final Object[] toArray()
	{
		return HashTable.this.chain.toArray();
	}

	@Override
	public final HashTable<K, V> reverse()
	{
		HashTable.this.chain.reverse();
		return this;
	}

	@Override
	public final HashTable<K, V> shiftTo(final long sourceIndex, final long targetIndex)
	{
		HashTable.this.chain.shiftTo(sourceIndex, targetIndex);
		return this;
	}

	@Override
	public final HashTable<K, V> shiftTo(final long sourceIndex, final long targetIndex, final long length)
	{
		HashTable.this.chain.shiftTo(sourceIndex, targetIndex, length);
		return this;
	}

	@Override
	public final HashTable<K, V> shiftBy(final long sourceIndex, final long distance)
	{
		HashTable.this.chain.shiftTo(sourceIndex, distance);
		return this;
	}

	@Override
	public final HashTable<K, V> shiftBy(final long sourceIndex, final long distance, final long length)
	{
		HashTable.this.chain.shiftTo(sourceIndex, distance, length);
		return this;
	}

	@Override
	public final HashTable<K, V> swap(final long indexA, final long indexB)
	{
		HashTable.this.chain.swap(indexA, indexB);
		return this;
	}

	@Override
	public final HashTable<K, V> swap(final long indexA, final long indexB, final long length)
	{
		HashTable.this.chain.swap(indexA, indexB, length);
		return this;
	}

	@Override
	public final HashEqualator<KeyValue<K, V>> equality()
	{
		return XHashing.keyValueHashEqualityKeyIdentity();
	}

	@Override
	public final boolean input(final long index, final KeyValue<K, V> element)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME HashTable.Entries#input()
	}

	@SafeVarargs
	@Override
	public final long inputAll(final long index, final KeyValue<K, V>... elements)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME HashTable.Entries#input()
	}

	@Override
	public final long inputAll(final long index, final KeyValue<K, V>[] elements, final int offset, final int length)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME HashTable.Entries#inputAll()
	}

	@Override
	public final long inputAll(final long index, final XGettingCollection<? extends KeyValue<K, V>> elements)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME HashTable.Entries#inputAll()
	}

	@Override
	public final boolean insert(final long index, final KeyValue<K, V> element)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME HashTable.Entries#insert()
	}

	@SafeVarargs
	@Override
	public final long insertAll(final long index, final KeyValue<K, V>... elements)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME HashTable.Entries#insert()
	}

	@Override
	public final long insertAll(final long index, final KeyValue<K, V>[] elements, final int offset, final int length)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME HashTable.Entries#insertAll()
	}

	@Override
	public final long insertAll(final long index, final XGettingCollection<? extends KeyValue<K, V>> elements)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME HashTable.Entries#insertAll()
	}

	@Override
	public final boolean prepend(final KeyValue<K, V> element)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME HashTable.Entries#prepend()
	}

	@Override
	public final boolean preput(final KeyValue<K, V> element)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME HashTable.Entries#preput()
	}

	@Override
	public final boolean nullInput(final long index)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME HashTable.Entries#nullInput()
	}

	@Override
	public final boolean nullInsert(final long index)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME HashTable.Entries#nullInsert()
	}

	@Override
	public final boolean nullPrepend()
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME HashTable.Entries#nullPrepend()
	}

	@SafeVarargs
	@Override
	public final HashTable<K, V> prependAll(final KeyValue<K, V>... elements)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME HashTable.Entries#prepend()
	}

	@Override
	public final HashTable<K, V> prependAll(final KeyValue<K, V>[] elements, final int srcStartIndex, final int srcLength)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME HashTable.Entries#prependAll()
	}

	@Override
	public final HashTable<K, V> prependAll(final XGettingCollection<? extends KeyValue<K, V>> elements)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME HashTable.Entries#prependAll()
	}

	@Override
	public final boolean nullPreput()
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME HashTable.Entries#nullPreput()
	}

	@SafeVarargs
	@Override
	public final HashTable<K, V> preputAll(final KeyValue<K, V>... elements)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME HashTable.Entries#preput()
	}

	@Override
	public final HashTable<K, V> preputAll(final KeyValue<K, V>[] elements, final int offset, final int length)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME HashTable.Entries#preputAll()
	}

	@Override
	public final HashTable<K, V> preputAll(final XGettingCollection<? extends KeyValue<K, V>> elements)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME HashTable.Entries#preputAll()
	}

	@Override
	public final boolean set(final long index, final KeyValue<K, V> element)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME HashTable.Entries#set()
	}

	@Override
	public final KeyValue<K, V> setGet(final long index, final KeyValue<K, V> element)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME HashTable.Entries#setGet()
	}

	@Override
	public final void setFirst(final KeyValue<K, V> element)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME HashTable.Entries#setFirst()
	}

	@Override
	public final void setLast(final KeyValue<K, V> element)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME HashTable.Entries#setLast()
	}

	@SafeVarargs
	@Override
	public final HashTable<K, V> setAll(final long index, final KeyValue<K, V>... elements)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME HashTable.Entries#set()
	}

	@Override
	public final HashTable<K, V> set(final long index, final KeyValue<K, V>[] elements, final int offset, final int length)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME HashTable.Entries#set()
	}

	@Override
	public final HashTable<K, V> set(final long index, final XGettingSequence<? extends KeyValue<K, V>> elements, final long offset, final long length)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME HashTable.Entries#set()
	}
	
	@Override
	public long substitute(final Function<? super KeyValue<K, V>, ? extends KeyValue<K, V>> mapper)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME HashTable#substitute()
	}
	
	final void replace(final ChainMapEntryLinkedStrongStrong<K, V> oldEntry, final K newElement)
	{
		final int newHash = System.identityHashCode(newElement);
		for(ChainMapEntryLinkedStrongStrong<K, V> e = this.slots[newHash & this.range]; e != null; e = e.link)
		{
			if(e.key == newElement)
			{
				if(e == oldEntry)
				{
					// simple case: the old entry's element gets replaced by a hash-equivalent new one.
					e.setKey0(newElement);
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


	public final class Keys implements XTable.Keys<K, V>, HashCollection<K>
	{
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		public final int hashDistributionRange()
		{
			return HashTable.this.slots.length;
		}

		@Override
		public final boolean hasVolatileHashElements()
		{
			return HashTable.this.chain.hasVolatileElements();
		}

		@Override
		public final void setHashDensity(final float hashDensity)
		{
			HashTable.this.setHashDensity(hashDensity);
		}

		@Override
		public final HashCollection.Analysis<Keys> analyze()
		{
			return AbstractChainEntryLinked.analyzeSlots(this, HashTable.this.slots);
		}



		///////////////////////////////////////////////////////////////////////////
		// getting methods //
		////////////////////

		@Override
		public final Equalator<? super K> equality()
		{
			return HashTable.this.hashEquality();
		}

		@Override
		public final Keys copy()
		{
			return HashTable.this.copy().keys();
		}

		/**
		 * This method creates a {@link EqConstHashEnum} instance containing all (currently existing) elements
		 * of this {@link ConstHashEnum}.<br>
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
		public final ConstHashEnum<K> immure()
		{
			this.consolidate();
			return ConstHashEnum.NewCustom( //const set may not contain volatile hash logic.
				HashTable.this.hashDensity,
				this
			);
		}

		@Override
		public final XGettingEnum<K> view()
		{
			return new EnumView<>(this);
		}

		@Override
		public final XEnum<K> range(final long lowIndex, final long highIndex)
		{
			throw new one.microstream.meta.NotImplementedYetError(); // FIXME HashTable.Keys#range()
		}

		@Override
		public final XGettingEnum<K> view(final long lowIndex, final long highIndex)
		{
			throw new one.microstream.meta.NotImplementedYetError(); // FIXME HashTable.Keys#view()
		}

		@Override
		public final K[] toArray(final Class<K> type)
		{
			return HashTable.this.chain.keyToArray(type);
		}

		// executing //

		@Override
		public final <P extends Consumer<? super K>> P iterate(final P procedure)
		{
			HashTable.this.chain.keyIterate(procedure);
			return procedure;
		}

		@Override
		public final <A> A join(final BiConsumer<? super K, ? super A> joiner, final A aggregate)
		{
			HashTable.this.chain.keyJoin(joiner, aggregate);
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
			return HashTable.this.chain.keyCount(predicate);
		}

		// element querying //

		@Override
		public final K seek(final K sample)
		{
			return HashTable.this.chain.keySeek(sample);
		}

		@Override
		public final K search(final Predicate<? super K> predicate)
		{
			return HashTable.this.chain.keySearch(predicate);
		}

		@Override
		public final K max(final Comparator<? super K> comparator)
		{
			return HashTable.this.chain.keyMax(comparator);
		}

		@Override
		public final K min(final Comparator<? super K> comparator)
		{
			return HashTable.this.chain.keyMin(comparator);
		}

		// boolean querying //

		@Override
		public final boolean hasVolatileElements()
		{
			return HashTable.this.chain.hasVolatileElements();
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
			return HashTable.this.chain.keyApplies(predicate);
		}

		@Override
		public final boolean applies(final Predicate<? super K> predicate)
		{
			return HashTable.this.chain.keyAppliesAll(predicate);
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
			for(ChainMapEntryLinkedStrongStrong<K, V> e = HashTable.this.slots[System.identityHashCode(element) & HashTable.this.range]; e != null; e = e.link)
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
			for(ChainMapEntryLinkedStrongStrong<K, V> e = HashTable.this.slots[System.identityHashCode(element) & HashTable.this.range]; e != null; e = e.link)
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
			return elements.applies(HashTable.this::containsKey);
		}

		// boolean querying - equality //

		@Override
		public final boolean equals(final XGettingCollection<? extends K> samples, final Equalator<? super K> equalator)
		{
			if(samples == null || !(samples instanceof HashTable<?, ?>.Keys))
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
			this.consolidate();
			if(HashTable.this.size != XTypes.to_int(samples.size()))
			{
				return false;
			}

			// if sizes are equal and all elements of collection are contained in this set, they must have equal content
			return HashTable.this.chain.keyEqualsContent(samples, equalator);
		}

		// data set procedures //

		@Override
		public final <C extends Consumer<? super K>> C intersect(
			final XGettingCollection<? extends K> other,
			final Equalator<? super K> equalator,
			final C target
		)
		{
			return HashTable.this.chain.keyIntersect(other, equalator, target);
		}

		@Override
		public final <C extends Consumer<? super K>> C except(
			final XGettingCollection<? extends K> other,
			final Equalator<? super K> equalator,
			final C target
		)
		{
			return HashTable.this.chain.keyExcept(other, equalator, target);
		}

		@Override
		public final <C extends Consumer<? super K>> C union(
			final XGettingCollection<? extends K> other,
			final Equalator<? super K> equalator,
			final C target
		)
		{
			return HashTable.this.chain.keyUnion(other, equalator, target);
		}

		@Override
		public final <C extends Consumer<? super K>> C copyTo(final C target)
		{
			if(target == this)
			{
				return target; // copying a set logic collection to itself would be a no-op, so spare the effort
			}
			return HashTable.this.chain.keyCopyTo(target);
		}

		@Override
		public final <C extends Consumer<? super K>> C filterTo(final C target, final Predicate<? super K> predicate)
		{
			return HashTable.this.chain.keyCopyTo(target, predicate);
		}

		@Override
		public final <C extends Consumer<? super K>> C distinct(final C target)
		{
			return HashTable.this.chain.keyDistinct(target);
		}

		@Override
		public final <C extends Consumer<? super K>> C distinct(final C target, final Equalator<? super K> equalator)
		{
			if(equalator instanceof IdentityEqualator<?>)
			{
				return this.copyTo(target);
			}
			return HashTable.this.chain.keyDistinct(target, equalator);
		}



		///////////////////////////////////////////////////////////////////////////
		// adding //
		///////////

		@Override
		public final long currentCapacity()
		{
			return HashTable.this.currentCapacity();
		}

		@Override
		public final long maximumCapacity()
		{
			return HashTable.this.maximumCapacity();
		}

		@Override
		public final boolean isFull()
		{
			return HashTable.this.isFull();
		}

		@Override
		public final long optimize()
		{
			return HashTable.this.optimize();
		}

		@Override
		public final Keys ensureFreeCapacity(final long requiredFreeCapacity)
		{
			HashTable.this.ensureFreeCapacity(requiredFreeCapacity);
			return this;
		}

		@Override
		public final Keys ensureCapacity(final long minimalCapacity)
		{
			HashTable.this.ensureCapacity(minimalCapacity);
			return this;
		}

		@Override
		public final boolean nullAdd()
		{
			return HashTable.this.nullKeyAdd();
		}

		@Override
		public final boolean add(final K element)
		{
			return HashTable.this.internalAddOnlyKey(element);
		}

		@SafeVarargs
		@Override
		public final Keys addAll(final K... elements)
		{
			final HashTable<K, V> parent = HashTable.this;
			for(int i = 0, len = elements.length; i < len; i++)
			{
				parent.internalAddOnlyKey(elements[i]);
			}
			return this;
		}

		@Override
		public final Keys addAll(final K[] elements, final int srcIndex, final int srcLength)
		{
			final int d;
			if((d = XArrays.validateArrayRange(elements, srcIndex, srcLength)) == 0)
			{
				return this;
			}

			final int bound = srcIndex + srcLength;
			final HashTable<K, V> parent = HashTable.this;
			for(int i = srcIndex; i != bound; i += d)
			{
				parent.internalAddOnlyKey(elements[i]);
			}

			return this;
		}

		@Override
		public final Keys addAll(final XGettingCollection<? extends K> elements)
		{
			elements.iterate(HashTable.this::internalAddOnlyKey);
			return this;
		}

		@Override
		public final boolean nullPut()
		{
			return HashTable.this.nullKeyPut();
		}

		@Override
		public final void accept(final K element)
		{
			HashTable.this.internalPutOnlyKey(element);
		}

		@Override
		public final boolean put(final K element)
		{
			return HashTable.this.internalPutOnlyKey(element);
		}

		@Override
		public final K addGet(final K element)
		{
			return HashTable.this.internalAddGetKey(element);
		}

		@Override
		public final K deduplicate(final K element)
		{
			return HashTable.this.internalSubstituteKey(element);
		}

		@Override
		public final K putGet(final K element)
		{
			return HashTable.this.internalPutGetKey(element);
		}

		@Override
		public K replace(final K element)
		{
			return HashTable.this.internalReplaceKey(element);
		}

		@SafeVarargs
		@Override
		public final Keys putAll(final K... elements)
		{
			final HashTable<K, V> parent = HashTable.this;
			for(int i = 0, len = elements.length; i < len; i++)
			{
				parent.internalPutOnlyKey(elements[i]);
			}
			return this;
		}

		@Override
		public final Keys putAll(final K[] elements, final int srcIndex, final int srcLength)
		{
			final int d;
			if((d = XArrays.validateArrayRange(elements, srcIndex, srcLength)) == 0)
			{
				return this;
			}

			final int bound = srcIndex + srcLength;
			final HashTable<K, V> parent = HashTable.this;
			for(int i = srcIndex; i != bound; i += d)
			{
				parent.internalPutOnlyKey(elements[i]);
			}

			return this;
		}

		@Override
		public final Keys putAll(final XGettingCollection<? extends K> elements)
		{
			elements.iterate(k ->
				HashTable.this.put(k, null)
			);

			return this;
		}



		///////////////////////////////////////////////////////////////////////////
		// removing //
		/////////////

		/**
		 * Allocates a new internal storage with default size. No cutting of entry references is performed.
		 * <p>
		 * This can be substantially faster than {@link #clear()} as long as enough heap size is available but will also
		 * fragment heap much faster and thus slow down garbage collection compared to {@link #clear()}.
		 * <p>
		 * To clear the set in a heap-clean way and reduce internal storage size to default, use both {@link #clear()}
		 * and {@link #truncate()}.
		 *
		 * @see XProcessingCollection#truncate()
		 */
		@Override
		public final void truncate()
		{
			HashTable.this.truncate();
		}

		@Override
		public final long consolidate()
		{
			return HashTable.this.consolidate();
		}

		// removing //

		@Override
		public final long remove(final K element)
		{
			return HashTable.this.removeKey(element);
		}

		@Override
		public final long nullRemove()
		{
			return HashTable.this.removeKey(null);
		}

		// reducing //

		@Override
		public final long removeBy(final Predicate<? super K> predicate)
		{
			return HashTable.this.chain.keyReduce(predicate);
		}

		// retaining //

		@Override
		public final long retainAll(final XGettingCollection<? extends K> elements)
		{
			return HashTable.this.chain.keyRetainAll(elements);
		}

		@Override
		public final <P extends Consumer<? super K>> P process(final P procedure)
		{
			HashTable.this.chain.keyProcess(procedure);
			return procedure;
		}

		@Override
		public final <C extends Consumer<? super K>> C moveTo(final C target, final Predicate<? super K> predicate)
		{
			HashTable.this.chain.keyMoveTo(target, predicate);
			return target;
		}

		// removing - all //

		@Override
		public final long removeAll(final XGettingCollection<? extends K> elements)
		{
			final int oldSize = HashTable.this.size;
			elements.iterate(HashTable.this::removeFor);
			return oldSize - HashTable.this.size;
		}

		// removing - duplicates //

		@Override
		public final long removeDuplicates()
		{
			return 0;
		}

		@Override
		public final long removeDuplicates(final Equalator<? super K> equalator)
		{
			if(equalator instanceof IdentityEqualator<?>)
			{
				return 0; // set is guaranteed to contain unique values according to its inherent equalator
			}

			// singleton null can be ignored here
			return HashTable.this.chain.keyRemoveDuplicates(equalator);
		}

		@Override
		public final Keys toReversed()
		{
			final HashTable<K, V> reversedVarSet = HashTable.this.copy();
			reversedVarSet.chain.reverse();
			return reversedVarSet.keys;
		}

		@Override
		public final <T extends Consumer<? super K>> T copySelection(final T target, final long... indices)
		{
			HashTable.this.chain.keyCopySelection(target, indices);
			return target;
		}

		@Override
		public final <P extends IndexedAcceptor<? super K>> P iterateIndexed(final P procedure)
		{
			HashTable.this.chain.keyIterateIndexed(procedure);
			return procedure;
		}

		@Override
		public final K at(final long index)
		{
			return HashTable.this.chain.keyGet(index);
		}

		@Override
		public final K get()
		{
			return HashTable.this.chain.keyFirst();
		}

		@Override
		public final K first()
		{
			return HashTable.this.chain.keyFirst();
		}

		@Override
		public final K last()
		{
			return HashTable.this.chain.keyLast();
		}

		@Override
		public final K poll()
		{
			return HashTable.this.size == 0 ? null : HashTable.this.chain.keyFirst();
		}

		@Override
		public final K peek()
		{
			return HashTable.this.size == 0 ? null : HashTable.this.chain.keyLast();
		}

		@Override
		public final long indexOf(final K element)
		{
			return HashTable.this.chain.keyIndexOf(element);
		}

		@Override
		public final long indexBy(final Predicate<? super K> predicate)
		{
			return HashTable.this.chain.keyIndexBy(predicate);
		}

		@Override
		public final boolean isSorted(final Comparator<? super K> comparator)
		{
			return HashTable.this.chain.keyIsSorted(comparator);
		}

		@Override
		public final long lastIndexOf(final K element)
		{
			return HashTable.this.chain.keyLastIndexOf(element);
		}

		@Override
		public final long lastIndexBy(final Predicate<? super K> predicate)
		{
			return HashTable.this.chain.keyLastIndexBy(predicate);
		}

		@Override
		public final long maxIndex(final Comparator<? super K> comparator)
		{
			return HashTable.this.chain.keyMaxIndex(comparator);
		}

		@Override
		public final long minIndex(final Comparator<? super K> comparator)
		{
			return HashTable.this.chain.keyMinIndex(comparator);
		}

		@Override
		public final long scan(final Predicate<? super K> predicate)
		{
			return HashTable.this.chain.keyScan(predicate);
		}

		@Override
		public final <C extends Consumer<? super K>> C moveSelection(final C target, final long... indices)
		{
			HashTable.this.chain.keyMoveSelection(target, indices);
			return target;
		}

		@Override
		public final K removeAt(final long index)
		{
			return HashTable.this.chain.keyRemove(index);
		}

		@Override
		public final K fetch()
		{
			return HashTable.this.chain.keyRemove(0);
		}

		@Override
		public final K pop()
		{
			return HashTable.this.chain.keyRemove(HashTable.this.size - 1);
		}

		@Override
		public final K pinch()
		{
			return HashTable.this.size == 0 ? null : HashTable.this.chain.keyRemove(0);
		}

		@Override
		public final K pick()
		{
			return HashTable.this.size == 0 ? null : HashTable.this.chain.keyRemove(HashTable.this.size - 1);
		}

		@Override
		public final K retrieve(final K element)
		{
			return HashTable.this.chain.keyRetrieve(element);
		}

		@Override
		public final K retrieveBy(final Predicate<? super K> predicate)
		{
			return HashTable.this.chain.keyRetrieve(predicate);
		}

		@Override
		public final boolean removeOne(final K element)
		{
			return HashTable.this.chain.keyRemoveOne(element);
		}

		@Override
		public final Keys removeRange(final long startIndex, final long length)
		{
			HashTable.this.chain.removeRange(startIndex, length);
			return this;
		}

		@Override
		public final Keys retainRange(final long startIndex, final long length)
		{
			HashTable.this.chain.retainRange(startIndex, length);
			return this;
		}

		@Override
		public final long removeSelection(final long[] indices)
		{
			return HashTable.this.chain.removeSelection(indices);
		}

		@Override
		public final boolean isEmpty()
		{
			return HashTable.this.isEmpty();
		}

		@Override
		public final Iterator<K> iterator()
		{
			return HashTable.this.chain.keyIterator();
		}

		@Override
		public final long size()
		{
			return HashTable.this.size;
		}

		@Override
		public final int rehash()
		{
			return HashTable.this.rehash();
		}

		@Override
		public final String toString()
		{
			if(HashTable.this.size == 0)
			{
				return "[]"; // array causes problems with escape condition otherwise
			}

			final VarString vc = VarString.New(HashTable.this.slots.length).append('[');
			HashTable.this.chain.keyAppendTo(vc, ',').append(']');
			return vc.toString();
		}

		@Override
		public final Object[] toArray()
		{
			return HashTable.this.chain.keyToArray();
		}

		/**
		 * Cuts all references to existing entries, effectively clearing the set.
		 * <p>
		 * The internal storage remains at its current size. All inter-entry references are cut as well, easing garbage
		 * collection of discarded entry instances belonging to different generations.
		 * <p>
		 * To simply reallocate a new internal storage with default size, see {@link #truncate()}
		 *
		 * @see XProcessingCollection#clear()
		 */
		@Override
		public final void clear()
		{
			HashTable.this.clear();
		}

		@Override
		public final Keys reverse()
		{
			HashTable.this.chain.reverse();
			return this;
		}

		@Override
		public final Keys sort(final Comparator<? super K> comparator)
		{
			HashTable.this.chain.keySort(comparator);
			return this;
		}

		@Override
		public final Keys shiftTo(final long sourceIndex, final long targetIndex)
		{
			HashTable.this.chain.shiftTo(sourceIndex, targetIndex);
			return this;
		}

		@Override
		public final Keys shiftTo(final long sourceIndex, final long targetIndex, final long length)
		{
			HashTable.this.chain.shiftTo(sourceIndex, targetIndex, length);
			return this;
		}

		@Override
		public final Keys shiftBy(final long sourceIndex, final long distance)
		{
			HashTable.this.chain.shiftTo(sourceIndex, distance);
			return this;
		}

		@Override
		public final Keys shiftBy(final long sourceIndex, final long distance, final long length)
		{
			HashTable.this.chain.shiftTo(sourceIndex, distance, length);
			return this;
		}

		@Override
		public final Keys swap(final long indexA, final long indexB)
		{
			HashTable.this.chain.swap(indexA, indexB);
			return this;
		}

		@Override
		public final Keys swap(final long indexA, final long indexB, final long length)
		{
			HashTable.this.chain.swap(indexA, indexB, length);
			return this;
		}

		@Override
		public final OldKeys old()
		{
			return new OldKeys();
		}

		@Override
		public final HashTable<K, V> parent()
		{
			return HashTable.this;
		}

		@Override
		public final HashEqualator<K> hashEquality()
		{
			return HashTable.this.hashEquality();
		}

		@Override
		public final float hashDensity()
		{
			return HashTable.this.hashDensity();
		}

		public final class OldKeys extends AbstractBridgeXSet<K>
		{
			protected OldKeys()
			{
				super(Keys.this);
			}

			@Override
			public final Keys parent()
			{
				return (Keys)super.parent();
			}

		}

		@Override
		public final boolean input(final long index, final K element)
		{
			throw new one.microstream.meta.NotImplementedYetError(); // FIXME HashTable.Keys#input()
		}

		@SafeVarargs
		@Override
		public final long inputAll(final long index, final K... elements)
		{
			throw new one.microstream.meta.NotImplementedYetError(); // FIXME HashTable.Keys#input()
		}

		@Override
		public final long inputAll(final long index, final K[] elements, final int offset, final int length)
		{
			throw new one.microstream.meta.NotImplementedYetError(); // FIXME HashTable.Keys#inputAll()
		}

		@Override
		public final long inputAll(final long index, final XGettingCollection<? extends K> elements)
		{
			throw new one.microstream.meta.NotImplementedYetError(); // FIXME HashTable.Keys#inputAll()
		}

		@Override
		public final boolean insert(final long index, final K element)
		{
			throw new one.microstream.meta.NotImplementedYetError(); // FIXME HashTable.Keys#insert()
		}

		@SafeVarargs
		@Override
		public final long insertAll(final long index, final K... elements)
		{
			throw new one.microstream.meta.NotImplementedYetError(); // FIXME HashTable.Keys#insert()
		}

		@Override
		public final long insertAll(final long index, final K[] elements, final int offset, final int length)
		{
			throw new one.microstream.meta.NotImplementedYetError(); // FIXME HashTable.Keys#insertAll()
		}

		@Override
		public final long insertAll(final long index, final XGettingCollection<? extends K> elements)
		{
			throw new one.microstream.meta.NotImplementedYetError(); // FIXME HashTable.Keys#insertAll()
		}

		@Override
		public final boolean prepend(final K element)
		{
			throw new one.microstream.meta.NotImplementedYetError(); // FIXME HashTable.Keys#prepend()
		}

		@Override
		public final boolean preput(final K element)
		{
			throw new one.microstream.meta.NotImplementedYetError(); // FIXME HashTable.Keys#preput()
		}

		@Override
		public final boolean nullInput(final long index)
		{
			throw new one.microstream.meta.NotImplementedYetError(); // FIXME HashTable.Keys#nullInput()
		}

		@Override
		public final boolean nullInsert(final long index)
		{
			throw new one.microstream.meta.NotImplementedYetError(); // FIXME HashTable.Keys#nullInsert()
		}

		@Override
		public final boolean nullPrepend()
		{
			throw new one.microstream.meta.NotImplementedYetError(); // FIXME HashTable.Keys#nullPrepend()
		}

		@Override
		public final Keys prependAll(@SuppressWarnings("unchecked") final K... elements)
		{
			throw new one.microstream.meta.NotImplementedYetError(); // FIXME HashTable.Keys#prepend()
		}

		@Override
		public final Keys prependAll(final K[] elements, final int srcStartIndex, final int srcLength)
		{
			throw new one.microstream.meta.NotImplementedYetError(); // FIXME HashTable.Keys#prependAll()
		}

		@Override
		public final Keys prependAll(final XGettingCollection<? extends K> elements)
		{
			throw new one.microstream.meta.NotImplementedYetError(); // FIXME HashTable.Keys#prependAll()
		}

		@Override
		public final boolean nullPreput()
		{
			throw new one.microstream.meta.NotImplementedYetError(); // FIXME HashTable.Keys#nullPreput()
		}

		@SafeVarargs
		@Override
		public final Keys preputAll(final K... elements)
		{
			throw new one.microstream.meta.NotImplementedYetError(); // FIXME HashTable.Keys#preput()
		}

		@Override
		public final Keys preputAll(final K[] elements, final int offset, final int length)
		{
			throw new one.microstream.meta.NotImplementedYetError(); // FIXME HashTable.Keys#preputAll()
		}

		@Override
		public final Keys preputAll(final XGettingCollection<? extends K> elements)
		{
			throw new one.microstream.meta.NotImplementedYetError(); // FIXME HashTable.Keys#preputAll()
		}

		@Override
		public final boolean set(final long index, final K element)
		{
			throw new one.microstream.meta.NotImplementedYetError(); // FIXME HashTable.Keys#set()
		}

		@Override
		public final K setGet(final long index, final K element)
		{
			throw new one.microstream.meta.NotImplementedYetError(); // FIXME HashTable.Keys#setGet()
		}

		@Override
		public final void setFirst(final K element)
		{
			throw new one.microstream.meta.NotImplementedYetError(); // FIXME HashTable.Keys#setFirst()
		}

		@Override
		public final void setLast(final K element)
		{
			throw new one.microstream.meta.NotImplementedYetError(); // FIXME HashTable.Keys#setLast()
		}

		@SafeVarargs
		@Override
		public final Keys setAll(final long index, final K... elements)
		{
			throw new one.microstream.meta.NotImplementedYetError(); // FIXME HashTable.Keys#set()
		}

		@Override
		public final Keys set(final long index, final K[] elements, final int offset, final int length)
		{
			throw new one.microstream.meta.NotImplementedYetError(); // FIXME HashTable.Keys#set()
		}

		@Override
		public final Keys set(final long index, final XGettingSequence<? extends K> elements, final long offset, final long length)
		{
			throw new one.microstream.meta.NotImplementedYetError(); // FIXME HashTable.Keys#set()
		}

		@Override
		public long substitute(final Function<? super K, ? extends K> mapper)
		{
			return HashTable.this.chain.keySubstitute(mapper, HashTable.this::replace);
		}

	}



	public final class Values implements XTable.Values<K, V>
	{
		@Override
		public final Equalator<? super V> equality()
		{
			return Equalator.identity();
		}

		@Override
		public final XList<V> copy()
		{
			return new BulkList<V>(XTypes.to_int(HashTable.this.size())).addAll(this);
		}

		@Override
		public final <P extends Consumer<? super V>> P iterate(final P procedure)
		{
			HashTable.this.chain.valuesIterate(procedure);
			return procedure;
		}

		@Override
		public final <A> A join(final BiConsumer<? super V, ? super A> joiner, final A aggregate)
		{
			HashTable.this.chain.valuesJoin(joiner, aggregate);
			return aggregate;
		}

		@Override
		public final <P extends IndexedAcceptor<? super V>> P iterateIndexed(final P procedure)
		{
			HashTable.this.chain.valuesIterateIndexed(procedure);
			return procedure;
		}

		@Override
		public final Values toReversed()
		{
			final HashTable<K, V> reversedVarSet = HashTable.this.copy();
			reversedVarSet.chain.reverse();
			return reversedVarSet.values;
		}

		@Override
		public final boolean containsSearched(final Predicate<? super V> predicate)
		{
			return HashTable.this.chain.valuesApplies(predicate);
		}

		@Override
		public final boolean applies(final Predicate<? super V> predicate)
		{
			return HashTable.this.chain.valuesAppliesAll(predicate);
		}

		@Override
		public final boolean contains(final V value)
		{
			return HashTable.this.chain.valuesContains(value);
		}

		@Override
		public final boolean containsAll(final XGettingCollection<? extends V> values)
		{
			return HashTable.this.chain.valuesContainsAll(values);
		}

		@Override
		public final boolean containsId(final V value)
		{
			return HashTable.this.chain.valuesContainsId(value);
		}

		@Override
		public final <T extends Consumer<? super V>> T copyTo(final T target)
		{
			HashTable.this.chain.valuesCopyTo(target);
			return target;
		}

		@Override
		public final <T extends Consumer<? super V>> T filterTo(final T target, final Predicate<? super V> predicate)
		{
			HashTable.this.chain.valuesCopyTo(target, predicate);
			return target;
		}

		@Override
		public final long count(final V value)
		{
			return HashTable.this.chain.valuesCount(value);
		}

		@Override
		public final long countBy(final Predicate<? super V> predicate)
		{
			return HashTable.this.chain.valuesCount(predicate);
		}

		@Override
		public final <T extends Consumer<? super V>> T distinct(final T target)
		{
			HashTable.this.chain.valuesDistinct(target);
			return target;
		}

		@Override
		public final <T extends Consumer<? super V>> T distinct(final T target, final Equalator<? super V> equalator)
		{
			HashTable.this.chain.valuesDistinct(target, equalator);
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
			return HashTable.this.chain.valuesEqualsContent(samples, equalator);
		}

		@Override
		public final <T extends Consumer<? super V>> T except(final XGettingCollection<? extends V> other, final Equalator<? super V> equalator, final T target)
		{
			HashTable.this.chain.valuesExcept(other, equalator, target);
			return target;
		}


		@Override
		public final boolean hasVolatileElements()
		{
			return HashTable.this.hasVolatileValues();
		}

		@Override
		public final <T extends Consumer<? super V>> T intersect(final XGettingCollection<? extends V> other, final Equalator<? super V> equalator, final T target)
		{
			HashTable.this.chain.valuesIntersect(other, equalator, target);
			return target;
		}

		@Override
		public final boolean isEmpty()
		{
			return HashTable.this.isEmpty();
		}

		@Override
		public final Iterator<V> iterator()
		{
			return HashTable.this.chain.valuesIterator();
		}

		@Override
		public final V max(final Comparator<? super V> comparator)
		{
			return HashTable.this.chain.valuesMax(comparator);
		}

		@Override
		public final V min(final Comparator<? super V> comparator)
		{
			return HashTable.this.chain.valuesMin(comparator);
		}

		@Override
		public final boolean nullAllowed()
		{
			return HashTable.this.nullAllowed();
		}

		@Override
		public final boolean nullContained()
		{
			return HashTable.this.chain.valuesContains(null);
		}

		@Override
		public final OldValues old()
		{
			return new OldValues();
		}

		@Override
		public final V seek(final V sample)
		{
			return HashTable.this.chain.valuesSeek(sample);
		}

		@Override
		public final V search(final Predicate<? super V> predicate)
		{
			return HashTable.this.chain.valuesSearch(predicate);
		}

		@Override
		public final long size()
		{
			return XTypes.to_int(HashTable.this.size());
		}

		@Override
		public final long maximumCapacity()
		{
			return XTypes.to_int(HashTable.this.size());
		}

		@Override
		public final boolean isFull()
		{
			return HashTable.this.isFull();
		}

		@Override
		public final long remainingCapacity()
		{
			return HashTable.this.remainingCapacity();
		}

		@Override
		public final String toString()
		{
			if(HashTable.this.size == 0)
			{
				return "[]"; // array causes problems with escape condition otherwise
			}

			final VarString vc = VarString.New(HashTable.this.slots.length).append('[');
			HashTable.this.chain.valuesAppendTo(vc, ',').append(']');
			return vc.toString();
		}

		@Override
		public final Object[] toArray()
		{
			return HashTable.this.chain.valuesToArray();
		}

		@Override
		public final V[] toArray(final Class<V> type)
		{
			return HashTable.this.chain.valuesToArray(type);
		}

		@Override
		public final <T extends Consumer<? super V>> T union(
			final XGettingCollection<? extends V> other,
			final Equalator<? super V> equalator,
			final T target
		)
		{
			HashTable.this.chain.valuesUnion(other, equalator, target);
			return target;
		}

		@Override
		public final HashTable<K, V> parent()
		{
			return HashTable.this;
		}

		@Override
		public final SubListView<V> view(final long fromIndex, final long toIndex)
		{
			return new SubListView<>(this, fromIndex, toIndex);
		}

		@Override
		public final ListIterator<V> listIterator()
		{
			return HashTable.this.chain.valuesListIterator(0);
		}

		@Override
		public final ListIterator<V> listIterator(final long index)
		{
			return HashTable.this.chain.valuesListIterator(index);
		}

		@Override
		public final SubListProcessor<V> range(final long fromIndex, final long toIndex)
		{
			return new SubListProcessor<>(this, fromIndex, toIndex);
		}

		@Override
		public final XImmutableList<V> immure()
		{
			return ConstList.New(this);
		}

		@Override
		public final ListView<V> view()
		{
			return new ListView<>(this);
		}

		@Override
		public final <T extends Consumer<? super V>> T copySelection(final T target, final long... indices)
		{
			HashTable.this.chain.valuesCopySelection(target, indices);
			return target;
		}

		@Override
		public final V at(final long index)
		{
			return HashTable.this.chain.valuesGet(index);
		}

		@Override
		public final V get()
		{
			return HashTable.this.chain.valuesFirst();
		}

		@Override
		public final V first()
		{
			return HashTable.this.chain.valuesFirst();
		}

		@Override
		public final V last()
		{
			return HashTable.this.chain.valuesLast();
		}

		@Override
		public final V poll()
		{
			return HashTable.this.size == 0 ? null : HashTable.this.chain.valuesFirst();
		}

		@Override
		public final V peek()
		{
			return HashTable.this.size == 0 ? null : HashTable.this.chain.valuesLast();
		}

		@Override
		public final long indexOf(final V value)
		{
			return HashTable.this.chain.valuesIndexOf(value);
		}

		@Override
		public final long indexBy(final Predicate<? super V> predicate)
		{
			return HashTable.this.chain.valuesIndexBy(predicate);
		}

		@Override
		public final boolean isSorted(final Comparator<? super V> comparator)
		{
			return HashTable.this.chain.valuesIsSorted(comparator);
		}

		@Override
		public final long lastIndexOf(final V value)
		{
			return HashTable.this.chain.valuesLastIndexOf(value);
		}

		@Override
		public final long lastIndexBy(final Predicate<? super V> predicate)
		{
			return HashTable.this.chain.valuesLastIndexBy(predicate);
		}

		@Override
		public final long maxIndex(final Comparator<? super V> comparator)
		{
			return HashTable.this.chain.valuesMaxIndex(comparator);
		}

		@Override
		public final long minIndex(final Comparator<? super V> comparator)
		{
			return HashTable.this.chain.valuesMinIndex(comparator);
		}

		@Override
		public final long scan(final Predicate<? super V> predicate)
		{
			return HashTable.this.chain.valuesScan(predicate);
		}

		@Override
		public final <C extends Consumer<? super V>> C moveSelection(final C target, final long... indices)
		{
			HashTable.this.chain.valuesMoveSelection(target, indices);
			return target;
		}

		@Override
		public final V removeAt(final long index)
		{
			return HashTable.this.chain.valuesRemove(index);
		}

		@Override
		public final V fetch()
		{
			return HashTable.this.chain.valuesRemove(0);
		}

		@Override
		public final V pop()
		{
			return HashTable.this.chain.valuesRemove(HashTable.this.size - 1);
		}

		@Override
		public final V pinch()
		{
			return HashTable.this.size == 0 ? null : HashTable.this.chain.valuesRemove(0);
		}

		@Override
		public final V pick()
		{
			return HashTable.this.size == 0 ? null : HashTable.this.chain.valuesRemove(HashTable.this.size - 1);
		}

		@Override
		public final V retrieve(final V value)
		{
			return HashTable.this.chain.valuesRetrieve(value);
		}

		@Override
		public final V retrieveBy(final Predicate<? super V> predicate)
		{
			return HashTable.this.chain.valuesRetrieve(predicate);
		}

		@Override
		public final boolean removeOne(final V element)
		{
			return HashTable.this.chain.valuesRemoveOne(element);
		}

		@Override
		public final Values removeRange(final long startIndex, final long length)
		{
			HashTable.this.chain.removeRange(startIndex, length);
			return this;
		}

		@Override
		public final Values retainRange(final long startIndex, final long length)
		{
			HashTable.this.chain.retainRange(startIndex, length);
			return this;
		}

		@Override
		public final long removeSelection(final long[] indices)
		{
			return HashTable.this.chain.removeSelection(indices);
		}

		@Override
		public final void clear()
		{
			HashTable.this.clear();
		}

		@Override
		public final long consolidate()
		{
			return HashTable.this.consolidate();
		}

		@Override
		public final <C extends Consumer<? super V>> C moveTo(final C target, final Predicate<? super V> predicate)
		{
			HashTable.this.chain.valuesMoveTo(target, predicate);
			return target;
		}

		@Override
		public final long nullRemove()
		{
			return HashTable.this.chain.valuesRemove(null);
		}

		@Override
		public final long optimize()
		{
			return HashTable.this.optimize();
		}

		@Override
		public final <P extends Consumer<? super V>> P process(final P procedure)
		{
			HashTable.this.chain.valuesProcess(procedure);
			return procedure;
		}

		@Override
		public final long removeBy(final Predicate<? super V> predicate)
		{
			return HashTable.this.chain.valuesReduce(predicate);
		}

		@Override
		public final long remove(final V value)
		{
			return HashTable.this.chain.valuesRemove(value);
		}

		@Override
		public final long removeAll(final XGettingCollection<? extends V> values)
		{
			return HashTable.this.chain.valuesRemoveAll(values);
		}

		@Override
		public final long removeDuplicates()
		{
			return HashTable.this.chain.valuesRemoveDuplicates();
		}

		@Override
		public final long removeDuplicates(final Equalator<? super V> equalator)
		{
			return HashTable.this.chain.valuesRemoveDuplicates(equalator);
		}

		@Override
		public final long retainAll(final XGettingCollection<? extends V> values)
		{
			return HashTable.this.chain.valuesRetainAll(values);
		}

		@Override
		public final void truncate()
		{
			HashTable.this.truncate();
		}

		@Override
		public final Values fill(final long offset, final long length, final V value)
		{
			HashTable.this.chain.valuesFill(offset, length, value);
			return this;
		}

		@Override
		public final long replace(final V value, final V replacement)
		{
			return HashTable.this.chain.valuesReplace(value, replacement);
		}

		@Override
		public final long replaceAll(final XGettingCollection<? extends V> values, final V replacement)
		{
			return HashTable.this.chain.valuesReplaceAll(values, replacement);
		}

		@Override
		public final long substitute(final Function<? super V, ? extends V> mapper)
		{
			return HashTable.this.chain.valuesSubstitute(mapper);
		}

		@Override
		public final long substitute(final Predicate<? super V> predicate, final Function<V, V> mapper)
		{
			return HashTable.this.chain.valuesSubstitute(predicate, mapper);
		}

		@Override
		public final boolean replaceOne(final V value, final V replacement)
		{
			return HashTable.this.chain.valuesReplaceOne(value, replacement);
		}

		@Override
		public final Values reverse()
		{
			HashTable.this.chain.reverse();
			return this;
		}

		@Override
		public final boolean set(final long index, final V value)
		{
			return HashTable.this.chain.valuesSet(index, value) == value;
		}

		@Override
		public final V setGet(final long index, final V value)
		{
			return HashTable.this.chain.valuesSet(index, value);
		}

		@Override
		public final Values setAll(final long offset, @SuppressWarnings("unchecked") final V... values)
		{
			HashTable.this.chain.valuesSet(offset, values);
			return this;
		}

		@Override
		public final Values set(final long offset, final V[] src, final int srcIndex, final int srcLength)
		{
			HashTable.this.chain.valuesSet(offset, src, srcIndex, srcLength);
			return this;
		}

		@Override
		public final Values set(final long offset, final XGettingSequence<? extends V> values, final long valuesOffset, final long valuesLength)
		{
			// (23.01.2017 TM)NOTE: copyTo() removed. No time for a replacement, atm.
			throw new one.microstream.meta.NotImplementedYetError(); // FIXME HashTable.Values#set()

		}

		@Override
		public final void setFirst(final V value)
		{
			HashTable.this.chain.valuesSet(0, value);
		}

		@Override
		public final void setLast(final V value)
		{
			HashTable.this.chain.valuesSet(HashTable.this.size - 1, value);
		}

		@Override
		public final Values sort(final Comparator<? super V> comparator)
		{
			HashTable.this.chain.valuesSort(comparator);
			return this;
		}

		@Override
		public final long replace(final Predicate<? super V> predicate, final V substitute)
		{
			return HashTable.this.chain.valuesSubstitute(predicate, substitute);
		}

		@Override
		public final boolean replaceOne(final Predicate<? super V> predicate, final V substitute)
		{
			return HashTable.this.chain.valuesSubstituteOne(predicate, substitute);
		}

		@Override
		public final Values shiftTo(final long sourceIndex, final long targetIndex)
		{
			HashTable.this.chain.shiftTo(sourceIndex, targetIndex);
			return this;
		}

		@Override
		public final Values shiftTo(final long sourceIndex, final long targetIndex, final long length)
		{
			HashTable.this.chain.shiftTo(sourceIndex, targetIndex, length);
			return this;
		}

		@Override
		public final Values shiftBy(final long sourceIndex, final long distance)
		{
			HashTable.this.chain.shiftTo(sourceIndex, distance);
			return this;
		}

		@Override
		public final Values shiftBy(final long sourceIndex, final long distance, final long length)
		{
			HashTable.this.chain.shiftTo(sourceIndex, distance, length);
			return this;
		}

		@Override
		public final Values swap(final long indexA, final long indexB)
		{
			HashTable.this.chain.swap(indexA, indexB);
			return this;
		}

		@Override
		public final Values swap(final long indexA, final long indexB, final long length)
		{
			HashTable.this.chain.swap(indexA, indexB, length);
			return this;
		}

		public final class OldValues extends AbstractOldSettingList<V>
		{
			protected OldValues()
			{
				super(Values.this);
			}

			@Override
			public final Values parent()
			{
				return (Values)super.parent();
			}

		}

	}



	public final class OldVarMap implements XTable.Bridge<K, V>
	{

		@Override
		public final void clear()
		{
			HashTable.this.clear();
		}

		@SuppressWarnings("unchecked")
		@Override
		public final boolean containsKey(final Object key)
		{
			try
			{
				return HashTable.this.containsKey((K)key);
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
				return HashTable.this.chain.valuesContains((V)value);
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
			 * So this dirty but architectural clean workaround is used.
			 */
			return (Set<java.util.Map.Entry<K, V>>)(Set<?>)HashTable.this.old();
		}

		@SuppressWarnings("unchecked")
		@Override
		public final V get(final Object key)
		{
			try
			{
				return HashTable.this.get((K)key);
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
			return HashTable.this.isEmpty();
		}

		@Override
		public final Set<K> keySet()
		{
			return HashTable.this.keys().old();
		}

		@Override
		public final V put(final K key, final V value)
		{
			return HashTable.this.oldPutGet(key, value);
		}

		@SuppressWarnings("unchecked")
		@Override
		public final void putAll(final Map<? extends K, ? extends V> m)
		{
			if(m instanceof XGettingMap.Bridge<?, ?>)
			{
				HashTable.this.addAll(((XGettingMap.Bridge<K, V>)m).parent());
				return;
			}

			final HashTable<K, V> parent = HashTable.this;
			for(final Map.Entry<? extends K, ? extends V> entry : m.entrySet())
			{
				parent.put(entry.getKey(), entry.getValue());
			}
		}

		@SuppressWarnings("unchecked")
		@Override
		public final V remove(final Object key)
		{
			try
			{
				return HashTable.this.removeFor((K)key);
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
		public final int size()
		{
			return XTypes.to_int(HashTable.this.size());
		}

		@Override
		public final Collection<V> values()
		{
			return HashTable.this.values.old(); // hehehe
		}

		@Override
		public final HashTable<K, V> parent()
		{
			return HashTable.this;
		}

	}

}

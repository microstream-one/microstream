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
import one.microstream.exceptions.ArrayCapacityException;
import one.microstream.functional.IndexedAcceptor;
import one.microstream.hashing.HashEqualator;
import one.microstream.hashing.XHashing;
import one.microstream.math.XMath;
import one.microstream.typing.Composition;
import one.microstream.typing.KeyValue;
import one.microstream.typing.XTypes;


/* (12.07.2012 TM) FIXME: complete EqHashTable implementation
 * See all not implemented errors in method stubs
 */
/**
 * Collection of key-value-pairs that is ordered and does not allow duplicate keys.
 * Aims to be more efficient, logically structured
 * and with more built-in features than {@link java.util.Map}.
 * <p>
 * Additional to the {@link HashTable}, this implementation needs an {@link HashEqualator}
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
 * @param <K> type of contained keys
 * @param <V> type of contained values
 */
public final class EqHashTable<K, V>
extends AbstractChainKeyValueCollection<K, V, ChainMapEntryLinkedHashedStrongStrong<K, V>>
implements XTable<K, V>, HashCollection<K>, Composition
{
	public interface Creator<K, V>
	{
		public EqHashTable<K, V> newInstance();
	}



	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////

	public static final <K, V> EqHashTable<K, V> New()
	{
		return new EqHashTable<>(
			DEFAULT_HASH_LENGTH,
			DEFAULT_HASH_FACTOR,
			XHashing.hashEqualityValue()
		);
	}

	public static final <K, V> EqHashTable<K, V> NewCustom(
		final int              initialHashLength
	)
	{
		return new EqHashTable<>(
			XHashing.padHashLength(initialHashLength),
			DEFAULT_HASH_FACTOR,
			XHashing.hashEqualityValue()
		);
	}

	public static final <K, V> EqHashTable<K, V> NewCustom(
		final float            hashDensity
	)
	{
		return new EqHashTable<>(
			DEFAULT_HASH_LENGTH,
			XHashing.validateHashDensity(hashDensity),
			XHashing.hashEqualityValue()
		);
	}

	public static final <K, V> EqHashTable<K, V> NewCustom(
		final int              initialHashLength,
		final float            hashDensity
	)
	{
		return new EqHashTable<>(
			XHashing.padHashLength(initialHashLength),
			XHashing.validateHashDensity(hashDensity),
			XHashing.hashEqualityValue()
		);
	}
	public static final <K, V> EqHashTable<K, V> New(
		final XGettingCollection<? extends KeyValue<? extends K, ? extends V>> entries
	)
	{
		return EqHashTable.<K, V>New()
			.internalAddEntries(entries)
		;
	}

	public static final <K, V> EqHashTable<K, V> NewCustom(
		final int              initialHashLength,
		final float            hashDensity      ,
		final XGettingCollection<? extends KeyValue<? extends K, ? extends V>> entries
	)
	{
		return new EqHashTable<K, V>(
			XHashing.padHashLength(initialHashLength),
			XHashing.validateHashDensity(hashDensity),
			XHashing.hashEqualityValue()
		).internalAddEntries(entries);
	}

	public static final <K, V> EqHashTable<K, V> NewSingle(final K key, final V value)
	{
		final EqHashTable<K, V> instance = New();
		instance.internalAdd(key, value);
		return instance;
	}

	@SafeVarargs
	public static final <K, V> EqHashTable<K, V> New(
		final KeyValue<? extends K, ? extends V>... entries
	)
	{
		return EqHashTable.<K, V>New()
			.internalAddEntries(new ArrayView<>(entries))
		;
	}

	@SafeVarargs
	public static final <K, V> EqHashTable<K, V> NewCustom(
		final int                                   initialHashLength,
		final float                                 hashDensity      ,
		final KeyValue<? extends K, ? extends V>... entries
	)
	{
		return new EqHashTable<K, V>(
			XHashing.padHashLength(initialHashLength),
			XHashing.validateHashDensity(hashDensity),
			XHashing.hashEqualityValue()
		).internalAddEntries(new ArrayView<>(entries));
	}

	public static final <K, V> EqHashTable<K, V> New(
		final HashEqualator<? super K> hashEqualator
	)
	{
		return new EqHashTable<>(
			DEFAULT_HASH_LENGTH,
			DEFAULT_HASH_FACTOR,
			notNull(hashEqualator)
		);
	}

	public static final <K, V> EqHashTable<K, V> NewCustom(
		final HashEqualator<? super K> hashEqualator    ,
		final int              initialHashLength
	)
	{
		return new EqHashTable<>(
			XHashing.padHashLength(initialHashLength),
			DEFAULT_HASH_FACTOR,
			notNull(hashEqualator)
		);
	}

	public static final <K, V> EqHashTable<K, V> NewCustom(
		final HashEqualator<? super K> hashEqualator,
		final float            hashDensity
	)
	{
		return new EqHashTable<>(
			DEFAULT_HASH_LENGTH,
			XHashing.validateHashDensity(hashDensity),
			notNull(hashEqualator)
		);
	}

	public static final <K, V> EqHashTable<K, V> NewCustom(
		final HashEqualator<? super K> hashEqualator    ,
		final int              initialHashLength,
		final float            hashDensity
	)
	{
		return new EqHashTable<>(
			XHashing.padHashLength(initialHashLength),
			XHashing.validateHashDensity(hashDensity),
			notNull(hashEqualator)
		);
	}
	
	public static final <K, V> EqHashTable<K, V> New(
		final HashEqualator<? super K> hashEqualator,
		final XGettingCollection<? extends KeyValue<? extends K, ? extends V>> entries
	)
	{
		return new EqHashTable<K, V>(
			DEFAULT_HASH_LENGTH,
			DEFAULT_HASH_FACTOR,
			notNull(hashEqualator)
		).internalAddEntries(entries);
	}

	public static final <K, V> EqHashTable<K, V> NewCustom(
		final HashEqualator<? super K> hashEqualator    ,
		final int              initialHashLength,
		final float            hashDensity      ,
		final XGettingCollection<? extends KeyValue<? extends K, ? extends V>> entries
	)
	{
		return new EqHashTable<K, V>(
			XHashing.padHashLength(initialHashLength),
			XHashing.validateHashDensity(hashDensity),
			notNull(hashEqualator)
		).internalAddEntries(entries);
	}

	@SafeVarargs
	public static final <K, V> EqHashTable<K, V> New(
		final HashEqualator<? super K>              hashEqualator,
		final KeyValue<? extends K, ? extends V>... entries
	)
	{
		return new EqHashTable<K, V>(
			DEFAULT_HASH_LENGTH,
			DEFAULT_HASH_FACTOR,
			notNull(hashEqualator)
		).internalAddEntries(new ArrayView<>(entries));
	}

	@SafeVarargs
	public static final <K, V> EqHashTable<K, V> NewCustom(
		final HashEqualator<? super K>              hashEqualator    ,
		final int                                   initialHashLength,
		final float                                 hashDensity      ,
		final KeyValue<? extends K, ? extends V>... entries
	)
	{
		return new EqHashTable<K, V>(
			XHashing.padHashLength(initialHashLength),
			XHashing.validateHashDensity(hashDensity),
			notNull(hashEqualator)
		).internalAddEntries(new ArrayView<>(entries));
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

	private EqHashTable(final EqHashTable<K, V> original)
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

	private EqHashTable(
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

	final boolean internalAddOnlyKey(final K key)
	{
		final int hash;
		for(ChainMapEntryLinkedHashedStrongStrong<K, V> e = this.slots[(hash = this.hashEqualator.hash(key)) & this.range]; e != null; e = e.link)
		{
			if(e.hash == hash && this.hashEqualator.equal(e.key(), key))
			{
				return false; // already contained
			}
		}
		this.chain.appendEntry(this.createNewEntry(hash, key, null));
		return true;
	}

	final boolean internalPutOnlyKey(final K key)
	{
		final int hash;
		for(ChainMapEntryLinkedHashedStrongStrong<K, V> e = this.slots[(hash = this.hashEqualator.hash(key)) & this.range]; e != null; e = e.link)
		{
			if(e.hash == hash && this.hashEqualator.equal(e.key(), key))
			{
				e.setKey(key); // intentionally no moving to end here to cleanly separate concerns
				return false;
			}
		}
		this.chain.appendEntry(this.createNewEntry(hash, key, null));
		return true;
	}

	final K internalPutGetKey(final K key)
	{
		final int hash;
		for(ChainMapEntryLinkedHashedStrongStrong<K, V> e = this.slots[(hash = this.hashEqualator.hash(key)) & this.range]; e != null; e = e.link)
		{
			if(e.hash == hash && this.hashEqualator.equal(e.key(), key))
			{
				return e.setKey(key);
			}
		}
		this.chain.appendEntry(this.createNewEntry(hash, key, null));
		return null;
	}

	final K internalAddGetKey(final K key)
	{
		final int hash;
		for(ChainMapEntryLinkedHashedStrongStrong<K, V> e = this.slots[(hash = this.hashEqualator.hash(key)) & this.range]; e != null; e = e.link)
		{
			if(e.hash == hash && this.hashEqualator.equal(e.key(), key))
			{
				return e.key();
			}
		}
		this.chain.appendEntry(this.createNewEntry(hash, key, null));
		
		return null;
	}

	final K internalReplaceKey(final K key)
	{
		final int hash;
		for(ChainMapEntryLinkedHashedStrongStrong<K, V> e = this.slots[(hash = this.hashEqualator.hash(key)) & this.range]; e != null; e = e.link)
		{
			if(e.hash == hash && this.hashEqualator.equal(e.key(), key))
			{
				return e.setKey(key);
			}
		}

		return null;
	}
	
	final K internalSubstituteKey(final K key)
	{
		final int hash;
		for(ChainMapEntryLinkedHashedStrongStrong<K, V> e = this.slots[(hash = this.hashEqualator.hash(key)) & this.range]; e != null; e = e.link)
		{
			if(e.hash == hash && this.hashEqualator.equal(e.key(), key))
			{
				return e.key = key;
			}
		}
		this.chain.appendEntry(this.createNewEntry(hash, key, null));
		
		return null;
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

	final EqHashTable<K, V> internalAddEntries(final XGettingCollection<? extends KeyValue<? extends K, ? extends V>> entries)
	{
		entries.iterate(new Consumer<KeyValue<? extends K, ? extends V>>()
		{
			@Override
			public void accept(final KeyValue<? extends K, ? extends V> e)
			{
				EqHashTable.this.internalAdd(e.key(), e.value());
			}
		});
		return this;
	}


	// only used for backwards compatibility with old collections
	final V oldPutGet(final K key, final V value)
	{
		final int hash;
		for(ChainMapEntryLinkedHashedStrongStrong<K, V> e = this.slots[(hash = this.hashEqualator.hash(key)) & this.range]; e != null; e = e.link)
		{
			if(e.hash == hash && this.hashEqualator.equal(e.key(), key))
			{
				// set only value, not key, according to inconsistent nonsense behavior in old collections
				return e.setValue(value);
			}
		}
		this.chain.appendEntry(this.createNewEntry(hash, key, value));
		return null;
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

	final int removeKey(final K key)
	{
		final int hash = this.hashEqualator.hash(key);
		ChainMapEntryLinkedHashedStrongStrong<K, V> last, e;
		if((e = this.slots[hash & this.range]) == null)
		{
			return 0;
		}

		// head entry special case
		if(e.hash == hash && this.hashEqualator.equal(e.key(), key))
		{
			this.slots[hash & this.range] = e.link;
			this.chain.disjoinEntry(e);
			this.size--;
			return 1; // return as key can only be contained once in a set
		}

		// search entry chain
		for(e = (last = e).link; e != null; e = (last = e).link)
		{
			if(e.hash == hash && this.hashEqualator.equal(e.key(), key))
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

	final void internalCollectUnhashed(final K key, final V value)
	{
		this.chain.appendEntry(new ChainMapEntryLinkedHashedStrongStrong<>(0, key, value, null));
	}
	
	final void replace(final ChainMapEntryLinkedHashedStrongStrong<K, V> oldEntry, final K newElement)
	{
		final int newHash = this.hashEqualator.hash(newElement);
		for(ChainMapEntryLinkedHashedStrongStrong<K, V> e = this.slots[newHash & this.range]; e != null; e = e.link)
		{
			if(e.hash == newHash && this.hashEqualator.equal(e.key, newElement))
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
				if(EqHashTable.this.add(e))
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
				if(EqHashTable.this.put(e))
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
	protected void internalRemoveEntry(final ChainMapEntryLinkedHashedStrongStrong<K, V> entry)
	{
		final ChainMapEntryLinkedHashedStrongStrong<K, V> setEntry = entry;
		ChainMapEntryLinkedHashedStrongStrong<K, V> last, e = this.slots[setEntry.hash & this.range];

		// remove entry from hashing chain
		if(e == setEntry)
		{
			// head entry special case
			this.slots[setEntry.hash & this.range] = setEntry.link;
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
	protected AbstractChainKeyValueStorage<K, V, ChainMapEntryLinkedHashedStrongStrong<K, V>> getInternalStorageChain()
	{
		return this.chain;
	}

	@Override
	public final long size()
	{
		return EqHashTable.this.size;
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
		final ChainMapEntryLinkedHashedStrongStrong<K, V>[] slots = this.slots;
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
		this.slots = ChainMapEntryLinkedHashedStrongStrong.array(DEFAULT_HASH_LENGTH);
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
	public final int rehash()
	{
		// local helper variables, including capacity recalculation while at rebuilding anyway
		final int                                          reqCapacity   = XHashing.padHashLength((int)(this.size / this.hashDensity));
		final ChainMapEntryLinkedHashedStrongStrong<K, V>[] slots         = ChainMapEntryLinkedHashedStrongStrong.<K, V>array(reqCapacity);
		final int                                          range         = reqCapacity >= Integer.MAX_VALUE ? Integer.MAX_VALUE : reqCapacity - 1;
		final HashEqualator<? super K>                     hashEqualator = this.hashEqualator;
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
	public final EqHashTable<K, V> copy()
	{
		final EqHashTable<K, V> newVarMap = new EqHashTable<>(this);
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
	public final EqConstHashTable<K, V> immure()
	{
		this.consolidate();
		return EqConstHashTable.NewCustom(this.hashEqualator, this.size, this.hashDensity, this);
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
	public final EqHashTable<K, V>.Keys keys()
	{
		return this.keys;
	}

	@Override
	public final XTable.EntriesBridge<K, V> old()
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME EqHashTable#old()
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
				collector.accept(EqHashTable.this.get(key));
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
		final int hash;
		for(ChainMapEntryLinkedHashedStrongStrong<K, V> e = this.slots[(hash = this.hashEqualator.hash(key)) & this.range]; e != null; e = e.link)
		{
			if(e.hash == hash && this.hashEqualator.equal(e.key(), key))
			{
				return e;
			}
		}
		this.chain.appendEntry(this.createNewEntry(hash, key, value));
		
		return null;
	}
	
	@Override
	public KeyValue<K, V> substitute(final K key, final V value)
	{
		final int hash;
		for(ChainMapEntryLinkedHashedStrongStrong<K, V> e = this.slots[(hash = this.hashEqualator.hash(key)) & this.range]; e != null; e = e.link)
		{
			if(e.hash == hash && this.hashEqualator.equal(e.key(), key))
			{
				return e;
			}
		}
		this.chain.appendEntry(this.createNewEntry(hash, key, value));
		
		return X.KeyValue(key, value);
	}
	
	@Override
	public KeyValue<K, V> deduplicate(final KeyValue<K, V> entry)
	{
		// can't delegate because the passed instance shall be returned, not a newly created one
		final int hash;
		for(ChainMapEntryLinkedHashedStrongStrong<K, V> e = this.slots[(hash = this.hashEqualator.hash(entry.key())) & this.range]; e != null; e = e.link)
		{
			if(e.hash == hash && this.hashEqualator.equal(e.key(), entry.key()))
			{
				return e;
			}
		}
		this.chain.appendEntry(this.createNewEntry(hash, entry.key(), entry.value()));
		
		return entry;
	}

	@Override
	public final KeyValue<K, V> putGet(final K key, final V value)
	{
		final int hash;
		for(ChainMapEntryLinkedHashedStrongStrong<K, V> e = this.slots[(hash = this.hashEqualator.hash(key)) & this.range]; e != null; e = e.link)
		{
			if(e.hash == hash && this.hashEqualator.equal(e.key(), key))
			{
				return X.KeyValue(e.setKey(key), e.setValue(value));
			}
		}
		this.chain.appendEntry(this.createNewEntry(hash, key, value));
		
		return null;
	}
	
	@Override
	public KeyValue<K, V> replace(final K key, final V value)
	{
		final int hash;
		for(ChainMapEntryLinkedHashedStrongStrong<K, V> e = this.slots[(hash = this.hashEqualator.hash(key)) & this.range]; e != null; e = e.link)
		{
			if(e.hash == hash && this.hashEqualator.equal(e.key(), key))
			{
				return X.KeyValue(e.setKey(key), e.setValue(value));
			}
		}

		return null;
	}

	@Override
	public final KeyValue<K, V> replace(final KeyValue<K, V> entry)
	{
		return this.replace(entry.key(), entry.value());
	}

	@Override
	public final KeyValue<K, V> setGet(final K key, final V value)
	{
		final int hash;
		for(ChainMapEntryLinkedHashedStrongStrong<K, V> e = this.slots[(hash = this.hashEqualator.hash(key)) & this.range]; e != null; e = e.link)
		{
			if(e.hash == hash && this.hashEqualator.equal(e.key(), key))
			{
				return X.KeyValue(e.setKey(key), e.setValue(value));
			}
		}
		return null;
	}

	@Override
	public final boolean add(final K key, final V value)
	{
		final int hash;
		for(ChainMapEntryLinkedHashedStrongStrong<K, V> e = this.slots[(hash = this.hashEqualator.hash(key)) & this.range]; e != null; e = e.link)
		{
			if(e.hash == hash && this.hashEqualator.equal(e.key(), key))
			{
				return false; // already contained
			}
		}
		this.chain.appendEntry(this.createNewEntry(hash, key, value));
		return true;
	}

	@Override
	public final boolean put(final K key, final V value)
	{
		final int hash;
		for(ChainMapEntryLinkedHashedStrongStrong<K, V> e = this.slots[(hash = this.hashEqualator.hash(key)) & this.range]; e != null; e = e.link)
		{
			if(e.hash == hash && this.hashEqualator.equal(e.key(), key))
			{
				e.set0(key, value); // intentionally no moving to end here to cleanly separate concerns
				return false;
			}
		}
		this.chain.appendEntry(this.createNewEntry(hash, key, value));
		return true;
	}

	@Override
	public final boolean set(final K key, final V value)
	{
		final int hash;
		for(ChainMapEntryLinkedHashedStrongStrong<K, V> e = this.slots[(hash = this.hashEqualator.hash(key)) & this.range]; e != null; e = e.link)
		{
			if(e.hash == hash && this.hashEqualator.equal(e.key(), key))
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
		final int hash;
		for(ChainMapEntryLinkedHashedStrongStrong<K, V> e = this.slots[(hash = this.hashEqualator.hash(key)) & this.range]; e != null; e = e.link)
		{
			if(e.hash == hash && this.hashEqualator.equal(e.key(), key))
			{
				e.setValue0(value);
				return false;
			}
		}
		this.chain.appendEntry(this.createNewEntry(hash, key, value));
		return true;
	}

	@Override
	public final boolean valueSet(final K key, final V value)
	{
		final int hash;
		for(ChainMapEntryLinkedHashedStrongStrong<K, V> e = this.slots[(hash = this.hashEqualator.hash(key)) & this.range]; e != null; e = e.link)
		{
			if(e.hash == hash && this.hashEqualator.equal(e.key(), key))
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
		final int hash;
		for(ChainMapEntryLinkedHashedStrongStrong<K, V> e = this.slots[(hash = this.hashEqualator.hash(key)) & this.range]; e != null; e = e.link)
		{
			if(e.hash == hash && this.hashEqualator.equal(e.key(), key))
			{
				return e.setValue(value);
			}
		}
		this.chain.appendEntry(this.createNewEntry(hash, key, value));
		return null;
	}

	@Override
	public final V valueSetGet(final K key, final V value)
	{
		final int hash;
		for(ChainMapEntryLinkedHashedStrongStrong<K, V> e = this.slots[(hash = this.hashEqualator.hash(key)) & this.range]; e != null; e = e.link)
		{
			if(e.hash == hash && this.hashEqualator.equal(e.key(), key))
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
		ChainMapEntryLinkedHashedStrongStrong<K, V> last, e;
		if((e = this.slots[(hash = this.hashEqualator.hash(key)) & this.range]) == null)
		{
			return null;
		}

		// head entry special case
		if(e.hash == hash && this.hashEqualator.equal(e.key(), key))
		{
			this.slots[hash & this.range] = e.link;
			this.chain.disjoinEntry(e);
			this.size--;
			return e.value(); // return as value can only be contained once in a set
		}

		// search entry chain
		for(e = (last = e).link; e != null; e = (last = e).link)
		{
			if(e.hash == hash && this.hashEqualator.equal(e.key(), key))
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
	public final HashCollection.Analysis<EqHashTable<K, V>> analyze()
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

	public final Consumer<KeyValue<K, V>> procedureRemoveEntry()
	{
		return entry ->
		{
			this.removeKey(entry.key());
		};
	}

	public final Predicate<KeyValue<K, V>> predicateContainsEntry()
	{
		return entry ->
		{
			final KeyValue<K, V> kv;
			if((kv = this.lookup(entry.key())) == null)
			{
				return false;
			}

			// equality of values is architectural restricted to simple referential equality
			return this.hashEqualator.equal(kv.key(), entry.key()) && kv.value() == entry.value();
		};
	}

	@Override
	public final EqHashTable<K, V> sort(final Comparator<? super KeyValue<K, V>> comparator)
	{
		this.chain.sort(comparator);
		return this;
	}

	@Override
	public long substitute(final Function<? super KeyValue<K, V>, ? extends KeyValue<K, V>> mapper)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME EqHashTable#substitute()
	}
		


	///////////////////////////////////////////////////////////////////////////
	// getting methods //
	////////////////////

	@Override
	public final XEnum<KeyValue<K, V>> range(final long lowIndex, final long highIndex)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME EqHashTable.Entries#range()
	}

	@Override
	public final XGettingEnum<KeyValue<K, V>> view(final long lowIndex, final long highIndex)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME EqHashTable.Entries#view()
	}

	@Override
	public final KeyValue<K, V>[] toArray(final Class<KeyValue<K, V>> type)
	{
		return EqHashTable.this.chain.toArray(type);
	}

	// executing //

	@Override
	public final <P extends Consumer<? super KeyValue<K, V>>> P iterate(final P procedure)
	{
		EqHashTable.this.chain.iterate(procedure);
		return procedure;
	}

	@Override
	public final <A> A join(final BiConsumer<? super KeyValue<K, V>, ? super A> joiner, final A aggregate)
	{
		EqHashTable.this.chain.join(joiner, aggregate);
		return aggregate;
	}

	@Override
	public final long count(final KeyValue<K, V> entry)
	{
		return EqHashTable.this.chain.count(entry, this.equality());
	}

	@Override
	public final long countBy(final Predicate<? super KeyValue<K, V>> predicate)
	{
		return EqHashTable.this.chain.count(predicate);
	}

	// element querying //

	@Override
	public final KeyValue<K, V> search(final Predicate<? super KeyValue<K, V>> predicate)
	{
		return EqHashTable.this.chain.search(predicate);
	}

	@Override
	public final KeyValue<K, V> max(final Comparator<? super KeyValue<K, V>> comparator)
	{
		return EqHashTable.this.chain.max(comparator);
	}

	@Override
	public final KeyValue<K, V> min(final Comparator<? super KeyValue<K, V>> comparator)
	{
		return EqHashTable.this.chain.min(comparator);
	}

	// boolean querying - applies //

	@Override
	public final boolean containsSearched(final Predicate<? super KeyValue<K, V>> predicate)
	{
		return EqHashTable.this.chain.containsSearched(predicate);
	}

	@Override
	public final boolean applies(final Predicate<? super KeyValue<K, V>> predicate)
	{
		return EqHashTable.this.chain.appliesAll(predicate);
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
		for(ChainMapEntryLinkedHashedStrongStrong<K, V> e = EqHashTable.this.slots[(hash = EqHashTable.this.hashEqualator.hash(entry.key())) & EqHashTable.this.range]; e != null; e = e.link)
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
		for(ChainMapEntryLinkedHashedStrongStrong<K, V> e = EqHashTable.this.slots[(hash = EqHashTable.this.hashEqualator.hash(entry.key())) & EqHashTable.this.range]; e != null; e = e.link)
		{
			if(e.hash == hash && EqHashTable.this.hashEqualator.equal(e.key(), entry.key()))
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
		for(ChainMapEntryLinkedHashedStrongStrong<K, V> e = this.slots[(hash = this.hashEqualator.hash(sample.key())) & this.range]; e != null; e = e.link)
		{
			if(e.hash == hash && this.hashEqualator.equal(e.key(), sample.key()))
			{
				return e;
			}
		}
		return null;
	}

	@Override
	public final boolean containsAll(final XGettingCollection<? extends KeyValue<K, V>> elements)
	{
		return elements.applies(EqHashTable.this.predicateContainsEntry());
	}

	// boolean querying - equality //

	@Override
	public final boolean equals(final XGettingCollection<? extends KeyValue<K, V>> samples, final Equalator<? super KeyValue<K, V>> equalator)
	{
		if(samples == null || !(samples instanceof EqHashTable<?, ?>.Keys))
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
		if(EqHashTable.this.size != XTypes.to_int(samples.size()))
		{
			return false;
		}

		// if sizes are equal and all elements of collection are contained in this set, they must have equal content
		return EqHashTable.this.chain.equalsContent(samples, equalator);
	}

	// data set procedures //

	@Override
	public final <C extends Consumer<? super KeyValue<K, V>>> C intersect(
		final XGettingCollection<? extends KeyValue<K, V>> other,
		final Equalator<? super KeyValue<K, V>> equalator,
		final C target
	)
	{
		return EqHashTable.this.chain.intersect(other, equalator, target);
	}

	@Override
	public final <C extends Consumer<? super KeyValue<K, V>>> C except(
		final XGettingCollection<? extends KeyValue<K, V>> other,
		final Equalator<? super KeyValue<K, V>> equalator,
		final C target
	)
	{
		return EqHashTable.this.chain.except(other, equalator, target);
	}

	@Override
	public final <C extends Consumer<? super KeyValue<K, V>>> C union(
		final XGettingCollection<? extends KeyValue<K, V>> other,
		final Equalator<? super KeyValue<K, V>> equalator,
		final C target
	)
	{
		return EqHashTable.this.chain.union(other, equalator, target);
	}

	@Override
	public final <C extends Consumer<? super KeyValue<K, V>>> C copyTo(final C target)
	{
		if(target == this)
		{
			return target; // copying a set logic collection to itself would be a no-op, so spare the effort
		}
		return EqHashTable.this.chain.copyTo(target);
	}

	@Override
	public final <C extends Consumer<? super KeyValue<K, V>>> C filterTo(final C target, final Predicate<? super KeyValue<K, V>> predicate)
	{
		return this.chain.copyTo(target, predicate);
	}

	@Override
	public final <C extends Consumer<? super KeyValue<K, V>>> C distinct(final C target)
	{
		return this.distinct(target, this.equality());
	}

	@Override
	public final <C extends Consumer<? super KeyValue<K, V>>> C distinct(final C target, final Equalator<? super KeyValue<K, V>> equalator)
	{
		return this.chain.distinct(target, equalator);
	}

	@Override
	public final boolean nullAdd()
	{
		return this.nullKeyAdd();
	}

	@Override
	public final boolean add(final KeyValue<K, V> entry)
	{
		return this.add(entry.key(), entry.value());

	}

	@SafeVarargs
	@Override
	public final EqHashTable<K, V> addAll(final KeyValue<K, V>... elements)
	{
		final EqHashTable<K, V> parent = EqHashTable.this;
		for(int i = 0, len = elements.length; i < len; i++)
		{
			parent.add(elements[i].key(), elements[i].value());
		}
		return this;
	}

	@Override
	public final EqHashTable<K, V> addAll(final KeyValue<K, V>[] elements, final int srcIndex, final int srcLength)
	{
		final int d;
		if((d = XArrays.validateArrayRange(elements, srcIndex, srcLength)) == 0)
		{
			return this;
		}

		final int bound = srcIndex + srcLength;
		final EqHashTable<K, V> parent = EqHashTable.this;
		for(int i = srcIndex; i != bound; i += d)
		{
			parent.add(elements[i].key(), elements[i].value());
		}

		return this;
	}

	@Override
	public final EqHashTable<K, V> addAll(final XGettingCollection<? extends KeyValue<K, V>> elements)
	{
		elements.iterate(this::add);
		return this;
	}

	@Override
	public final boolean nullPut()
	{
		return EqHashTable.this.nullKeyPut();
	}

	@Override
	public final void accept(final KeyValue<K, V> entry)
	{
		EqHashTable.this.put(entry.key(), entry.value());
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * In this implementation it overwrites the equal, already contained entry.
	 * A return value indicates a new entry.
	 */
	@Override
	public final boolean put(final KeyValue<K, V> entry)
	{
		return EqHashTable.this.put(entry.key(), entry.value());
	}

	@Override
	public final KeyValue<K, V> putGet(final KeyValue<K, V> entry)
	{
		return EqHashTable.this.putGet(entry.key(), entry.value());
	}

	@Override
	public final KeyValue<K, V> addGet(final KeyValue<K, V> entry)
	{
		return EqHashTable.this.addGet(entry.key(), entry.value());
	}

	@SafeVarargs
	@Override
	public final EqHashTable<K, V> putAll(final KeyValue<K, V>... elements)
	{
		final EqHashTable<K, V> parent = EqHashTable.this;
		for(int i = 0, len = elements.length; i < len; i++)
		{
			parent.put(elements[i].key(), elements[i].value());
		}
		return this;
	}

	@Override
	public final EqHashTable<K, V> putAll(final KeyValue<K, V>[] elements, final int srcIndex, final int srcLength)
	{
		final int d;
		if((d = XArrays.validateArrayRange(elements, srcIndex, srcLength)) == 0)
		{
			return this;
		}

		final int bound = srcIndex + srcLength;
		final EqHashTable<K, V> parent = EqHashTable.this;
		for(int i = srcIndex; i != bound; i += d)
		{
			parent.put(elements[i].key(), elements[i].value());
		}

		return this;
	}

	@Override
	public final EqHashTable<K, V> putAll(final XGettingCollection<? extends KeyValue<K, V>> elements)
	{
		return elements.iterate(this);
	}

	// removing //

	@Override
	public final long remove(final KeyValue<K, V> entry)
	{
		return EqHashTable.this.chain.remove(entry, this.equality());
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
		return EqHashTable.this.chain.reduce(predicate);
	}

	// retaining //

	@Override
	public final long retainAll(final XGettingCollection<? extends KeyValue<K, V>> elements)
	{
		return EqHashTable.this.chain.retainAll(elements, this.equality());
	}

	@Override
	public final <P extends Consumer<? super KeyValue<K, V>>> P process(final P procedure)
	{
		EqHashTable.this.chain.process(procedure);
		return procedure;
	}

	@Override
	public final <C extends Consumer<? super KeyValue<K, V>>> C moveTo(final C target, final Predicate<? super KeyValue<K, V>> predicate)
	{
		EqHashTable.this.chain.moveTo(target, predicate);
		return target;
	}

	// removing - all //

	@Override
	public final long removeAll(final XGettingCollection<? extends KeyValue<K, V>> elements)
	{
		final int oldSize = EqHashTable.this.size;
		elements.iterate(EqHashTable.this.procedureRemoveEntry());
		return oldSize - EqHashTable.this.size;
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
		return EqHashTable.this.chain.removeDuplicates(equalator);
	}

	@Override
	public final EqHashTable<K, V> toReversed()
	{
		final EqHashTable<K, V> reversedVarSet = EqHashTable.this.copy();
		reversedVarSet.chain.reverse();
		return reversedVarSet;
	}

	@Override
	public final <T extends Consumer<? super KeyValue<K, V>>> T copySelection(final T target, final long... indices)
	{
		EqHashTable.this.chain.copySelection(target, indices);
		return target;
	}

	@Override
	public final <P extends IndexedAcceptor<? super KeyValue<K, V>>> P iterateIndexed(final P procedure)
	{
		EqHashTable.this.chain.iterateIndexed(procedure);
		return procedure;
	}

	@Override
	public final KeyValue<K, V> at(final long index)
	{
		return EqHashTable.this.chain.get(index);
	}

	@Override
	public final KeyValue<K, V> get()
	{
		return EqHashTable.this.chain.first();
	}

	@Override
	public final KeyValue<K, V> first()
	{
		return EqHashTable.this.chain.first();
	}

	@Override
	public final KeyValue<K, V> last()
	{
		return EqHashTable.this.chain.last();
	}

	@Override
	public final KeyValue<K, V> poll()
	{
		return EqHashTable.this.size == 0 ? null : EqHashTable.this.chain.first();
	}

	@Override
	public final KeyValue<K, V> peek()
	{
		return EqHashTable.this.size == 0 ? null : EqHashTable.this.chain.last();
	}

	@Override
	public final long indexOf(final KeyValue<K, V> entry)
	{
		return EqHashTable.this.chain.indexOf(entry);
	}

	@Override
	public final long indexBy(final Predicate<? super KeyValue<K, V>> predicate)
	{
		return EqHashTable.this.chain.indexOf(predicate);
	}

	@Override
	public final boolean isSorted(final Comparator<? super KeyValue<K, V>> comparator)
	{
		return EqHashTable.this.chain.isSorted(comparator);
	}

	@Override
	public final long lastIndexOf(final KeyValue<K, V> entry)
	{
		return EqHashTable.this.chain.lastIndexBy(kv ->
			this.hashEqualator.equal(kv.key(), entry.key())
		);
	}

	@Override
	public final long lastIndexBy(final Predicate<? super KeyValue<K, V>> predicate)
	{
		return EqHashTable.this.chain.lastIndexBy(predicate);
	}

	@Override
	public final long maxIndex(final Comparator<? super KeyValue<K, V>> comparator)
	{
		return EqHashTable.this.chain.maxIndex(comparator);
	}

	@Override
	public final long minIndex(final Comparator<? super KeyValue<K, V>> comparator)
	{
		return EqHashTable.this.chain.minIndex(comparator);
	}

	@Override
	public final long scan(final Predicate<? super KeyValue<K, V>> predicate)
	{
		return EqHashTable.this.chain.scan(predicate);
	}

	@Override
	public final <C extends Consumer<? super KeyValue<K, V>>> C moveSelection(final C target, final long... indices)
	{
		EqHashTable.this.chain.moveSelection(target, indices);
		return target;
	}

	@Override
	public final KeyValue<K, V> removeAt(final long index)
	{
		return EqHashTable.this.chain.remove(index);
	}

	@Override
	public final KeyValue<K, V> fetch()
	{
		return EqHashTable.this.chain.remove(0);
	}

	@Override
	public final KeyValue<K, V> pop()
	{
		return EqHashTable.this.chain.remove(EqHashTable.this.size - 1);
	}

	@Override
	public final KeyValue<K, V> pinch()
	{
		return EqHashTable.this.size == 0 ? null : EqHashTable.this.chain.remove(0);
	}

	@Override
	public final KeyValue<K, V> pick()
	{
		return EqHashTable.this.size == 0 ? null : EqHashTable.this.chain.remove(EqHashTable.this.size - 1);
	}

	@Override
	public final KeyValue<K, V> retrieve(final KeyValue<K, V> entry)
	{
		return EqHashTable.this.chain.retrieve(entry);
	}

	@Override
	public final KeyValue<K, V> retrieveBy(final Predicate<? super KeyValue<K, V>> predicate)
	{
		return EqHashTable.this.chain.retrieve(predicate);
	}

	@Override
	public final boolean removeOne(final KeyValue<K, V> entry)
	{
		return EqHashTable.this.chain.removeOne(entry);
	}

	@Override
	public final EqHashTable<K, V> removeRange(final long startIndex, final long length)
	{
		EqHashTable.this.chain.removeRange(startIndex, length);
		return this;
	}

	@Override
	public final EqHashTable<K, V> retainRange(final long startIndex, final long length)
	{
		EqHashTable.this.chain.retainRange(startIndex, length);
		return this;
	}

	@Override
	public final long removeSelection(final long[] indices)
	{
		return EqHashTable.this.chain.removeSelection(indices);
	}

	@Override
	public final Iterator<KeyValue<K, V>> iterator()
	{
		return EqHashTable.this.chain.iterator();
	}

	@Override
	public final Object[] toArray()
	{
		return EqHashTable.this.chain.toArray();
	}

	@Override
	public final EqHashTable<K, V> reverse()
	{
		EqHashTable.this.chain.reverse();
		return this;
	}

	@Override
	public final EqHashTable<K, V> shiftTo(final long sourceIndex, final long targetIndex)
	{
		EqHashTable.this.chain.shiftTo(sourceIndex, targetIndex);
		return this;
	}

	@Override
	public final EqHashTable<K, V> shiftTo(final long sourceIndex, final long targetIndex, final long length)
	{
		EqHashTable.this.chain.shiftTo(sourceIndex, targetIndex, length);
		return this;
	}

	@Override
	public final EqHashTable<K, V> shiftBy(final long sourceIndex, final long distance)
	{
		EqHashTable.this.chain.shiftTo(sourceIndex, distance);
		return this;
	}

	@Override
	public final EqHashTable<K, V> shiftBy(final long sourceIndex, final long distance, final long length)
	{
		EqHashTable.this.chain.shiftTo(sourceIndex, distance, length);
		return this;
	}

	@Override
	public final EqHashTable<K, V> swap(final long indexA, final long indexB)
	{
		EqHashTable.this.chain.swap(indexA, indexB);
		return this;
	}

	@Override
	public final EqHashTable<K, V> swap(final long indexA, final long indexB, final long length)
	{
		EqHashTable.this.chain.swap(indexA, indexB, length);
		return this;
	}

	@Override
	public final HashEqualator<KeyValue<K, V>> equality()
	{
		return XHashing.<K, V>wrapAsKeyValue(EqHashTable.this.hashEqualator);
	}

	@Override
	public final boolean input(final long index, final KeyValue<K, V> element)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME EqHashTable.Entries#input()
	}

	@SafeVarargs
	@Override
	public final long inputAll(final long index, final KeyValue<K, V>... elements)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME EqHashTable.Entries#input()
	}

	@Override
	public final long inputAll(final long index, final KeyValue<K, V>[] elements, final int offset, final int length)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME EqHashTable.Entries#inputAll()
	}

	@Override
	public final long inputAll(final long index, final XGettingCollection<? extends KeyValue<K, V>> elements)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME EqHashTable.Entries#inputAll()
	}

	@Override
	public final boolean insert(final long index, final KeyValue<K, V> element)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME EqHashTable.Entries#insert()
	}

	@SafeVarargs
	@Override
	public final long insertAll(final long index, final KeyValue<K, V>... elements)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME EqHashTable.Entries#insert()
	}

	@Override
	public final long insertAll(final long index, final KeyValue<K, V>[] elements, final int offset, final int length)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME EqHashTable.Entries#insertAll()
	}

	@Override
	public final long insertAll(final long index, final XGettingCollection<? extends KeyValue<K, V>> elements)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME EqHashTable.Entries#insertAll()
	}

	@Override
	public final boolean prepend(final KeyValue<K, V> element)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME EqHashTable.Entries#prepend()
	}

	@Override
	public final boolean preput(final KeyValue<K, V> element)
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
	public final EqHashTable<K, V> prependAll(final KeyValue<K, V>... elements)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME EqHashTable.Entries#prepend()
	}

	@Override
	public final EqHashTable<K, V> prependAll(final KeyValue<K, V>[] elements, final int srcStartIndex, final int srcLength)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME EqHashTable.Entries#prependAll()
	}

	@Override
	public final EqHashTable<K, V> prependAll(final XGettingCollection<? extends KeyValue<K, V>> elements)
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
	public final EqHashTable<K, V> preputAll(final KeyValue<K, V>... elements)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME EqHashTable.Entries#preput()
	}

	@Override
	public final EqHashTable<K, V> preputAll(final KeyValue<K, V>[] elements, final int offset, final int length)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME EqHashTable.Entries#preputAll()
	}

	@Override
	public final EqHashTable<K, V> preputAll(final XGettingCollection<? extends KeyValue<K, V>> elements)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME EqHashTable.Entries#preputAll()
	}

	@Override
	public final boolean set(final long index, final KeyValue<K, V> element)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME EqHashTable.Entries#set()
	}

	@Override
	public final KeyValue<K, V> setGet(final long index, final KeyValue<K, V> element)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME EqHashTable.Entries#setGet()
	}

	@Override
	public final void setFirst(final KeyValue<K, V> element)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME EqHashTable.Entries#setFirst()
	}

	@Override
	public final void setLast(final KeyValue<K, V> element)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME EqHashTable.Entries#setLast()
	}

	@SafeVarargs
	@Override
	public final EqHashTable<K, V> setAll(final long index, final KeyValue<K, V>... elements)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME EqHashTable.Entries#set()
	}

	@Override
	public final EqHashTable<K, V> set(final long index, final KeyValue<K, V>[] elements, final int offset, final int length)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME EqHashTable.Entries#set()
	}

	@Override
	public final EqHashTable<K, V> set(final long index, final XGettingSequence<? extends KeyValue<K, V>> elements, final long offset, final long length)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME EqHashTable.Entries#set()
	}



	public static final <K, VK, VV> Function<K, EqHashTable<VK, VV>> supplier(final HashEqualator<VK> hashEqualator)
	{
		return new Function<K, EqHashTable<VK, VV>>()
		{
			@Override
			public final EqHashTable<VK, VV> apply(final K key)
			{
				return EqHashTable.New(hashEqualator);
			}

		};
	}

	public static final <K, VK, VV> Function<K, EqHashTable<VK, VV>> supplier()
	{
		return new Function<K, EqHashTable<VK, VV>>()
		{
			@Override
			public final EqHashTable<VK, VV> apply(final K key)
			{
				return EqHashTable.New();
			}

		};
	}




	public final class Keys implements XTable.Keys<K, V>, HashCollection<K>
	{
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		public final int hashDistributionRange()
		{
			return EqHashTable.this.slots.length;
		}

		@Override
		public final boolean hasVolatileHashElements()
		{
			return EqHashTable.this.chain.hasVolatileElements();
		}

		@Override
		public final void setHashDensity(final float hashDensity)
		{
			EqHashTable.this.setHashDensity(hashDensity);
		}

		@Override
		public final HashCollection.Analysis<Keys> analyze()
		{
			return AbstractChainEntryLinked.analyzeSlots(this, EqHashTable.this.slots);
		}



		///////////////////////////////////////////////////////////////////////////
		// getting methods //
		////////////////////

		@Override
		public final Equalator<? super K> equality()
		{
			return EqHashTable.this.hashEquality();
		}

		@Override
		public final Keys copy()
		{
			return EqHashTable.this.copy().keys();
		}

		/**
		 * This method creates a {@link EqConstHashEnum} instance containing all (currently existing) elements
		 * of this {@link EqConstHashEnum}.<br>
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
		public final EqConstHashEnum<K> immure()
		{
			this.consolidate();
			return EqConstHashEnum.New( //const set may not contain volatile hash logic.
				EqHashTable.this.hashEquality(),
				EqHashTable.this.hashDensity,
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
			throw new one.microstream.meta.NotImplementedYetError(); // FIXME EqHashTable.Keys#range()
		}

		@Override
		public final XGettingEnum<K> view(final long lowIndex, final long highIndex)
		{
			throw new one.microstream.meta.NotImplementedYetError(); // FIXME EqHashTable.Keys#view()
		}

		@Override
		public final K[] toArray(final Class<K> type)
		{
			return EqHashTable.this.chain.keyToArray(type);
		}

		// executing //

		@Override
		public final <P extends Consumer<? super K>> P iterate(final P procedure)
		{
			EqHashTable.this.chain.keyIterate(procedure);
			return procedure;
		}

		@Override
		public final <A> A join(final BiConsumer<? super K, ? super A> joiner, final A aggregate)
		{
			EqHashTable.this.chain.keyJoin(joiner, aggregate);
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
			return EqHashTable.this.chain.keyCount(predicate);
		}

		// element querying //

		@Override
		public final K seek(final K sample)
		{
			return EqHashTable.this.chain.keySeek(sample, EqHashTable.this.hashEqualator);
		}

		@Override
		public final K search(final Predicate<? super K> predicate)
		{
			return EqHashTable.this.chain.keySearch(predicate);
		}

		@Override
		public final K max(final Comparator<? super K> comparator)
		{
			return EqHashTable.this.chain.keyMax(comparator);
		}

		@Override
		public final K min(final Comparator<? super K> comparator)
		{
			return EqHashTable.this.chain.keyMin(comparator);
		}

		// boolean querying //

		@Override
		public final boolean hasVolatileElements()
		{
			return EqHashTable.this.chain.hasVolatileElements();
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
			return EqHashTable.this.chain.keyApplies(predicate);
		}

		@Override
		public final boolean applies(final Predicate<? super K> predicate)
		{
			return EqHashTable.this.chain.keyAppliesAll(predicate);
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
			for(ChainMapEntryLinkedHashedStrongStrong<K, V> e = EqHashTable.this.slots[(hash = EqHashTable.this.hashEqualator.hash(element)) & EqHashTable.this.range]; e != null; e = e.link)
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
			for(ChainMapEntryLinkedHashedStrongStrong<K, V> e = EqHashTable.this.slots[(hash = EqHashTable.this.hashEqualator.hash(element)) & EqHashTable.this.range]; e != null; e = e.link)
			{
				if(e.hash == hash && EqHashTable.this.hashEqualator.equal(e.key(), element))
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
			if(samples == null || !(samples instanceof EqHashTable<?, ?>.Keys))
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
			if(EqHashTable.this.size != XTypes.to_int(samples.size()))
			{
				return false;
			}

			// if sizes are equal and all elements of collection are contained in this set, they must have equal content
			return EqHashTable.this.chain.keyEqualsContent(samples, equalator);
		}

		// data set procedures //

		@Override
		public final <C extends Consumer<? super K>> C intersect(
			final XGettingCollection<? extends K> other,
			final Equalator<? super K> equalator,
			final C target
		)
		{
			return EqHashTable.this.chain.keyIntersect(other, equalator, target);
		}

		@Override
		public final <C extends Consumer<? super K>> C except(
			final XGettingCollection<? extends K> other,
			final Equalator<? super K> equalator,
			final C target
		)
		{
			return EqHashTable.this.chain.keyExcept(other, equalator, target);
		}

		@Override
		public final <C extends Consumer<? super K>> C union(
			final XGettingCollection<? extends K> other,
			final Equalator<? super K> equalator,
			final C target
		)
		{
			return EqHashTable.this.chain.keyUnion(other, equalator, target);
		}

		@Override
		public final <C extends Consumer<? super K>> C copyTo(final C target)
		{
			if(target == this)
			{
				return target; // copying a set logic collection to itself would be a no-op, so spare the effort
			}
			return EqHashTable.this.chain.keyCopyTo(target);
		}

		@Override
		public final <C extends Consumer<? super K>> C filterTo(final C target, final Predicate<? super K> predicate)
		{
			return EqHashTable.this.chain.keyCopyTo(target, predicate);
		}

		@Override
		public final <C extends Consumer<? super K>> C distinct(final C target)
		{
			return this.distinct(target, EqHashTable.this.hashEqualator);
		}

		@Override
		public final <C extends Consumer<? super K>> C distinct(final C target, final Equalator<? super K> equalator)
		{
			if(EqHashTable.this.hashEqualator == equalator)
			{
				return this.copyTo(target);
			}
			return EqHashTable.this.chain.keyDistinct(target, equalator);
		}



		///////////////////////////////////////////////////////////////////////////
		// adding //
		///////////

		@Override
		public final long currentCapacity()
		{
			return EqHashTable.this.currentCapacity();
		}

		@Override
		public final long maximumCapacity()
		{
			return EqHashTable.this.maximumCapacity();
		}

		@Override
		public final boolean isFull()
		{
			return EqHashTable.this.isFull();
		}

		@Override
		public final long optimize()
		{
			return EqHashTable.this.optimize();
		}

		@Override
		public final int rehash()
		{
			return EqHashTable.this.rehash();
		}

		@Override
		public final Keys ensureFreeCapacity(final long requiredFreeCapacity)
		{
			EqHashTable.this.ensureFreeCapacity(requiredFreeCapacity);
			return this;
		}

		@Override
		public final Keys ensureCapacity(final long minimalCapacity)
		{
			EqHashTable.this.ensureCapacity(minimalCapacity);
			return this;
		}

		@Override
		public final boolean nullAdd()
		{
			return EqHashTable.this.nullKeyAdd();
		}

		@Override
		public final boolean add(final K element)
		{
			return EqHashTable.this.internalAddOnlyKey(element);
		}

		@SafeVarargs
		@Override
		public final Keys addAll(final K... elements)
		{
			final EqHashTable<K, V> parent = EqHashTable.this;
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
			final EqHashTable<K, V> parent = EqHashTable.this;
			for(int i = srcIndex; i != bound; i += d)
			{
				parent.internalAddOnlyKey(elements[i]);
			}

			return this;
		}

		@Override
		public final Keys addAll(final XGettingCollection<? extends K> elements)
		{
			elements.iterate(EqHashTable.this::internalAddOnlyKey);
			return this;
		}

		@Override
		public final boolean nullPut()
		{
			return EqHashTable.this.nullKeyPut();
		}

		@Override
		public final void accept(final K element)
		{
			EqHashTable.this.internalPutOnlyKey(element);
		}

		@Override
		public final boolean put(final K element)
		{
			return EqHashTable.this.internalPutOnlyKey(element);
		}

		@Override
		public final K addGet(final K element)
		{
			return EqHashTable.this.internalAddGetKey(element);
		}
		
		@Override
		public K deduplicate(final K element)
		{
			return EqHashTable.this.internalAddGetKey(element);
		}

		@Override
		public final K putGet(final K element)
		{
			return EqHashTable.this.internalPutGetKey(element);
		}

		@Override
		public final K replace(final K element)
		{
			return EqHashTable.this.internalReplaceKey(element);
		}

		@SafeVarargs
		@Override
		public final Keys putAll(final K... elements)
		{
			final EqHashTable<K, V> parent = EqHashTable.this;
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
			final EqHashTable<K, V> parent = EqHashTable.this;
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
				EqHashTable.this.put(k, null)
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
			EqHashTable.this.truncate();
		}

		@Override
		public final long consolidate()
		{
			return EqHashTable.this.consolidate();
		}

		// removing //

		@Override
		public final long remove(final K element)
		{
			return EqHashTable.this.removeKey(element);
		}

		@Override
		public final long nullRemove()
		{
			return EqHashTable.this.removeKey(null);
		}

		// reducing //

		@Override
		public final long removeBy(final Predicate<? super K> predicate)
		{
			return EqHashTable.this.chain.keyReduce(predicate);
		}

		// retaining //

		@Override
		public final long retainAll(final XGettingCollection<? extends K> elements)
		{
			return EqHashTable.this.chain.keyRetainAll(elements);
		}

		@Override
		public final <P extends Consumer<? super K>> P process(final P procedure)
		{
			EqHashTable.this.chain.keyProcess(procedure);
			return procedure;
		}

		@Override
		public final <C extends Consumer<? super K>> C moveTo(final C target, final Predicate<? super K> predicate)
		{
			EqHashTable.this.chain.keyMoveTo(target, predicate);
			return target;
		}

		// removing - all //

		@Override
		public final long removeAll(final XGettingCollection<? extends K> elements)
		{
			final int oldSize = EqHashTable.this.size;
			elements.iterate(EqHashTable.this::removeFor);
			return oldSize - EqHashTable.this.size;
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
			if(EqHashTable.this.hashEqualator == equalator)
			{
				return 0; // set is guaranteed to contain unique values according to its inherent equalator
			}

			// singleton null can be ignored here
			return EqHashTable.this.chain.keyRemoveDuplicates(equalator);
		}

		@Override
		public final Keys toReversed()
		{
			final EqHashTable<K, V> reversedVarSet = EqHashTable.this.copy();
			reversedVarSet.chain.reverse();
			return reversedVarSet.keys;
		}

		@Override
		public final <T extends Consumer<? super K>> T copySelection(final T target, final long... indices)
		{
			EqHashTable.this.chain.keyCopySelection(target, indices);
			return target;
		}

		@Override
		public final <P extends IndexedAcceptor<? super K>> P iterateIndexed(final P procedure)
		{
			EqHashTable.this.chain.keyIterateIndexed(procedure);
			return procedure;
		}

		@Override
		public final K at(final long index)
		{
			return EqHashTable.this.chain.keyGet(index);
		}

		@Override
		public final K get()
		{
			return EqHashTable.this.chain.keyFirst();
		}

		@Override
		public final K first()
		{
			return EqHashTable.this.chain.keyFirst();
		}

		@Override
		public final K last()
		{
			return EqHashTable.this.chain.keyLast();
		}

		@Override
		public final K poll()
		{
			return EqHashTable.this.size == 0 ? null : EqHashTable.this.chain.keyFirst();
		}

		@Override
		public final K peek()
		{
			return EqHashTable.this.size == 0 ? null : EqHashTable.this.chain.keyLast();
		}

		@Override
		public final long indexOf(final K element)
		{
			return EqHashTable.this.chain.keyIndexOf(element, this.hashEquality());
		}

		@Override
		public final long indexBy(final Predicate<? super K> predicate)
		{
			return EqHashTable.this.chain.keyIndexBy(predicate);
		}

		@Override
		public final boolean isSorted(final Comparator<? super K> comparator)
		{
			return EqHashTable.this.chain.keyIsSorted(comparator);
		}

		@Override
		public final long lastIndexOf(final K element)
		{
			return EqHashTable.this.chain.keyLastIndexOf(element, this.hashEquality());
		}

		@Override
		public final long lastIndexBy(final Predicate<? super K> predicate)
		{
			return EqHashTable.this.chain.keyLastIndexBy(predicate);
		}

		@Override
		public final long maxIndex(final Comparator<? super K> comparator)
		{
			return EqHashTable.this.chain.keyMaxIndex(comparator);
		}

		@Override
		public final long minIndex(final Comparator<? super K> comparator)
		{
			return EqHashTable.this.chain.keyMinIndex(comparator);
		}

		@Override
		public final long scan(final Predicate<? super K> predicate)
		{
			return EqHashTable.this.chain.keyScan(predicate);
		}

		@Override
		public final <C extends Consumer<? super K>> C moveSelection(final C target, final long... indices)
		{
			EqHashTable.this.chain.keyMoveSelection(target, indices);
			return target;
		}

		@Override
		public final K removeAt(final long index)
		{
			return EqHashTable.this.chain.keyRemove(index);
		}

		@Override
		public final K fetch()
		{
			return EqHashTable.this.chain.keyRemove(0);
		}

		@Override
		public final K pop()
		{
			return EqHashTable.this.chain.keyRemove(EqHashTable.this.size - 1);
		}

		@Override
		public final K pinch()
		{
			return EqHashTable.this.size == 0 ? null : EqHashTable.this.chain.keyRemove(0);
		}

		@Override
		public final K pick()
		{
			return EqHashTable.this.size == 0 ? null : EqHashTable.this.chain.keyRemove(EqHashTable.this.size - 1);
		}

		@Override
		public final K retrieve(final K element)
		{
			return EqHashTable.this.chain.keyRetrieve(element, this.hashEquality());
		}

		@Override
		public final K retrieveBy(final Predicate<? super K> predicate)
		{
			return EqHashTable.this.chain.keyRetrieve(predicate);
		}

		@Override
		public final boolean removeOne(final K element)
		{
			return EqHashTable.this.chain.keyRemoveOne(element, this.hashEquality());
		}

		@Override
		public final Keys removeRange(final long startIndex, final long length)
		{
			EqHashTable.this.chain.removeRange(startIndex, length);
			return this;
		}

		@Override
		public final Keys retainRange(final long startIndex, final long length)
		{
			EqHashTable.this.chain.retainRange(startIndex, length);
			return this;
		}

		@Override
		public final long removeSelection(final long[] indices)
		{
			return EqHashTable.this.chain.removeSelection(indices);
		}

		@Override
		public final boolean isEmpty()
		{
			return EqHashTable.this.isEmpty();
		}

		@Override
		public final Iterator<K> iterator()
		{
			return EqHashTable.this.chain.keyIterator();
		}

		@Override
		public final long size()
		{
			return EqHashTable.this.size;
		}

		@Override
		public final String toString()
		{
			if(EqHashTable.this.size == 0)
			{
				return "[]"; // array causes problems with escape condition otherwise
			}

			final VarString vc = VarString.New(EqHashTable.this.slots.length).append('[');
			EqHashTable.this.chain.keyAppendTo(vc, ',').append(']');
			return vc.toString();
		}

		@Override
		public final Object[] toArray()
		{
			return EqHashTable.this.chain.keyToArray();
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
			EqHashTable.this.clear();
		}

		@Override
		public final Keys reverse()
		{
			EqHashTable.this.chain.reverse();
			return this;
		}

		@Override
		public final Keys sort(final Comparator<? super K> comparator)
		{
			EqHashTable.this.chain.keySort(comparator);
			return this;
		}

		@Override
		public final Keys shiftTo(final long sourceIndex, final long targetIndex)
		{
			EqHashTable.this.chain.shiftTo(sourceIndex, targetIndex);
			return this;
		}

		@Override
		public final Keys shiftTo(final long sourceIndex, final long targetIndex, final long length)
		{
			EqHashTable.this.chain.shiftTo(sourceIndex, targetIndex, length);
			return this;
		}

		@Override
		public final Keys shiftBy(final long sourceIndex, final long distance)
		{
			EqHashTable.this.chain.shiftTo(sourceIndex, distance);
			return this;
		}

		@Override
		public final Keys shiftBy(final long sourceIndex, final long distance, final long length)
		{
			EqHashTable.this.chain.shiftTo(sourceIndex, distance, length);
			return this;
		}

		@Override
		public final Keys swap(final long indexA, final long indexB)
		{
			EqHashTable.this.chain.swap(indexA, indexB);
			return this;
		}

		@Override
		public final Keys swap(final long indexA, final long indexB, final long length)
		{
			EqHashTable.this.chain.swap(indexA, indexB, length);
			return this;
		}

		@Override
		public final OldKeys old()
		{
			return new OldKeys();
		}

		@Override
		public final EqHashTable<K, V> parent()
		{
			return EqHashTable.this;
		}

		@Override
		public final HashEqualator<? super K> hashEquality()
		{
			return EqHashTable.this.hashEquality();
		}

		@Override
		public final float hashDensity()
		{
			return EqHashTable.this.hashDensity();
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
			throw new one.microstream.meta.NotImplementedYetError(); // FIXME EqHashTable.Keys#input()
		}

		@SafeVarargs
		@Override
		public final long inputAll(final long index, final K... elements)
		{
			throw new one.microstream.meta.NotImplementedYetError(); // FIXME EqHashTable.Keys#input()
		}

		@Override
		public final long inputAll(final long index, final K[] elements, final int offset, final int length)
		{
			throw new one.microstream.meta.NotImplementedYetError(); // FIXME EqHashTable.Keys#inputAll()
		}

		@Override
		public final long inputAll(final long index, final XGettingCollection<? extends K> elements)
		{
			throw new one.microstream.meta.NotImplementedYetError(); // FIXME EqHashTable.Keys#inputAll()
		}

		@Override
		public final boolean insert(final long index, final K element)
		{
			throw new one.microstream.meta.NotImplementedYetError(); // FIXME EqHashTable.Keys#insert()
		}

		@SafeVarargs
		@Override
		public final long insertAll(final long index, final K... elements)
		{
			throw new one.microstream.meta.NotImplementedYetError(); // FIXME EqHashTable.Keys#insert()
		}

		@Override
		public final long insertAll(final long index, final K[] elements, final int offset, final int length)
		{
			throw new one.microstream.meta.NotImplementedYetError(); // FIXME EqHashTable.Keys#insertAll()
		}

		@Override
		public final long insertAll(final long index, final XGettingCollection<? extends K> elements)
		{
			throw new one.microstream.meta.NotImplementedYetError(); // FIXME EqHashTable.Keys#insertAll()
		}

		@Override
		public final boolean prepend(final K element)
		{
			throw new one.microstream.meta.NotImplementedYetError(); // FIXME EqHashTable.Keys#prepend()
		}

		@Override
		public final boolean preput(final K element)
		{
			throw new one.microstream.meta.NotImplementedYetError(); // FIXME EqHashTable.Keys#preput()
		}

		@Override
		public final boolean nullInput(final long index)
		{
			throw new one.microstream.meta.NotImplementedYetError(); // FIXME EqHashTable.Keys#nullInput()
		}

		@Override
		public final boolean nullInsert(final long index)
		{
			throw new one.microstream.meta.NotImplementedYetError(); // FIXME EqHashTable.Keys#nullInsert()
		}

		@Override
		public final boolean nullPrepend()
		{
			throw new one.microstream.meta.NotImplementedYetError(); // FIXME EqHashTable.Keys#nullPrepend()
		}

		@Override
		public final Keys prependAll(@SuppressWarnings("unchecked") final K... elements)
		{
			throw new one.microstream.meta.NotImplementedYetError(); // FIXME EqHashTable.Keys#prepend()
		}

		@Override
		public final Keys prependAll(final K[] elements, final int srcStartIndex, final int srcLength)
		{
			throw new one.microstream.meta.NotImplementedYetError(); // FIXME EqHashTable.Keys#prependAll()
		}

		@Override
		public final Keys prependAll(final XGettingCollection<? extends K> elements)
		{
			throw new one.microstream.meta.NotImplementedYetError(); // FIXME EqHashTable.Keys#prependAll()
		}

		@Override
		public final boolean nullPreput()
		{
			throw new one.microstream.meta.NotImplementedYetError(); // FIXME EqHashTable.Keys#nullPreput()
		}

		@SafeVarargs
		@Override
		public final Keys preputAll(final K... elements)
		{
			throw new one.microstream.meta.NotImplementedYetError(); // FIXME EqHashTable.Keys#preput()
		}

		@Override
		public final Keys preputAll(final K[] elements, final int offset, final int length)
		{
			throw new one.microstream.meta.NotImplementedYetError(); // FIXME EqHashTable.Keys#preputAll()
		}

		@Override
		public final Keys preputAll(final XGettingCollection<? extends K> elements)
		{
			throw new one.microstream.meta.NotImplementedYetError(); // FIXME EqHashTable.Keys#preputAll()
		}

		@Override
		public final boolean set(final long index, final K element)
		{
			throw new one.microstream.meta.NotImplementedYetError(); // FIXME EqHashTable.Keys#set()
		}

		@Override
		public final K setGet(final long index, final K element)
		{
			throw new one.microstream.meta.NotImplementedYetError(); // FIXME EqHashTable.Keys#setGet()
		}

		@Override
		public final void setFirst(final K element)
		{
			throw new one.microstream.meta.NotImplementedYetError(); // FIXME EqHashTable.Keys#setFirst()
		}

		@Override
		public final void setLast(final K element)
		{
			throw new one.microstream.meta.NotImplementedYetError(); // FIXME EqHashTable.Keys#setLast()
		}

		@SafeVarargs
		@Override
		public final Keys setAll(final long index, final K... elements)
		{
			throw new one.microstream.meta.NotImplementedYetError(); // FIXME EqHashTable.Keys#set()
		}

		@Override
		public final Keys set(final long index, final K[] elements, final int offset, final int length)
		{
			throw new one.microstream.meta.NotImplementedYetError(); // FIXME EqHashTable.Keys#set()
		}

		@Override
		public final Keys set(final long index, final XGettingSequence<? extends K> elements, final long offset, final long length)
		{
			throw new one.microstream.meta.NotImplementedYetError(); // FIXME EqHashTable.Keys#set()
		}

		@Override
		public long substitute(final Function<? super K, ? extends K> mapper)
		{
			return EqHashTable.this.chain.keySubstitute(mapper, EqHashTable.this::replace);
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
			return new BulkList<V>(XTypes.to_int(EqHashTable.this.size())).addAll(this);
		}

		@Override
		public final <P extends Consumer<? super V>> P iterate(final P procedure)
		{
			EqHashTable.this.chain.valuesIterate(procedure);
			return procedure;
		}

		@Override
		public final <A> A join(final BiConsumer<? super V, ? super A> joiner, final A aggregate)
		{
			EqHashTable.this.chain.valuesJoin(joiner, aggregate);
			return aggregate;
		}

		@Override
		public final <P extends IndexedAcceptor<? super V>> P iterateIndexed(final P procedure)
		{
			EqHashTable.this.chain.valuesIterateIndexed(procedure);
			return procedure;
		}

		@Override
		public final Values toReversed()
		{
			final EqHashTable<K, V> reversedVarSet = EqHashTable.this.copy();
			reversedVarSet.chain.reverse();
			return reversedVarSet.values;
		}

		@Override
		public final boolean containsSearched(final Predicate<? super V> predicate)
		{
			return EqHashTable.this.chain.valuesApplies(predicate);
		}

		@Override
		public final boolean applies(final Predicate<? super V> predicate)
		{
			return EqHashTable.this.chain.valuesAppliesAll(predicate);
		}

		@Override
		public final boolean contains(final V value)
		{
			return EqHashTable.this.chain.valuesContains(value);
		}
		
		@Override
		public final boolean containsAll(final XGettingCollection<? extends V> values)
		{
			return EqHashTable.this.chain.valuesContainsAll(values);
		}

		@Override
		public final boolean containsId(final V value)
		{
			return EqHashTable.this.chain.valuesContainsId(value);
		}

		@Override
		public final <T extends Consumer<? super V>> T copyTo(final T target)
		{
			EqHashTable.this.chain.valuesCopyTo(target);
			return target;
		}

		@Override
		public final <T extends Consumer<? super V>> T filterTo(final T target, final Predicate<? super V> predicate)
		{
			EqHashTable.this.chain.valuesCopyTo(target, predicate);
			return target;
		}

		@Override
		public final long count(final V value)
		{
			return EqHashTable.this.chain.valuesCount(value);
		}

		@Override
		public final long countBy(final Predicate<? super V> predicate)
		{
			return EqHashTable.this.chain.valuesCount(predicate);
		}

		@Override
		public final <T extends Consumer<? super V>> T distinct(final T target)
		{
			EqHashTable.this.chain.valuesDistinct(target);
			return target;
		}

		@Override
		public final <T extends Consumer<? super V>> T distinct(final T target, final Equalator<? super V> equalator)
		{
			EqHashTable.this.chain.valuesDistinct(target, equalator);
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
			return EqHashTable.this.chain.valuesEqualsContent(samples, equalator);
		}

		@Override
		public final <T extends Consumer<? super V>> T except(final XGettingCollection<? extends V> other, final Equalator<? super V> equalator, final T target)
		{
			EqHashTable.this.chain.valuesExcept(other, equalator, target);
			return target;
		}

		@Override
		public final boolean hasVolatileElements()
		{
			return EqHashTable.this.hasVolatileValues();
		}

		@Override
		public final <T extends Consumer<? super V>> T intersect(final XGettingCollection<? extends V> other, final Equalator<? super V> equalator, final T target)
		{
			EqHashTable.this.chain.valuesIntersect(other, equalator, target);
			return target;
		}

		@Override
		public final boolean isEmpty()
		{
			return EqHashTable.this.isEmpty();
		}

		@Override
		public final Iterator<V> iterator()
		{
			return EqHashTable.this.chain.valuesIterator();
		}

		@Override
		public final V max(final Comparator<? super V> comparator)
		{
			return EqHashTable.this.chain.valuesMax(comparator);
		}

		@Override
		public final V min(final Comparator<? super V> comparator)
		{
			return EqHashTable.this.chain.valuesMin(comparator);
		}

		@Override
		public final boolean nullAllowed()
		{
			return EqHashTable.this.nullAllowed();
		}

		@Override
		public final boolean nullContained()
		{
			return EqHashTable.this.chain.valuesContains(null);
		}

		@Override
		public final OldValues old()
		{
			return new OldValues();
		}

		@Override
		public final V seek(final V sample)
		{
			return EqHashTable.this.chain.valuesSeek(sample);
		}

		@Override
		public final V search(final Predicate<? super V> predicate)
		{
			return EqHashTable.this.chain.valuesSearch(predicate);
		}

		@Override
		public final long size()
		{
			return XTypes.to_int(EqHashTable.this.size());
		}

		@Override
		public final long maximumCapacity()
		{
			return XTypes.to_int(EqHashTable.this.size());
		}

		@Override
		public final boolean isFull()
		{
			return EqHashTable.this.isFull();
		}

		@Override
		public final long remainingCapacity()
		{
			return EqHashTable.this.remainingCapacity();
		}

		@Override
		public final String toString()
		{
			if(EqHashTable.this.size == 0)
			{
				return "[]"; // array causes problems with escape condition otherwise
			}

			final VarString vc = VarString.New(EqHashTable.this.slots.length).append('[');
			EqHashTable.this.chain.valuesAppendTo(vc, ',').append(']');
			return vc.toString();
		}

		@Override
		public final Object[] toArray()
		{
			return EqHashTable.this.chain.valuesToArray();
		}

		@Override
		public final V[] toArray(final Class<V> type)
		{
			return EqHashTable.this.chain.valuesToArray(type);
		}

		@Override
		public final <T extends Consumer<? super V>> T union(
			final XGettingCollection<? extends V> other,
			final Equalator<? super V> equalator,
			final T target
		)
		{
			EqHashTable.this.chain.valuesUnion(other, equalator, target);
			return target;
		}

		@Override
		public final EqHashTable<K, V> parent()
		{
			return EqHashTable.this;
		}

		@Override
		public final SubListView<V> view(final long fromIndex, final long toIndex)
		{
			return new SubListView<>(this, fromIndex, toIndex);
		}

		@Override
		public final ListIterator<V> listIterator()
		{
			return EqHashTable.this.chain.valuesListIterator(0);
		}

		@Override
		public final ListIterator<V> listIterator(final long index)
		{
			return EqHashTable.this.chain.valuesListIterator(index);
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
			EqHashTable.this.chain.valuesCopySelection(target, indices);
			return target;
		}

		@Override
		public final V at(final long index)
		{
			return EqHashTable.this.chain.valuesGet(index);
		}

		@Override
		public final V get()
		{
			return EqHashTable.this.chain.valuesFirst();
		}

		@Override
		public final V first()
		{
			return EqHashTable.this.chain.valuesFirst();
		}

		@Override
		public final V last()
		{
			return EqHashTable.this.chain.valuesLast();
		}

		@Override
		public final V poll()
		{
			return EqHashTable.this.size == 0 ? null : EqHashTable.this.chain.valuesFirst();
		}

		@Override
		public final V peek()
		{
			return EqHashTable.this.size == 0 ? null : EqHashTable.this.chain.valuesLast();
		}

		@Override
		public final long indexOf(final V value)
		{
			return EqHashTable.this.chain.valuesIndexOf(value);
		}

		@Override
		public final long indexBy(final Predicate<? super V> predicate)
		{
			return EqHashTable.this.chain.valuesIndexBy(predicate);
		}

		@Override
		public final boolean isSorted(final Comparator<? super V> comparator)
		{
			return EqHashTable.this.chain.valuesIsSorted(comparator);
		}

		@Override
		public final long lastIndexOf(final V value)
		{
			return EqHashTable.this.chain.valuesLastIndexOf(value);
		}

		@Override
		public final long lastIndexBy(final Predicate<? super V> predicate)
		{
			return EqHashTable.this.chain.valuesLastIndexBy(predicate);
		}

		@Override
		public final long maxIndex(final Comparator<? super V> comparator)
		{
			return EqHashTable.this.chain.valuesMaxIndex(comparator);
		}

		@Override
		public final long minIndex(final Comparator<? super V> comparator)
		{
			return EqHashTable.this.chain.valuesMinIndex(comparator);
		}

		@Override
		public final long scan(final Predicate<? super V> predicate)
		{
			return EqHashTable.this.chain.valuesScan(predicate);
		}

		@Override
		public final <C extends Consumer<? super V>> C moveSelection(final C target, final long... indices)
		{
			EqHashTable.this.chain.valuesMoveSelection(target, indices);
			return target;
		}

		@Override
		public final V removeAt(final long index)
		{
			return EqHashTable.this.chain.valuesRemove(index);
		}

		@Override
		public final V fetch()
		{
			return EqHashTable.this.chain.valuesRemove(0);
		}

		@Override
		public final V pop()
		{
			return EqHashTable.this.chain.valuesRemove(EqHashTable.this.size - 1);
		}

		@Override
		public final V pinch()
		{
			return EqHashTable.this.size == 0 ? null : EqHashTable.this.chain.valuesRemove(0);
		}

		@Override
		public final V pick()
		{
			return EqHashTable.this.size == 0 ? null : EqHashTable.this.chain.valuesRemove(EqHashTable.this.size - 1);
		}

		@Override
		public final V retrieve(final V value)
		{
			return EqHashTable.this.chain.valuesRetrieve(value);
		}

		@Override
		public final V retrieveBy(final Predicate<? super V> predicate)
		{
			return EqHashTable.this.chain.valuesRetrieve(predicate);
		}

		@Override
		public final boolean removeOne(final V element)
		{
			return EqHashTable.this.chain.valuesRemoveOne(element);
		}

		@Override
		public final Values removeRange(final long startIndex, final long length)
		{
			EqHashTable.this.chain.removeRange(startIndex, length);
			return this;
		}

		@Override
		public final Values retainRange(final long startIndex, final long length)
		{
			EqHashTable.this.chain.retainRange(startIndex, length);
			return this;
		}

		@Override
		public final long removeSelection(final long[] indices)
		{
			return EqHashTable.this.chain.removeSelection(indices);
		}

		@Override
		public final void clear()
		{
			EqHashTable.this.clear();
		}

		@Override
		public final long consolidate()
		{
			return EqHashTable.this.consolidate();
		}

		@Override
		public final <C extends Consumer<? super V>> C moveTo(final C target, final Predicate<? super V> predicate)
		{
			EqHashTable.this.chain.valuesMoveTo(target, predicate);
			return target;
		}

		@Override
		public final long nullRemove()
		{
			return EqHashTable.this.chain.valuesRemove(null);
		}

		@Override
		public final long optimize()
		{
			return EqHashTable.this.optimize();
		}

		@Override
		public final <P extends Consumer<? super V>> P process(final P procedure)
		{
			EqHashTable.this.chain.valuesProcess(procedure);
			return procedure;
		}

		@Override
		public final long removeBy(final Predicate<? super V> predicate)
		{
			return EqHashTable.this.chain.valuesReduce(predicate);
		}

		@Override
		public final long remove(final V value)
		{
			return EqHashTable.this.chain.valuesRemove(value);
		}

		@Override
		public final long removeAll(final XGettingCollection<? extends V> values)
		{
			return EqHashTable.this.chain.valuesRemoveAll(values);
		}

		@Override
		public final long removeDuplicates()
		{
			return EqHashTable.this.chain.valuesRemoveDuplicates();
		}

		@Override
		public final long removeDuplicates(final Equalator<? super V> equalator)
		{
			return EqHashTable.this.chain.valuesRemoveDuplicates(equalator);
		}

		@Override
		public final long retainAll(final XGettingCollection<? extends V> values)
		{
			return EqHashTable.this.chain.valuesRetainAll(values);
		}

		@Override
		public final void truncate()
		{
			EqHashTable.this.truncate();
		}

		@Override
		public final Values fill(final long offset, final long length, final V value)
		{
			EqHashTable.this.chain.valuesFill(offset, length, value);
			return this;
		}

		@Override
		public final long replace(final V value, final V replacement)
		{
			return EqHashTable.this.chain.valuesReplace(value, replacement);
		}

		@Override
		public final long replaceAll(final XGettingCollection<? extends V> values, final V replacement)
		{
			return EqHashTable.this.chain.valuesReplaceAll(values, replacement);
		}

		@Override
		public final long substitute(final Function<? super V, ? extends V> mapper)
		{
			return EqHashTable.this.chain.valuesSubstitute(mapper);
		}

		@Override
		public final long substitute(final Predicate<? super V> predicate, final Function<V, V> mapper)
		{
			return EqHashTable.this.chain.valuesSubstitute(predicate, mapper);
		}

		@Override
		public final boolean replaceOne(final V value, final V replacement)
		{
			return EqHashTable.this.chain.valuesReplaceOne(value, replacement);
		}

		@Override
		public final Values reverse()
		{
			EqHashTable.this.chain.reverse();
			return this;
		}

		@Override
		public final boolean set(final long index, final V value)
		{
			return EqHashTable.this.chain.valuesSet(index, value) == value;
		}

		@Override
		public final V setGet(final long index, final V value)
		{
			return EqHashTable.this.chain.valuesSet(index, value);
		}

		@Override
		public final Values setAll(final long offset, @SuppressWarnings("unchecked") final V... values)
		{
			EqHashTable.this.chain.valuesSet(offset, values);
			return this;
		}

		@Override
		public final Values set(final long offset, final V[] src, final int srcIndex, final int srcLength)
		{
			EqHashTable.this.chain.valuesSet(offset, src, srcIndex, srcLength);
			return this;
		}


		@Override
		public final Values set(
			final long                          offset      ,
			final XGettingSequence<? extends V> values      ,
			final long                          valuesOffset,
			final long                          valuesLength
		)
		{
			// (23.01.2017 TM)NOTE: copyTo() removed. No time for a replacement, atm.
			throw new one.microstream.meta.NotImplementedYetError(); // FIXME EqHashTable.Values#set()
			
		}

		@Override
		public final void setFirst(final V value)
		{
			EqHashTable.this.chain.valuesSet(0, value);
		}

		@Override
		public final void setLast(final V value)
		{
			EqHashTable.this.chain.valuesSet(EqHashTable.this.size - 1, value);
		}

		@Override
		public final Values sort(final Comparator<? super V> comparator)
		{
			EqHashTable.this.chain.valuesSort(comparator);
			return this;
		}

		@Override
		public final long replace(final Predicate<? super V> predicate, final V substitute)
		{
			return EqHashTable.this.chain.valuesSubstitute(predicate, substitute);
		}

		@Override
		public final boolean replaceOne(final Predicate<? super V> predicate, final V substitute)
		{
			return EqHashTable.this.chain.valuesSubstituteOne(predicate, substitute);
		}

		@Override
		public final Values shiftTo(final long sourceIndex, final long targetIndex)
		{
			EqHashTable.this.chain.shiftTo(sourceIndex, targetIndex);
			return this;
		}

		@Override
		public final Values shiftTo(final long sourceIndex, final long targetIndex, final long length)
		{
			EqHashTable.this.chain.shiftTo(sourceIndex, targetIndex, length);
			return this;
		}

		@Override
		public final Values shiftBy(final long sourceIndex, final long distance)
		{
			EqHashTable.this.chain.shiftTo(sourceIndex, distance);
			return this;
		}

		@Override
		public final Values shiftBy(final long sourceIndex, final long distance, final long length)
		{
			EqHashTable.this.chain.shiftTo(sourceIndex, distance, length);
			return this;
		}

		@Override
		public final Values swap(final long indexA, final long indexB)
		{
			EqHashTable.this.chain.swap(indexA, indexB);
			return this;
		}

		@Override
		public final Values swap(final long indexA, final long indexB, final long length)
		{
			EqHashTable.this.chain.swap(indexA, indexB, length);
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
			EqHashTable.this.clear();
		}

		@SuppressWarnings("unchecked")
		@Override
		public final boolean containsKey(final Object key)
		{
			try
			{
				return EqHashTable.this.containsKey((K)key);
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
				return EqHashTable.this.chain.valuesContains((V)value);
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
			return (Set<java.util.Map.Entry<K, V>>)(Set<?>)EqHashTable.this.old();
		}

		@SuppressWarnings("unchecked")
		@Override
		public final V get(final Object key)
		{
			try
			{
				return EqHashTable.this.get((K)key);
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
			return EqHashTable.this.isEmpty();
		}

		@Override
		public final Set<K> keySet()
		{
			return EqHashTable.this.keys().old();
		}

		@Override
		public final V put(final K key, final V value)
		{
			return EqHashTable.this.oldPutGet(key, value);
		}

		@SuppressWarnings("unchecked")
		@Override
		public final void putAll(final Map<? extends K, ? extends V> m)
		{
			if(m instanceof XGettingMap.Bridge<?, ?>)
			{
				EqHashTable.this.addAll(((XGettingMap.Bridge<K, V>)m).parent());
				return;
			}

			final EqHashTable<K, V> parent = EqHashTable.this;
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
				return EqHashTable.this.removeFor((K)key);
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
			return XTypes.to_int(EqHashTable.this.size());
		}

		@Override
		public final Collection<V> values()
		{
			return EqHashTable.this.values.old(); // hehehe
		}

		@Override
		public final EqHashTable<K, V> parent()
		{
			return EqHashTable.this;
		}

	}

}

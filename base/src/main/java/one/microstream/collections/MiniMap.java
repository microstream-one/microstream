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

import one.microstream.math.XMath;
import one.microstream.typing.Composition;
import one.microstream.typing.KeyValue;


/**
 * Straight forward minimal implementation of a strongly referencing identity hashing map.
 * <p>
 * This implementation is preferable to full scale implementations like {@link HashEnum} or {@link EqHashEnum} in cases
 * where only basic mapping functionality but best performance and low memory need is required, for example to associate
 * handler instances to class instances.
 *
 * @param <K> the key type.
 * @param <V> the value type.
 */
public final class MiniMap<K, V> implements Composition
{
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////

	@SafeVarargs
	public static final <K, V> MiniMap<K, V> miniMap(final KeyValue<? extends K, ? extends V>... data)
	{
		return new MiniMap<>(data.length, data);
	}


	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	private Entry<K, V>[] slots;
	private int modulo;
	private int size;



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	@SuppressWarnings("unchecked")
	public MiniMap()
	{
		super();
		this.size = 0;
		this.slots = new Entry[1];
		this.modulo = 0;
	}

	@SuppressWarnings("unchecked")
	public MiniMap(final int initialCapacity)
	{
		super();
		this.size = 0;
		this.modulo = (this.slots = new Entry[XMath.pow2BoundMaxed(initialCapacity)]).length - 1;
	}

	@SuppressWarnings("unchecked")
	public MiniMap(final int initialCapacity, final KeyValue<? extends K, ? extends V>... data)
	{
		super();
		final Entry<K, V>[] slots;
		final int modulo;
		this.size = 0;
		this.modulo = modulo = (this.slots = slots = new Entry[XMath.pow2BoundMaxed(initialCapacity)]).length - 1;
		for(int i = 0; i < data.length; i++)
		{
			final KeyValue<? extends K, ? extends V> entry;
			if((entry = data[i]) == null)
			{
				continue;
			}

			final K key;
			if((key = entry.key()) == null)
			{
				continue;
			}

			slots[identityHashCode(key) & modulo] =
				new Entry<>(key, entry.value(), slots[identityHashCode(key) & modulo]
			);
		}
	}

	@SuppressWarnings("unchecked")
	MiniMap(final int size, final ConstMiniMap.Entry<K, V>[] source)
	{
		super();
		final Entry<K, V>[] slots;
		final int modulo;
		this.modulo = modulo = (this.slots = slots = new Entry[XMath.pow2BoundMaxed(this.size = size)]).length - 1;
		for(int i = 0; i < source.length; i++)
		{
			// iterate through all entries and assign them to the new storage
			for(ConstMiniMap.Entry<K, V> entry = source[i]; entry != null; entry = entry.link)
			{
				slots[identityHashCode(entry.key) & modulo] =
					new Entry<>(entry.key, entry.value, slots[identityHashCode(entry.key) & modulo])
				;
			}
		}
	}

	@SuppressWarnings("unchecked")
	MiniMap(final int size, final Entry<K, V>[] source)
	{
		super();
		final Entry<K, V>[] slots;
		final int modulo;
		this.modulo = modulo = (this.slots = slots = new Entry[XMath.pow2BoundMaxed(this.size = size)]).length - 1;
		for(int i = 0; i < source.length; i++)
		{
			// iterate through all entries and assign them to the new storage
			for(Entry<K, V> entry = source[i]; entry != null; entry = entry.link)
			{
				slots[identityHashCode(entry.key) & modulo] =
					new Entry<>(entry.key, entry.value, slots[identityHashCode(entry.key) & modulo])
				;
			}
		}
	}



	///////////////////////////////////////////////////////////////////////////
	// declared methods //
	/////////////////////

	public int size()
	{
		return this.size;
	}

	public MiniMap<K, V> copy()
	{
		return new MiniMap<>(this.size, this.slots);
	}

	public ConstMiniMap<K, V> toConstMap()
	{
		return new ConstMiniMap<>(this.size, this.slots);
	}

	public V get(final K key)
	{
		for(Entry<K, V> e = this.slots[identityHashCode(key) & this.modulo]; e != null; e = e.link)
		{
			if(e.key == key)
			{
				return e.value;
			}
		}
		if(key == null)
		{
			throw new NullPointerException();
		}
		return null;
	}

	public boolean containsKey(final K key)
	{
		for(Entry<K, V> e = this.slots[identityHashCode(key) & this.modulo]; e != null; e = e.link)
		{
			if(e.key == key)
			{
				return true;
			}
		}
		return false;
	}

	private void increaseStorage()
	{
		if(XMath.isGreaterThanOrEqualHighestPowerOf2(this.slots.length))
		{
			return;
		}
		this.rebuildStorage(this.slots.length << 1);
	}

	@SuppressWarnings("unchecked")
	private void rebuildStorage(final int newSlotLength)
	{
		final Entry<K, V>[] slots = this.slots, newSlots = new Entry[newSlotLength];
		final int newModulo = newSlotLength - 1;
		for(int i = 0; i < slots.length; i++)
		{
			// iterate through all entries and assign them to the new storage
			for(Entry<K, V> e = slots[i], link; e != null; e = link)
			{
				link = e.link;
				e.link = newSlots[System.identityHashCode(e.key) & newModulo];
				newSlots[System.identityHashCode(e.key) & newModulo] = e;
			}
		}
		this.slots = newSlots;
		this.modulo = newModulo;
	}

	public V put(final K key, final V value)
	{
		for(Entry<K, V> e = this.slots[identityHashCode(key) & this.modulo]; e != null; e = e.link)
		{
			if(e.key == key)
			{
				final V oldValue = e.value;
				e.value = value;
				return oldValue;
			}
		}
		if(key == null)
		{
			throw new NullPointerException();
		}
		this.slots[identityHashCode(key) & this.modulo] =
			new Entry<>(key, value, this.slots[identityHashCode(key) & this.modulo])
		;
		if(this.size++ >= this.modulo)
		{
			this.increaseStorage(); // storage has to be increased, all entries have to be transfered to new storage
		}
		return null;
	}

	public MiniMap<K, V> putAll(@SuppressWarnings("unchecked") final KeyValue<K, V>... data)
	{
		Entry<K, V>[] slots = this.slots;
		int modulo = this.modulo;
		for(int i = 0; i < data.length; i++)
		{
			// iterate through all entries and assign them to the new storage
			final K key;
			if((key = data[i].key()) == null)
			{
				continue;
			}
			slots[identityHashCode(key) & modulo] =
				new Entry<>(key, data[i].value(), slots[identityHashCode(key) & modulo])
			;
			if(this.size++ >= modulo)
			{
				this.increaseStorage(); // storage has to be increased, all entries have to be transfered to new storage
				slots = this.slots;
				modulo = this.modulo;
			}
		}
		return this;
	}

	public V remove(final K key)
	{
		if(key == null)
		{
			throw new NullPointerException();
		}

		Entry<K, V> entry; // head entry special case
		if((entry = this.slots[identityHashCode(key) & this.modulo]).key == key)
		{
			this.slots[identityHashCode(key) & this.modulo] = entry.link;
			this.size--;
			return entry.value;
		}

		Entry<K, V> last; // iterate hash chain
		while((entry = (last = entry).link) != null)
		{
			if(entry.key == key)
			{
				last.link = entry.link;
				this.size--;
				return entry.value;
			}
		}

		return null; // key not found
	}

	public void optimize()
	{
		if(XMath.pow2BoundMaxed(this.size) < this.modulo)
		{
			this.rebuildStorage(XMath.pow2BoundMaxed(this.size));
		}
	}

	public void clear()
	{
		final Entry<K, V>[] slots = this.slots;
		for(int i = 0; i < slots.length; i++)
		{
			for(Entry<K, V> e = slots[i], link; e != null; e = link)
			{
				link = e.link;
				e.key = null;
				e.value = null;
				e.link = null;
			}
			slots[i] = null;
		}
		this.size = 0;
	}

	@SuppressWarnings("unchecked")
	public KeyValue<K, V>[] toArray()
	{
		final Entry<K, V>[] slots = this.slots;
		final int slotsLength = slots.length;
		final KeyValue<K, V>[] array = new KeyValue[this.size];
		int a = 0;

		for(int i = 0; i < slotsLength; i++)
		{
			for(Entry<K, V> e = slots[i]; e != null; e = e.link)
			{
				array[a++] = KeyValue.New(e.key, e.value);
			}
		}

		return array;
	}

	public int iterateValues(final Consumer<? super V> procedure)
	{
		for(Entry<K, V> entry : this.slots)
		{
			for(; entry != null; entry = entry.link)
			{
				procedure.accept(entry.value);
			}
		}
		return this.size;
	}


	static final class Entry<K, V> implements Composition
	{
		K key;
		V value;
		Entry<K, V> link;

		Entry(final K key, final V value, final Entry<K, V> link)
		{
			super();
			this.key = key;
			this.value = value;
			this.link = link;
		}

	}

}

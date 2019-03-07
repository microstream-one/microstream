package net.jadoth.collections;

import static java.lang.System.identityHashCode;

import net.jadoth.equality.IdentityEqualityLogic;
import net.jadoth.math.XMath;
import net.jadoth.typing.KeyValue;


/**
 * Immutable version of {@link MiniMap}.
 *
 * @author Thomas Muenz
 *
 * @param <K> the key type.
 * @param <V> the value type.
 */
public final class ConstMiniMap<K, V> implements IdentityEqualityLogic
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields  //
	/////////////////////

	private final Entry<K, V>[] slots;
	private final int modulo;
	private final int size;



	///////////////////////////////////////////////////////////////////////////
	// constructors     //
	/////////////////////

	@SuppressWarnings("unchecked")
	ConstMiniMap(final int size, final MiniMap.Entry<K, V>[] source)
	{
		super();
		final Entry<K, V>[] slots;
		final int modulo;
		this.modulo = modulo = (this.slots = slots = new Entry[XMath.pow2BoundMaxed(this.size = size)]).length - 1;
		for(int i = 0; i < source.length; i++)
		{
			// iterate through all entries and assign them to the new storage
			for(MiniMap.Entry<K, V> entry = source[i]; entry != null; entry = entry.link)
			{
				slots[identityHashCode(entry.key) & modulo] =
					new Entry<>(entry.key, entry.value, slots[identityHashCode(entry.key) & modulo])
				;
			}
		}
	}

	@SuppressWarnings("unchecked")
	ConstMiniMap(final int size, final Entry<K, V>[] source)
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


	@SuppressWarnings("unchecked")
	public ConstMiniMap()
	{
		super();
		this.size = 0;
		this.modulo = 0;
		this.slots = new Entry[0];
	}

	@SuppressWarnings("unchecked")
	public ConstMiniMap(final KeyValue<K, V>... data)
	{
		super();
		final Entry<K, V>[] slots;
		final int modulo;
		this.modulo = modulo = (this.slots = slots = new Entry[XMath.pow2BoundMaxed(data.length)]).length - 1;
		int size = 0;
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
			size++;
		}
		this.size = size;
	}



	///////////////////////////////////////////////////////////////////////////
	// declared methods //
	/////////////////////

	public int size()
	{
		return this.size;
	}

	public ConstMiniMap<K, V> copy()
	{
		return new ConstMiniMap<>(this.size, this.slots);
	}

	public MiniMap<K, V> toMiniMap()
	{
		return new MiniMap<>(this.size, this.slots);
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


	static final class Entry<K, V>
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

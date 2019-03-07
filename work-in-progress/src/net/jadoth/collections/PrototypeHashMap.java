package net.jadoth.collections;

import net.jadoth.exceptions.ArrayCapacityException;
import net.jadoth.math.XMath;
import net.jadoth.typing.Composition;


/**
 *
 * @author Thomas Muenz
 *
 */
@Deprecated
public final class PrototypeHashMap<E> implements Composition
{
	///////////////////////////////////////////////////////////////////////////
	// static methods   //
	/////////////////////

	public static final <T> PrototypeHashMap<T> New()
	{
		return new PrototypeHashMap<>(1);
	}

	public static final <T> PrototypeHashMap<T> New(final int initialStorageLength)
	{
		return new PrototypeHashMap<>(XMath.pow2BoundMaxed(initialStorageLength));
	}

	@SuppressWarnings("unchecked")
	private static <T> Entry<T>[] newSlots(final int slotsLength)
	{
		return new Entry[slotsLength];
	}



	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	private Entry<E>[] slots;
	private int        range;
	private int        size ;



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	private PrototypeHashMap(final int storageLength)
	{
		super();
		this.slots = newSlots(storageLength);
		this.range = storageLength >= Integer.MAX_VALUE ?Integer.MAX_VALUE :storageLength - 1;
	}



	///////////////////////////////////////////////////////////////////////////
	// declared methods //
	/////////////////////

	public final boolean add(final long key, final E value)
	{
		for(Entry<E> entry = this.slots[(int)(key & this.range)]; entry != null; entry = entry.link)
		{
			if(entry.key == key)
			{
				// add logic: discard new value (guaranteed single insert)
				return false;
			}
		}
		this.addEntry(key, value);
		return true;
	}

	public final boolean put(final long key, final E value)
	{
		for(Entry<E> entry = this.slots[(int)(key & this.range)]; entry != null; entry = entry.link)
		{
			if(entry.key == key)
			{
				entry.value = value; // put logic: replace old value (guaranteed insert)
				return false;
			}
		}
		this.addEntry(key, value);
		return true;
	}

	private void addEntry(final long key, final E value)
	{
		if(this.size >= this.range)
		{
			this.enlargeStorage();
		}
		this.slots[(int)(key & this.range)] = new Entry<>(key, value, this.slots[(int)(key & this.range)]);
		this.size++;
	}

	private void enlargeStorage()
	{
		if(this.size >= Integer.MAX_VALUE)
		{
			throw new ArrayCapacityException(1L<<31);
		}
		this.rebuildStorage((int)(this.slots.length * 2.0f));
	}

	private void rebuildStorage(final int newLength)
	{
		final int newRange = newLength == Integer.MAX_VALUE ?newLength :newLength - 1;
		final Entry<E>[] oldSlots = this.slots, newSlots = newSlots(newLength);
		for(int i = 0; i < oldSlots.length; i++)
		{
			if(oldSlots[i] == null)
			{
				continue;
			}
			for(Entry<E> next, entry = oldSlots[i]; entry != null; entry = next)
			{
				next = entry.link;
				entry.link = newSlots[(int)(entry.key & newRange)];
				newSlots[(int)(entry.key & newRange)] = entry;
			}
		}
		this.slots = newSlots;
		this.range = newRange;
	}

	public final E get(final long key)
	{
		for(Entry<E> entry = this.slots[(int)(key & this.range)]; entry != null; entry = entry.link)
		{
			if(entry.key == key)
			{
				return entry.value;
			}
		}
		return null;
	}

	public final void clear()
	{
		final Entry<E>[] hashSlots = this.slots;
		for(int i = 0, length = hashSlots.length; i < length; i++)
		{
			hashSlots[i] = null;
		}
		this.size = 0;
	}

	public final int size()
	{
		return this.size;
	}

	private static final class Entry<E> implements Composition
	{
		final long key  ;
		      E    value;
		Entry<E>   link ;

		Entry(final long key, final E value, final Entry<E> next)
		{
			super();
			this.key   = key  ;
			this.value = value;
			this.link  = next ;
		}

	}

}

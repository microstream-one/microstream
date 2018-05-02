package net.jadoth.experimental.collections;

import static java.lang.System.identityHashCode;

import java.lang.ref.WeakReference;
import java.util.function.Consumer;

import net.jadoth.Jadoth;
import net.jadoth.collections.BulkList;
import net.jadoth.collections.KeyValue;
import net.jadoth.collections.interfaces.OptimizableCollection;
import net.jadoth.collections.interfaces.Sized;
import net.jadoth.collections.types.XList;
import net.jadoth.functional._longProcedure;
import net.jadoth.math.JadothMath;

/**
 * Primitive (read: fast) pseudo map implementation that maps long id values to weakly referenced objects.
 *
 * @author Thomas Muenz
 *
 */
@Deprecated
public final class WeakHashMapObject_long<T> implements Sized, OptimizableCollection
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	private Entry<T>[] slots;
	private int modulo;
	private int size = 0;



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	@SuppressWarnings("unchecked")
	public WeakHashMapObject_long()
	{
		super();
		this.slots = new Entry[1];
		this.modulo = 0;
	}

	@SuppressWarnings("unchecked")
	public WeakHashMapObject_long(int slotSize)
	{
		super();
		slotSize = JadothMath.pow2BoundCapped(slotSize);
		this.slots = new Entry[slotSize];
		this.modulo = slotSize - 1;
	}



	///////////////////////////////////////////////////////////////////////////
	// override methods //
	/////////////////////

	/**
	 * @return the size
	 */
	@Override
	public long size()
	{
		return this.size;
	}

	/**
	 * @return
	 * @see net.jadoth.collections.interfaces.Sized#isEmpty()
	 */
	@Override
	public boolean isEmpty()
	{
		return this.size == 0;
	}



	///////////////////////////////////////////////////////////////////////////
	// declared methods //
	/////////////////////

	@SuppressWarnings("unchecked")
	private void internalOptimize()
	{
		int c = 0;
		final Entry<T>[] buffer = new Entry[this.size];
		for(Entry<T> entry : this.slots)
		{
			for(; entry != null; entry = entry.next)
			{
				if(entry.ref.get() != null)
				{
					buffer[c++] = entry;
				}
			}
		}

		final int newCapacity = JadothMath.pow2BoundCapped(c);
		final Entry<T>[] newSlots;
		final int newSlotCountMinusOne;
		if(newCapacity == this.slots.length)
		{
			if(c == this.size) return; // if capacity and size is the same, abort redundant optimization

			newSlots = this.slots;
			newSlotCountMinusOne = this.modulo;
			for(int i = 0; i < newCapacity; i++)
			{
				newSlots[i] = null; // clear slots array
			}
		}
		else
		{
			this.slots = newSlots = new Entry[newCapacity];
			this.modulo = newSlotCountMinusOne = newCapacity - 1;
		}
		this.size = c;

		int index;
		for(final Entry<T> entry : buffer)
		{
			entry.next = newSlots[index = identityHashCode(entry.ref.get()) & newSlotCountMinusOne];
			newSlots[index] = entry;
		}
	}

	public void put(final T object, final long id)
	{
		final int index;
		final Entry<T> head = this.slots[index = identityHashCode(object) & this.modulo];

		// case 1: new head entry
		if(head == null)
		{
			this.slots[index] = new Entry<>(id, object);
			if(this.size++ >= this.modulo)
			{
				this.internalOptimize();
			}
			return;
		}

		// case 2: replace registered object
		if(head.id == id)
		{
			head.ref = new WeakReference<>(object);
			return;
		}
		for(Entry<T> entry = head.next; entry != null; entry = entry.next)
		{
			if(entry.id == id)
			{
				entry.ref = new WeakReference<>(object);
				return;
			}
		}

		// case 3: append new entry
		this.slots[index] = new Entry<>(id, object, head);
		if(this.size++ >= this.modulo)
		{
			this.internalOptimize();
		}
	}


	public long get(final T object)
	{
		if(object == null)
		{
			throw new NullPointerException();
		}
		for(Entry<T> entry = this.slots[identityHashCode(object) & this.modulo]; entry != null; entry = entry.next)
		{
			if(entry.ref.get() == object)
			{
				return entry.id;
			}
		}
		return 0;
	}

	public XList<T> getObjects()
	{
		final BulkList<T> list = new BulkList<>(this.size);
		for(Entry<T> entry : this.slots)
		{
			for(T object; entry != null; entry = entry.next)
			{
				if((object = entry.ref.get()) != null)
				{
					list.add(object);
				}
			}
		}
		this.size = Jadoth.to_int(list.size());
		return list;
	}

	public XList<Long> getIds()
	{
		final BulkList<Long> list = new BulkList<>(this.size);
		for(Entry<T> entry : this.slots)
		{
			for(; entry != null; entry = entry.next)
			{
				if(entry.ref.get() != null)
				{
					list.add(entry.id);
				}
			}
		}
		this.size = Jadoth.to_int(list.size());
		return list;
	}

	public int iterateObjects(final Consumer<? super T> procedure)
	{
		int count = 0;
		for(Entry<T> entry : this.slots)
		{
			for(T object; entry != null; entry = entry.next)
			{
				if((object = entry.ref.get()) != null)
				{
					procedure.accept(object);
					count++;
				}
			}
		}
		this.size = count;
		return count;
	}

	/**
	 * Optimizes the internal storage and returns the remaining amount of entries.
	 * @return the amount of entries after the optimization is been completed.
	 */
	@Override
	public long optimize()
	{
		this.internalOptimize();
		return this.size;
	}

	public int iterateIds(final Consumer<? super Long> procedure)
	{
		int count = 0;
		for(Entry<T> entry : this.slots)
		{
			for(; entry != null; entry = entry.next)
			{
				if(entry.ref.get() != null)
				{
					procedure.accept(entry.id);
					count++;
				}
			}
		}
		this.size = count;
		return count;
	}

	public int iterateIds(final _longProcedure procedure)
	{
		int count = 0;
		for(Entry<T> entry : this.slots)
		{
			for(; entry != null; entry = entry.next)
			{
				if(entry.ref.get() != null)
				{
					procedure.accept(entry.id);
					count++;
				}
			}
		}
		this.size = count;
		return count;
	}

	public int iterate(final Consumer<KeyValue<? super T, ? super Long>> procedure)
	{
		int count = 0;
		for(Entry<T> entry : this.slots)
		{
			for(; entry != null; entry = entry.next)
			{
				if(entry.ref.get() != null)
				{
					procedure.accept(entry);
					count++;
				}
			}
		}
		this.size = count;
		return count;
	}

	public void clear()
	{
		final Entry<T>[] slots = this.slots;
		for(int i = 0, len = slots.length; i < len; i++)
		{
			slots[i] = null;
		}
		this.size = 0;
	}


	static final class Entry<T> implements KeyValue<T, Long>
	{
		final long id;
		WeakReference<T> ref;
		Entry<T> next;

		Entry(final long id, final T object, final Entry<T> next)
		{
			super();
			this.id = id;
			this.ref = new WeakReference<>(object);
			this.next = next;
		}

		Entry(final long id, final T object)
		{
			super();
			this.id = id;
			this.ref = new WeakReference<>(object);
			this.next = null;
		}

		@Override
		public T key()
		{
			return this.ref.get();
		}

		@Override
		public Long value()
		{
			return this.id;
		}

	}

}

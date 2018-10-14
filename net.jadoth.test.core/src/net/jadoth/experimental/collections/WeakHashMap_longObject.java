package net.jadoth.experimental.collections;

import static java.lang.System.identityHashCode;

import java.lang.ref.WeakReference;
import java.util.function.Consumer;

import net.jadoth.collections.BulkList;
import net.jadoth.collections.interfaces.OptimizableCollection;
import net.jadoth.collections.interfaces.Sized;
import net.jadoth.collections.types.XList;
import net.jadoth.functional._longProcedure;
import net.jadoth.math.XMath;
import net.jadoth.typing.XTypes;
import net.jadoth.typing.KeyValue;

/**
 * Primitive (read: fast) pseudo map implementation that maps long id values to weakly referenced objects.
 *
 * @author Thomas Muenz
 *
 */
@Deprecated
public final class WeakHashMap_longObject<T> implements Sized, OptimizableCollection
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
	public WeakHashMap_longObject()
	{
		super();
		this.slots  = new Entry[1];
		this.modulo = 0;
	}

	@SuppressWarnings("unchecked")
	public WeakHashMap_longObject(int slotSize)
	{
		super();
		slotSize = XMath.pow2BoundCapped(slotSize);
		this.slots = new Entry[slotSize];
		this.modulo = slotSize - 1;
	}



	///////////////////////////////////////////////////////////////////////////
	// override methods //
	/////////////////////

	@Override
	public long size()
	{
		return this.size;
	}

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

		final int newCapacity = XMath.pow2BoundCapped(c);
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

	public void put(final long id, final T object)
	{
		// (07.02.2011 TM)FIXME: overhaul like VarSet
		final int index;
		final Entry<T> head;

		// case 1: new head entry
		if((head = this.slots[index = (int)(id ^ id >>> 32) & this.modulo]) == null)
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

	public T get(final long id)
	{
		// ids are assumed to be roughly sequential, hence (id ^ id>>>32) should not be necessary for distribution.
		for(Entry<T> entry = this.slots[(int)(id & this.modulo)]; entry != null; entry = entry.next)
		{
			if(entry.id == id)
			{
				return entry.ref.get();
			}
		}
		return null;
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
		this.size = XTypes.to_int(list.size());
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
		this.size = XTypes.to_int(list.size());
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

	public int iterate(final Consumer<KeyValue<? super Long, ? super T>> procedure)
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


	static final class Entry<T> implements KeyValue<Long, T>
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
		public Long key()
		{
			return this.id;
		}

		@Override
		public T value()
		{
			return this.ref.get();
		}

	}

}

package net.jadoth.collections;

import static java.lang.System.identityHashCode;

import java.util.function.Consumer;

import net.jadoth.collections.interfaces.OptimizableCollection;
import net.jadoth.collections.interfaces.Sized;
import net.jadoth.collections.types.IdentityEqualityLogic;
import net.jadoth.collections.types.XList;
import net.jadoth.functional._longProcedure;
import net.jadoth.math.JadothMath;
import net.jadoth.util.Composition;
import net.jadoth.util.KeyValue;

/**
 * Primitive (read: fast) synchronized pseudo map implementation that maps long id values to weakly referenced objects.
 *
 * @author Thomas Muenz
 *
 */
public final class HashMapObjectId<T> implements Sized, OptimizableCollection, Composition, IdentityEqualityLogic
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	private Entry<T>[] slots;
	private int modulo;
	private int size;



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	@SuppressWarnings("unchecked")
	public HashMapObjectId()
	{
		super();
		this.slots  = new Entry[1];
		this.modulo = 0;
	}

	@SuppressWarnings("unchecked")
	public HashMapObjectId(final int slotSize)
	{
		super();
		final int cappedSlotSize = JadothMath.pow2BoundCapped(slotSize);
		this.slots = new Entry[cappedSlotSize];
		this.modulo = cappedSlotSize - 1;
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
	private void rebuild(final int newCapacity)
	{
		final Entry<T>[] newSlots = new Entry[newCapacity]; // potential int overflow ignored deliberately
		final int newSlotCountMinusOne = newCapacity - 1;
		int index;
		for(Entry<T> entry : this.slots)
		{
			for(Entry<T> next; entry != null; entry = next)
			{
				next = entry.next;
				entry.next = newSlots[index = identityHashCode(entry.ref) & newSlotCountMinusOne];
				newSlots[index] = entry;
			}
		}
		this.slots = newSlots;
		this.modulo = newSlotCountMinusOne;
	}

	public boolean add(final T object, final long id)
	{
		final int index;
		final Entry<T> head = this.slots[index = identityHashCode(object) & this.modulo];

		// case 1: new head entry
		if(head == null)
		{
			this.slots[index] = new Entry<>(id, object);
			if(this.size++ >= this.modulo)
			{
				this.rebuild(this.modulo + 1 << 1);
			}
			return true;
		}

		// case 2: replace registered id
		if(head.ref == object)
		{
			return false;
		}
		for(Entry<T> entry = head.next; entry != null; entry = entry.next)
		{
			if(entry.ref == object)
			{
				return false;
			}
		}

		// case 3: append new entry
		this.slots[index] = new Entry<>(id, object, head);
		if(this.size++ >= this.modulo)
		{
			this.rebuild(this.modulo + 1 << 1);
		}
		return true;
	}

	public boolean put(final T object, final long id)
	{
		final int index;
		final Entry<T> head = this.slots[index = identityHashCode(object) & this.modulo];

		// case 1: new head entry
		if(head == null)
		{
			this.slots[index] = new Entry<>(id, object);
			if(this.size++ >= this.modulo)
			{
				this.rebuild(this.modulo + 1 << 1);
			}
			return true;
		}

		// case 2: replace registered id
		if(head.ref == object)
		{
			head.id = id;
			return false;
		}
		for(Entry<T> entry = head.next; entry != null; entry = entry.next)
		{
			if(entry.ref == object)
			{
				entry.id = id;
				return false;
			}
		}

		// case 3: append new entry
		this.slots[index] = new Entry<>(id, object, head);
		if(this.size++ >= this.modulo)
		{
			this.rebuild(this.modulo + 1 << 1);
		}
		return true;
	}

	public long putGet(final T object, final long id)
	{
		final int index;
		final Entry<T> head = this.slots[index = identityHashCode(object) & this.modulo];

		// case 1: new head entry
		if(head == null)
		{
			this.slots[index] = new Entry<>(id, object);
			if(this.size++ >= this.modulo)
			{
				this.rebuild(this.modulo + 1 << 1);
			}
			return 0L;
		}

		// case 2: replace registered object
		if(head.ref == object)
		{
			final long oldId = head.id;
			head.id = id;
			return oldId;
		}
		for(Entry<T> entry = head.next; entry != null; entry = entry.next)
		{
			if(entry.ref == object)
			{
				final long oldId = entry.id;
				entry.id = id;
				return oldId;
			}
		}

		// case 3: append new entry
		this.slots[index] = new Entry<>(id, object, head);
		if(this.size++ >= this.modulo)
		{
			this.rebuild(this.modulo + 1 << 1);
		}
		return 0L;
	}

	public long get(final T object)
	{
		if(object == null)
		{
			throw new NullPointerException();
		}
		for(Entry<T> entry = this.slots[identityHashCode(object) & this.modulo]; entry != null; entry = entry.next)
		{
			if(entry.ref == object)
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
			for(; entry != null; entry = entry.next)
			{
				list.add(entry.ref);
			}
		}
		return list;
	}

	public XList<Long> getIds()
	{
		final BulkList<Long> list = new BulkList<>(this.size);
		for(Entry<T> entry : this.slots)
		{
			for(; entry != null; entry = entry.next)
			{
				list.add(entry.id);
			}
		}
		return list;
	}

	public int iterateObjects(final Consumer<? super T> procedure)
	{
		for(Entry<T> entry : this.slots)
		{
			for(; entry != null; entry = entry.next)
			{
				procedure.accept(entry.ref);
			}
		}
		return this.size;
	}

	/**
	 * Optimizes the internal storage and returns the remaining amount of entries.
	 * @return the amount of entries after the optimization is been completed.
	 */
	@Override
	public long optimize()
	{
		final int newCapacity;
		if((newCapacity = JadothMath.pow2BoundCapped(this.size)) != this.slots.length)
		{
			this.rebuild(newCapacity);
		}
		return this.size;
	}

	public int iterateIds(final Consumer<? super Long> procedure)
	{
		for(Entry<T> entry : this.slots)
		{
			for(; entry != null; entry = entry.next)
			{
				procedure.accept(entry.id);
			}
		}
		return this.size;
	}

	public int iterateIds(final _longProcedure procedure)
	{
		for(Entry<T> entry : this.slots)
		{
			for(; entry != null; entry = entry.next)
			{
				procedure.accept(entry.id);
			}
		}
		return this.size;
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


	static final class Entry<T> implements KeyValue<T, Long>, Composition
	{
		long id;
		final T ref;
		Entry<T> next;

		Entry(final long id, final T object, final Entry<T> next)
		{
			super();
			this.id = id;
			this.ref = object;
			this.next = next;
		}

		Entry(final long id, final T object)
		{
			super();
			this.id = id;
			this.ref = object;
			this.next = null;
		}

		@Override
		public T key()
		{
			return this.ref;
		}

		@Override
		public Long value()
		{
			return this.id;
		}

	}

}

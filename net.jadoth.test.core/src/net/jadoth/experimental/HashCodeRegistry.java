package net.jadoth.experimental;

import java.lang.ref.WeakReference;

import net.jadoth.exceptions.ArrayCapacityException;
import net.jadoth.hash.HashingType;

/**
 * <b>THIS IS PROBABLY NONSENSE AS ITS USE WOULD CREATE INCONSISTENCIES. STILL, MAYBE THERE MIGHT BE A FUTURE USE.</b>
 *
 *
 * This class associates the first hash code that is provided for each element instance with it and will return it
 * whenever queried for an already registered element instance, discarding any new hash code values that might be
 * provided for it.
 * <p>
 * The purpose of this registry is to provide constant (or immutable) hash code values for mutable elements
 * even if their {@link Object#hashCode()} implementation generates different hash codes when the element's internal
 * state changed.<br>
 * As a consequence, the so produced constant hash codes allow for hash collections to safely assume immutable
 * hash codes (see {@link HashingType#OBJECT_HASHCODE_IMMUTABLE}) without getting "confused" like JDK's hash
 * collections do (which is nothing else than a bug in their implementation).
 * <p>
 * Note that it is not necassary to use this class to manage hash codes of elements that already inherently
 * produce immutable hash codes (e.g. all classes that do not override {@link Object#hashCode()} and all classes
 * that are immutable as far as their hash code generation is concearned). This class is only meant for use with
 * elements whose classes' {@link Object#hashCode()} generates different hash code values for the same element.
 * <p>
 * The elements themselves are weak referenced, meaning the following consequences:
 * <ul>
 * <li>the use of this registry does not produce memory leaks by forever referencing actually no longer used instances.</li>
 * <li>registered elements can unpredictably "vanish", leaving empty entries that will be discarded
 * just as unpredictable (which should not be a problem because their elements are no longer used by the program
 * in the first place.)</li>
 * </ul>
 *
 *
 * @author Thomas Muenz
 *
 */
@Deprecated
public final class HashCodeRegistry<E>
{
	///////////////////////////////////////////////////////////////////////////
	// constants        //
	/////////////////////

	private static final int DEFAULT_INITIAL_CAPACITY = 16;
	private static final int MAXIMUM_CAPACITY         = 1 << 30;
	private static final float DEFAULT_LOAD_FACTOR    = 0.75f;



	///////////////////////////////////////////////////////////////////////////
	//  static methods  //
	/////////////////////

	private static String exceptionInitialCapacity(final int initialCapacity)
	{
		return "Illegal initial capacity: " + initialCapacity;
	}

	private static String exceptionLoadFactor(final float loadFactor)
	{
		return "Illegal load factor: " + loadFactor;
	}

	private static int calcCapacity(final int n)
	{
		if(n < 0)
		{
			throw new IllegalArgumentException(exceptionInitialCapacity(n));
		}
		else if(n > MAXIMUM_CAPACITY){
			return MAXIMUM_CAPACITY;
		}
		int p2 = DEFAULT_INITIAL_CAPACITY;
		while(p2 < n)
		{
			p2 <<= 1;
		}
		return p2;
	}

	private static float loadFactor(final float loadFactor)
	{
		if(loadFactor <= 0 || Float.isNaN(loadFactor))
		{
			throw new IllegalArgumentException(exceptionLoadFactor(loadFactor));
		}
		return loadFactor;
	}



	///////////////////////////////////////////////////////////////////////////
	// instance fields  //
	/////////////////////

	private Entry<E>[] slots;
	private int slotLengthMinusOne;

	private final float loadFactor;
	private int threshold;
	private int size;



	///////////////////////////////////////////////////////////////////////////
	// constructors     //
	/////////////////////

	public HashCodeRegistry()
	{
		super();
		this.slots = this.newEntries(DEFAULT_INITIAL_CAPACITY);
		this.slotLengthMinusOne = DEFAULT_INITIAL_CAPACITY - 1;
		this.loadFactor = DEFAULT_LOAD_FACTOR;
		this.threshold = (int)(DEFAULT_INITIAL_CAPACITY * DEFAULT_LOAD_FACTOR);
		this.size = 0;
	}

	public HashCodeRegistry(int initialCapacity)
	{
		super();
		initialCapacity = calcCapacity(initialCapacity);
		this.slots = this.newEntries(initialCapacity);
		this.slotLengthMinusOne = initialCapacity - 1;
		this.loadFactor = DEFAULT_LOAD_FACTOR;
		this.threshold = (int)(initialCapacity * DEFAULT_LOAD_FACTOR);
		this.size = 0;
	}

	public HashCodeRegistry(float loadFactor)
	{
		super();
		loadFactor = loadFactor(loadFactor);
		this.slots = this.newEntries(DEFAULT_INITIAL_CAPACITY);
		this.slotLengthMinusOne = DEFAULT_INITIAL_CAPACITY - 1;
		this.loadFactor = loadFactor;
		this.threshold = (int)(DEFAULT_INITIAL_CAPACITY * loadFactor);
		this.size = 0;
	}

	public HashCodeRegistry(int initialCapacity, float loadFactor)
	{
		super();
		initialCapacity = calcCapacity(initialCapacity);
		loadFactor = loadFactor(loadFactor);
		this.slots = this.newEntries(initialCapacity);
		this.slotLengthMinusOne = initialCapacity - 1;
		this.loadFactor = loadFactor;
		this.threshold = (int)(initialCapacity * loadFactor);
		this.size = 0;
	}



	///////////////////////////////////////////////////////////////////////////
	// declared methods //
	/////////////////////

	/**
	 * In contrast to common Maps' put() method, this method does not always set the new value (the passed hashCode
	 * in this case), but instead keeps an already existing hashCode for the passed element.<br>
	 * This effectively makes the hash code for the passed element constant.
	 *
	 * @param element
	 * @param hashCode
	 * @return
	 */
	public int put(final E element, final int hashCode)
	{
		if(element == null)
		{
			return 0; // null always has hash code 0, does not to be handled in the collection
		}

		final int hash = System.identityHashCode(element);

		// synchronize concurrency-critical part as HashCodeMaps have to be usable by multiple threads
		synchronized(this){
			Entry<E> last, entry = this.slots[hash & this.slotLengthMinusOne];

			// case: no entry chain in this slot, yet. So start one.
			if(entry == null)
			{
				this.incrementSize(); // incrementSize must stand BEFORE new entry creation
				this.slots[hash & this.slotLengthMinusOne] = new Entry<>(new Entry<>(element, hashCode));
				return hashCode;
			}

			// case: already registered (or scrolling to last entry in the chain)
			for(entry = (last = entry).next; entry != null; entry = (last = entry).next)
			{
				final E e = entry.ref.get();
				if(e == null){ // if current entry's element is gone, disjoint entry
					last.next = entry.next; // disjoint entry in the current chain
					entry = last; // funny: reverse-assign entry variable to current last. "Mark time" for one entry
					this.size--;
					continue;
				}
				// note that hash code checking is not needed because of identity comparison
				if(e == element)
				{
					return entry.hashCode; // element is already registered, return old hashCode, discard new one
				}
			}

			// case: not registered yet, append new entry to current last
			last.next = new Entry<>(element, hashCode);
			this.incrementSize(); // incrementSize must stand AFTER new entry creation
			// (26.12.2010)FIXME: dann ist aber eins zu viel drin
			return hashCode;
		}
	}


	private void removeEmptyEntries()
	{
		final Entry<E>[] slots = this.slots;
		int size = this.size;
		for(int i = 0, slotCount = slots.length; i < slotCount; i++)
		{
			Entry<E> last, entry = slots[i];
			if(entry == null) continue; // no entry chain at all in this oldEntries bucket, so go on

			// iterate through current chain's entries and disjoint all empty entries
			for(entry = (last = entry).next; entry != null; entry = (last = entry).next)
			{
				if(entry.ref.get() == null)
				{
					last.next = entry.next; // disjoint entry in the current chain
					entry = last; // funny: reverse-assign entry variable to current last. "Mark time" for one entry
					size--; // decrement size, locally for now
					// no further action is needed as current entry is no longer referenced and will get GCed.
				}
			}
		}
		this.size = size;
	}

	private void checkSize()
	{
		if(this.size == Integer.MIN_VALUE)
		{
			this.size--;
			throw new ArrayCapacityException();
		}
	}

	private void incrementSize() throws IndexOutOfBoundsException
	{
		// if still enough space, simply increment size and return
		if(this.size++ < this.threshold)
		{
			return;
		}

		// check for integer overflow, correct if necessary and throw exception, preventing addition of another element
		this.checkSize();

		// if storage already has maximum capacity, there's nothing to be increased anymore
		final int oldCapacity = this.slots.length;
		if(oldCapacity == MAXIMUM_CAPACITY)
		{
			this.threshold = Integer.MAX_VALUE;
			return;
		}

		// try to free up empty entries first before rebuilding storage
		this.removeEmptyEntries();

		// check if there's enough room now
		if(this.size < this.threshold) return;

		// finally, at this point, it is clear that the storage has to be increased
		this.rebuildStorage(oldCapacity<<1);
	}

	private void rebuildStorage(final int newSlotLength)
	{
		final Entry<E>[] oldSlots = this.slots;
		final int oldSlotLength = oldSlots.length;
		if(oldSlotLength == newSlotLength) return;

		final int newSlotLengthMinusOne = newSlotLength - 1;
		final Entry<E>[] newSlots = this.newEntries(newSlotLength);

		// iterate through all old storage slots and transfer every entry in occupied slots
		for(Entry<E> entry : oldSlots)
		{
			if(entry == null) continue; // no entry chain at all in this slot, so go on

			// for each entry e in the current entry chain, while keeping a reference to the next entry as well
			for(Entry<E> next = entry.next; next != null; next = (entry = next).next)
			{
				/* disjoint current entry from old chain before transfering it to the new storage
				 * Notes:
				 * 1.) the reference to the next old chain entry is not lost
				 * 2.) setNext() is intentionally not called to not falsely signal the
				 *     internal logic that the "next" entry shall be removed.
				 * 3.) empty entries are not removed intentionally. See consolidate().
				 */
				entry.next = null;

				final E element = entry.ref.get();
				if(element == null) continue; // discard empty entries

				// transfer current entry to the new storage
				final int newEntriesIndex = System.identityHashCode(element) & newSlotLengthMinusOne;
				Entry<E> newEntry = newSlots[newEntriesIndex];

				if(newEntry == null)
				{
					// case new head entry
					newSlots[newEntriesIndex] = new Entry<>(entry);
				}
				else
				{
					// case append to the end of an already created new entry chain
					while(newEntry.next != null)
					{
						newEntry = newEntry.next;
					}
					newEntry.next = entry;
				}
			}
		}
		this.slots = newSlots;
		this.threshold = (int)(newSlotLength * this.loadFactor);
		this.slotLengthMinusOne = newSlotLengthMinusOne;
	}

	@SuppressWarnings("unchecked")
	private Entry<E>[] newEntries(final int capacity)
	{
		return new Entry[capacity];
	}





	static final class Entry<E>
	{
		final int hashCode; // this is not the entry's hash as in normal hash collections but the "value" of the entry
		final WeakReference<E> ref;
		Entry<E> next;


		Entry(final Entry<E> next)
		{
			super();
			this.ref = null;
			this.hashCode = -1;
			this.next = next;
		}

		Entry(final E element, final int hashCode)
		{
			super();
			this.ref = new WeakReference<>(element);
			this.hashCode = hashCode;
			this.next = null;
		}

	}

}

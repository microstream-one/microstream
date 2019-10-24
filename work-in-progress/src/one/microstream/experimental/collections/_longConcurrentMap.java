package one.microstream.experimental.collections;
import static one.microstream.reflect.XReflect.getDeclaredField;

import java.util.Arrays;
import java.util.function.Consumer;

import one.microstream.X;
import one.microstream.collections.interfaces.Sized;
import one.microstream.memory.sun.MemoryAccessorSun;
import one.microstream.typing.Clearable;
import one.microstream.typing.KeyValue;
import one.microstream.typing._longKeyValue;
import sun.misc.Unsafe;


/**
 * A (mostly) lock-free concurrent map.
 * <p>
 * More precisely, locking is only used during storage rebuild and then only for modifying procedures.
 * So if the hash capacity is ensured to be big enough to avoid storage rebuilds while in use, modifying use is actually
 * completely lock-free. As is read-only use in any case.
 * <p>
 * This implementation uses {@link sun.misc.Unsafe} low level compareAndSwap functionality and thus requires a SUN
 * JRE runtime environment.
 *
 * @author Thomas Muenz
 *
 */
public final class _longConcurrentMap implements Clearable, Sized
{
	// ! \\ copied from ExperimentalLockFreeConcurrentHashMap. Maintain there!




	// (18.02.2012)XXX: check all "return 0L;" if it is a problem


	private static final Unsafe unsafe = (Unsafe)MemoryAccessorSun.getMemoryAccess();
	private static final long FIELD_ADDRESS_modLevel = unsafe.objectFieldOffset(getDeclaredField(_longConcurrentMap.class, "modLevel"));
	private static final long FIELD_ADDRESS_size = unsafe.objectFieldOffset(getDeclaredField(_longConcurrentMap.class, "size"));
	private static final long FIELD_ADDRESS_link = unsafe.objectFieldOffset(getDeclaredField(_longEntry.class, "link"));

	// stolen from AtomicReferenceArray
	private static final int ABO = unsafe.arrayBaseOffset(_longEntry[].class);
	private static final int AIS = unsafe.arrayIndexScale(_longEntry[].class);

	/* Funny thing:
	 * A combined array/chain storage would allow indefinite amount of entries.
	 * But size and toArray() are limited to max int amount of entries.
	 * Thus the map has to be as well.
	 * Now add() can't safely ensure to check for max int size due to concurrency (or the effort wouldn't be worth it)
	 * So an artifical max size is introduced, leaving the remaining ~500k as a concurrency buffer (should be enough ^^)
	 */
	private static final int MAX_SIZE = 2147000000;

	private static final int   MINIMUM_CAPACITY = 1024;
	private static final float DEFAULT_HASH_FACTOR = 1.20f;



	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////

	private static String exceptionHashDensity(final float hashDensity)
	{
		return "Illegal hash density: " + hashDensity;
	}

	private static int calculateCapacity(final int minimalCapacity)
	{
		if(minimalCapacity > 1<<30){ // JVM technical limit
			return Integer.MAX_VALUE;
		}
		int capacity = MINIMUM_CAPACITY;
		while(capacity < minimalCapacity)
		{
			capacity <<= 1;
		}
		return capacity;
	}

	private static float hashDensity(final float hashDensity)
	{
		if(hashDensity <= 0 || Float.isNaN(hashDensity))
		{
			throw new IllegalArgumentException(exceptionHashDensity(hashDensity));
		}
		return hashDensity;
	}




	private volatile _longEntry[] slots;
	private volatile float hashDensity;
	private volatile int size;
	private transient volatile int capacity;

	/**
	 * current concurrent modification level. Each simultaneously happening modification (add and remove)
	 * is guaranteed to increases this number by one before executing the modification and guaranteed to decreases it
	 * when the procedure is done.
	 * Storage rebuild will not commence if this number is not equal to 0 (this effectively means a required storage
	 * rebuild is postponed until all currently happening modifications are completed).
	 * <p>
	 * The logic using this variable is closely connected to {@link #wait}.
	 */
	private transient volatile int modLevel = 0; // note: not defended against 2^31 concurrently modifying threads :D

	/**
	 * Signaling flag indicating that modification procedures have to wait, mostly due to a storage rebuild.
	 */
	private transient volatile boolean wait = false; // mostly for rebuild, secondarly for stuff like toArray() etc.

	/**
	 * Special entry used a) as a monitor and b) for handling null key values.
	 */
	private final Object empty = new Object();


	public static final _longConcurrentMap New(final int initialSize, final float hashDensity)
	{
		return new _longConcurrentMap(calculateCapacity(initialSize), hashDensity(hashDensity));
	}


	public _longConcurrentMap()
	{
		this(MINIMUM_CAPACITY, DEFAULT_HASH_FACTOR);
	}

	private _longConcurrentMap(final int uncheckedInitialSize, final float uncheckedHashDensity)
	{
		super();
		this.capacity = (int)(
			(this.slots = new _longEntry[MINIMUM_CAPACITY]).length * (this.hashDensity = uncheckedHashDensity)
		);
		this.size = 0;
	}

	public void execute(final Consumer<? super _longKeyValue> procedure)
	{
		final _longEntry[] slots;
		final int length = (slots = this.slots).length;
		for(int i = 0; i < length; i++)
		{
			if(slots[i] == null) continue; // should be faster than setting up the for loop just to skip it again
			for(_longEntry e = slots[i]; e != null; e = e.link)
			{
				procedure.accept(e);
			}
		}
	}


	/**
	 * Utility method to ensure sufficient storage capacity in advance to avoid locking storage rebuilds.
	 *
	 * @param desiredCapacity the desired total capacity
	 * @see #ensureFreeCapacity(int)
	 */
	public _longConcurrentMap ensureCapacity(final int desiredCapacity)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME Auto-generated method stub, not implemented yet
	}

	public _longConcurrentMap ensureFreeCapacity(final int desiredFreeCapacity)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME Auto-generated method stub, not implemented yet
	}

	public void setHashDensity(final float hashDensity)
	{
		this.hashDensity = hashDensity(hashDensity);
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME rebuild
	}


	private void increaseStorage()
	{
		synchronized(this.empty){
			final int length, mod;
			final _longEntry[] slots, newSlots;
			mod = (newSlots = new _longEntry[(length = (slots = this.slots).length)<<1]).length - 1; // just for fun :D

			// copy all existing entries to new storage
			for(int i = 0; i < length; i++)
			{
				if(slots[i] == null) continue; // should be faster than setting up the for loop just to skip it again
				for(_longEntry e = slots[i]; e != null; e = e.link)
				{
					// volatile semantics procedures not needed due to synchronized block
					newSlots[e.hash & mod] = new _longEntry(e.key, e.value, newSlots[e.hash & mod]);
				}
			}

			// storage rebuild completion
			this.capacity = newSlots.length == 1<<30 ? Integer.MAX_VALUE : (int)(newSlots.length * this.hashDensity);
			System.out.println("slots = "+newSlots.length+", capacity = "+this.capacity);
			this.slots = newSlots;
			this.wait = false;
			this.empty.notifyAll();
		}
	}

	private void incrementSizeAndDecrementModLevel()
	{
		// only gets called if modLevel is > 0.
		int size;
		do {
			size = this.size;
		}
		while(!unsafe.compareAndSwapInt(this, FIELD_ADDRESS_size, size, size+1));

		if(size >= this.capacity && !this.wait)
		{
			this.wait = true; // doesn't hurt if it gets set to true more than once by concurrent threads
		}

		int modLevel;
		do { // concurrent race of atomic writes to get one decrement in
			modLevel = this.modLevel;
		}
		while(!unsafe.compareAndSwapInt(this, FIELD_ADDRESS_modLevel, modLevel, modLevel-1));
		if(this.wait && modLevel == 1){ // modLevel 1 (which got decremented to 0 in field) means last modifier thread
			this.increaseStorage(); // last modifier thread can safely rebuild storage and then turn off the wait
		}
	}

	private void decrementSize()
	{
		// only gets called if modLevel is > 0.
		int size;
		do {
			size = this.size;
		}
		while(!unsafe.compareAndSwapInt(this, FIELD_ADDRESS_size, size, size-1));
	}

	// (21.07.2011 TM)TODO: try if inlining decrementModLevel into size methods is faster
	private void decrementModLevel()
	{
		// only gets called if modLevel is > 0.
		int modLevel;
		do { // concurrent race of atomic writes to get one decrement in
			modLevel = this.modLevel;
		}
		while(!unsafe.compareAndSwapInt(this, FIELD_ADDRESS_modLevel, modLevel, modLevel-1));
		if(this.wait && modLevel == 1){ // modLevel 1 (which got decremented to 0 in field) means last modifier thread
			this.increaseStorage(); // last modifier thread can safely rebuild storage and then turn off the wait
		}
	}



	private static int hash(final long value)
	{
		// (18.02.2012)TODO could this be replaced by value & Integer.MAX_VALUE or so ?
		return (int)(value ^ value >>> 32);
	}

	public long put(final long key, final long value)
	{
		if(this.size >= MAX_SIZE)
		{
			throw new IndexOutOfBoundsException();
		}

		// register modification or recognize meanwhile appeared rebuild
		int modLevel;
		do{
			if(this.wait)
			{
				// accept locking for concurrent storage rebuilding corner case (can be prevented by ensuring capacity)
				synchronized(this.empty){
					// if rebuild already happened before acquiring the lock
					try
					{
						while(this.wait)
						{
							this.empty.wait(0); // prevent deadlock: wait for rebuild's notifyAll()
						}
					}
					catch(final InterruptedException e)
					{
						return 0L;
					}
				}
			}
		}
		while(!unsafe.compareAndSwapInt(this, FIELD_ADDRESS_modLevel, modLevel = this.modLevel, modLevel+1));

		// at this point the modification has been registered (so no storage rebuild can happen at the moment)

		final _longEntry[] slots; // caching in local field is always faster than >1 accesses to volatile field
		final long hashIndexOffset = ABO + AIS * (long)(hash(key) & (slots = this.slots).length-1);

		// full logic case: scan chain for already existing key and for concurrent chain entry insertion
		_longEntry head, new_longEntry = null;
		do{
			for(_longEntry e = head = (_longEntry)unsafe.getObjectVolatile(slots, hashIndexOffset); e != null; e = e.link)
			{
				if(e.key == key)
				{
					this.decrementModLevel();
					final long oldValue = e.value; // key already contained
					e.value = value;
					return oldValue;
				}
			}
			new_longEntry = new _longEntry(key, value);
		}
		while(!unsafe.compareAndSwapObject(slots, hashIndexOffset, new_longEntry.link = head, new_longEntry));
		/* Note:
		 * On every failed enqueuing attempt into the hash chain the hash chain has to be completely scanned through
		 * again, because it can't be guaranteed that only one entry was added into the chain meanwhile.
		 * This can cause considerable performance cost if the chain is very long,
		 * but should almost never be notable for good hash distributions (big enough capacity etc.)
		 */

		this.incrementSizeAndDecrementModLevel();
		return 0L; // entry has been successfully added to the hash chain
	}

	public long add(final long key, final long value)
	{
		if(this.size >= MAX_SIZE)
		{
			throw new IndexOutOfBoundsException();
		}

		// register modification or recognize meanwhile appeared rebuild
		int modLevel;
		do{
			if(this.wait)
			{
				// accept locking for concurrent storage rebuilding corner case (can be prevented by ensuring capacity)
				synchronized(this.empty){
					// if rebuild already happened before acquiring the lock
					try
					{
						while(this.wait)
						{
							this.empty.wait(0); // prevent deadlock: wait for rebuild's notifyAll()
						}
					}
					catch(final InterruptedException e)
					{
						return 0L;
					}
				}
			}
		}
		while(!unsafe.compareAndSwapInt(this, FIELD_ADDRESS_modLevel, modLevel = this.modLevel, modLevel+1));

		// at this point the modification has been registered (so no storage rebuild can happen at the moment)

		final _longEntry[] slots; // caching in local field is always faster than >1 accesses to volatile field
		final long hashIndexOffset = ABO + AIS * (long)(hash(key) & (slots = this.slots).length-1);

		// full logic case: scan chain for already existing key and for concurrent chain entry insertion
		_longEntry head, new_longEntry = null;
		do{
			for(_longEntry e = head = (_longEntry)unsafe.getObjectVolatile(slots, hashIndexOffset); e != null; e = e.link)
			{
				if(e.key == key)
				{
					this.decrementModLevel();
					return e.value;
				}
			}
			if(new_longEntry == null)
			{
				new_longEntry = new _longEntry(key, value);
			}
		}
		while(!unsafe.compareAndSwapObject(slots, hashIndexOffset, new_longEntry.link = head, new_longEntry));
		/* Note:
		 * On every failed enqueuing attempt into the hash chain the hash chain has to be completely scanned through
		 * again, because it can't be guaranteed that only one entry was added into the chain meanwhile.
		 * This can cause considerable performance cost if the chain is very long,
		 * but should almost never be notable for good hash distributions (big enough capacity etc.)
		 */

		this.incrementSizeAndDecrementModLevel();
		return 0L; // entry has been successfully added to the hash chain
	}

	public long get(final long key)
	{
		/* thread safe because:
		 * - read only
		 * - key can never change
		 * - change of value while entry is located is no problem, just a normal "happens before" modification
		 * - at worst e.link can concurrently flip to null, still no problem here
		 * - during rebuild: either retrieves old or new slots array, but both are consistent
		 */

		final _longEntry[] slots; // needed to ensure the right modulo is used and to avoid a second volatile load
		for(_longEntry e = (_longEntry)unsafe.getObjectVolatile(slots=this.slots, ABO + AIS*(hash(key) & slots.length-1)); e != null; e = e.link)
		{
			if(e.key == key){ // hashing is assumed to be well distributed if performance matters, peroid.
				return e.value;
			}
		}
		return 0L;
	}

	public long remove(final long key)
	{
		// register modification or recognize meanwhile appeared rebuild
		int modLevel;
		do{
			if(this.wait)
			{
				// accept locking for concurrent storage rebuilding corner case (can be prevented by ensuring capacity)
				synchronized(this.empty){
					// if rebuild already happened before acquiring the lock
					try
					{
						while(this.wait)
						{
							this.empty.wait(0); // prevent deadlock: wait for rebuild's notifyAll()
						}
					}
					catch(final InterruptedException e)
					{
						return 0L;
					}
				}
			}
		}
		while(!unsafe.compareAndSwapInt(this, FIELD_ADDRESS_modLevel, modLevel = this.modLevel, modLevel+1));

		_longEntry head;
		headcase:
		{ // common and simple case: head entry is the one to be removed (but tricky label construction :D)
			final _longEntry[] slots; // caching in local field is always faster than >1 accesses to volatile field
			final long hashIndexOffset = ABO + AIS * (long)(hash(key) & (slots = this.slots).length-1);
			do{
				if((head = (_longEntry)unsafe.getObjectVolatile(slots, hashIndexOffset)) == null)
				{
					this.decrementModLevel(); // note that completing remove procedures can do a storage rebuild
					return 0L;
				}
				if(head.key != key)
				{
					break headcase;
				}
			}
			while(!unsafe.compareAndSwapObject(slots, hashIndexOffset, head, head.link));
			this.decrementSize();
			this.decrementModLevel(); // note that completing remove procedures can do a storage rebuild
			return head.value;
		}

		/* Note:
		 * Any entry that has been inserted time-wise afterwards, chain-wise before the "head" found at this point
		 * is ignored intentionally. This can be seen as a part of the "happens-before" relationship between
		 * concurrent procedures: the (critial part of the) removing procedure already happened before the
		 * insertion of the new entry, so the remove attempt does not notice it.
		 * This can lead to the remove procedure completing with the removable entry left in the map, but due to
		 * the descrirbed timing considerations, this is intentional.
		 * In other words:
		 */

		chainscan:
		while(true)
		{
			for(_longEntry last, e = (last = head).link; e != null; e = (last = e).link)
			{
				if(e.key == key)
				{
					if(!unsafe.compareAndSwapObject(last, FIELD_ADDRESS_link, e, e.link))
					{
						continue chainscan; // restart chain scanning loop
					}
					this.decrementSize();
					this.decrementModLevel(); // note that completing remove procedures can do a storage rebuild
					// note: can't set e.link to null to ease GC because it might be concurrently used for iteration
					return e.value;
				}
			}
			return 0L; // key not found in hash chain, nothing to remove
		}
		/* Note:
		 * On every failed removal attempt from the hash chain the hash chain has to be completely scanned through
		 * again, because last no longer points to the correct entry.
		 * This can cause considerable performance cost if the chain is very long,
		 * but should almost never be notable for good hash distributions (big enough capacity etc.)
		 */
	}

	@Override
	public String toString()
	{
		return Arrays.toString(this.toArray());
	}

	@Override
	public long size()
	{
		return this.size;
	}


	/**
	 * See this method only as a secondary utility functionality, while actual element iteration should
	 * be done by using {@link #execute(Consumer)}.
	 * <p>
	 * Note that just like storage rebuild, this method waits for all modifying procedures to complete
	 * and blocks any new modifying procedures until it is completed itself.
	 * <p>
	 * Returns an array of {@link KeyValue} instances representing the entries of this map at the time
	 * the method has been called or {@code null} if the thread has been interrupted while waiting for the
	 * current modification procedures to complete.
	 *
	 * @return an array containing this map's entries or {@code null} if the thread has been interrupted.
	 */
	public _longKeyValue[] toArray()
	{
		synchronized(this.empty){
			this.wait = true;
			while(this.modLevel > 0)
			{
				try
				{
					// timed waiting is not nice but no idea how to do it otherwise for secondary procedure like this
					Thread.sleep(1);
				}
				catch(final InterruptedException e)
				{
					// waiting thread has been interrupted, so abort array creation. Not sure if best thing to do, though.
					return null;
				}
			}

			final _longKeyValue[] array = new _longKeyValue[this.size];
			final _longEntry[] slots = this.slots;
			final int length = this.slots.length;
			int a = 0;
			for(int i = 0; i < length; i++)
			{
				if(slots[i] == null) continue; // should be faster than setting up the for loop just to skip it again
				for(_longEntry e = slots[i]; e != null; e = e.link)
				{
					array[a++] = X._longKeyValue(e.key, e.value);
				}
			}
			this.wait = false;
			return array;
		}
	}

	@Override
	public boolean isEmpty()
	{
		return this.size == 0;
	}

	@Override
	public void clear()
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME LockFree_longMap#clear()
	}

}

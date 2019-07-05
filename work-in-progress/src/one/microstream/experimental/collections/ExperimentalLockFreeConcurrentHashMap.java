package one.microstream.experimental.collections;
import static one.microstream.reflect.XReflect.getDeclaredField;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Consumer;

import one.microstream.X;
import one.microstream.memory.XMemory;
import one.microstream.typing.KeyValue;
import sun.misc.Unsafe;


/**
 * A (mostly) lock-free concurrent map using identity-hashing /-equality.
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
 * @param <K>
 * @param <V>
 */
public final class ExperimentalLockFreeConcurrentHashMap<K,V> implements ConcurrentMap<K,V> // (19.07.2011)XXX: only for cliffclick tests
{
	private static final Unsafe unsafe = (Unsafe)XMemory.getSystemInstance();
	private static final long FIELD_ADDRESS_modLevel = unsafe.objectFieldOffset(getDeclaredField(ExperimentalLockFreeConcurrentHashMap.class, "modLevel"));
	private static final long FIELD_ADDRESS_size = unsafe.objectFieldOffset(getDeclaredField(ExperimentalLockFreeConcurrentHashMap.class, "size"));
	private static final long FIELD_ADDRESS_link = unsafe.objectFieldOffset(getDeclaredField(VolatileEntry.class, "link"));

	// stolen from AtomicReferenceArray
	private static final int ABO = unsafe.arrayBaseOffset(VolatileEntry[].class);
	private static final int AIS = unsafe.arrayIndexScale(VolatileEntry[].class);

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




	private volatile VolatileEntry[] slots;
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


	public static final <K, V> ExperimentalLockFreeConcurrentHashMap<K, V> create(final int initialSize, final float hashDensity)
	{
		return new ExperimentalLockFreeConcurrentHashMap<>(calculateCapacity(initialSize), hashDensity(hashDensity));
	}


	public ExperimentalLockFreeConcurrentHashMap()
	{
		this(MINIMUM_CAPACITY, DEFAULT_HASH_FACTOR);
	}

	private ExperimentalLockFreeConcurrentHashMap(final int uncheckedInitialSize, final float uncheckedHashDensity)
	{
		super();
		this.capacity = (int)(
			(this.slots = new VolatileEntry[MINIMUM_CAPACITY]).length * (this.hashDensity = uncheckedHashDensity)
		);
		this.size = 0;
	}



	@SuppressWarnings("unchecked")
	public void execute(final Consumer<? super KeyValue<K,V>> procedure)
	{
		final VolatileEntry[] slots;
		final int length = (slots = this.slots).length;
		for(int i = 0; i < length; i++)
		{
			if(slots[i] == null) continue; // should be faster than setting up the for loop just to skip it again
			for(VolatileEntry e = slots[i]; e != null; e = e.link)
			{
				procedure.accept((KeyValue<K, V>)e);
			}
		}
	}


	/**
	 * Utility method to ensure sufficient storage capacity in advance to avoid locking storage rebuilds.
	 *
	 * @param desiredCapacity the desired total capacity
	 * @see #ensureFreeCapacity(int)
	 */
	public ExperimentalLockFreeConcurrentHashMap<K, V> ensureCapacity(final int desiredCapacity)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME Auto-generated method stub, not implemented yet
	}

	public ExperimentalLockFreeConcurrentHashMap<K, V> ensureFreeCapacity(final int desiredFreeCapacity)
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
		System.out.println("increaseStorage");
		synchronized(this.empty){
			final int length, mod;
			final VolatileEntry[] slots, newSlots;
			mod = (newSlots = new VolatileEntry[(length = (slots = this.slots).length)<<1]).length - 1; // just for fun :D

			// copy all existing entries to new storage
			for(int hash, i = 0; i < length; i++)
			{
				if(slots[i] == null) continue; // should be faster than setting up the for loop just to skip it again
				for(VolatileEntry e = slots[i]; e != null; e = e.link)
				{
					// volatile semantics procedures not needed due to synchronized block
					newSlots[(hash = e.key.hashCode()) & mod] = new VolatileEntry(e.key, e.value, newSlots[hash & mod]);
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

	void incrementSize()
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

	@SuppressWarnings("unchecked")
	@Override
	public V put(final K key, final V value)
	{
		if(this.size >= MAX_SIZE)
		{
			throw new IndexOutOfBoundsException();
		}

		// (19.07.2011 TM)TODO: handle null key case

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
						return null;
					}
				}
			}
		}
		while(!unsafe.compareAndSwapInt(this, FIELD_ADDRESS_modLevel, modLevel = this.modLevel, modLevel+1));

		// at this point the modification has been registered (so no storage rebuild can happen at the moment)

		final VolatileEntry[] slots; // caching in local field is always faster than >1 accesses to volatile field
		final long hashIndexOffset = ABO + AIS * (long)(key.hashCode() & (slots = this.slots).length-1);

		// full logic case: scan chain for already existing key and for concurrent chain entry insertion
		VolatileEntry head, newVolatileEntry = null;
		do{
			for(VolatileEntry e = head = (VolatileEntry)unsafe.getObjectVolatile(slots, hashIndexOffset); e != null; e = e.link)
			{
				if(e.key.equals(key))
				{
					this.decrementModLevel();
					final V oldValue = (V)e.value; // key already contained
					e.value = value;
					return oldValue;
				}
			}
			newVolatileEntry = new VolatileEntry(key, value);
//			if(newVolatileEntry == null)
//			{
//				newVolatileEntry = new VolVolatileEntry(key, value);
//			}
//			else
//			{
//				System.out.println(this.size);
//			}
		}
		while(!unsafe.compareAndSwapObject(slots, hashIndexOffset, newVolatileEntry.link = head, newVolatileEntry));
		/* Note:
		 * On every failed enqueuing attempt into the hash chain the hash chain has to be completely scanned through
		 * again, because it can't be guaranteed that only one entry was added into the chain meanwhile.
		 * This can cause considerable performance cost if the chain is very long,
		 * but should almost never be notable for good hash distributions (big enough capacity etc.)
		 */

		this.incrementSizeAndDecrementModLevel();
		return null; // entry has been successfully added to the hash chain
	}


	@SuppressWarnings("unchecked")
	public V add(final K key, final V value)
	{
		if(this.size >= MAX_SIZE)
		{
			throw new IndexOutOfBoundsException();
		}
		// (19.07.2011 TM)TODO: handle null key case

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
						return null;
					}
				}
			}
		}
		while(!unsafe.compareAndSwapInt(this, FIELD_ADDRESS_modLevel, modLevel = this.modLevel, modLevel+1));

		// at this point the modification has been registered (so no storage rebuild can happen at the moment)

		final VolatileEntry[] slots; // caching in local field is always faster than >1 accesses to volatile field
		final long hashIndexOffset = ABO + AIS * (long)(key.hashCode() & (slots = this.slots).length-1);

		// full logic case: scan chain for already existing key and for concurrent chain entry insertion
		VolatileEntry head, newVolatileEntry = null;
		do{
			for(VolatileEntry e = head = (VolatileEntry)unsafe.getObjectVolatile(slots, hashIndexOffset); e != null; e = e.link)
			{
				if(e.key.equals(key))
				{
					this.decrementModLevel();
					return (V)e.value;
				}
			}
			if(newVolatileEntry == null)
			{
				newVolatileEntry = new VolatileEntry(key, value);
			}
		}
		while(!unsafe.compareAndSwapObject(slots, hashIndexOffset, newVolatileEntry.link = head, newVolatileEntry));
		/* Note:
		 * On every failed enqueuing attempt into the hash chain the hash chain has to be completely scanned through
		 * again, because it can't be guaranteed that only one entry was added into the chain meanwhile.
		 * This can cause considerable performance cost if the chain is very long,
		 * but should almost never be notable for good hash distributions (big enough capacity etc.)
		 */

		System.out.println("increment "+this.size+" for ("+key+" -> "+value+")");
		this.incrementSizeAndDecrementModLevel();
		return null; // entry has been successfully added to the hash chain
	}


	@SuppressWarnings("unchecked")
	@Override
	public V putIfAbsent(final K key, final V value)
	{
		if(this.size >= MAX_SIZE)
		{
			throw new IndexOutOfBoundsException();
		}
		// (19.07.2011 TM)TODO: handle null key case

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
						return null;
					}
				}
			}
		}
		while(!unsafe.compareAndSwapInt(this, FIELD_ADDRESS_modLevel, modLevel = this.modLevel, modLevel+1));

		// at this point the modification has been registered (so no storage rebuild can happen at the moment)

		final VolatileEntry[] slots; // caching in local field is always faster than >1 accesses to volatile field
		final long hashIndexOffset = ABO + AIS * (long)(key.hashCode() & (slots = this.slots).length-1);

		// full logic case: scan chain for already existing key and for concurrent chain entry insertion
		VolatileEntry head, newVolatileEntry = null;
		do{
			for(VolatileEntry e = head = (VolatileEntry)unsafe.getObjectVolatile(slots, hashIndexOffset); e != null; e = e.link)
			{
				if(e.key.equals(key))
				{
					this.decrementModLevel();
					return (V)e.value;
				}
			}
			if(newVolatileEntry == null)
			{
				newVolatileEntry = new VolatileEntry(key, value);
			}
		}
		while(!unsafe.compareAndSwapObject(slots, hashIndexOffset, newVolatileEntry.link = head, newVolatileEntry));
		/* Note:
		 * On every failed enqueuing attempt into the hash chain the hash chain has to be completely scanned through
		 * again, because it can't be guaranteed that only one entry was added into the chain meanwhile.
		 * This can cause considerable performance cost if the chain is very long,
		 * but should almost never be notable for good hash distributions (big enough capacity etc.)
		 */

		this.incrementSizeAndDecrementModLevel();
		return null; // entry has been successfully added to the hash chain
	}

	@Override
	@SuppressWarnings("unchecked")
	public V get(final Object key)
	{
		/* thread safe because:
		 * - read only
		 * - key can never change
		 * - change of value while entry is located is no problem, just a normal "happens before" modification
		 * - at worst e.link can concurrently flip to null, still no problem here
		 * - during rebuild: either retrieves old or new slots array, but both are consistent
		 */

		final VolatileEntry[] slots; // needed to ensure the right modulo is used and to avoid a second volatile load
		for(VolatileEntry e = (VolatileEntry)unsafe.getObjectVolatile(slots=this.slots, ABO + AIS*(key.hashCode() & slots.length-1)); e != null; e = e.link)
		{
			if(e.key.equals(key)){ // hashing is assumed to be well distributed if performance matters, peroid.
				return (V)e.value;
			}
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	@Override
	public V remove(final Object key)
	{
		// (19.07.2011 TM)TODO: handle null key case

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
						return null;
					}
				}
			}
		}
		while(!unsafe.compareAndSwapInt(this, FIELD_ADDRESS_modLevel, modLevel = this.modLevel, modLevel+1));

		VolatileEntry head;
		headcase:
		{ // common and simple case: head entry is the one to be removed (but tricky label construction :D)
			final VolatileEntry[] slots; // caching in local field is always faster than >1 accesses to volatile field
			final long hashIndexOffset = ABO + AIS * (long)(key.hashCode() & (slots = this.slots).length-1);
			do{
				if((head = (VolatileEntry)unsafe.getObjectVolatile(slots, hashIndexOffset)) == null)
				{
					this.decrementModLevel(); // note that completing remove procedures can do a storage rebuild
					return null;
				}
				if(!head.key.equals(key))
				{
					break headcase;
				}
			}
			while(!unsafe.compareAndSwapObject(slots, hashIndexOffset, head, head.link));
			this.decrementSize();
			this.decrementModLevel(); // note that completing remove procedures can do a storage rebuild
			return (V)head.value;
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
			for(VolatileEntry last, e = (last = head).link; e != null; e = (last = e).link)
			{
				if(e.key == key || e.key.equals(key))
				{
					if(!unsafe.compareAndSwapObject(last, FIELD_ADDRESS_link, e, e.link))
					{
						continue chainscan; // restart chain scanning loop
					}
					this.decrementSize();
					this.decrementModLevel(); // note that completing remove procedures can do a storage rebuild
					// note: can't set e.link to null to ease GC because it might be concurrently used for iteration
					return (V)e.value;
				}
			}
			return null; // key not found in hash chain, nothing to remove
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
	public int size()
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
	@SuppressWarnings("unchecked")
	public KeyValue<K,V>[] toArray()
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

			final KeyValue<K,V>[] array = new KeyValue[this.size];
			final VolatileEntry[] slots = this.slots;
			final int length = this.slots.length;
			int a = 0;
			for(int i = 0; i < length; i++)
			{
				if(slots[i] == null) continue; // should be faster than setting up the for loop just to skip it again
				for(VolatileEntry e = slots[i]; e != null; e = e.link)
				{
					array[a++] = X.KeyValue((K)e.key, (V)e.value);
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
	public boolean containsKey(final Object key)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME Auto-generated method stub, not implemented yet
	}

	@Override
	public boolean containsValue(final Object value)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME Auto-generated method stub, not implemented yet
	}

	@Override
	public void putAll(final Map<? extends K, ? extends V> m)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME Auto-generated method stub, not implemented yet
	}

	@Override
	public void clear()
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME Auto-generated method stub, not implemented yet
	}

	@Override
	public Set<K> keySet()
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME Auto-generated method stub, not implemented yet
	}

	@Override
	public Collection<V> values()
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME Auto-generated method stub, not implemented yet
	}

	@Override
	public Set<java.util.Map.Entry<K, V>> entrySet()
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME Auto-generated method stub, not implemented yet
	}

	@Override
	public boolean remove(final Object key, final Object value)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME Auto-generated method stub, not implemented yet
	}

	@Override
	public boolean replace(final K key, final V oldValue, final V newValue)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME Auto-generated method stub, not implemented yet
	}

	@Override
	public V replace(final K key, final V value)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME Auto-generated method stub, not implemented yet
	}

}


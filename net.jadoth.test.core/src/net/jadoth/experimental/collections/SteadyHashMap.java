package net.jadoth.experimental.collections;
import static net.jadoth.reflect.XReflect.getDeclaredField;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Consumer;

import net.jadoth.X;
import net.jadoth.collections.BulkList;
import net.jadoth.collections.XArrays;
import net.jadoth.memory.XMemory;
import net.jadoth.typing.XTypes;
import net.jadoth.typing.KeyValue;
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
public final class SteadyHashMap<K,V> implements ConcurrentMap<K,V> // (19.07.2011)XXX: only for cliffclick tests
{
	private static final Unsafe unsafe = (Unsafe)XMemory.getSystemInstance();
	private static final long FIELD_ADDRESS_size = unsafe.objectFieldOffset(getDeclaredField(SteadyHashMap.class, "size"));

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

	private static final int   MINIMUM_CAPACITY = 16;



	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////

	private static int calculateCapacity(final int minimalCapacity)
	{
		if(minimalCapacity > 1<<30){ // JVM technical limit
			return 1<<30;
		}
		int capacity = MINIMUM_CAPACITY;
		while(capacity < minimalCapacity)
		{
			capacity <<= 1;
		}
		return capacity;
	}



	private final VolatileEntry[] slots;
	private volatile int size;

	public SteadyHashMap(final int initialSize)
	{
		super();
		this.slots = new VolatileEntry[calculateCapacity(initialSize)];
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
	public SteadyHashMap<K, V> ensureCapacity(final int desiredCapacity)
	{
		throw new net.jadoth.meta.NotImplementedYetError(); // FIXME Auto-generated method stub, not implemented yet
	}

	public SteadyHashMap<K, V> ensureFreeCapacity(final int desiredFreeCapacity)
	{
		throw new net.jadoth.meta.NotImplementedYetError(); // FIXME Auto-generated method stub, not implemented yet
	}


	private void incrementSize()
	{
		// only gets called if modLevel is > 0.
		int size;
		do {
			size = this.size;
		}
		while(!unsafe.compareAndSwapInt(this, FIELD_ADDRESS_size, size, size+1));
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


	@SuppressWarnings("unchecked")
	@Override
	public V put(final K key, final V value)
	{
		if(value == null)
		{
			throw new NullPointerException();
		}
		if(this.size >= MAX_SIZE)
		{
			throw new IndexOutOfBoundsException();
		}
		// (19.07.2011 TM)TODO: handle null key case

		final VolatileEntry[] slots; // caching in local field is always faster than >1 accesses to volatile field
		final long hashIndexOffset = ABO + AIS * (long)(key.hashCode() & (slots = this.slots).length-1);

		VolatileEntry head, newVolatileEntry = null;
		do{
			for(VolatileEntry e = head = (VolatileEntry)unsafe.getObjectVolatile(slots, hashIndexOffset); e != null; e = e.link)
			{
				if(e.key.equals(key))
				{
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
		 * again, because it can't be guaranteed that only one VolatileEntry was added into the chain meanwhile.
		 * This can cause considerable performance cost if the chain is very long,
		 * but should almost never be notable for good hash distributions (big enough capacity etc.)
		 */

		this.incrementSize();
		return null; // VolatileEntry has been successfully added to the hash chain
	}


	@SuppressWarnings("unchecked")
	@Override
	public V putIfAbsent(final K key, final V value)
	{
		if(value == null)
		{
			throw new NullPointerException();
		}
		if(this.size >= MAX_SIZE)
		{
			throw new IndexOutOfBoundsException();
		}

		final VolatileEntry[] slots; // caching in local field is always faster than >1 accesses to volatile field
		final long hashIndexOffset = ABO + AIS * (long)(key.hashCode() & (slots = this.slots).length-1);

		VolatileEntry head, newVolatileEntry = null;
		do{
			for(VolatileEntry e = head = (VolatileEntry)unsafe.getObjectVolatile(slots, hashIndexOffset); e != null; e = e.link)
			{
				if(e.key.equals(key))
				{
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
		 * again, because it can't be guaranteed that only one VolatileEntry was added into the chain meanwhile.
		 * This can cause considerable performance cost if the chain is very long,
		 * but should almost never be notable for good hash distributions (big enough capacity etc.)
		 */

		this.incrementSize();
		return null; // VolatileEntry has been successfully added to the hash chain
	}

	@Override
	@SuppressWarnings("unchecked")
	public V get(final Object key)
	{
		/* thread safe because:
		 * - read only
		 * - key can never change
		 * - change of value while VolatileEntry is located is no problem, just a normal "happens before" modification
		 * - at worst e.link can concurrently flip to null, still no problem here
		 */
		for(VolatileEntry e = (VolatileEntry)unsafe.getObjectVolatile(this.slots, ABO + AIS*(key.hashCode() & this.slots.length-1)); e != null; e = e.link)
		{
			if(e.key.equals(key)){ // hashing is assumed to be well distributed if performance matters, peroid.
				return (V)e.value;
			}
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	private V internalRemove(final long hashIndexOffset, final Object key)
	{
		VolatileEntry last;
		for(VolatileEntry e = last = (VolatileEntry)unsafe.getObjectVolatile(this.slots, hashIndexOffset);
			e != null;
			e = (last = e).link
		){
			if(!e.key.equals(key))
			{
				continue;
			}
			synchronized(last){
				if(last.value != null){ // check if last has already been removed
					if(e == last){ // head VolatileEntry special case
						if(unsafe.compareAndSwapObject(this.slots, hashIndexOffset, e, e.link))
						{
							final V oldValue = (V)e.value;
							e.value = null;
							this.decrementSize();
							return oldValue;
						}
						// else fall through to retry
					}
					else if(last.link == e) { // check if e has already been removed meanwhile
						synchronized(e){
							last.link = e.link; // both links are ensured to
							final V oldValue = (V)e.value;
							e.value = null;
							this.decrementSize();
							return oldValue;
						}
					}
					else
					{
						return null; // VolatileEntry e has already been removed
					}
				}
			}
			// reaching here means last has been removed meanwhile or e has been swamped out of head position
			return this.internalRemove(hashIndexOffset, key);
		}
		return null; // key not found
	}

	@Override
	public V remove(final Object key)
	{
		return this.internalRemove(ABO + AIS*(long)(key.hashCode() & this.slots.length-1), key);
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
		final BulkList<KeyValue<K,V>> buffer = new BulkList<>(this.size);
		final VolatileEntry[] slots = this.slots;
		final int length = this.slots.length;
		for(int i = 0; i < length; i++)
		{
			if(slots[i] == null) continue; // should be faster than setting up the for loop just to skip it again
			for(VolatileEntry e = slots[i]; e != null; e = e.link)
			{
				buffer.add(X.KeyValue((K)e.key, (V)e.value));
			}
		}

		@SuppressWarnings("rawtypes")
		final KeyValue[] kv = new KeyValue[XTypes.to_int(buffer.size())];

		XArrays.copyTo(buffer, kv);

		return kv;
	}

	@Override
	public boolean isEmpty()
	{
		return this.size == 0;
	}

	@Override
	public boolean containsKey(final Object key)
	{
		throw new net.jadoth.meta.NotImplementedYetError(); // FIXME Auto-generated method stub, not implemented yet
	}

	@Override
	public boolean containsValue(final Object value)
	{
		throw new net.jadoth.meta.NotImplementedYetError(); // FIXME Auto-generated method stub, not implemented yet
	}

	@Override
	public void putAll(final Map<? extends K, ? extends V> m)
	{
		throw new net.jadoth.meta.NotImplementedYetError(); // FIXME Auto-generated method stub, not implemented yet
	}

	@Override
	public void clear()
	{
		throw new net.jadoth.meta.NotImplementedYetError(); // FIXME Auto-generated method stub, not implemented yet
	}

	@Override
	public Set<K> keySet()
	{
		throw new net.jadoth.meta.NotImplementedYetError(); // FIXME Auto-generated method stub, not implemented yet
	}

	@Override
	public Collection<V> values()
	{
		throw new net.jadoth.meta.NotImplementedYetError(); // FIXME Auto-generated method stub, not implemented yet
	}

	@Override
	public Set<java.util.Map.Entry<K, V>> entrySet()
	{
		throw new net.jadoth.meta.NotImplementedYetError(); // FIXME Auto-generated method stub, not implemented yet
	}

	@Override
	public boolean remove(final Object key, final Object value)
	{
		throw new net.jadoth.meta.NotImplementedYetError(); // FIXME Auto-generated method stub, not implemented yet
	}

	@Override
	public boolean replace(final K key, final V oldValue, final V newValue)
	{
		throw new net.jadoth.meta.NotImplementedYetError(); // FIXME Auto-generated method stub, not implemented yet
	}

	@Override
	public V replace(final K key, final V value)
	{
		throw new net.jadoth.meta.NotImplementedYetError(); // FIXME Auto-generated method stub, not implemented yet
	}

}


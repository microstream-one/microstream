package one.microstream.collections.lazy;

/*-
 * #%L
 * MicroStream Base
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

import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.Spliterator;
import java.util.function.Consumer;

import one.microstream.branching.ThrowBreak;
import one.microstream.reference.Lazy;
import one.microstream.reference.LazyClearObserver;
import one.microstream.reference.ObjectSwizzling;
import one.microstream.reference.ObservedLazyReference;

/**
 * This map implementation internally uses {@link Lazy} references internally,
 * to enable automatic partial loading of it's content.
 * <br><br>
 * Internally the key value pairs are kept in segments that are controlled by
 * {@link Lazy} references. The maxSegmentSize define the maximal desired number of
 * key / value pairs to be kept in a single segment. When loading data all elements of a segment
 * are loaded.
 * In case of hash collisions a
 * segment may exceed that desired maxSegmentSize. Key / value entries are sorted
 * ascending by the keys hash values.
 * <br><br>
 * This implementation requires an active microstream storage with specialized
 * type handlers. Without those handles a correct behavior is not guaranteed.
 * The required handlers are:
 * BinaryHandlerLazyHashMap
 * BinaryHandlerLazyHashMapSegmentEntryList
 * BinaryHandlerLazyObservable
 * <br><br>
 * The Map gets bound to a specific storage instance at the first store.
 * After the map has been persisted the first time it is no more possible to
 * persist it to a different storage. In that case an IllegalStateException
 * is thrown.
 * 
 * @param <K> Type of keys.
 * @param <V> Type of values.
 */
public class LazyHashMap<K, V> implements Map<K, V>
{
	private static final int MAX_SEGMENT_SIZE_DEFAULT = 1000;

	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	private final int maxSegmentSize;
	private final ArrayList<Segment<Entry<K, V>>> segments;
	private int size;
	private int modCount;
	private transient ObjectSwizzling loader;
	private final LazySegmentUnloader unloader;

	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	/**
	 * Creates a new {@link LazyHashMap} with a default maximum segment size of 1000.
	 */
	public LazyHashMap()
	{
		super();
		this.maxSegmentSize = MAX_SEGMENT_SIZE_DEFAULT;
		this.segments = new ArrayList<>();
		this.unloader = new LazySegmentUnloader.Default(5);
	}
	
	/**
	 * Creates a new {@link LazyHashMap} with a maximum desired segment size.
	 * The desired maximum segment size is not a hard limit. The map may exceed that
	 * limit.
	 * 
	 * @param maxSegmentSize maximum desired segment size, must be non negative.
	 */
	public LazyHashMap(final int maxSegmentSize)
	{
		super();
		
		if(maxSegmentSize < 0) {
			throw new IllegalArgumentException("Illegal maxSegmentSize: " + maxSegmentSize + ". Must be 0 or greater!");
		}
		
		this.maxSegmentSize = maxSegmentSize;
		this.segments = new ArrayList<>();
		this.unloader = new LazySegmentUnloader.Default(5);
	}
	
	/**
	 * Creates a new {@link LazyHashMap} with a maximum desired segment size.
	 * The desired maximum segment size is not a hard limit. The map may exceed that
	 * limit.
	 * 
	 * @param maxSegmentSize maximum desired segment size, must be non negative.
	 * @param lazySegmentUnloader LazySegmentUnloader instance
	 */
	public LazyHashMap(final int maxSegmentSize, final LazySegmentUnloader lazySegmentUnloader)
	{
		super();
		
		if(maxSegmentSize < 0) {
			throw new IllegalArgumentException("Illegal maxSegmentSize: " + maxSegmentSize + ". Must be 0 or greater!");
		}
		
		this.maxSegmentSize = maxSegmentSize;
		this.segments = new ArrayList<>();
		this.unloader = lazySegmentUnloader;
	}
	
	/**
	 * Creates a new copy from the supplied  {@link LazyHashMap}.
	 * <br>
	 * The key and value Objects of the source map will not be copied,
	 * both maps will reference the same key and value object instances.
	 * 
	 * @param map to be copied.
	 */
	public LazyHashMap(final LazyHashMap<K, V> map)
	{
		super();
		this.maxSegmentSize = map.maxSegmentSize;
		this.segments = new ArrayList<>();
		this.unloader = map.unloader.copy();
		this.putAll(map);
	}
		
	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////

	/**
	 * Returns the current number of internal segments.
	 * 
	 * @return the current number of internal segments.
	 */
    public long getSegmentCount()
	{
		return this.segments.size();
	}
    
    /**
     * Returns an Iterable over the Segment in this list.
     * 
     * @return an Iterable over the Segment in this list
     */
	public Iterable<? extends Segment<?>> segments()
	{
		return this.segments;
	}
    
    /**
	 * Returns the maximum segment size of this {@link LazyArrayList}.
	 * 
	 * @return the maximum segment size of this {@link LazyArrayList}
	 */
	public int getMaxSegmentSize()
	{
		return this.maxSegmentSize;
	}
	
	/**
	 * Hash function used in that map implementation.
	 * 
	 * @param key Object to calculate the has for.
	 * @return hash.
	 */
	protected int hash(final Object key)
	{
		int h;
        return key == null ? 0 : (h = key.hashCode()) ^ h >>> 16;
	}
		
    /**
     * Do a binary search for the segment that contains the supplied hash.
     *
     * @param hash index to be searched for.
     * @param lowSegmentIndex lower limit to search within.
     * @param highSegmentIndex upper limit to search within.
     * @return Segment containing the hash or null.
     */
	private Segment<Entry<K, V>> searchSegment(final int hash, final int lowSegmentIndex, final int highSegmentIndex)
	{
		if(this.segments.size() < 1)
		{
			return null;
		}
		
		int hi = highSegmentIndex;
		int lo = lowSegmentIndex;
		
		while (lo <= hi)
		{
			final int mid = lo  + (hi - lo) / 2;
			final Segment<Entry<K, V>> segment = this.segments.get(mid);
			final int cmp = segment.compareHash(hash);
			if(cmp == 0)
			{
				return segment;
			}
			else if(cmp < 0)
			{
				hi = mid - 1;
			}
			else
			{
				lo = mid + 1;
			}
            
    	}
    	//should not be reached as there should be at least one segment that covers the whole hash range
		throw new NoSuchElementException("No segment found for hash " + hash);
    }

	private Entry<K, V> insert(final Entry<K, V> entry)
	{
		Segment<Entry<K, V>> segment = this.searchSegment(entry.hash, 0, this.segments.size());
		
		if(segment == null)
		{
			segment = new Segment<>(this.maxSegmentSize);
			this.segments.add(segment);
		}
		
		final Entry<K, V> retVal = segment.insert(entry);

		// split required?
		if (segment.segmentSize > this.maxSegmentSize)
		{
			final int smin = segment.getData().get(0).hash;
			final int smax = segment.getData().get(segment.segmentSize - 1).hash;
			final int mid = (int) (((long)smin + (long)smax) / 2);

			final int splitIndex = segment.findNextPosition(mid);

			if (splitIndex == 0 || splitIndex > segment.segmentSize-1)
			{
				return retVal;
			}

			final Segment<Entry<K, V>> newSegment = segment.split(splitIndex);

			newSegment.min = mid;
			newSegment.max = segment.max;
			segment.max = mid;

			this.segments.add(this.segments.indexOf(segment) + 1, newSegment);
		}

		return retVal;
	}
    
	private Entry<K, V> getByHash(final Object key)
	{
		final int hash = this.hash(key);
		final LazyHashMap<K, V>.Segment<Entry<K, V>> segment = this.searchSegment(hash, 0, this.segments.size());
		
		if(segment == null)
		{
			return null;
		}
		
		return segment.getByHash(hash, key);
	}

	@Override
	public int size()
	{
		return this.size;
	}

	@Override
	public boolean isEmpty()
	{
		return this.size < 1;
	}

	@Override
	public boolean containsKey(final Object key)
	{
		return this.getByHash(key) != null;
	}

	@Override
	public boolean containsValue(final Object value)
	{
		for (final Segment<Entry<K, V>> segment : this.segments)
		{
			for (final Entry<K, V> entry : segment.getData())
			{
				if (entry.value == value || entry.value != null && entry.value.equals(value))
				{
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public V get(final Object key)
	{
		final Entry<K, V> result = this.getByHash(key);
		if(result == null)
		{
			return null;
		}
		return result.value;
	}

	@Override
	public V put(final K key, final V value)
	{
		this.modCount++;
		final Entry<K, V> oldEntry = this.insert(new Entry<>(this.hash(key), key, value));
		if(oldEntry != null)
		{
			return oldEntry.value;
		}
		this.size++;
		return null;
	}

	@Override
	public V remove(final Object key)
	{
		
		if(this.segments.size() < 1)
		{
			return null;
		}
			
		final int hash = this.hash(key);

		final LazyHashMap<K, V>.Segment<Entry<K, V>> s = this.searchSegment(hash, 0, this.segments.size());
		final Optional<V> removedValue = s.remove(key);
		if (removedValue == null)
		{
			return null;
		}

		this.size--;
		this.modCount++;

		this.removeSegmentIfEmpty(s);
		return removedValue.orElse(null);
	}
	
	@Override
	public V replace(final K key, final V value)
	{
		if(this.segments.size() < 1)
		{
			return null;
		}
		
		final int hash = this.hash(key);

		final LazyHashMap<K, V>.Segment<Entry<K, V>> s = this.searchSegment(hash, 0, this.segments.size());
		final Optional<V> replacedValue = s.replace(hash, key, value);

		if (replacedValue == null)
		{
			return null;
		}
		
		this.modCount++;
		return replacedValue.orElse(null);
	}
	
	@Override
	public boolean replace(final K key, final V oldValue, final V newValue)
	{
		if(this.segments.size() < 1)
		{
			return false;
		}
		
		final int hash = this.hash(key);

		final LazyHashMap<K, V>.Segment<Entry<K, V>> s = this.searchSegment(hash, 0, this.segments.size());
		final boolean replaced = s.replace(hash, key, oldValue, newValue);
		
		if (replaced)
		{
			this.modCount++;
		}

		return replaced;
	}

	/**
	 * Checks if segment is empty and removes if empty.
	 * 
	 * @param segment segment to be checked.
	 * @return true if segment has been removed, otherwise false.
	 */
	private boolean removeSegmentIfEmpty(final LazyHashMap<K, V>.Segment<Entry<K, V>> segment)
	{
		if (segment.segmentSize < 1)
		{
			final int index = this.segments.indexOf(segment);
			if (index > 0)
			{
				final LazyHashMap<K, V>.Segment<Entry<K, V>> left = this.segments.get(index - 1);
				left.max = segment.max;
			}
			else
			{
				if (this.segments.size() > 1)
				{
					final LazyHashMap<K, V>.Segment<Entry<K, V>> right = this.segments.get(index + 1);
					right.min = segment.min;
				}
			}
			this.segments.remove(index);
			this.unloader.remove(segment);
			return true;
		}
		return false;
	}
	
	@Override
	public void putAll(final Map<? extends K, ? extends V> m)
	{
		for (final Map.Entry<? extends K, ? extends V> e : m.entrySet())
		{
			final K key = e.getKey();
			final V value = e.getValue();
			this.put(key, value);
		}

	}

	@Override
	public void clear()
	{
		this.segments.clear();
		this.size = 0;
		this.modCount++;
	}

	@Override
	public LazySet<K> keySet()
	{
		return new KeySet();
	}

	@Override
	public LazyCollection<V> values()
	{
		return new Values();
	}

	@Override
	public LazySet<Map.Entry<K, V>> entrySet()
	{
		return new EntrySet();
	}
	
	
	/**
	 * Returns the string representation of this map.
	 * Key-value pairs will be coded as 'key'='value'.
	 * They are grouped by the internal segments.
	 * If internal segments are unloaded they will not get loaded,
	 * the key-value pairs stored in those segments will not be included
	 * in the returned string. Unloaded segments just report the number
	 * of unloaded contained elements.
	 * 
	 * @return a string representation of this map.
	 */
	@Override
	public String toString()
	{
		final Iterator<? extends LazyHashMap<K, V>.Segment<?>> segmentsIterator = this.segments().iterator();
		if(!segmentsIterator.hasNext())
		{
			return "{}";
		}
		
		final StringBuilder sb = new StringBuilder();
		sb.append('{');
		for(;;)
		{
			final LazyHashMap<K, V>.Segment<?> s = segmentsIterator.next();
			sb.append(s.toString());
			if(!segmentsIterator.hasNext()) {
				 return sb.append('}').toString();
			}
			sb.append(',').append(' ');
		}
	}

	//required by BinaryHandlerLazyHashMap
	@SuppressWarnings({ "unchecked", "unused" })
	private void addSegment(final int min, final int max, final int segmentSize, final Object data)
	{
		this.segments.add(new Segment<>(min, max, segmentSize, (ObservedLazyReference<LazyHashMapSegmentEntryList<K, V>>) data));
	}
	
	/**
	 * Helper class to transfer segment and indices information
	 *
	 */
	private static class IndexPosition
	{
		int segmentIndex;
		int segmentStartIndex;
		int segmentSize;

		public IndexPosition(final int segmentIndex, final int segmentStartIndex, final int segmentSize)
		{
			super();
			this.segmentIndex = segmentIndex;
			this.segmentStartIndex = segmentStartIndex;
			this.segmentSize = segmentSize;
		}
	}

	private IndexPosition calculateIndexPosition(final int index)
	{
		Objects.checkIndex(index, this.size);

		int segmentIndex = 0;
		int segmentStartIndex = 0;

		for (final Segment<Entry<K, V>> segment : this.segments)
		{
			if (segmentStartIndex + segment.segmentSize - 1 >= index)
			{
				return new IndexPosition(segmentIndex, segmentStartIndex, segment.segmentSize);
			}

			segmentIndex++;
			segmentStartIndex += segment.segmentSize;
		}

		throw new NoSuchElementException("Can't determine IndexPosition for index " + index);
	}
	
	/**
	 * Links the map to a {@link ObjectSwizzling} instance.
	 * 
	 * @param objectLoader ObjectSwizzling instance.
	 */
	public void link(final ObjectSwizzling objectLoader)
	{
		if(this.loader != null)
		{
			return;
		}
		this.loader = objectLoader;
	}
	
	/**
 	 * Throws an IllegalStateException if the current loader is
 	 * not null and is not the provided one.
 	 *
	 * @param objectLoader to be verified.
	 */
	public void verifyLoader(final ObjectSwizzling objectLoader)
	{
		if(this.loader != null && this.loader != objectLoader)
		{
			throw new IllegalStateException("Map already bound to an other storage!");
		}
	}
	
	public static class LazyHashMapSegmentEntryList<K, V> extends ArrayList<Entry<K, V>>
	{
		public LazyHashMapSegmentEntryList(final int initialCapacity)
		{
			super(initialCapacity);
		}

		@SuppressWarnings("unchecked")
		public void addEntry(final int hash, final Object key, final Object value)
		{
			this.add(new Entry<>(hash, (K) key, (V) value));
		}
	}
	
	public class Segment<E extends Entry<K, V>> implements LazyClearObserver, LazySegment<LazyHashMapSegmentEntryList<K,V>>
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private final ObservedLazyReference<LazyHashMapSegmentEntryList<K, V>> data;
		private int min;
		private int max;
		private int segmentSize;
		private transient boolean modified;
		private boolean allowUnloading = true;

		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		/**
		 * Default constructor
		 */
		Segment(final int initialCapacity)
		{
			super();
			this.data = Lazy.register(new ObservedLazyReference.Default<>(
				new LazyHashMapSegmentEntryList<>(initialCapacity),
				this));

			this.min = Integer.MIN_VALUE;
			this.max = Integer.MAX_VALUE;
			this.modified = true;
		}

		Segment(final int min, final int max, final int segmentSize, final ObservedLazyReference<LazyHashMapSegmentEntryList<K, V>> data)
		{
			super();
			this.min = min;
			this.max = max;
			this.segmentSize = segmentSize;
			this.data = data;
			this.data.setClearObserver(this);
		}
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		@Override
		public int size()
		{
			return this.segmentSize;
		}
		
		@Override
		public boolean isLoaded()
		{
			return this.data.isLoaded();
		}
		
		@Override
		public boolean isModified()
		{
			return this.modified;
		}

		@Override
		public void unloadSegment()
		{
			this.data.clear();
		}
		
		//required by BinaryHandlerLazyHashMap
		@SuppressWarnings("unused")
		private void cleanModified()
		{
			this.modified = false;
		}
		
		//required by BinaryHandlerLazyHashMap
		@SuppressWarnings("unused")
		private Lazy<LazyHashMapSegmentEntryList<K, V>> getLazy()
		{
			return this.data;
		}
		
		@Override
		public LazyHashMapSegmentEntryList<K, V> getData()
		{
			LazyHashMap.this.unloader.unload(this);
			return this.data.get();
		}
		
		//required by BinaryHandlerLazyArrayList
		@SuppressWarnings("unused")
		private LazyHashMapSegmentEntryList<K, V> getLazyData()
		{
			return this.data.get();
		}
		
		/**
		 * Compares the supplied hash to with the segments min and max properties.
		 * 
		 * @param hash to be compared with.
		 * @return hash &lt; 0: -1 <br> hash &gt;= max: 1 <br> min &lt; hash &lt; max: 0.
		 */
		public int compareHash(final int hash)
		{
			if(hash < this.min)
			{
				return -1;
			}
			if(hash >= this.max)
			{
				return 1;
			}
			return 0;
		}
		
		/**
		 * Search for an entry by its keys hash and value.
		 * 
		 * @param hash the key hash
		 * @param key the key value
		 * @return found entry or null
		 */
		public Entry<K, V> getByHash(final int hash, final Object key)
		{
			final LazyHashMapSegmentEntryList<K, V> entries = this.getData();

			for (final Entry<K, V> entry : entries)
			{
				if (entry.hash == hash && entry.key == null || entry.key != null && entry.key.equals(key))
				{
					return entry;
				}
			}

			return null;
		}
		
		private Optional<V> remove(final Object key)
		{
			final LazyHashMapSegmentEntryList<K, V>  entries = this.getData();
			for (final Entry<K, V> entry : entries)
			{
				if (entry.key == null || entry.key.equals(key))
				{
					entries.remove(entry);
					this.modified = true;
					this.segmentSize--;
					return Optional.ofNullable(entry.value);
				}
			}
			return null;

		}
		
		private Entry<K, V> remove(final int i)
		{
			this.segmentSize--;
			this.modified = true;
			return this.getData().remove(i);
		}
		
		private  Entry<K, V> insert(final E entry)
		{
			final LazyHashMapSegmentEntryList<K, V> entries = this.getData();
			final LazyHashMapSegmentEntryList<K, V> e = this.getData();
			
			for (int i = 0; i < e.size(); i++)
			{
				if (e.get(i).hash > entry.hash)
				{
					entries.add(i, entry);
					this.modified = true;
					this.segmentSize++;
					return null;
				}
				else if(e.get(i).hash == entry.hash)
				{
					final K key = e.get(i).key;
					if(key == null || key.equals(entry.key))
					{
						this.modified = true;
						return entries.set(i, entry);
					}
					entries.add(i, entry);
					this.modified = true;
					this.segmentSize++;
					return null;
				}
			}
			entries.add(entry);
						
			this.modified = true;
			this.segmentSize++;
			return null;
		}
		
		private Optional<V> replace(final int hash, final K key, final V value)
		{
			final Entry<K, V> current = this.getByHash(hash, key);
			if(current != null)
			{
				this.modified = true;
				final V old = current.value;
				current.value = value;
				return Optional.ofNullable(old);
			}
			return null;
			
		}
		
		private boolean replace(final int hash, final K key, final V oldValue, final V newValue)
		{
			final Entry<K, V> current = this.getByHash(hash, key);
			if(current != null && current.value.equals(oldValue))
			{
				current.value = newValue;
				this.modified = true;
				return true;
			}
			return false;
		}
		
		@Override
		public boolean allowClear()
		{
			return !this.modified;
		}

		/**
		 * Search position the first entry with an equal or greater hash then the
		 * provided one.
		 * 
		 * @param hash input hash.
		 * 
		 * @return index of the first element with an equal or greater hash
		 */
		private int findNextPosition(final int hash)
		{
			final LazyHashMapSegmentEntryList<K, V> e = this.getData();
			for (int i = 0; i < e.size(); i++)
			{
				if (e.get(i).hash >= hash)
				{
					return i;
				}
			}
			return e.size();
		}

		private Segment<Entry<K, V>> split(final int index)
		{
			final LazyHashMapSegmentEntryList<K, V> e = this.getData();
			final List<Entry<K, V>> part = e.subList(index, e.size());

			final Segment<Entry<K, V>> newSegment = new Segment<>(0);
			newSegment.getData().addAll(part);
			newSegment.segmentSize = newSegment.getData().size();

			e.removeAll(part);
			this.segmentSize = e.size();

			return newSegment;
		}

		/**
		 * Returns the string representation of this segment.
		 * Key-value pairs will be coded as 'key'='value'.
		 * If the segment is unloaded it will not get loaded,
		 * the key-value pairs stored in an unloaded segment will not be included
		 * in the returned string. Instead, it just reports the number of unloaded
		 * elements.
		 * 
		 * @return a string representation of this segment.
		 */
		@Override
		public String toString()
		{
			if(!this.isLoaded())
			{
				return "[ " + this.segmentSize + " unloaded Elements]";
			}
			
			final Iterator<Entry<K, V>> i = this.getData().iterator();
			
			if(!i.hasNext())
			{
				return "[]";
			}
			
			final StringBuilder sb = new StringBuilder();
			sb.append('[');
			for(;;)
			{
				final Entry<K, V> entry = i.next();
				sb.append(entry.toString());
				if(!i.hasNext()) {
					 return sb.append(']').toString();
				}
				sb.append(',').append(' ');
			}
		}

		@Override
		public void allowUnload(final boolean allow)
		{
			this.allowUnloading = allow;
		}
		
		@Override
		public boolean unloadAllowed()
		{
			return this.allowUnloading;
		}
	}

	public static class Entry<K, V> implements Map.Entry<K, V>
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		protected final int hash;
		protected final K key;
		protected V value;

		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		public Entry(final int hash, final K key, final V value)
		{
			super();
			this.hash = hash;
			this.key = key;
			this.value = value;
		}
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		/**
		 * Get the key hash.
		 * 
		 * @return hash of the key.
		 */
		public int getHash()
		{
			return this.hash;
		}
		
		@Override
		public K getKey()
		{
			return this.key;
		}

		@Override
		public V getValue()
		{
			return this.value;
		}

		@Override
		public V setValue(final V value)
		{
			throw new UnsupportedOperationException();
		}

		/**
		 * Returns the string representation of this Entry
		 * as Key-value pair coded as 'key'='value'.
		 * 
		 * @return a string representation of this Entry.
		 */
		@Override
		public String toString()
		{
			return this.key + "=" + this.value;
		}
		
		@Override
		public final int hashCode()
		{
			return Objects.hashCode(this.hash)
				^ Objects.hashCode(this.key)
				^ Objects.hashCode(this.value);
		}
		
		@Override
		public boolean equals(final Object obj)
		{
			if (obj == this)
			{
				return true;
			}
			if (obj instanceof Entry)
			{
				final Entry<?,?> e = (Entry<?,?>)obj;
				return Objects.equals(this.hash, e.getHash()) &&
					Objects.equals(this.key, e.getKey()) &&
					Objects.equals(this.value, e.getValue());
			}
			return false;
		}
	}

	final class EntrySet extends AbstractSet<Map.Entry<K, V>> implements LazySet<Map.Entry<K, V>>
	{
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		@Override
		public Iterator<Map.Entry<K, V>> iterator()
		{
			return new EntryIterator();
		}

		@Override
		public int size()
		{
			return LazyHashMap.this.size;
		}

		@Override
		public void clear()
		{
			LazyHashMap.this.clear();
		}
		
		@Override
		public final Spliterator<Map.Entry<K, V>> spliterator()
		{
			return new EntrySpliterator<>(LazyHashMap.this, 0, -1, 0);
		}
		
		@Override
		public <P extends Consumer<Lazy<?>>> P iterateLazyReferences(final P procedure)
		{
			try
			{
				for(final LazyHashMap<K, V>.Segment<Entry<K, V>> segment : LazyHashMap.this.segments)
				{
					procedure.accept(segment.data);
				}
			}
			catch(final ThrowBreak b)
			{
				// abort iteration
			}
			
			return procedure;
		}

		@Override
		public boolean consolidate()
		{
			return false;
		}

	}

	final class KeySet extends AbstractSet<K> implements LazySet<K>
	{
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		@Override
		public Iterator<K> iterator()
		{
			return new KeyIterator();
		}

		@Override
		public int size()
		{
			return LazyHashMap.this.size;
		}

		@Override
		public void clear()
		{
			LazyHashMap.this.clear();
		}

		@Override
		public final Spliterator<K> spliterator()
		{
			return new KeySpliterator<>(LazyHashMap.this, 0, -1, 0);
		}
		
		@Override
		public <P extends Consumer<Lazy<?>>> P iterateLazyReferences(final P procedure)
		{
			try
			{
				for(final LazyHashMap<K, V>.Segment<Entry<K, V>> segment : LazyHashMap.this.segments)
				{
					procedure.accept(segment.data);
				}
			}
			catch(final ThrowBreak b)
			{
				// abort iteration
			}
			
			return procedure;
		}

		@Override
		public boolean consolidate()
		{
			return false;
		}

	}

	final class Values extends AbstractSet<V> implements LazyCollection<V>
	{
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		@Override
		public Iterator<V> iterator()
		{
			return new ValueIterator();
		}

		@Override
		public int size()
		{
			return LazyHashMap.this.size;
		}

		@Override
		public void clear()
		{
			LazyHashMap.this.clear();
		}
		
		@Override
		public final Spliterator<V> spliterator()
		{
			return new ValueSpliterator<>(LazyHashMap.this, 0, -1, 0);
		}

		@Override
		public <P extends Consumer<Lazy<?>>> P iterateLazyReferences(final P procedure)
		{
			try
			{
				for(final LazyHashMap<K, V>.Segment<Entry<K, V>> segment : LazyHashMap.this.segments)
				{
					procedure.accept(segment.data);
				}
			}
			catch(final ThrowBreak b)
			{
				// abort iteration
			}
			
			return procedure;
		}

		@Override
		public boolean consolidate()
		{
			return false;
		}

	}

	final class EntryIterator extends LazyMapIterator implements Iterator<Map.Entry<K, V>>
	{
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		@Override
		public final Entry<K, V> next()
		{
			return super.nextEntry();
		}
	}

	final class KeyIterator extends LazyMapIterator implements Iterator<K>
	{
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		@Override
		public final K next()
		{
			return super.nextEntry().getKey();
		}
	}

	final class ValueIterator extends LazyMapIterator implements Iterator<V>
	{
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		@Override
		public final V next()
		{
			return super.nextEntry().getValue();
		}
	}
	
	abstract private class LazyMapIterator
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private int segmentIndex = -1;
		private int localIndex   = -1;
		private int currentLocalIndex = -1;
		private int nextIndex    = -1;
		private final int expectedModCount;
		private LazyHashMap<K, V>.Segment<Entry<K, V>> currentSegment;

		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		public LazyMapIterator()
		{
			super();
			this.expectedModCount = LazyHashMap.this.modCount;
			
			if(LazyHashMap.this.size > 0) {
				this.nextIndex = 0;
				this.segmentIndex = 0;
				this.localIndex = 0;
			}
			
		}
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		public boolean hasNext()
		{
			return  this.nextIndex >= 0;
		}
		
		public Entry<K, V> nextEntry()
		{
			if (LazyHashMap.this.modCount != this.expectedModCount)
			{
				throw new ConcurrentModificationException();
			}
			
			if (!this.hasNext())
			{
				throw new NoSuchElementException();
			}
			                                   
            this.currentSegment = LazyHashMap.this.segments.get(this.segmentIndex);
            final Entry<K, V> e = this.currentSegment.getData().get(this.localIndex);
            
            this.currentLocalIndex = this.localIndex;
            
            //check next:
            this.nextIndex++;
            if(this.nextIndex >= LazyHashMap.this.size)
            {
            	this.nextIndex = -1;
            }
            else
            {
	            //advance:
				if (this.localIndex < this.currentSegment.segmentSize - 1)
				{
					// next entry is in current segment
					this.localIndex++;
				}
				else
				{
					// advance segment;
					this.localIndex = 0;
					this.segmentIndex++;
				}
            }
                        
            return e;
		}
		
		public final void remove()
		{
			if(this.currentLocalIndex < 0)
			{
				 throw new IllegalStateException();
			}
			if (LazyHashMap.this.modCount != this.expectedModCount)
			{
				throw new ConcurrentModificationException();
			}
						
			this.currentSegment.remove(this.currentLocalIndex);
			LazyHashMap.this.size--;
									
			if(this.nextIndex > LazyHashMap.this.size)
			{
            	this.nextIndex = -1;
            }
						
			this.currentLocalIndex = -1;
			this.localIndex = Math.max(0, --this.localIndex);
			this.nextIndex--;
					
			if(LazyHashMap.this.removeSegmentIfEmpty(this.currentSegment))
			{
				this.localIndex = 0;
				this.segmentIndex--;
			}
			
		}
	}

	static class SegmentsSpliterator<K, V>
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		protected final LazyHashMap<K, V> map;
		protected int index; // current index, modified on advance/split
		protected int fence; // one past last index
		protected int expectedModCount; // for commodification checks
		protected int segmentIndex;
		protected int localIndex;
		protected LazyHashMap<K, V>.Segment<Entry<K, V>> currentSegment;

		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		public SegmentsSpliterator(
			final LazyHashMap<K, V> map,
			final int index,
			final int fence,
			final int expectedModCount)
		{
			super();
			this.map = map;
			this.index = index;
			this.fence = fence;
			this.expectedModCount = expectedModCount;

			this.segmentIndex = map.calculateIndexPosition(index).segmentIndex;
			this.currentSegment = map.segments.get(this.segmentIndex);
		}

		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		/**
		 * allow late binding
		 */
		protected int getFence()
		{
			int hi;
			if ((hi = this.fence) < 0)
			{
				this.expectedModCount = this.map.modCount;
				hi = this.fence = this.map.size;
			}
			return hi;
		}
		
		public long estimateSize()
		{
			return this.getFence() - this.index;
		}

		public int characteristics()
		{
			return Spliterator.ORDERED | Spliterator.SIZED | Spliterator.SUBSIZED;
		}
	}

	static class KeySpliterator<K, V> extends SegmentsSpliterator<K, V> implements Spliterator<K>
	{
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		public KeySpliterator(
			final LazyHashMap<K, V> map,
			final int index,
			final int fence,
			final int expectedModCount)
		{
			super(map, index, fence, expectedModCount);
		}

		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		@Override
		public boolean tryAdvance(final Consumer<? super K> action)
		{
			if (action == null)
			{
				throw new NullPointerException();
			}

			final int hi = this.getFence();

			if (this.index < hi)
			{
				final K key = this.currentSegment.getData().get(this.localIndex).key;
				action.accept(key);
				
				if (this.map.modCount != this.expectedModCount)
				{
					throw new ConcurrentModificationException();
				}

				// advance
				this.index++;
				if (this.index >= hi)
				{
					return true;
				}

				if (this.localIndex < this.currentSegment.segmentSize - 1)
				{
					this.localIndex++;
				}
				else
				{
					this.localIndex = 0;
					this.segmentIndex++;
					this.currentSegment = this.map.segments.get(this.segmentIndex);
				}
				
				return true;
			}
			return false;
		}

		@Override
		public Spliterator<K> trySplit()
		{
			final int lo = this.index;
			final int hi = this.getFence();
			final int mid = lo + hi >>> 1;

			final IndexPosition sIndex = this.map.calculateIndexPosition(mid);

			if (lo < mid)
			{
				// return null if no more split possible
				// because all elements are in same segment
				if (hi <= sIndex.segmentStartIndex + sIndex.segmentSize && lo >= sIndex.segmentStartIndex)
				{
					return null;
				}

				final int dist_lo = mid - sIndex.segmentStartIndex;
				final int dist_hi = sIndex.segmentStartIndex + sIndex.segmentSize - mid;

				// put midSegment to left or right?
				int splitIndex;
				if (dist_lo < dist_hi)
				{
					splitIndex = sIndex.segmentStartIndex;
				}
				else
				{
					splitIndex = sIndex.segmentStartIndex + sIndex.segmentSize;
				}

				this.index = splitIndex;
				this.segmentIndex = this.map.calculateIndexPosition(this.index).segmentIndex;
				this.currentSegment = this.map.segments.get(this.segmentIndex);

				return new KeySpliterator<>(this.map, lo, splitIndex, this.expectedModCount);
			}
			return null;
		}

	}
	
	static class EntrySpliterator<K,V> extends SegmentsSpliterator<K, V> implements Spliterator<Map.Entry<K, V>>
	{
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		public EntrySpliterator(
			final LazyHashMap<K, V> map,
			final int index,
			final int fence,
			final int expectedModCount)
		{
			super(map, index, fence, expectedModCount);
		}

		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		@Override
		public boolean tryAdvance(final Consumer<? super Map.Entry<K, V>> action)
		{
			if (action == null)
			{
				throw new NullPointerException();
			}

			final int hi = this.getFence();

			if (this.index < hi)
			{
				final Map.Entry<K, V> entry = this.currentSegment.getData().get(this.localIndex);
				action.accept(entry);
				
				if (this.map.modCount != this.expectedModCount)
				{
					throw new ConcurrentModificationException();
				}

				// advance
				this.index++;
				if (this.index >= hi)
				{
					return true;
				}

				if (this.localIndex < this.currentSegment.segmentSize - 1)
				{
					this.localIndex++;
				}
				else
				{
					this.localIndex = 0;
					this.segmentIndex++;
					this.currentSegment = this.map.segments.get(this.segmentIndex);
				}

				return true;
			}
			return false;
		}

		@Override
		public Spliterator<Map.Entry<K, V>> trySplit()
		{
			final int lo = this.index;
			final int hi = this.getFence();
			final int mid = lo + hi >>> 1;

			final IndexPosition sIndex = this.map.calculateIndexPosition(mid);

			if (lo < mid)
			{
				// return null if no more split possible
				// because all elements are in same segment
				if (hi <= sIndex.segmentStartIndex + sIndex.segmentSize && lo >= sIndex.segmentStartIndex)
				{
					return null;
				}

				final int dist_lo = mid - sIndex.segmentStartIndex;
				final int dist_hi = sIndex.segmentStartIndex + sIndex.segmentSize - mid;

				// put midSegment to left or right?
				int splitIndex;
				if (dist_lo < dist_hi)
				{
					splitIndex = sIndex.segmentStartIndex;
				}
				else
				{
					splitIndex = sIndex.segmentStartIndex + sIndex.segmentSize;
				}

				this.index = splitIndex;
				this.segmentIndex = this.map.calculateIndexPosition(this.index).segmentIndex;
				this.currentSegment = this.map.segments.get(this.segmentIndex);

				return new EntrySpliterator<>(this.map, lo, splitIndex, this.expectedModCount);
			}
			return null;
		}

	}
	
	static class ValueSpliterator<K, V> extends SegmentsSpliterator<K, V> implements Spliterator<V>
	{
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		public ValueSpliterator(
			final LazyHashMap<K, V> map,
			final int index,
			final int fence,
			final int expectedModCount)
		{
			super(map, index, fence, expectedModCount);
		}

		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		@Override
		public boolean tryAdvance(final Consumer<? super V> action)
		{
			if (action == null)
			{
				throw new NullPointerException();
			}

			final int hi = this.getFence();

			if (this.index < hi)
			{
				final V value = this.currentSegment.getData().get(this.localIndex).value;
				action.accept(value);
				
				if (this.map.modCount != this.expectedModCount)
				{
					throw new ConcurrentModificationException();
				}

				// advance
				this.index++;
				if (this.index >= hi)
				{
					return true;
				}

				if (this.localIndex < this.currentSegment.segmentSize - 1)
				{
					this.localIndex++;
				}
				else
				{
					this.localIndex = 0;
					this.segmentIndex++;
					this.currentSegment = this.map.segments.get(this.segmentIndex);
				}

				return true;
			}
			return false;
		}

		@Override
		public Spliterator<V> trySplit()
		{
			final int lo = this.index;
			final int hi = this.getFence();
			final int mid = lo + hi >>> 1;

			final IndexPosition sIndex = this.map.calculateIndexPosition(mid);

			if (lo < mid)
			{
				// return null if no more split possible
				// because all elements are in same segment
				if (hi <= sIndex.segmentStartIndex + sIndex.segmentSize && lo >= sIndex.segmentStartIndex)
				{
					return null;
				}

				final int dist_lo = mid - sIndex.segmentStartIndex;
				final int dist_hi = sIndex.segmentStartIndex + sIndex.segmentSize - mid;

				// put midSegment to left or right?
				int splitIndex;
				if (dist_lo < dist_hi)
				{
					splitIndex = sIndex.segmentStartIndex;
				}
				else
				{
					splitIndex = sIndex.segmentStartIndex + sIndex.segmentSize;
				}

				this.index = splitIndex;
				this.segmentIndex = this.map.calculateIndexPosition(this.index).segmentIndex;
				this.currentSegment = this.map.segments.get(this.segmentIndex);

				return new ValueSpliterator<>(this.map, lo, splitIndex, this.expectedModCount);
			}
			return null;
		}

	}

}

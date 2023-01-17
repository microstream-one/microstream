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
import java.util.Iterator;
import java.util.Spliterator;

import one.microstream.collections.lazy.LazyHashMap.Entry;

/**
 * Implementation of the {@code Set} interface, backed by a {@link LazyHashMap}
 *
 * @param <T> Type of contained elements
 */
public class LazyHashSet<T>
	extends AbstractSet<T>
{
	
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////
	
	private final LazyHashMap<T, Object> map;
	
	// Dummy value to associate with an Object in the backing Map
	private static final Object PRESENT = new Object();
	
	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////
	
	/**
	 * Create a new LazyHashSet instance
	 * with default maximal segment size.
	 */
	public LazyHashSet()
	{
		super();
		this.map = new LazyHashMap<>();
	}

	/**
	 * Creates a new {@link LazyHashSet} with a custom segment size.
	 * The desired maximum segment size is not a hard limit. The map may exceed that
	 * limit.
	 * 
	 * @param maxSegmentSize maximum desired segment size, must be non negative.
	 */
	public LazyHashSet(final int maxSegmentSize)
	{
		super();
		
		if(maxSegmentSize < 0)
		{
			throw new IllegalArgumentException("Illegal maxSegmentSize: " + maxSegmentSize + ". Must be 0 or greater!");
		}
		
		this.map = new LazyHashMap<>(maxSegmentSize);
	}
	
	/**
	 * Creates a new {@link LazyHashSet} with a maximum desired segment size.
	 * The desired maximum segment size is not a hard limit. The map may exceed that
	 * limit.
	 * 
	 * @param maxSegmentSize maximum desired segment size, must be non negative.
	 * @param lazySegmentUnloader LazySegmentUnloader instance
	 */
	public LazyHashSet(final int maxSegmentSize, final LazySegmentUnloader lazySegmentUnloader)
	{
		super();
		
		if(maxSegmentSize < 0)
		{
			throw new IllegalArgumentException("Illegal maxSegmentSize: " + maxSegmentSize + ". Must be 0 or greater!");
		}

		this.map = new LazyHashMap<>(maxSegmentSize, lazySegmentUnloader);
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
		return this.map.getSegmentCount();
	}
	
	/**
	 * Returns an Iterable over the Segment in this list.
	 * 
	 * @return an Iterable over the Segment in this list
	 */
	public Iterable<? extends LazyHashMap<T, Object>.Segment<?>> segments()
	{
		return this.map.segments();
	}
	
	/**
	 * Returns the maximum segment size of this {@link LazyArrayList}.
	 * 
	 * @return the maximum segment size of this {@link LazyArrayList}
	 */
	public int getMaxSegmentSize()
	{
		return this.map.getMaxSegmentSize();
	}
	
	@Override
	public Iterator<T> iterator()
	{
		return this.map.keySet().iterator();
	}

	@Override
	public int size()
	{
		return this.map.size();
	}
	
	@Override
	public boolean isEmpty()
	{
		return this.map.isEmpty();
	}
	
	@Override
	public boolean contains(final Object o)
	{
		return this.map.containsKey(o);
	}
		
	@Override
	public boolean add(final T e)
	{
		return this.map.put(e, PRESENT) == null;
	}
	
	@Override
	public boolean remove(final Object o)
	{
		return this.map.remove(o) == PRESENT;
	}
	
	@Override
	public void clear()
	{
		this.map.clear();
	}
	
	@Override
	public Spliterator<T> spliterator()
	{
		return this.map.keySet().spliterator();
	}
	
	@Override
	public String toString()
	{
		final Iterator<? extends LazyHashMap<T, Object>.Segment<?>> iterator = this.map.segments().iterator();
		
		if(!iterator.hasNext())
		{
			return "{}";
		}
		
		final StringBuilder sb = new StringBuilder();
		sb.append('{');
		
		while(iterator.hasNext())
		{
			final LazyHashMap<T, Object>.Segment<?> segment = iterator.next();
			sb.append("[");
			if(segment.isLoaded())
			{
				final Iterator<Entry<T, Object>> entryItertor = segment.getData().iterator();
				
				while (entryItertor.hasNext())
				{
					final Entry<T, Object> entry = entryItertor.next();
					sb.append(entry.key);
					if (entryItertor.hasNext())
					{
						sb.append(',').append(' ');
					}
				}
			}
			else
			{
				sb.append(" " + segment.size() + " unloaded Elements ");
			}
			sb.append("]");
		}
		
		return sb.append('}').toString();
		
	}
}

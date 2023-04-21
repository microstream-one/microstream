package one.microstream.collections.lazy;

/*-
 * #%L
 * MicroStream Base
 * %%
 * Copyright (C) 2019 - 2023 MicroStream Software
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

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map.Entry;

import org.slf4j.Logger;

import one.microstream.util.logging.Logging;

/**
 * Defines methods to control automated unloading
 * of LazySegments.
 * 
 */
public interface LazySegmentUnloader
{
	/**
	 * Tries to unload segments.
	 * Most implementation should not unload the currently used segment.
	 * 
	 * @param currentLazySegment LazySegment that is currently in use.
	 */
	public void unload(LazySegment<?> currentLazySegment);
	
	/**
	 * Unregister the provided segment from the Unloader.
	 * 
	 * @param segment Segment to be unregistered.
	 */
	public void remove(LazySegment<?> segment);
	
	/**
	 * Create a new copy of this LazySegmentUnloader.
	 * 
	 * @return a new copy of this LazySegmentUnloader
	 */
	public LazySegmentUnloader copy();
	
	/**
	 * Default implementation of LazyUnloader
	 * <br>
	 * This implementation will try to keep a configurable number of last loaded
	 * segments in memory.
	 * The number of loaded elements may vary because not stored or modified segments
	 * can't be unloaded.
	 * 
	 */
	public final class Default implements LazySegmentUnloader
	{
		private final static Logger logger = Logging.getLogger(Default.class);

		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private final int desiredLoadCount;
		
		//don't persist state! Must be recreated after a persisted instance of this class has been reloaded
		transient private LinkedList<LazySegment<?>> loadedSegments;
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		/**
		 * Creates a LazyUnloader.Default instance.
		 * This instance tries to keep two segments at least in memory.
		 */
		public Default()
		{
			super();
			this.desiredLoadCount = 2;
			this.loadedSegments = new LinkedList<>();
		}
		
		/**
		 * Creates a LazyUnloader.Default instance.
		 * 
		 * @param desiredLoadCount number of segments that should be kept in memory, must be greater than 0.
		 */
		public Default(final int desiredLoadCount)
		{
			super();
			
			if(desiredLoadCount < 1)
			{
				throw new IllegalArgumentException("the desired load count must be create then zero: " + desiredLoadCount);
			}
			
			this.desiredLoadCount = desiredLoadCount;
			this.loadedSegments = new LinkedList<>();
		}
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		@Override
		public synchronized void unload(final LazySegment<?> lazySegment)
		{
			if(this.loadedSegments == null)
			{
				this.loadedSegments = new LinkedList<>();
			}
			
			if(this.setLastUsed(lazySegment))
			{
				final Iterator<LazySegment<?>> iterator = this.loadedSegments.iterator();
				while(iterator.hasNext() && this.loadedSegments.size() > this.desiredLoadCount)
				{
					final LazySegment<?> segment = iterator.next();
					if(segment != lazySegment
							&& !segment.isModified()
							&&  segment.isLoaded()
							&&  segment.unloadAllowed()
							)
					{
						logger.debug("unloading segment {}", segment.hashCode());
						segment.unloadSegment();
						iterator.remove();
					}
				}
			}
		}
		
		/**
		 * Add provided segment on top of "loaded" list.
		 * 
		 * @param lazySegment current segment
		 * @return true if a new segment was added
		 */
		private boolean setLastUsed(final LazySegment<?> lazySegment)
		{
			if(lazySegment != this.loadedSegments.peekLast())
			{
				this.loadedSegments.remove(lazySegment);
				this.loadedSegments.add(lazySegment);
				return true;
			}
			return false;
		}
		
		@Override
		public LazySegmentUnloader copy()
		{
			return new Default(this.desiredLoadCount);
		}

		@Override
		public void remove(final LazySegment<?> segment)
		{
			this.loadedSegments.remove(segment);
		}
		
	}
	
	public final class Timed implements LazySegmentUnloader
	{
		private final static Logger logger = Logging.getLogger(Timed.class);
		
		//don't persist state! Must be recreated after a persisted instance of this class has been reloaded
		transient private HashMap<LazySegment<?>, Long> loadedSegments;
		long lifetime;
					
		public Timed(final long lifetime)
		{
			super();
			this.lifetime = lifetime;
			this.loadedSegments = new HashMap<>();
		}

		@Override
		public void unload(final LazySegment<?> currentLazySegment)
		{
			if(this.loadedSegments == null)
			{
				this.loadedSegments = new HashMap<>();
			}
			
			final long currentTime = System.currentTimeMillis();
			this.loadedSegments.put(currentLazySegment, currentTime);
			
			
			final Iterator<Entry<LazySegment<?>, Long>> iterator = this.loadedSegments.entrySet().iterator();
			while(iterator.hasNext())
			{
				final Entry<LazySegment<?>, Long> item = iterator.next();
				final LazySegment<?> segment = item.getKey();
				if(item.getValue() + this.lifetime < currentTime )
				{
					if(segment != currentLazySegment
							&& !segment.isModified()
							&&  segment.isLoaded()
							&&  segment.unloadAllowed()
							)
					{
						logger.debug("unloading segment {}", segment.hashCode());
						segment.unloadSegment();
					}
				}
			}
		}

		@Override
		public LazySegmentUnloader copy()
		{
			return new Timed(this.lifetime);
		}

		@Override
		public void remove(final LazySegment<?> segment)
		{
			this.loadedSegments.remove(segment);
		}
				
	}
		
	/**
	 *  LazyUnloader implementation that does no unloading.
	 *
	 */
	public final class Never implements LazySegmentUnloader
	{
		@Override
		public void unload(final LazySegment<?> lazySegment)
		{
			//no op
		}

		@Override
		public LazySegmentUnloader copy()
		{
			return new Never();
		}

		@Override
		public void remove(final LazySegment<?> segment)
		{
			//no op
		}

	}

}

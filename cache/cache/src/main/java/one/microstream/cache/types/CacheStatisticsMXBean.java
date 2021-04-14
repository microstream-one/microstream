
package one.microstream.cache.types;

import java.util.concurrent.atomic.AtomicLong;
import java.util.function.LongSupplier;


public interface CacheStatisticsMXBean extends javax.cache.management.CacheStatisticsMXBean
{
	public void increaseCacheRemovals(final long number);
	
	public void increaseCacheExpiries(final long number);
	
	public void increaseCachePuts(final long number);
	
	public void increaseCacheHits(final long number);
	
	public void increaseCacheMisses(final long number);
	
	public void increaseCacheEvictions(final long number);
	
	public void addGetTimeNano(final long duration);
	
	public void addPutTimeNano(final long duration);
	
	public void addRemoveTimeNano(final long duration);
	
	
	public static class Default implements CacheStatisticsMXBean
	{
		private final static long     NANOSECONDS_IN_A_MICROSECOND = 1000L;
		
		private final transient LongSupplier sizeSupplier;
		
		private final AtomicLong      cacheRemovals                = new AtomicLong();
		private final AtomicLong      cacheExpiries                = new AtomicLong();
		private final AtomicLong      cachePuts                    = new AtomicLong();
		private final AtomicLong      cacheHits                    = new AtomicLong();
		private final AtomicLong      cacheMisses                  = new AtomicLong();
		private final AtomicLong      cacheEvictions               = new AtomicLong();
		private final AtomicLong      cachePutTimeTakenNanos       = new AtomicLong();
		private final AtomicLong      cacheGetTimeTakenNanos       = new AtomicLong();
		private final AtomicLong      cacheRemoveTimeTakenNanos    = new AtomicLong();
		
		Default(final LongSupplier sizeSupplier)
		{
			this.sizeSupplier = sizeSupplier;
		}
		
		@Override
		public void clear()
		{
			this.cachePuts.set(0);
			this.cacheMisses.set(0);
			this.cacheRemovals.set(0);
			this.cacheExpiries.set(0);
			this.cacheHits.set(0);
			this.cacheEvictions.set(0);
			this.cacheGetTimeTakenNanos.set(0);
			this.cachePutTimeTakenNanos.set(0);
			this.cacheRemoveTimeTakenNanos.set(0);
		}
		
		public long getEntryCount()
		{
			return this.sizeSupplier.getAsLong();
		}
		
		@Override
		public long getCacheHits()
		{
			return this.cacheHits.longValue();
		}
		
		@Override
		public float getCacheHitPercentage()
		{
			final long hits = this.getCacheHits();
			return hits == 0
				? 0
				: (float)hits / this.getCacheGets() * 100.0f;
		}
		
		@Override
		public long getCacheMisses()
		{
			return this.cacheMisses.longValue();
		}
		
		@Override
		public float getCacheMissPercentage()
		{
			final long misses = this.getCacheMisses();
			return misses == 0
				? 0
				: (float)misses / this.getCacheGets() * 100.0f;
		}
		
		@Override
		public long getCacheGets()
		{
			return this.getCacheHits() + this.getCacheMisses();
		}
		
		@Override
		public long getCachePuts()
		{
			return this.cachePuts.longValue();
		}
		
		@Override
		public long getCacheRemovals()
		{
			return this.cacheRemovals.longValue();
		}
		
		@Override
		public long getCacheEvictions()
		{
			return this.cacheEvictions.longValue();
		}
		
		@Override
		public float getAverageGetTime()
		{
			final long cacheGetTimeTakenNanos = this.cacheGetTimeTakenNanos.longValue();
			final long cacheGets              = this.getCacheGets();
			return cacheGetTimeTakenNanos == 0 || cacheGets == 0
				? 0
				: (cacheGetTimeTakenNanos / cacheGets) / NANOSECONDS_IN_A_MICROSECOND;
		}
		
		@Override
		public float getAveragePutTime()
		{
			final long cachePutTimeTakenNanos = this.cachePutTimeTakenNanos.longValue();
			final long cacheGets              = this.getCacheGets();
			return cachePutTimeTakenNanos == 0 || cacheGets == 0
				? 0
				: (cachePutTimeTakenNanos / cacheGets) / NANOSECONDS_IN_A_MICROSECOND;
		}
		
		@Override
		public float getAverageRemoveTime()
		{
			final long cacheRemoveTimeTakenNanos = this.cacheRemoveTimeTakenNanos.longValue();
			final long cacheGets                 = this.getCacheGets();
			return cacheRemoveTimeTakenNanos == 0 || cacheGets == 0
				? 0
				: (cacheRemoveTimeTakenNanos / cacheGets) / NANOSECONDS_IN_A_MICROSECOND;
		}
		
		@Override
		public void increaseCacheRemovals(final long number)
		{
			this.cacheRemovals.getAndAdd(number);
		}
		
		@Override
		public void increaseCacheExpiries(final long number)
		{
			this.cacheExpiries.getAndAdd(number);
		}
		
		@Override
		public void increaseCachePuts(final long number)
		{
			this.cachePuts.getAndAdd(number);
		}
		
		@Override
		public void increaseCacheHits(final long number)
		{
			this.cacheHits.getAndAdd(number);
		}
		
		@Override
		public void increaseCacheMisses(final long number)
		{
			this.cacheMisses.getAndAdd(number);
		}
		
		@Override
		public void increaseCacheEvictions(final long number)
		{
			this.cacheEvictions.getAndAdd(number);
		}
		
		@Override
		public void addGetTimeNano(final long duration)
		{
			if(this.cacheGetTimeTakenNanos.get() <= Long.MAX_VALUE - duration)
			{
				this.cacheGetTimeTakenNanos.addAndGet(duration);
			}
			else
			{
				// Counter full. Just reset.
				this.clear();
				this.cacheGetTimeTakenNanos.set(duration);
			}
		}
		
		@Override
		public void addPutTimeNano(final long duration)
		{
			if(this.cachePutTimeTakenNanos.get() <= Long.MAX_VALUE - duration)
			{
				this.cachePutTimeTakenNanos.addAndGet(duration);
			}
			else
			{
				this.clear();
				this.cachePutTimeTakenNanos.set(duration);
			}
		}
		
		@Override
		public void addRemoveTimeNano(final long duration)
		{
			if(this.cacheRemoveTimeTakenNanos.get() <= Long.MAX_VALUE - duration)
			{
				this.cacheRemoveTimeTakenNanos.addAndGet(duration);
			}
			else
			{
				this.clear();
				this.cacheRemoveTimeTakenNanos.set(duration);
			}
		}
		
	}
	
}

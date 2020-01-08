
package one.microstream.cache;

import static one.microstream.X.notNull;

import java.util.Comparator;
import java.util.Random;
import java.util.function.Predicate;

import one.microstream.X;
import one.microstream.typing.KeyValue;

@FunctionalInterface
public interface EvictionPolicy
{
	public Iterable<KeyValue<Object, CachedValue>> pickEntriesToEvict(CacheTable cacheTable);
	
	
	public static Predicate<CacheTable> MaxCacheSizePredicate(final long maxCacheSize)
	{
		return cache -> cache.size() >= maxCacheSize;
	}
	
	public static Comparator<KeyValue<Object, CachedValue>> LeastRecentlyUsedComparator()
	{
		return (kv1, kv2) -> Long.compare(kv1.value().accessTime(), kv2.value().accessTime());
	}
	
	public static Comparator<KeyValue<Object, CachedValue>> LeastFrequentlyUsedComparator()
	{
		return (kv1, kv2) -> Long.compare(kv1.value().accessCount(), kv2.value().accessCount());
	}
	
	public static EvictionPolicy LeastRecentlyUsed(final long maxCacheSize)
	{
		return LeastRecentlyUsed(MaxCacheSizePredicate(maxCacheSize), null);
	}
	
	public static EvictionPolicy LeastRecentlyUsed(
		final Predicate<CacheTable>                     evictionNecessity,
		final Predicate<KeyValue<Object, CachedValue>>  evictionPermission
	)
	{
		return Sampling(evictionNecessity, evictionPermission, LeastRecentlyUsedComparator());
	}
	
	public static EvictionPolicy LeastFrequentlyUsed(final long maxCacheSize)
	{
		return LeastFrequentlyUsed(MaxCacheSizePredicate(maxCacheSize), null);
	}
	
	public static EvictionPolicy LeastFrequentlyUsed(
		final Predicate<CacheTable>                     evictionNecessity,
		final Predicate<KeyValue<Object, CachedValue>>  evictionPermission
	)
	{
		return Sampling(evictionNecessity, evictionPermission, LeastFrequentlyUsedComparator());
	}
	
	public static EvictionPolicy FirstInFirstOut(final long maxCacheSize)
	{
		return FirstInFirstOut(MaxCacheSizePredicate(maxCacheSize), null);
	}
	
	public static EvictionPolicy FirstInFirstOut(
		final Predicate<CacheTable>                     evictionNecessity,
		final Predicate<KeyValue<Object, CachedValue>>  evictionPermission
	)
	{
		return Searching(evictionNecessity, evictionPermission);
	}
	
	public static EvictionPolicy Sampling(
		final Predicate<CacheTable>                     evictionNecessity,
		final Predicate<KeyValue<Object, CachedValue>>  evictionPermission,
		final Comparator<KeyValue<Object, CachedValue>> comparator
	)
	{
		return new Sampling(evictionNecessity, evictionPermission, comparator);
	}
	
	public static EvictionPolicy Searching(
		final Predicate<CacheTable>                     evictionNecessity,
		final Predicate<KeyValue<Object, CachedValue>>  evictionPermission
	)
	{
		return new Searching(evictionNecessity, evictionPermission);
	}
		
	
	public static class Sampling implements EvictionPolicy
	{
		final static int    MAX_SAMPLE_COUNT   =     10    ;
		final static int    SAMPLE_THRESHOLD   = 10_000    ;
		final static int    MIN_SAMPLE_SIZE    =     15    ;
		final static int    MAX_SAMPLE_SIZE    =    100    ;
		final static double SAMPLE_SIZE_FACTOR =      0.002;
		
		private final Predicate<CacheTable>                     evictionNecessity;
		private final Predicate<KeyValue<Object, CachedValue>>  evictionPermission;
		private final Comparator<KeyValue<Object, CachedValue>> comparator;
		private final Random                                    random;
				
		Sampling(
			final Predicate<CacheTable>                     evictionNecessity,
			final Predicate<KeyValue<Object, CachedValue>>  evictionPermission,
			final Comparator<KeyValue<Object, CachedValue>> comparator
		)
		{
			super();
			
			this.evictionNecessity  = evictionNecessity;
			this.evictionPermission = evictionPermission != null
				? evictionPermission
				: kv -> true;
			this.comparator         = notNull(comparator);
			this.random             = new Random();
		}
		
		@Override
		public Iterable<KeyValue<Object, CachedValue>> pickEntriesToEvict(final CacheTable cacheTable)
		{
			if(this.evictionNecessity != null && !this.evictionNecessity.test(cacheTable))
			{
				return null;
			}
			
			for(int i = 0; i < MAX_SAMPLE_COUNT; i++)
			{
				final KeyValue<Object, CachedValue> entryToEvict = this.sample(cacheTable);
				if(entryToEvict != null && this.evictionPermission.test(entryToEvict))
				{
					return X.Constant(entryToEvict);
				}
			}
			
			return null;
		}
		
		/*
		 * Eviction by sampling.
		 * Pick sample range of bigger caches and sort instead of whole cache.
		 * Extensive tests show deviation <~1% and massive performace gain.
		 */
		private KeyValue<Object, CachedValue> sample(final CacheTable cacheTable)
		{
			final int cacheSize = X.checkArrayRange(cacheTable.size());
			if(cacheSize < SAMPLE_THRESHOLD)
			{
				return cacheTable.min(this.comparator);
			}

			final int optSampleSize = (int)(cacheSize * SAMPLE_SIZE_FACTOR);
			final int sampleSize    = optSampleSize < MIN_SAMPLE_SIZE
				? MIN_SAMPLE_SIZE
				: optSampleSize > MAX_SAMPLE_SIZE
					? MAX_SAMPLE_SIZE
					: optSampleSize;
			final int offset = this.random.nextInt(cacheSize - sampleSize - 1);
			return cacheTable.rangeMin(offset, sampleSize, this.comparator);
		}
		
	}
	
	
	public static class Searching implements EvictionPolicy
	{
		private final Predicate<CacheTable>                     evictionNecessity;
		private final Predicate<KeyValue<Object, CachedValue>>  evictionPermission;
				
		Searching(
			final Predicate<CacheTable>                     evictionNecessity,
			final Predicate<KeyValue<Object, CachedValue>>  evictionPermission
		)
		{
			super();
			
			this.evictionNecessity  = evictionNecessity;
			this.evictionPermission = evictionPermission != null
				? evictionPermission
				: kv -> true;
		}
		
		@Override
		public Iterable<KeyValue<Object, CachedValue>> pickEntriesToEvict(final CacheTable cacheTable)
		{
			return this.evictionNecessity == null || this.evictionNecessity.test(cacheTable)
				? X.Constant(cacheTable.search(this.evictionPermission))
				: null;
		}
		
	}
	
}

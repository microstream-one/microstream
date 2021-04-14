
package one.microstream.cache.types;

import static one.microstream.X.notNull;

import java.util.Comparator;
import java.util.Random;
import java.util.function.Predicate;

import one.microstream.X;
import one.microstream.collections.EqHashEnum;
import one.microstream.collections.types.XEnum;
import one.microstream.reference._intReference;
import one.microstream.typing.KeyValue;

/**
 * Function to pick the entries which should be evicted.
 *
 */
@FunctionalInterface
public interface EvictionPolicy
{
	/**
	 * Select the entries which should be evicted.
	 */
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
	
	public static Comparator<KeyValue<Object, CachedValue>> BiggestObjectsComparator()
	{
		return (kv1, kv2) -> Long.compare(kv2.value().byteSizeEstimate(), kv1.value().byteSizeEstimate());
	}
	
	public static int DefaultElementCount()
	{
		return 4;
	}
	
	public static EvictionPolicy LeastRecentlyUsed(final long maxCacheSize)
	{
		return LeastRecentlyUsed(
			DefaultElementCount(),
			maxCacheSize
		);
	}
	
	public static EvictionPolicy LeastRecentlyUsed(
		final int  elementCount,
		final long maxCacheSize
	)
	{
		return LeastRecentlyUsed(
			() -> elementCount,
			MaxCacheSizePredicate(maxCacheSize),
			null
		);
	}
	
	public static EvictionPolicy LeastRecentlyUsed(
		final _intReference                             elementCount,
		final Predicate<CacheTable>                     evictionNecessity,
		final Predicate<KeyValue<Object, CachedValue>>  evictionPermission
	)
	{
		return Sampling(
			elementCount,
			evictionNecessity,
			evictionPermission,
			LeastRecentlyUsedComparator()
		);
	}
	
	public static EvictionPolicy LeastFrequentlyUsed(final long maxCacheSize)
	{
		return LeastFrequentlyUsed(
			DefaultElementCount(),
			maxCacheSize
		);
	}
	
	public static EvictionPolicy LeastFrequentlyUsed(
		final int  elementCount,
		final long maxCacheSize
	)
	{
		return LeastFrequentlyUsed(
			() -> elementCount,
			MaxCacheSizePredicate(maxCacheSize),
			null
		);
	}
	
	public static EvictionPolicy LeastFrequentlyUsed(
		final _intReference                             elementCount,
		final Predicate<CacheTable>                     evictionNecessity,
		final Predicate<KeyValue<Object, CachedValue>>  evictionPermission
	)
	{
		return Sampling(
			elementCount,
			evictionNecessity,
			evictionPermission,
			LeastFrequentlyUsedComparator()
		);
	}
	
	public static EvictionPolicy BiggestObjects(
		final int  elementCount,
		final long maxCacheSize
	)
	{
		return BiggestObjects(
			() -> elementCount,
			MaxCacheSizePredicate(maxCacheSize),
			null
		);
	}
	
	public static EvictionPolicy BiggestObjects(
		final _intReference                             elementCount,
		final Predicate<CacheTable>                     evictionNecessity,
		final Predicate<KeyValue<Object, CachedValue>>  evictionPermission
	)
	{
		return Sampling(
			elementCount,
			evictionNecessity,
			evictionPermission,
			BiggestObjectsComparator()
		);
	}
	
	public static EvictionPolicy FirstInFirstOut(
		final int  elementCount,
		final long maxCacheSize
	)
	{
		return FirstInFirstOut(
			() -> elementCount,
			MaxCacheSizePredicate(maxCacheSize),
			null
		);
	}
	
	public static EvictionPolicy FirstInFirstOut(
		final _intReference                             elementCount,
		final Predicate<CacheTable>                     evictionNecessity,
		final Predicate<KeyValue<Object, CachedValue>>  evictionPermission
	)
	{
		return Searching(
			elementCount,
			evictionNecessity,
			evictionPermission
		);
	}
	
	public static EvictionPolicy Sampling(
		final _intReference                             elementCount,
		final Predicate<CacheTable>                     evictionNecessity,
		final Predicate<KeyValue<Object, CachedValue>>  evictionPermission,
		final Comparator<KeyValue<Object, CachedValue>> comparator
	)
	{
		return new Sampling(
			elementCount,
			evictionNecessity,
			evictionPermission,
			comparator
		);
	}
	
	public static EvictionPolicy Searching(
		final _intReference                             elementCount,
		final Predicate<CacheTable>                     evictionNecessity,
		final Predicate<KeyValue<Object, CachedValue>>  evictionPermission
	)
	{
		return new Searching(
			elementCount,
			evictionNecessity,
			evictionPermission
		);
	}
		
	

	/*
	 * Eviction by sampling.
	 * Pick sample range of bigger caches and sort instead of whole cache.
	 * Extensive tests show deviation <~1% and massive performace gain.
	 */
	public static class Sampling implements EvictionPolicy
	{
		final static int    MAX_SAMPLE_COUNT   =     10    ;
		final static int    SAMPLE_THRESHOLD   = 10_000    ;
		final static int    MIN_SAMPLE_SIZE    =     15    ;
		final static int    MAX_SAMPLE_SIZE    =    100    ;
		final static double SAMPLE_SIZE_FACTOR =      0.002;
		
		private final _intReference                             elementCount;
		private final Predicate<CacheTable>                     evictionNecessity;
		private final Predicate<KeyValue<Object, CachedValue>>  evictionPermission;
		private final Comparator<KeyValue<Object, CachedValue>> comparator;
		private final Random                                    random;
				
		Sampling(
			final _intReference                             elementCount,
			final Predicate<CacheTable>                     evictionNecessity,
			final Predicate<KeyValue<Object, CachedValue>>  evictionPermission,
			final Comparator<KeyValue<Object, CachedValue>> comparator
		)
		{
			super();
			
			this.elementCount       = notNull(elementCount);
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
			
			final int elementCount = this.elementCount.get();
			if(elementCount <= 0)
			{
				throw new RuntimeException("Illegal element count for eviction: " + elementCount + " <= 0");
			}
			

			if(elementCount == 1)
			{
				for(int sample = 0; sample < MAX_SAMPLE_COUNT; sample++)
				{
					final KeyValue<Object, CachedValue> entryToEvict = this.sample(cacheTable);
					if(entryToEvict != null && this.evictionPermission.test(entryToEvict))
					{
						return X.Constant(entryToEvict);
					}
				}
			}
			else
			{
				for(int sample = 0; sample < MAX_SAMPLE_COUNT; sample++)
				{
					final XEnum<KeyValue<Object, CachedValue>> entriesToEvict = EqHashEnum.NewCustom(elementCount);
					for(int i = 0; i < elementCount; i++)
					{
						final KeyValue<Object, CachedValue> entryToEvict = this.sample(cacheTable);
						if(entryToEvict != null && this.evictionPermission.test(entryToEvict))
						{
							entriesToEvict.add(entryToEvict);
						}
					}
					if(!entriesToEvict.isEmpty())
					{
						return entriesToEvict;
					}
				}
			}
			
			return null;
		}
		
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
			final int offset        = this.random.nextInt(cacheSize - sampleSize - 1);
			return cacheTable.rangeMin(
				offset,
				sampleSize,
				this.comparator
			);
		}
		
	}
	
	
	public static class Searching implements EvictionPolicy
	{
		private final _intReference                             elementCount;
		private final Predicate<CacheTable>                     evictionNecessity;
		private final Predicate<KeyValue<Object, CachedValue>>  evictionPermission;
				
		Searching(
			final _intReference                             elementCount,
			final Predicate<CacheTable>                     evictionNecessity,
			final Predicate<KeyValue<Object, CachedValue>>  evictionPermission
		)
		{
			super();
			
			this.elementCount       = notNull(elementCount);
			this.evictionNecessity  = evictionNecessity;
			this.evictionPermission = evictionPermission != null
				? evictionPermission
				: kv -> true;
		}
		
		@Override
		public Iterable<KeyValue<Object, CachedValue>> pickEntriesToEvict(final CacheTable cacheTable)
		{
			if(this.evictionNecessity != null && !this.evictionNecessity.test(cacheTable))
			{
				return null;
			}
			
			final int elementCount = this.elementCount.get();
			if(elementCount <= 0)
			{
				throw new RuntimeException("Illegal element count for eviction: " + elementCount + " <= 0");
			}
			
			final XEnum<KeyValue<Object, CachedValue>> entriesToEvict = EqHashEnum.NewCustom(elementCount);
			cacheTable.iterate(kv -> {
				if(this.evictionPermission.test(kv))
				{
					entriesToEvict.add(kv);
					if(entriesToEvict.size() >= elementCount)
					{
						throw X.BREAK();
					}
				}
			});
			
			return entriesToEvict;
		}
		
	}
	
}

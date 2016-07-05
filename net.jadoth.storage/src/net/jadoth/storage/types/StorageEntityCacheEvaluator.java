package net.jadoth.storage.types;

import static net.jadoth.math.JadothMath.positive;
import net.jadoth.util.chars.VarString;

/**
 * Function type that evaluates if a live entity (entity with cached data) shall be unloaded (its cache cleared).
 * <p>
 * Note that any implementation of this type must be safe enough to never throw an exception as this would doom
 * the storage thread that executes it. Catching any exception would not prevent the problem for the channel thread
 * as the function has to work in order for the channel to work properly.
 * It is therefore strongly suggested that implementations only use "exception free" logic (like simple arithmetic)
 * or handle any possible exception internally.
 *
 * @author TM
 */
@FunctionalInterface
public interface StorageEntityCacheEvaluator
{
	public boolean clearEntityCache(long totalCacheSize, long evaluationTime, StorageEntity entity);



	public final class Implementation implements StorageEntityCacheEvaluator
	{
		///////////////////////////////////////////////////////////////////////////
		// constants        //
		/////////////////////
		
		// To satisfy CheckStyle. See algorithm comment below. Shifting by 16 means roughly age in minutes and is fast.
		private static final int C16 = 16;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		/**
		 * Abstract threshold value, roughly comparable to size in bytes with a time component, at which a cache
		 * must be cleared of some entities.
		 */
		private final long threshold;

		/**
		 * Entity age timeout value in milliseconds at which the entity should definitely be cleared from the cache,
		 * independant of size, type or cache fullness.
		 * Example: After one day of not being used, an entity can definitely be unloaded from the cache.
		 *
		 * Can be set to very low value like 1 hour (3_600_000 ms) for systems with very sporadic activity.
		 * (e.g. an application that performs big operations only every once in a while and is very dormant in between)
		 * Can be set to a medium value like 1 day (86_400_000 ms) for typical server systems
		 * (e.g. constantly working systems with medium load)
		 * Can be set to a huge value to like 1 year or max long to disable the timeout and solely rely on the threshold
		 */
		private final long msTimeout  ;



		///////////////////////////////////////////////////////////////////////////
		// constructors     //
		/////////////////////

		public Implementation(final long threshold, final long millisecondTimeout)
		{
			super();
			this.threshold = positive(threshold);
			this.msTimeout = positive(millisecondTimeout)  ;
		}



		///////////////////////////////////////////////////////////////////////////
		// override methods //
		/////////////////////

		@Override
		public final boolean clearEntityCache(
			final long          cacheSize,
			final long          evalTime ,
			final StorageEntity e
		)
		{
//			DEBUGStorage.println("evaluating " + e);
			/* simple default algorithm to take cache size, entity cached data length, age and reference into account:
			 *
			 * - subtract current cache size from threashold, resulting in an abstract value how "free" the cache is.
			 *   This also means that if the current cache size alone reaches the threshold, the entity will definitely
			 *   be cleared from the cache, no matter what (panic mode to avoid out of memory situations).
			 *
			 * - calculate "weight" of the entity and compare it to the memory's "freeness" to evaluate.
			 *
			 * "weight" calculation:
			 * - the entity's memory consumption (cached data length)
			 * - mutliplied by its "cache age" in ms, divided by 2^16 (roughly equals age in minutes)
			 *   the division is crucial to give newly loaded entities a kind of "grace time" in order to
			 *   not constantly unload and load entities in filled systems (avoid live lock)
			 * - multiply weight by 2 if entity has no references.
			 *   Non-reference entities tend to be huge (text, binaries) and won't be needed by GC, so they have
			 *   less priority to stay in cache or higher priority to be cleared before entities with references.
			 *
			 * In conclusion, this algorithm means:
			 * The more the cache approaches the threshold, the more likely it gets that entities will be
			 * cleared from the cache, especially large, old, non-reference entities.
			 * And the older (not recently used) entities become, the more likely it gets that they will be cleared,
			 * to a point where eventually every entity will be unloaded in a system without activity, resulting in
			 * dormant systems automatically only consuming a minimum of memory.
			 *
			 * Example:
			 * Assume an "abstract" threshold value of 10 billion and a current cache size of 5 GB ("50% full")
			 * A binary array with a (huge) size of 1 GB will be unloaded after 3 minutes of not being used.
			 * 0th minute: 10bil - 5 bil = 5 bil > 1 bil * 0 * 2
			 * 1st minute: 10bil - 5 bil = 5 bil > 1 bil * 1 * 2
			 * 2nd minute: 10bil - 5 bil = 5 bil > 1 bil * 2 * 2
			 * 3rd minute: 10bil - 5 bil = 5 bil < 1 bil * 3 * 2 -> unload
			 * While smaller entities (typcially ~100 byte), especially ones with reference, can almost always stay
			 * in cache until the timeout hits or the cache gets critically full.
			 * (10bil - 5 bil = 5 bil < 100 * ~90_YEARS * 1)
			 * This is a quite reasonable approach given the rather fast execution time of the formula.
			 *
			 * Note that this method operates solely on this instance and the entity instance, no further pointer
			 * chasing (e.g. to the entity type) and therefore potential cache miss is needed.
			 */
			final long ageInMs = evalTime - e.lastTouched();
//			DEBUGStorage.println("evaluating with cache size " + cacheSize + " and entity age " + ageInMs + " " + e);
			return ageInMs >= this.msTimeout
				|| this.threshold - cacheSize < e.cachedDataLength() * (ageInMs >>> C16) << (e.hasReferences() ? 0 : 1)
			;
		}

		@Override
		public String toString()
		{
			return VarString.New()
				.add(this.getClass().getName()).add(':').lf()
				.blank().add("threshold ").tab().add('=').blank().add(this.threshold).lf()
				.blank().add("timeout   ").tab().add('=').blank().add(this.msTimeout)
				.toString()
			;
		}

	}

}

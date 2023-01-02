package one.microstream.storage.types;

/*-
 * #%L
 * microstream-storage
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

import one.microstream.chars.VarString;
import one.microstream.exceptions.NumberRangeException;

/**
 * Function type that evaluates if a live entity (entity with cached data) shall be unloaded (its cache cleared).
 * <p>
 * Note that any implementation of this type must be safe enough to never throw an exception as this would doom
 * the storage thread that executes it. Catching any exception would not prevent the problem for the channel thread
 * as the function has to work in order for the channel to work properly.
 * It is therefore strongly suggested that implementations only use "exception free" logic (like simple arithmetic)
 * or handle any possible exception internally.
 *
 */
@FunctionalInterface
public interface StorageEntityCacheEvaluator
{
	public boolean clearEntityCache(long totalCacheSize, long evaluationTime, StorageEntity entity);

	public default boolean initiallyCacheEntity(
		final long          totalCacheSize,
		final long          evaluationTime,
		final StorageEntity entity
	)
	{
		return !this.clearEntityCache(totalCacheSize, evaluationTime, entity);
	}



	public interface Defaults
	{
		public static long defaultCacheThreshold()
		{
			// ~1 GB default threshold
			return 1_000_000_000;
		}

		public static long defaultTimeoutMs()
		{
			// 1 day default timeout
			return 86_400_000;
		}
	}

	public interface Validation
	{
		public static long minimumTimeoutMs()
		{
			return 1;
		}
		public static long minimumThreshold()
		{
			return 1;
		}

		public static void validateParameters(
			final long timeoutMs,
			final long threshold
		)
			throws IllegalArgumentException
		{
			if(timeoutMs < minimumTimeoutMs())
			{
				throw new IllegalArgumentException(
					"Specified millisecond timeout of "
					+ timeoutMs
					+ " is lower than the minimum value "
					+ minimumTimeoutMs()+ "."
				);
			}
			if(threshold < minimumThreshold())
			{
				throw new IllegalArgumentException(
					"Specified threshold of "
					+ threshold
					+ " is lower than the minimum value "
					+ minimumThreshold()+ "."
				);
			}
		}
	}

	/**
	 * Pseudo-constructor method to create a new {@link StorageEntityCacheEvaluator} instance
	 * using default values defined by {@link StorageEntityCacheEvaluator.Defaults}.
	 * <p>
	 * For explanations and customizing values, see {@link StorageEntityCacheEvaluator#New(long, long)}.
	 *
	 * @return a new {@link StorageEntityCacheEvaluator} instance.
	 *
	 * @see StorageEntityCacheEvaluator#New(long)
	 * @see StorageEntityCacheEvaluator#New(long, long)
	 * @see StorageEntityCacheEvaluator.Defaults
	 */
	public static StorageEntityCacheEvaluator New()
	{
		/*
		 * Validates its own default value, but the cost is negligible and it is a
		 * good defense against accidentally erroneous changes of the default value.
		 */
		return New(
			Defaults.defaultTimeoutMs()     ,
			Defaults.defaultCacheThreshold()
		);
	}

	/**
	 * Pseudo-constructor method to create a new {@link StorageEntityCacheEvaluator} instance
	 * using the passed value and default values defined by {@link StorageEntityCacheEvaluator.Defaults}.
	 * <p>
	 * For explanations and customizing values, see {@link StorageEntityCacheEvaluator#New(long, long)}.
	 *
	 * @param timeoutMs the time (in milliseconds, greater than 0) of not being read (the "age"), after which a particular
	 *        entity's data will be cleared from the Storage's internal cache.
	 *
	 * @return a new {@link StorageEntityCacheEvaluator} instance.
	 *
	 * @throws NumberRangeException if the passed value is equal to or lower than 0.
	 *
	 * @see StorageEntityCacheEvaluator#New()
	 * @see StorageEntityCacheEvaluator#New(long, long)
	 * @see StorageEntityCacheEvaluator.Defaults
	 */
	public static StorageEntityCacheEvaluator New(final long timeoutMs)
	{
		return New(
			timeoutMs                       ,
			Defaults.defaultCacheThreshold()
		);
	}

	/**
	 * Pseudo-constructor method to create a new {@link StorageEntityCacheEvaluator} instance
	 * using the passed values.
	 * <p>
	 * In the default implementation, two values are combined to calculate an entity's "cache weight":
	 * its "age" (the time in milliseconds of not being read) and its size in bytes. The resulting value is
	 * in turn compared to an abstract "free space" value, calculated by subtracting the current total cache size
	 * in bytes from the abstract {@literal threshold} value defined here. If this comparison deems the tested entity
	 * to be "too heavy" for the cache, its data is cleared from the cache. It is also cleared from the cache if its
	 * "age" is greater than the {@literal timeout} defined here.<br>
	 * This is a relatively simple and extremely fast algorithm to create the following behavior:<br>
	 * <ol>
	 * <li>Cached data that seems to not be used currently ("too old") is cleared.</li>
	 * <li>Apart from that, as long as there is "enough space", nothing is cleared.</li>
	 * <li>The old and bigger an entity's data is, the more likely it is to be cleared.</li>
	 * <li>The less free space there is in the cache, the sooner cached entity data is cleared.</li>
	 * </ol>
	 * This combination of rules is relatively accurate on keeping cached what is needed and dropping the rest,
	 * while being easily tailorable to suit an application's needs.
	 *
	 * @param timeoutMs the time (in milliseconds, greater than 0) of not being read (the "age"), after which a particular
	 *        entity's data will be cleared from the Storage's internal cache.
	 *
	 * @param threshold an abstract value (greater than 0) to evaluate the product of size and age of an entity in relation
	 *        to the current cache size in order to determine if the entity's data shall be cleared from the cache.
	 *
	 * @return a new {@link StorageEntityCacheEvaluator} instance.
	 *
	 * @throws NumberRangeException if any of the passed values is equal to or lower than 0.
	 *
	 * @see StorageEntityCacheEvaluator#New()
	 * @see StorageEntityCacheEvaluator#New(long)
	 * @see StorageEntityCacheEvaluator.Defaults
	 */
	public static StorageEntityCacheEvaluator New(
		final long timeoutMs,
		final long threshold
	)
	{
		Validation.validateParameters(timeoutMs, threshold);

		return new StorageEntityCacheEvaluator.Default(timeoutMs, threshold);
	}

	public final class Default implements StorageEntityCacheEvaluator
	{
		///////////////////////////////////////////////////////////////////////////
		// constants //
		//////////////

		/*
		 * To satisfy CheckStyle. See algorithm comment below.
		 * Shifting by 16 means roughly age in minutes and is fast.
		 */
		private static final int C16 = 16;



		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		/**
		 * Entity age timeout value in milliseconds at which the entity should definitely be cleared from the cache,
		 * independent of size, type or cache fullness.
		 * Example: After one day of not being used, an entity can definitely be unloaded from the cache.
		 * <p/>
		 * Can be set to very low value like 1 hour (3_600_000 ms) for systems with very sporadic activity.
		 * (e.g. an application that performs big operations only every once in a while and is very dormant in between)
		 * Can be set to a medium value like 1 day (86_400_000 ms) for typical server systems
		 * (e.g. constantly working systems with medium load)
		 * Can be set to a huge value to like 1 year or max long to disable the timeout and solely rely on the threshold
		 */
		private final long timeoutMs;

		/**
		 * Abstract threshold value, roughly comparable to size in bytes with a time component, at which a cache
		 * must be cleared of some entities.
		 */
		private final long threshold;



		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		Default(final long timeoutMs, final long threshold)
		{
			super();
			this.timeoutMs = timeoutMs;
			this.threshold = threshold;
		}



		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		public long timeout()
		{
			return this.timeoutMs;
		}
		
		public long threshold()
		{
			return this.threshold;
		}


		@Override
		public final boolean clearEntityCache(
			final long          cacheSize,
			final long          evalTime ,
			final StorageEntity e
		)
		{
			/* simple default algorithm to take cache size, entity cached data length, age and reference into account:
			 *
			 * - subtract current cache size from threshold, resulting in an abstract value how "free" the cache is.
			 *   This also means that if the current cache size alone reaches the threshold, the entity will definitely
			 *   be cleared from the cache, no matter what (panic mode to avoid out of memory situations).
			 *
			 * - calculate "weight" of the entity and compare it to the memory's "freeness" to evaluate.
			 *
			 * "weight" calculation:
			 * - the entity's memory consumption (cached data length)
			 * - multiplied by its "cache age" in ms, divided by 2^16 (roughly equals age in minutes)
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
			 * dormant systems automatically having an empty cache.
			 *
			 * Example:
			 * Assume an "abstract" threshold value of 10 billion and a current cache size of 5 GB ("50% full")
			 * A binary array with a (huge) size of 1 GB will be unloaded after 3 minutes of not being used.
			 * 0th minute: 10bil - 5 bil = 5 bil > 1 bil * 0 * 2
			 * 1st minute: 10bil - 5 bil = 5 bil > 1 bil * 1 * 2
			 * 2nd minute: 10bil - 5 bil = 5 bil > 1 bil * 2 * 2
			 * 3rd minute: 10bil - 5 bil = 5 bil < 1 bil * 3 * 2 -> unload
			 * While smaller entities (typically ~100 byte), especially ones with reference, can almost always stay
			 * in cache until the timeout hits or the cache gets critically full.
			 * (10bil - 5 bil = 5 bil < 100 * ~90_YEARS * 1)
			 * This is a quite reasonable approach given the rather fast execution time of the formula.
			 *
			 * Note that this method operates solely on this instance and the entity instance, no further pointer
			 * chasing (e.g. to the entity type) and therefore potential cache miss is needed.
			 */
			final long ageInMs = evalTime - e.lastTouched();
			/*
			 * Note on ">>":
			 * Cannot use ">>>" here, because some entities are touched "in the future", making age negative.
			 * Unsigned shifting makes that a giant positive age, causing an unwanted unload.
			 * For the formula to be correct, the signed shift has to be used.
			 */
			return ageInMs >= this.timeoutMs
				|| this.threshold - cacheSize < e.cachedDataLength() * (ageInMs >> C16) << (e.hasReferences() ? 0 : 1)
			;
		}

		@Override
		public String toString()
		{
			return VarString.New()
				.add(this.getClass().getName()).add(':').lf()
				.blank().add("threshold ").tab().add('=').blank().add(this.threshold).lf()
				.blank().add("timeout   ").tab().add('=').blank().add(this.timeoutMs)
				.toString()
			;
		}

	}

}

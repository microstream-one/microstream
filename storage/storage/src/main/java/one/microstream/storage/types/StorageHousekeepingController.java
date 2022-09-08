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

import static one.microstream.X.notNull;
import static one.microstream.math.XMath.positive;

import org.slf4j.Logger;

import one.microstream.chars.VarString;
import one.microstream.util.logging.Logging;

public interface StorageHousekeepingController
{
	/**
	 * @return The housekeeping interval in milliseconds.
	 */
	public long housekeepingIntervalMs();

	/**
	 * @return The general housekeeping time budget per interval in nanoseconds.
	 */
	public long housekeepingTimeBudgetNs();

	/**
	 * @return The garbage collection housekeeping time budget per interval in nanoseconds.
	 */
	public long garbageCollectionTimeBudgetNs();

	/**
	 * @return The live/cache check housekeeping time budget per interval in nanoseconds.
	 */
	public long liveCheckTimeBudgetNs();

	/**
	 * @return The file cleanup housekeeping time budget per interval in nanoseconds.
	 */
	public long fileCheckTimeBudgetNs();

	
	
	public interface Validation
	{
		public static long minimumHousekeepingIntervalMs()
		{
			return 1;
		}
		
		public static long minimumHousekeepingTimeBudgetNs()
		{
			return 0;
		}
		
		public static void validateParameters(
			final long housekeepingIntervalMs  ,
			final long housekeepingTimeBudgetNs
		)
			throws IllegalArgumentException
		{
			if(housekeepingIntervalMs < minimumHousekeepingIntervalMs())
			{
				throw new IllegalArgumentException(
					"Specified housekeeping millisecond interval of "
					+ housekeepingIntervalMs
					+ " is lower than the minimum value "
					+ minimumHousekeepingIntervalMs()+ "."
				);
			}
			if(housekeepingTimeBudgetNs < minimumHousekeepingTimeBudgetNs())
			{
				throw new IllegalArgumentException(
					"Specified housekeeping nanosecond time budget of "
					+ housekeepingTimeBudgetNs
					+ " is lower than the minimum value "
					+ minimumHousekeepingTimeBudgetNs()+ "."
				);
			}
		}
	}

	/**
	 * Pseudo-constructor method to create a new {@link StorageHousekeepingController} instance
	 * using default values defined by {@link StorageHousekeepingController.Defaults}.
	 * 
	 * @return a new {@link StorageHousekeepingController} instance.
	 * 
	 * @see StorageHousekeepingController#New(long, long)
	 * @see Storage#HousekeepingController()
	 * @see StorageHousekeepingController.Defaults
	 */
	public static StorageHousekeepingController New()
	{
		/*
		 * Validates its own default values, but the cost is neglible and it is a
		 * good defense against accidentally erroneous changes of the default values.
		 */
		return new StorageHousekeepingController.Default(
			Defaults.defaultHousekeepingIntervalMs(),
			Defaults.defaultHousekeepingTimeBudgetNs()
		);
	}
	
	/**
	 * Pseudo-constructor method to create a new {@link StorageHousekeepingController} instance
	 * using the passed values.<p>
	 * The combination of these two values can be used to define how much percentage of the system's computing power
	 * shall be used for storage housekeeping.<br>
	 * Example:<br>
	 * 10 Million ns (= 10 ms) housekeeping budget every 1000 ms
	 * means (roughly) 1% of the computing power will be used for storage housekeeping.<p>
	 * Note that in an application where no store occures over a longer period of time, all housekeeping tasks
	 * will eventually be completed, reducing the required computing power to 0. When the next store occurs, the
	 * housekeeping starts anew.<br>
	 * How long the housekeeping requires to complete depends on the computing power it is granted by the
	 * {@link StorageHousekeepingController}, other configurations (like entity data cache timeouts)
	 * and the amount of data that has to be managed.
	 * <p>
	 * See all "issue~" methods in {@link StorageConnection} for a way to call housekeeping actions explicitly
	 * and causing them to be executed completely.
	 * 
	 * @param housekeepingIntervalMs the interval in milliseconds that the storage threads shall
	 *        execute their various housekeeping actions (like cache clearing checks, file consolidation, etc.).
	 *        Must be greater than zero.
	 * 
	 * @param housekeepingTimeBudgetNs the time budget in nanoseconds that each storage thread will use to perform
	 *        a housekeeping action. This is a best effort value, not a strictly reliable border value. This means
	 *        a housekeeping action can occasionally take slightly longer than specified here.
	 *        Must be greater than zero.
	 * 
	 * @return a new {@link StorageHousekeepingController} instance.
	 * 
	 * @see StorageHousekeepingController#New()
	 * @see Storage#HousekeepingController(long, long)
	 */
	public static StorageHousekeepingController New(
		final long housekeepingIntervalMs  ,
		final long housekeepingTimeBudgetNs
	)
	{
		Validation.validateParameters(housekeepingIntervalMs, housekeepingTimeBudgetNs);
		
		return new StorageHousekeepingController.Default(
			housekeepingIntervalMs  ,
			housekeepingTimeBudgetNs
		);
	}
	
	public interface Defaults
	{
		public static long defaultHousekeepingIntervalMs()
		{
			return 1_000; // ms
		}
		
		public static long defaultHousekeepingTimeBudgetNs()
		{
			return 10_000_000; // ns
		}
	}


	public final class Default implements StorageHousekeepingController
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		private final long intervalMs, nanoTimeBudget;



		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		Default(final long intervalMs, final long nanoTimeBudget)
		{
			super();
			this.intervalMs     = intervalMs    ;
			this.nanoTimeBudget = nanoTimeBudget;
		}



		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		public final long housekeepingIntervalMs()
		{
			return this.intervalMs;
		}

		@Override
		public final long housekeepingTimeBudgetNs()
		{
			return this.nanoTimeBudget;
		}

		@Override
		public final long garbageCollectionTimeBudgetNs()
		{
			// no special treatment in generic base implementation
			return this.housekeepingTimeBudgetNs();
		}

		@Override
		public final long liveCheckTimeBudgetNs()
		{
			// no special treatment in generic base implementation
			return this.housekeepingTimeBudgetNs();
		}

		@Override
		public final long fileCheckTimeBudgetNs()
		{
			// no special treatment in generic base implementation
			return this.housekeepingTimeBudgetNs();
		}

		@Override
		public String toString()
		{
			return VarString.New()
				.add(this.getClass().getName()).add(':').lf()
				.blank().add("house keeping interval"        ).tab().add('=').blank().add(this.intervalMs).lf()
				.blank().add("house keeping nano time budget").tab().add('=').blank().add(this.nanoTimeBudget)
				.toString()
			;
		}

	}

	
	
	public static StorageHousekeepingController Adaptive(
		final long                 increaseThresholdMs,
		final long                 increaseAmountNs   ,
		final long                 maximumNs          ,
		final StorageFoundation<?> foundation
	)
	{
		return Adaptive(
			StorageHousekeepingController.New(),
			increaseThresholdMs                ,
			increaseAmountNs                   ,
			maximumNs                          ,
			foundation
		);
	}
	
	public static StorageHousekeepingController Adaptive(
		final StorageHousekeepingController delegate           ,
		final long                          increaseThresholdMs,
		final long                          increaseAmountNs   ,
		final long                          maximumNs          ,
		final StorageFoundation<?>          foundation
	)
	{
		final StorageHousekeepingController.Adaptive controller = new StorageHousekeepingController.Adaptive(
			notNull (delegate           ),
			positive(increaseThresholdMs),
			positive(increaseAmountNs   ),
			positive(maximumNs          )
		);
		foundation.addEventLogger(controller);
		return controller;
	}
	
	
	public static AdaptiveBuilder AdaptiveBuilder()
	{
		return AdaptiveBuilder(StorageHousekeepingController.New());
	}
	
	
	public static AdaptiveBuilder AdaptiveBuilder(final StorageHousekeepingController delegate)
	{
		return new AdaptiveBuilder.Default(
			notNull(delegate)
		);
	}
	
	
	public interface AdaptiveBuilder
	{
		public AdaptiveBuilder increaseThresholdMs(long increaseThresholdMs);
		
		public AdaptiveBuilder increaseAmountNs(long increaseAmountNs);
		
		public AdaptiveBuilder maximumNs(long maximumNs);
		
		public StorageHousekeepingController buildFor(final StorageFoundation<?> foundation);
		
		
		public static class Default implements AdaptiveBuilder
		{
			private final StorageHousekeepingController delegate            ;
			private long                                increaseThresholdMs = Adaptive.Defaults.increaseThresholdMs();
			private long                                increaseAmountNs    = Adaptive.Defaults.increaseAmountNs   ();
			private long                                maximumNs           = Adaptive.Defaults.maximumNs          ();
			
			Default(final StorageHousekeepingController delegate)
			{
				super();
				this.delegate = delegate;
			}
		
			@Override
			public AdaptiveBuilder increaseThresholdMs(final long increaseThresholdMs)
			{
				this.increaseThresholdMs = increaseThresholdMs;
				return this;
			}
			
			@Override
			public AdaptiveBuilder increaseAmountNs(final long increaseAmountNs)
			{
				this.increaseAmountNs = increaseAmountNs;
				return this;
			}
			
			@Override
			public AdaptiveBuilder maximumNs(final long maximumNs)
			{
				this.maximumNs = maximumNs;
				return this;
			}
			
			@Override
			public StorageHousekeepingController buildFor(final StorageFoundation<?> foundation)
			{
				final StorageHousekeepingController.Adaptive controller = new StorageHousekeepingController.Adaptive(
					this.delegate           ,
					this.increaseThresholdMs,
					this.increaseAmountNs   ,
					this.maximumNs
				);
				foundation.addEventLogger(controller);
				return controller;
			}
			
		}
		
	}
	
	
	public final class Adaptive implements StorageHousekeepingController, StorageEventLogger
	{
		public interface Defaults
		{
			public static long increaseThresholdMs()
			{
				return 5000; // 5 seconds
			}
			
			public static long increaseAmountNs()
			{
				return 50_000_000; // 50 ms
			}
			
			public static long maximumNs()
			{
				return 500_000_000; // half second
			}
		}
		

		private final static Logger logger = Logging.getLogger(Adaptive.class);
		
		
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private final StorageHousekeepingController delegate           ;
		private final long                          increaseThresholdMs;
		private final long                          increaseAmountNs   ;
		private final long                          maximumNs          ;
				
		// mutable adaptive state
		
		private long                                lastFinishedGCCycle = 0;
		private long                                lastIncrease        = 0;
		private long                                currentIncreaseNs   = 0;
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		Adaptive(
			final StorageHousekeepingController delegate           ,
			final long                          increaseThresholdMs,
			final long                          increaseAmountNs   ,
			final long                          maximumNs
		)
		{
			super();
			this.delegate            = delegate           ;
			this.increaseThresholdMs = increaseThresholdMs;
			this.increaseAmountNs    = increaseAmountNs   ;
			this.maximumNs           = maximumNs          ;
		}
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		private synchronized void reset()
		{
			this.lastFinishedGCCycle = this.lastIncrease = System.currentTimeMillis();
			this.internalSetIncrease(0);
		}
		
		private synchronized long increaseNs()
		{
			final long now = System.currentTimeMillis();
			if( now - this.increaseThresholdMs > this.lastFinishedGCCycle
			&& (this.lastIncrease <= 0 || now - this.lastIncrease > this.increaseThresholdMs)
			)
			{
				this.lastIncrease = now;
				this.internalSetIncrease(this.currentIncreaseNs + this.increaseAmountNs);
			}
			return this.currentIncreaseNs;
		}
		
		private void internalSetIncrease(final long increaseNs)
		{
			this.currentIncreaseNs = Math.min(
				this.maximumNs,
				increaseNs
			);
			logger.debug("New adaptive housekeeping increase: {} ns", String.format("%,d", this.currentIncreaseNs));
		}

		@Override
		public long housekeepingIntervalMs()
		{
			return this.delegate.housekeepingIntervalMs();
		}

		@Override
		public long housekeepingTimeBudgetNs()
		{
			return Math.min(
				this.maximumNs,
				this.delegate.housekeepingTimeBudgetNs() + this.increaseNs()
			);
		}

		@Override
		public long garbageCollectionTimeBudgetNs()
		{
			return Math.min(
				this.maximumNs,
				this.delegate.garbageCollectionTimeBudgetNs() + this.increaseNs()
			);
		}

		@Override
		public long liveCheckTimeBudgetNs()
		{
			return Math.min(
				this.maximumNs,
				this.delegate.liveCheckTimeBudgetNs() + this.increaseNs()
			);
		}

		@Override
		public long fileCheckTimeBudgetNs()
		{
			return Math.min(
				this.maximumNs,
				this.delegate.fileCheckTimeBudgetNs() + this.increaseNs()
			);
		}
		
		@Override
		public void logGarbageCollectorNotNeeded()
		{
			this.reset();
		}
		
		@Override
		public void logGarbageCollectorSweepingComplete(final StorageEntityCache<?> entityCache)
		{
			this.reset();
		}
		
	}

}

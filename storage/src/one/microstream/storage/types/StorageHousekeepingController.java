package one.microstream.storage.types;

import one.microstream.chars.VarString;
import one.microstream.math.XMath;

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
	
	public static StorageHousekeepingController New(
		final long housekeepingIntervalMs  ,
		final long housekeepingTimeBudgetNs
	)
	{
		return new StorageHousekeepingController.Default(
			XMath.positive(housekeepingIntervalMs)  ,
			XMath.positive(housekeepingTimeBudgetNs)
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

}

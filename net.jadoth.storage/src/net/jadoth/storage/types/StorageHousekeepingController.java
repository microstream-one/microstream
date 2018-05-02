package net.jadoth.storage.types;

import net.jadoth.util.chars.VarString;

public interface StorageHousekeepingController
{
	/**
	 * @return The housekeeping interval in milliseconds.
	 */
	public long housekeepingInterval();

	/**
	 * @return The general housekeeping time budget per interval in nanoseconds.
	 */
	public long housekeepingNanoTimeBudgetBound();

	/**
	 * @return The garbage collection housekeeping time budget per interval in nanoseconds.
	 */
	public long garbageCollectionNanoTimeBudget();

	/**
	 * @return The live/cache check housekeeping time budget per interval in nanoseconds.
	 */
	public long liveCheckNanoTimeBudget();

	/**
	 * @return The file cleanup housekeeping time budget per interval in nanoseconds.
	 */
	public long fileCheckNanoTimeBudget();



	public final class Implementation implements StorageHousekeepingController
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields  //
		/////////////////////

		private final long housekeepingInterval, housekeepingNanoTimeBudget;



		///////////////////////////////////////////////////////////////////////////
		// constructors     //
		/////////////////////

		public Implementation(final long housekeepingInterval, final long housekeepingNanoTimeBudget)
		{
			super();
			this.housekeepingInterval       = housekeepingInterval      ;
			this.housekeepingNanoTimeBudget = housekeepingNanoTimeBudget;
		}



		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		public final long housekeepingInterval()
		{
			return this.housekeepingInterval;
		}

		@Override
		public final long housekeepingNanoTimeBudgetBound()
		{
			return this.housekeepingNanoTimeBudget;
		}

		@Override
		public final long garbageCollectionNanoTimeBudget()
		{
			// no special treatment in generic base implementation
			return this.housekeepingNanoTimeBudgetBound();
		}

		@Override
		public final long liveCheckNanoTimeBudget()
		{
			// no special treatment in generic base implementation
			return this.housekeepingNanoTimeBudgetBound();
		}

		@Override
		public final long fileCheckNanoTimeBudget()
		{
			// no special treatment in generic base implementation
			return this.housekeepingNanoTimeBudgetBound();
		}

		@Override
		public String toString()
		{
			return VarString.New()
				.add(this.getClass().getName()).add(':').lf()
				.blank().add("house keeping interval"        ).tab().add('=').blank().add(this.housekeepingInterval).lf()
				.blank().add("house keeping nano time budget").tab().add('=').blank().add(this.housekeepingNanoTimeBudget)
				.toString()
			;
		}

	}

}

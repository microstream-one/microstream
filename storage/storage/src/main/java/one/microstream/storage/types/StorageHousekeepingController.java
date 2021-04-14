package one.microstream.storage.types;

import one.microstream.chars.VarString;

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
	 * @return {@linkDoc StorageHousekeepingController#New(long, long)@return}
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

}

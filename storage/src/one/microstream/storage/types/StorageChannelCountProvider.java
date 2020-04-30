package one.microstream.storage.types;

import one.microstream.chars.VarString;
import one.microstream.chars.XChars;
import one.microstream.math.XMath;
import one.microstream.persistence.binary.types.BinaryChannelCountProvider;


@FunctionalInterface
public interface StorageChannelCountProvider extends BinaryChannelCountProvider
{
	@Override
	public int getChannelCount();



	public interface Defaults
	{
		public static int defaultChannelCount()
		{
			// single-threaded by default.
			return 1;
		}
	}

	public interface Validation
	{
		/**
		 * What was that: <i>'640 KB ought to be enough RAM for anybody'</i>?<br>
		 * Nevertheless, I'll stick with that bound for now.<br>
		 * TM, 2013-06-20
		 * <p>
		 * On a more serious note:<br>
		 * This check has no actual technical background, it is just a safety net against
		 * oversight mistakes to prevent creation of thousands of threads and files.<br>
		 * Can be altered or removed anytime.
		 *
		 * @return the maximum channel count value of {@literal 1024}.
		 */
		public static int maximumChannelCount()
		{
			return 1024;
		}

		/**
		 * This is NOT necessarily the default channel count.
		 * If the default changes to 2, the lowest valid count will still be 1!
		 *
		 * @return the minimum channel count value of {@literal 1}.
		 */
		public static int minimumChannelCount()
		{
			return 1;
		}


		public static boolean isValidChannelCountRange(final int channelCount)
		{
			// breakpoint-friendly statement
			return channelCount >= minimumChannelCount() && channelCount <= maximumChannelCount()
				? true
				: false
			;
		}

		public static boolean isValidChannelCountPow2Value(final int channelCount)
		{
			// breakpoint-friendly statement
			return XMath.isPow2(channelCount)
				? true
				: false
			;
		}

		public static void validateParameters(
			final int channelCount
		)
			throws IllegalArgumentException
		{
			if(!isValidChannelCountRange(channelCount))
			{
				// (26.03.2019 TM)EXCP: proper exception
				throw new IllegalArgumentException(
					"Specified channel count " + channelCount
					+ " is not in the range of valid channel counts: "
					+ XChars.mathRangeIncInc(minimumChannelCount(), maximumChannelCount())
					+ "."
				);
			}

			if(!isValidChannelCountPow2Value(channelCount))
			{
				// (26.03.2019 TM)EXCP: proper exception
				throw new IllegalArgumentException(
					"Specified channel count " + channelCount
					+ " is not a power-of-2 int value "
					+ "(i.e. channelCount = 2^n for any n in [0;30], e.g. 1, 2, 4, 8, 16, ...)."
				);
			}
		}
	}

	public static int validateChannelCount(final int channelCount) throws IllegalArgumentException
	{
		Validation.validateParameters(channelCount);

		return channelCount;
	}




	/**
	 * Pseudo-constructor method to create a new {@link StorageChannelCountProvider} instance
	 * using default values defined by {@link StorageChannelCountProvider.Defaults}.
	 * <p>
	 * For explanations and customizing values, see {@link StorageChannelCountProvider#New(int)}.
	 *
	 * @return {@linkDoc StorageChannelCountProvider#New(int)@return}
	 *
	 * @see StorageChannelCountProvider#New(int)
	 * @see StorageChannelCountProvider.Defaults
	 */
	public static StorageChannelCountProvider New()
	{
		/*
		 * Validates its own default value, but the cost is neglible and it is a
		 * good defense against accidentally erroneous changes of the default value.
		 */
		return New(
			Defaults.defaultChannelCount()
		);
	}

	/**
	 * Pseudo-constructor method to create a new {@link StorageChannelCountProvider} instance
	 * using the passed value.
	 * <p>
	 * A "channel" is the combination of exclusive storage {@link Thread} maintaining an entity registry, data cache,
	 * and the like and a storage directory containing storage data files exclusively used by that thread.<br>
	 * The more channels there are, the more parallel loading and storing operations will be executed and the faster
	 * the application will become, provided a hardware that can effectively execute that many threads.
	 * <p>
	 * Since channels use bitwise modulo hashing, the number of channels must always be
	 * a 2^n number with n greater than or equal 0.<br>
	 * (Meaning 1, 2, 4, 8, 16, etc.)
	 *
	 * @param channelCount the number of channels. Must be a 2^n number with n greater than or equal 0.
	 *
	 * @return a new {@link StorageChannelCountProvider} instance.
	 *
	 * @throws IllegalArgumentException if the passed value is higher than the value returned by
	 *         {@link StorageChannelCountProvider.Validation#maximumChannelCount()}
	 *
	 * @see StorageChannelCountProvider#New()
	 * @see StorageChannelCountProvider.Defaults
	 */
	public static StorageChannelCountProvider New(final int channelCount)
	{
		Validation.validateParameters(channelCount);

		return new StorageChannelCountProvider.Default(channelCount);
	}

	public final class Default implements StorageChannelCountProvider
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		private final int channelCount;



		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		Default(final int channelCount)
		{
			super();
			this.channelCount = channelCount;
		}



		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		public final int getChannelCount()
		{
			return this.channelCount;
		}

		@Override
		public String toString()
		{
			return VarString.New()
				.add(this.getClass().getName()).add(':').lf()
				.blank().add("channel count").tab().add('=').blank().add(this.channelCount)
				.toString()
			;
		}

	}

}

package one.microstream.storage.types;

import one.microstream.chars.VarString;
import one.microstream.reference._intReference;


public interface StorageChannelCountProvider extends _intReference
{
	@Override
	public int get();

	
	
	public interface Defaults
	{
		public static int defaultChannelCount()
		{
			// single-threaded by default.
			return 1;
		}
		
		/**
		 * What was that: <i>'640 KB ought to be enough RAM for anybody'</i>?<br>
		 * Nevertheless, I'll stick with that bound for now.<br>
		 * TM, 2013-06-20
		 * <p>
		 * On a more serious note:<br>
		 * This check has no actual technical background, it is just a safety net against
		 * oversight mistakes to prevent creation of hundreds of threads and files.<br>
		 * Can be altered or removed anytime.
		 * 
		 * @return the default highest sane channel count value of {@literal 64}.
		 */
		public static int defaultHighestSaneChannelCount()
		{
			return 64;
		}
	}
	
	
	public static boolean isValidChannelCount(final int channelCount)
	{
		if(channelCount > Defaults.defaultHighestSaneChannelCount())
		{
			return false;
		}
		
		/*
		 * This is NOT necessarily the default channel count.
		 * If that changes to 2, the lowest valid count will still be 1!
		 */
		return channelCount >= 1;
	}
	
	public static int validateChannelCount(final int channelCount) throws IllegalArgumentException
	{
		if(isValidChannelCount(channelCount))
		{
			return channelCount;
		}
		
		// (26.03.2019 TM)EXCP: proper exception
		throw new IllegalArgumentException("Not a sane channel count: " + channelCount);
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
	 *         {@link StorageChannelCountProvider.Defaults#defaultHighestSaneChannelCount()}
	 * 
	 * @see StorageChannelCountProvider#New()
	 * @see StorageChannelCountProvider.Defaults
	 */
	public static StorageChannelCountProvider New(final int channelCount)
	{
		return new StorageChannelCountProvider.Default(
			validateChannelCount(channelCount)
		);
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
		public final int get()
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

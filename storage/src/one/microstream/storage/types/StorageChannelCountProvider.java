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
		
		public static int defaultHighestSaneChannelCount()
		{
			/* (20.06.2013 TM)NOTE:
			 * What was that: '640 KB ought to be enough RAM for anybody'?
			 * Nevertheless, I'll stick with that bound for now.
			 * 
			 * On a more serious note:
			 * This check has no actual technical background, it is just a safety net against
			 * versight mistakes to prevent creation of hundreds of threads and files.
			 * Can be altered or removed anytime.
			 */
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
	
	public static int validateChannelCount(final int channelCount)
	{
		if(isValidChannelCount(channelCount))
		{
			return channelCount;
		}
		
		// (26.03.2019 TM)EXCP: proper exception
		throw new IllegalArgumentException("Not a sane channel count: " + channelCount);
	}
	
	
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

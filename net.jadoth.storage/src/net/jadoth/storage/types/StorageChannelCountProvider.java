package net.jadoth.storage.types;

import net.jadoth.math.JadothMath;
import net.jadoth.reference._intReference;
import net.jadoth.util.chars.VarString;


public interface StorageChannelCountProvider extends _intReference
{
	@Override
	public int get();



	public final class Implementation implements StorageChannelCountProvider
	{
		///////////////////////////////////////////////////////////////////////////
		// constants        //
		/////////////////////
		
		// what was that: '640K ought to be enough for anybody'? Nevertheless, I'll stick with this for now
		private static final int HIGHEST_SANE_CHANNEL_COUNT = 64;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// instance fields  //
		/////////////////////

		private final int channelCount;



		///////////////////////////////////////////////////////////////////////////
		// constructors     //
		/////////////////////

		public Implementation(final int channelCount)
		{
			super();
			/* (20.06.2013 TM)NOTE: this check has no actual technical background,
			 * just a safety net against oversight mistakes to prevent creation of hundreds of threads and files.
			 * Can be altered or removed anytime.
			 */
			if(channelCount > HIGHEST_SANE_CHANNEL_COUNT)
			{
				// (16.04.2016)EXCP: properly typed exception
				throw new IllegalArgumentException("Insane channel counts are not supported");
			}
			this.channelCount = JadothMath.positive(channelCount);
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

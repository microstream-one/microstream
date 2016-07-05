package net.jadoth.storage.types;


@FunctionalInterface
public interface StorageValidRootIdCalculator
{
	public long determineValidRootId(StorageEntityCache<?>[] entityCaches);



	@FunctionalInterface
	public interface Provider
	{
		public StorageValidRootIdCalculator provideValidRootIdCalculator(int channelCount);


		public final class Implementation implements StorageValidRootIdCalculator.Provider
		{
			@Override
			public StorageValidRootIdCalculator provideValidRootIdCalculator(final int channelCount)
			{
				return new StorageValidRootIdCalculator.Implementation(channelCount);
			}
		}
	}

	public final class Implementation implements StorageValidRootIdCalculator
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		private final long[] maxRootOids;



		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		Implementation(final int channelCount)
		{
			super();
			this.maxRootOids = new long[channelCount];
		}



		///////////////////////////////////////////////////////////////////////////
		// override methods //
		/////////////////////

		@Override
		public final long determineValidRootId(final StorageEntityCache<?>[] entityCaches)
		{
			for(int i = 0; i < entityCaches.length; i++)
			{
				this.maxRootOids[i] = entityCaches[i].getHighestRootInstanceObjectId();
			}

			long totalMax = 0;
			for(int i = 0; i < entityCaches.length; i++)
			{
				if(this.maxRootOids[i] >= totalMax)
				{
					totalMax = this.maxRootOids[i];
				}
			}

			return totalMax;
		}

	}

}

package one.microstream.storage.types;

public interface StorageRawFileStatisticsItem
{
	public long fileCount();

	public long liveDataLength();

	public long totalDataLength();



	public abstract class Abstract implements StorageRawFileStatisticsItem
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		final long fileCount      ;
		final long liveDataLength ;
		final long totalDataLength;



		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		Abstract(final long fileCount, final long liveDataLength, final long totalDataLength)
		{
			super();
			this.fileCount       = fileCount      ;
			this.liveDataLength  = liveDataLength ;
			this.totalDataLength = totalDataLength;
		}



		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		public final long fileCount()
		{
			return this.fileCount;
		}

		@Override
		public final long liveDataLength()
		{
			return this.liveDataLength;
		}

		@Override
		public final long totalDataLength()
		{
			return this.totalDataLength;
		}

	}

}

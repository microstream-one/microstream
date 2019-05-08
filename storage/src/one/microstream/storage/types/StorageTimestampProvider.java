package one.microstream.storage.types;

public interface StorageTimestampProvider
{
	/**
	 * Provides the current timestamp in nanosecond precision but not necessarily in nanosecond accuracy.
	 * However it is guaranteed that subsequent calls of this method never return an equal or lower value.
	 * 
	 * @return a strictly monotone increasing timestamp with nanosecond precision.
	 */
	public long currentNanoTimestamp();
	
	
	public final class Default implements StorageTimestampProvider
	{
		private long lastTimeMillis, currentOffset;

		@Override
		public synchronized long currentNanoTimestamp()
		{
			final long currentTimeMillis;
			if((currentTimeMillis = System.currentTimeMillis()) == this.lastTimeMillis)
			{
				return Storage.millisecondsToNanoseconds(currentTimeMillis) + ++this.currentOffset;
			}
			// a read and check every time is faster than an (almost always unnecessary) write every time.
			if(this.currentOffset != 0)
			{
				this.currentOffset = 0;
			}
			return Storage.millisecondsToNanoseconds(this.lastTimeMillis = currentTimeMillis);
		}
		
	}
}

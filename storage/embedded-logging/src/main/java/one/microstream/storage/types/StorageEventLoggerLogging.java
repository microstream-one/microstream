package one.microstream.storage.types;

import static one.microstream.X.notNull;

public interface StorageEventLoggerLogging extends StorageEventLogger, StorageLoggingWrapper<StorageEventLogger>
{
	static StorageEventLoggerLogging New(final StorageEventLogger wrapped)
	{
		return new Default(notNull(wrapped));
	}
	
    public class Default
    	extends StorageLoggingWrapper.Abstract<StorageEventLogger>
    	implements StorageEventLoggerLogging
    {

		protected Default(final StorageEventLogger wrapped)
		{
			super(wrapped);
		}
    	
		@Override
		public void logChannelProcessingDisabled(final StorageChannel channel)
		{
			this.wrapped().logChannelProcessingDisabled(channel);
			
			this.logger().storageEventLogger_afterLogChannelProcessingDisabled(channel);
		}

		@Override
		public void logChannelStoppedWorking(final StorageChannel channel)
		{
			this.wrapped().logChannelStoppedWorking(channel);
			
			this.logger().storageEventLogger_afterLogChannelStoppedWorking(channel);
		}

		@Override
		public void logDisruption(final StorageChannel channel, final Throwable t)
		{
			this.wrapped().logDisruption(channel, t);
			
			this.logger().storageEventLogger_afterLogDisruption(channel, t);
		}

		@Override
		public void logLiveCheckComplete(final StorageEntityCache<?> entityCache)
		{
			this.wrapped().logLiveCheckComplete(entityCache);
			
			this.logger().storageEventLogger_afterLogLiveCheckComplete(entityCache);
		}

		@Override
		public void logGarbageCollectorSweepingComplete(final StorageEntityCache<?> entityCache)
		{
			this.wrapped().logGarbageCollectorSweepingComplete(entityCache);
			
			this.logger().storageEventLogger_afterLogGarbageCollectorSweepingComplete(entityCache);
		}

		@Override
		public void logGarbageCollectorNotNeeded()
		{
			this.wrapped().logGarbageCollectorNotNeeded();
			
			this.logger().storageEventLogger_afterLogGarbageCollectorNotNeeded();
		}

		@Override
		public void logGarbageCollectorCompletedHotPhase(final long gcHotGeneration, final long lastGcHotCompletion)
		{
			this.wrapped().logGarbageCollectorCompletedHotPhase(gcHotGeneration, lastGcHotCompletion);
			
			this.logger().storageEventLogger_afterLogGarbageCollectorCompletedHotPhase(gcHotGeneration, lastGcHotCompletion);
		}

		@Override
		public void logGarbageCollectorCompleted(final long gcColdGeneration, final long lastGcColdCompletion)
		{
			this.wrapped().logGarbageCollectorCompleted(gcColdGeneration, lastGcColdCompletion);
			
			this.logger().storageEventLogger_afterLogGarbageCollectorCompleted(gcColdGeneration, lastGcColdCompletion);
		}

		@Override
		public void logGarbageCollectorEncounteredZombieObjectId(final long objectId)
		{
			this.wrapped().logGarbageCollectorEncounteredZombieObjectId(objectId);
			
			this.logger().storageEventLogger_afterLogGarbageCollectorEncounteredZombieObjectId(objectId);
		}
    }

}

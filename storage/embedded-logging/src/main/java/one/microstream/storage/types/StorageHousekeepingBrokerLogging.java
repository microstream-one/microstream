package one.microstream.storage.types;

import static one.microstream.X.notNull;

public interface StorageHousekeepingBrokerLogging extends StorageHousekeepingBroker, StorageLoggingWrapper<StorageHousekeepingBroker>
{
	public static StorageHousekeepingBrokerLogging New(final StorageHousekeepingBroker wrapped)
	{
		return new Default(notNull(wrapped));
	}
	
	public class Default
		extends StorageLoggingWrapper.Abstract<StorageHousekeepingBroker>
		implements StorageHousekeepingBrokerLogging
	{

		protected Default(final StorageHousekeepingBroker wrapped)
		{
			super(wrapped);
		}
		
		@Override
		public boolean performIssuedFileCleanupCheck(final StorageHousekeepingExecutor executor, final long nanoTimeBudget)
		{
			this.logger().storageHousekeepingBroker_beforePerformIssuedFileCleanupCheck(executor, nanoTimeBudget);
			
			final boolean result =  this.wrapped().performIssuedFileCleanupCheck(executor, nanoTimeBudget);
			
			this.logger().storageHousekeepingBroker_afterPerformIssuedFileCleanupCheck(executor, result);
			
			return result;
		}

		@Override
		public boolean performIssuedGarbageCollection(final StorageHousekeepingExecutor executor, final long nanoTimeBudget)
		{
			this.logger().storageHousekeepingBroker_beforePerformIssuedGarbageCollection(executor, nanoTimeBudget);
			
			final boolean result =  this.wrapped().performIssuedGarbageCollection(executor, nanoTimeBudget);
			
			this.logger().storageHousekeepingBroker_afterPerformIssuedGarbageCollection(executor, result);
			
			return result;
		}

		@Override
		public boolean performIssuedEntityCacheCheck(final StorageHousekeepingExecutor executor, final long nanoTimeBudget,
			final StorageEntityCacheEvaluator entityEvaluator)
		{
			this.logger().storageHousekeepingBroker_beforePerformIssuedEntityCacheCheck(executor, nanoTimeBudget);
			
			final boolean result =  this.wrapped().performIssuedEntityCacheCheck(executor, nanoTimeBudget, entityEvaluator);
			
			this.logger().storageHousekeepingBroker_afterePerformIssuedEntityCacheCheck(executor, result);
			
			return result;
		}

		@Override
		public boolean performFileCleanupCheck(final StorageHousekeepingExecutor executor, final long nanoTimeBudget)
		{
			this.logger().storageHousekeepingBroker_beforePerformFileCleanupCheck(executor, nanoTimeBudget);
			
			final boolean result = this.wrapped().performFileCleanupCheck(executor, nanoTimeBudget);
			
			this.logger().storageHousekeepingBroker_afterPerformFileCleanupCheck(executor, result);
			
			return result;
		}

		@Override
		public boolean performGarbageCollection(final StorageHousekeepingExecutor executor, final long nanoTimeBudget)
		{
			this.logger().storageHousekeepingBroker_beforePerformGarbageCollection(executor, nanoTimeBudget);
			
			final boolean result =  this.wrapped().performGarbageCollection(executor, nanoTimeBudget);
			
			this.logger().storageHousekeepingBroker_afterPerformGarbageCollection(executor, result);
			
			return result;
		}

		@Override
		public boolean performEntityCacheCheck(final StorageHousekeepingExecutor executor, final long nanoTimeBudget)
		{
			this.logger().storageHousekeepingBroker_beforePerformEntityCacheCheck(executor, nanoTimeBudget);
			
			final boolean result =  this.wrapped().performEntityCacheCheck(executor, nanoTimeBudget);
			
			this.logger().storageHousekeepingBroker_afterPerformEntityCacheCheck(executor, result);
			
			return result;
		}
		
	}
	
}

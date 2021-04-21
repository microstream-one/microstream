package one.microstream.storage.types;

import static one.microstream.X.notNull;

public interface StorageEntityCacheEvaluatorLogging extends StorageEntityCacheEvaluator, StorageLoggingWrapper<StorageEntityCacheEvaluator>
{
	public static StorageEntityCacheEvaluatorLogging New(final StorageEntityCacheEvaluator wrapped)
	{
		return new Default(notNull(wrapped));
	}
	
	static class Default
		extends StorageLoggingWrapper.Abstract<StorageEntityCacheEvaluator>
		implements StorageEntityCacheEvaluatorLogging
	{
		protected Default(final StorageEntityCacheEvaluator wrapped)
		{
			super(wrapped);
		}

		@Override
		public boolean clearEntityCache(final long totalCacheSize, final long evaluationTime, final StorageEntity entity)
		{
			final boolean result = this.wrapped().clearEntityCache(totalCacheSize, evaluationTime, entity);
			
			if(result == true)
			{
				this.logger().storageEntityCacheEvaluator_afterClearEntityCache_true(entity);
			}
			
			
			return result;
		}

		@Override
		public boolean initiallyCacheEntity(final long totalCacheSize, final long evaluationTime, final StorageEntity entity)
		{
			return this.wrapped().initiallyCacheEntity(totalCacheSize, evaluationTime, entity);
		}
		
	}
	
}

package one.microstream.storage.types;

public interface StorageHousekeepingBroker
{
	public boolean performIssuedFileCleanupCheck(
		StorageHousekeepingExecutor executor      ,
		long                        nanoTimeBudget
	);
	
	public boolean performIssuedGarbageCollection(
		StorageHousekeepingExecutor executor      ,
		long                        nanoTimeBudget
	);
	
	public boolean performIssuedEntityCacheCheck(
		StorageHousekeepingExecutor executor       ,
		long                        nanoTimeBudget ,
		StorageEntityCacheEvaluator entityEvaluator
	);
	
	
	public boolean performFileCleanupCheck(
		StorageHousekeepingExecutor executor      ,
		long                        nanoTimeBudget
	);
	
	public boolean performGarbageCollection(
		StorageHousekeepingExecutor executor      ,
		long                        nanoTimeBudget
	);
	
	public boolean performEntityCacheCheck(
		StorageHousekeepingExecutor executor      ,
		long                        nanoTimeBudget
	);
	
	
	public static StorageHousekeepingBroker New()
	{
		return new StorageHousekeepingBroker.Default();
	}
	
	public final class Default implements StorageHousekeepingBroker
	{
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		Default()
		{
			super();
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		@Override
		public boolean performIssuedFileCleanupCheck(
			final StorageHousekeepingExecutor executor      ,
			final long                        nanoTimeBudget
		)
		{
			return executor.performIssuedFileCleanupCheck(nanoTimeBudget);
		}
		
		@Override
		public boolean performIssuedGarbageCollection(
			final StorageHousekeepingExecutor executor      ,
			final long                        nanoTimeBudget
		)
		{
			return executor.performIssuedGarbageCollection(nanoTimeBudget);
		}
		
		@Override
		public boolean performIssuedEntityCacheCheck(
			final StorageHousekeepingExecutor executor      ,
			final long                        nanoTimeBudget,
			final StorageEntityCacheEvaluator evaluator
		)
		{
			return executor.performIssuedEntityCacheCheck(nanoTimeBudget, evaluator);
		}
		

		@Override
		public boolean performFileCleanupCheck(
			final StorageHousekeepingExecutor executor      ,
			final long                        nanoTimeBudget
		)
		{
			return executor.performFileCleanupCheck(nanoTimeBudget);
		}

		@Override
		public boolean performGarbageCollection(
			final StorageHousekeepingExecutor executor      ,
			final long                        nanoTimeBudget
		)
		{
			return executor.performGarbageCollection(nanoTimeBudget);
		}

		@Override
		public boolean performEntityCacheCheck(
			final StorageHousekeepingExecutor executor      ,
			final long                        nanoTimeBudget
		)
		{
			return executor.performEntityCacheCheck(nanoTimeBudget);
		}
		
	}
	
}

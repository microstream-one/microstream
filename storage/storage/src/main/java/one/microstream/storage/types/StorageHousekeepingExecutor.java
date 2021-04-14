package one.microstream.storage.types;

public interface StorageHousekeepingExecutor
{

	public boolean performIssuedFileCleanupCheck(long nanoTimeBudget);
	
	public boolean performIssuedGarbageCollection(long nanoTimeBudget);
	
	public boolean performIssuedEntityCacheCheck(long nanoTimeBudget, StorageEntityCacheEvaluator evaluator);
	
	
	public boolean performFileCleanupCheck(long nanoTimeBudget);
	
	public boolean performGarbageCollection(long nanoTimeBudget);
	
	public boolean performEntityCacheCheck(long nanoTimeBudget);
	
}

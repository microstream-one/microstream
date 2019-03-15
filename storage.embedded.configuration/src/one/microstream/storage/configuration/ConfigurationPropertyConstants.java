
package one.microstream.storage.configuration;


public interface ConfigurationPropertyConstants
{
	public final static String	STORAGE_DIRECTORY							= "storage.directory";
	public final static String	STORAGE_CHANNEL_COUNT						= "storage.channelCount";
	public final static String	STORAGE_CHANNEL_DIRECTORY_BASENAME			= "storage.channelDirectoryBaseName";
	public final static String	STORAGE_FILE_BASENAME						= "storage.fileBaseName";
	public final static String	STORAGE_FILE_SUFFIX							= "storage.fileSuffix";
	public final static String	STORAGE_FILENAME_TYPE_DICTIONARY			= "storage.filenameTypeDictionary";
	public final static String	STORAGE_FILENAME_TYPE_ID					= "storage.filenameTypeId";
	public final static String	STORAGE_FILENAME_OBJECT_ID					= "storage.filenameObjectId";
	public final static String	STORAGE_HOUSEKEEPING_INTERVAL				= "storage.houseKeeping.interval";
	public final static String	STORAGE_HOUSEKEEPING_NANO_TIME_BUDGET		= "storage.houseKeeping.nanoTimeBudget";
	public final static String	STORAGE_ENTITY_CACHE_EVALUATOR_THRESHOLD	= "storage.entityCacheEvaluator.threshold";
	public final static String	STORAGE_ENTITY_CACHE_EVALUATOR_TIMEOUT		= "storage.entityCacheEvaluator.timeout";
	public final static String	STORAGE_DATA_FILE_EVALUATOR_MIN_FILE_SIZE	= "storage.dataFileEvaluator.minFileSize";
	public final static String	STORAGE_DATA_FILE_EVALUATOR_MAX_FILE_SIZE	= "storage.dataFileEvaluator.maxFileSize";
	public final static String	STORAGE_DATA_FILE_EVALUATOR_DISSOLVE_RATIO	= "storage.dataFileEvaluator.dissolveRatio";
}

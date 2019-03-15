
package one.microstream.storage.configuration;


public interface ConfigurationPropertyNames
{
	public final static String	BASE_DIRECTORY                = "baseDirectory";
	public final static String	DELETION_DIRECTORY            = "deletionDirectory";
	public final static String	TRUNCATION_DIRECTORY          = "truncationDirectory";
	public final static String	BACKUP_DIRECTORY              = "backupDirectory";
	public final static String	CHANNEL_COUNT                 = "channelCount";
	public final static String	CHANNEL_DIRECTORY_PREFIX      = "channelDirectoryPrefix";
	public final static String	DATA_FILE_PREFIX              = "dataFilePrefix";
	public final static String	DATA_FILE_SUFFIX              = "dataFileSuffix";
	public final static String	TRANSACTION_FILE_PREFIX       = "transactionFilePrefix";
	public final static String	TRANSACTION_FILE_SUFFIX	      = "transactionFileSuffix";
	public final static String	TYPE_DICTIONARY_FILENAME      = "typeDictionaryFilename";
	public final static String	TYPE_ID_FILENAME              = "typeIdFilename";
	public final static String	OBJECT_ID_FILENAME            = "objectIdFilename";
	public final static String	HOUSEKEEPING_INTERVAL         = "houseKeepingInterval";
	public final static String	HOUSEKEEPING_NANO_TIME_BUDGET = "houseKeepingNanoTimeBudget";
	public final static String	ENTITY_CACHE_THRESHOLD        = "entityCacheThreshold";
	public final static String	ENTITY_CACHE_TIMEOUT          = "entityCacheTimeout";
	public final static String	DATA_FILE_MIN_SIZE            = "dataFileMinSize";
	public final static String	DATA_FILE_MAX_SIZE            = "dataFileMaxSize";
	public final static String	DATA_FILE_DISSOLVE_RATIO      = "dataFileDissolveRatio";
}

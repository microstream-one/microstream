package one.microstream.storage.exceptions;

import one.microstream.collections.types.XGettingTable;
import one.microstream.storage.types.StorageBackupFile;

public class StorageExceptionBackupEmptyStorageForNonEmptyBackup
extends StorageExceptionBackupChannelIndex
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////
	
	private final XGettingTable<Long, StorageBackupFile> backupFiles;

	

	///////////////////////////////////////////////////////////////////////////
	// constructors     //
	/////////////////////

	public StorageExceptionBackupEmptyStorageForNonEmptyBackup(
		final long                                   channelIndex,
		final XGettingTable<Long, StorageBackupFile> backupFiles
	)
	{
		super(channelIndex);
		this.backupFiles = backupFiles;
	}

	public StorageExceptionBackupEmptyStorageForNonEmptyBackup(
		final long                                   channelIndex,
		final XGettingTable<Long, StorageBackupFile> backupFiles ,
		final String                                 message
	)
	{
		super(channelIndex, message);
		this.backupFiles = backupFiles;
	}

	public StorageExceptionBackupEmptyStorageForNonEmptyBackup(
		final long                                   channelIndex,
		final XGettingTable<Long, StorageBackupFile> backupFiles ,
		final Throwable                              cause
	)
	{
		super(channelIndex, cause);
		this.backupFiles = backupFiles;
	}

	public StorageExceptionBackupEmptyStorageForNonEmptyBackup(
		final long                                   channelIndex,
		final XGettingTable<Long, StorageBackupFile> backupFiles ,
		final String                                 message     ,
		final Throwable                              cause
	)
	{
		super(channelIndex, message, cause);
		this.backupFiles = backupFiles;
	}

	public StorageExceptionBackupEmptyStorageForNonEmptyBackup(
		final long                                   channelIndex      ,
		final XGettingTable<Long, StorageBackupFile> backupFiles       ,
		final String                                 message           ,
		final Throwable                              cause             ,
		final boolean                                enableSuppression ,
		final boolean                                writableStackTrace
	)
	{
		super(channelIndex, message, cause, enableSuppression, writableStackTrace);
		this.backupFiles = backupFiles;
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////
	
	public final XGettingTable<Long, StorageBackupFile> backupFiles()
	{
		return this.backupFiles;
	}
	
}

package one.microstream.storage.exceptions;

import one.microstream.collections.types.XGettingTable;
import one.microstream.storage.types.StorageBackupDataFile;
import one.microstream.storage.types.StorageInventory;

public class StorageExceptionBackupEmptyStorageBackupAhead
extends StorageExceptionBackupChannelIndex
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////
	
	private final StorageInventory                          storageInventory;
	private final XGettingTable<Long, StorageBackupDataFile> backupFiles     ;

	

	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public StorageExceptionBackupEmptyStorageBackupAhead(
		final StorageInventory                           storageInventory,
		final XGettingTable<Long, StorageBackupDataFile> backupFiles
	)
	{
		super(storageInventory.channelIndex());
		this.storageInventory = storageInventory;
		this.backupFiles      = backupFiles     ;
	}

	public StorageExceptionBackupEmptyStorageBackupAhead(
		final StorageInventory                           storageInventory,
		final XGettingTable<Long, StorageBackupDataFile> backupFiles     ,
		final String                                     message
	)
	{
		super(storageInventory.channelIndex(), message);
		this.storageInventory = storageInventory;
		this.backupFiles      = backupFiles     ;
	}

	public StorageExceptionBackupEmptyStorageBackupAhead(
		final StorageInventory                           storageInventory,
		final XGettingTable<Long, StorageBackupDataFile> backupFiles     ,
		final Throwable                                  cause
	)
	{
		super(storageInventory.channelIndex(), cause);
		this.storageInventory = storageInventory;
		this.backupFiles      = backupFiles     ;
	}

	public StorageExceptionBackupEmptyStorageBackupAhead(
		final StorageInventory                           storageInventory,
		final XGettingTable<Long, StorageBackupDataFile> backupFiles     ,
		final String                                     message         ,
		final Throwable                                  cause
	)
	{
		super(storageInventory.channelIndex(), message, cause);
		this.storageInventory = storageInventory;
		this.backupFiles      = backupFiles     ;
	}

	public StorageExceptionBackupEmptyStorageBackupAhead(
		final StorageInventory                           storageInventory  ,
		final XGettingTable<Long, StorageBackupDataFile> backupFiles       ,
		final String                                     message           ,
		final Throwable                                  cause             ,
		final boolean                                    enableSuppression ,
		final boolean                                    writableStackTrace
	)
	{
		super(storageInventory.channelIndex(), message, cause, enableSuppression, writableStackTrace);
		this.storageInventory = storageInventory;
		this.backupFiles      = backupFiles     ;
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////
	
	public final StorageInventory storageInventory()
	{
		return this.storageInventory;
	}
	
	public final XGettingTable<Long, StorageBackupDataFile> backupFiles()
	{
		return this.backupFiles;
	}
	
}

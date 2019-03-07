package net.jadoth.storage.exceptions;

import net.jadoth.collections.types.XGettingTable;
import net.jadoth.storage.types.StorageBackupFile;
import net.jadoth.storage.types.StorageInventory;

public class StorageExceptionBackupEmptyStorageBackupAhead
extends StorageExceptionBackupChannelIndex
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////
	
	private final StorageInventory                       storageInventory;
	private final XGettingTable<Long, StorageBackupFile> backupFiles     ;

	

	///////////////////////////////////////////////////////////////////////////
	// constructors     //
	/////////////////////

	public StorageExceptionBackupEmptyStorageBackupAhead(
		final StorageInventory                       storageInventory,
		final XGettingTable<Long, StorageBackupFile> backupFiles
	)
	{
		super(storageInventory.channelIndex());
		this.storageInventory = storageInventory;
		this.backupFiles      = backupFiles     ;
	}

	public StorageExceptionBackupEmptyStorageBackupAhead(
		final StorageInventory                       storageInventory,
		final XGettingTable<Long, StorageBackupFile> backupFiles     ,
		final String                                 message
	)
	{
		super(storageInventory.channelIndex(), message);
		this.storageInventory = storageInventory;
		this.backupFiles      = backupFiles     ;
	}

	public StorageExceptionBackupEmptyStorageBackupAhead(
		final StorageInventory                       storageInventory,
		final XGettingTable<Long, StorageBackupFile> backupFiles     ,
		final Throwable                              cause
	)
	{
		super(storageInventory.channelIndex(), cause);
		this.storageInventory = storageInventory;
		this.backupFiles      = backupFiles     ;
	}

	public StorageExceptionBackupEmptyStorageBackupAhead(
		final StorageInventory                       storageInventory,
		final XGettingTable<Long, StorageBackupFile> backupFiles     ,
		final String                                 message         ,
		final Throwable                              cause
	)
	{
		super(storageInventory.channelIndex(), message, cause);
		this.storageInventory = storageInventory;
		this.backupFiles      = backupFiles     ;
	}

	public StorageExceptionBackupEmptyStorageBackupAhead(
		final StorageInventory                       storageInventory  ,
		final XGettingTable<Long, StorageBackupFile> backupFiles       ,
		final String                                 message           ,
		final Throwable                              cause             ,
		final boolean                                enableSuppression ,
		final boolean                                writableStackTrace
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
	
	public final XGettingTable<Long, StorageBackupFile> backupFiles()
	{
		return this.backupFiles;
	}
	
}

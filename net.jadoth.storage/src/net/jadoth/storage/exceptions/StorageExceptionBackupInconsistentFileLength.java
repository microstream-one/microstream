package net.jadoth.storage.exceptions;

import net.jadoth.collections.types.XGettingTable;
import net.jadoth.storage.types.StorageBackupFile;
import net.jadoth.storage.types.StorageInventory;
import net.jadoth.storage.types.StorageInventoryFile;

public class StorageExceptionBackupInconsistentFileLength
extends StorageExceptionBackupChannelIndex
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////
	
	private final StorageInventory                       storageInventory;
	private final XGettingTable<Long, StorageBackupFile> backupFiles     ;
	private final StorageInventoryFile                   storageFile     ;
	private final StorageBackupFile                      backupFile      ;

	

	///////////////////////////////////////////////////////////////////////////
	// constructors     //
	/////////////////////

	public StorageExceptionBackupInconsistentFileLength(
		final StorageInventory                       storageInventory,
		final XGettingTable<Long, StorageBackupFile> backupFiles     ,
		final StorageInventoryFile                   storageFile     ,
		final StorageBackupFile                      backupFile
	)
	{
		super(storageInventory.channelIndex());
		this.storageInventory = storageInventory;
		this.backupFiles      = backupFiles     ;
		this.storageFile      = storageFile     ;
		this.backupFile       = backupFile      ;
	}

	public StorageExceptionBackupInconsistentFileLength(
		final StorageInventory                       storageInventory,
		final XGettingTable<Long, StorageBackupFile> backupFiles     ,
		final StorageInventoryFile                   storageFile     ,
		final StorageBackupFile                      backupFile      ,
		final String                                 message
	)
	{
		super(storageInventory.channelIndex(), message);
		this.storageInventory = storageInventory;
		this.backupFiles      = backupFiles     ;
		this.storageFile      = storageFile     ;
		this.backupFile       = backupFile      ;
	}

	public StorageExceptionBackupInconsistentFileLength(
		final StorageInventory                       storageInventory,
		final XGettingTable<Long, StorageBackupFile> backupFiles     ,
		final StorageInventoryFile                   storageFile     ,
		final StorageBackupFile                      backupFile      ,
		final Throwable                              cause
	)
	{
		super(storageInventory.channelIndex(), cause);
		this.storageInventory = storageInventory;
		this.backupFiles      = backupFiles     ;
		this.storageFile      = storageFile     ;
		this.backupFile       = backupFile      ;
	}

	public StorageExceptionBackupInconsistentFileLength(
		final StorageInventory                       storageInventory,
		final XGettingTable<Long, StorageBackupFile> backupFiles     ,
		final StorageInventoryFile                   storageFile     ,
		final StorageBackupFile                      backupFile      ,
		final String                                 message         ,
		final Throwable                              cause
	)
	{
		super(storageInventory.channelIndex(), message, cause);
		this.storageInventory = storageInventory;
		this.backupFiles      = backupFiles     ;
		this.storageFile      = storageFile     ;
		this.backupFile       = backupFile      ;
	}

	public StorageExceptionBackupInconsistentFileLength(
		final StorageInventory                       storageInventory  ,
		final XGettingTable<Long, StorageBackupFile> backupFiles       ,
		final StorageInventoryFile                   storageFile       ,
		final StorageBackupFile                      backupFile        ,
		final String                                 message           ,
		final Throwable                              cause             ,
		final boolean                                enableSuppression ,
		final boolean                                writableStackTrace
	)
	{
		super(storageInventory.channelIndex(), message, cause, enableSuppression, writableStackTrace);
		this.storageInventory = storageInventory;
		this.backupFiles      = backupFiles     ;
		this.storageFile      = storageFile     ;
		this.backupFile       = backupFile      ;
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
	
	public final StorageInventoryFile storageFile()
	{
		return this.storageFile;
	}
	
	public final StorageBackupFile backupFile()
	{
		return this.backupFile;
	}
	
}

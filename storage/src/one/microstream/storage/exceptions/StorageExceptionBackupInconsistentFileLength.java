package one.microstream.storage.exceptions;

import one.microstream.chars.VarString;
import one.microstream.collections.types.XGettingTable;
import one.microstream.storage.types.ZStorageBackupFile;
import one.microstream.storage.types.StorageInventory;
import one.microstream.storage.types.ZStorageInventoryFile;

public class StorageExceptionBackupInconsistentFileLength
extends StorageExceptionBackupChannelIndex
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////
	
	private final StorageInventory                       storageInventory ;
	private final XGettingTable<Long, ZStorageBackupFile> backupFiles      ;
	private final ZStorageInventoryFile                   storageFile      ;
	private final long                                   storageFileLength;
	private final ZStorageBackupFile                      backupFile       ;
	private final long                                   backupFileLength ;

	

	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public StorageExceptionBackupInconsistentFileLength(
		final StorageInventory                       storageInventory ,
		final XGettingTable<Long, ZStorageBackupFile> backupFiles      ,
		final ZStorageInventoryFile                   storageFile      ,
		final long                                   storageFileLength,
		final ZStorageBackupFile                      backupFile       ,
		final long                                   backupFileLength
	)
	{
		super(storageInventory.channelIndex());
		this.storageInventory  = storageInventory ;
		this.backupFiles       = backupFiles      ;
		this.storageFile       = storageFile      ;
		this.storageFileLength = storageFileLength;
		this.backupFile        = backupFile       ;
		this.backupFileLength  = backupFileLength ;
	}

	public StorageExceptionBackupInconsistentFileLength(
		final StorageInventory                       storageInventory ,
		final XGettingTable<Long, ZStorageBackupFile> backupFiles      ,
		final ZStorageInventoryFile                   storageFile      ,
		final long                                   storageFileLength,
		final ZStorageBackupFile                      backupFile       ,
		final long                                   backupFileLength ,
		final String                                 message
	)
	{
		super(storageInventory.channelIndex(), message);
		this.storageInventory  = storageInventory ;
		this.backupFiles       = backupFiles      ;
		this.storageFile       = storageFile      ;
		this.storageFileLength = storageFileLength;
		this.backupFile        = backupFile       ;
		this.backupFileLength  = backupFileLength ;
	}

	public StorageExceptionBackupInconsistentFileLength(
		final StorageInventory                       storageInventory ,
		final XGettingTable<Long, ZStorageBackupFile> backupFiles      ,
		final ZStorageInventoryFile                   storageFile      ,
		final long                                   storageFileLength,
		final ZStorageBackupFile                      backupFile       ,
		final long                                   backupFileLength ,
		final Throwable                              cause
	)
	{
		super(storageInventory.channelIndex(), cause);
		this.storageInventory  = storageInventory ;
		this.backupFiles       = backupFiles      ;
		this.storageFile       = storageFile      ;
		this.storageFileLength = storageFileLength;
		this.backupFile        = backupFile       ;
		this.backupFileLength  = backupFileLength ;
	}

	public StorageExceptionBackupInconsistentFileLength(
		final StorageInventory                       storageInventory ,
		final XGettingTable<Long, ZStorageBackupFile> backupFiles      ,
		final ZStorageInventoryFile                   storageFile      ,
		final long                                   storageFileLength,
		final ZStorageBackupFile                      backupFile       ,
		final long                                   backupFileLength ,
		final String                                 message         ,
		final Throwable                              cause
	)
	{
		super(storageInventory.channelIndex(), message, cause);
		this.storageInventory  = storageInventory ;
		this.backupFiles       = backupFiles      ;
		this.storageFile       = storageFile      ;
		this.storageFileLength = storageFileLength;
		this.backupFile        = backupFile       ;
		this.backupFileLength  = backupFileLength ;
	}

	public StorageExceptionBackupInconsistentFileLength(
		final StorageInventory                       storageInventory ,
		final XGettingTable<Long, ZStorageBackupFile> backupFiles      ,
		final ZStorageInventoryFile                   storageFile      ,
		final long                                   storageFileLength,
		final ZStorageBackupFile                      backupFile       ,
		final long                                   backupFileLength ,
		final String                                 message           ,
		final Throwable                              cause             ,
		final boolean                                enableSuppression ,
		final boolean                                writableStackTrace
	)
	{
		super(storageInventory.channelIndex(), message, cause, enableSuppression, writableStackTrace);
		this.storageInventory  = storageInventory ;
		this.backupFiles       = backupFiles      ;
		this.storageFile       = storageFile      ;
		this.storageFileLength = storageFileLength;
		this.backupFile        = backupFile       ;
		this.backupFileLength  = backupFileLength ;
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////
	
	public final StorageInventory storageInventory()
	{
		return this.storageInventory;
	}
	
	public final XGettingTable<Long, ZStorageBackupFile> backupFiles()
	{
		return this.backupFiles;
	}
	
	public final ZStorageInventoryFile storageFile()
	{
		return this.storageFile;
	}
	
	public final ZStorageBackupFile backupFile()
	{
		return this.backupFile;
	}
	
	@Override
	public String assembleDetailString()
	{
		return VarString.New()
			.add(this.storageFile.identifier()).add('[').add(this.storageFileLength).add(']')
			.add(" <-> ")
			.add(this.backupFile.identifier()).add('[').add(this.backupFileLength).add(']')
			.toString()
		;
	}
	
}

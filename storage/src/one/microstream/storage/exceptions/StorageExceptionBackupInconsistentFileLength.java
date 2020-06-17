package one.microstream.storage.exceptions;

import one.microstream.chars.VarString;
import one.microstream.collections.types.XGettingTable;
import one.microstream.storage.types.StorageBackupDataFile;
import one.microstream.storage.types.StorageInventory;
import one.microstream.storage.types.StorageLiveDataFile;

public class StorageExceptionBackupInconsistentFileLength
extends StorageExceptionBackupChannelIndex
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////
	
	private final StorageInventory                           storageInventory ;
	private final XGettingTable<Long, StorageBackupDataFile> backupDataFiles  ;
	private final StorageLiveDataFile                        liveDataFile     ;
	private final long                                       storageFileLength;
	private final StorageBackupDataFile                      backupDataFile   ;
	private final long                                       backupFileLength ;

	

	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public StorageExceptionBackupInconsistentFileLength(
		final StorageInventory                           storageInventory ,
		final XGettingTable<Long, StorageBackupDataFile> backupDataFiles  ,
		final StorageLiveDataFile                        liveDataFile     ,
		final long                                       storageFileLength,
		final StorageBackupDataFile                      backupDataFile   ,
		final long                                       backupFileLength
	)
	{
		super(storageInventory.channelIndex());
		this.storageInventory  = storageInventory ;
		this.backupDataFiles   = backupDataFiles  ;
		this.liveDataFile      = liveDataFile     ;
		this.storageFileLength = storageFileLength;
		this.backupDataFile    = backupDataFile   ;
		this.backupFileLength  = backupFileLength ;
	}

	public StorageExceptionBackupInconsistentFileLength(
		final StorageInventory                           storageInventory ,
		final XGettingTable<Long, StorageBackupDataFile> backupDataFiles  ,
		final StorageLiveDataFile                        liveDataFile     ,
		final long                                       storageFileLength,
		final StorageBackupDataFile                      backupDataFile   ,
		final long                                       backupFileLength ,
		final String                                     message
	)
	{
		super(storageInventory.channelIndex(), message);
		this.storageInventory  = storageInventory ;
		this.backupDataFiles   = backupDataFiles  ;
		this.liveDataFile      = liveDataFile     ;
		this.storageFileLength = storageFileLength;
		this.backupDataFile    = backupDataFile   ;
		this.backupFileLength  = backupFileLength ;
	}

	public StorageExceptionBackupInconsistentFileLength(
		final StorageInventory                           storageInventory ,
		final XGettingTable<Long, StorageBackupDataFile> backupDataFiles  ,
		final StorageLiveDataFile                        liveDataFile     ,
		final long                                       storageFileLength,
		final StorageBackupDataFile                      backupDataFile   ,
		final long                                       backupFileLength ,
		final Throwable                                  cause
	)
	{
		super(storageInventory.channelIndex(), cause);
		this.storageInventory  = storageInventory ;
		this.backupDataFiles   = backupDataFiles  ;
		this.liveDataFile      = liveDataFile     ;
		this.storageFileLength = storageFileLength;
		this.backupDataFile    = backupDataFile   ;
		this.backupFileLength  = backupFileLength ;
	}

	public StorageExceptionBackupInconsistentFileLength(
		final StorageInventory                           storageInventory ,
		final XGettingTable<Long, StorageBackupDataFile> backupDataFiles  ,
		final StorageLiveDataFile                        liveDataFile     ,
		final long                                       storageFileLength,
		final StorageBackupDataFile                      backupDataFile   ,
		final long                                       backupFileLength ,
		final String                                     message          ,
		final Throwable                                  cause
	)
	{
		super(storageInventory.channelIndex(), message, cause);
		this.storageInventory  = storageInventory ;
		this.backupDataFiles   = backupDataFiles  ;
		this.liveDataFile      = liveDataFile     ;
		this.storageFileLength = storageFileLength;
		this.backupDataFile    = backupDataFile   ;
		this.backupFileLength  = backupFileLength ;
	}

	public StorageExceptionBackupInconsistentFileLength(
		final StorageInventory                           storageInventory  ,
		final XGettingTable<Long, StorageBackupDataFile> backupDataFiles   ,
		final StorageLiveDataFile                        liveDataFile      ,
		final long                                       storageFileLength ,
		final StorageBackupDataFile                      backupDataFile    ,
		final long                                       backupFileLength  ,
		final String                                     message           ,
		final Throwable                                  cause             ,
		final boolean                                    enableSuppression ,
		final boolean                                    writableStackTrace
	)
	{
		super(storageInventory.channelIndex(), message, cause, enableSuppression, writableStackTrace);
		this.storageInventory  = storageInventory ;
		this.backupDataFiles   = backupDataFiles  ;
		this.liveDataFile      = liveDataFile     ;
		this.storageFileLength = storageFileLength;
		this.backupDataFile    = backupDataFile   ;
		this.backupFileLength  = backupFileLength ;
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
		return this.backupDataFiles;
	}
	
	public final StorageLiveDataFile storageFile()
	{
		return this.liveDataFile;
	}
	
	public final StorageBackupDataFile backupFile()
	{
		return this.backupDataFile;
	}
	
	@Override
	public String assembleDetailString()
	{
		return VarString.New()
			.add(this.liveDataFile.identifier()).add('[').add(this.storageFileLength).add(']')
			.add(" <-> ")
			.add(this.backupDataFile.identifier()).add('[').add(this.backupFileLength).add(']')
			.toString()
		;
	}
	
}

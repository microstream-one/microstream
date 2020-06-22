package one.microstream.storage.exceptions;

import one.microstream.chars.VarString;
import one.microstream.collections.types.XGettingTable;
import one.microstream.storage.types.StorageBackupDataFile;
import one.microstream.storage.types.StorageDataFile;
import one.microstream.storage.types.StorageInventory;

public class StorageExceptionBackupInconsistentFileLength
extends StorageExceptionBackupChannelIndex
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////
	
	private final StorageInventory                           storageInventory ;
	private final XGettingTable<Long, StorageBackupDataFile> backupDataFiles  ;
	private final StorageDataFile                            dataFile         ;
	private final long                                       storageFileLength;
	private final StorageBackupDataFile                      backupDataFile   ;
	private final long                                       backupFileLength ;

	

	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public StorageExceptionBackupInconsistentFileLength(
		final StorageInventory                           storageInventory ,
		final XGettingTable<Long, StorageBackupDataFile> backupDataFiles  ,
		final StorageDataFile                            dataFile         ,
		final long                                       storageFileLength,
		final StorageBackupDataFile                      backupDataFile   ,
		final long                                       backupFileLength
	)
	{
		super(storageInventory.channelIndex());
		this.storageInventory  = storageInventory ;
		this.backupDataFiles   = backupDataFiles  ;
		this.dataFile          = dataFile         ;
		this.storageFileLength = storageFileLength;
		this.backupDataFile    = backupDataFile   ;
		this.backupFileLength  = backupFileLength ;
	}

	public StorageExceptionBackupInconsistentFileLength(
		final StorageInventory                           storageInventory ,
		final XGettingTable<Long, StorageBackupDataFile> backupDataFiles  ,
		final StorageDataFile                            dataFile         ,
		final long                                       storageFileLength,
		final StorageBackupDataFile                      backupDataFile   ,
		final long                                       backupFileLength ,
		final String                                     message
	)
	{
		super(storageInventory.channelIndex(), message);
		this.storageInventory  = storageInventory ;
		this.backupDataFiles   = backupDataFiles  ;
		this.dataFile          = dataFile         ;
		this.storageFileLength = storageFileLength;
		this.backupDataFile    = backupDataFile   ;
		this.backupFileLength  = backupFileLength ;
	}

	public StorageExceptionBackupInconsistentFileLength(
		final StorageInventory                           storageInventory ,
		final XGettingTable<Long, StorageBackupDataFile> backupDataFiles  ,
		final StorageDataFile                            dataFile         ,
		final long                                       storageFileLength,
		final StorageBackupDataFile                      backupDataFile   ,
		final long                                       backupFileLength ,
		final Throwable                                  cause
	)
	{
		super(storageInventory.channelIndex(), cause);
		this.storageInventory  = storageInventory ;
		this.backupDataFiles   = backupDataFiles  ;
		this.dataFile          = dataFile         ;
		this.storageFileLength = storageFileLength;
		this.backupDataFile    = backupDataFile   ;
		this.backupFileLength  = backupFileLength ;
	}

	public StorageExceptionBackupInconsistentFileLength(
		final StorageInventory                           storageInventory ,
		final XGettingTable<Long, StorageBackupDataFile> backupDataFiles  ,
		final StorageDataFile                            dataFile         ,
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
		this.dataFile          = dataFile         ;
		this.storageFileLength = storageFileLength;
		this.backupDataFile    = backupDataFile   ;
		this.backupFileLength  = backupFileLength ;
	}

	public StorageExceptionBackupInconsistentFileLength(
		final StorageInventory                           storageInventory  ,
		final XGettingTable<Long, StorageBackupDataFile> backupDataFiles   ,
		final StorageDataFile                            dataFile          ,
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
		this.dataFile          = dataFile         ;
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
	
	public final StorageDataFile dataFile()
	{
		return this.dataFile;
	}
	
	public final StorageBackupDataFile backupFile()
	{
		return this.backupDataFile;
	}
	
	@Override
	public String assembleDetailString()
	{
		return VarString.New()
			.add(this.dataFile.identifier()).add('[').add(this.storageFileLength).add(']')
			.add(" <-> ")
			.add(this.backupDataFile.identifier()).add('[').add(this.backupFileLength).add(']')
			.toString()
		;
	}
	
}

package one.microstream.storage.exceptions;

import one.microstream.chars.VarString;
import one.microstream.storage.types.ZStorageBackupFile;
import one.microstream.storage.types.ZStorageInventoryFile;

public class StorageExceptionBackupCopying
extends StorageExceptionBackupChannelIndex
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////
	
	private final ZStorageInventoryFile storageFile   ;
	private final long                 sourcePosition;
	private final long                 length        ;
	private final ZStorageBackupFile    backupFile    ;

	

	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public StorageExceptionBackupCopying(
		final ZStorageInventoryFile storageFile   ,
		final long                 sourcePosition,
		final long                 length        ,
		final ZStorageBackupFile    backupFile
	)
	{
		super(storageFile.channelIndex());
		this.storageFile    = storageFile   ;
		this.sourcePosition = sourcePosition;
		this.length         = length        ;
		this.backupFile     = backupFile    ;
	}

	public StorageExceptionBackupCopying(
		final ZStorageInventoryFile storageFile   ,
		final long                 sourcePosition,
		final long                 length        ,
		final ZStorageBackupFile    backupFile    ,
		final String               message
	)
	{
		super(storageFile.channelIndex(), message);
		this.storageFile    = storageFile   ;
		this.sourcePosition = sourcePosition;
		this.length         = length        ;
		this.backupFile     = backupFile    ;
	}

	public StorageExceptionBackupCopying(
		final ZStorageInventoryFile storageFile   ,
		final long                 sourcePosition,
		final long                 length        ,
		final ZStorageBackupFile    backupFile    ,
		final Throwable            cause
	)
	{
		super(storageFile.channelIndex(), cause);
		this.storageFile    = storageFile   ;
		this.sourcePosition = sourcePosition;
		this.length         = length        ;
		this.backupFile     = backupFile    ;
	}

	public StorageExceptionBackupCopying(
		final ZStorageInventoryFile storageFile   ,
		final long                 sourcePosition,
		final long                 length        ,
		final ZStorageBackupFile    backupFile    ,
		final String               message       ,
		final Throwable            cause
	)
	{
		super(storageFile.channelIndex(), message, cause);
		this.storageFile    = storageFile   ;
		this.sourcePosition = sourcePosition;
		this.length         = length        ;
		this.backupFile     = backupFile    ;
	}

	public StorageExceptionBackupCopying(
		final ZStorageInventoryFile storageFile       ,
		final long                 sourcePosition    ,
		final long                 length            ,
		final ZStorageBackupFile    backupFile        ,
		final String               message           ,
		final Throwable            cause             ,
		final boolean              enableSuppression ,
		final boolean              writableStackTrace
	)
	{
		super(storageFile.channelIndex(), message, cause, enableSuppression, writableStackTrace);
		this.storageFile    = storageFile   ;
		this.sourcePosition = sourcePosition;
		this.length         = length        ;
		this.backupFile     = backupFile    ;
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////
	
	public final ZStorageInventoryFile storageFile()
	{
		return this.storageFile;
	}
	
	public final long sourcePosition()
	{
		return this.sourcePosition;
	}
	
	public final long length()
	{
		return this.length;
	}
	
	public final ZStorageBackupFile backupFile()
	{
		return this.backupFile;
	}
	
	@Override
	public String assembleDetailString()
	{
		return VarString.New()
			.add(this.storageFile.identifier()).add('@').add(this.sourcePosition).add('+').add(this.length)
			.add(" -> ")
			.add(this.backupFile.identifier())
			.toString()
		;
	}
	
}

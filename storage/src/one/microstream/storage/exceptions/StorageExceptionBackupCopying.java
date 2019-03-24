package one.microstream.storage.exceptions;

import one.microstream.chars.VarString;
import one.microstream.storage.types.StorageBackupFile;
import one.microstream.storage.types.StorageInventoryFile;

public class StorageExceptionBackupCopying
extends StorageExceptionBackupChannelIndex
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////
	
	private final StorageInventoryFile storageFile   ;
	private final long                 sourcePosition;
	private final long                 length        ;
	private final StorageBackupFile    backupFile    ;

	

	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public StorageExceptionBackupCopying(
		final StorageInventoryFile storageFile   ,
		final long                 sourcePosition,
		final long                 length        ,
		final StorageBackupFile    backupFile
	)
	{
		super(storageFile.channelIndex());
		this.storageFile    = storageFile   ;
		this.sourcePosition = sourcePosition;
		this.length         = length        ;
		this.backupFile     = backupFile    ;
	}

	public StorageExceptionBackupCopying(
		final StorageInventoryFile storageFile   ,
		final long                 sourcePosition,
		final long                 length        ,
		final StorageBackupFile    backupFile    ,
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
		final StorageInventoryFile storageFile   ,
		final long                 sourcePosition,
		final long                 length        ,
		final StorageBackupFile    backupFile    ,
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
		final StorageInventoryFile storageFile   ,
		final long                 sourcePosition,
		final long                 length        ,
		final StorageBackupFile    backupFile    ,
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
		final StorageInventoryFile storageFile       ,
		final long                 sourcePosition    ,
		final long                 length            ,
		final StorageBackupFile    backupFile        ,
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
	
	public final StorageInventoryFile storageFile()
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
	
	public final StorageBackupFile backupFile()
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

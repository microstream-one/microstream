package one.microstream.storage.exceptions;

import one.microstream.chars.VarString;
import one.microstream.storage.types.StorageBackupFile;
import one.microstream.storage.types.StorageLiveFile;

public class StorageExceptionBackupCopying
extends StorageExceptionBackup
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////
	
	private final StorageLiveFile<?> liveDataFile   ;
	private final long               sourcePosition;
	private final long               length        ;
	private final StorageBackupFile  backupFile    ;

	

	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public StorageExceptionBackupCopying(
		final StorageLiveFile<?> storageFile   ,
		final long               sourcePosition,
		final long               length        ,
		final StorageBackupFile  backupFile
	)
	{
		super();
		this.liveDataFile    = storageFile   ;
		this.sourcePosition = sourcePosition;
		this.length         = length        ;
		this.backupFile     = backupFile    ;
	}

	public StorageExceptionBackupCopying(
		final StorageLiveFile<?> storageFile   ,
		final long               sourcePosition,
		final long               length        ,
		final StorageBackupFile  backupFile    ,
		final String             message
	)
	{
		super(message);
		this.liveDataFile    = storageFile   ;
		this.sourcePosition = sourcePosition;
		this.length         = length        ;
		this.backupFile     = backupFile    ;
	}

	public StorageExceptionBackupCopying(
		final StorageLiveFile<?> storageFile   ,
		final long               sourcePosition,
		final long               length        ,
		final StorageBackupFile  backupFile    ,
		final Throwable          cause
	)
	{
		super(cause);
		this.liveDataFile    = storageFile   ;
		this.sourcePosition = sourcePosition;
		this.length         = length        ;
		this.backupFile     = backupFile    ;
	}

	public StorageExceptionBackupCopying(
		final StorageLiveFile<?> storageFile   ,
		final long               sourcePosition,
		final long               length        ,
		final StorageBackupFile  backupFile    ,
		final String             message       ,
		final Throwable          cause
	)
	{
		super(message, cause);
		this.liveDataFile    = storageFile   ;
		this.sourcePosition = sourcePosition;
		this.length         = length        ;
		this.backupFile     = backupFile    ;
	}

	public StorageExceptionBackupCopying(
		final StorageLiveFile<?> storageFile       ,
		final long               sourcePosition    ,
		final long               length            ,
		final StorageBackupFile  backupFile        ,
		final String             message           ,
		final Throwable          cause             ,
		final boolean            enableSuppression ,
		final boolean            writableStackTrace
	)
	{
		super(message, cause, enableSuppression, writableStackTrace);
		this.liveDataFile   = storageFile   ;
		this.sourcePosition = sourcePosition;
		this.length         = length        ;
		this.backupFile     = backupFile    ;
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////
	
	public final StorageLiveFile<?> liveDataFile()
	{
		return this.liveDataFile;
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
			.add(this.liveDataFile.identifier()).add('@').add(this.sourcePosition).add('+').add(this.length)
			.add(" -> ")
			.add(this.backupFile.identifier())
			.toString()
		;
	}
	
}

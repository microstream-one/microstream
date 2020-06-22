package one.microstream.storage.exceptions;

import one.microstream.chars.VarString;
import one.microstream.storage.types.StorageBackupFile;
import one.microstream.storage.types.StorageChannelFile;

public class StorageExceptionBackupCopying
extends StorageExceptionBackup
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////
	
	private final StorageChannelFile sourceFile    ;
	private final long               sourcePosition;
	private final long               length        ;
	private final StorageBackupFile  backupFile    ;

	

	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public StorageExceptionBackupCopying(
		final StorageChannelFile sourceFile    ,
		final long               sourcePosition,
		final long               length        ,
		final StorageBackupFile  backupFile
	)
	{
		super();
		this.sourceFile     = sourceFile    ;
		this.sourcePosition = sourcePosition;
		this.length         = length        ;
		this.backupFile     = backupFile    ;
	}

	public StorageExceptionBackupCopying(
		final StorageChannelFile sourceFile   ,
		final long               sourcePosition,
		final long               length        ,
		final StorageBackupFile  backupFile    ,
		final String             message
	)
	{
		super(message);
		this.sourceFile     = sourceFile    ;
		this.sourcePosition = sourcePosition;
		this.length         = length        ;
		this.backupFile     = backupFile    ;
	}

	public StorageExceptionBackupCopying(
		final StorageChannelFile sourceFile   ,
		final long               sourcePosition,
		final long               length        ,
		final StorageBackupFile  backupFile    ,
		final Throwable          cause
	)
	{
		super(cause);
		this.sourceFile     = sourceFile    ;
		this.sourcePosition = sourcePosition;
		this.length         = length        ;
		this.backupFile     = backupFile    ;
	}

	public StorageExceptionBackupCopying(
		final StorageChannelFile sourceFile    ,
		final long               sourcePosition,
		final long               length        ,
		final StorageBackupFile  backupFile    ,
		final String             message       ,
		final Throwable          cause
	)
	{
		super(message, cause);
		this.sourceFile     = sourceFile    ;
		this.sourcePosition = sourcePosition;
		this.length         = length        ;
		this.backupFile     = backupFile    ;
	}

	public StorageExceptionBackupCopying(
		final StorageChannelFile sourceFile        ,
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
		this.sourceFile     = sourceFile    ;
		this.sourcePosition = sourcePosition;
		this.length         = length        ;
		this.backupFile     = backupFile    ;
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////
	
	public final StorageChannelFile sourceFile()
	{
		return this.sourceFile;
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
			.add(this.sourceFile.identifier()).add('@').add(this.sourcePosition).add('+').add(this.length)
			.add(" -> ")
			.add(this.backupFile.identifier())
			.toString()
		;
	}
	
}

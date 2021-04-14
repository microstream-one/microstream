package one.microstream.storage.exceptions;

public class StorageExceptionBackupDisabled extends StorageExceptionBackup
{
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public StorageExceptionBackupDisabled()
	{
		super();
	}

	public StorageExceptionBackupDisabled(final String message)
	{
		super(message);
	}

	public StorageExceptionBackupDisabled(final Throwable cause)
	{
		super(cause);
	}

	public StorageExceptionBackupDisabled(final String message, final Throwable cause)
	{
		super(message, cause);
	}

	public StorageExceptionBackupDisabled(
		final String    message           ,
		final Throwable cause             ,
		final boolean   enableSuppression ,
		final boolean   writableStackTrace
	)
	{
		super(message, cause, enableSuppression, writableStackTrace);
	}
	
}

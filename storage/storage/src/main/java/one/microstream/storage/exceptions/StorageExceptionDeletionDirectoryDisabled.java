package one.microstream.storage.exceptions;

public class StorageExceptionDeletionDirectoryDisabled extends StorageExceptionBackup
{
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public StorageExceptionDeletionDirectoryDisabled()
	{
		super();
	}

	public StorageExceptionDeletionDirectoryDisabled(final String message)
	{
		super(message);
	}

	public StorageExceptionDeletionDirectoryDisabled(final Throwable cause)
	{
		super(cause);
	}

	public StorageExceptionDeletionDirectoryDisabled(final String message, final Throwable cause)
	{
		super(message, cause);
	}

	public StorageExceptionDeletionDirectoryDisabled(
		final String    message           ,
		final Throwable cause             ,
		final boolean   enableSuppression ,
		final boolean   writableStackTrace
	)
	{
		super(message, cause, enableSuppression, writableStackTrace);
	}
	
}

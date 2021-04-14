package one.microstream.storage.exceptions;

public class StorageExceptionBackup extends StorageException
{
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public StorageExceptionBackup()
	{
		super();
	}

	public StorageExceptionBackup(final String message)
	{
		super(message);
	}

	public StorageExceptionBackup(final Throwable cause)
	{
		super(cause);
	}

	public StorageExceptionBackup(final String message, final Throwable cause)
	{
		super(message, cause);
	}

	public StorageExceptionBackup(
		final String    message           ,
		final Throwable cause             ,
		final boolean   enableSuppression ,
		final boolean   writableStackTrace
	)
	{
		super(message, cause, enableSuppression, writableStackTrace);
	}
	
}

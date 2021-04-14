package one.microstream.storage.exceptions;

public class StorageExceptionFileDeletionDisabled extends StorageExceptionBackup
{
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public StorageExceptionFileDeletionDisabled()
	{
		super();
	}

	public StorageExceptionFileDeletionDisabled(final String message)
	{
		super(message);
	}

	public StorageExceptionFileDeletionDisabled(final Throwable cause)
	{
		super(cause);
	}

	public StorageExceptionFileDeletionDisabled(final String message, final Throwable cause)
	{
		super(message, cause);
	}

	public StorageExceptionFileDeletionDisabled(
		final String    message           ,
		final Throwable cause             ,
		final boolean   enableSuppression ,
		final boolean   writableStackTrace
	)
	{
		super(message, cause, enableSuppression, writableStackTrace);
	}
	
}

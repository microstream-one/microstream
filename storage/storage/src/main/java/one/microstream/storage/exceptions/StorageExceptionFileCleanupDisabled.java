package one.microstream.storage.exceptions;

public class StorageExceptionFileCleanupDisabled extends StorageException
{
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public StorageExceptionFileCleanupDisabled()
	{
		super();
	}

	public StorageExceptionFileCleanupDisabled(final String message)
	{
		super(message);
	}

	public StorageExceptionFileCleanupDisabled(final Throwable cause)
	{
		super(cause);
	}

	public StorageExceptionFileCleanupDisabled(final String message, final Throwable cause)
	{
		super(message, cause);
	}

	public StorageExceptionFileCleanupDisabled(
		final String    message           ,
		final Throwable cause             ,
		final boolean   enableSuppression ,
		final boolean   writableStackTrace
	)
	{
		super(message, cause, enableSuppression, writableStackTrace);
	}



}

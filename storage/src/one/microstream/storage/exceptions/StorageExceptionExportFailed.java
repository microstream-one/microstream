package one.microstream.storage.exceptions;

public class StorageExceptionExportFailed extends StorageException
{
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public StorageExceptionExportFailed()
	{
		super();
	}

	public StorageExceptionExportFailed(final String message)
	{
		super(message);
	}

	public StorageExceptionExportFailed(final Throwable cause)
	{
		super(cause);
	}

	public StorageExceptionExportFailed(final String message, final Throwable cause)
	{
		super(message, cause);
	}

	public StorageExceptionExportFailed(
		final String    message           ,
		final Throwable cause             ,
		final boolean   enableSuppression ,
		final boolean   writableStackTrace
	)
	{
		super(message, cause, enableSuppression, writableStackTrace);
	}



}

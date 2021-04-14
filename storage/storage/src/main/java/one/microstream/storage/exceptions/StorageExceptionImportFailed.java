package one.microstream.storage.exceptions;

public class StorageExceptionImportFailed extends StorageException
{
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public StorageExceptionImportFailed()
	{
		super();
	}

	public StorageExceptionImportFailed(final String message)
	{
		super(message);
	}

	public StorageExceptionImportFailed(final Throwable cause)
	{
		super(cause);
	}

	public StorageExceptionImportFailed(final String message, final Throwable cause)
	{
		super(message, cause);
	}

	public StorageExceptionImportFailed(
		final String    message           ,
		final Throwable cause             ,
		final boolean   enableSuppression ,
		final boolean   writableStackTrace
	)
	{
		super(message, cause, enableSuppression, writableStackTrace);
	}



}

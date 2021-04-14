package one.microstream.storage.exceptions;

public class StorageExceptionInitialization extends StorageException
{
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public StorageExceptionInitialization()
	{
		super();
	}

	public StorageExceptionInitialization(
		final String    message           ,
		final Throwable cause             ,
		final boolean   enableSuppression ,
		final boolean   writableStackTrace
	)
	{
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public StorageExceptionInitialization(final String message, final Throwable cause)
	{
		super(message, cause);
	}

	public StorageExceptionInitialization(final String message)
	{
		super(message);
	}

	public StorageExceptionInitialization(final Throwable cause)
	{
		super(cause);
	}
	
}

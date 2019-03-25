package one.microstream.storage.exceptions;

public class StorageExceptionInitializationFailed extends StorageExceptionInitialization
{
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public StorageExceptionInitializationFailed()
	{
		super();
	}

	public StorageExceptionInitializationFailed(final String message)
	{
		super(message);
	}

	public StorageExceptionInitializationFailed(final Throwable cause)
	{
		super(cause);
	}

	public StorageExceptionInitializationFailed(final String message, final Throwable cause)
	{
		super(message, cause);
	}

	public StorageExceptionInitializationFailed(
		final String    message           ,
		final Throwable cause             ,
		final boolean   enableSuppression ,
		final boolean   writableStackTrace
	)
	{
		super(message, cause, enableSuppression, writableStackTrace);
	}



}

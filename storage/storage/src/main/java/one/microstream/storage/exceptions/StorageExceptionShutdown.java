package one.microstream.storage.exceptions;

public class StorageExceptionShutdown extends StorageException
{
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public StorageExceptionShutdown()
	{
		super();
	}

	public StorageExceptionShutdown(
		final String    message           ,
		final Throwable cause             ,
		final boolean   enableSuppression ,
		final boolean   writableStackTrace
	)
	{
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public StorageExceptionShutdown(final String message, final Throwable cause)
	{
		super(message, cause);
	}

	public StorageExceptionShutdown(final String message)
	{
		super(message);
	}

	public StorageExceptionShutdown(final Throwable cause)
	{
		super(cause);
	}
	
}
